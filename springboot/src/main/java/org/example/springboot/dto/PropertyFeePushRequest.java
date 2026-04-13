package org.example.springboot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "物业费推送到业主端请求")
public class PropertyFeePushRequest {

    @Schema(description = "费用月份，格式 yyyy-MM", example = "2026-03", requiredMode = Schema.RequiredMode.REQUIRED)
    private String feeDate;
}
