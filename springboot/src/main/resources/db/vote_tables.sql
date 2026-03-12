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
