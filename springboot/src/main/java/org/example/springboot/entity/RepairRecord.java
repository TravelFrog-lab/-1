package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@TableName("repair_record")
@Schema(description = "维修记录")
public class RepairRecord {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "申请人ID")
    private Long applicantId;

    @Schema(description = "维修项目ID")
    private Long repairTypeId;

    @Schema(description = "维修房屋ID")
    private Long houseId;

    @Schema(description = "期待上门时间", example = "2025-03-26 00:00:00")
    private LocalDateTime expectedTime;

    @Schema(description = "维修描述")
    private String description;

    @Schema(description = "维修人员ID")
    private Long maintainerId;

    @Schema(description = "维修状态")
    private Status status;

    @Schema(description = "维修结果描述")
    private String resultDescription;

    @Schema(description = "实际维修时间", example = "2025-03-26 00:00:00")
    private LocalDateTime actualTime;

    @Schema(description = "维修结果评价")
    private String evaluation;

    @Schema(description = "维修费用")
    private BigDecimal feeAmount;

    @Schema(description = "支付状态")
    private PayStatus payStatus;

    @Schema(description = "创建时间", example = "2025-03-26 00:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2025-03-26 00:00:00")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Schema(description = "申请人信息")
    private Owner applicant;

    @TableField(exist = false)
    @Schema(description = "维修项目信息")
    private RepairType repairType;

    @TableField(exist = false)
    @Schema(description = "房屋信息")
    private House house;

    @TableField(exist = false)
    @Schema(description = "维修人员信息")
    private MaintenanceStaff maintainer;

    public enum Status {
        PENDING,      // 待处理
        IN_PROGRESS,  // 处理中
        COMPLETED,    // 已完成
        CANCELLED     // 已取消
    }

    public enum PayStatus {
        UNPAID,   // 未支付
        PAID      // 已支付
    }
} 