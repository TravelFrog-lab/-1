package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Carousel;
import org.example.springboot.service.CarouselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 固定路径（/page、/all、/add、/update、/batch）须在 /{id} 之前，避免 all、batch、add 等被当成 id。
 */
@RestController
@RequestMapping("/carousels")
@Tag(name = "轮播图管理", description = "轮播图的增删改查接口")
public class CarouselController {

    @Autowired
    private CarouselService carouselService;

    @GetMapping("/page")
    @Operation(summary = "分页查询轮播图列表")
    public Result<Page<Carousel>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "标题") @RequestParam(required = false) String title,
            @Parameter(description = "标签") @RequestParam(required = false) String tag) {
        return carouselService.list(pageNum, pageSize, title, tag);
    }

    @GetMapping("/all")
    @Operation(summary = "查询所有轮播图")
    public Result<List<Carousel>> listAll() {
        return carouselService.listAll();
    }

    @PostMapping("/add")
    @Operation(summary = "新增轮播图")
    public Result<?> add(@RequestBody Carousel carousel) {
        return carouselService.add(carousel);
    }

    @PutMapping("/update")
    @Operation(summary = "更新轮播图")
    public Result<?> update(@RequestBody Carousel carousel) {
        return carouselService.update(carousel);
    }

    @PutMapping("/{id}/sort-order")
    @Operation(summary = "更新轮播图排序")
    public Result<?> updateSortOrder(
            @Parameter(description = "轮播图ID") @PathVariable Long id,
            @Parameter(description = "新排序") @RequestParam Integer newOrder) {
        return carouselService.updateSort(id, newOrder);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询轮播图")
    public Result<Carousel> getById(
            @Parameter(description = "轮播图ID") @PathVariable Long id) {
        return carouselService.getById(id);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除轮播图")
    public Result<?> batchDelete(
            @Parameter(description = "轮播图ID列表") @RequestBody List<Long> ids) {
        return carouselService.batchDelete(ids);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除轮播图")
    public Result<?> delete(
            @Parameter(description = "轮播图ID") @PathVariable Long id) {
        return carouselService.delete(id);
    }
}
