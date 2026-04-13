package org.example.springboot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 物业费推送到业主端（生成待缴账单）的统计结果
 */
@Data
@Schema(description = "物业费推送结果")
public class PropertyFeePushResult {

    @Schema(description = "费用月份 yyyy-MM")
    private String feeMonth;

    @Schema(description = "新推送成功条数（业主端缴费记录中新增未缴账单）")
    private int successCount;

    @Schema(description = "跳过条数（该月账单已存在）")
    private int skipCount;

    @Schema(description = "失败条数")
    private int failCount;

    @Schema(description = "参与处理的业主数（已启用且已绑定房屋）")
    private int totalOwners;

    @Schema(description = "失败样例（最多若干条）")
    private List<String> failSamples = new ArrayList<>();

    public void addFailSample(String line) {
        if (failSamples.size() < 20) {
            failSamples.add(line);
        }
    }
}
