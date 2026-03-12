-- 为用户表增加头像字段（执行一次即可；若已存在会报 Duplicate column，可忽略）
ALTER TABLE `user` ADD COLUMN `avatar` VARCHAR(512) DEFAULT NULL COMMENT '头像路径' AFTER `phone`;
