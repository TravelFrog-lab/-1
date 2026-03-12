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
@TableName("property_fee")
@Schema(description = "物业费记录")
public class PropertyFee {

    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "房屋id")
    private Long houseId;
    @Schema(description = "业主Id")
    private Long ownerId;

    @Schema(description = "物业费所属年月")
    private LocalDate feeDate;

    @Schema(description = "物业费金额")
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
    private  Owner owner;
    @TableField(exist = false)
    @Schema(description = "关联的房屋信息")
    private House house;

    public enum Status {
        PAID,    // 已缴费
        UNPAID   // 未缴费
    }

    // 手动添加getter/setter方法（兼容性）
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getHouseId() {
        return houseId;
    }

    public void setHouseId(Long houseId) {
        this.houseId = houseId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public LocalDate getFeeDate() {
        return feeDate;
    }

    public void setFeeDate(LocalDate feeDate) {
        this.feeDate = feeDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getAlipayOrderNo() {
        return alipayOrderNo;
    }

    public void setAlipayOrderNo(String alipayOrderNo) {
        this.alipayOrderNo = alipayOrderNo;
    }

    public String getAlipayTradeNo() {
        return alipayTradeNo;
    }

    public void setAlipayTradeNo(String alipayTradeNo) {
        this.alipayTradeNo = alipayTradeNo;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public LocalDateTime getNotifyTime() {
        return notifyTime;
    }

    public void setNotifyTime(LocalDateTime notifyTime) {
        this.notifyTime = notifyTime;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
} 