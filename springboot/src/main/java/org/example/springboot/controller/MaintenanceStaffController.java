package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.MaintenanceStaff;
import org.example.springboot.service.MaintenanceStaffService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/maintenance-staff")
@Tag(name = "后勤人员管理", description = "后勤人员的增删改查接口")
public class MaintenanceStaffController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceStaffController.class);
    
    @Autowired
    private MaintenanceStaffService maintenanceStaffService;

    @GetMapping("/page")
    @Operation(summary = "分页查询后勤人员列表")
    public Result<Page<MaintenanceStaff>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        LOGGER.info("Listing maintenance staff with pageNum: {}, pageSize: {}", pageNum, pageSize);
        return Result.success(maintenanceStaffService.list(pageNum, pageSize));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询后勤人员")
    public Result<MaintenanceStaff> getById(
            @Parameter(description = "后勤人员ID") @PathVariable Long id) {
        LOGGER.info("Getting maintenance staff by id: {}", id);
        return maintenanceStaffService.getById(id);
    }

    @PostMapping
    @Operation(summary = "新增后勤人员")
    public Result<?> add(@RequestBody MaintenanceStaff staff) {
        LOGGER.info("Adding new maintenance staff: {}", staff);
        return maintenanceStaffService.add(staff);
    }

    @PutMapping
    @Operation(summary = "更新后勤人员信息")
    public Result<?> update(@RequestBody MaintenanceStaff staff) {
        LOGGER.info("Updating maintenance staff: {}", staff);
        return maintenanceStaffService.update(staff);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除后勤人员")
    public Result<?> delete(
            @Parameter(description = "后勤人员ID") @PathVariable Long id) {
        LOGGER.info("Deleting maintenance staff: {}", id);
        return maintenanceStaffService.delete(id);
    }
    //getAll
    @GetMapping("/all")
    @Operation(summary = "查询所有后勤人员")
    public Result<List<MaintenanceStaff>> getAll() {
        return Result.success(maintenanceStaffService.list(1,Integer.MAX_VALUE).getRecords());
    }


    @DeleteMapping("/batch")
    @Operation(summary = "批量删除后勤人员")
    public Result<?> batchDelete(
            @Parameter(description = "后勤人员ID列表") @RequestBody List<Long> ids) {
        return maintenanceStaffService.batchDelete(ids);
    }

    @GetMapping("/userId/{userId}")
    @Operation(summary = "根据用户ID查询后勤人员")
    public Result<?> getByUserId(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        return maintenanceStaffService.getByUserId(userId);
    }

} 