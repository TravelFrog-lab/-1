-- 添加家政人员用户数据
INSERT INTO `user` (`id`, `username`, `name`, `phone`, `password`, `role`, `sex`, `created_at`, `updated_at`, `status`) VALUES
(8, 'keeper1', '李阿姨', '13800138001', '$2a$10$3JgH9Nw.ZA3ZYoaKP1jU8.qP.dBK5hFNEZOo7252PwRYzoQBuRZFO', 'KEEPER', 'FEMALE', NOW(), NOW(), 'ENABLED'),
(9, 'keeper2', '王师傅', '13800138002', '$2a$10$3JgH9Nw.ZA3ZYoaKP1jU8.qP.dBK5hFNEZOo7252PwRYzoQBuRZFO', 'KEEPER', 'MALE', NOW(), NOW(), 'ENABLED'),
(10, 'keeper3', '张阿姨', '13800138003', '$2a$10$3JgH9Nw.ZA3ZYoaKP1jU8.qP.dBK5hFNEZOo7252PwRYzoQBuRZFO', 'KEEPER', 'FEMALE', NOW(), NOW(), 'ENABLED'),
(11, 'keeper4', '赵师傅', '13800138004', '$2a$10$3JgH9Nw.ZA3ZYoaKP1jU8.qP.dBK5hFNEZOo7252PwRYzoQBuRZFO', 'KEEPER', 'MALE', NOW(), NOW(), 'ENABLED'),
(12, 'keeper5', '刘阿姨', '13800138005', '$2a$10$3JgH9Nw.ZA3ZYoaKP1jU8.qP.dBK5hFNEZOo7252PwRYzoQBuRZFO', 'KEEPER', 'FEMALE', NOW(), NOW(), 'DISABLED');

-- 添加家政人员信息
INSERT INTO `housekeeper` (`id`, `user_id`, `name`, `phone`, `id_card`, `status`, `description`, `work_years`, `rating`, `created_at`, `updated_at`) VALUES
(1, 8, '李阿姨', '13800138001', '110101196001011234', 'ACTIVE', '从事家政工作15年，擅长家居清洁、烹饪和老人照顾，有丰富的经验和耐心。', 15, 4.8, NOW(), NOW()),
(2, 9, '王师傅', '13800138002', '110101197001011235', 'ACTIVE', '专业水电维修10年经验，擅长各类家电维修和安装，服务认真负责。', 10, 4.9, NOW(), NOW()),
(3, 10, '张阿姨', '13800138003', '110101196501011236', 'ACTIVE', '擅长照顾婴幼儿和老人，有护理证书，性格温和有耐心，做事细心。', 8, 4.7, NOW(), NOW()),
(4, 11, '赵师傅', '13800138004', '110101198001011237', 'ACTIVE', '专业保洁5年经验，擅长深度清洁和除螨服务，使用环保清洁产品。', 5, 4.5, NOW(), NOW()),
(5, 12, '刘阿姨', '13800138005', '110101197501011238', 'DISABLED', '擅长中式烹饪，曾在高档餐厅工作，可提供上门做饭和家宴服务。', 12, 4.6, NOW(), NOW());

-- 添加家政服务项目
INSERT INTO `housekeeping_service` (`id`, `name`, `description`, `base_price`, `unit`, `category`, `created_at`, `updated_at`) VALUES
(1, '日常保洁', '包括房间清扫、擦拭家具、清洁厨卫等基础保洁服务', 80.00, '小时', '清洁服务', NOW(), NOW()),
(2, '深度保洁', '包括家电清洗、玻璃清洁、地板打蜡等深度清洁服务', 150.00, '次', '清洁服务', NOW(), NOW()),
(3, '老人照护', '为老人提供生活照料、陪伴聊天、协助吃药等服务', 100.00, '小时', '照护服务', NOW(), NOW()),
(4, '育儿嫂服务', '为婴幼儿提供照料、喂养、早教等专业服务', 120.00, '小时', '照护服务', NOW(), NOW()),
(5, '上门做饭', '根据客户需求提供上门采购食材、烹饪服务', 100.00, '次', '烹饪服务', NOW(), NOW());

-- 添加家政人员服务项目关联
INSERT INTO `housekeeper_service` (`id`, `housekeeper_id`, `service_id`, `price`, `created_at`, `updated_at`) VALUES
(1, 1, 1, 85.00, NOW(), NOW()),
(2, 1, 2, 160.00, NOW(), NOW()),
(3, 1, 3, 110.00, NOW(), NOW()),
(4, 2, 1, 90.00, NOW(), NOW()),
(5, 3, 3, 105.00, NOW(), NOW()),
(6, 3, 4, 130.00, NOW(), NOW()),
(7, 4, 1, 80.00, NOW(), NOW()),
(8, 4, 2, 150.00, NOW(), NOW()),
(9, 5, 5, 120.00, NOW(), NOW()); 