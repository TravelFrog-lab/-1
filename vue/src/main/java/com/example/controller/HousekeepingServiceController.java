package com.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.Result;
import com.example.entity.HousekeepingService;
import com.example.service.HousekeepingServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/housekeeping-service")
@Tag(name = "家政服务项目管理", description = "家政服务项目相关接口")
public class HousekeepingServiceController {
    
    @Autowired
    private HousekeepingServiceService housekeepingServiceService;
    
    @GetMapping("/page")
    @Operation(summary = "分页查询服务项目")
    public Result<IPage<HousekeepingService>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category) {
        return housekeepingServiceService.page(pageNum, pageSize, name, category);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询服务项目")
    public Result<HousekeepingService> getById(@PathVariable Long id) {
        return housekeepingServiceService.getById(id);
    }
    
    @PostMapping
    @Operation(summary = "新增服务项目")
    public Result<?> save(@RequestBody HousekeepingService service) {
        return housekeepingServiceService.save(service);
    }
    
    @PutMapping
    @Operation(summary = "更新服务项目")
    public Result<?> update(@RequestBody HousekeepingService service) {
        return housekeepingServiceService.update(service);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除服务项目")
    public Result<?> delete(@PathVariable Long id) {
        return housekeepingServiceService.delete(id);
    }
    
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除服务项目")
    public Result<?> batchDelete(@RequestBody List<Long> ids) {
        return housekeepingServiceService.batchDelete(ids);
    }
    
    @GetMapping("/list")
    @Operation(summary = "获取所有服务项目")
    public Result<List<HousekeepingService>> list() {
        return housekeepingServiceService.list();
    }
    
    @GetMapping("/category/{category}")
    @Operation(summary = "根据类别获取服务项目")
    public Result<List<HousekeepingService>> listByCategory(@PathVariable String category) {
        return housekeepingServiceService.listByCategory(category);
    }
} 