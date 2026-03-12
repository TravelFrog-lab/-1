package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.ParkingFee;
import org.example.springboot.service.ParkingFeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/parking-fee")
@Tag(name = "停车费管理", description = "停车费记录的增删改查接口")
public class ParkingFeeController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ParkingFeeController.class);
    
    @Autowired
    private ParkingFeeService parkingFeeService;

    @GetMapping("/page")
    @Operation(summary = "分页查询停车费记录列表")
    public Result<Page<ParkingFee>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "车牌号码") @RequestParam(required = false) String plateNumber,
            @Parameter(description = "缴费状态") @RequestParam(required = false) ParkingFee.Status status) {
        return parkingFeeService.list(pageNum, pageSize, plateNumber, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询停车费记录")
    public Result<ParkingFee> getById(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        return parkingFeeService.getById(id);
    }

    @PostMapping
    @Operation(summary = "新增停车费记录")
    public Result<?> add(@RequestBody ParkingFee parkingFee) {
        return parkingFeeService.add(parkingFee);
    }

    @PutMapping
    @Operation(summary = "更新停车费记录")
    public Result<?> update(@RequestBody ParkingFee parkingFee) {
        return parkingFeeService.update(parkingFee);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除停车费记录")
    public Result<?> delete(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        return parkingFeeService.delete(id);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除停车费记录")
    public Result<?> batchDelete(
            @Parameter(description = "记录ID列表") @RequestBody List<Long> ids) {
        return parkingFeeService.batchDelete(ids);
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "缴纳停车费")
    public Result<?> pay(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        return parkingFeeService.pay(id);
    }

    @PostMapping("/{id}/checkout")
    @Operation(summary = "车辆出场")
    public Result<?> checkout(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        return parkingFeeService.checkout(id);
    }

    @GetMapping("/checkPayStatus")
    @Operation(summary = "检查停车费支付状态")
    public Result<?> checkPayStatus(
            @RequestParam String orderNo) {
        return parkingFeeService.checkPayStatus(orderNo);
    }
} 