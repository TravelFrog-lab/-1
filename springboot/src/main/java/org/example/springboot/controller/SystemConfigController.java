package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.SystemConfig;
import org.example.springboot.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system-config")
@Tag(name = "系统参数管理", description = "系统参数的查询和更新接口")
public class SystemConfigController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemConfigController.class);
    
    @Autowired
    private SystemConfigService systemConfigService;

    @GetMapping
    @Operation(summary = "获取系统参数")
    public Result<SystemConfig> getConfig() {
        return systemConfigService.getConfig();
    }

    @PutMapping
    @Operation(summary = "更新系统参数")
    public Result<?> updateConfig(@RequestBody SystemConfig config) {
        return systemConfigService.updateConfig(config);
    }
} 