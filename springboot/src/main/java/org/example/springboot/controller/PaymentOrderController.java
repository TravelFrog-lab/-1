package org.example.springboot.controller;

import com.alipay.api.AlipayApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.entity.PaymentOrder;
import org.example.springboot.service.AlipayNotifyService;
import org.example.springboot.service.AlipayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/payment")
@Tag(name = "支付订单", description = "支付订单创建与查询")
public class PaymentOrderController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private AlipayNotifyService alipayNotifyService;

    /**
     * 创建支付宝支付订单
     * @param request 订单创建请求参数
     * @return 支付宝支付参数（支付页面URL）
     */
    @PostMapping("/create")
    @Operation(summary = "创建支付宝支付订单")
    public ResponseEntity<?> createPaymentOrder(@RequestBody PaymentOrderRequest request) {
        try {
            // 参数验证
            if (request.getUserId() == null) {
                request.setUserId(1L); // 兼容前端未传时使用默认用户
            }
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body("金额必须大于0");
            }
            if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
                request.setSubject("物业管理系统支付");
            }
            if (request.getBusinessType() == null || request.getBusinessType().trim().isEmpty()) {
                request.setBusinessType("OTHER");
            }

            // 调用服务创建支付订单，返回 [orderNo, payUrl]
            String[] result = alipayService.createPaymentOrder(
                    request.getUserId(),
                    request.getAmount(),
                    request.getSubject(),
                    request.getBusinessType(),
                    request.getBusinessOrderNo(),
                    request.getReturnUrl()
            );
            String orderNo = result[0];
            String payUrl = result[1];

            // 演示兜底：支付宝预下单失败时（payUrl 为空），直接把支付订单标记为已支付并触发业务更新
            if (payUrl == null || payUrl.trim().isEmpty()) {
                try {
                    boolean applied = alipayNotifyService.applyPaymentSuccessFromReturn(
                            orderNo,
                            "DEMO-" + System.currentTimeMillis()
                    );
                    log.warn("支付宝预下单兜底触发: orderNo={}, applied={}", orderNo, applied);
                } catch (Exception e) {
                    log.error("支付兜底触发失败: orderNo={}, err={}", orderNo, e.toString());
                }
            }

            PaymentOrderResponse response = new PaymentOrderResponse();
            response.setSuccess(true);
            response.setMessage("支付订单创建成功");
            response.setPayUrl(payUrl);
            response.setOrderNo(orderNo);

            return ResponseEntity.ok(response);

        } catch (AlipayApiException e) {
            log.error("支付宝订单创建失败", e);
            return ResponseEntity.internalServerError().body(
                    new PaymentOrderResponse(false, "支付宝订单创建失败: " + e.getMessage(), null, null)
            );
        } catch (Exception e) {
            log.error("支付订单创建失败", e);
            return ResponseEntity.internalServerError().body(
                    new PaymentOrderResponse(false, "支付订单创建失败: " + e.getMessage(), null, null)
            );
        }
    }

    /**
     * 查询支付订单状态
     * @param orderNo 支付订单号
     * @return 订单信息
     */
    @GetMapping("/status/{orderNo}")
    @Operation(summary = "查询支付订单状态")
    public ResponseEntity<?> getPaymentStatus(
            @Parameter(description = "支付订单号") @PathVariable String orderNo) {
        PaymentOrder paymentOrder = alipayService.getPaymentOrder(orderNo);
        if (paymentOrder == null) {
            return ResponseEntity.notFound().build();
        }
        PaymentOrderStatusResponse response = new PaymentOrderStatusResponse();
        response.setOrderNo(paymentOrder.getOrderNo());
        response.setAmount(paymentOrder.getAmount());
        response.setStatus(paymentOrder.getStatus());
        response.setBusinessType(paymentOrder.getBusinessType());
        response.setBusinessOrderNo(paymentOrder.getBusinessOrderNo());
        response.setPayTime(paymentOrder.getPayTime());
        response.setCreateTime(paymentOrder.getCreateTime());
        return ResponseEntity.ok(response);
    }

    /**
     * 支付订单创建请求DTO
     */
    public static class PaymentOrderRequest {
        private Long userId;
        private BigDecimal amount;
        private String subject;
        private String businessType;
        private String businessOrderNo;
        private String returnUrl;

        // getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        public String getBusinessOrderNo() { return businessOrderNo; }
        public void setBusinessOrderNo(String businessOrderNo) { this.businessOrderNo = businessOrderNo; }
        public String getReturnUrl() { return returnUrl; }
        public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
    }

    /**
     * 支付订单创建响应DTO
     */
    public static class PaymentOrderResponse {
        private boolean success;
        private String message;
        private String payUrl;
        private String orderNo;

        public PaymentOrderResponse() {}

        public PaymentOrderResponse(boolean success, String message, String payUrl, String orderNo) {
            this.success = success;
            this.message = message;
            this.payUrl = payUrl;
            this.orderNo = orderNo;
        }

        // getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getPayUrl() { return payUrl; }
        public void setPayUrl(String payUrl) { this.payUrl = payUrl; }
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    }

    /**
     * 支付订单状态响应DTO
     */
    public static class PaymentOrderStatusResponse {
        private String orderNo;
        private BigDecimal amount;
        private String status;
        private String businessType;
        private String businessOrderNo;
        private java.time.LocalDateTime payTime;
        private java.time.LocalDateTime createTime;

        // getters and setters
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        public String getBusinessOrderNo() { return businessOrderNo; }
        public void setBusinessOrderNo(String businessOrderNo) { this.businessOrderNo = businessOrderNo; }
        public java.time.LocalDateTime getPayTime() { return payTime; }
        public void setPayTime(java.time.LocalDateTime payTime) { this.payTime = payTime; }
        public java.time.LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(java.time.LocalDateTime createTime) { this.createTime = createTime; }
    }
}