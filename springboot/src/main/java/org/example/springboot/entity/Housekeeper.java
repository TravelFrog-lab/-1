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
@TableName("housekeeper")
@Schema(description = "家政人员信息")
public class Housekeeper {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "家政人员ID")
    private Long id;
    
    @Schema(description = "关联用户ID")
    private Long userId;
    
    @Schema(description = "姓名")
    private String name;
    
    @Schema(description = "联系电话")
    private String phone;
    
    @Schema(description = "身份证号")
    private String idCard;
    
    @Schema(description = "状态：ACTIVE-活跃，DISABLED-禁用")
    private String status;
    
    @Schema(description = "个人描述")
    private String description;
    
    @Schema(description = "工作年限")
    private Integer workYears;
    
    @Schema(description = "评分")
    private BigDecimal rating;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
    
    @TableField(exist = false)
    @Schema(description = "关联用户信息")
    private User user;
} 