-- 支付订单表
CREATE TABLE IF NOT EXISTS `payment_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付订单ID',
  `order_no` varchar(64) NOT NULL COMMENT '支付订单号，唯一',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '订单金额',
  `status` varchar(20) NOT NULL COMMENT '支付状态: UNPAID-未支付, PAID-已支付, FAILED-支付失败, REFUNDED-已退款, CLOSED-已关闭',
  `pay_type` varchar(20) NOT NULL COMMENT '支付方式: ALIPAY-支付宝, WECHAT-微信',
  `business_type` varchar(50) NOT NULL COMMENT '业务类型: HOUSEKEEPING-家政服务, PROPERTY_FEE-物业费, PARKING_FEE-停车费, REPAIR-报修',
  `business_order_no` varchar(64) COMMENT '业务订单号（如家政订单号HK123、物业费订单号WY456）',
  `alipay_order_no` varchar(64) COMMENT '支付宝商户订单号（out_trade_no）',
  `alipay_trade_no` varchar(64) COMMENT '支付宝交易号（trade_no）',
  `subject` varchar(256) NOT NULL COMMENT '订单标题',
  `return_url` varchar(512) COMMENT '支付返回URL（return_url）',
  `notify_url` varchar(512) COMMENT '异步通知URL（notify_url）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `pay_time` datetime COMMENT '支付时间',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_business` (`business_type`, `business_order_no`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付订单';

-- 投票表
CREATE TABLE IF NOT EXISTS `vote` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(200) NOT NULL COMMENT '投票主题',
  `description` text COMMENT '详细描述',
  `vote_type` varchar(20) NOT NULL COMMENT 'SINGLE-单选,MULTIPLE-多选',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `eligibility` varchar(20) NOT NULL DEFAULT 'OWNER' COMMENT 'OWNER-仅业主,ALL-全部',
  `eligibility_building_ids` varchar(500) DEFAULT NULL COMMENT '指定楼栋ID列表JSON',
  `creator_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `result_after_end_only` tinyint(1) DEFAULT '0' COMMENT '是否截止后才显示结果:0-否,1-是',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_vote_time` (`start_time`,`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投票/表决';

-- 投票选项表
CREATE TABLE IF NOT EXISTS `vote_option` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `vote_id` bigint NOT NULL COMMENT '投票ID',
  `option_text` varchar(500) NOT NULL COMMENT '选项文案',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_vote_id` (`vote_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投票选项';

-- 投票记录表
CREATE TABLE IF NOT EXISTS `vote_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `vote_id` bigint NOT NULL COMMENT '投票ID',
  `option_id` bigint NOT NULL COMMENT '选项ID',
  `user_id` bigint NOT NULL COMMENT '投票人用户ID',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vote_user_option` (`vote_id`,`user_id`,`option_id`),
  KEY `idx_vote_id` (`vote_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投票记录';