package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("house")
@Schema(description = "房屋信息")
public class House {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "房屋地址")
    private String address;

    @TableField(exist = false)
    @Schema(description = "楼栋号(前端展示/传输字段)")
    private String buildingNo;

    @TableField(exist = false)
    @Schema(description = "单元号(前端展示/传输字段)")
    private String unitNo;

    @TableField(exist = false)
    @Schema(description = "房号(前端展示/传输字段)")
    private String roomNo;

    @Schema(description = "房屋类型ID")
    private Long houseTypeId;

    @Schema(description = "房屋面积")
    private BigDecimal area;

    @Schema(description = "房屋状态")
    private Status status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Schema(description = "关联的房屋类型信息")
    private HouseType houseType;

    public enum Status {
        VACANT,     // 空置
        OCCUPIED,   // 在住
        RENTED      // 出租
    }
} 