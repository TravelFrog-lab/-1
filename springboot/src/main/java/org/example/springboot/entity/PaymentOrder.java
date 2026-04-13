package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_order")
@Schema(description = "支付订单")
public class PaymentOrder {

    @TableId(type = IdType.AUTO)
    @Schema(description = "支付订单ID")
    private Long id;

    @Schema(description = "支付订单号，唯一")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "订单金额")
    private BigDecimal amount;

    @Schema(description = "支付状态: UNPAID-未支付, PAID-已支付, FAILED-支付失败, REFUNDED-已退款, CLOSED-已关闭")
    private String status;

    @Schema(description = "支付方式: ALIPAY-支付宝, WECHAT-微信")
    private String payType;

    @Schema(description = "业务类型: HOUSEKEEPING-家政服务, PROPERTY_FEE-物业费, REPAIR-报修")
    private String businessType;

    @Schema(description = "业务订单号（如家政订单号HK123、物业费订单号WY456）")
    private String businessOrderNo;

    @Schema(description = "支付宝商户订单号（out_trade_no）")
    private String alipayOrderNo;

    @Schema(description = "支付宝交易号（trade_no）")
    private String alipayTradeNo;

    @Schema(description = "订单标题")
    private String subject;

    @Schema(description = "支付返回URL（return_url）")
    private String returnUrl;

    @Schema(description = "异步通知URL（notify_url）")
    private String notifyUrl;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "备注")
    private String remark;
}