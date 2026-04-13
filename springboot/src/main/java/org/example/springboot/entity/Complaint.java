package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("complaint")
@Schema(description = "投诉记录")
public class Complaint {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "投诉人ID")
    private Long complainantId;

    @Schema(description = "投诉内容")
    private String content;

    /** 类型编码：PROPERTY/ENVIRONMENT/NOISE/FACILITY/PARKING/OTHER */
    @Schema(description = "投诉类型")
    private String complaintType;

    @Schema(description = "处理状态")
    private Status status;

    @Schema(description = "处理结果")
    private String result;

    @Schema(description = "处理人员ID")
    private Long handlerId;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "处理结果评价")
    private String evaluation;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Schema(description = "投诉人信息")
    private Owner complainant;

    @TableField(exist = false)
    @Schema(description = "处理人员信息")
    private User handler;

    public enum Status {
        PENDING,     // 待处理
        PROCESSING,  // 处理中
        RESOLVED    // 已解决
    }
} 