package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pet_sitting_request")
@Schema(description = "宠物代喂服务请求")
public class PetSittingRequest {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "业主ID")
    private Long ownerId;

    @Schema(description = "代喂人员ID")
    private Long sitterId;

    @Schema(description = "要求描述")
    private String description;

    @Schema(description = "期待服务时间")
    private LocalDateTime expectedTime;

    @Schema(description = "联系状态")
    private Status status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Schema(description = "业主信息")
    private Owner owner;

    @TableField(exist = false)
    @Schema(description = "代喂人员信息")
    private PetSitter sitter;

    public enum Status {
        PENDING,    // 待联系
        CONTACTED,  // 已联系
        COMPLETED,  // 已完成
        CANCELLED   // 已取消
    }
} 