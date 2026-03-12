package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Complaint;
import org.example.springboot.entity.MaintenanceStaff;
import org.example.springboot.entity.RepairRecord;
import org.example.springboot.entity.User;
import org.example.springboot.mapper.MaintenanceStaffMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.mapper.RepairRecordMapper;
import org.example.springboot.mapper.ComplaintMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaintenanceStaffService extends ServiceImpl<MaintenanceStaffMapper, MaintenanceStaff> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceStaffService.class);
    
    @Resource
    private MaintenanceStaffMapper maintenanceStaffMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private RepairRecordMapper repairRecordMapper;
    
    @Resource
    private ComplaintMapper complaintMapper;

    public Page<MaintenanceStaff> list(Integer pageNum, Integer pageSize) {
        LOGGER.info("Listing maintenance staff, pageNum: {}, pageSize: {}", pageNum, pageSize);
        Page<MaintenanceStaff> page = page(new Page<>(pageNum, pageSize));
        // 填充用户信息
        page.getRecords().forEach(staff -> {

            User user = userMapper.selectById(staff.getUserId());
            staff.setUser(user);
        });
        return page;
    }


    public Result<MaintenanceStaff> getById(Long id) {
        LOGGER.info("Getting maintenance staff by id: {}", id);
        MaintenanceStaff staff = maintenanceStaffMapper.selectById(id);
        if (staff == null) {
            return Result.error("-1", "后勤人员不存在");
        }
        User user = userMapper.selectById(staff.getUserId());
        staff.setUser(user);
        return Result.success(staff);
    }

    @Transactional
    public Result<?> add(MaintenanceStaff staff) {
        LOGGER.info("Adding new maintenance staff: {}", staff);
        // 检查用户是否存在且角色是否正确
        User user = userMapper.selectById(staff.getUserId());
        if (user == null) {
            return Result.error("-1", "关联的用户不存在");
        }
        if (!User.Role.MAINTENANCE.name().equals(user.getRole())) {
            return Result.error("-1", "用户角色必须是后勤人员");
        }
        
        // 检查是否已存在该用户的后勤人员记录
        if (exists(staff.getUserId())) {
            return Result.error("-1", "该用户已是后勤人员");
        }
        
        save(staff);
        return Result.success();
    }

    private boolean exists(Long userId) {
        return count(new LambdaQueryWrapper<MaintenanceStaff>()
                .eq(MaintenanceStaff::getUserId, userId)) > 0;
    }

    @Transactional
    public Result<?> update(MaintenanceStaff staff) {
        LOGGER.info("Updating maintenance staff: {}", staff);
        if (!updateById(staff)) {
            return Result.error("-1", "后勤人员不存在");
        }
        return Result.success();
    }

    @Transactional
    public Result<?> delete(Long id) {
        LOGGER.info("Deleting maintenance staff: {}", id);
        
        MaintenanceStaff staff = maintenanceStaffMapper.selectById(id);
        if (staff == null) {
            return Result.error("-1", "后勤人员不存在");
        }
        
        // 检查是否有未完成的维修记录
        if (repairRecordMapper.selectCount(new LambdaQueryWrapper<RepairRecord>()
                .eq(RepairRecord::getMaintainerId, id)
                .in(RepairRecord::getStatus, 
                    RepairRecord.Status.IN_PROGRESS)) > 0) {
            return Result.error("-1", "该后勤人员存在未完成的维修工作，无法删除");
        }
        
        // 检查是否有未处理的投诉
        if (complaintMapper.selectCount(new LambdaQueryWrapper<Complaint>()
                .eq(Complaint::getHandlerId, id)
                .in(Complaint::getStatus,
                    Complaint.Status.PROCESSING)) > 0) {
            return Result.error("-1", "该后勤人员存在未处理完成的投诉，无法删除");
        }
        
        // 检查是否有已完成的维修记录（可选，取决于业务需求）
        if (repairRecordMapper.selectCount(new LambdaQueryWrapper<RepairRecord>()
                .eq(RepairRecord::getMaintainerId, id)) > 0) {
            return Result.error("-1", "该后勤人员存在维修记录，无法删除");
        }


        if (!removeById(id)) {
            return Result.error("-1", "后勤人员不存在");
        }
        return Result.success();
    }

    @Transactional
    public Result<?> batchDelete(List<Long> ids) {
        LOGGER.info("Batch deleting maintenance staff: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要删除的记录");
        }
        
        // 获取所有要删除的后勤人员记录
        List<MaintenanceStaff> staffList = maintenanceStaffMapper.selectBatchIds(ids);
        if (staffList.isEmpty()) {
            return Result.error("-1", "未找到要删除的后勤人员记录");
        }
        
        // 检查是否有未完成的维修记录
        if (repairRecordMapper.selectCount(new LambdaQueryWrapper<RepairRecord>()
                .in(RepairRecord::getMaintainerId, ids)
                .in(RepairRecord::getStatus, 
                    RepairRecord.Status.IN_PROGRESS)) > 0) {
            return Result.error("-1", "选中的后勤人员中存在未完成维修工作的人员，无法删除");
        }
        
        // 检查是否有未处理的投诉
        if (complaintMapper.selectCount(new LambdaQueryWrapper<Complaint>()
                .in(Complaint::getHandlerId, ids)
                .in(Complaint::getStatus,
                    Complaint.Status.PROCESSING)) > 0) {
            return Result.error("-1", "选中的后勤人员中存在未处理完成投诉的人员，无法删除");
        }

        // 删除关联的用户记录
        List<Long> userIds = staffList.stream()
                .map(MaintenanceStaff::getUserId)
                .collect(Collectors.toList());
        if (userMapper.deleteBatchIds(userIds) <= 0) {
            return Result.error("-1", "批量删除用户信息失败");
        }

        removeBatchByIds(ids);
        return Result.success();
    }
    public Result<?> getByUserId(Long userId) {
        List<MaintenanceStaff> staff = maintenanceStaffMapper.selectList(new LambdaQueryWrapper<MaintenanceStaff>().eq(MaintenanceStaff::getUserId, userId));
        if(staff == null || staff.isEmpty()) {
            return Result.success();
        }
        return Result.success(staff.get(0));

    }
} 