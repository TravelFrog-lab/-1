-- 家政人员表
CREATE TABLE `housekeeper` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `phone` varchar(20) NOT NULL COMMENT '联系电话',
  `id_card` varchar(18) NOT NULL COMMENT '身份证号',
  `status` enum('ACTIVE','DISABLED') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `description` text COMMENT '个人描述',
  `work_years` int NOT NULL DEFAULT 0 COMMENT '工作年限',
  `rating` decimal(2,1) DEFAULT 5.0 COMMENT '评分',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_housekeeper_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政人员表';


-- 家政服务项目表
CREATE TABLE `housekeeping_service` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '服务名称',
  `description` text COMMENT '服务描述',
  `base_price` decimal(10,2) NOT NULL COMMENT '基础价格',
  `unit` varchar(20) NOT NULL COMMENT '计价单位(小时/次/平米)',
  `category` varchar(50) NOT NULL COMMENT '服务类别',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政服务项目表';

-- 家政人员服务项目关联表
CREATE TABLE `housekeeper_service` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `housekeeper_id` bigint NOT NULL COMMENT '家政人员ID',
  `service_id` bigint NOT NULL COMMENT '服务项目ID',
  `price` decimal(10,2) NOT NULL COMMENT '服务价格',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_housekeeper_id` (`housekeeper_id`),
  KEY `idx_service_id` (`service_id`),
  CONSTRAINT `fk_housekeeper_service_housekeeper` FOREIGN KEY (`housekeeper_id`) REFERENCES `housekeeper` (`id`),
  CONSTRAINT `fk_housekeeper_service_service` FOREIGN KEY (`service_id`) REFERENCES `housekeeping_service` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政人员服务项目关联表';

-- 家政服务订单表
CREATE TABLE `housekeeping_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL COMMENT '订单编号',
  `owner_id` bigint NOT NULL COMMENT '业主ID',
  `housekeeper_id` bigint NOT NULL COMMENT '家政人员ID',
  `service_id` bigint NOT NULL COMMENT '服务项目ID',
  `house_id` bigint NOT NULL COMMENT '服务地址(房屋)ID',
  `appointment_time` datetime NOT NULL COMMENT '预约时间',
  `service_duration` int DEFAULT NULL COMMENT '服务时长(分钟)',
  `amount` decimal(10,2) NOT NULL COMMENT '订单金额',
  `status` enum('PENDING','CONFIRMED','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING' COMMENT '订单状态',
  `payment_status` enum('UNPAID','PAID','REFUNDED') NOT NULL DEFAULT 'UNPAID' COMMENT '支付状态',
  `remark` text COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_housekeeper_id` (`housekeeper_id`),
  KEY `idx_service_id` (`service_id`),
  KEY `idx_house_id` (`house_id`),
  CONSTRAINT `fk_housekeeping_order_owner` FOREIGN KEY (`owner_id`) REFERENCES `owner` (`id`),
  CONSTRAINT `fk_housekeeping_order_housekeeper` FOREIGN KEY (`housekeeper_id`) REFERENCES `housekeeper` (`id`),
  CONSTRAINT `fk_housekeeping_order_service` FOREIGN KEY (`service_id`) REFERENCES `housekeeping_service` (`id`),
  CONSTRAINT `fk_housekeeping_order_house` FOREIGN KEY (`house_id`) REFERENCES `house` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家政服务订单表'; 