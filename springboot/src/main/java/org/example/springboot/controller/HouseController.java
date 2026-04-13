package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.House;
import org.example.springboot.service.HouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 固定路径 /page、/all、/batch 必须在 /{id} 之前，否则 /houses/all 等会被误匹配为 id=all → 500。
 */
@RestController
@RequestMapping("/houses")
@Tag(name = "房屋管理", description = "房屋信息的增删改查接口")
public class HouseController {

    @Autowired
    private HouseService houseService;

    @GetMapping("/page")
    @Operation(summary = "分页查询房屋列表")
    public Result<Page<House>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "房屋地址") @RequestParam(required = false) String address,
            @Parameter(description = "房屋状态") @RequestParam(required = false) House.Status status) {
        return Result.success(houseService.list(pageNum, pageSize, address, status));
    }

    @GetMapping("/all")
    @Operation(summary = "查询所有房屋")
    public Result<List<House>> getAll() {
        return Result.success(houseService.list(1, Integer.MAX_VALUE, null, null).getRecords());
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询房屋")
    public Result<House> getById(
            @Parameter(description = "房屋ID") @PathVariable Long id) {
        return houseService.getById(id);
    }

    @PostMapping
    @Operation(summary = "新增房屋")
    public Result<?> add(@RequestBody House house) {
        return houseService.add(house);
    }

    @PutMapping
    @Operation(summary = "更新房屋信息")
    public Result<?> update(@RequestBody House house) {
        return houseService.update(house);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除房屋")
    public Result<?> batchDelete(
            @Parameter(description = "房屋ID列表") @RequestBody List<Long> ids) {
        return houseService.batchDelete(ids);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除房屋")
    public Result<?> delete(
            @Parameter(description = "房屋ID") @PathVariable Long id) {
        return houseService.delete(id);
    }
}
