package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("parking_space")
@Schema(description = "车位信息")
public class ParkingSpace {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "车位类型")
    private Type type;

    @Schema(description = "车位编号")
    private String code;

    @Schema(description = "车位位置")
    private String location;
    @TableField(exist = false)
    @Schema(description = "使用状态")
    private String status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    public enum Type {
        PUBLIC,   // 公共车位
        PRIVATE   // 私人车位
    }
} 