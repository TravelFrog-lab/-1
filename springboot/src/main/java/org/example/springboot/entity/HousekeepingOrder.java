package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("housekeeping_order")
@Schema(description = "家政服务订单")
public class HousekeepingOrder {

    @TableId(type = IdType.AUTO)
    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "业主ID")
    private Long ownerId;

    @Schema(description = "家政人员ID")
    private Long housekeeperId;

    @Schema(description = "服务项目ID")
    private Long serviceId;



    @Schema(description = "预约时间")
    private LocalDateTime appointmentTime;

    @Schema(description = "服务时长(分钟)")
    private Integer serviceDuration;

    @Schema(description = "订单金额")
    private BigDecimal amount;

    @Schema(description = "订单状态: PENDING-待确认, CONFIRMED-已确认, IN_PROGRESS-进行中, COMPLETED-已完成, CANCELLED-已取消")
    private String status;

    @Schema(description = "支付状态: UNPAID-未支付, PAID-已支付, REFUNDED-已退款")
    private String paymentStatus;

    @Schema(description = "支付方式: ALIPAY-支付宝, WECHAT-微信, CASH-现金")
    private String payType;

    @Schema(description = "支付宝订单号（商户侧）")
    private String alipayOrderNo;

    @Schema(description = "支付宝交易号（平台侧）")
    private String alipayTradeNo;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "支付宝异步通知时间")
    private LocalDateTime notifyTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Schema(description = "业主信息")
    private Owner owner;

    @TableField(exist = false)
    @Schema(description = "家政人员信息")
    private Housekeeper housekeeper;

    @TableField(exist = false)
    @Schema(description = "服务项目信息")
    private HousekeepingService service;

    // 手动添加getter/setter方法（兼容性）
    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getHousekeeperId() {
        return housekeeperId;
    }

    public void setHousekeeperId(Long housekeeperId) {
        this.housekeeperId = housekeeperId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public Integer getServiceDuration() {
        return serviceDuration;
    }

    public void setServiceDuration(Integer serviceDuration) {
        this.serviceDuration = serviceDuration;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
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

    public LocalDateTime getPayTime() {
        return payTime;
    }

    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }

    public LocalDateTime getNotifyTime() {
        return notifyTime;
    }

    public void setNotifyTime(LocalDateTime notifyTime) {
        this.notifyTime = notifyTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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