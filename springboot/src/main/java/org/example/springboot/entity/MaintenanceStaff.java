package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("maintenance_staff")
@Schema(description = "后勤人员")
public class MaintenanceStaff {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "关联用户ID")
    private Long userId;

    @Schema(description = "职位")
    private Position position;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Schema(description = "关联的用户信息")
    private User user;

    @TableField(exist = false)
    @Schema(description = "历史工单平均评价星级（1–5，无评价时为 null）")
    private Double avgEvaluationRating;

    public enum Position {
        MAINTENANCE,  // 后勤
        SECURITY     // 安保
    }
} 