package org.example.springboot.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.config.AlipayConfig;
import org.example.springboot.entity.PaymentOrder;
import org.example.springboot.mapper.PaymentOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class AlipayService {

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private PaymentOrderMapper paymentOrderMapper;

    private AlipayClient getAlipayClient() {
        return new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getPrivateKey(),
                alipayConfig.getFormat(),
                alipayConfig.getCharset(),
                alipayConfig.getPublicKey(),
                alipayConfig.getSignType()
        );
    }

    /**
     * 生成支付订单号（格式：PAYyyyyMMddHHmmss + 6位随机数）
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        return "PAY" + timestamp + random;
    }

    /**
     * 创建支付宝支付订单（预下单）
     * @return [0]=支付订单号 orderNo, [1]=支付跳转 URL（GET 方式，供前端 location.href 跳转）
     */
    @Transactional
    public String[] createPaymentOrder(Long userId, BigDecimal amount, String subject,
                                     String businessType, String businessOrderNo, String returnUrl) throws AlipayApiException {
        // 1. 生成支付订单号
        String paymentOrderNo = generateOrderNo();

        // 2. 创建支付订单记录
        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setOrderNo(paymentOrderNo);
        paymentOrder.setUserId(userId);
        paymentOrder.setAmount(amount);
        paymentOrder.setSubject(subject);
        paymentOrder.setBusinessType(businessType != null ? businessType : "OTHER");
        paymentOrder.setBusinessOrderNo(businessOrderNo);
        paymentOrder.setPayType("ALIPAY");
        paymentOrder.setStatus("UNPAID");
        paymentOrder.setReturnUrl(returnUrl != null && !returnUrl.isEmpty() ? returnUrl : alipayConfig.getReturnUrl());
        paymentOrder.setNotifyUrl(alipayConfig.getNotifyUrl());
        paymentOrder.setCreateTime(LocalDateTime.now());
        paymentOrder.setUpdateTime(LocalDateTime.now());

        // 3. 保存到数据库
        paymentOrderMapper.insert(paymentOrder);
        log.info("支付订单创建成功: orderNo={}, amount={}, businessType={}", paymentOrderNo, amount, businessType);

        // 4. 调用支付宝预下单：同步回调地址一律使用配置文件，确保跳到后端而非前端
        String payUrl = createAlipayOrder(paymentOrderNo, amount, subject, alipayConfig.getReturnUrl());

        return new String[]{paymentOrderNo, payUrl};
    }

    /**
     * 调用支付宝SDK创建订单（内部方法），GET 方式返回支付链接
     * @return 支付跳转 URL（前端用 window.location.href 即可打开支付宝沙箱）
     */
    public String createAlipayOrder(String orderNo, BigDecimal amount, String subject, String returnUrl) throws AlipayApiException {
        AlipayClient alipayClient = getAlipayClient();
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(returnUrl != null ? returnUrl : alipayConfig.getReturnUrl());
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setBizContent(String.format(
                "{\"out_trade_no\":\"%s\",\"total_amount\":\"%s\",\"subject\":\"%s\",\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}",
                orderNo, amount.toString(), subject
        ));
        // 使用 GET 方式，返回支付链接 URL，前端可直接 location.href 跳转
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "GET");
        if (response.isSuccess()) {
            log.info("支付宝预下单成功: orderNo={}, return_url={}", orderNo, request.getReturnUrl());
            return response.getBody();
        } else {
            log.error("支付宝预下单失败: orderNo={}, msg={}", orderNo, response.getSubMsg());
            throw new RuntimeException("支付宝订单创建失败: " + response.getSubMsg());
        }
    }

    /**
     * 根据支付订单号查询支付订单
     * @param orderNo 支付订单号
     * @return PaymentOrder
     */
    public PaymentOrder getPaymentOrder(String orderNo) {
        return paymentOrderMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getOrderNo, orderNo)
        );
    }

    /**
     * 更新支付订单状态
     * @param orderNo 支付订单号
     * @param status 状态
     * @param alipayTradeNo 支付宝交易号（可选）
     */
    @Transactional
    public void updatePaymentStatus(String orderNo, String status, String alipayTradeNo) {
        PaymentOrder paymentOrder = getPaymentOrder(orderNo);
        if (paymentOrder != null) {
            // 幂等性检查：如果订单已经是最终状态，跳过更新
            if (isFinalStatus(paymentOrder.getStatus())) {
                log.info("支付订单已是最终状态，跳过更新: orderNo={}, currentStatus={}", orderNo, paymentOrder.getStatus());
                return;
            }

            paymentOrder.setStatus(status);
            paymentOrder.setAlipayTradeNo(alipayTradeNo);
            paymentOrder.setUpdateTime(LocalDateTime.now());
            if ("PAID".equals(status)) {
                paymentOrder.setPayTime(LocalDateTime.now());
            }
            paymentOrderMapper.updateById(paymentOrder);
            log.info("支付订单状态更新: orderNo={}, status={}", orderNo, status);
        }
    }

    /**
     * 判断订单状态是否为最终状态
     * @param status 订单状态
     * @return true: 最终状态，false: 非最终状态
     */
    private boolean isFinalStatus(String status) {
        return "PAID".equals(status) || "FAILED".equals(status) ||
                "CLOSED".equals(status) || "REFUNDED".equals(status);
    }
}