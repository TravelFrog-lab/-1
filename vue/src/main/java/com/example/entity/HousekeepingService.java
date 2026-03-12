package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("housekeeping_service")
@Schema(description = "家政服务项目")
public class HousekeepingService {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "服务项目ID")
    private Long id;
    
    @Schema(description = "服务名称")
    private String name;
    
    @Schema(description = "服务描述")
    private String description;
    
    @Schema(description = "基础价格")
    private BigDecimal basePrice;
    
    @Schema(description = "计价单位(小时/次/平米)")
    private String unit;
    
    @Schema(description = "服务类别")
    private String category;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
} 