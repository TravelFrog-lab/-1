package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("private_parking")
@Schema(description = "私人车位使用记录")
public class PrivateParking {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "车位ID")
    private Long parkingSpaceId;

    @Schema(description = "业主ID")
    private Long ownerId;

    @Schema(description = "车牌信息")
    private String plateInfo;


    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Schema(description = "关联的车位信息")
    private ParkingSpace parkingSpace;

    @TableField(exist = false)
    @Schema(description = "关联的业主信息")
    private Owner owner;
} 