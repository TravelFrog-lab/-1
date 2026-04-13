package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.HouseType;
import org.example.springboot.service.HouseTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 固定路径 /page、/all、/batch 必须在 /{id} 之前，否则 /house-types/page 等会误匹配 → 500。
 */
@RestController
@RequestMapping("/house-types")
@Tag(name = "房屋类型管理", description = "房屋类型的增删改查接口")
public class HouseTypeController {

    @Autowired
    private HouseTypeService houseTypeService;

    @GetMapping("/page")
    @Operation(summary = "分页查询房屋类型列表")
    public Result<Page<HouseType>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "类型名称") @RequestParam(required = false) String name) {
        return Result.success(houseTypeService.list(pageNum, pageSize, name));
    }

    @GetMapping("/all")
    @Operation(summary = "查询所有房屋类型")
    public Result<List<HouseType>> getAll() {
        return Result.success(houseTypeService.list(1, Integer.MAX_VALUE, null).getRecords());
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询房屋类型")
    public Result<HouseType> getById(
            @Parameter(description = "房屋类型ID") @PathVariable Long id) {
        return houseTypeService.getById(id);
    }

    @PostMapping
    @Operation(summary = "新增房屋类型")
    public Result<?> add(@RequestBody HouseType houseType) {
        return houseTypeService.add(houseType);
    }

    @PutMapping
    @Operation(summary = "更新房屋类型")
    public Result<?> update(@RequestBody HouseType houseType) {
        return houseTypeService.update(houseType);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除房屋类型")
    public Result<?> batchDelete(
            @Parameter(description = "房屋类型ID列表") @RequestBody List<Long> ids) {
        return houseTypeService.batchDelete(ids);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除房屋类型")
    public Result<?> delete(
            @Parameter(description = "房屋类型ID") @PathVariable Long id) {
        return houseTypeService.delete(id);
    }
}
