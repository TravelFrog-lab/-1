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

    @GetMapping("/parking/stats")
    @Operation(summary = "获取停车统计")
    public Result<?> getParkingStats() {
        return dashboardService.getParkingStats();
    }

    @GetMapping("/community-stats")
    @Operation(summary = "首页社区数据概览")
    public Result<?> getCommunityStats() {
        return dashboardService.getCommunityStats();
    }
} 