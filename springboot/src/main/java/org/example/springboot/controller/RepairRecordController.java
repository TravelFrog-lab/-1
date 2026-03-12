package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.RepairRecord;
import org.example.springboot.service.RepairRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/repair-records")
@Tag(name = "维修记录管理", description = "维修记录的增删改查接口")
public class RepairRecordController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RepairRecordController.class);
    
    @Autowired
    private RepairRecordService repairRecordService;

    @GetMapping("/page")
    @Operation(summary = "分页查询维修记录列表")
    public Result<Page<RepairRecord>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "申请人ID") @RequestParam(required = false) String applicantId,
            @Parameter(description = "申请人ID") @RequestParam(required = false) String applicantName,
            @Parameter(description = "维修人员Id") @RequestParam(required = false) String maintainerId,
            @Parameter(description = "维修状态") @RequestParam(required = false) RepairRecord.Status status,
            @Parameter(description = "维修类型") @RequestParam(required = false) String repairTypeId
            ) {
        return repairRecordService.list(pageNum, pageSize, applicantId, applicantName, status,repairTypeId,maintainerId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询维修记录")
    public Result<RepairRecord> getById(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        return repairRecordService.getById(id);
    }

    @PostMapping
    @Operation(summary = "新增维修记录")
    public Result<?> add(@RequestBody RepairRecord record) {
        return repairRecordService.add(record);
    }

    @PutMapping
    @Operation(summary = "更新维修记录")
    public Result<?> update(@RequestBody RepairRecord record) {
        return repairRecordService.update(record);
    }

    @PutMapping("/edit-pending")
    @Operation(summary = "业主编辑待处理的维修记录（仅报修类型和描述可修改）")
    public Result<?> editPendingRecord(@RequestBody RepairRecord record) {
        return repairRecordService.editPendingRecord(record);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除维修记录")
    public Result<?> delete(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        return repairRecordService.delete(id);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除维修记录")
    public Result<?> batchDelete(
            @Parameter(description = "记录ID列表") @RequestBody List<Long> ids) {
        return repairRecordService.batchDelete(ids);
    }

    @GetMapping("/pending")
    @Operation(summary = "查询待维修记录列表")
    public Result<Page<RepairRecord>> listPendingRepairs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return repairRecordService.listPendingRepairs(pageNum, pageSize);
    }

    @GetMapping("/maintainer/{maintainerId}")
    @Operation(summary = "查询维修人员的维修记录")
    public Result<Page<RepairRecord>> listMaintainerRepairs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "维修人员ID") @PathVariable Long maintainerId,
            @Parameter(description = "维修状态") @RequestParam(required = false) RepairRecord.Status status) {
        return repairRecordService.listMaintainerRepairs(pageNum, pageSize, maintainerId, status);
    }

    @PostMapping("/{id}/take")
    @Operation(summary = "维修人员接单")
    public Result<?> takeRepairJob(
            @Parameter(description = "维修记录ID") @PathVariable Long id,
            @Parameter(description = "维修人员ID") @RequestParam Long maintainerId) {
        return repairRecordService.takeRepairJob(id, maintainerId);
    }

    @PutMapping("/assign/{id}")
    @Operation(summary = "分配维修人员")
    public Result<?> assignMaintainer(
            @Parameter(description = "维修记录ID") @PathVariable Long id,
            @Parameter(description = "维修人员ID") @RequestParam Long maintainerId) {
        return repairRecordService.assignMaintainer(id, maintainerId);
    }

    @PutMapping("/complete/{id}")
    @Operation(summary = "完成维修（可同时填写费用）")
    public Result<?> completeRepair(
            @Parameter(description = "维修记录ID") @PathVariable Long id,
            @Parameter(description = "维修结果描述") @RequestParam String result,
            @Parameter(description = "维修费用") @RequestParam(required = false) java.math.BigDecimal fee) {
        return repairRecordService.completeRepair(id, result, fee);
    }

    @PutMapping("/{id}/pay")
    @Operation(summary = "标记维修费用已支付（业主支付完成后调用）")
    public Result<?> pay(
            @Parameter(description = "维修记录ID") @PathVariable Long id) {
        return repairRecordService.markPaid(id);
    }

}

