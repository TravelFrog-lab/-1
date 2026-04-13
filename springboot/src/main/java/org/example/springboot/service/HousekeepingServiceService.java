package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.example.springboot.common.Result;
import org.example.springboot.entity.HousekeepingService;
import org.example.springboot.mapper.HousekeepingServiceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HousekeepingServiceService {
    
    public static final Logger LOGGER = LoggerFactory.getLogger(HousekeepingServiceService.class);
    
    @Autowired
    private HousekeepingServiceMapper housekeepingServiceMapper;
    
    /**
     * 分页查询服务项目
     */
    public Result<IPage<HousekeepingService>> page(Integer pageNum, Integer pageSize, String name, String category) {
        LOGGER.info("分页查询服务项目: pageNum={}, pageSize={}, name={}, category={}", pageNum, pageSize, name, category);
        
        LambdaQueryWrapper<HousekeepingService> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            queryWrapper.like(HousekeepingService::getName, name);
        }
        if (StringUtils.hasText(category)) {
            queryWrapper.eq(HousekeepingService::getCategory, category);
        }
        
        Page<HousekeepingService> page = new Page<>(pageNum, pageSize);
        IPage<HousekeepingService> servicePage = housekeepingServiceMapper.selectPage(page, queryWrapper);
        
        return Result.success(servicePage);
    }
    
    /**
     * 根据ID查询服务项目
     */
    public Result<HousekeepingService> getById(Long id) {
        LOGGER.info("根据ID查询服务项目: id={}", id);
        
        HousekeepingService service = housekeepingServiceMapper.selectById(id);
        if (service == null) {
            return Result.error("-1", "服务项目不存在");
        }
        
        return Result.success(service);
    }
    
    /**
     * 新增服务项目
     */
    public Result<?> save(HousekeepingService service) {
        LOGGER.info("新增服务项目: {}", service);
        
        // 检查名称是否已存在
        LambdaQueryWrapper<HousekeepingService> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HousekeepingService::getName, service.getName());
        if (housekeepingServiceMapper.selectCount(queryWrapper) > 0) {
            return Result.error("-1", "服务名称已存在");
        }
        
        // 设置创建和更新时间
        service.setCreatedAt(LocalDateTime.now());
        service.setUpdatedAt(LocalDateTime.now());
        
        housekeepingServiceMapper.insert(service);
        return Result.success();
    }
    
    /**
     * 更新服务项目
     */
    public Result<?> update(HousekeepingService service) {
        LOGGER.info("更新服务项目: {}", service);
        
        // 检查服务项目是否存在
        HousekeepingService existService = housekeepingServiceMapper.selectById(service.getId());
        if (existService == null) {
            return Result.error("-1", "服务项目不存在");
        }
        
        // 检查名称是否已存在（排除自身）
        LambdaQueryWrapper<HousekeepingService> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HousekeepingService::getName, service.getName())
                .ne(HousekeepingService::getId, service.getId());
        if (housekeepingServiceMapper.selectCount(queryWrapper) > 0) {
            return Result.error("-1", "服务名称已存在");
        }
        
        // 更新时间
        service.setUpdatedAt(LocalDateTime.now());
        
        housekeepingServiceMapper.updateById(service);
        return Result.success();
    }
    
    /**
     * 删除服务项目
     */
    public Result<?> delete(Long id) {
        LOGGER.info("删除服务项目: id={}", id);
        
        // 检查服务项目是否存在
        HousekeepingService service = housekeepingServiceMapper.selectById(id);
        if (service == null) {
            return Result.error("-1", "服务项目不存在");
        }
        
        // TODO: 检查是否有关联的订单或家政人员，如果有则不允许删除
        
        housekeepingServiceMapper.deleteById(id);
        return Result.success();
    }
    
    /**
     * 批量删除服务项目
     */
    public Result<?> batchDelete(List<Long> ids) {
        LOGGER.info("批量删除服务项目: ids={}", ids);
        
        // TODO: 检查是否有关联的订单或家政人员，如果有则不允许删除
        
        housekeepingServiceMapper.deleteBatchIds(ids);
        return Result.success();
    }
    
    /**
     * 获取所有服务项目
     */
    public Result<List<HousekeepingService>> list() {
        LOGGER.info("获取所有服务项目");
        
        List<HousekeepingService> services = housekeepingServiceMapper.selectList(null);
        return Result.success(services);
    }
    
    /**
     * 根据类别获取服务项目
     */
    public Result<List<HousekeepingService>> listByCategory(String category) {
        LOGGER.info("根据类别获取服务项目: category={}", category);
        
        LambdaQueryWrapper<HousekeepingService> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HousekeepingService::getCategory, category);
        
        List<HousekeepingService> services = housekeepingServiceMapper.selectList(queryWrapper);
        return Result.success(services);
    }
} 