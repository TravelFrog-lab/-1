package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.PetSitter;
import org.example.springboot.service.PetSitterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pet-sitter")
@Tag(name = "宠物代喂人员管理", description = "宠物代喂人员的增删改查接口")
public class PetSitterController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PetSitterController.class);
    
    @Autowired
    private PetSitterService petSitterService;

    @GetMapping("/page")
    @Operation(summary = "分页查询代喂人员列表")
    public Result<Page<PetSitter>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "姓名") @RequestParam(required = false) String name,
            @Parameter(description = "状态") @RequestParam(required = false) PetSitter.Status status) {
        return Result.success(petSitterService.list(pageNum, pageSize, name, status));
    }


    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询代喂人员")
    public Result<PetSitter> getById(
            @Parameter(description = "人员ID") @PathVariable Long id) {
        return petSitterService.getById(id);
    }

    @PostMapping
    @Operation(summary = "新增代喂人员")
    public Result<?> add(@RequestBody PetSitter sitter) {
        return petSitterService.add(sitter);
    }

    @PutMapping
    @Operation(summary = "更新代喂人员")
    public Result<?> update(@RequestBody PetSitter sitter) {
        return petSitterService.update(sitter);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除代喂人员")
    public Result<?> delete(
            @Parameter(description = "人员ID") @PathVariable Long id) {
        return petSitterService.delete(id);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除代喂人员")
    public Result<?> batchDelete(
            @Parameter(description = "人员ID列表") @RequestBody List<Long> ids) {
        return petSitterService.batchDelete(ids);
    }
    //getAll
    @GetMapping("/all")
    @Operation(summary = "查询所有代喂人员")
    public Result<List<PetSitter>> getAll() {
        return Result.success(petSitterService.list(1,Integer.MAX_VALUE,null,null).getRecords());
    }



    @PutMapping("/{id}/status")
    @Operation(summary = "更新代喂人员状态")
    public Result<?> updateStatus(
            @Parameter(description = "人员ID") @PathVariable Long id,
            @Parameter(description = "状态") @RequestParam PetSitter.Status status) {
        return petSitterService.updateStatus(id, status);
    }
} 