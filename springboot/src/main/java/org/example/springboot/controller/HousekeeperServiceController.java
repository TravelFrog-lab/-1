package org.example.springboot.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.HousekeeperServiceRelation;
import org.example.springboot.service.HousekeeperServiceRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/housekeeper-service")
@Tag(name = "家政人员服务项目关联管理", description = "家政人员服务项目关联相关接口")
public class HousekeeperServiceController {
    
    @Autowired
    private HousekeeperServiceRelationService housekeeperServiceRelationService;
    @GetMapping("/service/{serviceId}")
    @Operation(summary = "根据服务项目ID获取家政人员列表")
    public Result<List<HousekeeperServiceRelation>> listByService(@PathVariable Long serviceId) {
        return housekeeperServiceRelationService.listByService(serviceId);
    }
    @GetMapping("/housekeeper/{housekeeperId}")
    @Operation(summary = "根据家政人员ID获取服务项目列表")
    public Result<List<HousekeeperServiceRelation>> listByHousekeeper(@PathVariable Long housekeeperId) {
        return housekeeperServiceRelationService.listByHousekeeper(housekeeperId);
    }
    
    @PostMapping
    @Operation(summary = "添加家政人员服务项目关联")
    public Result<?> save(@RequestBody HousekeeperServiceRelation housekeeperServiceRelation) {
        return housekeeperServiceRelationService.save(housekeeperServiceRelation);
    }
    
    @PutMapping
    @Operation(summary = "更新家政人员服务项目关联")
    public Result<?> update(@RequestBody HousekeeperServiceRelation housekeeperServiceRelation) {
        return housekeeperServiceRelationService.update(housekeeperServiceRelation);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除家政人员服务项目关联")
    public Result<?> delete(@PathVariable Long id) {
        return housekeeperServiceRelationService.delete(id);
    }
    
    @PostMapping("/batch/{housekeeperId}")
    @Operation(summary = "批量保存家政人员服务项目关联")
    public Result<?> batchSave(@PathVariable Long housekeeperId, @RequestBody List<HousekeeperServiceRelation> services) {
        return housekeeperServiceRelationService.batchSave(housekeeperId, services);
    }
} 