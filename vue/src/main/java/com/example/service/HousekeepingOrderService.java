package com.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Result;
import com.example.entity.*;
import com.example.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
public class HousekeepingOrderService {
    
    public static final Logger LOGGER = LoggerFactory.getLogger(HousekeepingOrderService.class);
    
    @Autowired
    private HousekeepingOrderMapper housekeepingOrderMapper;
    
    @Autowired
    private OwnerMapper ownerMapper;
    
    @Autowired
    private HousekeeperMapper housekeeperMapper;
    
    @Autowired
    private HousekeepingServiceMapper housekeepingServiceMapper;
    
    @Autowired
    private HouseMapper houseMapper;
    
    @Autowired
    private HousekeeperServiceMapper housekeeperServiceMapper;
    
    /**
     * 分页查询订单
     */
    public Result<IPage<HousekeepingOrder>> page(Integer pageNum, Integer pageSize, String orderNo, String status, Long ownerId, Long housekeeperId) {
        LOGGER.info("分页查询订单: pageNum={}, pageSize={}, orderNo={}, status={}, ownerId={}, housekeeperId={}", 
                pageNum, pageSize, orderNo, status, ownerId, housekeeperId);
        
        LambdaQueryWrapper<HousekeepingOrder> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(orderNo)) {
            queryWrapper.like(HousekeepingOrder::getOrderNo, orderNo);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(HousekeepingOrder::getStatus, status);
        }
        if (ownerId != null) {
            queryWrapper.eq(HousekeepingOrder::getOwnerId, ownerId);
        }
        if (housekeeperId != null) {
            queryWrapper.eq(HousekeepingOrder::getHousekeeperId, housekeeperId);
        }
        
        queryWrapper.orderByDesc(HousekeepingOrder::getCreatedAt);
        
        Page<HousekeepingOrder> page = new Page<>(pageNum, pageSize);
        IPage<HousekeepingOrder> orderPage = housekeepingOrderMapper.selectPage(page, queryWrapper);
        
        // 填充关联信息
        orderPage.getRecords().forEach(this::fillOrderInfo);
        
        return Result.success(orderPage);
    }
    
    /**
     * 根据ID查询订单
     */
    public Result<HousekeepingOrder> getById(Long id) {
        LOGGER.info("根据ID查询订单: id={}", id);
        
        HousekeepingOrder order = housekeepingOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("-1", "订单不存在");
        }
        
        // 填充关联信息
        fillOrderInfo(order);
        
        return Result.success(order);
    }
    
    /**
     * 创建订单
     */
    public Result<?> create(HousekeepingOrder order) {
        LOGGER.info("创建订单: {}", order);
        
        // 检查业主是否存在
        Owner owner = ownerMapper.selectById(order.getOwnerId());
        if (owner == null) {
            return Result.error("-1", "业主不存在");
        }
        
        // 检查家政人员是否存在
        Housekeeper housekeeper = housekeeperMapper.selectById(order.getHousekeeperId());
        if (housekeeper == null) {
            return Result.error("-1", "家政人员不存在");
        }
        
        // 检查服务项目是否存在
        HousekeepingService service = housekeepingServiceMapper.selectById(order.getServiceId());
        if (service == null) {
            return Result.error("-1", "服务项目不存在");
        }
        
        // 检查房屋是否存在
        House house = houseMapper.selectById(order.getHouseId());
        if (house == null) {
            return Result.error("-1", "房屋不存在");
        }
        
        // 检查家政人员是否提供该服务
        LambdaQueryWrapper<HousekeeperService> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HousekeeperService::getHousekeeperId, order.getHousekeeperId())
                .eq(HousekeeperService::getServiceId, order.getServiceId());
        HousekeeperService housekeeperService = housekeeperServiceMapper.selectOne(queryWrapper);
        if (housekeeperService == null) {
            return Result.error("-1", "该家政人员不提供此服务");
        }
        
        // 生成订单编号
        String orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        
        // 设置订单金额
        BigDecimal amount = calculateAmount(housekeeperService.getPrice(), order.getServiceDuration(), service.getUnit());
        order.setAmount(amount);
        
        // 设置订单状态
        order.setStatus("PENDING");
        order.setPaymentStatus("UNPAID");
        
        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        
        housekeepingOrderMapper.insert(order);
        return Result.success();
    }
    
    /**
     * 更新订单
     */
    public Result<?> update(HousekeepingOrder order) {
        LOGGER.info("更新订单: {}", order);
        
        // 检查订单是否存在
        HousekeepingOrder existOrder = housekeepingOrderMapper.selectById(order.getId());
        if (existOrder == null) {
            return Result.error("-1", "订单不存在");
        }
        
        // 检查订单状态
        if ("COMPLETED".equals(existOrder.getStatus()) || "CANCELLED".equals(existOrder.getStatus())) {
            return Result.error("-1", "已完成或已取消的订单不能修改");
        }
        
        // 如果修改了服务项目或家政人员，需要重新计算金额
        if (!existOrder.getServiceId().equals(order.getServiceId()) || 
                !existOrder.getHousekeeperId().equals(order.getHousekeeperId()) ||
                !existOrder.getServiceDuration().equals(order.getServiceDuration())) {
            
            // 检查家政人员是否提供该服务
            LambdaQueryWrapper<HousekeeperService> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(HousekeeperService::getHousekeeperId, order.getHousekeeperId())
                    .eq(HousekeeperService::getServiceId, order.getServiceId());
            HousekeeperService housekeeperService = housekeeperServiceMapper.selectOne(queryWrapper);
            if (housekeeperService == null) {
                return Result.error("-1", "该家政人员不提供此服务");
            }
            
            // 获取服务项目信息
            HousekeepingService service = housekeepingServiceMapper.selectById(order.getServiceId());
            if (service == null) {
                return Result.error("-1", "服务项目不存在");
            }
            
            // 重新计算金额
            BigDecimal amount = calculateAmount(housekeeperService.getPrice(), order.getServiceDuration(), service.getUnit());
            order.setAmount(amount);
        }
        
        // 更新时间
        order.setUpdatedAt(LocalDateTime.now());
        
        housekeepingOrderMapper.updateById(order);
        return Result.success();
    }
    
    /**
     * 取消订单
     */
    public Result<?> cancel(Long id) {
        LOGGER.info("取消订单: id={}", id);
        
        // 检查订单是否存在
        HousekeepingOrder order = housekeepingOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("-1", "订单不存在");
        }
        
        // 检查订单状态
        if ("COMPLETED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            return Result.error("-1", "已完成或已取消的订单不能取消");
        }
        
        // 如果已支付，需要退款
        if ("PAID".equals(order.getPaymentStatus())) {
            order.setPaymentStatus("REFUNDED");
        }
        
        // 更新订单状态
        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());
        
        housekeepingOrderMapper.updateById(order);
        return Result.success();
    }
    
    /**
     * 确认订单
     */
    public Result<?> confirm(Long id) {
        LOGGER.info("确认订单: id={}", id);
        
        // 检查订单是否存在
        HousekeepingOrder order = housekeepingOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("-1", "订单不存在");
        }
        
        // 检查订单状态
        if (!"PENDING".equals(order.getStatus())) {
            return Result.error("-1", "只有待确认的订单可以确认");
        }
        
        // 更新订单状态
        order.setStatus("CONFIRMED");
        order.setUpdatedAt(LocalDateTime.now());
        
        housekeepingOrderMapper.updateById(order);
        return Result.success();
    }
    
    /**
     * 开始服务
     */
    public Result<?> start(Long id) {
        LOGGER.info("开始服务: id={}", id);
        
        // 检查订单是否存在
        HousekeepingOrder order = housekeepingOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("-1", "订单不存在");
        }
        
        // 检查订单状态
        if (!"CONFIRMED".equals(order.getStatus())) {
            return Result.error("-1", "只有已确认的订单可以开始服务");
        }
        
        // 检查支付状态
        if (!"PAID".equals(order.getPaymentStatus())) {
            return Result.error("-1", "订单未支付，不能开始服务");
        }
        
        // 更新订单状态
        order.setStatus("IN_PROGRESS");
        order.setUpdatedAt(LocalDateTime.now());
        
        housekeepingOrderMapper.updateById(order);
        return Result.success();
    }
    
    /**
     * 完成服务
     */
    public Result<?> complete(Long id) {
        LOGGER.info("完成服务: id={}", id);
        
        // 检查订单是否存在
        HousekeepingOrder order = housekeepingOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("-1", "订单不存在");
        }
        
        // 检查订单状态
        if (!"IN_PROGRESS".equals(order.getStatus())) {
            return Result.error("-1", "只有进行中的订单可以完成");
        }
        
        // 更新订单状态
        order.setStatus("COMPLETED");
        order.setUpdatedAt(LocalDateTime.now());
        
        housekeepingOrderMapper.updateById(order);
        return Result.success();
    }
    
    /**
     * 支付订单
     */
    public Result<?> pay(Long id) {
        LOGGER.info("支付订单: id={}", id);
        
        // 检查订单是否存在
        HousekeepingOrder order = housekeepingOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("-1", "订单不存在");
        }
        
        // 检查订单状态
        if ("CANCELLED".equals(order.getStatus())) {
            return Result.error("-1", "已取消的订单不能支付");
        }
        
        // 检查支付状态
        if ("PAID".equals(order.getPaymentStatus()) || "REFUNDED".equals(order.getPaymentStatus())) {
            return Result.error("-1", "订单已支付或已退款");
        }
        
        // 更新支付状态
        order.setPaymentStatus("PAID");
        order.setUpdatedAt(LocalDateTime.now());
        
        housekeepingOrderMapper.updateById(order);
        return Result.success();
    }
    
    /**
     * 填充订单关联信息
     */
    private void fillOrderInfo(HousekeepingOrder order) {
        // 填充业主信息
        Owner owner = ownerMapper.selectById(order.getOwnerId());
        order.setOwner(owner);
        
        // 填充家政人员信息
        Housekeeper housekeeper = housekeeperMapper.selectById(order.getHousekeeperId());
        order.setHousekeeper(housekeeper);
        
        // 填充服务项目信息
        HousekeepingService service = housekeepingServiceMapper.selectById(order.getServiceId());
        order.setService(service);
        
        // 填充房屋信息
        House house = houseMapper.selectById(order.getHouseId());
        order.setHouse(house);
    }
    
    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        // 生成格式: 年月日时分秒 + 4位随机数
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String dateTime = LocalDateTime.now().format(formatter);
        
        // 生成4位随机数
        Random random = new Random();
        int randomNum = random.nextInt(10000);
        String randomStr = String.format("%04d", randomNum);
        
        return "HK" + dateTime + randomStr;
    }
    
    /**
     * 计算订单金额
     */
    private BigDecimal calculateAmount(BigDecimal price, Integer duration, String unit) {
        if ("小时".equals(unit)) {
            // 按小时计费，不足1小时按1小时计算
            int hours = (duration + 59) / 60;  // 向上取整
            return price.multiply(new BigDecimal(hours));
        } else if ("次".equals(unit)) {
            // 按次计费，直接返回价格
            return price;
        } else if ("平米".equals(unit)) {
            // 按平米计费，需要房屋面积信息，这里简化处理
            return price;
        } else {
            // 其他计费方式，默认返回价格
            return price;
        }
    }
} 