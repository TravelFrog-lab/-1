package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Announcement;
import org.example.springboot.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/announcements")
@Tag(name = "公告管理", description = "公告的增删改查接口")
public class AnnouncementController {
    
    @Autowired
    private AnnouncementService announcementService;

    @GetMapping("/page")
    @Operation(summary = "分页查询公告列表")
    public Result<Page<Announcement>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "标题") @RequestParam(required = false) String title,
            @Parameter(description = "类型") @RequestParam(required = false) Announcement.Type type
            
            
            ) {
        return Result.success(announcementService.list(pageNum, pageSize, title,type));
    }

    @GetMapping("/all")
    @Operation(summary = "查询所有公告")
    public Result<List<Announcement>> getAll() {
        return Result.success(announcementService.list(1,Integer.MAX_VALUE,null,null).getRecords());
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询公告")
    public Result<Announcement> getById(
            @Parameter(description = "公告ID") @PathVariable Long id) {
        return announcementService.getById(id);
    }

    @PostMapping
    @Operation(summary = "新增公告")
    public Result<?> add(@RequestBody Announcement announcement) {
        return announcementService.add(announcement);
    }

    @PutMapping
    @Operation(summary = "更新公告")
    public Result<?> update(@RequestBody Announcement announcement) {
        return announcementService.update(announcement);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除公告")
    public Result<?> batchDelete(
            @Parameter(description = "公告ID列表") @RequestBody List<Long> ids) {
        return announcementService.batchDelete(ids);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除公告")
    public Result<?> delete(
            @Parameter(description = "公告ID") @PathVariable Long id) {
        return announcementService.delete(id);
    }
} 