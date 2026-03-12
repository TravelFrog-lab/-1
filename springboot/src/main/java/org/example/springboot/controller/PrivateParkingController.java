package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.PrivateParking;
import org.example.springboot.service.PrivateParkingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/private-parking")
@Tag(name = "私人车位使用记录管理", description = "私人车位使用记录的增删改查接口")
public class PrivateParkingController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PrivateParkingController.class);
    
    @Autowired
    private PrivateParkingService privateParkingService;

    @GetMapping("/page")
    @Operation(summary = "分页查询私人车位使用记录列表")
    public Result<Page<PrivateParking>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "车牌信息") @RequestParam(required = false) String plateInfo,
            @Parameter(description = "ownerId") @RequestParam(required = false) String ownerId,
            @Parameter(description = "ownerName") @RequestParam(required = false) String ownerName,
            @Parameter(description = "location") @RequestParam(required = false) String location




    ) {
        return Result.success(privateParkingService.list(pageNum, pageSize, plateInfo, ownerId,ownerName,location));
    }


    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询私人车位使用记录")
    public Result<PrivateParking> getById(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        return privateParkingService.getById(id);
    }

    // 根据owenrId查询私人车位使用记录
    @GetMapping("/ownerId/{ownerId}")
    @Operation(summary = "根据ownerId查询私人车位使用记录")
    public Result<List<PrivateParking>> getByOwerId(
            @Parameter(description = "ownerId") @PathVariable String ownerId) {
        return Result.success(privateParkingService.list(1,Integer.MAX_VALUE,null,ownerId,null,null).getRecords());
    }

    @PostMapping
    @Operation(summary = "新增私人车位使用记录")
    public Result<?> add(@RequestBody PrivateParking privateParking) {
        return privateParkingService.add(privateParking);
    }

    @PutMapping
    @Operation(summary = "更新私人车位使用记录")
    public Result<?> update(@RequestBody PrivateParking privateParking) {
        return privateParkingService.update(privateParking);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除私人车位使用记录")
    public Result<?> delete(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        return privateParkingService.delete(id);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除私人车位使用记录")
    public Result<?> batchDelete(
            @Parameter(description = "记录ID列表") @RequestBody List<Long> ids) {
        return privateParkingService.batchDelete(ids);
    }
} 