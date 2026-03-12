package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("parking_fee")
@Schema(description = "停车费记录")
public class ParkingFee {

    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "停车位ID")
    private Long parkingSpaceId;

    @Schema(description = "业主ID")
    private Long ownerId;

    @Schema(description = "车牌号")
    private String plateNumber;

    @Schema(description = "入场时间")
    private LocalDateTime entryTime;

    @Schema(description = "出场时间")
    private LocalDateTime exitTime;

    @Schema(description = "停车费金额")
    private BigDecimal feeAmount;

    @Schema(description = "停车费所属年月")
    private LocalDate feeDate;

    @Schema(description = "停车费金额")
    private BigDecimal amount;

    @Schema(description = "缴费状态")
    private Status status;

    @Schema(description = "支付方式: ALIPAY-支付宝, WECHAT-微信, CASH-现金")
    private String payType;

    @Schema(description = "支付宝订单号（商户侧）")
    private String alipayOrderNo;

    @Schema(description = "支付宝交易号（平台侧）")
    private String alipayTradeNo;

    @Schema(description = "缴费时间")
    private LocalDateTime paymentTime;

    @Schema(description = "支付宝异步通知时间")
    private LocalDateTime notifyTime;

    @Schema(description = "缴费备注")
    private String remarks;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Schema(description = "关联的业主信息")
    private Owner owner;

    @TableField(exist = false)
    @Schema(description = "关联的停车位信息")
    private ParkingSpace parkingSpace;

    public enum Status {
        PAID,    // 已缴费
        UNPAID   // 未缴费
    }
}