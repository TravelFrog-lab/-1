-- 在 wanda_property 库中执行此脚本，创建支付订单表（若 500 报错 payment_order 不存在时使用）
USE wanda_property;

CREATE TABLE IF NOT EXISTS `payment_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付订单ID',
  `order_no` varchar(64) NOT NULL COMMENT '支付订单号，唯一',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '订单金额',
  `status` varchar(20) NOT NULL COMMENT '支付状态: UNPAID-未支付, PAID-已支付, FAILED-支付失败, REFUNDED-已退款, CLOSED-已关闭',
  `pay_type` varchar(20) NOT NULL COMMENT '支付方式: ALIPAY-支付宝, WECHAT-微信',
  `business_type` varchar(50) NOT NULL COMMENT '业务类型',
  `business_order_no` varchar(64) DEFAULT NULL COMMENT '业务订单号',
  `alipay_order_no` varchar(64) DEFAULT NULL COMMENT '支付宝商户订单号',
  `alipay_trade_no` varchar(64) DEFAULT NULL COMMENT '支付宝交易号',
  `subject` varchar(256) NOT NULL COMMENT '订单标题',
  `return_url` varchar(512) DEFAULT NULL COMMENT '支付返回URL',
  `notify_url` varchar(512) DEFAULT NULL COMMENT '异步通知URL',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_business` (`business_type`,`business_order_no`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付订单';
