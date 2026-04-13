package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.dto.PropertyFeePushResult;
import org.example.springboot.entity.*;
import org.example.springboot.mapper.*;
import org.example.springboot.util.HouseTypeReferenceAreas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物业费管理服务
 */
@Service
public class PropertyFeeService extends ServiceImpl<PropertyFeeMapper, PropertyFee> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyFeeService.class);
    
    @Resource
    private PropertyFeeMapper propertyFeeMapper;
    
    @Resource
    private OwnerMapper ownerMapper;
    
    @Resource
    private HouseMapper houseMapper;

    @Resource
    private UserMapper userMapper;
    
    @Resource
    private HouseTypeMapper houseTypeMapper;
    



    /**
     * 检查物业费支付状态
     * @param orderNo 订单编号
     * @return 支付状态
     */
    public Result<?> checkPayStatus(String orderNo) {
        LambdaQueryWrapper<PropertyFee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PropertyFee::getOrderNo, orderNo);
        PropertyFee propertyFee = propertyFeeMapper.selectOne(wrapper);
        if (propertyFee == null) {
            return Result.error("-1", "物业费记录不存在");
        }
        return Result.success(propertyFee.getStatus());
    }

    /**
     * 分页查询物业费记录
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param ownerId 业主id（可选）
     * @param status 缴费状态（可选）
     * @param feeDate 物业费所属年月（可选）
     * @return 分页结果
     */
    public Result<Page<PropertyFee>> list(Integer pageNum, Integer pageSize, String ownerId, 
            PropertyFee.Status status, String ownerName, String location, LocalDate feeDate) {
        LOGGER.info("Listing property fees, pageNum: {}, pageSize: {}, ownerName: {}, status: {}, feeDate: {}", 
                pageNum, pageSize, ownerId, status, feeDate);
        
        LambdaQueryWrapper<PropertyFee> wrapper = new LambdaQueryWrapper<>();
        
        if(StringUtils.isNotBlank(ownerName)){
            List<Long> userIds   =  userMapper.selectList(new LambdaQueryWrapper<User>()
                        .like(User::getName, ownerName))
                        .stream()
                        .map(User::getId)
                        .toList();
                if(userIds.isEmpty()){
                    return Result.success(new Page<>(pageNum, pageSize));
                }
                List<Long> ownerIds = ownerMapper.selectList(new LambdaQueryWrapper<Owner>()
                        .in(Owner::getUserId, userIds))
                        .stream()
                        .map(Owner::getId)
                        .toList();
                if(ownerIds.isEmpty()){
                    return Result.success(new Page<>(pageNum, pageSize));
                }
                wrapper.in(PropertyFee::getOwnerId, ownerIds);
            }
            if(StringUtils.isNotBlank(location)){
                List<Long> houseIds = houseMapper.selectList(new LambdaQueryWrapper<House>()    
                        .like(House::getAddress, location))
                        .stream()
                        .map(House::getId)
                        .toList();
                if(houseIds.isEmpty()){
                    return Result.success(new Page<>(pageNum, pageSize));
                }
                wrapper.in(PropertyFee::getHouseId, houseIds);
            }
        
         
            // List<Long> ownerIds = null;
            // if (StringUtils.isNotBlank(ownerId)) {
            //     ownerIds = ownerMapper.selectList(new LambdaQueryWrapper<Owner>()
            //             .like(Owner::getId, ownerId))
            //             .stream()
            //             .map(Owner::getId)
            //             .toList();
            //     if (ownerIds.isEmpty()) {
            //         return Result.success(new Page<>(pageNum, pageSize));
            //     }
            // }
            

            if (ownerId != null) {
                wrapper.in(PropertyFee::getOwnerId, ownerId);
            }
            if (status != null) {
                wrapper.eq(PropertyFee::getStatus, status);
            }
            if (feeDate != null) {
                wrapper.eq(PropertyFee::getFeeDate, feeDate);
            }
            
            wrapper.orderByDesc(PropertyFee::getCreatedAt);
            
            Page<PropertyFee> page = new Page<>(pageNum, pageSize);
            Page<PropertyFee> resultPage = propertyFeeMapper.selectPage(page, wrapper);
            // 填充关联信息
            resultPage.getRecords().forEach(this::fillAssociatedInfo);
            return Result.success(resultPage);
    }

    /**
     * 填充物业费记录关联的业主和房屋信息
     */
    private void fillAssociatedInfo(PropertyFee propertyFee) {
        Owner owner = ownerMapper.selectById(propertyFee.getOwnerId());

        if(owner!=null){
            Long userId = owner.getUserId();
            User user = userMapper.selectById(userId);
            owner.setUser(user);
            propertyFee.setOwner(owner);
        }else{
            User nullUser = new User();
            nullUser.setName("未知用户");
            Owner nullOwner = new Owner();
            nullOwner.setUser(nullUser);
            propertyFee.setOwner(nullOwner);
        }
        House house = houseMapper.selectById(propertyFee.getHouseId());
        if (house != null) {
            propertyFee.setHouse(house);
            HouseType houseType = houseTypeMapper.selectById(house.getHouseTypeId());
            HouseTypeReferenceAreas.apply(houseType);
            house.setHouseType(houseType);
        }
    }

    /**
     * 根据ID查询物业费记录
     *
     * @param id 记录ID
     * @return 记录详情
     */
    public Result<PropertyFee> getById(Long id) {
        LOGGER.info("Getting property fee by id: {}", id);
        PropertyFee propertyFee = propertyFeeMapper.selectById(id);
        if (propertyFee == null) {
            return Result.error("-1", "物业费记录不存在");
        }
        fillAssociatedInfo(propertyFee);
        return Result.success(propertyFee);
    }

    /**
     * 添加物业费记录
     *
     * @param propertyFee 物业费信息
     * @return 操作结果
     */
    @Transactional
    public Result<?> add(PropertyFee propertyFee) {
        LOGGER.info("Adding new property fee: {}", propertyFee);
        // 检查业主是否存在
        Owner owner = ownerMapper.selectById(propertyFee.getOwnerId());
        if (owner == null) {
            return Result.error("-1", "业主不存在");
        }
        
        // 检查房屋是否存在
        House house = houseMapper.selectById(propertyFee.getHouseId());
        if (house == null) {
            return Result.error("-1", "房屋不存在");
        }
        

        
        // 检查是否有未缴费的记录
        if (propertyFeeMapper.selectCount(new LambdaQueryWrapper<PropertyFee>()
                .eq(PropertyFee::getHouseId, propertyFee.getHouseId())
                .eq(PropertyFee::getStatus, PropertyFee.Status.UNPAID)) > 0) {
            return Result.error("-1", "该房屋存在未缴费的物业费记录");
        }
        
        // 设置初始状态
        propertyFee.setStatus(PropertyFee.Status.UNPAID);
        propertyFee.setCreatedAt(LocalDateTime.now());
        if (propertyFee.getOrderNo() == null || propertyFee.getOrderNo().trim().isEmpty()) {
            propertyFee.setOrderNo(generateOrderNo());
        }
        propertyFeeMapper.insert(propertyFee);
        return Result.success();
    }

    /**
     * 更新物业费记录
     *
     * @param propertyFee 物业费信息
     * @return 操作结果
     */
    @Transactional
    public Result<?> update(PropertyFee propertyFee) {
        LOGGER.info("Updating property fee: {}", propertyFee);
        PropertyFee original = propertyFeeMapper.selectById(propertyFee.getId());
        if (original == null) {
            return Result.error("-1", "物业费记录不存在");
        }
        
        // 如果已缴费，不允许修改
        if (original.getStatus() == PropertyFee.Status.PAID) {
            return Result.error("-1", "已缴费的记录不能修改");
        }
        
        if (propertyFeeMapper.updateById(propertyFee) <= 0) {
            return Result.error("-1", "物业费记录不存在");
        }
        return Result.success();
    }

    /**
     * 删除物业费记录
     * 只能删除已缴费的记录
     *
     * @param id 记录ID
     * @return 操作结果
     */
    @Transactional
    public Result<?> delete(Long id) {
        LOGGER.info("Deleting property fee: {}", id);
        PropertyFee propertyFee = propertyFeeMapper.selectById(id);
        if (propertyFee == null) {
            return Result.error("-1", "物业费记录不存在");
        }
        
        // 只能删除已缴费的记录
        if (propertyFee.getStatus() != PropertyFee.Status.PAID) {
            return Result.error("-1", "只能删除已缴费的记录");
        }

        if (propertyFeeMapper.deleteById(id) <= 0) {
            return Result.error("-1", "物业费记录不存在");
        }
        return Result.success();
    }

    /**
     * 批量删除物业费记录
     * 只能删除已缴费的记录
     *
     * @param ids 记录ID列表
     * @return 操作结果
     */
    @Transactional
    public Result<?> batchDelete(List<Long> ids) {
        LOGGER.info("Batch deleting property fees: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要删除的记录");
        }
        
        // 检查是否都是已缴费的记录
        if (propertyFeeMapper.selectCount(new LambdaQueryWrapper<PropertyFee>()
                .in(PropertyFee::getId, ids)
                .ne(PropertyFee::getStatus, PropertyFee.Status.PAID)) > 0) {
            return Result.error("-1", "只能删除已缴费的记录");
        }

        propertyFeeMapper.deleteBatchIds(ids);
        return Result.success();
    }

    /**
     * 缴费
     *
     * @param id 记录ID
     * @return 操作结果
     */
    @Transactional
    public Result<?> pay(Long id) {
        LOGGER.info("Paying property fee: {}", id);
        PropertyFee propertyFee = propertyFeeMapper.selectById(id);
        if (propertyFee == null) {
            return Result.error("-1", "物业费记录不存在");
        }
        
        if (propertyFee.getStatus() == PropertyFee.Status.PAID) {
            return Result.error("-1", "该物业费已缴纳");
        }
        
        propertyFee.setStatus(PropertyFee.Status.PAID);
        propertyFee.setPaymentTime(LocalDateTime.now());
        
        propertyFeeMapper.updateById(propertyFee);
        return Result.success();
    }

    /**
     * 计算物业费
     * @param ownerId 业主ID

     * @return 计算结果
     */
    @Transactional
    public Result<?> calculatePropertyFee(Long ownerId, String feeDateStr) {
        LOGGER.info("Calculating property fee for owner: {}, date: {}", ownerId, feeDateStr);
        
        // 解析日期字符串为LocalDate
        LocalDate feeDate;
        try {
            // 将yyyy-MM格式转换为yyyy-MM-01格式，以便解析为LocalDate
            feeDate = LocalDate.parse(feeDateStr + "-01");
        } catch (Exception e) {
            return Result.error("-1", "日期格式错误，请使用yyyy-MM格式，例如：2024-02");
        }
        
        // 检查业主是否存在
        Owner owner = ownerMapper.selectById(ownerId);
        if (owner == null) {
            return Result.error("-1", "业主不存在");
        }
        
        // 检查物业费日期是否在入住日期之后
        if (owner.getCheckInTime() != null && 
            feeDate.isBefore(owner.getCheckInTime().toLocalDate())) {
            return Result.error("-1", "物业费计算日期不能早于入住日期");
        }
        
        // 检查是否已存在物业费记录
        LambdaQueryWrapper<PropertyFee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PropertyFee::getOwnerId, ownerId)
               .eq(PropertyFee::getFeeDate, feeDate);
        if (propertyFeeMapper.selectCount(wrapper) > 0) {
            return Result.error("-1", "该月物业费记录已存在");
        }
        
        // 获取房屋信息
        House house = houseMapper.selectById(owner.getHouseId());
        if (house == null) {
            return Result.error("-1", "房屋信息不存在");
        }
        
        // 获取房屋类型信息
        HouseType houseType = houseTypeMapper.selectById(house.getHouseTypeId());
        if (houseType == null) {
            return Result.error("-1", "房屋类型信息不存在");
        }
        HouseTypeReferenceAreas.apply(houseType);
        BigDecimal billArea = HouseTypeReferenceAreas.areaForBilling(house, houseType);
        if (billArea == null) {
            return Result.error("-1", "房屋面积未设置，无法计算物业费");
        }
        // 计算物业费：计费面积 * 物业费月单价（四种约定户型用写死面积）
        BigDecimal amount = billArea.multiply(houseType.getPropertyFee());
        
        // 生成订单编号：WY + 年月日时分秒 + 6位随机数
        String orderNo = generateOrderNo();
        
        // 创建物业费记录
        PropertyFee propertyFee = new PropertyFee();
        propertyFee.setOrderNo(orderNo);
        propertyFee.setOwnerId(ownerId);
        propertyFee.setHouseId(house.getId());
        propertyFee.setFeeDate(feeDate);
        propertyFee.setAmount(amount);
        propertyFee.setStatus(PropertyFee.Status.UNPAID);
        propertyFee.setCreatedAt(LocalDateTime.now());
        
        propertyFeeMapper.insert(propertyFee);
        
        return Result.success(propertyFee);
    }

    /**
     * 生成订单编号
     * 格式：WY + 年月日时分秒 + 6位随机数
     * @return 订单编号
     */
    private String generateOrderNo() {
        // 生成时间部分：年月日时分秒
        String timePart = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        
        // 生成6位随机数
        String randomPart = String.format("%06d", (int)(Math.random() * 1000000));
        
        // 组合订单编号
        return "WY" + timePart + randomPart;
    }

    /**
     * 可参与物业费推送的业主：账号已启用且已绑定房屋
     */
    private List<Owner> listOwnersEligibleForPropertyFeeBill() {
        return ownerMapper.selectList(new LambdaQueryWrapper<Owner>()
                .eq(Owner::getStatus, Owner.ReviewStatus.ENABLED)
                .isNotNull(Owner::getHouseId));
    }

    /**
     * 按月份为符合条件的业主生成账单（与业主端缴费记录同源）
     */
    private PropertyFeePushResult runPushForMonth(String feeDateStr) {
        PropertyFeePushResult out = new PropertyFeePushResult();
        out.setFeeMonth(feeDateStr);
        List<Owner> owners = listOwnersEligibleForPropertyFeeBill();
        out.setTotalOwners(owners.size());
        for (Owner owner : owners) {
            Result<?> result = calculatePropertyFee(owner.getId(), feeDateStr);
            if ("0".equals(result.getCode())) {
                out.setSuccessCount(out.getSuccessCount() + 1);
            } else {
                String msg = result.getMsg() != null ? result.getMsg() : "未知错误";
                if (msg.contains("已存在")) {
                    out.setSkipCount(out.getSkipCount() + 1);
                } else {
                    out.setFailCount(out.getFailCount() + 1);
                    String userHint = "";
                    if (owner.getUserId() != null) {
                        User u = userMapper.selectById(owner.getUserId());
                        if (u != null && StringUtils.isNotBlank(u.getName())) {
                            userHint = u.getName() + " ";
                        }
                    }
                    out.addFailSample(userHint + "业主ID " + owner.getId() + "：" + msg);
                }
            }
        }
        return out;
    }

    /**
     * 一键推送：为所有符合条件的业主生成指定月份待缴账单，业主可在业主端「物业费」中查看并支付。
     */
    public Result<PropertyFeePushResult> pushPropertyFeesToOwners(String feeDateStr) {
        if (feeDateStr == null || feeDateStr.trim().isEmpty()) {
            return Result.error("-1", "费用月份不能为空");
        }
        String month = feeDateStr.trim();
        try {
            LocalDate.parse(month + "-01");
        } catch (Exception e) {
            return Result.error("-1", "日期格式错误，请使用yyyy-MM格式，例如：2024-02");
        }
        PropertyFeePushResult out = runPushForMonth(month);
        return Result.success(out);
    }

    /**
     * 批量计算物业费

     * @return 计算结果
     */
    @Transactional
    public Result<?> batchCalculatePropertyFee(String feeDateStr) {
        LOGGER.info("Batch calculating property fee for date: {}", feeDateStr);
        try {
            LocalDate.parse(feeDateStr + "-01");
        } catch (Exception e) {
            return Result.error("-1", "日期格式错误，请使用yyyy-MM格式，例如：2024-02");
        }
        PropertyFeePushResult out = runPushForMonth(feeDateStr);
        if (out.getTotalOwners() == 0) {
            return Result.error("-1", "没有可推送的业主（需账号已启用且已绑定房屋）");
        }
        String msg = String.format(
                "推送完成：成功 %d 条，跳过 %d 条（该月已存在），失败 %d 条",
                out.getSuccessCount(), out.getSkipCount(), out.getFailCount());
        return Result.success(msg);
    }
} 