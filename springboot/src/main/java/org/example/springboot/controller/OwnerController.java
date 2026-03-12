package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Owner;
import org.example.springboot.service.OwnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/owner")
@Tag(name = "业主管理", description = "业主信息的增删改查接口")
public class OwnerController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OwnerController.class);
    
    @Autowired
    private OwnerService ownerService;

    @GetMapping("/page")
    @Operation(summary = "分页查询业主列表")
    public Result<Page<Owner>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "身份证号") @RequestParam(required = false) String idCard,
            @Parameter(description = "车牌号") @RequestParam(required = false) String plateNumber) {
        return Result.success(ownerService.list(pageNum, pageSize, idCard, plateNumber));
    }


    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询业主")
    public Result<Owner> getById(
            @Parameter(description = "业主ID") @PathVariable Long id) {
        return ownerService.getById(id);
    }

    @PostMapping
    @Operation(summary = "新增业主")
    public Result<?> add(@RequestBody Owner owner) {
        return ownerService.add(owner);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新业主信息")
    public Result<?> update(@PathVariable Long id, @RequestBody Owner owner) {
        return ownerService.update(id,owner);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除业主")
    public Result<?> delete(
            @Parameter(description = "业主ID") @PathVariable Long id) {
        return ownerService.delete(id);
    }

    
    //getAll
    @GetMapping("/all")
    @Operation(summary = "查询所有业主")
    public Result<List<Owner>> getAll() {
        return Result.success(ownerService.list(1,Integer.MAX_VALUE,null,null).getRecords());
    }


    @DeleteMapping("/batch")
    @Operation(summary = "批量删除业主")
    public Result<?> batchDelete(
            @Parameter(description = "业主ID列表") @RequestBody List<Long> ids) {
        return ownerService.batchDelete(ids);
    }
    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID查询业主")
    public Result<Owner> getByUserId(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        return ownerService.getByUserId(userId);
    }   
} 