package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.SystemConfig;
import org.example.springboot.mapper.SystemConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统配置管理服务
 */
@Service
public class SystemConfigService extends ServiceImpl<SystemConfigMapper, SystemConfig> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemConfigService.class);
    private static final Long CONFIG_ID = 1L;
    
    @Resource
    private SystemConfigMapper systemConfigMapper;






    /**
     * 删除系统配置
     *
     * @param id 配置ID
     * @return 操作结果
     */
    @Transactional
    public Result<?> delete(Long id) {
        LOGGER.info("Deleting system config: {}", id);
        if (systemConfigMapper.deleteById(id) <= 0) {
            return Result.error("-1", "系统配置不存在");
        }
        return Result.success();
    }

    /**
     * 批量删除系统配置
     *
     * @param ids 配置ID列表
     * @return 操作结果
     */
    @Transactional
    public Result<?> batchDelete(List<Long> ids) {
        LOGGER.info("Batch deleting system configs: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要删除的记录");
        }
        
        systemConfigMapper.deleteBatchIds(ids);
        return Result.success();
    }



    /**
     * 获取系统配置
     * 如果配置不存在，则创建默认配置
     *
     * @return 系统配置
     */
    public Result<SystemConfig> getConfig() {
        LOGGER.info("Getting system config");
        SystemConfig config = systemConfigMapper.selectById(CONFIG_ID);
        if (config == null) {
            // 如果配置不存在，创建默认配置
            config = createDefaultConfig();
        }
        return Result.success(config);
    }

    /**
     * 更新系统配置
     *
     * @param config 配置信息
     * @return 操作结果
     */
    @Transactional
    public Result<?> updateConfig(SystemConfig config) {
        LOGGER.info("Updating system config: {}", config);
        
        // 验证cron表达式
        if (!CronExpression.isValidExpression(config.getPropertyFeeAutoCalcTime())) {
            return Result.error("-1", "无效的cron表达式");
        }
        
        // 强制设置ID为1
        config.setId(CONFIG_ID);
        
        if (systemConfigMapper.updateById(config) <= 0) {
           createDefaultConfig();
        }
        return Result.success();
    }

    /**
     * 创建默认系统配置
     */
    private SystemConfig createDefaultConfig() {
        LOGGER.info("Creating default system config");
        SystemConfig config = new SystemConfig();
        config.setId(CONFIG_ID);
        config.setPropertyFeeAutoCalcEnabled(false);
        config.setPropertyFeeAutoCalcTime("0 0 1 * *"); // 每月1号凌晨执行
        
        systemConfigMapper.insert(config);
        return config;
    }

    /**
     * 检查是否启用了物业费自动计算
     */
    public boolean isPropertyFeeAutoCalcEnabled() {
        SystemConfig config = systemConfigMapper.selectById(CONFIG_ID);
        return config != null && config.getPropertyFeeAutoCalcEnabled();
    }

    /**
     * 获取物业费自动计算的cron表达式
     */
    public String getPropertyFeeAutoCalcCron() {
        SystemConfig config = systemConfigMapper.selectById(CONFIG_ID);
        return config != null ? config.getPropertyFeeAutoCalcTime() : "0 0 1 * *";
    }
} 