package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.VolunteerActivity;
import org.example.springboot.service.VolunteerActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 固定路径 /batch 必须在 /{id} 之前，否则 batch 会被当成 id。
 */
@RestController
@RequestMapping({"/community-activity", "/volunteer-activity"})
@Tag(name = "小区活动管理", description = "小区活动的增删改查接口")
public class VolunteerActivityController {
    
    @Autowired
    private VolunteerActivityService volunteerActivityService;

    @GetMapping("/page")
    @Operation(summary = "分页查询小区活动列表")
    public Result<Page<VolunteerActivity>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "活动名称") @RequestParam(required = false) String name) {
        return volunteerActivityService.list(pageNum, pageSize, name);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询志愿活动")
    public Result<VolunteerActivity> getById(
            @Parameter(description = "活动ID") @PathVariable Long id) {
        return volunteerActivityService.getById(id);
    }

    @PostMapping
    @Operation(summary = "新增小区活动")
    public Result<?> add(@RequestBody VolunteerActivity activity) {
        return volunteerActivityService.add(activity);
    }

    @PutMapping
    @Operation(summary = "更新小区活动")
    public Result<?> update(@RequestBody VolunteerActivity activity) {
        return volunteerActivityService.update(activity);
    }

    @PostMapping("/{id}/register")
    @Operation(summary = "报名小区活动")
    public Result<?> register(
            @Parameter(description = "活动ID") @PathVariable Long id,
            @Parameter(description = "业主ID") @RequestParam Long volunteerId) {
        return volunteerActivityService.register(id, volunteerId);
    }

    @PostMapping("/{id}/cancel-registration")
    @Operation(summary = "取消报名")
    public Result<?> cancelRegistration(
            @Parameter(description = "活动ID") @PathVariable Long id,
            @Parameter(description = "业主ID") @RequestParam Long volunteerId) {
        return volunteerActivityService.cancelRegistration(id, volunteerId);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除小区活动")
    public Result<?> batchDelete(
            @Parameter(description = "活动ID列表") @RequestBody List<Long> ids) {
        return volunteerActivityService.batchDelete(ids);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除小区活动")
    public Result<?> delete(
            @Parameter(description = "活动ID") @PathVariable Long id) {
        return volunteerActivityService.delete(id);
    }
}
