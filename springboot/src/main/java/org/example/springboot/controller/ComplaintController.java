package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Complaint;
import org.example.springboot.service.ComplaintService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/complaints")
@Tag(name = "投诉建议管理", description = "投诉建议的增删改查接口")
public class ComplaintController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ComplaintController.class);
    
    @Autowired
    private ComplaintService complaintService;

    @GetMapping("/page")
    @Operation(summary = "分页查询投诉记录列表")
    public Result<Page<Complaint>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "投诉人") @RequestParam(required = false) String complainantId,
            @Parameter(description = "处理状态") @RequestParam(required = false) Complaint.Status status,
            @Parameter(description = "投诉人姓名") @RequestParam(required = false) String complainantName

            ) {
        return complaintService.list(pageNum, pageSize,complainantId, status,complainantName);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询投诉记录")
    public Result<Complaint> getById(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        return complaintService.getById(id);
    }

    @PostMapping
    @Operation(summary = "新增投诉记录")
    public Result<?> add(@RequestBody Complaint complaint) {
        return complaintService.add(complaint);
    }

    @PutMapping
    @Operation(summary = "更新投诉记录")
    public Result<?> update(@RequestBody Complaint complaint) {
        return complaintService.update(complaint);
    }
    
    @PutMapping("/edit-pending")
    @Operation(summary = "业主编辑待处理的投诉记录（仅投诉内容可修改）")
    public Result<?> editPendingComplaint(@RequestBody Complaint complaint) {
        return complaintService.editPendingComplaint(complaint);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除投诉记录")
    public Result<?> delete(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        return complaintService.delete(id);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除投诉记录")
    public Result<?> batchDelete(
            @Parameter(description = "记录ID列表") @RequestBody List<Long> ids) {
        return complaintService.batchDelete(ids);
    }

} 