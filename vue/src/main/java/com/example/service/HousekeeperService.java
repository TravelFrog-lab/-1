package com.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Result;
import com.example.entity.Housekeeper;
import com.example.entity.User;
import com.example.mapper.HousekeeperMapper;
import com.example.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HousekeeperService {
    
    public static final Logger LOGGER = LoggerFactory.getLogger(HousekeeperService.class);
    
    @Autowired
    private HousekeeperMapper housekeeperMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 分页查询家政人员
     */
    public Result<IPage<Housekeeper>> page(Integer pageNum, Integer pageSize, String name, String phone, String status) {
        LOGGER.info("分页查询家政人员: pageNum={}, pageSize={}, name={}, phone={}, status={}", pageNum, pageSize, name, phone, status);
        
        LambdaQueryWrapper<Housekeeper> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            queryWrapper.like(Housekeeper::getName, name);
        }
        if (StringUtils.hasText(phone)) {
            queryWrapper.like(Housekeeper::getPhone, phone);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Housekeeper::getStatus, status);
        }
        
        Page<Housekeeper> page = new Page<>(pageNum, pageSize);
        IPage<Housekeeper> housekeeperPage = housekeeperMapper.selectPage(page, queryWrapper);
        
        // 填充用户信息
        housekeeperPage.getRecords().forEach(housekeeper -> {
            User user = userMapper.selectById(housekeeper.getUserId());
            housekeeper.setUser(user);
        });
        
        return Result.success(housekeeperPage);
    }
    
    /**
     * 根据ID查询家政人员
     */
    public Result<Housekeeper> getById(Long id) {
        LOGGER.info("根据ID查询家政人员: id={}", id);
        
        Housekeeper housekeeper = housekeeperMapper.selectById(id);
        if (housekeeper == null) {
            return Result.error("-1", "家政人员不存在");
        }
        
        // 填充用户信息
        User user = userMapper.selectById(housekeeper.getUserId());
        housekeeper.setUser(user);
        
        return Result.success(housekeeper);
    }
    
    /**
     * 根据用户ID查询家政人员
     */
    public Result<Housekeeper> getByUserId(Long userId) {
        LOGGER.info("根据用户ID查询家政人员: userId={}", userId);
        
        LambdaQueryWrapper<Housekeeper> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Housekeeper::getUserId, userId);
        
        Housekeeper housekeeper = housekeeperMapper.selectOne(queryWrapper);
        if (housekeeper == null) {
            return Result.error("-1", "家政人员不存在");
        }
        
        // 填充用户信息
        User user = userMapper.selectById(housekeeper.getUserId());
        housekeeper.setUser(user);
        
        return Result.success(housekeeper);
    }
    
    /**
     * 新增家政人员
     */
    @Transactional
    public Result<?> save(Housekeeper housekeeper) {
        LOGGER.info("新增家政人员: {}", housekeeper);
        
        // 检查用户是否存在
        User user = userMapper.selectById(housekeeper.getUserId());
        if (user == null) {
            return Result.error("-1", "关联的用户不存在");
        }
        
        // 检查用户是否已经关联了其他家政人员
        LambdaQueryWrapper<Housekeeper> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Housekeeper::getUserId, housekeeper.getUserId());
        if (housekeeperMapper.selectCount(queryWrapper) > 0) {
            return Result.error("-1", "该用户已关联其他家政人员");
        }
        
        // 检查手机号是否已存在
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Housekeeper::getPhone, housekeeper.getPhone());
        if (housekeeperMapper.selectCount(queryWrapper) > 0) {
            return Result.error("-1", "手机号已存在");
        }
        
        // 设置默认值
        if (housekeeper.getRating() == null) {
            housekeeper.setRating(new BigDecimal("5.0"));
        }
        if (housekeeper.getStatus() == null) {
            housekeeper.setStatus("ACTIVE");
        }
        
        // 更新用户角色
        user.setRole("KEEPER");
        userMapper.updateById(user);
        
        // 保存家政人员信息
        housekeeper.setCreatedAt(LocalDateTime.now());
        housekeeper.setUpdatedAt(LocalDateTime.now());
        housekeeperMapper.insert(housekeeper);
        
        return Result.success();
    }
    
    /**
     * 更新家政人员
     */
    public Result<?> update(Housekeeper housekeeper) {
        LOGGER.info("更新家政人员: {}", housekeeper);
        
        // 检查家政人员是否存在
        Housekeeper existHousekeeper = housekeeperMapper.selectById(housekeeper.getId());
        if (existHousekeeper == null) {
            return Result.error("-1", "家政人员不存在");
        }
        
        // 如果更改了关联用户
        if (!existHousekeeper.getUserId().equals(housekeeper.getUserId())) {
            // 检查新用户是否存在
            User newUser = userMapper.selectById(housekeeper.getUserId());
            if (newUser == null) {
                return Result.error("-1", "关联的用户不存在");
            }
            
            // 检查新用户是否已经关联了其他家政人员
            LambdaQueryWrapper<Housekeeper> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Housekeeper::getUserId, housekeeper.getUserId())
                    .ne(Housekeeper::getId, housekeeper.getId());
            if (housekeeperMapper.selectCount(queryWrapper) > 0) {
                return Result.error("-1", "该用户已关联其他家政人员");
            }
            
            // 更新旧用户角色
            User oldUser = userMapper.selectById(existHousekeeper.getUserId());
            if (oldUser != null && "KEEPER".equals(oldUser.getRole())) {
                oldUser.setRole("OWNER"); // 默认改为业主角色
                userMapper.updateById(oldUser);
            }
            
            // 更新新用户角色
            newUser.setRole("KEEPER");
            userMapper.updateById(newUser);
        }
        
        // 检查手机号是否已存在（排除自身）
        LambdaQueryWrapper<Housekeeper> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Housekeeper::getPhone, housekeeper.getPhone())
                .ne(Housekeeper::getId, housekeeper.getId());
        if (housekeeperMapper.selectCount(queryWrapper) > 0) {
            return Result.error("-1", "手机号已存在");
        }
        
        // 更新家政人员信息
        housekeeper.setUpdatedAt(LocalDateTime.now());
        housekeeperMapper.updateById(housekeeper);
        
        return Result.success();
    }
    
    /**
     * 删除家政人员
     */
    @Transactional
    public Result<?> delete(Long id) {
        LOGGER.info("删除家政人员: id={}", id);
        
        // 检查家政人员是否存在
        Housekeeper housekeeper = housekeeperMapper.selectById(id);
        if (housekeeper == null) {
            return Result.error("-1", "家政人员不存在");
        }
        
        // TODO: 检查是否有关联的订单，如果有则不允许删除
        
        // 删除家政人员
        housekeeperMapper.deleteById(id);
        
        // 更新用户角色
        User user = userMapper.selectById(housekeeper.getUserId());
        if (user != null && "KEEPER".equals(user.getRole())) {
            user.setRole("OWNER"); // 默认改为业主角色
            userMapper.updateById(user);
        }
        
        return Result.success();
    }
    
    /**
     * 获取所有家政人员
     */
    public Result<List<Housekeeper>> list() {
        LOGGER.info("获取所有家政人员");
        
        List<Housekeeper> housekeepers = housekeeperMapper.selectList(null);
        
        // 填充用户信息
        housekeepers.forEach(housekeeper -> {
            User user = userMapper.selectById(housekeeper.getUserId());
            housekeeper.setUser(user);
        });
        
        return Result.success(housekeepers);
    }
    
    /**
     * 更新家政人员状态
     */
    public Result<?> updateStatus(Long id, String status) {
        LOGGER.info("更新家政人员状态: id={}, status={}", id, status);
        
        // 检查家政人员是否存在
        Housekeeper housekeeper = housekeeperMapper.selectById(id);
        if (housekeeper == null) {
            return Result.error("-1", "家政人员不存在");
        }
        
        // 检查状态是否有效
        if (!"ACTIVE".equals(status) && !"DISABLED".equals(status)) {
            return Result.error("-1", "无效的状态值");
        }
        
        // 更新状态
        housekeeper.setStatus(status);
        housekeeperMapper.updateById(housekeeper);
        
        return Result.success();
    }
} 