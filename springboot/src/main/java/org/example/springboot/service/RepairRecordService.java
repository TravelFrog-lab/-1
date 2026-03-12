package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.*;
import org.example.springboot.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

/**
 * 维修记录管理服务
 */
@Service
public class RepairRecordService extends ServiceImpl<RepairRecordMapper, RepairRecord> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RepairRecordService.class);
    
    @Resource
    private RepairRecordMapper repairRecordMapper;
    
    @Resource
    private OwnerMapper ownerMapper;
    
    @Resource
    private HouseMapper houseMapper;
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private MaintenanceStaffMapper maintenanceStaffMapper;

    @Resource
    private RepairTypeMapper repairTypeMapper;

    /**
     * 分页查询维修记录
     *
     * @param pageNum 页码
     * @param pageSize 每页大小

     * @param status 维修状态（可选）
     * @return 分页结果
     */
    public Result<Page<RepairRecord>> list(Integer pageNum, Integer pageSize, String applicantId, String applicantName, RepairRecord.Status status, String typeId,  String maintainerId) {
        LOGGER.info("Listing repair records, pageNum: {}, pageSize: {}, applicantId: {}, applicantName: {}, status: {}, typeId: {}",
                pageNum, pageSize, applicantId, applicantName, status, typeId);
        
        LambdaQueryWrapper<RepairRecord> wrapper = new LambdaQueryWrapper<>();

        if(StringUtils.isNotBlank(maintainerId)){
            wrapper.eq(RepairRecord::getMaintainerId, maintainerId);
        }

        if (typeId != null) {
           RepairType repairType = repairTypeMapper.selectById(typeId);
            if (repairType != null) {
                wrapper.eq(RepairRecord::getRepairTypeId, repairType.getId());
            }
        }
        if (StringUtils.isNotBlank(applicantId)) {
            wrapper.eq(RepairRecord::getApplicantId, applicantId);
        }
        if (StringUtils.isNotBlank(applicantName)) {
          List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().like(User::getName, applicantName));
               List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            if (userIds != null && !userIds.isEmpty()) {

                List<Owner> owners = ownerMapper.selectList(new LambdaQueryWrapper<Owner>().in(Owner::getUserId, userIds));
                List<Long> ownerIds = owners.stream().map(Owner::getId).collect(Collectors.toList());
                if(ownerIds != null && !ownerIds.isEmpty()){
                    wrapper.in(RepairRecord::getApplicantId, ownerIds);
                }else{
                    return Result.success(new Page<RepairRecord>(pageNum, pageSize));
                }
            }else{
                return Result.success(new Page<RepairRecord>(pageNum, pageSize));
            }
        }
        if (status != null) {
            wrapper.eq(RepairRecord::getStatus, status);
        }
        wrapper.orderByDesc(RepairRecord::getCreatedAt);
        
        Page<RepairRecord> page = new Page<>(pageNum, pageSize);
        Page<RepairRecord> resultPage = repairRecordMapper.selectPage(page, wrapper);
        // 填充关联信息
        resultPage.getRecords().forEach(this::fillAssociatedInfo);
        return Result.success(resultPage);
    }

    /**
     * 填充维修记录关联的业主、房屋和维修人员信息
     */
    private void fillAssociatedInfo(RepairRecord record) {
        Owner owner = ownerMapper.selectById(record.getApplicantId());

        if(owner!=null){
            Long userId = owner.getUserId();
            User user = userMapper.selectById(userId);
            owner.setUser(user);
            record.setApplicant(owner);
        }else{
            User nullUser = new User();
            nullUser.setName("未知用户");
            Owner nullOwner = new Owner();
            nullOwner.setUser(nullUser);
            record.setApplicant(nullOwner);
        }

        
        House house = houseMapper.selectById(record.getHouseId());
        record.setHouse(house);
        
        if (record.getMaintainerId() != null) {
            MaintenanceStaff maintainer = maintenanceStaffMapper.selectById(record.getMaintainerId());

            if(maintainer!=null){
                Long userId = maintainer.getUserId();
                User user = userMapper.selectById(userId);
                maintainer.setUser(user);
                record.setMaintainer(maintainer);
            }else{
                User nullUser = new User();
                nullUser.setName("未知用户");
                MaintenanceStaff nullMaintainer = new MaintenanceStaff();
                nullMaintainer.setUser(nullUser);
                record.setMaintainer(nullMaintainer);
            }
        }
        RepairType repairType = repairTypeMapper.selectById(record.getRepairTypeId());
        record.setRepairType(repairType);
    }

    /**
     * 根据ID查询维修记录
     *
     * @param id 记录ID
     * @return 记录详情
     */
    public Result<RepairRecord> getById(Long id) {
        LOGGER.info("Getting repair record by id: {}", id);
        RepairRecord record = repairRecordMapper.selectById(id);
        if (record == null) {
            return Result.error("-1", "维修记录不存在");
        }
        fillAssociatedInfo(record);//填充外键信息
        return Result.success(record);
    }

    /**
     * 添加维修记录
     *
     * @param record 维修记录信息
     * @return 操作结果
     */
    @Transactional
    public Result<?> add(RepairRecord record) {
        LOGGER.info("Adding new repair record: {}", record);
        // 检查业主是否存在
        Owner owner = ownerMapper.selectById(record.getApplicantId());
        if (owner == null) {
            return Result.error("-1", "业主不存在");
        }
        
        // 检查房屋是否存在
        House house = houseMapper.selectById(record.getHouseId());
        if (house == null) {
            return Result.error("-1", "房屋不存在");
        }
        
        // 设置初始状态
        record.setStatus(RepairRecord.Status.PENDING);
        record.setCreatedAt(LocalDateTime.now());
        // 初始支付状态
        record.setPayStatus(RepairRecord.PayStatus.UNPAID);
        
        repairRecordMapper.insert(record);
        return Result.success();
    }

    /**
     * 更新维修记录
     *
     * @param record 维修记录信息
     * @return 操作结果
     */
    @Transactional
    public Result<?> update(RepairRecord record) {
        LOGGER.info("Updating repair record: {}", record);
        RepairRecord original = repairRecordMapper.selectById(record.getId());
        if (original == null) {
            return Result.error("-1", "维修记录不存在");
        }
        
        // 检查状态变更是否合法
        if (!isValidStatusTransition(original.getStatus(), record.getStatus())) {
            return Result.error("-1", "非法的状态变更");
        }
        
        // 如果分配了维修人员，检查维修人员是否存在
        if (record.getMaintainerId() != null) {
            MaintenanceStaff maintainer = maintenanceStaffMapper.selectById(record.getMaintainerId());
            if (maintainer == null) {
                return Result.error("-1", "维修人员不存在");
            }
        }
        
        if (repairRecordMapper.updateById(record) <= 0) {
            return Result.error("-1", "维修记录不存在");
        }
        return Result.success();
    }

    /**
     * 检查状态变更是否合法
     * PENDING -> IN_PROGRESS
     * IN_PROGRESS -> COMPLETED/CANCELLED
     * COMPLETED/CANCELLED -> 不可变更
     *
     * @param from 原状态
     * @param to 目标状态
     * @return 是否合法
     */
    private boolean isValidStatusTransition(RepairRecord.Status from, RepairRecord.Status to) {
        if (from == to) {
            return true;
        }

        return switch (from) {
            case PENDING -> to == RepairRecord.Status.IN_PROGRESS;
            case IN_PROGRESS -> to == RepairRecord.Status.COMPLETED || to == RepairRecord.Status.CANCELLED;
            case COMPLETED, CANCELLED -> false;
            default -> false;
        };
    }

    /**
     * 删除维修记录
     * 只能删除已完成或已取消的记录
     *
     * @param id 记录ID
     * @return 操作结果
     */
    @Transactional
    public Result<?> delete(Long id) {
        LOGGER.info("Deleting repair record: {}", id);
        RepairRecord record = repairRecordMapper.selectById(id);
        if (record == null) {
            return Result.error("-1", "维修记录不存在");
        }
        
        // 只能删除已完成或已取消的记录
        if (record.getStatus() != RepairRecord.Status.COMPLETED 
                && record.getStatus() != RepairRecord.Status.CANCELLED) {
            return Result.error("-1", "只能删除已完成或已取消的维修记录");
        }

        if (repairRecordMapper.deleteById(id) <= 0) {
            return Result.error("-1", "维修记录不存在");
        }
        return Result.success();
    }

    /**
     * 批量删除维修记录
     * 只能删除已完成或已取消的记录
     *
     * @param ids 记录ID列表
     * @return 操作结果
     */
    @Transactional
    public Result<?> batchDelete(List<Long> ids) {
        LOGGER.info("Batch deleting repair records: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要删除的记录");
        }
        
        // 检查是否都是已完成或已取消的记录
        if (repairRecordMapper.selectCount(new LambdaQueryWrapper<RepairRecord>()
                .in(RepairRecord::getId, ids)
                .notIn(RepairRecord::getStatus, 
                    RepairRecord.Status.COMPLETED,
                    RepairRecord.Status.CANCELLED)) > 0) {
            return Result.error("-1", "只能删除已完成或已取消的维修记录");
        }

        repairRecordMapper.deleteBatchIds(ids);
        return Result.success();
    }

    /**
     * 维修人员接单
     * @param id 维修记录ID
     * @param maintainerId 维修人员ID
     * @return 操作结果
     */
    @Transactional
    public Result<?> takeRepairJob(Long id, Long maintainerId) {
        LOGGER.info("Maintainer {} taking repair job: {}", maintainerId, id);
        // 检查维修记录是否存在
        RepairRecord record = repairRecordMapper.selectById(id);
        if (record == null) {
            return Result.error("-1", "维修记录不存在");
        }
        // 检查维修人员是否存在
        MaintenanceStaff maintainer = maintenanceStaffMapper.selectById(maintainerId);
        if (maintainer == null) {
            return Result.error("-1", "维修人员不存在");
        }
        // 检查维修人员角色是否正确
        User user = userMapper.selectById(maintainer.getUserId());
        if (user == null || !User.Role.MAINTENANCE.name().equals(user.getRole())) {
            return Result.error("-1", "无效的维修人员");
        }
        // 检查维修记录状态
        if (record.getStatus() != RepairRecord.Status.PENDING) {
            return Result.error("-1", "该维修记录已被接单或已完成");
        }
        // 更新维修记录
        record.setMaintainerId(maintainerId);
        record.setStatus(RepairRecord.Status.IN_PROGRESS);
        
        if (repairRecordMapper.updateById(record) <= 0) {
            return Result.error("-1", "接单失败");
        }
        return Result.success();
    }

    /**
     * 查询待维修记录列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public Result<Page<RepairRecord>> listPendingRepairs(Integer pageNum, Integer pageSize) {
        LOGGER.info("Listing pending repair records, pageNum: {}, pageSize: {}", pageNum, pageSize);
        
        LambdaQueryWrapper<RepairRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairRecord::getStatus, RepairRecord.Status.PENDING)
              .orderByDesc(RepairRecord::getCreatedAt);
        
        Page<RepairRecord> page = new Page<>(pageNum, pageSize);
        Page<RepairRecord> resultPage = repairRecordMapper.selectPage(page, wrapper);
        resultPage.getRecords().forEach(this::fillAssociatedInfo);
        return Result.success(resultPage);
    }

    /**
     * 查询维修人员的维修记录
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param maintainerId 维修人员ID
     * @param status 维修状态（可选）
     * @return 分页结果
     */
    public Result<Page<RepairRecord>> listMaintainerRepairs(Integer pageNum, Integer pageSize, 
            Long maintainerId, RepairRecord.Status status) {
        LOGGER.info("Listing maintainer repair records, pageNum: {}, pageSize: {}, maintainerId: {}, status: {}", 
                pageNum, pageSize, maintainerId, status);
        
        LambdaQueryWrapper<RepairRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairRecord::getMaintainerId, maintainerId);
        if (status != null) {
            wrapper.eq(RepairRecord::getStatus, status);
        }
        wrapper.orderByDesc(RepairRecord::getCreatedAt);
        
        Page<RepairRecord> page = new Page<>(pageNum, pageSize);
        Page<RepairRecord> resultPage = repairRecordMapper.selectPage(page, wrapper);
        resultPage.getRecords().forEach(this::fillAssociatedInfo);
        return Result.success(resultPage);
    }

    /**
     * 分配维修人员
     * @param id 维修记录ID
     * @param maintainerId 维修人员ID
     * @return 操作结果
     */
    @Transactional
    public Result<?> assignMaintainer(Long id, Long maintainerId) {
        LOGGER.info("Assigning maintainer {} to repair record: {}", maintainerId, id);
        
        // 检查维修记录是否存在
        RepairRecord record = repairRecordMapper.selectById(id);
        if (record == null) {
            return Result.error("-1", "维修记录不存在");
        }
        
        // 检查维修人员是否存在
        MaintenanceStaff maintainer = maintenanceStaffMapper.selectById(maintainerId);
        if (maintainer == null) {
            return Result.error("-1", "维修人员不存在");
        }
        
        // 检查维修人员角色是否正确
        User user = userMapper.selectById(maintainer.getUserId());
        if (user == null || !User.Role.MAINTENANCE.name().equals(user.getRole())) {
            return Result.error("-1", "无效的维修人员");
        }
        
        // 检查维修记录状态
        if (record.getStatus() != RepairRecord.Status.PENDING) {
            return Result.error("-1", "只能为待处理的维修记录分配维修人员");
        }
        
        // 更新维修记录
        record.setMaintainerId(maintainerId);
        record.setStatus(RepairRecord.Status.IN_PROGRESS);
        
        if (repairRecordMapper.updateById(record) <= 0) {
            return Result.error("-1", "分配失败");
        }
        return Result.success();
    }

    /**
     * 完成维修
     * @param id 维修记录ID
     * @param resultDescription 维修结果描述
     * @return 操作结果
     */
    @Transactional
    public Result<?> completeRepair(Long id, String resultDescription, BigDecimal feeAmount) {
        LOGGER.info("Completing repair record: {}, result: {}, fee: {}", id, resultDescription, feeAmount);
        
        // 检查维修记录是否存在
        RepairRecord record = repairRecordMapper.selectById(id);
        if (record == null) {
            return Result.error("-1", "维修记录不存在");
        }
        
        // 检查维修记录状态
        if (record.getStatus() != RepairRecord.Status.IN_PROGRESS) {
            return Result.error("-1", "只能完成进行中的维修记录");
        }
        
        // 更新维修记录
        record.setStatus(RepairRecord.Status.COMPLETED);
        record.setResultDescription(resultDescription);
        record.setActualTime(LocalDateTime.now());
        if (feeAmount != null && feeAmount.compareTo(BigDecimal.ZERO) >= 0) {
            record.setFeeAmount(feeAmount);
            // 费用为 0 视为已支付，其它金额为待支付
            record.setPayStatus(
                    feeAmount.compareTo(BigDecimal.ZERO) == 0
                            ? RepairRecord.PayStatus.PAID
                            : RepairRecord.PayStatus.UNPAID
            );
        }
        
        if (repairRecordMapper.updateById(record) <= 0) {
            return Result.error("-1", "完成维修失败");
        }
        return Result.success();
    }

    /**
     * 标记维修费用已支付
     * @param id 维修记录ID
     * @return 操作结果
     */
    @Transactional
    public Result<?> markPaid(Long id) {
        LOGGER.info("Mark repair record as paid: {}", id);

        RepairRecord record = repairRecordMapper.selectById(id);
        if (record == null) {
            return Result.error("-1", "维修记录不存在");
        }
        if (record.getFeeAmount() == null || record.getFeeAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("-1", "该维修记录未设置收费金额");
        }
        if (record.getPayStatus() == RepairRecord.PayStatus.PAID) {
            return Result.error("-1", "该维修记录已支付");
        }

        record.setPayStatus(RepairRecord.PayStatus.PAID);
        if (repairRecordMapper.updateById(record) <= 0) {
            return Result.error("-1", "更新支付状态失败");
        }
        return Result.success();
    }

    /**
     * 业主编辑待处理的维修记录（仅报修类型和描述可修改）
     *
     * @param record 包含要更新的字段的维修记录
     * @return 操作结果
     */
    @Transactional
    public Result<?> editPendingRecord(RepairRecord record) {
        LOGGER.info("Editing pending repair record: {}", record);
        
        // 检查维修记录是否存在
        RepairRecord original = repairRecordMapper.selectById(record.getId());
        if (original == null) {
            return Result.error("-1", "维修记录不存在");
        }
        
        // 检查状态是否为待处理
        if (original.getStatus() != RepairRecord.Status.PENDING) {
            return Result.error("-1", "只能编辑待处理状态的维修记录");
        }
        
        // 检查维修类型是否存在
        if (record.getRepairTypeId() != null && !record.getRepairTypeId().equals(original.getRepairTypeId())) {
            RepairType repairType = repairTypeMapper.selectById(record.getRepairTypeId());
            if (repairType == null) {
                return Result.error("-1", "维修类型不存在");
            }
        }
        
        // 创建一个新对象，只包含允许修改的字段
        RepairRecord updatedRecord = new RepairRecord();
        updatedRecord.setId(record.getId());
        updatedRecord.setRepairTypeId(record.getRepairTypeId());
        updatedRecord.setDescription(record.getDescription());
        
        // 更新记录
        if (repairRecordMapper.updateById(updatedRecord) <= 0) {
            return Result.error("-1", "更新维修记录失败");
        }
        
        return Result.success();
    }
} 