package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.springboot.common.Result;
import org.example.springboot.entity.*;
import org.example.springboot.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private OwnerMapper ownerMapper;
    @Autowired
    private PropertyFeeMapper propertyFeeMapper;
    @Autowired
    private RepairRecordMapper repairRecordMapper;
    @Autowired
    private ComplaintMapper complaintMapper;
    @Autowired
    private ParkingSpaceMapper parkingSpaceMapper;
    @Autowired
    private ParkingFeeMapper parkingFeeMapper;
    @Autowired
    private HouseMapper houseMapper;
    @Autowired
    private HouseTypeMapper houseTypeMapper;
    @Autowired
    private VolunteerActivityMapper volunteerActivityMapper;

    public Result<?> getOverview() {
        Map<String, Object> data = new HashMap<>();
        
        // 1. 业主总数
        data.put("totalOwners", ownerMapper.selectCount(null));
        
        // 2. 物业费收缴率
        long totalFees = propertyFeeMapper.selectCount(null);
        long paidFees = propertyFeeMapper.selectCount(
            new LambdaQueryWrapper<PropertyFee>()
                .eq(PropertyFee::getStatus, PropertyFee.Status.PAID)
        );
        data.put("propertyFeeRate", totalFees > 0 ? (double)paidFees/totalFees : 0);
        
        // 3. 维修完成率
        long totalRepairs = repairRecordMapper.selectCount(null);
        long completedRepairs = repairRecordMapper.selectCount(
            new LambdaQueryWrapper<RepairRecord>()
                .eq(RepairRecord::getStatus, RepairRecord.Status.COMPLETED)
        );
        data.put("repairCompletionRate", totalRepairs > 0 ? (double)completedRepairs/totalRepairs : 0);
        
        // 4. 投诉处理率
        long totalComplaints = complaintMapper.selectCount(null);
        long resolvedComplaints = complaintMapper.selectCount(
            new LambdaQueryWrapper<Complaint>()
                .eq(Complaint::getStatus, Complaint.Status.RESOLVED)
        );
        data.put("complaintResolutionRate", totalComplaints > 0 ? (double)resolvedComplaints/totalComplaints : 0);
        
        // 5. 车位使用率
        long totalParkingSpaces = parkingSpaceMapper.selectCount(null);
        long occupiedSpaces = parkingFeeMapper.selectCount(
            new LambdaQueryWrapper<ParkingFee>()
                .isNull(ParkingFee::getExitTime)
        );
        data.put("parkingOccupancyRate", totalParkingSpaces > 0 ? (double)occupiedSpaces/totalParkingSpaces : 0);

        return Result.success(data);
    }

    public Result<?> getPropertyFeeStats(Integer years) {
        Map<String, Object> data = new HashMap<>();
        
        // 限制年数范围为1-3年
        years = Math.min(Math.max(years, 1), 3);
        
        // 获取当前日期
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusYears(years).withDayOfMonth(1);
        
        // 准备月份数据
        List<String> months = new ArrayList<>();
        List<BigDecimal> expectedFees = new ArrayList<>();
        List<BigDecimal> actualFees = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        // 计算每个月的数据
        for (LocalDate date = startDate; !date.isAfter(now); date = date.plusMonths(1)) {
            String monthStr = date.format(formatter);
            months.add(monthStr);
            
            // 当月应收金额
            BigDecimal expected = propertyFeeMapper.selectList(
                new LambdaQueryWrapper<PropertyFee>()
                    .ge(PropertyFee::getFeeDate, date)
                    .lt(PropertyFee::getFeeDate, date.plusMonths(1))
            ).stream().map(PropertyFee::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 当月实收金额
            BigDecimal actual = propertyFeeMapper.selectList(
                new LambdaQueryWrapper<PropertyFee>()
                    .ge(PropertyFee::getFeeDate, date)
                    .lt(PropertyFee::getFeeDate, date.plusMonths(1))
                    .eq(PropertyFee::getStatus, PropertyFee.Status.PAID)
            ).stream().map(PropertyFee::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            
            expectedFees.add(expected);
            actualFees.add(actual);
        }
        
        data.put("months", months);
        data.put("expectedFees", expectedFees);
        data.put("actualFees", actualFees);
        
        return Result.success(data);
    }

    public Result<?> getHouseTypeStats() {
        Map<String, Object> data = new HashMap<>();
        
        // 获取所有房屋类型
        List<HouseType> houseTypes = houseTypeMapper.selectList(null);
        
        // 统计每种房屋类型的数量
        Map<Long, Long> typeCountMap = houseMapper.selectList(null)
            .stream()
            .collect(Collectors.groupingBy(
                House::getHouseTypeId,
                Collectors.counting()
            ));
        
        // 构建前端需要的数据格式
        List<Map<String, Object>> typeStats = houseTypes.stream()
            .map(type -> {
                Map<String, Object> item = new HashMap<>();
                item.put("name", type.getName());
                item.put("value", typeCountMap.getOrDefault(type.getId(), 0L));
                return item;
            })
            .collect(Collectors.toList());
        
        data.put("houseTypes", typeStats);
        
        return Result.success(data);
    }

    public Result<?> getComplaintStats() {
        Map<String, Object> data = new HashMap<>();
        
        // 1. 各状态投诉数量
        Map<Complaint.Status, Long> statusCounts = complaintMapper.selectList(null)
            .stream()
            .collect(Collectors.groupingBy(
                Complaint::getStatus,
                Collectors.counting()
            ));
        
        // 2. 平均处理时长 - 添加空值检查
        List<Complaint> resolvedComplaints = complaintMapper.selectList(
            new LambdaQueryWrapper<Complaint>()
                .eq(Complaint::getStatus, Complaint.Status.RESOLVED)
        );
        
        double avgProcessTime = resolvedComplaints.stream()
            .filter(complaint -> complaint.getCreatedAt() != null && complaint.getHandleTime() != null)
            .mapToLong(complaint -> 
                ChronoUnit.HOURS.between(complaint.getCreatedAt(), complaint.getHandleTime())
            )
            .average()
            .orElse(0);
        
        data.put("statusCounts", statusCounts);
        data.put("avgProcessTime", avgProcessTime);

        return Result.success(data);
    }

    public Result<?> getParkingStats() {
        Map<String, Object> data = new HashMap<>();
        
        // 1. 车位类型统计
        Map<ParkingSpace.Type, Long> typeCounts = parkingSpaceMapper.selectList(null)
            .stream()
            .collect(Collectors.groupingBy(
                ParkingSpace::getType,
                Collectors.counting()
            ));
        
        // 2. 停车费收入统计
        BigDecimal totalIncome = parkingFeeMapper.selectList(
            new LambdaQueryWrapper<ParkingFee>()
                .eq(ParkingFee::getStatus, ParkingFee.Status.PAID)
        ).stream().map(ParkingFee::getFeeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        data.put("typeCounts", typeCounts);
        data.put("totalIncome", totalIncome);

        return Result.success(data);
    }

    /**
     * 首页社区数据概览：服务满意度、本月报修单、社区总人数、进行中活动
     */
    public Result<?> getCommunityStats() {
        Map<String, Object> data = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfThisMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);
        LocalDateTime startOfNextMonth = startOfThisMonth.plusMonths(1);

        // 1. 服务满意度：维修完成率与投诉处理率各占50%，换算成0-100
        long totalRepairs = repairRecordMapper.selectCount(null);
        long completedRepairs = repairRecordMapper.selectCount(
            new LambdaQueryWrapper<RepairRecord>().eq(RepairRecord::getStatus, RepairRecord.Status.COMPLETED));
        long totalComplaints = complaintMapper.selectCount(null);
        long resolvedComplaints = complaintMapper.selectCount(
            new LambdaQueryWrapper<Complaint>().eq(Complaint::getStatus, Complaint.Status.RESOLVED));
        double repairRate = totalRepairs > 0 ? (double) completedRepairs / totalRepairs : 0;
        double complaintRate = totalComplaints > 0 ? (double) resolvedComplaints / totalComplaints : 0;
        int satisfaction = (int) Math.round((repairRate * 0.5 + complaintRate * 0.5) * 100);
        satisfaction = Math.min(100, Math.max(0, satisfaction));
        data.put("serviceSatisfaction", satisfaction);
        data.put("serviceSatisfactionTrend", "+0%");

        // 2. 本月报修单
        long monthlyRepairCount = repairRecordMapper.selectCount(
            new LambdaQueryWrapper<RepairRecord>()
                .ge(RepairRecord::getCreatedAt, startOfThisMonth)
                .lt(RepairRecord::getCreatedAt, startOfNextMonth));
        long lastMonthRepairCount = repairRecordMapper.selectCount(
            new LambdaQueryWrapper<RepairRecord>()
                .ge(RepairRecord::getCreatedAt, startOfLastMonth)
                .lt(RepairRecord::getCreatedAt, startOfThisMonth));
        String monthlyRepairTrend = lastMonthRepairCount > 0
            ? String.format("%+d%%", (int) Math.round((monthlyRepairCount - lastMonthRepairCount) * 100.0 / lastMonthRepairCount))
            : "+0%";
        data.put("monthlyRepairCount", monthlyRepairCount);
        data.put("monthlyRepairTrend", monthlyRepairTrend);

        // 3. 社区总人数（业主数）
        long totalPopulation = ownerMapper.selectCount(null);
        data.put("totalCommunityPopulation", totalPopulation);
        data.put("totalCommunityPopulationTrend", "+0%");

        // 4. 进行中活动：当前时间在活动开始与结束之间
        long ongoingActivityCount = volunteerActivityMapper.selectCount(
            new LambdaQueryWrapper<VolunteerActivity>()
                .le(VolunteerActivity::getStartTime, now)
                .ge(VolunteerActivity::getEndTime, now));
        data.put("ongoingActivityCount", ongoingActivityCount);
        data.put("ongoingActivityTrend", "+0");

        return Result.success(data);
    }
} 