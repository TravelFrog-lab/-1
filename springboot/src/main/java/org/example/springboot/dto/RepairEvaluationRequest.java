package org.example.springboot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "业主维修评价（星级 + 文字）")
public class RepairEvaluationRequest {

    @Schema(description = "星级 1-5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer evaluationRating;

    @Schema(description = "评价内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String evaluation;
}
