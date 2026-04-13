package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("house_type")
@Schema(description = "房屋类型")
public class HouseType {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "类型名称")
    private String name;

    @Schema(description = "类型描述")
    private String description;

    @Schema(description = "物业费月单价")
    private BigDecimal propertyFee;

    /**
     * 参考建筑面积（㎡）：优先读取数据库 reference_area。
     * 若库内为空，再由业务兜底逻辑按约定户型名称填充。
     */
    @Schema(description = "参考建筑面积(㎡)")
    private BigDecimal referenceArea;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
} 