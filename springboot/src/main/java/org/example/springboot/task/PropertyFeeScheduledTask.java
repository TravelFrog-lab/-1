package org.example.springboot.task;

import org.example.springboot.common.Result;
import org.example.springboot.dto.PropertyFeePushResult;
import org.example.springboot.service.PropertyFeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 物业费：每月定时为业主生成当月待缴账单（与管理员「一键推送到业主端」同一套逻辑）。
 */
@Component
@ConditionalOnProperty(name = "property-fee.scheduled-push.enabled", havingValue = "true", matchIfMissing = true)
public class PropertyFeeScheduledTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyFeeScheduledTask.class);
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    @Resource
    private PropertyFeeService propertyFeeService;

    /**
     * 默认：每月 10 日 09:00:00 执行，推送「当前自然月」账单。
     * Cron 为 6 位：秒 分 时 日 月 周，可通过 property-fee.scheduled-push.cron 覆盖。
     */
    @Scheduled(cron = "${property-fee.scheduled-push.cron:0 0 9 10 * ?}")
    public void monthlyPushPropertyFeesToOwners() {
        String month = LocalDate.now().format(MONTH_FMT);
        LOGGER.info("[物业费定时任务] 开始推送月份 {} 的待缴账单到业主端", month);
        try {
            Result<PropertyFeePushResult> result = propertyFeeService.pushPropertyFeesToOwners(month);
            if (result == null) {
                LOGGER.warn("[物业费定时任务] 返回为空");
                return;
            }
            if (!"0".equals(result.getCode())) {
                LOGGER.warn("[物业费定时任务] 执行未成功: code={}, msg={}", result.getCode(), result.getMsg());
                return;
            }
            PropertyFeePushResult data = result.getData();
            if (data != null) {
                LOGGER.info("[物业费定时任务] 完成 month={} 总业主={} 成功={} 跳过={} 失败={}",
                        data.getFeeMonth(), data.getTotalOwners(),
                        data.getSuccessCount(), data.getSkipCount(), data.getFailCount());
            } else {
                LOGGER.info("[物业费定时任务] 完成，无统计明细");
            }
        } catch (Exception e) {
            LOGGER.error("[物业费定时任务] 异常: {}", e.getMessage(), e);
        }
    }
}
