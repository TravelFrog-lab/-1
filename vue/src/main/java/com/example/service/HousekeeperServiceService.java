package com.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.Result;
import com.example.entity.HousekeeperService;
import com.example.entity.HousekeepingService;
import com.example.mapper.HousekeeperServiceMapper;
import com.example.mapper.HousekeepingServiceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HousekeeperServiceService {
    
    public static final Logger LOGGER = LoggerFactory.getLogger(HousekeeperServiceService.class);
    
    @Autowired
    private HousekeeperServiceMapper housekeeperServiceMapper;
    
    @Autowired
    private HousekeepingServiceMapper housekeepingServiceMapper;
    
    /**
     * 根据家政人员ID获取服务项目列表
     */
    public Result<List<HousekeeperService>> listByHousekeeper(Long housekeeperId) {
        LOGGER.info("根据家政人员ID获取服务项目列表: housekeeperId={}", housekeeperId);
        
        LambdaQueryWrapper<HousekeeperService> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HousekeeperService::getHousekeeperId, housekeeperId);
        
        List<HousekeeperService> list = housekeeperServiceMapper.selectList(queryWrapper);
        
        // 填充服务项目信息
        list.forEach(item -> {
            HousekeepingService service = housekeepingServiceMapper.selectById(item.getServiceId());
            item.setService(service);
        });
        
        return Result.success(list);
    }
    
    /**
     * 添加家政人员服务项目关联
     */
    public Result<?> save(HousekeeperService housekeeperService) {
        LOGGER.info("添加家政人员服务项目关联: {}", housekeeperService);
        
        // 检查是否已存在相同关联
        LambdaQueryWrapper<HousekeeperService> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HousekeeperService::getHousekeeperId, housekeeperService.getHousekeeperId())
                .eq(HousekeeperService::getServiceId, housekeeperService.getServiceId());
        
        if (housekeeperServiceMapper.selectCount(queryWrapper) > 0) {
            return Result.error("-1", "该服务项目已关联");
        }
        
        // 设置创建和更新时间
        housekeeperService.setCreatedAt(LocalDateTime.now());
        housekeeperService.setUpdatedAt(LocalDateTime.now());
        
        housekeeperServiceMapper.insert(housekeeperService);
        return Result.success();
    }
    
    /**
     * 更新家政人员服务项目关联
     */
    public Result<?> update(HousekeeperService housekeeperService) {
        LOGGER.info("更新家政人员服务项目关联: {}", housekeeperService);
        
        // 检查关联是否存在
        HousekeeperService existRelation = housekeeperServiceMapper.selectById(housekeeperService.getId());
        if (existRelation == null) {
            return Result.error("-1", "关联不存在");
        }
        
        // 更新时间
        housekeeperService.setUpdatedAt(LocalDateTime.now());
        
        housekeeperServiceMapper.updateById(housekeeperService);
        return Result.success();
    }
    
    /**
     * 删除家政人员服务项目关联
     */
    public Result<?> delete(Long id) {
        LOGGER.info("删除家政人员服务项目关联: id={}", id);
        
        // 检查关联是否存在
        HousekeeperService relation = housekeeperServiceMapper.selectById(id);
        if (relation == null) {
            return Result.error("-1", "关联不存在");
        }
        
        housekeeperServiceMapper.deleteById(id);
        return Result.success();
    }
    
    /**
     * 批量保存家政人员服务项目关联
     */
    @Transactional
    public Result<?> batchSave(Long housekeeperId, List<HousekeeperService> services) {
        LOGGER.info("批量保存家政人员服务项目关联: housekeeperId={}, services={}", housekeeperId, services);
        
        // 先删除该家政人员的所有关联
        LambdaQueryWrapper<HousekeeperService> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HousekeeperService::getHousekeeperId, housekeeperId);
        housekeeperServiceMapper.delete(queryWrapper);
        
        // 再添加新的关联
        if (services != null && !services.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (HousekeeperService service : services) {
                service.setHousekeeperId(housekeeperId);
                service.setCreatedAt(now);
                service.setUpdatedAt(now);
                housekeeperServiceMapper.insert(service);
            }
        }
        
        return Result.success();
    }
} 