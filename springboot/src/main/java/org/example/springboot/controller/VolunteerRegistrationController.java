package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.VolunteerRegistration;
import org.example.springboot.service.VolunteerRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/community-registration", "/volunteer-registration"})
@Tag(name = "小区活动报名管理", description = "小区活动报名的查询和签到接口")
public class VolunteerRegistrationController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(VolunteerRegistrationController.class);
    
    @Autowired
    private VolunteerRegistrationService volunteerRegistrationService;

    @GetMapping("/my-activity-ids")
    @Operation(summary = "当前业主已报名的活动ID列表（用于活动列表按钮）")
    public Result<List<Long>> myActivityIds(
            @Parameter(description = "业主ID") @RequestParam Long volunteerId) {
        return volunteerRegistrationService.listActivityIdsByVolunteer(volunteerId);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询报名记录列表")
    public Result<Page<VolunteerRegistration>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "活动名称") @RequestParam(required = false) String name,
            @Parameter(description = "报名人ID") @RequestParam(required = false) Long volunteerId,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "报名人姓名") @RequestParam(required = false) String volunteerName


            ) {
        return volunteerRegistrationService.list(pageNum, pageSize, name, volunteerId,status,volunteerName);
    }


    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询报名记录")
    public Result<VolunteerRegistration> getById(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        return volunteerRegistrationService.getById(id);
    }

    @PostMapping("/{id}/check-in")
    @Operation(summary = "活动签到")
    public Result<?> checkIn(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        return volunteerRegistrationService.checkIn(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "取消报名")
    public Result<?> delete(@Parameter(description = "报名记录ID") @PathVariable Long id) {
        return volunteerRegistrationService.cancel(id);
    }
} 