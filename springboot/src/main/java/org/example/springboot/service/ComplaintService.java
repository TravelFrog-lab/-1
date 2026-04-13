package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Complaint;
import org.example.springboot.entity.MaintenanceStaff;
import org.example.springboot.entity.Owner;
import org.example.springboot.entity.User;
import org.example.springboot.mapper.ComplaintMapper;
import org.example.springboot.mapper.MaintenanceStaffMapper;
import org.example.springboot.mapper.OwnerMapper;
import org.example.springboot.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 投诉建议管理服务
 */
@Service
public class ComplaintService extends ServiceImpl<ComplaintMapper, Complaint> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ComplaintService.class);
    
    @Resource
    private ComplaintMapper complaintMapper;
    
    @Resource
    private OwnerMapper ownerMapper;
    
    @Resource
    private UserMapper userMapper;

    /**
     * 分页查询投诉记录
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param status 处理状态（可选）
     * @return 分页结果
     */
    public Result<Page<Complaint>> list(Integer pageNum, Integer pageSize,String complainantId, Complaint.Status status,String complainantName) {
        LOGGER.info("Listing complaints, pageNum: {}, pageSize: {}, status: {}", 
                pageNum, pageSize, status);
        
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(complainantId)){
            wrapper.eq(Complaint::getComplainantId, complainantId);
        }
        if(StringUtils.isNotBlank(complainantName)){
            List<Long> userIds = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .like(User::getName, complainantName))
                    .stream()
                    .map(User::getId)
                    .toList();
            if(userIds.isEmpty()){
                return Result.success(new Page<>());
            }
            List<Long> ownerIds = ownerMapper.selectList(new LambdaQueryWrapper<Owner>()
                    .in(Owner::getUserId, userIds))
                    .stream()
                    .map(Owner::getId)
                    .toList();
            if(ownerIds.isEmpty()){
                return Result.success(new Page<>());
            }
            
            wrapper.in(Complaint::getComplainantId, ownerIds);



        }
        if (status != null) {
            wrapper.eq(Complaint::getStatus, status);
        }


        wrapper.orderByDesc(Complaint::getCreatedAt);
        
        Page<Complaint> page = new Page<>(pageNum, pageSize);
        Page<Complaint> resultPage = complaintMapper.selectPage(page, wrapper);
        
        // 填充关联信息
        resultPage.getRecords().forEach(this::fillAssociatedInfo);
        return Result.success(resultPage);
    }

    /**
     * 填充投诉记录关联的投诉人和处理人信息
     */
    private void fillAssociatedInfo(Complaint complaint) {
        Owner complainant = ownerMapper.selectById(complaint.getComplainantId());
        if(complainant!=null){
            Long userId = complainant.getUserId();
            User user = userMapper.selectById(userId);
            complainant.setUser(user);
            complaint.setComplainant(complainant);
        }else{
            User nullUser = new User();
            nullUser.setName("未知用户");
            Owner nullOwner = new Owner();
            nullOwner.setUser(nullUser);
            complaint.setComplainant(nullOwner);
        }
        
        if (complaint.getHandlerId() != null) {
            User handler = userMapper.selectById(complaint.getHandlerId());
            complaint.setHandler(handler);
        }
    }

    /**
     * 根据ID查询投诉记录
     *
     * @param id 记录ID
     * @return 记录详情
     */
    public Result<Complaint> getById(Long id) {
        LOGGER.info("Getting complaint by id: {}", id);
        Complaint complaint = complaintMapper.selectById(id);
        if (complaint == null) {
            return Result.error("-1", "投诉记录不存在");
        }
        fillAssociatedInfo(complaint);
        return Result.success(complaint);
    }

    /**
     * 添加投诉记录
     *
     * @param complaint 投诉信息
     * @return 操作结果
     */
    @Transactional
    public Result<?> add(Complaint complaint) {
        LOGGER.info("Adding new complaint: {}", complaint);
        
        // 检查投诉人是否存在
        Owner complainant = ownerMapper.selectById(complaint.getComplainantId());
        if (complainant == null) {
            return Result.error("-1", "投诉人不存在");
        }
        
        // 设置初始状态和时间
        complaint.setStatus(Complaint.Status.PENDING);
        if (complaint.getComplaintType() == null || complaint.getComplaintType().isBlank()) {
            complaint.setComplaintType("OTHER");
        }
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());
        
        complaintMapper.insert(complaint);
        return Result.success();
    }

    /**
     * 更新投诉记录
     *
     * @param complaint 投诉信息
     * @return 操作结果
     */
    @Transactional
    public Result<?> update(Complaint complaint) {
        LOGGER.info("Updating complaint: {}", complaint);
        Complaint original = complaintMapper.selectById(complaint.getId());
        if (original == null) {
            return Result.error("-1", "投诉记录不存在");
        }
        
        // 检查状态变更是否合法
        if (!isValidStatusTransition(original.getStatus(), complaint.getStatus())) {
            return Result.error("-1", "非法的状态变更");
        }
        
        // 如果分配了处理人，检查处理人是否存在
        if (complaint.getHandlerId() != null) {
            User handler = userMapper.selectById(complaint.getHandlerId());
            if (handler == null) {
                return Result.error("-1", "处理人不存在");
            }
        }
        
        // 如果状态变为已处理，设置处理时间
        if (complaint.getStatus() == Complaint.Status.RESOLVED) {
            complaint.setHandleTime(LocalDateTime.now());
        }
        
        complaint.setUpdatedAt(LocalDateTime.now());
        
        if (complaintMapper.updateById(complaint) <= 0) {
            return Result.error("-1", "投诉记录不存在");
        }
        return Result.success();
    }

    /**
     * 检查状态变更是否合法
     * PENDING -> PROCESSING
     * PROCESSING -> RESOLVED
     * RESOLVED -> 不可变更
     *
     * @param from 原状态
     * @param to 目标状态
     * @return 是否合法
     */
    private boolean isValidStatusTransition(Complaint.Status from, Complaint.Status to) {
        if (from == to) {
            return true;
        }

        return switch (from) {
            case PENDING -> to == Complaint.Status.PROCESSING||to == Complaint.Status.RESOLVED;
            case PROCESSING -> to == Complaint.Status.RESOLVED;
            case RESOLVED -> false;
        };
    }

    /**
     * 删除投诉记录
     * 允许删除：待处理、已处理
     */
    @Transactional
    public Result<?> delete(Long id) {
        LOGGER.info("Deleting complaint: {}", id);
        Complaint complaint = complaintMapper.selectById(id);
        if (complaint == null) {
            return Result.error("-1", "投诉记录不存在");
        }
        
        // 仅允许删除待处理或已处理；处理中不可删除，避免流程中断
        if (complaint.getStatus() != Complaint.Status.PENDING
                && complaint.getStatus() != Complaint.Status.RESOLVED) {
            return Result.error("-1", "仅待处理或已处理的投诉可删除");
        }

        if (complaintMapper.deleteById(id) <= 0) {
            return Result.error("-1", "投诉记录不存在");
        }
        return Result.success();
    }

    /**
     * 批量删除投诉记录
     * 允许删除：待处理、已处理
     */
    @Transactional
    public Result<?> batchDelete(List<Long> ids) {
        LOGGER.info("Batch deleting complaints: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要删除的记录");
        }
        
        // 检查是否都可删除（待处理或已处理）
        if (complaintMapper.selectCount(new LambdaQueryWrapper<Complaint>()
                .in(Complaint::getId, ids)
                .notIn(Complaint::getStatus, Complaint.Status.PENDING, Complaint.Status.RESOLVED)) > 0) {
            return Result.error("-1", "仅待处理或已处理的投诉可删除");
        }

        complaintMapper.deleteBatchIds(ids);
        return Result.success();
    }

    /**
     * 业主编辑待处理的投诉记录（仅投诉内容可修改）
     *
     * @param complaint 包含要更新的字段的投诉记录
     * @return 操作结果
     */
    @Transactional
    public Result<?> editPendingComplaint(Complaint complaint) {
        LOGGER.info("Editing pending complaint: {}", complaint);
        
        // 检查投诉记录是否存在
        Complaint original = complaintMapper.selectById(complaint.getId());
        if (original == null) {
            return Result.error("-1", "投诉记录不存在");
        }
        
        // 检查状态是否为待处理
        if (original.getStatus() != Complaint.Status.PENDING) {
            return Result.error("-1", "只能编辑待处理状态的投诉记录");
        }
        
        // 创建一个新对象，只包含允许修改的字段
        Complaint updatedComplaint = new Complaint();
        updatedComplaint.setId(complaint.getId());
        updatedComplaint.setContent(complaint.getContent());
        updatedComplaint.setUpdatedAt(LocalDateTime.now());
        
        // 更新记录
        if (complaintMapper.updateById(updatedComplaint) <= 0) {
            return Result.error("-1", "更新投诉记录失败");
        }
        
        return Result.success();
    }
}