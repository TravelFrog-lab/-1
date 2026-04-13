package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.dto.PropertyFeePushRequest;
import org.example.springboot.dto.PropertyFeePushResult;
import org.example.springboot.entity.PropertyFee;
import org.example.springboot.service.PropertyFeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * 固定路径（/page、/calculate、/calculateBatch、/checkPayStatus、/batch）必须在 /{id} 之前。
 */
@RestController
@RequestMapping("/property-fees")
@Tag(name = "物业费管理", description = "物业费记录的增删改查接口")
public class PropertyFeeController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyFeeController.class);
    
    @Autowired
    private PropertyFeeService propertyFeeService;

    @GetMapping("/page")
    @Operation(summary = "分页查询物业费记录列表")
    public Result<Page<PropertyFee>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "业主ID") @RequestParam(required = false) String ownerId,
            @Parameter(description = "缴费状态") @RequestParam(required = false) PropertyFee.Status status,
            @Parameter(description = "业主姓名") @RequestParam(required = false) String ownerName,
            @Parameter(description = "房屋位置") @RequestParam(required = false) String address,
            @Parameter(description = "费用年月(格式：yyyy-MM)") @RequestParam(required = false) String feeDate) {
        LocalDate date = null;
        if (feeDate != null && !feeDate.isEmpty()) {
            try {
                date = YearMonth.parse(feeDate).atDay(1);
            } catch (Exception e) {
                LOGGER.error("日期格式错误: {}", feeDate, e);
                return Result.error("-1", "日期格式错误，请使用yyyy-MM格式，例如：2024-02");
            }
        }
        return propertyFeeService.list(pageNum, pageSize, ownerId, status, ownerName, address, date);
    }

    @GetMapping("/calculate")
    @Operation(summary = "计算物业费")
    public Result<?> calculatePropertyFee(
            @RequestParam Long ownerId,
            @RequestParam String feeDate) {
        return propertyFeeService.calculatePropertyFee(ownerId, feeDate);
    }

    @GetMapping("/calculateBatch")
    @Operation(summary = "批量计算物业费")
    public Result<?> batchCalculatePropertyFee(
            @RequestParam String feeDate) {
        return propertyFeeService.batchCalculatePropertyFee(feeDate);
    }

    /**
     * 一键推送（GET）：与批量计算逻辑一致，返回详细统计。使用 GET 避免部分环境下 POST 被静态资源映射拦截导致 405。
     * 必须放在 /{id} 之前。
     */
    @GetMapping("/pushToOwners")
    @Operation(summary = "一键推送物业费账单到业主端（GET）", description = "为已启用且已绑定房屋的业主生成指定月份待缴账单")
    public Result<PropertyFeePushResult> pushPropertyFeesToOwnersGet(
            @RequestParam("feeDate") String feeDate) {
        if (feeDate == null || feeDate.trim().isEmpty()) {
            return Result.error("-1", "请指定费用月份 feeDate（格式 yyyy-MM）");
        }
        return propertyFeeService.pushPropertyFeesToOwners(feeDate.trim());
    }

    @PostMapping("/push")
    @Operation(summary = "一键推送物业费账单到业主端（POST）", description = "为已启用且已绑定房屋的业主生成指定月份待缴账单，业主可在业主端缴费记录中查看并支付")
    public Result<PropertyFeePushResult> pushPropertyFeesToOwners(@RequestBody(required = false) PropertyFeePushRequest body) {
        if (body == null || body.getFeeDate() == null || body.getFeeDate().trim().isEmpty()) {
            return Result.error("-1", "请指定费用月份 feeDate（格式 yyyy-MM）");
        }
        return propertyFeeService.pushPropertyFeesToOwners(body.getFeeDate());
    }

    @GetMapping("/checkPayStatus")
    @Operation(summary = "检查物业费支付状态")
    public Result<?> checkPayStatus(
            @RequestParam String orderNo) {
        return propertyFeeService.checkPayStatus(orderNo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询物业费记录")
    public Result<PropertyFee> getById(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        return propertyFeeService.getById(id);
    }

    @PostMapping
    @Operation(summary = "新增物业费记录")
    public Result<?> add(@RequestBody PropertyFee propertyFee) {
        return propertyFeeService.add(propertyFee);
    }

    @PutMapping
    @Operation(summary = "更新物业费记录")
    public Result<?> update(@RequestBody PropertyFee propertyFee) {
        return propertyFeeService.update(propertyFee);
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "缴纳物业费")
    public Result<?> pay(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        return propertyFeeService.pay(id);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除物业费记录")
    public Result<?> batchDelete(
            @Parameter(description = "记录ID列表") @RequestBody List<Long> ids) {
        return propertyFeeService.batchDelete(ids);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除物业费记录")
    public Result<?> delete(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        return propertyFeeService.delete(id);
    }
} 
