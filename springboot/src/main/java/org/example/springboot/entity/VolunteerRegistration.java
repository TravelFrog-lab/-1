package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("volunteer_registration")
@Schema(description = "小区活动报名")
public class VolunteerRegistration {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "报名人ID")
    private Long volunteerId;

    @Schema(description = "报名状态")
    private Status status;

    @Schema(description = "报名时间")
    private LocalDateTime registerTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Schema(description = "活动信息")
    private VolunteerActivity activity;

    @TableField(exist = false)
    @Schema(description = "报名人信息")
    private Owner volunteer;

    public enum Status {
        REGISTERED,  // 已报名
        CHECKED_IN   // 已签到
    }
} 