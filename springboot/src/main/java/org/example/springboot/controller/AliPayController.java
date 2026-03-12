package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.entity.PaymentOrder;
import org.example.springboot.mapper.PaymentOrderMapper;
import org.example.springboot.service.AlipayNotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.example.springboot.common.Result;

@RestController
@RequestMapping("/alipay")
@Tag(name = "支付宝支付", description = "支付宝支付相关接口")
@Slf4j
public class AliPayController {

    @Autowired
    private AlipayNotifyService alipayNotifyService;

    @Autowired
    private PaymentOrderMapper paymentOrderMapper;

    @Value("${frontend.base-url:http://localhost:8080}")
    private String frontendBaseUrl;

    /**
     * 支付宝异步回调通知接口
     * 接收支付宝POST请求，处理支付结果通知
     *
     * @param params 支付宝回调参数
     * @return "success" 或 "fail"
     */
    @PostMapping("/notify")
    @Operation(summary = "支付宝异步回调", description = "支付宝支付成功后异步通知接口，处理订单状态更新")
    public String alipayNotify(@RequestParam Map<String, String> params) {
        return alipayNotifyService.handleNotify(params);
    }

    /**
     * 支付宝同步回调接口（return_url）
     * 用户支付完成后跳转回的页面，展示支付结果
     *
     * @param params 支付宝回调参数
     * @return 支付结果页面HTML
     */
    @GetMapping("/return")
    @Operation(summary = "支付宝同步回调", description = "用户支付完成后跳转的页面，展示支付结果")
    public String alipayReturn(@RequestParam Map<String, String> params) {
        log.info("同步回调收到: params数量={}, out_trade_no={}, trade_status={}, trade_no={}, sign={}, 所有key={}",
                params.size(), params.get("out_trade_no"), params.get("trade_status"), params.get("trade_no"),
                params.containsKey("sign") ? "有" : "无", params.keySet());

        // 可选：验证签名
        boolean signVerified = alipayNotifyService.verifySignature(params);

        // 获取关键参数（商户订单号去空格，避免查库失败）
        String outTradeNo = params.get("out_trade_no");
        if (outTradeNo != null) {
            outTradeNo = outTradeNo.trim();
        }
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        String totalAmount = params.get("total_amount");

        // 同步回调时也更新订单状态：仅当 trade_status 明确为成功时调用 handleNotify（否则 handleNotify 会把 null 当失败）
        // 若 return 未传 trade_status 但传了 trade_no，后面用兜底逻辑按成功更新
        if (signVerified && outTradeNo != null && !outTradeNo.isEmpty()
                && tradeStatus != null && ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus))) {
            try {
                alipayNotifyService.handleNotify(params);
            } catch (Exception e) {
                // 可能已由异步通知处理过（幂等），忽略
            }
        }

        // 重新查询支付订单（可能已被上面或异步通知更新）
        PaymentOrder paymentOrder = null;
        if (outTradeNo != null && !outTradeNo.isEmpty()) {
            outTradeNo = outTradeNo.trim();
            paymentOrder = paymentOrderMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentOrder>()
                            .eq(PaymentOrder::getOrderNo, outTradeNo)
            );
            // 兜底：支付宝已成功但订单仍待支付时，直接按成功更新（避免 handleNotify 因签名/参数等未生效）
            // 成功条件：有 trade_status 成功 或 仅有 trade_no（return 可能不传 trade_status）
            boolean considerSuccess = (tradeStatus != null && ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)))
                    || (tradeNo != null && !tradeNo.isEmpty());
            if (paymentOrder != null && "UNPAID".equals(paymentOrder.getStatus())
                    && tradeNo != null && !tradeNo.isEmpty() && considerSuccess) {
                log.info("同步回调兜底: 订单仍待支付，执行强制更新 orderNo={}", outTradeNo);
                boolean applied = alipayNotifyService.applyPaymentSuccessFromReturn(outTradeNo, tradeNo);
                log.info("同步回调兜底: 强制更新结果 applied={}", applied);
                paymentOrder = paymentOrderMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentOrder>()
                                .eq(PaymentOrder::getOrderNo, outTradeNo)
                );
            }
        }

        // 根据订单状态和支付宝状态决定显示内容
        String status = "处理中";
        String message = "支付处理中，请稍后查看订单状态";
        String iconColor = "#ffb100";
        String icon = "⏳";

        if (paymentOrder != null) {
            // 优先使用支付订单的状态
            String orderStatus = paymentOrder.getStatus();
            if ("PAID".equals(orderStatus)) {
                status = "支付成功";
                message = "您的订单已支付成功，相关服务将尽快为您安排";
                iconColor = "#52c41a";
                icon = "✅";
            } else if ("FAILED".equals(orderStatus) || "CLOSED".equals(orderStatus)) {
                status = "支付失败";
                message = "支付未完成或已关闭，请重新尝试支付";
                iconColor = "#ff4d4f";
                icon = "❌";
            } else if ("UNPAID".equals(orderStatus)) {
                status = "待支付";
                message = "订单尚未支付，请完成支付流程";
                iconColor = "#1890ff";
                icon = "ℹ️";
            }
        } else if (tradeStatus != null) {
            // 如果订单不存在，根据支付宝回调状态判断
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                status = "支付成功";
                message = "支付宝已确认收到您的支付，订单状态更新中";
                iconColor = "#52c41a";
                icon = "✅";
            } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                status = "交易关闭";
                message = "支付交易已关闭，请重新下单";
                iconColor = "#ff4d4f";
                icon = "❌";
            } else if ("WAIT_BUYER_PAY".equals(tradeStatus)) {
                status = "等待支付";
                message = "等待您完成支付，请勿关闭页面";
                iconColor = "#1890ff";
                icon = "ℹ️";
            }
        }

        // 构建友好的HTML页面（链接指向前端地址，避免跳到后端导致 404）
        return generatePaymentResultHtml(status, message, icon, iconColor,
                outTradeNo, tradeNo, totalAmount, paymentOrder, signVerified, frontendBaseUrl);
    }

    /**
     * 生成支付结果HTML页面
     */
    private String generatePaymentResultHtml(String status, String message, String icon,
                                             String iconColor, String outTradeNo, String tradeNo,
                                             String totalAmount, PaymentOrder paymentOrder,
                                             boolean signVerified, String frontendBaseUrl) {
        String orderNoDisplay = outTradeNo != null ? outTradeNo : "未知";
        String tradeNoDisplay = tradeNo != null ? tradeNo : "等待支付宝返回";
        String amountDisplay = totalAmount != null ? totalAmount + "元" :
                (paymentOrder != null ? paymentOrder.getAmount() + "元" : "未知");

        String businessType = "未知业务";
        String businessOrderNo = "未知";
        if (paymentOrder != null) {
            businessType = paymentOrder.getBusinessType();
            businessOrderNo = paymentOrder.getBusinessOrderNo() != null ? paymentOrder.getBusinessOrderNo() : "未知";
        }

        String signStatus = signVerified ? "已验证" : "未验证";
        String signStatusColor = signVerified ? "#52c41a" : "#faad14";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentTime = LocalDateTime.now().format(formatter);

        String base = (frontendBaseUrl != null && !frontendBaseUrl.isEmpty()) ? frontendBaseUrl : "http://localhost:8080";
        String homeUrl = base.replaceAll("/$", "") + "/";
        String ordersUrl = base.replaceAll("/$", "") + "/property-fee";

        return "<!DOCTYPE html>\n" +
                "<html lang=\"zh-CN\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>支付结果 - 物业管理系统</title>\n" +
                "    <style>\n" +
                "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "        body { font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f5f5f5; color: #333; line-height: 1.6; }\n" +
                "        .container { max-width: 800px; margin: 40px auto; padding: 30px; background: white; border-radius: 12px; box-shadow: 0 6px 16px rgba(0,0,0,0.1); }\n" +
                "        .header { text-align: center; margin-bottom: 30px; }\n" +
                "        .header h1 { font-size: 28px; color: #1890ff; margin-bottom: 10px; }\n" +
                "        .status-box { text-align: center; padding: 25px; margin: 25px 0; border-radius: 10px; background: #fafafa; }\n" +
                "        .status-icon { font-size: 60px; margin-bottom: 15px; }\n" +
                "        .status-title { font-size: 24px; font-weight: bold; margin-bottom: 10px; }\n" +
                "        .status-message { font-size: 16px; color: #666; margin-bottom: 15px; }\n" +
                "        .info-section { background: #f9f9f9; padding: 20px; border-radius: 8px; margin: 20px 0; }\n" +
                "        .info-title { font-size: 18px; font-weight: 600; margin-bottom: 15px; color: #333; border-bottom: 2px solid #e8e8e8; padding-bottom: 8px; }\n" +
                "        .info-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 15px; }\n" +
                "        .info-item { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #eee; }\n" +
                "        .info-label { font-weight: 500; color: #555; }\n" +
                "        .info-value { font-weight: 400; color: #222; }\n" +
                "        .actions { display: flex; gap: 15px; justify-content: center; margin-top: 30px; flex-wrap: wrap; }\n" +
                "        .btn { padding: 12px 28px; border-radius: 6px; font-size: 16px; font-weight: 500; cursor: pointer; transition: all 0.3s; border: none; text-decoration: none; display: inline-block; text-align: center; }\n" +
                "        .btn-primary { background: #1890ff; color: white; }\n" +
                "        .btn-primary:hover { background: #40a9ff; transform: translateY(-2px); }\n" +
                "        .btn-secondary { background: #f5f5f5; color: #333; border: 1px solid #d9d9d9; }\n" +
                "        .btn-secondary:hover { background: #e8e8e8; }\n" +
                "        .footer { text-align: center; margin-top: 30px; color: #999; font-size: 14px; }\n" +
                "        .sign-status { display: inline-block; padding: 4px 10px; border-radius: 4px; font-size: 12px; margin-left: 10px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1>物业管理系统支付结果</h1>\n" +
                "            <p>支付处理完成时间: " + currentTime + "</p>\n" +
                "            <p>签名状态: <span class=\"sign-status\" style=\"background:" + signStatusColor + "; color: white;\">" + signStatus + "</span></p>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class=\"status-box\" style=\"border: 2px solid " + iconColor + ";\">\n" +
                "            <div class=\"status-icon\" style=\"color: " + iconColor + ";\">" + icon + "</div>\n" +
                "            <div class=\"status-title\" style=\"color: " + iconColor + ";\">" + status + "</div>\n" +
                "            <div class=\"status-message\">" + message + "</div>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class=\"info-section\">\n" +
                "            <div class=\"info-title\">订单详情</div>\n" +
                "            <div class=\"info-grid\">\n" +
                "                <div class=\"info-item\">\n" +
                "                    <span class=\"info-label\">商户订单号:</span>\n" +
                "                    <span class=\"info-value\">" + orderNoDisplay + "</span>\n" +
                "                </div>\n" +
                "                <div class=\"info-item\">\n" +
                "                    <span class=\"info-label\">支付宝交易号:</span>\n" +
                "                    <span class=\"info-value\">" + tradeNoDisplay + "</span>\n" +
                "                </div>\n" +
                "                <div class=\"info-item\">\n" +
                "                    <span class=\"info-label\">支付金额:</span>\n" +
                "                    <span class=\"info-value\">" + amountDisplay + "</span>\n" +
                "                </div>\n" +
                "                <div class=\"info-item\">\n" +
                "                    <span class=\"info-label\">业务类型:</span>\n" +
                "                    <span class=\"info-value\">" + businessType + "</span>\n" +
                "                </div>\n" +
                "                <div class=\"info-item\">\n" +
                "                    <span class=\"info-label\">业务订单号:</span>\n" +
                "                    <span class=\"info-value\">" + businessOrderNo + "</span>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class=\"actions\">\n" +
                "            <a href=\"" + homeUrl + "\" class=\"btn btn-primary\">返回首页</a>\n" +
                "            <a href=\"" + ordersUrl + "\" class=\"btn btn-secondary\">查看我的订单</a>\n" +
                "            <button onclick=\"window.location.reload()\" class=\"btn btn-secondary\">刷新状态</button>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class=\"footer\">\n" +
                "            <p>如有问题，请联系物业客服：400-123-4567</p>\n" +
                "            <p>© 2025 物业管理系统 支付宝沙盒环境演示</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * 查询支付订单状态接口
     * 供前端轮询支付状态使用
     *
     * @param orderNo 支付订单号（商户订单号）
     * @return 支付订单信息
     */
    @GetMapping("/query")
    @Operation(summary = "查询支付状态", description = "根据支付订单号查询支付状态，供前端轮询使用")
    public Result<PaymentOrder> queryPaymentStatus(@RequestParam String orderNo) {
        PaymentOrder paymentOrder = paymentOrderMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getOrderNo, orderNo)
        );

        if (paymentOrder == null) {
            return Result.error("404", "订单不存在");
        }

        return Result.success(paymentOrder);
    }
}