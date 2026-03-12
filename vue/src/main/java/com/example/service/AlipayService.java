package com.example.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.example.common.Result;
import com.example.config.AlipayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝沙箱电脑网站支付：创建订单并返回支付链接（或表单 HTML）。
 */
@Service
public class AlipayService {

    private static final Logger log = LoggerFactory.getLogger(AlipayService.class);
    private static final String PRODUCT_CODE = "FAST_INSTANT_TRADE_PAY";

    @Autowired
    private AlipayConfig alipayConfig;

    /**
     * 创建电脑网站支付订单，返回 payUrl 供前端跳转支付宝沙箱。
     *
     * @param outTradeNo 商户订单号
     * @param totalAmount 金额（元，如 "0.01"）
     * @param subject 订单标题
     * @param returnUrl 同步回调地址（前端或后端均可）
     * @param notifyUrl 异步通知地址（后端接口，需公网可访问）
     */
    public Result<Map<String, String>> createPagePay(String outTradeNo, String totalAmount, String subject,
                                                      String returnUrl, String notifyUrl) {
        if (alipayConfig.getAppId() == null || alipayConfig.getAppId().isEmpty()) {
            return Result.error("-1", "请先配置 alipay.app-id（支付宝沙箱 AppID）");
        }
        if (alipayConfig.getPrivateKey() == null || alipayConfig.getPrivateKey().isEmpty()) {
            return Result.error("-1", "请先配置 alipay.private-key（应用私钥）");
        }
        if (alipayConfig.getAlipayPublicKey() == null || alipayConfig.getAlipayPublicKey().isEmpty()) {
            return Result.error("-1", "请先配置 alipay.alipay-public-key（支付宝公钥）");
        }

        String rUrl = returnUrl != null && !returnUrl.isEmpty() ? returnUrl : alipayConfig.getReturnUrl();
        String nUrl = notifyUrl != null && !notifyUrl.isEmpty() ? notifyUrl : alipayConfig.getNotifyUrl();
        if (rUrl == null || rUrl.isEmpty()) {
            rUrl = "http://localhost:8080";
        }

        try {
            AlipayClient client = new DefaultAlipayClient(
                    alipayConfig.getGatewayUrl(),
                    alipayConfig.getAppId(),
                    alipayConfig.getPrivateKey(),
                    "json",
                    "UTF-8",
                    alipayConfig.getAlipayPublicKey(),
                    "RSA2"
            );

            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setReturnUrl(rUrl);
            if (nUrl != null && !nUrl.isEmpty()) {
                request.setNotifyUrl(nUrl);
            }

            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(outTradeNo);
            model.setProductCode(PRODUCT_CODE);
            model.setTotalAmount(totalAmount);
            model.setSubject(subject);
            model.setBody(subject);
            request.setBizModel(model);

            // GET 方式返回支付链接，前端直接 location.href 跳转
            String payUrl = client.pageExecute(request, "GET").getBody();
            Map<String, String> data = new HashMap<>();
            data.put("payUrl", payUrl);
            data.put("orderNo", outTradeNo);
            log.info("支付宝沙箱订单创建成功: outTradeNo={}, payUrl={}", outTradeNo, payUrl != null ? payUrl.substring(0, Math.min(80, payUrl.length())) + "..." : "");
            return Result.success(data);
        } catch (AlipayApiException e) {
            log.error("支付宝下单失败", e);
            return Result.error("-1", "支付宝下单失败: " + e.getMessage());
        }
    }
}
