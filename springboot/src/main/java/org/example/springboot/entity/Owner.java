package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Data
@TableName("owner")
@Schema(description = "业主信息")
public class Owner {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "关联用户ID")
    private Long userId;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "家庭成员信息(JSON)")
    private String familyMembers;

    @Schema(description = "关联房屋ID")
    private Long houseId;

    @Schema(description = "工作单位")
    private String workplace;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "入住时间")
    private LocalDateTime checkInTime;

    @Schema(description = "备注信息")
    private String remarks;

    @Schema(description = "车牌号码")
    private String plateNumber;

    @Schema(description = "宠物信息")
    private String petInfo;

    @Schema(description = "装修情况")
    private DecorationStatus decorationStatus;


    @Schema(description = "创建时间")
    private LocalDateTime createdAt;


    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "状态")
    private ReviewStatus status;


    @TableField(exist = false)
    @Schema(description = "关联的用户信息")
    private User user;

    @TableField(exist = false)
    @Schema(description = "关联的房屋信息")
    private House house;

    public enum DecorationStatus {
        NONE,    // 未装修
        SIMPLE,  // 简单装修
        LUXURY   // 精装修
    }
    public enum ReviewStatus {
        DISABLED, // 禁用
        ENABLED, // 启用
    }
} 