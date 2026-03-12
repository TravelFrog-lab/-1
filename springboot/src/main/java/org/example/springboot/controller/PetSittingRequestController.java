package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.PetSittingRequest;
import org.example.springboot.service.PetSittingRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pet-sitting-request")
@Tag(name = "宠物代喂服务请求管理", description = "宠物代喂服务请求的增删改查接口")
public class PetSittingRequestController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PetSittingRequestController.class);
    
    @Autowired
    private PetSittingRequestService petSittingRequestService;

    @GetMapping("/page")
    @Operation(summary = "分页查询服务请求列表")
    public Result<Page<PetSittingRequest>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "业主ID") @RequestParam(required = false) Long ownerId,
            @Parameter(description = "代喂人员ID") @RequestParam(required = false) Long sitterId,
            @Parameter(description = "联系状态") @RequestParam(required = false) PetSittingRequest.Status status,
            @Parameter(description = "业主姓名") @RequestParam(required = false) String ownerName,
            @Parameter(description = "代喂人员姓名") @RequestParam(required = false) String sitterName  ) {
        return petSittingRequestService.list(pageNum, pageSize, ownerId, sitterId, status, ownerName, sitterName);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询服务请求")
    public Result<PetSittingRequest> getById(
            @Parameter(description = "请求ID") @PathVariable Long id) {
        return petSittingRequestService.getById(id);
    }

    @PostMapping
    @Operation(summary = "新增服务请求")
    public Result<?> add(@RequestBody PetSittingRequest request) {
        return petSittingRequestService.add(request);
    }

    @PutMapping
    @Operation(summary = "更新服务请求")
    public Result<?> update(@RequestBody PetSittingRequest request) {
        return petSittingRequestService.update(request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除服务请求")
    public Result<?> delete(
            @Parameter(description = "请求ID") @PathVariable Long id) {
        return petSittingRequestService.delete(id);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除服务请求")
    public Result<?> batchDelete(
            @Parameter(description = "请求ID列表") @RequestBody List<Long> ids) {
        return petSittingRequestService.batchDelete(ids);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新服务请求状态")
    public Result<?> updateStatus(
            @Parameter(description = "请求ID") @PathVariable Long id,
            @Parameter(description = "状态") @RequestParam PetSittingRequest.Status status) {
        PetSittingRequest data = petSittingRequestService.getById(id).getData();
        if(data==null)return Result.error("-1","当前服务不存在");
        data.setStatus(status);

        return petSittingRequestService.update(data);
    }
} 