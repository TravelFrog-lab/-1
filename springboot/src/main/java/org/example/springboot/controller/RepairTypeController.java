package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.RepairType;
import org.example.springboot.service.RepairTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/repair-types")
@Tag(name = "维修项目类型管理", description = "维修项目类型的增删改查接口")
public class RepairTypeController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RepairTypeController.class);
    
    @Autowired
    private RepairTypeService repairTypeService;

    @GetMapping("/page")
    @Operation(summary = "分页查询维修项目类型列表")
    public Result<Page<RepairType>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "类型名称") @RequestParam(required = false) String name) {
        return Result.success(repairTypeService.list(pageNum, pageSize, name));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询维修项目类型")
    public Result<RepairType> getById(
            @Parameter(description = "类型ID") @PathVariable Long id) {
        return repairTypeService.getById(id);
    }

    @PostMapping
    @Operation(summary = "新增维修项目类型")
    public Result<?> add(@RequestBody RepairType repairType) {
        return repairTypeService.add(repairType);
    }
    //getAll
    @GetMapping("/all")
    @Operation(summary = "查询所有维修项目类型")
    public Result<List<RepairType>> getAll() {
        return Result.success(repairTypeService.list(1,Integer.MAX_VALUE,null).getRecords());
    }

    @PutMapping
    @Operation(summary = "更新维修项目类型")
    public Result<?> update(@RequestBody RepairType repairType) {
        return repairTypeService.update(repairType);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除维修项目类型")
    public Result<?> delete(
            @Parameter(description = "类型ID") @PathVariable Long id) {
        return repairTypeService.delete(id);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除维修项目类型")
    public Result<?> batchDelete(
            @Parameter(description = "类型ID列表") @RequestBody List<Long> ids) {
        return repairTypeService.batchDelete(ids);
    }
} 