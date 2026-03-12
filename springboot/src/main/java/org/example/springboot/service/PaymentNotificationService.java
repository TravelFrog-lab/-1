package org.example.springboot.service;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot.entity.Notification;
import org.example.springboot.entity.PaymentOrder;
import org.example.springboot.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 支付结果通知服务
 * 处理支付成功/失败时的通知发送，包括站内信和模拟短信
 */
@Slf4j
@Service
public class PaymentNotificationService {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @Autowired
    private SmsService smsService;

    /**
     * 发送支付结果通知
     *
     * @param paymentOrder 支付订单
     * @param paymentStatus 支付状态（PAID/FAILED/CLOSED等）
     * @param tradeNo 支付宝交易号（可选）
     */
    public void sendPaymentNotification(PaymentOrder paymentOrder, String paymentStatus, String tradeNo) {
        if (paymentOrder == null) {
            log.error("支付订单为空，无法发送通知");
            return;
        }

        Long userId = paymentOrder.getUserId();
        if (userId == null) {
            log.error("支付订单缺少用户ID，无法发送通知: orderNo={}", paymentOrder.getOrderNo());
            return;
        }

        // 获取用户信息
        User user = userService.getUserById(userId.intValue());
        if (user == null) {
            log.error("用户不存在，无法发送通知: userId={}", userId);
            return;
        }

        // 生成通知内容
        String title = generateNotificationTitle(paymentStatus);
        String content = generateNotificationContent(paymentOrder, paymentStatus, tradeNo);
        String smsContent = generateSmsContent(paymentOrder, paymentStatus);

        // 1. 发送站内信
        sendInAppNotification(userId, title, content);

        // 2. 模拟发送短信（实际环境可调用真实短信服务）
        simulateSmsNotification(user.getPhone(), smsContent);

        // 3. 记录通知日志
        logPaymentNotification(paymentOrder, paymentStatus, tradeNo, userId, user.getPhone());

        log.info("支付结果通知发送完成: orderNo={}, status={}, userId={}",
                paymentOrder.getOrderNo(), paymentStatus, userId);
    }

    /**
     * 生成通知标题
     */
    private String generateNotificationTitle(String paymentStatus) {
        switch (paymentStatus) {
            case "PAID":
                return "支付成功通知";
            case "FAILED":
                return "支付失败通知";
            case "CLOSED":
                return "交易关闭通知";
            case "REFUNDED":
                return "退款成功通知";
            default:
                return "支付状态更新通知";
        }
    }

    /**
     * 生成通知内容
     */
    private String generateNotificationContent(PaymentOrder paymentOrder, String paymentStatus, String tradeNo) {
        StringBuilder content = new StringBuilder();
        content.append("尊敬的业主，您的支付订单状态已更新：\n\n");
        content.append("订单号：").append(paymentOrder.getOrderNo()).append("\n");
        content.append("业务类型：").append(paymentOrder.getBusinessType()).append("\n");
        content.append("支付金额：").append(paymentOrder.getAmount()).append("元\n");
        content.append("支付状态：").append(mapStatusToChinese(paymentStatus)).append("\n");

        if (tradeNo != null && !tradeNo.isEmpty()) {
            content.append("支付宝交易号：").append(tradeNo).append("\n");
        }

        content.append("\n");
        content.append(generateStatusMessage(paymentStatus));
        content.append("\n\n感谢您使用物业管理系统！");

        return content.toString();
    }

    /**
     * 生成短信内容
     */
    private String generateSmsContent(PaymentOrder paymentOrder, String paymentStatus) {
        return String.format("【物业管理系统】您的订单%s支付%s，金额%s元。%s",
                paymentOrder.getOrderNo(),
                mapStatusToChinese(paymentStatus),
                paymentOrder.getAmount(),
                generateShortStatusMessage(paymentStatus));
    }

    /**
     * 发送站内信
     */
    private void sendInAppNotification(Long userId, String title, String content) {
        try {
            Notification notification = new Notification();
            notification.setReceiverId(userId);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setReceiverType(Notification.ReceiverType.OWNER);
            notification.setIsRead(0);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setUpdatedAt(LocalDateTime.now());

            notificationService.add(notification);
            log.debug("站内信发送成功: userId={}, title={}", userId, title);
        } catch (Exception e) {
            log.error("站内信发送失败: userId={}, title={}", userId, title, e);
        }
    }

    /**
     * 模拟短信发送
     */
    private void simulateSmsNotification(String phone, String content) {
        try {
            // 实际环境中可以调用真实的短信服务
            // 这里仅模拟发送并记录日志
            log.info("[模拟短信发送] 手机号: {}, 内容: {}", phone, content);

            // 如果是测试环境，可以记录到数据库或文件中
            // 这里仅记录到日志

        } catch (Exception e) {
            log.error("模拟短信发送失败: phone={}", phone, e);
        }
    }

    /**
     * 记录支付通知日志
     */
    private void logPaymentNotification(PaymentOrder paymentOrder, String paymentStatus,
                                        String tradeNo, Long userId, String phone) {
        // 这里可以记录到专门的日志表或文件中
        // 目前仅记录到应用日志
        log.info("支付通知日志 - 订单号: {}, 用户ID: {}, 手机号: {}, 状态: {}, 支付宝交易号: {}",
                paymentOrder.getOrderNo(), userId, phone, paymentStatus, tradeNo != null ? tradeNo : "N/A");
    }

    /**
     * 将系统状态码映射为中文
     */
    private String mapStatusToChinese(String status) {
        switch (status) {
            case "PAID":
                return "成功";
            case "FAILED":
                return "失败";
            case "CLOSED":
                return "关闭";
            case "REFUNDED":
                return "已退款";
            case "UNPAID":
                return "未支付";
            default:
                return status;
        }
    }

    /**
     * 生成状态相关的提示信息
     */
    private String generateStatusMessage(String paymentStatus) {
        switch (paymentStatus) {
            case "PAID":
                return "您的支付已成功完成，相关服务将尽快为您安排。";
            case "FAILED":
                return "支付未成功，请您检查支付账户或重新尝试支付。";
            case "CLOSED":
                return "本次交易已关闭，如有疑问请联系客服。";
            case "REFUNDED":
                return "退款已成功处理，款项将原路退回。";
            default:
                return "您的支付订单状态已更新，请关注最新状态。";
        }
    }

    /**
     * 生成简短的状态提示信息（用于短信）
     */
    private String generateShortStatusMessage(String paymentStatus) {
        switch (paymentStatus) {
            case "PAID":
                return "请留意服务安排。";
            case "FAILED":
                return "请重新尝试支付。";
            case "CLOSED":
                return "请联系客服。";
            case "REFUNDED":
                return "请查收退款。";
            default:
                return "";
        }
    }

    /**
     * 批量发送支付结果通知（适用于批量处理场景）
     *
     * @param paymentOrders 支付订单列表
     * @param paymentStatus 支付状态
     */
    public void batchSendPaymentNotifications(java.util.List<PaymentOrder> paymentOrders, String paymentStatus) {
        if (paymentOrders == null || paymentOrders.isEmpty()) {
            log.warn("支付订单列表为空，跳过批量通知");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (PaymentOrder order : paymentOrders) {
            try {
                sendPaymentNotification(order, paymentStatus, order.getAlipayTradeNo());
                successCount++;
            } catch (Exception e) {
                log.error("支付通知发送失败: orderNo={}", order.getOrderNo(), e);
                failCount++;
            }
        }

        log.info("批量支付通知完成: 总数={}, 成功={}, 失败={}",
                paymentOrders.size(), successCount, failCount);
    }
}