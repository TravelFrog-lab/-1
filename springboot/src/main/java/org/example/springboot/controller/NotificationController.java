package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Notification;
import org.example.springboot.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification")
@Tag(name = "通知管理", description = "通知的增删改查接口")
public class NotificationController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationController.class);
    
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/page")
    @Operation(summary = "分页查询通知列表")
    public Result<Page<Notification>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "接收人ID") @RequestParam(required = false) Long receiverId,
            @Parameter(description = "标题") @RequestParam(required = false) String title,
            @Parameter(description = "接收人类型") @RequestParam(required = false) Notification.ReceiverType receiverType) {
        return Result.success(notificationService.list(pageNum, pageSize, receiverId, title, receiverType));
    }




    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询通知")
    public Result<Notification> getById(
            @Parameter(description = "通知ID") @PathVariable Long id) {
        return notificationService.getNotificationById(id);
    }

    // getByUserId
  

    

    @PostMapping

    @Operation(summary = "新增通知")
    public Result<?> add(@RequestBody Notification notification) {
        return notificationService.add(notification);
    }

    @PostMapping("/send-to-all")
    @Operation(summary = "群发通知")
    public Result<?> sendToAll(
            @RequestBody Notification notification){
        return notificationService.sendToAll(notification);
    }

    @PutMapping
    @Operation(summary = "更新通知")
    public Result<?> update(@RequestBody Notification notification) {
        return notificationService.update(notification);
    }
      @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID查询通知")
    public Result<List<Notification>> getByUserId(@PathVariable Long userId) {
        return Result.success(notificationService.list(1,Integer.MAX_VALUE,userId,null,null).getRecords());
    }



    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知")
    public Result<?> delete(
            @Parameter(description = "通知ID") @PathVariable Long id) {
        return notificationService.delete(id);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除通知")
    public Result<?> batchDelete(
            @Parameter(description = "通知ID列表") @RequestBody List<Long> ids) {
        return notificationService.batchDelete(ids);
    }
} 