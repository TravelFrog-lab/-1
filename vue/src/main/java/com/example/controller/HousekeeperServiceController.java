package com.example.controller;

import com.example.common.Result;
import com.example.entity.HousekeeperService;
import com.example.service.HousekeeperServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/housekeeper-service")
@Tag(name = "家政人员服务项目关联管理", description = "家政人员服务项目关联相关接口")
public class HousekeeperServiceController {
    
    @Autowired
    private HousekeeperServiceService housekeeperServiceService;
    
    @GetMapping("/housekeeper/{housekeeperId}")
    @Operation(summary = "根据家政人员ID获取服务项目列表")
    public Result<List<HousekeeperService>> listByHousekeeper(@PathVariable Long housekeeperId) {
        return housekeeperServiceService.listByHousekeeper(housekeeperId);
    }
    
    @PostMapping
    @Operation(summary = "添加家政人员服务项目关联")
    public Result<?> save(@RequestBody HousekeeperService housekeeperService) {
        return housekeeperServiceService.save(housekeeperService);
    }
    
    @PutMapping
    @Operation(summary = "更新家政人员服务项目关联")
    public Result<?> update(@RequestBody HousekeeperService housekeeperService) {
        return housekeeperServiceService.update(housekeeperService);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除家政人员服务项目关联")
    public Result<?> delete(@PathVariable Long id) {
        return housekeeperServiceService.delete(id);
    }
    
    @PostMapping("/batch/{housekeeperId}")
    @Operation(summary = "批量保存家政人员服务项目关联")
    public Result<?> batchSave(@PathVariable Long housekeeperId, @RequestBody List<HousekeeperService> services) {
        return housekeeperServiceService.batchSave(housekeeperId, services);
    }
} 