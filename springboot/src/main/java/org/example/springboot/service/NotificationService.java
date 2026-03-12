package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Notification;
import org.example.springboot.entity.User;
import org.example.springboot.mapper.NotificationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class NotificationService extends ServiceImpl<NotificationMapper, Notification> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private UserService userService;
    
    @Resource
    private NotificationMapper notificationMapper;

    public Page<Notification> list(Integer pageNum, Integer pageSize, Long receiverId, String title,
            Notification.ReceiverType receiverType) {
        LOGGER.info("Listing notifications, pageNum: {}, pageSize: {}, receiverId: {}, title: {}, receiverType: {}", 
                pageNum, pageSize, receiverId, title, receiverType);

        
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        if (receiverId != null) {
            wrapper.eq(Notification::getReceiverId, receiverId);
        }
        if (receiverType != null) {
            wrapper.eq(Notification::getReceiverType, receiverType);
        }
        if (StringUtils.isNotEmpty(title)) {
            wrapper.like(Notification::getTitle, title);
        }
        wrapper.orderByDesc(Notification::getCreatedAt);
        

        Page<Notification> page = page(new Page<>(pageNum, pageSize), wrapper);
        // 填充接收人信息
        page.getRecords().forEach(this::fillReceiverInfo);
        return page;
    }

    private void fillReceiverInfo(Notification notification) {
        User receiver = userService.getUserById(notification.getReceiverId().intValue());
        notification.setReceiver(receiver);
    }

    public Result<Notification> getNotificationById(Long id) {
        LOGGER.info("Getting notification by id: {}", id);
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            return Result.error("-1", "通知不存在");
        }
        fillReceiverInfo(notification);
        return Result.success(notification);
    }

    @Transactional
    public Result<?> add(Notification notification) {
        LOGGER.info("Adding new notification: {}", notification);
        // 验证接收人是否存在
        User receiver = userService.getUserById(notification.getReceiverId().intValue());
        if (receiver == null) {
            return Result.error("-1", "接收人不存在");
        }
        

        notificationMapper.insert(notification);
        return Result.success();
    }



    @Transactional
    public Result<?> update(Notification notification) {
        LOGGER.info("Updating notification: {}", notification);
        if (notificationMapper.updateById(notification) <= 0) {
            return Result.error("-1", "通知不存在");
        }
        return Result.success();
    }

    @Transactional
    public Result<?> delete(Long id) {
        LOGGER.info("Deleting notification: {}", id);
        if (notificationMapper.deleteById(id) <= 0) {
            return Result.error("-1", "通知不存在");
        }
        return Result.success();
    }

    @Transactional
    public Result<?> batchDelete(List<Long> ids) {
        LOGGER.info("Batch deleting notifications: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要删除的记录");
        }
        
        notificationMapper.deleteBatchIds(ids);
        return Result.success();
    }

    @Transactional
    public Result<?> sendToAll(Notification notification) {
        LOGGER.info("Sending notification to all users of type: {}", notification.getReceiverType());
        
        // 获取指定类型的所有用户
        List<User> users = userService.getUsersByRole(convertReceiverTypeToRole(notification.getReceiverType()));
        if (users.isEmpty()) {
            return Result.error("-1", "没有找到指定类型的用户");
        }
        
        // 为每个用户创建通知
        for (User user : users) {
            Notification userNotification = new Notification();
            userNotification.setReceiverId(user.getId());
            userNotification.setTitle(notification.getTitle());
            userNotification.setContent(notification.getContent());
            userNotification.setReceiverType(notification.getReceiverType());
            notificationMapper.insert(userNotification);
        }
        
        return Result.success();
    }

    private User.Role convertReceiverTypeToRole(Notification.ReceiverType receiverType) {
        switch (receiverType) {
            case OWNER:
                return User.Role.OWNER;
            case ADMIN:
                return User.Role.ADMIN;
            case MAINTENANCE:
                return User.Role.MAINTENANCE;
            default:
                throw new IllegalArgumentException("Unsupported receiver type: " + receiverType);
        }
    }
} 