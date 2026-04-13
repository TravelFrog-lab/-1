package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "数据统计", description = "系统数据统计接口")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/overview")
    @Operation(summary = "获取总览数据")
    public Result<?> getOverview() {
        return dashboardService.getOverview();
    }

    @GetMapping("/property-fee/stats")
    @Operation(summary = "获取物业费统计")
    public Result<?> getPropertyFeeStats(@RequestParam(defaultValue = "1") Integer years) {
        return dashboardService.getPropertyFeeStats(years);
    }

    @GetMapping("/house-type/stats") 
    @Operation(summary = "获取房屋类型统计")
    public Result<?> getHouseTypeStats() {
        return dashboardService.getHouseTypeStats();
    }

    @GetMapping("/complaint/stats")
    @Operation(summary = "获取投诉统计")
    public Result<?> getComplaintStats() {
        return dashboardService.getComplaintStats();
    }

    @GetMapping("/community-stats")
    @Operation(summary = "首页社区数据概览")
    public Result<?> getCommunityStats() {
        return dashboardService.getCommunityStats();
    }

    @GetMapping("/owner-pending-todos")
    @Operation(summary = "业主端首页待办列表与数量（与数据库一致）")
    public Result<?> getOwnerPendingTodos(@RequestParam Long userId) {
        return dashboardService.getOwnerPendingTodos(userId);
    }

    @GetMapping("/owner-repair-type-monthly")
    @Operation(summary = "业主端本月报修类型统计（报修数/完成数）")
    public Result<?> getOwnerRepairTypeMonthly(@RequestParam Long userId) {
        return dashboardService.getOwnerRepairTypeMonthly(userId);
    }

    @GetMapping("/maintainer-overview")
    @Operation(summary = "维修工端首页：个人工单统计与图表（按 maintenance_staff 维度）")
    public Result<?> getMaintainerOverview(@RequestParam Long userId) {
        return dashboardService.getMaintainerOverview(userId);
    }

    @GetMapping("/housekeeper-overview")
    @Operation(summary = "家政人员端数据看板：本人订单统计与图表（按 housekeeper 维度）")
    public Result<?> getHousekeeperOverview(@RequestParam Long userId) {
        return dashboardService.getHousekeeperOverview(userId);
    }
} 