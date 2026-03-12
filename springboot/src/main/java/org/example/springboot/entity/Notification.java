package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification")
@Schema(description = "通知")
public class Notification {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "接收人ID")
    private Long receiverId;

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "通知内容")
    private String content;

    @Schema(description = "接收人类型")
    private ReceiverType receiverType;

    @Schema(description = "是否已读")
    private Integer isRead;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Schema(description = "接收人信息")
    private User receiver;

    public enum ReceiverType {
        OWNER,          // 业主
        ADMIN,          // 管理员
        MAINTENANCE,    // 维修人员
    }
} 