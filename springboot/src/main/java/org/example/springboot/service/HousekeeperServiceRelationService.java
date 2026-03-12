package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import org.example.springboot.common.Result;
import org.example.springboot.entity.Housekeeper;
import org.example.springboot.entity.HousekeeperServiceRelation;
import org.example.springboot.entity.HousekeepingService;
import org.example.springboot.mapper.HousekeeperMapper;
import org.example.springboot.mapper.HousekeeperServiceRelationMapper;
import org.example.springboot.mapper.HousekeepingServiceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HousekeeperServiceRelationService {
    
    public static final Logger LOGGER = LoggerFactory.getLogger(HousekeeperServiceRelationService.class);
    
    @Autowired
    private HousekeeperServiceRelationMapper housekeeperServiceRelationMapper;
    
    @Autowired
    private HousekeepingServiceMapper housekeepingServiceMapper;
    @Autowired
    private HousekeeperMapper housekeeperMapper; // 添加这一行
    /**
     * 根据家政人员ID获取服务项目列表
     */
    public Result<List<HousekeeperServiceRelation>> listByHousekeeper(Long housekeeperId) {
        LOGGER.info("根据家政人员ID获取服务项目列表: housekeeperId={}", housekeeperId);
        
        LambdaQueryWrapper<HousekeeperServiceRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HousekeeperServiceRelation::getHousekeeperId, housekeeperId);
        
        List<HousekeeperServiceRelation> list = housekeeperServiceRelationMapper.selectList(queryWrapper);
        
        // 填充服务项目信息
        list.forEach(item -> {
            HousekeepingService service = housekeepingServiceMapper.selectById(item.getServiceId());
            item.setService(service);
        });
        
        return Result.success(list);
    }
    /**
     * 根据服务项目ID获取家政人员列表
     */
    public Result<List<HousekeeperServiceRelation>> listByService(Long serviceId) {
        LOGGER.info("根据服务项目ID获取家政人员列表: serviceId={}", serviceId);

        LambdaQueryWrapper<HousekeeperServiceRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HousekeeperServiceRelation::getServiceId, serviceId);

        List<HousekeeperServiceRelation> list = housekeeperServiceRelationMapper.selectList(queryWrapper);

        // 填充家政人员信息
        list.forEach(item -> {
            Housekeeper housekeeper = housekeeperMapper.selectById(item.getHousekeeperId());
            item.setHousekeeper(housekeeper);
        });

        return Result.success(list);
    }
    
    /**
     * 添加家政人员服务项目关联
     */
    public Result<?> save(HousekeeperServiceRelation housekeeperService) {
        LOGGER.info("添加家政人员服务项目关联: {}", housekeeperService);
        
        // 检查是否已存在相同关联
        LambdaQueryWrapper<HousekeeperServiceRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HousekeeperServiceRelation::getHousekeeperId, housekeeperService.getHousekeeperId())
                .eq(HousekeeperServiceRelation::getServiceId, housekeeperService.getServiceId());
        
        if (housekeeperServiceRelationMapper.selectCount(queryWrapper) > 0) {
            return Result.error("-1", "该服务项目已关联");
        }
        
        // 设置创建和更新时间
        housekeeperService.setCreatedAt(LocalDateTime.now());
        housekeeperService.setUpdatedAt(LocalDateTime.now());
        
        housekeeperServiceRelationMapper.insert(housekeeperService);
        return Result.success();
    }
    
    /**
     * 更新家政人员服务项目关联
     */
    public Result<?> update(HousekeeperServiceRelation housekeeperService) {
        LOGGER.info("更新家政人员服务项目关联: {}", housekeeperService);
        
        // 检查关联是否存在
        HousekeeperServiceRelation existRelation = housekeeperServiceRelationMapper.selectById(housekeeperService.getId());
        if (existRelation == null) {
            return Result.error("-1", "关联不存在");
        }
        
        // 更新时间
        housekeeperService.setUpdatedAt(LocalDateTime.now());
        
        housekeeperServiceRelationMapper.updateById(housekeeperService);
        return Result.success();
    }
    
    /**
     * 删除家政人员服务项目关联
     */
    public Result<?> delete(Long id) {
        LOGGER.info("删除家政人员服务项目关联: id={}", id);
        
        // 检查关联是否存在
        HousekeeperServiceRelation relation = housekeeperServiceRelationMapper.selectById(id);
        if (relation == null) {
            return Result.error("-1", "关联不存在");
        }
        
        housekeeperServiceRelationMapper.deleteById(id);
        return Result.success();
    }
    
    /**
     * 批量保存家政人员服务项目关联
     */
    @Transactional
    public Result<?> batchSave(Long housekeeperId, List<HousekeeperServiceRelation> services) {
        LOGGER.info("批量保存家政人员服务项目关联: housekeeperId={}, services={}", housekeeperId, services);
        
        // 先删除该家政人员的所有关联
        LambdaQueryWrapper<HousekeeperServiceRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HousekeeperServiceRelation::getHousekeeperId, housekeeperId);
        housekeeperServiceRelationMapper.delete(queryWrapper);
        
        // 再添加新的关联
        if (services != null && !services.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (HousekeeperServiceRelation service : services) {
                service.setHousekeeperId(housekeeperId);
                service.setCreatedAt(now);
                service.setUpdatedAt(now);
                housekeeperServiceRelationMapper.insert(service);
            }
        }
        
        return Result.success();
    }
} 