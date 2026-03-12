package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("system_config")
@Schema(description = "系统参数")
public class SystemConfig {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "是否开启物业费自动计算")
    private Boolean propertyFeeAutoCalcEnabled;

    @Schema(description = "物业费自动计算时间(cron表达式)")
    private String propertyFeeAutoCalcTime;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
} 