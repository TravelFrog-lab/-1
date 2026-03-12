package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("housekeeping_order")
@Schema(description = "家政服务订单")
public class HousekeepingOrder {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "订单ID")
    private Long id;
    
    @Schema(description = "订单编号")
    private String orderNo;
    
    @Schema(description = "业主ID")
    private Long ownerId;
    
    @Schema(description = "家政人员ID")
    private Long housekeeperId;
    
    @Schema(description = "服务项目ID")
    private Long serviceId;
    
    @Schema(description = "预约时间")
    private LocalDateTime appointmentTime;
    
    @Schema(description = "服务时长(分钟)")
    private Integer serviceDuration;
    
    @Schema(description = "订单金额")
    private BigDecimal amount;
    
    @Schema(description = "订单状态: PENDING-待确认, CONFIRMED-已确认, IN_PROGRESS-进行中, COMPLETED-已完成, CANCELLED-已取消")
    private String status;
    
    @Schema(description = "支付状态: UNPAID-未支付, PAID-已支付, REFUNDED-已退款")
    private String paymentStatus;
    
    @Schema(description = "备注")
    private String remark;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
    
    @TableField(exist = false)
    @Schema(description = "业主信息")
    private Owner owner;
    
    @TableField(exist = false)
    @Schema(description = "家政人员信息")
    private Housekeeper housekeeper;
    
    @TableField(exist = false)
    @Schema(description = "服务项目信息")
    private HousekeepingService service;
} 