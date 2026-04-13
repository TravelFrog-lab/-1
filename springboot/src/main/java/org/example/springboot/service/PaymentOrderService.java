package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.entity.PaymentOrder;
import org.example.springboot.mapper.PaymentOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付订单服务类
 * 封装支付订单的CRUD操作，确保事务一致性
 */
@Slf4j
@Service
public class PaymentOrderService extends ServiceImpl<PaymentOrderMapper, PaymentOrder> {

    @Autowired
    private PaymentOrderMapper paymentOrderMapper;

    /**
     * 创建支付订单
     *
     * @param paymentOrder 支付订单实体
     * @return 创建的支付订单
     */
    @Transactional
    public PaymentOrder createPaymentOrder(PaymentOrder paymentOrder) {
        paymentOrder.setCreateTime(LocalDateTime.now());
        paymentOrder.setUpdateTime(LocalDateTime.now());
        paymentOrderMapper.insert(paymentOrder);
        log.info("支付订单创建成功: orderNo={}, amount={}, businessType={}",
                paymentOrder.getOrderNo(), paymentOrder.getAmount(), paymentOrder.getBusinessType());
        return paymentOrder;
    }

    /**
     * 根据订单号查询支付订单
     *
     * @param orderNo 支付订单号
     * @return 支付订单，不存在则返回null
     */
    public PaymentOrder getByOrderNo(String orderNo) {
        return paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getOrderNo, orderNo)
        );
    }

    /**
     * 根据业务类型和业务订单号查询支付订单
     *
     * @param businessType 业务类型
     * @param businessOrderNo 业务订单号
     * @return 支付订单列表
     */
    public List<PaymentOrder> getByBusinessOrder(String businessType, String businessOrderNo) {
        return paymentOrderMapper.selectList(
                new LambdaQueryWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getBusinessType, businessType)
                        .eq(PaymentOrder::getBusinessOrderNo, businessOrderNo)
        );
    }

    /**
     * 根据用户ID查询支付订单
     *
     * @param userId 用户ID
     * @return 支付订单列表
     */
    public List<PaymentOrder> getByUserId(Long userId) {
        return paymentOrderMapper.selectList(
                new LambdaQueryWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getUserId, userId)
                        .orderByDesc(PaymentOrder::getCreateTime)
        );
    }

    /**
     * 更新支付订单状态
     *
     * @param orderNo 支付订单号
     * @param newStatus 新状态
     * @param alipayTradeNo 支付宝交易号（可选）
     * @return 更新是否成功
     */
    @Transactional
    public boolean updateStatus(String orderNo, String newStatus, String alipayTradeNo) {
        PaymentOrder paymentOrder = getByOrderNo(orderNo);
        if (paymentOrder == null) {
            log.error("支付订单不存在，无法更新状态: orderNo={}", orderNo);
            return false;
        }

        paymentOrder.setStatus(newStatus);
        paymentOrder.setUpdateTime(LocalDateTime.now());
        if (alipayTradeNo != null && !alipayTradeNo.isEmpty()) {
            paymentOrder.setAlipayTradeNo(alipayTradeNo);
        }
        if ("PAID".equals(newStatus)) {
            paymentOrder.setPayTime(LocalDateTime.now());
        }

        int result = paymentOrderMapper.updateById(paymentOrder);
        boolean success = result > 0;
        if (success) {
            log.info("支付订单状态更新成功: orderNo={}, newStatus={}, alipayTradeNo={}",
                    orderNo, newStatus, alipayTradeNo);
        } else {
            log.error("支付订单状态更新失败: orderNo={}", orderNo);
        }
        return success;
    }

    /**
     * 更新支付订单状态并记录支付宝信息
     *
     * @param orderNo 支付订单号
     * @param newStatus 新状态
     * @param alipayOrderNo 支付宝商户订单号
     * @param alipayTradeNo 支付宝交易号
     * @return 更新是否成功
     */
    @Transactional
    public boolean updateStatusWithAlipayInfo(String orderNo, String newStatus,
                                              String alipayOrderNo, String alipayTradeNo) {
        PaymentOrder paymentOrder = getByOrderNo(orderNo);
        if (paymentOrder == null) {
            log.error("支付订单不存在，无法更新状态: orderNo={}", orderNo);
            return false;
        }

        paymentOrder.setStatus(newStatus);
        paymentOrder.setAlipayOrderNo(alipayOrderNo);
        paymentOrder.setAlipayTradeNo(alipayTradeNo);
        paymentOrder.setUpdateTime(LocalDateTime.now());
        if ("PAID".equals(newStatus)) {
            paymentOrder.setPayTime(LocalDateTime.now());
        }

        int result = paymentOrderMapper.updateById(paymentOrder);
        boolean success = result > 0;
        if (success) {
            log.info("支付订单状态及支付宝信息更新成功: orderNo={}, newStatus={}, alipayTradeNo={}",
                    orderNo, newStatus, alipayTradeNo);
        } else {
            log.error("支付订单状态更新失败: orderNo={}", orderNo);
        }
        return success;
    }

    /**
     * 删除支付订单（逻辑删除或物理删除）
     * 注意：谨慎使用，通常只用于测试或管理后台
     *
     * @param orderNo 支付订单号
     * @return 删除是否成功
     */
    @Transactional
    public boolean deleteByOrderNo(String orderNo) {
        PaymentOrder paymentOrder = getByOrderNo(orderNo);
        if (paymentOrder == null) {
            log.warn("支付订单不存在，无需删除: orderNo={}", orderNo);
            return false;
        }

        int result = paymentOrderMapper.deleteById(paymentOrder.getId());
        boolean success = result > 0;
        if (success) {
            log.info("支付订单删除成功: orderNo={}", orderNo);
        } else {
            log.error("支付订单删除失败: orderNo={}", orderNo);
        }
        return success;
    }

    /**
     * 检查订单是否处于最终状态
     *
     * @param orderNo 支付订单号
     * @return true: 最终状态，false: 非最终状态或订单不存在
     */
    public boolean isFinalStatus(String orderNo) {
        PaymentOrder paymentOrder = getByOrderNo(orderNo);
        if (paymentOrder == null) {
            return false;
        }
        return isFinalStatusInternal(paymentOrder.getStatus());
    }

    /**
     * 判断订单状态是否为最终状态
     *
     * @param status 订单状态
     * @return true: 最终状态，false: 非最终状态
     */
    private boolean isFinalStatusInternal(String status) {
        return "PAID".equals(status) || "FAILED".equals(status) ||
                "CLOSED".equals(status) || "REFUNDED".equals(status);
    }

    /**
     * 统计用户的总支付金额
     *
     * @param userId 用户ID
     * @return 总支付金额
     */
    public BigDecimal getTotalPaidAmountByUserId(Long userId) {
        List<PaymentOrder> orders = paymentOrderMapper.selectList(
                new LambdaQueryWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getUserId, userId)
                        .eq(PaymentOrder::getStatus, "PAID")
        );

        return orders.stream()
                .map(PaymentOrder::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}