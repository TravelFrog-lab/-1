package com.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.Result;
import com.example.entity.HousekeepingOrder;
import com.example.service.HousekeepingOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/housekeeping-order")
@Tag(name = "家政服务订单管理", description = "家政服务订单相关接口")
public class HousekeepingOrderController {
    
    @Autowired
    private HousekeepingOrderService housekeepingOrderService;
    
    @GetMapping("/page")
    @Operation(summary = "分页查询订单")
    public Result<IPage<HousekeepingOrder>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long housekeeperId) {
        return housekeepingOrderService.page(pageNum, pageSize, orderNo, status, ownerId, housekeeperId);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询订单")
    public Result<HousekeepingOrder> getById(@PathVariable Long id) {
        return housekeepingOrderService.getById(id);
    }
    
    @PostMapping
    @Operation(summary = "创建订单")
    public Result<?> create(@RequestBody HousekeepingOrder order) {
        return housekeepingOrderService.create(order);
    }
    
    @PutMapping
    @Operation(summary = "更新订单")
    public Result<?> update(@RequestBody HousekeepingOrder order) {
        return housekeepingOrderService.update(order);
    }
    
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单")
    public Result<?> cancel(@PathVariable Long id) {
        return housekeepingOrderService.cancel(id);
    }
    
    @PutMapping("/{id}/confirm")
    @Operation(summary = "确认订单")
    public Result<?> confirm(@PathVariable Long id) {
        return housekeepingOrderService.confirm(id);
    }
    
    @PutMapping("/{id}/start")
    @Operation(summary = "开始服务")
    public Result<?> start(@PathVariable Long id) {
        return housekeepingOrderService.start(id);
    }
    
    @PutMapping("/{id}/complete")
    @Operation(summary = "完成服务")
    public Result<?> complete(@PathVariable Long id) {
        return housekeepingOrderService.complete(id);
    }
    
    @PutMapping("/{id}/pay")
    @Operation(summary = "支付订单")
    public Result<?> pay(@PathVariable Long id) {
        return housekeepingOrderService.pay(id);
    }
} 