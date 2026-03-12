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
@TableName("housekeeper_service")
@Schema(description = "家政人员服务项目关联")
public class HousekeeperService {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "关联ID")
    private Long id;
    
    @Schema(description = "家政人员ID")
    private Long housekeeperId;
    
    @Schema(description = "服务项目ID")
    private Long serviceId;
    
    @Schema(description = "服务价格")
    private BigDecimal price;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
    
    @TableField(exist = false)
    @Schema(description = "服务项目信息")
    private HousekeepingService service;
} 