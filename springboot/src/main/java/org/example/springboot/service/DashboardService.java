package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.springboot.common.Result;
import org.example.springboot.entity.*;
import org.example.springboot.mapper.*;
import org.example.springboot.util.HouseTypeReferenceAreas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
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
    private HouseMapper houseMapper;
    @Autowired
    private HouseTypeMapper houseTypeMapper;
    @Autowired
    private RepairTypeMapper repairTypeMapper;
    @Autowired
    private VolunteerActivityMapper volunteerActivityMapper;
    @Autowired
    private VoteRecordMapper voteRecordMapper;
    @Autowired
    private VoteMapper voteMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MaintenanceStaffMapper maintenanceStaffMapper;
    @Autowired
    private HousekeepingOrderMapper housekeepingOrderMapper;
    @Autowired
    private HousekeeperMapper housekeeperMapper;
    @Autowired
    private HousekeepingServiceMapper housekeepingServiceCatalogMapper;

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
        HouseTypeReferenceAreas.apply(houseTypes);
        
        // 统计每种房屋类型的数量（house_type_id 为 null 时 groupingBy 会 NPE，需过滤）
        Map<Long, Long> typeCountMap = houseMapper.selectList(null)
            .stream()
            .filter(h -> h.getHouseTypeId() != null)
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

    /**
     * 首页社区数据概览：本月服务满意度、本月报修完成率、房屋统计等
     */
    public Result<?> getCommunityStats() {
        Map<String, Object> data = new HashMap<>();
        LocalDateTime startOfThisMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);
        LocalDateTime startOfNextMonth = startOfThisMonth.plusMonths(1);

        // 1. 本月服务满意度：仅统计「本月创建」的报修单、投诉单（完成/已解决占比加权）
        int monthSatisfaction = computeMonthlyServiceSatisfaction(startOfThisMonth, startOfNextMonth);
        data.put("serviceSatisfaction", monthSatisfaction);
        int lastMonthSatisfaction = computeMonthlyServiceSatisfaction(startOfLastMonth, startOfThisMonth);
        String satTrend = formatPercentPointTrend(monthSatisfaction, lastMonthSatisfaction);
        data.put("serviceSatisfactionTrend", satTrend);

        // 2. 本月报修完成率 + 明细
        long monthRepairTotal = repairRecordMapper.selectCount(
                new LambdaQueryWrapper<RepairRecord>()
                        .ge(RepairRecord::getCreatedAt, startOfThisMonth)
                        .lt(RepairRecord::getCreatedAt, startOfNextMonth));
        long monthRepairCompleted = repairRecordMapper.selectCount(
                new LambdaQueryWrapper<RepairRecord>()
                        .ge(RepairRecord::getCreatedAt, startOfThisMonth)
                        .lt(RepairRecord::getCreatedAt, startOfNextMonth)
                        .eq(RepairRecord::getStatus, RepairRecord.Status.COMPLETED));
        int repairCompletionRate = monthRepairTotal > 0
                ? (int) Math.round(100.0 * monthRepairCompleted / monthRepairTotal)
                : 0;
        data.put("monthlyRepairCompletionRate", repairCompletionRate);
        data.put("monthlyRepairCompletedCount", monthRepairCompleted);
        data.put("monthlyRepairTotalCount", monthRepairTotal);

        long prevRepairTotal = repairRecordMapper.selectCount(
                new LambdaQueryWrapper<RepairRecord>()
                        .ge(RepairRecord::getCreatedAt, startOfLastMonth)
                        .lt(RepairRecord::getCreatedAt, startOfThisMonth));
        long prevRepairCompleted = repairRecordMapper.selectCount(
                new LambdaQueryWrapper<RepairRecord>()
                        .ge(RepairRecord::getCreatedAt, startOfLastMonth)
                        .lt(RepairRecord::getCreatedAt, startOfThisMonth)
                        .eq(RepairRecord::getStatus, RepairRecord.Status.COMPLETED));
        int prevRepairRate = prevRepairTotal > 0
                ? (int) Math.round(100.0 * prevRepairCompleted / prevRepairTotal)
                : 0;
        data.put("monthlyRepairCompletionTrend", formatPercentPointTrend(repairCompletionRate, prevRepairRate));

        // 兼容旧字段（若有其它页面仍用）
        data.put("monthlyRepairCount", monthRepairTotal);
        data.put("monthlyRepairTrend", data.get("monthlyRepairCompletionTrend"));

        // 近 6 个月报修单量（兼容旧接口）+ 每月完成率（柱状图）
        List<Integer> repairMonthlyTrend = new ArrayList<>();
        List<String> repairMonthLabels = new ArrayList<>();
        List<Integer> repairMonthlyCompletedCounts = new ArrayList<>();
        List<Integer> repairCompletionMonthlyRates = new ArrayList<>();
        // 近 6 个月投票参与人次（每条 vote_record 计 1 次）
        List<Integer> voteMonthlyParticipation = new ArrayList<>();
        List<String> voteMonthLabels = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = LocalDate.now().withDayOfMonth(1).minusMonths(i);
            LocalDateTime monthStart = monthDate.atStartOfDay();
            LocalDateTime monthEnd = monthDate.plusMonths(1).atStartOfDay();
            long repairCnt = repairRecordMapper.selectCount(
                    new LambdaQueryWrapper<RepairRecord>()
                            .ge(RepairRecord::getCreatedAt, monthStart)
                            .lt(RepairRecord::getCreatedAt, monthEnd));
            long repairDoneCnt = repairRecordMapper.selectCount(
                    new LambdaQueryWrapper<RepairRecord>()
                            .ge(RepairRecord::getCreatedAt, monthStart)
                            .lt(RepairRecord::getCreatedAt, monthEnd)
                            .eq(RepairRecord::getStatus, RepairRecord.Status.COMPLETED));
            repairMonthlyTrend.add((int) repairCnt);
            repairMonthlyCompletedCounts.add((int) repairDoneCnt);
            repairCompletionMonthlyRates.add(repairCnt > 0
                    ? (int) Math.round(100.0 * repairDoneCnt / repairCnt)
                    : 0);
            repairMonthLabels.add(monthDate.getMonthValue() + "月");

            long voteCnt = voteRecordMapper.selectCount(
                    new LambdaQueryWrapper<VoteRecord>()
                            .ge(VoteRecord::getCreatedAt, monthStart)
                            .lt(VoteRecord::getCreatedAt, monthEnd));
            voteMonthlyParticipation.add((int) voteCnt);
            voteMonthLabels.add(monthDate.getMonthValue() + "月");
        }
        data.put("repairMonthlyTrend", repairMonthlyTrend);
        data.put("repairMonthLabels", repairMonthLabels);
        data.put("repairMonthlyCompletedCounts", repairMonthlyCompletedCounts);
        data.put("repairCompletionMonthlyRates", repairCompletionMonthlyRates);
        data.put("voteMonthlyParticipation", voteMonthlyParticipation);
        data.put("voteMonthLabels", voteMonthLabels);

        // 2b. 本期投票参与情况（按投票项目，默认展示最近3个项目）
        long enabledOwnerCount = ownerMapper.selectCount(
                new LambdaQueryWrapper<Owner>().eq(Owner::getStatus, Owner.ReviewStatus.ENABLED));
        long totalUserCount = userMapper.selectCount(null);
        List<Vote> latestVotes = voteMapper.selectList(new LambdaQueryWrapper<Vote>()
                .orderByDesc(Vote::getCreatedAt)
                .last("LIMIT 3"));
        // 由旧到新展示，更符合阅读习惯
        java.util.Collections.reverse(latestVotes);
        List<String> voteProjectLabels = new ArrayList<>();
        List<Integer> voteProjectParticipantCounts = new ArrayList<>();
        List<Integer> voteProjectEligibleCounts = new ArrayList<>();
        List<Integer> voteProjectRates = new ArrayList<>();
        for (Vote vote : latestVotes) {
            voteProjectLabels.add(vote.getTitle() != null ? vote.getTitle() : ("投票#" + vote.getId()));
            List<VoteRecord> records = voteRecordMapper.selectList(
                    new LambdaQueryWrapper<VoteRecord>().eq(VoteRecord::getVoteId, vote.getId()));
            int participantCount = (int) records.stream()
                    .map(VoteRecord::getUserId)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .count();
            int eligibleCount = vote.getEligibility() == Vote.Eligibility.OWNER
                    ? (int) enabledOwnerCount
                    : (int) totalUserCount;
            int rate = eligibleCount > 0
                    ? (int) Math.round(100.0 * participantCount / eligibleCount)
                    : 0;
            voteProjectParticipantCounts.add(participantCount);
            voteProjectEligibleCounts.add(eligibleCount);
            voteProjectRates.add(rate);
        }
        data.put("voteProjectLabels", voteProjectLabels);
        data.put("voteProjectParticipantCounts", voteProjectParticipantCounts);
        data.put("voteProjectEligibleCounts", voteProjectEligibleCounts);
        data.put("voteProjectRates", voteProjectRates);

        // 3. 业主数（横幅等仍可用）
        long totalPopulation = ownerMapper.selectCount(null);
        data.put("totalCommunityPopulation", totalPopulation);
        data.put("totalCommunityPopulationTrend", "+0%");

        // 4. 房屋总数
        long totalHouseCount = houseMapper.selectCount(null);
        data.put("totalHouseCount", totalHouseCount);

        // 5. 小区活动总数（横幅「小区活动」数字）
        long communityActivityTotalCount = volunteerActivityMapper.selectCount(null);
        data.put("communityActivityTotalCount", communityActivityTotalCount);

        // 6. 房屋类型分布（兼容旧接口）
        List<HouseType> houseTypes = houseTypeMapper.selectList(null);
        HouseTypeReferenceAreas.apply(houseTypes);
        Map<Long, Long> houseTypeCountMap = houseMapper.selectList(null).stream()
                .filter(h -> h.getHouseTypeId() != null)
                .collect(Collectors.groupingBy(House::getHouseTypeId, Collectors.counting()));
        List<Map<String, Object>> houseTypeDistribution = new ArrayList<>();
        for (HouseType ht : houseTypes) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", ht.getName());
            item.put("value", houseTypeCountMap.getOrDefault(ht.getId(), 0L));
            houseTypeDistribution.add(item);
        }
        data.put("houseTypeDistribution", houseTypeDistribution);

        // 6b. 投诉类型分布（饼图）
        List<Map<String, Object>> complaintTypeDistribution = buildComplaintTypeDistribution();
        data.put("complaintTypeDistribution", complaintTypeDistribution);

        // 6c. 报修单按维修类型分布（管理端柱状图）
        List<Map<String, Object>> repairTypeDistribution = buildRepairTypeDistribution();
        data.put("repairTypeDistribution", repairTypeDistribution);

        return Result.success(data);
    }

    /**
     * 本月服务满意度：本月创建报修中已完成占比、本月创建投诉中已解决占比，各 50% 权重；若某一类本月无单则仅算另一类。
     */
    private int computeMonthlyServiceSatisfaction(LocalDateTime rangeStart, LocalDateTime rangeEndExclusive) {
        long rTotal = repairRecordMapper.selectCount(
                new LambdaQueryWrapper<RepairRecord>()
                        .ge(RepairRecord::getCreatedAt, rangeStart)
                        .lt(RepairRecord::getCreatedAt, rangeEndExclusive));
        long rDone = repairRecordMapper.selectCount(
                new LambdaQueryWrapper<RepairRecord>()
                        .ge(RepairRecord::getCreatedAt, rangeStart)
                        .lt(RepairRecord::getCreatedAt, rangeEndExclusive)
                        .eq(RepairRecord::getStatus, RepairRecord.Status.COMPLETED));
        long cTotal = complaintMapper.selectCount(
                new LambdaQueryWrapper<Complaint>()
                        .ge(Complaint::getCreatedAt, rangeStart)
                        .lt(Complaint::getCreatedAt, rangeEndExclusive));
        long cResolved = complaintMapper.selectCount(
                new LambdaQueryWrapper<Complaint>()
                        .ge(Complaint::getCreatedAt, rangeStart)
                        .lt(Complaint::getCreatedAt, rangeEndExclusive)
                        .eq(Complaint::getStatus, Complaint.Status.RESOLVED));

        if (rTotal == 0 && cTotal == 0) {
            return 0;
        }
        double rr = rTotal > 0 ? (double) rDone / rTotal : -1;
        double cr = cTotal > 0 ? (double) cResolved / cTotal : -1;
        int score;
        if (rr >= 0 && cr >= 0) {
            score = (int) Math.round((rr * 0.5 + cr * 0.5) * 100);
        } else if (rr >= 0) {
            score = (int) Math.round(rr * 100);
        } else {
            score = (int) Math.round(cr * 100);
        }
        return Math.min(100, Math.max(0, score));
    }

    /** 与上期相比的百分点差，如 +5% / -3% / +0% */
    private static String formatPercentPointTrend(int current, int previous) {
        int diff = current - previous;
        if (diff == 0) {
            return "+0%";
        }
        return String.format("%+d%%", diff);
    }

    private static final List<String> COMPLAINT_TYPE_CODES = Arrays.asList(
            "PROPERTY", "ENVIRONMENT", "NOISE", "FACILITY", "PARKING", "OTHER");

    private static String normalizeComplaintTypeCode(String raw) {
        if (raw == null || raw.isBlank()) {
            return "OTHER";
        }
        String u = raw.trim().toUpperCase();
        return COMPLAINT_TYPE_CODES.contains(u) ? u : "OTHER";
    }

    private static String complaintTypeLabel(String code) {
        return switch (code) {
            case "PROPERTY" -> "物业服务";
            case "ENVIRONMENT" -> "环境卫生";
            case "NOISE" -> "噪音扰民";
            case "FACILITY" -> "设施损坏";
            case "PARKING" -> "停车相关问题";
            default -> "其他";
        };
    }

    /**
     * 投诉类型分布（与 complaint.complaint_type 一致；历史无类型记为「其他」）
     */
    private List<Map<String, Object>> buildComplaintTypeDistribution() {
        Map<String, Long> counts = new HashMap<>();
        for (String code : COMPLAINT_TYPE_CODES) {
            counts.put(code, 0L);
        }
        for (Complaint c : complaintMapper.selectList(null)) {
            String code = normalizeComplaintTypeCode(c.getComplaintType());
            counts.merge(code, 1L, Long::sum);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (String code : COMPLAINT_TYPE_CODES) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", complaintTypeLabel(code));
            item.put("value", counts.getOrDefault(code, 0L));
            out.add(item);
        }
        return out;
    }

    /**
     * 报修记录按维修项目类型（repair_type）统计条数，供管理端柱状图
     */
    private List<Map<String, Object>> buildRepairTypeDistribution() {
        List<RepairType> types = repairTypeMapper.selectList(null);
        List<RepairRecord> allRepairs = repairRecordMapper.selectList(null);
        Map<Long, Long> countByTypeId = allRepairs.stream()
                .filter(r -> r.getRepairTypeId() != null)
                .collect(Collectors.groupingBy(RepairRecord::getRepairTypeId, Collectors.counting()));
        long nullTypeCount = allRepairs.stream()
                .filter(r -> r.getRepairTypeId() == null)
                .count();

        List<Map<String, Object>> out = new ArrayList<>();
        for (RepairType t : types) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", t.getName() != null ? t.getName() : ("类型#" + t.getId()));
            item.put("value", countByTypeId.getOrDefault(t.getId(), 0L));
            out.add(item);
        }
        if (nullTypeCount > 0) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", "未指定类型");
            item.put("value", nullTypeCount);
            out.add(item);
        }
        return out;
    }

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter D_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 业主首页「我的待办」列表与横幅数量：与数据库未结事项一一对应。
     */
    public Result<?> getOwnerPendingTodos(Long userId) {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> todos = new ArrayList<>();
        if (userId == null) {
            data.put("pendingTodoCount", 0L);
            data.put("todos", todos);
            return Result.success(data);
        }
        Owner owner = ownerMapper.selectOne(
                new LambdaQueryWrapper<Owner>().eq(Owner::getUserId, userId).last("LIMIT 1"));
        if (owner == null) {
            data.put("pendingTodoCount", 0L);
            data.put("todos", todos);
            return Result.success(data);
        }
        Long ownerId = owner.getId();

        List<PropertyFee> unpaidFees = propertyFeeMapper.selectList(
                new LambdaQueryWrapper<PropertyFee>()
                        .eq(PropertyFee::getOwnerId, ownerId)
                        .eq(PropertyFee::getStatus, PropertyFee.Status.UNPAID)
                        .orderByDesc(PropertyFee::getFeeDate));
        for (PropertyFee pf : unpaidFees) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", "pf-" + pf.getId());
            row.put("propertyFeeId", pf.getId());
            if (pf.getAmount() != null) {
                row.put("feeAmount", pf.getAmount());
            }
            if (pf.getOrderNo() != null && !pf.getOrderNo().isEmpty()) {
                row.put("orderNo", pf.getOrderNo());
            }
            row.put("feeMonthYm", pf.getFeeDate() != null
                    ? pf.getFeeDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))
                    : "");
            row.put("title", "物业费待缴纳");
            String monthStr = pf.getFeeDate() != null ? pf.getFeeDate().format(D_FMT) : "-";
            row.put("description", "所属月份：" + monthStr + " · 金额：¥" + (pf.getAmount() != null ? pf.getAmount().toPlainString() : "0"));
            row.put("deadline", monthStr);
            row.put("icon", "el-icon-money");
            row.put("action", "/property-fee");
            LocalDateTime pfSort = pf.getCreatedAt() != null
                    ? pf.getCreatedAt()
                    : (pf.getFeeDate() != null ? pf.getFeeDate().atStartOfDay() : null);
            row.put("sortTime", toEpochMillis(pfSort));
            todos.add(row);
        }

        List<RepairRecord> repairs = repairRecordMapper.selectList(
                new LambdaQueryWrapper<RepairRecord>()
                        .eq(RepairRecord::getApplicantId, ownerId)
                        .in(RepairRecord::getStatus, Arrays.asList(
                                RepairRecord.Status.PENDING,
                                RepairRecord.Status.IN_PROGRESS))
                        .orderByDesc(RepairRecord::getCreatedAt));
        for (RepairRecord rr : repairs) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", "rp-" + rr.getId());
            row.put("title", rr.getStatus() == RepairRecord.Status.PENDING ? "报修待接单" : "报修处理中");
            String desc = rr.getDescription();
            if (desc != null && desc.length() > 100) {
                desc = desc.substring(0, 100) + "...";
            }
            row.put("description", desc != null ? desc : "-");
            if (rr.getExpectedTime() != null) {
                row.put("deadline", rr.getExpectedTime().format(DT_FMT));
            } else if (rr.getCreatedAt() != null) {
                row.put("deadline", rr.getCreatedAt().format(DT_FMT));
            } else {
                row.put("deadline", "-");
            }
            row.put("icon", "el-icon-service");
            row.put("action", "/repair");
            row.put("sortTime", toEpochMillis(rr.getCreatedAt()));
            todos.add(row);
        }

        List<Complaint> complaints = complaintMapper.selectList(
                new LambdaQueryWrapper<Complaint>()
                        .eq(Complaint::getComplainantId, ownerId)
                        .in(Complaint::getStatus, Arrays.asList(
                                Complaint.Status.PENDING,
                                Complaint.Status.PROCESSING))
                        .orderByDesc(Complaint::getCreatedAt));
        for (Complaint c : complaints) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", "cp-" + c.getId());
            row.put("title", c.getStatus() == Complaint.Status.PENDING ? "投诉待处理" : "投诉处理中");
            String desc = c.getContent();
            if (desc != null) {
                desc = desc.replaceAll("<[^>]+>", "");
                if (desc.length() > 100) {
                    desc = desc.substring(0, 100) + "...";
                }
            }
            row.put("description", desc != null && !desc.isEmpty() ? desc : "-");
            row.put("deadline", c.getCreatedAt() != null ? c.getCreatedAt().format(DT_FMT) : "-");
            row.put("icon", "el-icon-chat-line-round");
            row.put("action", "/complaint");
            row.put("sortTime", toEpochMillis(c.getCreatedAt()));
            todos.add(row);
        }

        todos.sort((a, b) -> {
            long ta = ((Number) a.getOrDefault("sortTime", 0L)).longValue();
            long tb = ((Number) b.getOrDefault("sortTime", 0L)).longValue();
            return Long.compare(tb, ta);
        });

        data.put("pendingTodoCount", (long) todos.size());
        data.put("todos", todos);
        return Result.success(data);
    }

    /**
     * 业主端首页：本月报修类型统计（每个类型：报修总数、已完成数）
     */
    public Result<?> getOwnerRepairTypeMonthly(Long userId) {
        Map<String, Object> data = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Integer> created = new ArrayList<>();
        List<Integer> completed = new ArrayList<>();

        if (userId == null) {
            data.put("labels", labels);
            data.put("createdCounts", created);
            data.put("completedCounts", completed);
            return Result.success(data);
        }

        Owner owner = ownerMapper.selectOne(
                new LambdaQueryWrapper<Owner>().eq(Owner::getUserId, userId).last("LIMIT 1"));
        if (owner == null) {
            data.put("labels", labels);
            data.put("createdCounts", created);
            data.put("completedCounts", completed);
            return Result.success(data);
        }

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);
        Long ownerId = owner.getId();

        List<RepairType> types = repairTypeMapper.selectList(new LambdaQueryWrapper<RepairType>()
                .orderByAsc(RepairType::getId));

        for (RepairType type : types) {
            labels.add(type.getName() != null ? type.getName() : ("类型#" + type.getId()));
            long total = repairRecordMapper.selectCount(new LambdaQueryWrapper<RepairRecord>()
                    .eq(RepairRecord::getApplicantId, ownerId)
                    .eq(RepairRecord::getRepairTypeId, type.getId())
                    .ge(RepairRecord::getCreatedAt, startOfMonth)
                    .lt(RepairRecord::getCreatedAt, endOfMonth));
            long done = repairRecordMapper.selectCount(new LambdaQueryWrapper<RepairRecord>()
                    .eq(RepairRecord::getApplicantId, ownerId)
                    .eq(RepairRecord::getRepairTypeId, type.getId())
                    .ge(RepairRecord::getCreatedAt, startOfMonth)
                    .lt(RepairRecord::getCreatedAt, endOfMonth)
                    .eq(RepairRecord::getStatus, RepairRecord.Status.COMPLETED));
            created.add((int) total);
            completed.add((int) done);
        }

        data.put("labels", labels);
        data.put("createdCounts", created);
        data.put("completedCounts", completed);
        return Result.success(data);
    }

    private static long toEpochMillis(LocalDateTime ldt) {
        if (ldt == null) {
            return 0L;
        }
        return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private static final DateTimeFormatter YM_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * 维修工首页看板（方案 A：不新增状态枚举）。
     * 今日新派单：今日创建且已指派给该师傅、未取消。
     * 在办工单：PENDING + IN_PROGRESS（当前库无「已接单未开工」独立状态）。
     * 待处理扇区：占位 0（与 ACCEPTED 对齐需后续扩展状态机）。
     */
    public Result<?> getMaintainerOverview(Long userId) {
        Map<String, Object> data = new HashMap<>();
        if (userId == null) {
            return Result.error("-1", "userId 不能为空");
        }
        MaintenanceStaff staff = maintenanceStaffMapper.selectOne(
                new LambdaQueryWrapper<MaintenanceStaff>()
                        .eq(MaintenanceStaff::getUserId, userId)
                        .last("LIMIT 1"));
        if (staff == null) {
            data.put("hasStaffProfile", false);
            data.put("todayNewAssigned", 0);
            data.put("inWorkOrders", 0);
            data.put("monthCompleted", 0);
            data.put("hasAvgRating", false);
            data.put("avgRating", null);
            data.put("repairTypePie", List.of());
            data.put("ratingBars", List.of());
            data.put("completionTrendMonths", List.of());
            data.put("completionTrendCounts", List.of());
            data.put("statusRing", List.of());
            return Result.success(data);
        }
        Long mid = staff.getId();
        data.put("hasStaffProfile", true);

        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = monthStart.plusMonths(1);

        long todayNew = repairRecordMapper.selectCount(
                new LambdaQueryWrapper<RepairRecord>()
                        .eq(RepairRecord::getMaintainerId, mid)
                        .ge(RepairRecord::getCreatedAt, dayStart)
                        .lt(RepairRecord::getCreatedAt, dayEnd)
                        .ne(RepairRecord::getStatus, RepairRecord.Status.CANCELLED));

        long inWork = repairRecordMapper.selectCount(
                new LambdaQueryWrapper<RepairRecord>()
                        .eq(RepairRecord::getMaintainerId, mid)
                        .in(RepairRecord::getStatus, Arrays.asList(
                                RepairRecord.Status.PENDING,
                                RepairRecord.Status.IN_PROGRESS)));

        long monthDone = repairRecordMapper.selectCount(
                new LambdaQueryWrapper<RepairRecord>()
                        .eq(RepairRecord::getMaintainerId, mid)
                        .eq(RepairRecord::getStatus, RepairRecord.Status.COMPLETED)
                        .and(w -> w
                                .and(w2 -> w2.isNotNull(RepairRecord::getActualTime)
                                        .ge(RepairRecord::getActualTime, monthStart)
                                        .lt(RepairRecord::getActualTime, monthEnd))
                                .or(w2 -> w2.isNull(RepairRecord::getActualTime)
                                        .ge(RepairRecord::getUpdatedAt, monthStart)
                                        .lt(RepairRecord::getUpdatedAt, monthEnd))));

        List<RepairRecord> ratedList = repairRecordMapper.selectList(
                new LambdaQueryWrapper<RepairRecord>()
                        .eq(RepairRecord::getMaintainerId, mid)
                        .eq(RepairRecord::getStatus, RepairRecord.Status.COMPLETED)
                        .isNotNull(RepairRecord::getEvaluationRating));
        boolean hasAvg = !ratedList.isEmpty();
        Double avgRating = null;
        if (hasAvg) {
            double sum = ratedList.stream().mapToInt(r -> r.getEvaluationRating() != null ? r.getEvaluationRating() : 0).sum();
            avgRating = BigDecimal.valueOf(sum)
                    .divide(BigDecimal.valueOf(ratedList.size()), 1, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        data.put("todayNewAssigned", todayNew);
        data.put("inWorkOrders", inWork);
        data.put("monthCompleted", monthDone);
        data.put("hasAvgRating", hasAvg);
        data.put("avgRating", avgRating);

        List<RepairRecord> mine = repairRecordMapper.selectList(
                new LambdaQueryWrapper<RepairRecord>()
                        .eq(RepairRecord::getMaintainerId, mid)
                        .ne(RepairRecord::getStatus, RepairRecord.Status.CANCELLED));

        Map<Long, Long> byType = mine.stream()
                .filter(r -> r.getRepairTypeId() != null)
                .collect(Collectors.groupingBy(RepairRecord::getRepairTypeId, Collectors.counting()));
        List<RepairType> types = repairTypeMapper.selectList(null);
        List<Map<String, Object>> repairTypePie = new ArrayList<>();
        for (RepairType t : types) {
            long v = byType.getOrDefault(t.getId(), 0L);
            if (v > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", t.getName() != null ? t.getName() : ("类型#" + t.getId()));
                item.put("value", v);
                repairTypePie.add(item);
            }
        }
        data.put("repairTypePie", repairTypePie);

        int[] starCounts = new int[6];
        for (RepairRecord r : mine) {
            if (r.getStatus() != RepairRecord.Status.COMPLETED) {
                continue;
            }
            if (r.getEvaluationRating() != null) {
                int s = r.getEvaluationRating();
                if (s >= 1 && s <= 5) {
                    starCounts[s]++;
                }
            }
        }
        List<Map<String, Object>> ratingBars = new ArrayList<>();
        for (int s = 1; s <= 5; s++) {
            Map<String, Object> row = new HashMap<>();
            row.put("star", s);
            row.put("label", s + "星");
            row.put("value", starCounts[s]);
            ratingBars.add(row);
        }
        data.put("ratingBars", ratingBars);

        List<String> trendMonths = new ArrayList<>();
        List<Integer> trendCounts = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = LocalDate.now().withDayOfMonth(1).minusMonths(i);
            LocalDateTime ms = monthDate.atStartOfDay();
            LocalDateTime me = monthDate.plusMonths(1).atStartOfDay();
            long cnt = repairRecordMapper.selectCount(
                    new LambdaQueryWrapper<RepairRecord>()
                            .eq(RepairRecord::getMaintainerId, mid)
                            .eq(RepairRecord::getStatus, RepairRecord.Status.COMPLETED)
                            .and(w -> w
                                    .and(w2 -> w2.isNotNull(RepairRecord::getActualTime)
                                            .ge(RepairRecord::getActualTime, ms)
                                            .lt(RepairRecord::getActualTime, me))
                                    .or(w2 -> w2.isNull(RepairRecord::getActualTime)
                                            .ge(RepairRecord::getUpdatedAt, ms)
                                            .lt(RepairRecord::getUpdatedAt, me))));
            trendMonths.add(monthDate.format(YM_FMT));
            trendCounts.add((int) cnt);
        }
        data.put("completionTrendMonths", trendMonths);
        data.put("completionTrendCounts", trendCounts);

        long cPending = mine.stream().filter(r -> r.getStatus() == RepairRecord.Status.PENDING).count();
        long cProg = mine.stream().filter(r -> r.getStatus() == RepairRecord.Status.IN_PROGRESS).count();
        long cDone = mine.stream().filter(r -> r.getStatus() == RepairRecord.Status.COMPLETED).count();

        List<Map<String, Object>> statusRing = new ArrayList<>();
        statusRing.add(mapStatusSlice("待接单", cPending));
        statusRing.add(mapStatusSlice("待处理", 0L));
        statusRing.add(mapStatusSlice("处理中", cProg));
        statusRing.add(mapStatusSlice("已完成", cDone));
        data.put("statusRing", statusRing);

        long ringTotal = cPending + cProg + cDone;
        data.put("statusRingTotal", ringTotal);

        return Result.success(data);
    }

    private static Map<String, Object> mapStatusSlice(String name, long value) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("value", value);
        return m;
    }

    /**
     * 家政人员端数据看板：按 housekeeper.user_id 关联，统计本人订单。
     */
    public Result<?> getHousekeeperOverview(Long userId) {
        Map<String, Object> data = new HashMap<>();
        if (userId == null) {
            return Result.error("-1", "userId 不能为空");
        }
        Housekeeper hk = housekeeperMapper.selectOne(
                new LambdaQueryWrapper<Housekeeper>()
                        .eq(Housekeeper::getUserId, userId)
                        .last("LIMIT 1"));
        if (hk == null) {
            data.put("hasHousekeeperProfile", false);
            data.put("todayNewOrders", 0);
            data.put("inProgressOrders", 0);
            data.put("monthCompleted", 0);
            data.put("hasAvgRating", false);
            data.put("avgRating", null);
            data.put("servicePie", List.of());
            data.put("ratingBars", List.of());
            data.put("completionTrendMonths", List.of());
            data.put("completionTrendCounts", List.of());
            data.put("statusRing", List.of());
            return Result.success(data);
        }
        Long hid = hk.getId();
        data.put("hasHousekeeperProfile", true);

        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = monthStart.plusMonths(1);

        long todayNew = housekeepingOrderMapper.selectCount(
                new LambdaQueryWrapper<HousekeepingOrder>()
                        .eq(HousekeepingOrder::getHousekeeperId, hid)
                        .ge(HousekeepingOrder::getCreatedAt, dayStart)
                        .lt(HousekeepingOrder::getCreatedAt, dayEnd)
                        .ne(HousekeepingOrder::getStatus, "CANCELLED"));

        long inProgress = housekeepingOrderMapper.selectCount(
                new LambdaQueryWrapper<HousekeepingOrder>()
                        .eq(HousekeepingOrder::getHousekeeperId, hid)
                        .in(HousekeepingOrder::getStatus, Arrays.asList("PENDING", "CONFIRMED", "IN_PROGRESS")));

        long monthDone = housekeepingOrderMapper.selectCount(
                new LambdaQueryWrapper<HousekeepingOrder>()
                        .eq(HousekeepingOrder::getHousekeeperId, hid)
                        .eq(HousekeepingOrder::getStatus, "COMPLETED")
                        .ge(HousekeepingOrder::getUpdatedAt, monthStart)
                        .lt(HousekeepingOrder::getUpdatedAt, monthEnd));

        List<HousekeepingOrder> ratedList = housekeepingOrderMapper.selectList(
                new LambdaQueryWrapper<HousekeepingOrder>()
                        .eq(HousekeepingOrder::getHousekeeperId, hid)
                        .eq(HousekeepingOrder::getStatus, "COMPLETED")
                        .isNotNull(HousekeepingOrder::getEvaluationRating));
        boolean hasAvg = !ratedList.isEmpty();
        Double avgRating = null;
        if (hasAvg) {
            double sum = ratedList.stream()
                    .mapToInt(r -> r.getEvaluationRating() != null ? r.getEvaluationRating() : 0)
                    .sum();
            avgRating = BigDecimal.valueOf(sum)
                    .divide(BigDecimal.valueOf(ratedList.size()), 1, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        data.put("todayNewOrders", todayNew);
        data.put("inProgressOrders", inProgress);
        data.put("monthCompleted", monthDone);
        data.put("hasAvgRating", hasAvg);
        data.put("avgRating", avgRating);

        List<HousekeepingOrder> mine = housekeepingOrderMapper.selectList(
                new LambdaQueryWrapper<HousekeepingOrder>()
                        .eq(HousekeepingOrder::getHousekeeperId, hid)
                        .ne(HousekeepingOrder::getStatus, "CANCELLED"));

        Map<Long, Long> byService = mine.stream()
                .filter(o -> o.getServiceId() != null)
                .collect(Collectors.groupingBy(HousekeepingOrder::getServiceId, Collectors.counting()));
        List<Map<String, Object>> servicePie = new ArrayList<>();
        for (Map.Entry<Long, Long> e : byService.entrySet()) {
            HousekeepingService svc = housekeepingServiceCatalogMapper.selectById(e.getKey());
            if (svc == null) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("name", svc.getName() != null ? svc.getName() : ("服务#" + e.getKey()));
            item.put("value", e.getValue());
            servicePie.add(item);
        }
        data.put("servicePie", servicePie);

        int[] starCounts = new int[6];
        for (HousekeepingOrder r : mine) {
            if (!"COMPLETED".equals(r.getStatus()) || r.getEvaluationRating() == null) {
                continue;
            }
            int s = r.getEvaluationRating();
            if (s >= 1 && s <= 5) {
                starCounts[s]++;
            }
        }
        List<Map<String, Object>> ratingBars = new ArrayList<>();
        for (int s = 1; s <= 5; s++) {
            Map<String, Object> row = new HashMap<>();
            row.put("star", s);
            row.put("label", s + "星");
            row.put("value", starCounts[s]);
            ratingBars.add(row);
        }
        data.put("ratingBars", ratingBars);

        List<String> trendMonths = new ArrayList<>();
        List<Integer> trendCounts = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = LocalDate.now().withDayOfMonth(1).minusMonths(i);
            LocalDateTime ms = monthDate.atStartOfDay();
            LocalDateTime me = monthDate.plusMonths(1).atStartOfDay();
            long cnt = housekeepingOrderMapper.selectCount(
                    new LambdaQueryWrapper<HousekeepingOrder>()
                            .eq(HousekeepingOrder::getHousekeeperId, hid)
                            .eq(HousekeepingOrder::getStatus, "COMPLETED")
                            .ge(HousekeepingOrder::getUpdatedAt, ms)
                            .lt(HousekeepingOrder::getUpdatedAt, me));
            trendMonths.add(monthDate.format(YM_FMT));
            trendCounts.add((int) cnt);
        }
        data.put("completionTrendMonths", trendMonths);
        data.put("completionTrendCounts", trendCounts);

        long cPending = mine.stream().filter(o -> "PENDING".equals(o.getStatus())).count();
        long cConfirmed = mine.stream().filter(o -> "CONFIRMED".equals(o.getStatus())).count();
        long cProg = mine.stream().filter(o -> "IN_PROGRESS".equals(o.getStatus())).count();
        long cDone = mine.stream().filter(o -> "COMPLETED".equals(o.getStatus())).count();

        List<Map<String, Object>> statusRing = new ArrayList<>();
        statusRing.add(mapStatusSlice("待接单", cPending));
        statusRing.add(mapStatusSlice("已接单", cConfirmed));
        statusRing.add(mapStatusSlice("进行中", cProg));
        statusRing.add(mapStatusSlice("已完成", cDone));
        data.put("statusRing", statusRing);

        return Result.success(data);
    }
}
