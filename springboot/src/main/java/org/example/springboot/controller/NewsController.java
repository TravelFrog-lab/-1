package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.News;
import org.example.springboot.service.NewsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/news")
@Tag(name = "新闻管理", description = "新闻的增删改查接口")
public class NewsController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NewsController.class);
    
    @Autowired
    private NewsService newsService;

    @GetMapping("/page")
    @Operation(summary = "分页查询新闻列表")
    public Result<Page<News>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "标题") @RequestParam(required = false) String title) {
        return newsService.list(pageNum, pageSize, title);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询新闻")
    public Result<News> getById(
            @Parameter(description = "新闻ID") @PathVariable Long id) {
        return newsService.getById(id);
    }

    @PostMapping
    @Operation(summary = "新增新闻")
    public Result<?> add(@RequestBody News news) {
        return newsService.add(news);
    }

    @PutMapping
    @Operation(summary = "更新新闻")
    public Result<?> update(@RequestBody News news) {
        return newsService.update(news);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除新闻")
    public Result<?> delete(
            @Parameter(description = "新闻ID") @PathVariable Long id) {
        return newsService.delete(id);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除新闻")
    public Result<?> batchDelete(
            @Parameter(description = "新闻ID列表") @RequestBody List<Long> ids) {
        return newsService.batchDelete(ids);
    }
} 