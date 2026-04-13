package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.*;
import org.example.springboot.mapper.*;
import org.example.springboot.util.HouseTypeReferenceAreas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
public class OwnerService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OwnerService.class);
    
    @Resource
    private OwnerMapper ownerMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private HouseMapper houseMapper;
    
    @Resource
    private PropertyFeeMapper propertyFeeMapper;
    
    @Resource
    private RepairRecordMapper repairRecordMapper;
    
    @Resource
    private ComplaintMapper complaintMapper;
    
    @Resource
    private VolunteerRegistrationMapper volunteerRegistrationMapper;

    @Resource
    private HouseTypeMapper houseTypeMapper;

    @Resource
    private HousekeepingOrderMapper housekeepingOrderMapper;



    /**
     * 填充业主关联信息
     */
    private void syncHouseToOccupied(Long houseId) {
        if (houseId == null) return;
        House house = houseMapper.selectById(houseId);
        if (house == null) return;
        if (house.getStatus() != House.Status.OCCUPIED) {
            house.setStatus(House.Status.OCCUPIED);
            houseMapper.updateById(house);
        }
    }

    private void syncHouseToVacantIfNoOwner(Long houseId) {
        if (houseId == null) return;
        Long ownerCount = ownerMapper.selectCount(new LambdaQueryWrapper<Owner>()
                .eq(Owner::getHouseId, houseId));
        if (ownerCount != null && ownerCount > 0) return;
        House house = houseMapper.selectById(houseId);
        if (house == null) return;
        if (house.getStatus() != House.Status.VACANT) {
            house.setStatus(House.Status.VACANT);
            houseMapper.updateById(house);
        }
    }

    /**
     * 填充业主关联信息
     */
    private void fillOwnerAssociations(Owner owner) {
        if (owner.getUserId() != null) {
            owner.setUser(userMapper.selectById(owner.getUserId()));
        } else {
            owner.setUser(null);
        }
        if (owner.getHouseId() == null) {
            House nullHouse = new House();
            nullHouse.setAddress("暂无");
            nullHouse.setArea(BigDecimal.ZERO);
            HouseType nullHouseType = new HouseType();
            nullHouseType.setName("暂无信息");
            nullHouseType.setPropertyFee(BigDecimal.ZERO);
            nullHouseType.setReferenceArea(BigDecimal.ZERO);
            nullHouse.setHouseType(nullHouseType);
            owner.setHouse(nullHouse);
            return;
        }
        House house = houseMapper.selectById(owner.getHouseId());
        if(house != null){
            // house_type_id 为空时不能 selectById(null)，否则 MyBatis-Plus 会抛异常导致整接口 500
            HouseType houseType = null;
            if (house.getHouseTypeId() != null) {
                houseType = houseTypeMapper.selectById(house.getHouseTypeId());
            }
            if (houseType == null) {
                houseType = new HouseType();
                houseType.setName("暂无信息");
                houseType.setPropertyFee(BigDecimal.ZERO);
            } else {
                HouseTypeReferenceAreas.apply(houseType);
            }
            house.setHouseType(houseType);
            owner.setHouse(house);
        }else{
            House nullHouse = new House();
            nullHouse.setAddress("暂无");
            nullHouse.setArea(BigDecimal.ZERO);
            HouseType nullHouseType = new HouseType();
            nullHouseType.setName("暂无信息");
            nullHouseType.setPropertyFee(BigDecimal.ZERO);
            nullHouseType.setReferenceArea(BigDecimal.ZERO);
            nullHouse.setHouseType(nullHouseType);
            owner.setHouse(nullHouse);
        }

    }

    public Result<Owner> getByUserId(Long userId) {
        if (userId == null) {
            return Result.error("-1", "用户ID不能为空");
        }
        List<Owner> list = ownerMapper.selectList(
                new LambdaQueryWrapper<Owner>().eq(Owner::getUserId, userId).orderByDesc(Owner::getCreatedAt));
        if (list == null || list.isEmpty()) {
            return Result.error("-1", "业主不存在");
        }
        if (list.size() > 1) {
            LOGGER.warn("用户 userId={} 存在多条业主记录，取最新一条；请清理重复数据", userId);
        }
        Owner owner = list.get(0);
        fillOwnerAssociations(owner);
        return Result.success(owner);
    }
    


    /**
     * 检查业主是否有未完成的关联记录，并删除已完成的记录
     * @return 如果有未完成记录，返回错误信息；否则返回null
     */
    private String checkAndDeleteCompletedRecords(Long ownerId) {
        // 检查未缴纳的物业费
        LambdaQueryWrapper<PropertyFee> propertyFeeWrapper = new LambdaQueryWrapper<PropertyFee>()
                .eq(PropertyFee::getOwnerId, ownerId);
        if (propertyFeeMapper.selectCount(propertyFeeWrapper
                .eq(PropertyFee::getStatus, PropertyFee.Status.UNPAID)) > 0) {
            return "该业主存在未缴纳的物业费，无法删除";
        }
        // 删除已缴费的物业费记录
        propertyFeeMapper.delete(propertyFeeWrapper
                .eq(PropertyFee::getStatus, PropertyFee.Status.PAID));
        
        // 检查未完成的维修记录
        LambdaQueryWrapper<RepairRecord> repairWrapper = new LambdaQueryWrapper<RepairRecord>()
                .eq(RepairRecord::getApplicantId, ownerId);
        if (repairRecordMapper.selectCount(repairWrapper
                .in(RepairRecord::getStatus, 
                    RepairRecord.Status.PENDING,
                    RepairRecord.Status.IN_PROGRESS)) > 0) {
            return "该业主存在未完成的维修记录，无法删除";
        }
        // 删除已完成的维修记录
        repairRecordMapper.delete(repairWrapper
                .eq(RepairRecord::getStatus, RepairRecord.Status.COMPLETED));
        
        // 检查未完成的投诉记录
        LambdaQueryWrapper<Complaint> complaintWrapper = new LambdaQueryWrapper<Complaint>()
                .eq(Complaint::getComplainantId, ownerId);
        if (complaintMapper.selectCount(complaintWrapper
                .in(Complaint::getStatus,
                    Complaint.Status.PENDING,
                    Complaint.Status.PROCESSING)) > 0) {
            return "该业主存在未处理的投诉记录，无法删除";
        }
        // 删除已处理的投诉记录
        complaintMapper.delete(complaintWrapper
                .eq(Complaint::getStatus, Complaint.Status.RESOLVED));
        
        // 检查未完成的小区活动
        LambdaQueryWrapper<VolunteerRegistration> volunteerWrapper = new LambdaQueryWrapper<VolunteerRegistration>()
                .eq(VolunteerRegistration::getVolunteerId, ownerId);
        if (volunteerRegistrationMapper.selectCount(volunteerWrapper
                .eq(VolunteerRegistration::getStatus, VolunteerRegistration.Status.REGISTERED)) > 0) {
            return "该业主存在未完成的小区活动，无法删除";
        }
        // 删除已完成的小区活动记录
        volunteerRegistrationMapper.delete(volunteerWrapper
                .eq(VolunteerRegistration::getStatus, VolunteerRegistration.Status.REGISTERED));

        //检查是否存在家政订单
        LambdaQueryWrapper<HousekeepingOrder> housekeepingOrderWrapper = new LambdaQueryWrapper<HousekeepingOrder>()
                .eq(HousekeepingOrder::getOwnerId, ownerId).ne(HousekeepingOrder::getStatus, "COMPLETED").ne(HousekeepingOrder::getStatus, "CANCELLED");
        if (housekeepingOrderMapper.selectCount(housekeepingOrderWrapper) > 0) {
            return "该业主存在未完成或未取消的家政订单，无法删除";
        }
       
        //删除已完成的家政订单
        housekeepingOrderMapper.delete(housekeepingOrderWrapper);
        
        
        return null;
    }

    public Page<Owner> list(Integer pageNum, Integer pageSize, String ownerName, String houseAddress, String phone) {
        LOGGER.info("Listing owners, pageNum: {}, pageSize: {}, ownerName: {}, houseAddress: {}, phone: {}",
                pageNum, pageSize, ownerName, houseAddress, phone);

        boolean filterUser = (ownerName != null && !ownerName.trim().isEmpty())
                || (phone != null && !phone.trim().isEmpty());
        if (filterUser) {
            LambdaQueryWrapper<User> uw = new LambdaQueryWrapper<>();
            uw.eq(User::getRole, User.Role.OWNER);
            if (ownerName != null && !ownerName.trim().isEmpty()) {
                uw.like(User::getName, ownerName.trim());
            }
            if (phone != null && !phone.trim().isEmpty()) {
                uw.like(User::getPhone, phone.trim());
            }
            List<Long> userIds = userMapper.selectList(uw).stream().map(User::getId).collect(Collectors.toList());
            if (userIds.isEmpty()) {
                return emptyOwnerPage(pageNum, pageSize);
            }
            LambdaQueryWrapper<Owner> wrapper = new LambdaQueryWrapper<Owner>().in(Owner::getUserId, userIds);
            return finishOwnerPage(pageNum, pageSize, wrapper, houseAddress);
        }

        LambdaQueryWrapper<Owner> wrapper = new LambdaQueryWrapper<>();
        return finishOwnerPage(pageNum, pageSize, wrapper, houseAddress);
    }

    private Page<Owner> finishOwnerPage(int pageNum, int pageSize, LambdaQueryWrapper<Owner> wrapper, String houseAddress) {
        if (houseAddress != null && !houseAddress.trim().isEmpty()) {
            List<Long> houseIds = houseMapper.selectList(
                    new LambdaQueryWrapper<House>().like(House::getAddress, houseAddress.trim())
            ).stream().map(House::getId).collect(Collectors.toList());
            if (houseIds.isEmpty()) {
                return emptyOwnerPage(pageNum, pageSize);
            }
            wrapper.in(Owner::getHouseId, houseIds);
        }
        wrapper.orderByDesc(Owner::getCreatedAt);
        Page<Owner> page = new Page<>(pageNum, pageSize);
        Page<Owner> resultPage = ownerMapper.selectPage(page, wrapper);
        resultPage.getRecords().forEach(this::fillOwnerAssociations);
        return resultPage;
    }

    private static Page<Owner> emptyOwnerPage(int pageNum, int pageSize) {
        Page<Owner> p = new Page<>(pageNum, pageSize);
        p.setTotal(0);
        p.setRecords(Collections.emptyList());
        return p;
    }


    public Result<Owner> getById(Long id) {
        LOGGER.info("Getting owner by id: {}", id);
        Owner owner = ownerMapper.selectById(id);
        if (owner == null) {
            return Result.error("-1", "业主不存在");
        }
        fillOwnerAssociations(owner);
        return Result.success(owner);
    }

    @Transactional
    public Result<?> add(Owner owner) {
        LOGGER.info("Adding new owner: {}", owner);
        // 检查用户是否存在且角色是否正确
        User user = userMapper.selectById(owner.getUserId());
        if (user == null) {
            return Result.error("-1", "关联的用户不存在");
        }
        if (!User.Role.OWNER.name().equals(user.getRole())) {
            return Result.error("-1", "用户角色必须是业主");
        }
        // 检查用户是否已经被其他业主关联
        if (ownerMapper.selectCount(new LambdaQueryWrapper<Owner>()
                .eq(Owner::getUserId, owner.getUserId())) > 0) {
            return Result.error("-1", "该用户已经被其他业主关联");
        }
        // 检查房屋是否存在
        House house = houseMapper.selectById(owner.getHouseId());
        if (house == null) {
            return Result.error("-1", "房屋不存在");
        }
        // 检查房屋是否已被占用
        if (ownerMapper.selectCount(new LambdaQueryWrapper<Owner>()
                .eq(Owner::getHouseId, owner.getHouseId())) > 0) {
            return Result.error("-1", "该房屋已有业主居住");
        }
        ownerMapper.insert(owner);
        // 业主入住房屋后，联动房屋状态为已入住
        syncHouseToOccupied(owner.getHouseId());
        return Result.success();
    }

    @Transactional
    public Result<?> update(Long id, Owner owner) {
        LOGGER.info("Updating owner: {}", owner);
        Owner old = ownerMapper.selectById(id);
        if(old == null){
            return Result.error("-1", "业主不存在");
        }
        
        User newUser = owner.getUser();
        if (newUser == null) {
            return Result.error("-1", "用户信息不能为空");
        }
        
        // 检查用户是否已经被其他业主关联
        LambdaQueryWrapper<Owner> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(Owner::getUserId, owner.getUserId())
                  .ne(Owner::getId, owner.getId());
        if (ownerMapper.selectCount(userWrapper) > 0) {
            return Result.error("-1", "该用户已经被其他业主关联");
        }
        
        // 检查房屋是否存在
        House house = houseMapper.selectById(owner.getHouseId());
        if (house == null) {
            return Result.error("-1", "房屋不存在");
        }
        
        // 检查房屋是否已被其他业主占用
        LambdaQueryWrapper<Owner> houseWrapper = new LambdaQueryWrapper<>();
        houseWrapper.eq(Owner::getHouseId, owner.getHouseId())
                   .ne(Owner::getId, owner.getId());
        if (ownerMapper.selectCount(houseWrapper) > 0) {
            return Result.error("-1", "该房屋已有其他业主居住");
        }
        
        if (ownerMapper.updateById(owner) <= 0) {
            return Result.error("-1", "更新失败");
        }
        if(userMapper.updateById(newUser) <= 0){
            return Result.error("-1", "用户不存在");
        }
        // 若换房：新房标记已入住，旧房在无业主时回空置；未换房则确保已入住
        Long oldHouseId = old.getHouseId();
        Long newHouseId = owner.getHouseId();
        syncHouseToOccupied(newHouseId);
        if (oldHouseId != null && !oldHouseId.equals(newHouseId)) {
            syncHouseToVacantIfNoOwner(oldHouseId);
        }
        return Result.success();
    }




    @Transactional
    public Result<?> delete(Long id) {
        LOGGER.info("Deleting owner: {}", id);
        
        Owner owner = ownerMapper.selectById(id);
        if (owner == null) {
            return Result.error("-1", "业主不存在");
        }
        
        String errorMsg = checkAndDeleteCompletedRecords(id);
        if (errorMsg != null) {
            return Result.error("-1", errorMsg);
        }



        if (ownerMapper.deleteById(id)<=0) {
            return Result.error("-1", "删除业主失败");
        }
        // 删除业主后，若房屋无其他业主，回写为空置
        syncHouseToVacantIfNoOwner(owner.getHouseId());
        return Result.success();
    }

    @Transactional
    public Result<?> batchDelete(List<Long> ids) {
        LOGGER.info("Batch deleting owners: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要删除的记录");
        }
        
        for (Long id : ids) {
            String errorMsg = checkAndDeleteCompletedRecords(id);
            if (errorMsg != null) {
                return Result.error("-1", "部分业主存在未完成的记录，无法删除");
            }
        }

        List<Owner> ownerList = ownerMapper.selectBatchIds(ids);
        if (ownerList.isEmpty()) {
            return Result.error("-1", "未找到要删除的业主记录");
        }
        ownerMapper.deleteByIds(ids);
        // 批量删除后回写房屋状态
        for (Owner owner : ownerList) {
            syncHouseToVacantIfNoOwner(owner.getHouseId());
        }
        return Result.success();
    }
} 