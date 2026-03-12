package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pet_sitter")
@Schema(description = "宠物代喂人员")
public class PetSitter {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "服务价格")
    private BigDecimal servicePrice;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    public enum Status {
        ACTIVE,    // 正常
        DISABLED   // 禁用
    }
} 