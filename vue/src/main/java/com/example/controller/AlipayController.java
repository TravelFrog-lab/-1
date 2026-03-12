package com.example.controller;

import com.example.common.Result;
import com.example.service.AlipayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付宝沙箱支付接口。前端“确认支付”会调 POST /alipay/pay，后端返回 payUrl，前端跳转支付宝沙箱。
 */
@RestController
@RequestMapping("/alipay")
@Tag(name = "支付宝沙箱支付", description = "电脑网站支付，需配置 alipay.app-id、private-key、alipay-public-key 等")
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    @PostMapping("/pay")
    @Operation(summary = "创建支付订单并返回支付链接")
    public Result<Map<String, String>> pay(@RequestBody Map<String, Object> body) {
        String amount = getString(body, "amount", "totalAmount");
        String subject = getString(body, "subject");
        String outTradeNo = getString(body, "outTradeNo", "businessOrderNo");
        String returnUrl = getString(body, "returnUrl");
        String notifyUrl = getString(body, "notifyUrl");

        if (amount == null || amount.isEmpty()) {
            amount = "0.01";
        }
        if (subject == null || subject.isEmpty()) {
            subject = "物业管理系统支付";
        }
        if (outTradeNo == null || outTradeNo.isEmpty()) {
            outTradeNo = "ORD" + System.currentTimeMillis();
        }

        return alipayService.createPagePay(outTradeNo, amount, subject, returnUrl, notifyUrl);
    }

    private static String getString(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object v = map.get(key);
            if (v != null && !v.toString().isEmpty()) {
                return v.toString().trim();
            }
        }
        return null;
    }
}
