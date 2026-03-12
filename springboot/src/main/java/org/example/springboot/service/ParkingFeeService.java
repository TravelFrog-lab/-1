package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.ParkingFee;
import org.example.springboot.entity.ParkingSpace;
import org.example.springboot.mapper.ParkingFeeMapper;
import org.example.springboot.mapper.ParkingSpaceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ParkingFeeService extends ServiceImpl<ParkingFeeMapper, ParkingFee> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ParkingFeeService.class);
    
    // 每小时收费标准（可以后续改为从配置表读取）
    private static final BigDecimal HOURLY_RATE = new BigDecimal("5.00");

    @Resource
    private ParkingSpaceMapper parkingSpaceMapper;
    
    @Resource
    private ParkingFeeMapper parkingFeeMapper;


    public Result<Page<ParkingFee>> list(Integer pageNum, Integer pageSize, String plateNumber, ParkingFee.Status status) {
        LOGGER.info("Listing parking fees, pageNum: {}, pageSize: {}, plateNumber: {}, status: {}", 
                pageNum, pageSize, plateNumber, status);
        
        LambdaQueryWrapper<ParkingFee> wrapper = new LambdaQueryWrapper<>();
        if (plateNumber != null && !plateNumber.trim().isEmpty()) {
            wrapper.like(ParkingFee::getPlateNumber, plateNumber);
        }
        if (status != null) {
            wrapper.eq(ParkingFee::getStatus, status);
        }
        wrapper.orderByDesc(ParkingFee::getCreatedAt);
        
        Page<ParkingFee> page = new Page<>(pageNum, pageSize);
        List<ParkingFee> records = parkingFeeMapper.selectPage(page, wrapper).getRecords();
        for (ParkingFee record : records) {
            Long parkingSpaceId = record.getParkingSpaceId();
            if(parkingSpaceId!=null){
                ParkingSpace parkingSpace = parkingSpaceMapper.selectById(parkingSpaceId);
                record.setParkingSpace(parkingSpace);
            }
        }
        page.setRecords(records);
       return Result.success(page);
    }

    public Result<ParkingFee> getById(Long id) {
        LOGGER.info("Getting parking fee by id: {}", id);
        ParkingFee parkingFee = parkingFeeMapper.selectById(id);
        if (parkingFee == null) {
            return Result.error("-1", "停车费记录不存在");
        }
        Long parkingSpaceId = parkingFee.getParkingSpaceId();
        if(parkingSpaceId!=null){
            ParkingSpace parkingSpace = parkingSpaceMapper.selectById(parkingSpaceId);
            parkingFee.setParkingSpace(parkingSpace);
        }
        return Result.success(parkingFee);
    }

    @Transactional
    public Result<?> add(ParkingFee parkingFee) {
        LOGGER.info("Adding new parking fee: {}", parkingFee);
        // 检查是否有未结算的记录
        if (parkingFeeMapper.selectCount(new LambdaQueryWrapper<ParkingFee>()
                .eq(ParkingFee::getPlateNumber, parkingFee.getPlateNumber())
                .eq(ParkingFee::getStatus, ParkingFee.Status.UNPAID)
               ) > 0) {
            return Result.error("-1", "该车辆尚未缴费");
        }
        // 检查车位目前是否空闲
        List<ParkingFee> parkingFees = parkingFeeMapper.selectList(new LambdaQueryWrapper<ParkingFee>()
                .eq(ParkingFee::getParkingSpaceId, parkingFee.getParkingSpaceId())
                .isNull(ParkingFee::getExitTime)
        );
        if(!parkingFees.isEmpty()){
            return Result.error("-1", "车位目前不空闲");
        }
        
        
        // parkingFee.setEntryTime(LocalDateTime.now());
        parkingFeeMapper.insert(parkingFee);
        return Result.success();

    }

    @Transactional
    public Result<?> update(ParkingFee parkingFee) {
        LOGGER.info("Updating parking fee: {}", parkingFee);

        if (parkingFeeMapper.updateById(parkingFee) <= 0) {
            return Result.error("-1", "停车费记录不存在");
        }

        // 检查是否有未结算的记录（排除当前记录）
        if (parkingFeeMapper.selectCount(new LambdaQueryWrapper<ParkingFee>()
                .eq(ParkingFee::getPlateNumber, parkingFee.getPlateNumber())
                .eq(ParkingFee::getStatus, ParkingFee.Status.UNPAID)
                .ne(ParkingFee::getId, parkingFee.getId())  // 排除当前记录
               ) > 0) {
            return Result.error("-1", "该车辆尚未缴费");
        }

        // 检查车位目前是否空闲（排除当前记录）
        List<ParkingFee> parkingFees = parkingFeeMapper.selectList(new LambdaQueryWrapper<ParkingFee>()
                .eq(ParkingFee::getParkingSpaceId, parkingFee.getParkingSpaceId())
                .isNull(ParkingFee::getExitTime)
                .ne(ParkingFee::getId, parkingFee.getId())  // 排除当前记录
        );
        if(!parkingFees.isEmpty()){
            return Result.error("-1", "车位目前不空闲");
        }

        return Result.success();
    }

    @Transactional
    public Result<?> delete(Long id) {
        LOGGER.info("Deleting parking fee: {}", id);
        if (parkingFeeMapper.deleteById(id) <= 0) {
            return Result.error("-1", "停车费记录不存在");
        }
        return Result.success();
    }

    @Transactional
    public Result<?> batchDelete(List<Long> ids) {
        LOGGER.info("Batch deleting parking fees: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要删除的记录");
        }
        
        parkingFeeMapper.deleteBatchIds(ids);
        return Result.success();
    }

    @Transactional
    public Result<?> checkout(Long id) {
        LOGGER.info("Checking out parking fee: {}", id);
        ParkingFee parkingFee = parkingFeeMapper.selectById(id);
        if (parkingFee == null) {
            return Result.error("-1", "停车费记录不存在");
        }
        
        if (parkingFee.getExitTime() != null) {
            return Result.error("-1", "该车辆已出场");
        }
        
        // 设置出场时间并计算费用
        LocalDateTime exitTime = LocalDateTime.now();
        parkingFee.setExitTime(exitTime);
        
        // 计算停车时长（小时）
        Duration duration = Duration.between(parkingFee.getEntryTime(), exitTime);
        long hours = duration.toHours() + (duration.toMinutesPart() > 0 ? 1 : 0); // 不足一小时按一小时计算
        
        // 计算费用
        BigDecimal fee = HOURLY_RATE.multiply(new BigDecimal(hours));
        parkingFee.setFeeAmount(fee);
        
        // 生成订单编号
        parkingFee.setOrderNo(generateOrderNo());
        
        parkingFeeMapper.updateById(parkingFee);
        return Result.success(fee);
    }

    public Result<?> pay(Long id) {
        LOGGER.info("Paying parking fee: {}", id);
        ParkingFee parkingFee = parkingFeeMapper.selectById(id);
        if (parkingFee == null) {
            return Result.error("-1", "停车费记录不存在");
        }
        if (parkingFee.getExitTime() == null) {
            return Result.error("-1", "该车辆未出场");
        }
        if (parkingFee.getStatus() != ParkingFee.Status.UNPAID) {
            return Result.error("-1", "该停车费已结算");
        }
        parkingFee.setStatus(ParkingFee.Status.PAID);
        parkingFeeMapper.updateById(parkingFee);
        return Result.success();
    }

    /**
     * 检查停车费支付状态
     * @param orderNo 订单编号
     * @return 支付状态
     */
    public Result<?> checkPayStatus(String orderNo) {
        LambdaQueryWrapper<ParkingFee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParkingFee::getOrderNo, orderNo);
        ParkingFee parkingFee = parkingFeeMapper.selectOne(wrapper);
        if (parkingFee == null) {
            return Result.error("-1", "停车费记录不存在");
        }
        return Result.success(parkingFee.getStatus());
    }

    /**
     * 生成订单编号
     * 格式：TC + 年月日时分秒 + 6位随机数
     * @return 订单编号
     */
    private String generateOrderNo() {
        // 生成时间部分：年月日时分秒
        String timePart = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        
        // 生成6位随机数
        String randomPart = String.format("%06d", (int)(Math.random() * 1000000));
        
        // 组合订单编号
        return "TC" + timePart + randomPart;
    }
} 