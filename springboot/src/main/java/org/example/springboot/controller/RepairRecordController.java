package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.dto.RepairEvaluationRequest;
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
            @Parameter(description = "维修状态（单值，与 statuses 二选一）") @RequestParam(required = false) RepairRecord.Status status,
            @Parameter(description = "维修类型") @RequestParam(required = false) String repairTypeId,
            @Parameter(description = "多个状态，逗号分隔，如 PENDING,WAIT_REASSIGN（与 status 二选一；若传 ownerTab 则忽略）") @RequestParam(required = false) String statuses,
            @Parameter(description = "业主端列表分类 pending/progress/to_evaluate/done（to_evaluate=已完工待评价，done=已评价归档），优先于 status/statuses") @RequestParam(required = false) String ownerTab
            ) {
        return repairRecordService.list(pageNum, pageSize, applicantId, applicantName, status, repairTypeId, maintainerId, statuses, ownerTab);
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

    @PutMapping("/{id}/reject")
    @Operation(summary = "维修人员拒单（填写原因）")
    public Result<?> rejectRepairJob(
            @Parameter(description = "维修记录ID") @PathVariable Long id,
            @Parameter(description = "维修人员ID") @RequestParam Long maintainerId,
            @Parameter(description = "拒单原因") @RequestParam String reason) {
        return repairRecordService.rejectRepairJob(id, maintainerId, reason);
    }

    @PutMapping("/assign/{id}")
    @Operation(summary = "分配维修人员")
    public Result<?> assignMaintainer(
            @Parameter(description = "维修记录ID") @PathVariable Long id,
            @Parameter(description = "维修人员ID") @RequestParam Long maintainerId) {
        return repairRecordService.assignMaintainer(id, maintainerId);
    }

    @PutMapping("/complete/{id}")
    @Operation(summary = "完成维修")
    public Result<?> completeRepair(
            @Parameter(description = "维修记录ID") @PathVariable Long id,
            @Parameter(description = "维修结果描述") @RequestParam String result) {
        return repairRecordService.completeRepair(id, result);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "业主取消待处理报修")
    public Result<?> cancelRepair(
            @Parameter(description = "维修记录ID") @PathVariable Long id,
            @Parameter(description = "申请人ID（业主）") @RequestParam Long applicantId) {
        return repairRecordService.cancelRepair(id, applicantId);
    }

    @PutMapping("/{id}/evaluation")
    @Operation(summary = "业主评价已完成报修（1-5星 + 文字，仅一次）")
    public Result<?> submitOwnerEvaluation(
            @Parameter(description = "维修记录ID") @PathVariable Long id,
            @Parameter(description = "申请人ID（业主）") @RequestParam Long applicantId,
            @RequestBody RepairEvaluationRequest body) {
        return repairRecordService.submitOwnerEvaluation(id, applicantId, body);
    }

    @PutMapping("/complete-force/{id}")
    @Operation(summary = "管理员强制完成维修（不要求已进入处理中）")
    public Result<?> completeForceRepair(
            @Parameter(description = "维修记录ID") @PathVariable Long id,
            @Parameter(description = "维修结果描述") @RequestParam String result) {
        return repairRecordService.completeRepairForce(id, result);
    }

    @PutMapping("/{id}/pay")
    @Operation(summary = "标记维修费用已支付（业主支付完成后调用）")
    public Result<?> pay(
            @Parameter(description = "维修记录ID") @PathVariable Long id) {
        return repairRecordService.markPaid(id);
    }

}

