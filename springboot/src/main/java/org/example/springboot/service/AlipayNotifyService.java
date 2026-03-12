package org.example.springboot.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.config.AlipayConfig;
import org.example.springboot.entity.*;
import org.example.springboot.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 支付宝异步通知处理服务
 * 处理支付宝支付回调，更新支付订单状态并触发业务逻辑
 */
@Slf4j
@Service
public class AlipayNotifyService {

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private PaymentOrderMapper paymentOrderMapper;

    @Autowired
    private HousekeepingOrderMapper housekeepingOrderMapper;

    @Autowired
    private PropertyFeeMapper propertyFeeMapper;

    @Autowired
    private ParkingFeeMapper parkingFeeMapper;

    @Autowired
    private RepairRecordMapper repairRecordMapper;

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private PaymentOrderService paymentOrderService;

    @Autowired
    private PaymentNotificationService paymentNotificationService;

    /**
     * 验证支付宝回调签名
     * @param params 回调参数
     * @return 验证结果
     */
    public boolean verifySignature(Map<String, String> params) {
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType()
            );
            if (!signVerified) {
                log.error("支付宝回调签名验证失败");
            }
            return signVerified;
        } catch (AlipayApiException e) {
            log.error("支付宝签名验证异常", e);
            return false;
        }
    }

    /**
     * 处理支付宝异步通知
     * @param params 回调参数
     * @return 处理结果 "success" 或 "fail"
     */
    @Transactional
    public String handleNotify(Map<String, String> params) {
        // 1. 验证签名
        if (!verifySignature(params)) {
            return "fail";
        }

        // 2. 解析关键参数（去除首尾空格，避免 GET 参数导致查不到订单）
        String outTradeNo = params.get("out_trade_no");
        if (outTradeNo != null) {
            outTradeNo = outTradeNo.trim();
        }
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        String totalAmount = params.get("total_amount");

        log.info("收到支付宝异步通知: 订单号={}, 支付宝交易号={}, 状态={}, 金额={}",
                outTradeNo, tradeNo, tradeStatus, totalAmount);

        // 3. 查询支付订单
        PaymentOrder paymentOrder = paymentOrderService.getByOrderNo(outTradeNo);

        if (paymentOrder == null) {
            log.error("支付订单不存在: outTradeNo={}", outTradeNo);
            return "fail";
        }

        // 4. 幂等性检查：如果订单已经是最终状态，直接返回成功
        if (isFinalStatus(paymentOrder.getStatus())) {
            log.info("支付订单已是最终状态，跳过处理: orderNo={}, status={}", outTradeNo, paymentOrder.getStatus());
            return "success";
        }

        // 5. 根据交易状态更新支付订单状态
        String newStatus = mapTradeStatus(tradeStatus);
        if (newStatus == null) {
            log.error("未知的交易状态: tradeStatus={}", tradeStatus);
            return "fail";
        }

        // 更新支付订单状态
        boolean updateSuccess = paymentOrderService.updateStatusWithAlipayInfo(
                outTradeNo, newStatus, outTradeNo, tradeNo);

        if (!updateSuccess) {
            log.error("支付订单状态更新失败: orderNo={}, newStatus={}", outTradeNo, newStatus);
            return "fail";
        }

        log.info("支付订单状态更新完成: orderNo={}, newStatus={}", outTradeNo, newStatus);

        // 发送支付结果通知
        try {
            paymentNotificationService.sendPaymentNotification(paymentOrder, newStatus, tradeNo);
        } catch (Exception e) {
            log.error("支付结果通知发送失败，但不影响主流程: orderNo={}", outTradeNo, e);
        }

        // 6. 触发业务逻辑更新
        try {
            triggerBusinessLogic(paymentOrder, outTradeNo, tradeNo, newStatus);
        } catch (Exception e) {
            log.error("业务逻辑处理异常", e);
            // 业务逻辑失败不影响支付宝回调响应，仍返回success避免支付宝重复通知
            // 但记录错误以便后续人工处理
        }

        return "success";
    }

    /**
     * 同步回调兜底：当支付宝已返回成功但 handleNotify 未成功更新时，直接更新订单与业务状态（不再校验签名）。
     * 仅用于 return_url 场景，且仅当 trade_status 已为 TRADE_SUCCESS/TRADE_FINISHED 时调用。
     *
     * @param outTradeNo 商户订单号
     * @param tradeNo    支付宝交易号
     * @return 是否进行了更新
     */
    @Transactional
    public boolean applyPaymentSuccessFromReturn(String outTradeNo, String tradeNo) {
        if (outTradeNo == null || (outTradeNo = outTradeNo.trim()).isEmpty() || tradeNo == null) {
            return false;
        }
        PaymentOrder paymentOrder = paymentOrderService.getByOrderNo(outTradeNo);
        if (paymentOrder == null || isFinalStatus(paymentOrder.getStatus())) {
            return false;
        }
        boolean ok = paymentOrderService.updateStatusWithAlipayInfo(outTradeNo, "PAID", outTradeNo, tradeNo);
        if (!ok) {
            return false;
        }
        paymentOrder = paymentOrderService.getByOrderNo(outTradeNo);
        if (paymentOrder != null) {
            try {
                triggerBusinessLogic(paymentOrder, outTradeNo, tradeNo, "PAID");
            } catch (Exception e) {
                log.error("同步回调兜底-业务逻辑更新异常: orderNo={}", outTradeNo, e);
            }
        }
        return true;
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

    /**
     * 将支付宝交易状态映射为系统支付订单状态
     * @param tradeStatus 支付宝交易状态
     * @return 系统支付订单状态
     */
    private String mapTradeStatus(String tradeStatus) {
        switch (tradeStatus) {
            case "TRADE_SUCCESS":
            case "TRADE_FINISHED":
                return "PAID";
            case "TRADE_CLOSED":
                return "CLOSED";
            case "WAIT_BUYER_PAY":
                return "UNPAID";
            default:
                // 其他状态如 TRADE_FAILED, TRADE_CANCELED 等视为失败
                return "FAILED";
        }
    }

    /**
     * 触发业务逻辑更新
     * @param paymentOrder 支付订单
     * @param paymentOrderNo 支付订单号（out_trade_no）
     * @param tradeNo 支付宝交易号
     * @param paymentStatus 支付状态
     */
    private void triggerBusinessLogic(PaymentOrder paymentOrder, String paymentOrderNo, String tradeNo, String paymentStatus) {
        String businessType = paymentOrder.getBusinessType();
        String businessOrderNo = paymentOrder.getBusinessOrderNo();

        log.info("触发业务逻辑更新: businessType={}, businessOrderNo={}, paymentStatus={}",
                businessType, businessOrderNo, paymentStatus);

        if ("HOUSEKEEPING".equals(businessType)) {
            updateHousekeepingOrder(businessOrderNo, paymentOrderNo, tradeNo, paymentStatus);
        } else if ("PROPERTY_FEE".equals(businessType)) {
            updatePropertyFee(businessOrderNo, paymentOrderNo, tradeNo, paymentStatus);
        } else if ("PARKING_FEE".equals(businessType)) {
            updateParkingFee(businessOrderNo, paymentOrderNo, tradeNo, paymentStatus);
        } else if ("REPAIR".equals(businessType)) {
            updateRepairRecord(businessOrderNo, paymentOrderNo, tradeNo, paymentStatus);
        } else {
            log.warn("未知业务类型: businessType={}", businessType);
        }
    }

    /**
     * 更新家政服务订单支付状态
     * @param orderNo 家政订单号（业务订单号）
     * @param paymentOrderNo 支付订单号（out_trade_no）
     * @param tradeNo 支付宝交易号
     * @param paymentStatus 支付状态
     */
    private void updateHousekeepingOrder(String orderNo, String paymentOrderNo, String tradeNo, String paymentStatus) {
        HousekeepingOrder order = housekeepingOrderMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HousekeepingOrder>()
                        .eq(HousekeepingOrder::getOrderNo, orderNo)
        );

        if (order == null) {
            log.error("家政订单不存在: orderNo={}", orderNo);
            return;
        }

        if ("PAID".equals(paymentStatus)) {
            // 避免重复更新
            if (!"PAID".equals(order.getPaymentStatus())) {
                order.setPaymentStatus("PAID");
                order.setPayType("ALIPAY");
                order.setAlipayOrderNo(paymentOrderNo);
                order.setAlipayTradeNo(tradeNo);
                order.setPayTime(LocalDateTime.now());
                order.setNotifyTime(LocalDateTime.now());
                order.setUpdatedAt(LocalDateTime.now());
                housekeepingOrderMapper.updateById(order);
                log.info("家政订单支付状态更新成功: orderNo={}, paymentOrderNo={}", orderNo, paymentOrderNo);
            } else {
                log.info("家政订单已支付，跳过重复更新: orderNo={}", orderNo);
            }
        } else if ("FAILED".equals(paymentStatus) || "CLOSED".equals(paymentStatus)) {
            // 支付失败或关闭，可记录状态
            order.setPaymentStatus("FAILED");
            order.setUpdatedAt(LocalDateTime.now());
            housekeepingOrderMapper.updateById(order);
            log.info("家政订单支付状态更新为失败: orderNo={}", orderNo);
        }
    }

    /**
     * 更新物业费记录支付状态
     * @param orderNo 物业费订单号（业务订单号）
     * @param paymentOrderNo 支付订单号（out_trade_no）
     * @param tradeNo 支付宝交易号
     * @param paymentStatus 支付状态
     */
    private void updatePropertyFee(String orderNo, String paymentOrderNo, String tradeNo, String paymentStatus) {
        PropertyFee propertyFee = null;
        // 支持 businessOrderNo 为 "ID:23" 形式，用于 order_no 为空的物业费记录
        if (orderNo != null && orderNo.startsWith("ID:")) {
            try {
                long id = Long.parseLong(orderNo.substring(3).trim());
                propertyFee = propertyFeeMapper.selectById(id);
            } catch (NumberFormatException e) {
                log.warn("物业费 businessOrderNo 格式错误，非数字 ID: {}", orderNo);
            }
        }
        if (propertyFee == null && orderNo != null && !orderNo.isEmpty()) {
            propertyFee = propertyFeeMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PropertyFee>()
                            .eq(PropertyFee::getOrderNo, orderNo)
            );
        }

        if (propertyFee == null) {
            log.error("物业费记录不存在: businessOrderNo={}", orderNo);
            return;
        }

        if ("PAID".equals(paymentStatus)) {
            if (propertyFee.getStatus() != PropertyFee.Status.PAID) {
                propertyFee.setStatus(PropertyFee.Status.PAID);
                propertyFee.setPayType("ALIPAY");
                propertyFee.setAlipayOrderNo(paymentOrderNo);
                propertyFee.setAlipayTradeNo(tradeNo);
                propertyFee.setPaymentTime(LocalDateTime.now());
                propertyFee.setNotifyTime(LocalDateTime.now());
                propertyFeeMapper.updateById(propertyFee);
                log.info("物业费支付状态更新成功: id={}, paymentOrderNo={}", propertyFee.getId(), paymentOrderNo);
            } else {
                log.info("物业费已支付，跳过重复更新: id={}", propertyFee.getId());
            }
        } else if ("FAILED".equals(paymentStatus) || "CLOSED".equals(paymentStatus)) {
            propertyFee.setStatus(PropertyFee.Status.UNPAID);
            propertyFeeMapper.updateById(propertyFee);
            log.info("物业费支付状态更新为未支付: id={}", propertyFee.getId());
        }
    }

    /**
     * 更新停车费记录支付状态
     * @param orderNo 停车费订单号（业务订单号）
     * @param paymentOrderNo 支付订单号（out_trade_no）
     * @param tradeNo 支付宝交易号
     * @param paymentStatus 支付状态
     */
    private void updateParkingFee(String orderNo, String paymentOrderNo, String tradeNo, String paymentStatus) {
        ParkingFee parkingFee = parkingFeeMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ParkingFee>()
                        .eq(ParkingFee::getOrderNo, orderNo)
        );

        if (parkingFee == null) {
            log.error("停车费记录不存在: orderNo={}", orderNo);
            return;
        }

        if ("PAID".equals(paymentStatus)) {
            if (parkingFee.getStatus() != ParkingFee.Status.PAID) {
                parkingFee.setStatus(ParkingFee.Status.PAID);
                parkingFee.setPayType("ALIPAY");
                parkingFee.setAlipayOrderNo(paymentOrderNo);
                parkingFee.setAlipayTradeNo(tradeNo);
                parkingFee.setPaymentTime(LocalDateTime.now());
                parkingFee.setNotifyTime(LocalDateTime.now());
                parkingFeeMapper.updateById(parkingFee);
                log.info("停车费支付状态更新成功: orderNo={}, paymentOrderNo={}", orderNo, paymentOrderNo);
            } else {
                log.info("停车费已支付，跳过重复更新: orderNo={}", orderNo);
            }
        } else if ("FAILED".equals(paymentStatus) || "CLOSED".equals(paymentStatus)) {
            parkingFee.setStatus(ParkingFee.Status.UNPAID);
            parkingFeeMapper.updateById(parkingFee);
            log.info("停车费支付状态更新为未支付: orderNo={}", orderNo);
        }
    }

    /**
     * 更新报修记录支付状态
     * @param orderNo 报修记录ID（业务订单号，使用 RepairRecord.id 字符串）
     * @param paymentOrderNo 支付订单号（out_trade_no）
     * @param tradeNo 支付宝交易号
     * @param paymentStatus 支付状态
     */
    private void updateRepairRecord(String orderNo, String paymentOrderNo, String tradeNo, String paymentStatus) {
        log.info("报修订单支付回调处理: orderNo={}, paymentStatus={}, paymentOrderNo={}, tradeNo={}",
                orderNo, paymentStatus, paymentOrderNo, tradeNo);

        if (orderNo == null || orderNo.isEmpty()) {
            log.error("报修订单号为空，无法更新支付状态");
            return;
        }

        Long repairId;
        try {
            repairId = Long.parseLong(orderNo.trim());
        } catch (NumberFormatException e) {
            log.error("报修业务订单号格式错误，必须为数字ID: {}", orderNo);
            return;
        }

        RepairRecord record = repairRecordMapper.selectById(repairId);
        if (record == null) {
            log.error("报修记录不存在: id={}", repairId);
            return;
        }

        // 尝试获取支付订单金额，便于同步到 feeAmount
        PaymentOrder po = paymentOrderService.getByOrderNo(paymentOrderNo);

        if ("PAID".equals(paymentStatus)) {
            if (record.getPayStatus() == RepairRecord.PayStatus.PAID) {
                log.info("报修记录已标记为已支付，跳过重复更新: id={}", repairId);
                return;
            }
            // 若尚未设置费用，使用支付订单金额作为费用
            if (record.getFeeAmount() == null && po != null && po.getAmount() != null) {
                record.setFeeAmount(po.getAmount());
            }
            record.setPayStatus(RepairRecord.PayStatus.PAID);
            repairRecordMapper.updateById(record);
            log.info("报修记录支付状态更新为已支付: id={}, amount={}", repairId, record.getFeeAmount());
        } else if ("FAILED".equals(paymentStatus) || "CLOSED".equals(paymentStatus)) {
            // 支付失败或关闭，维持/回退为未支付
            record.setPayStatus(RepairRecord.PayStatus.UNPAID);
            repairRecordMapper.updateById(record);
            log.info("报修记录支付状态更新为未支付: id={}", repairId);
        }
    }
}