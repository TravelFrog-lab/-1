/*
 Navicat Premium Data Transfer

 Source Server         : ftfx
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : wanda_property

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 08/05/2025 09:24:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_dict (移至 dict_item 之前)
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dict_type_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典编码',
  `dict_type_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_type_code`(`dict_type_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统字典表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for announcement
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告内容',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `type` enum('REPAIR','ACTIVITY','PAY') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'ACTIVITY' COMMENT '公告类型',
  `publisher` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发布人信息',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for carousel
-- ----------------------------
DROP TABLE IF EXISTS `carousel`;
CREATE TABLE `carousel`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `image_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `tag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '轮播图表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for complaint
-- ----------------------------
DROP TABLE IF EXISTS `complaint`;
CREATE TABLE `complaint`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `complainant_id` bigint NOT NULL COMMENT '投诉人ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '投诉内容',
  `status` enum('PENDING','PROCESSING','RESOLVED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '处理状态',
  `result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '处理结果',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '处理人员ID',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `evaluation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '处理结果评价',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_complainant_id`(`complainant_id` ASC) USING BTREE,
  INDEX `idx_handler_id`(`handler_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '投诉记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dict_item
-- ----------------------------
DROP TABLE IF EXISTS `dict_item`;
CREATE TABLE `dict_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dict_type_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联的字典类型code',
  `item_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典项的键',
  `item_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典项的值',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '字典项的描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `item_key`(`item_key` ASC) USING BTREE,
  INDEX `dict_type_code`(`dict_type_code` ASC) USING BTREE,
  CONSTRAINT `dict_item_ibfk_1` FOREIGN KEY (`dict_type_code`) REFERENCES `sys_dict` (`dict_type_code`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 283 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统字典项表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dict_item
-- ----------------------------
INSERT INTO `dict_item` VALUES (1, 'icon', 'user', 'el-icon-user', NULL, '2024-07-30 23:06:45', '2025-02-09 10:55:05');
INSERT INTO `dict_item` VALUES (2, 'icon', 'house', 'el-icon-house', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (3, 'icon', 'menu', 'el-icon-menu', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (4, 'icon', 's-custom', 'el-icon-s-custom', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (5, 'icon', 's-grid', 'el-icon-s-grid', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (6, 'icon', 'document', 'el-icon-document', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (7, 'icon', 'coffee', 'el-icon-coffee', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (8, 'icon', 's-marketing', 'el-icon-s-marketing', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (9, 'icon', 'phone-outline', 'el-icon-phone-outline', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (10, 'icon', 'platform-eleme', 'el-icon-platform-eleme', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (11, 'icon', 'eleme', 'el-icon-eleme', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (12, 'icon', 'delete-solid', 'el-icon-delete-solid', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (13, 'icon', 'delete', 'el-icon-delete', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (14, 'icon', 's-tools', 'el-icon-s-tools', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (15, 'icon', 'setting', 'el-icon-setting', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (16, 'icon', 'user-solid', 'el-icon-user-solid', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (17, 'icon', 'phone', 'el-icon-phone', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (18, 'icon', 'more', 'el-icon-more', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (19, 'icon', 'more-outline', 'el-icon-more-outline', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (20, 'icon', 'star-on', 'el-icon-star-on', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (21, 'icon', 'star-off', 'el-icon-star-off', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (22, 'icon', 's-goods', 'el-icon-s-goods', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (23, 'icon', 'goods', 'el-icon-goods', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (24, 'icon', 'warning', 'el-icon-warning', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (25, 'icon', 'warning-outline', 'el-icon-warning-outline', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (26, 'icon', 'question', 'el-icon-question', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (27, 'icon', 'info', 'el-icon-info', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (28, 'icon', 'remove', 'el-icon-remove', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (29, 'icon', 'circle-plus', 'el-icon-circle-plus', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (30, 'icon', 'success', 'el-icon-success', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (31, 'icon', 'error', 'el-icon-error', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (32, 'icon', 'zoom-in', 'el-icon-zoom-in', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (33, 'icon', 'zoom-out', 'el-icon-zoom-out', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (34, 'icon', 'remove-outline', 'el-icon-remove-outline', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (35, 'icon', 'circle-plus-outline', 'el-icon-circle-plus-outline', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (36, 'icon', 'circle-check', 'el-icon-circle-check', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (37, 'icon', 'circle-close', 'el-icon-circle-close', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (38, 'icon', 's-help', 'el-icon-s-help', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (39, 'icon', 'help', 'el-icon-help', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (40, 'icon', 'minus', 'el-icon-minus', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (41, 'icon', 'plus', 'el-icon-plus', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (42, 'icon', 'check', 'el-icon-check', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (43, 'icon', 'close', 'el-icon-close', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (44, 'icon', 'picture', 'el-icon-picture', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (45, 'icon', 'picture-outline', 'el-icon-picture-outline', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (46, 'icon', 'picture-outline-round', 'el-icon-picture-outline-round', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (47, 'icon', 'upload', 'el-icon-upload', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (48, 'icon', 'upload2', 'el-icon-upload2', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (49, 'icon', 'download', 'el-icon-download', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (50, 'icon', 'camera-solid', 'el-icon-camera-solid', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (51, 'icon', 'camera', 'el-icon-camera', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (52, 'icon', 'video-camera-solid', 'el-icon-video-camera-solid', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (53, 'icon', 'video-camera', 'el-icon-video-camera', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (54, 'icon', 'message-solid', 'el-icon-message-solid', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (55, 'icon', 'bell', 'el-icon-bell', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (56, 'icon', 's-cooperation', 'el-icon-s-cooperation', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (57, 'icon', 's-order', 'el-icon-s-order', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (58, 'icon', 's-platform', 'el-icon-s-platform', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (59, 'icon', 's-fold', 'el-icon-s-fold', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (60, 'icon', 's-unfold', 'el-icon-s-unfold', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (61, 'icon', 's-operation', 'el-icon-s-operation', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (62, 'icon', 's-promotion', 'el-icon-s-promotion', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (63, 'icon', 's-home', 'el-icon-s-home', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (64, 'icon', 's-release', 'el-icon-s-release', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (65, 'icon', 's-ticket', 'el-icon-s-ticket', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (66, 'icon', 's-management', 'el-icon-s-management', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (67, 'icon', 's-open', 'el-icon-s-open', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (68, 'icon', 's-shop', 'el-icon-s-shop', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (69, 'icon', 's-flag', 'el-icon-s-flag', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (70, 'icon', 's-comment', 'el-icon-s-comment', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (71, 'icon', 's-finance', 'el-icon-s-finance', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (72, 'icon', 's-claim', 'el-icon-s-claim', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (73, 'icon', 's-opportunity', 'el-icon-s-opportunity', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (74, 'icon', 's-data', 'el-icon-s-data', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (75, 'icon', 's-check', 'el-icon-s-check', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (76, 'icon', 'share', 'el-icon-share', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (77, 'icon', 'd-caret', 'el-icon-d-caret', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (78, 'icon', 'caret-left', 'el-icon-caret-left', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (79, 'icon', 'caret-right', 'el-icon-caret-right', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (80, 'icon', 'caret-bottom', 'el-icon-caret-bottom', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (81, 'icon', 'caret-top', 'el-icon-caret-top', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (82, 'icon', 'bottom-left', 'el-icon-bottom-left', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (83, 'icon', 'bottom-right', 'el-icon-bottom-right', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (84, 'icon', 'back', 'el-icon-back', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (85, 'icon', 'right', 'el-icon-right', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (86, 'icon', 'bottom', 'el-icon-bottom', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (87, 'icon', 'top', 'el-icon-top', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (88, 'icon', 'top-left', 'el-icon-top-left', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (89, 'icon', 'top-right', 'el-icon-top-right', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (90, 'icon', 'arrow-left', 'el-icon-arrow-left', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (91, 'icon', 'arrow-right', 'el-icon-arrow-right', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (92, 'icon', 'arrow-down', 'el-icon-arrow-down', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (93, 'icon', 'arrow-up', 'el-icon-arrow-up', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (94, 'icon', 'd-arrow-left', 'el-icon-d-arrow-left', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (95, 'icon', 'd-arrow-right', 'el-icon-d-arrow-right', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (96, 'icon', 'video-pause', 'el-icon-video-pause', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (97, 'icon', 'video-play', 'el-icon-video-play', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (98, 'icon', 'refresh', 'el-icon-refresh', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (99, 'icon', 'refresh-right', 'el-icon-refresh-right', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (100, 'icon', 'refresh-left', 'el-icon-refresh-left', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (101, 'icon', 'finished', 'el-icon-finished', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (102, 'icon', 'sort', 'el-icon-sort', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (103, 'icon', 'sort-up', 'el-icon-sort-up', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (104, 'icon', 'sort-down', 'el-icon-sort-down', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (105, 'icon', 'rank', 'el-icon-rank', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (106, 'icon', 'loading', 'el-icon-loading', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (107, 'icon', 'view', 'el-icon-view', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (108, 'icon', 'c-scale-to-original', 'el-icon-c-scale-to-original', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (109, 'icon', 'date', 'el-icon-date', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (110, 'icon', 'edit', 'el-icon-edit', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (111, 'icon', 'edit-outline', 'el-icon-edit-outline', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (112, 'icon', 'folder', 'el-icon-folder', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (113, 'icon', 'folder-opened', 'el-icon-folder-opened', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (114, 'icon', 'folder-add', 'el-icon-folder-add', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (115, 'icon', 'folder-remove', 'el-icon-folder-remove', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (116, 'icon', 'folder-delete', 'el-icon-folder-delete', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (117, 'icon', 'folder-checked', 'el-icon-folder-checked', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (118, 'icon', 'tickets', 'el-icon-tickets', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (119, 'icon', 'document-remove', 'el-icon-document-remove', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (120, 'icon', 'document-delete', 'el-icon-document-delete', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (121, 'icon', 'document-copy', 'el-icon-document-copy', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (122, 'icon', 'document-checked', 'el-icon-document-checked', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (123, 'icon', 'document-add', 'el-icon-document-add', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (124, 'icon', 'printer', 'el-icon-printer', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (125, 'icon', 'paperclip', 'el-icon-paperclip', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (126, 'icon', 'takeaway-box', 'el-icon-takeaway-box', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (127, 'icon', 'search', 'el-icon-search', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (128, 'icon', 'monitor', 'el-icon-monitor', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (129, 'icon', 'attract', 'el-icon-attract', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (130, 'icon', 'mobile', 'el-icon-mobile', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (131, 'icon', 'scissors', 'el-icon-scissors', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (132, 'icon', 'umbrella', 'el-icon-umbrella', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (133, 'icon', 'headset', 'el-icon-headset', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (134, 'icon', 'brush', 'el-icon-brush', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (135, 'icon', 'mouse', 'el-icon-mouse', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (136, 'icon', 'coordinate', 'el-icon-coordinate', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (137, 'icon', 'magic-stick', 'el-icon-magic-stick', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (138, 'icon', 'reading', 'el-icon-reading', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (139, 'icon', 'data-line', 'el-icon-data-line', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (140, 'icon', 'data-board', 'el-icon-data-board', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (141, 'icon', 'pie-chart', 'el-icon-pie-chart', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (142, 'icon', 'data-analysis', 'el-icon-data-analysis', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (143, 'icon', 'collection-tag', 'el-icon-collection-tag', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (144, 'icon', 'film', 'el-icon-film', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (145, 'icon', 'suitcase', 'el-icon-suitcase', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (146, 'icon', 'suitcase-1', 'el-icon-suitcase-1', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (147, 'icon', 'receiving', 'el-icon-receiving', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (148, 'icon', 'collection', 'el-icon-collection', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (149, 'icon', 'files', 'el-icon-files', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (150, 'icon', 'notebook-1', 'el-icon-notebook-1', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (151, 'icon', 'notebook-2', 'el-icon-notebook-2', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (152, 'icon', 'toilet-paper', 'el-icon-toilet-paper', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (153, 'icon', 'office-building', 'el-icon-office-building', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (154, 'icon', 'school', 'el-icon-school', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (155, 'icon', 'table-lamp', 'el-icon-table-lamp', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (156, 'icon', 'no-smoking', 'el-icon-no-smoking', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (157, 'icon', 'smoking', 'el-icon-smoking', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (158, 'icon', 'shopping-cart-full', 'el-icon-shopping-cart-full', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (159, 'icon', 'shopping-cart-1', 'el-icon-shopping-cart-1', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (160, 'icon', 'shopping-cart-2', 'el-icon-shopping-cart-2', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (161, 'icon', 'shopping-bag-1', 'el-icon-shopping-bag-1', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (162, 'icon', 'shopping-bag-2', 'el-icon-shopping-bag-2', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (163, 'icon', 'sold-out', 'el-icon-sold-out', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (164, 'icon', 'sell', 'el-icon-sell', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (165, 'icon', 'present', 'el-icon-present', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (166, 'icon', 'box', 'el-icon-box', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (167, 'icon', 'bank-card', 'el-icon-bank-card', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (168, 'icon', 'money', 'el-icon-money', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (169, 'icon', 'coin', 'el-icon-coin', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (170, 'icon', 'wallet', 'el-icon-wallet', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (171, 'icon', 'discount', 'el-icon-discount', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (172, 'icon', 'price-tag', 'el-icon-price-tag', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (173, 'icon', 'news', 'el-icon-news', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (174, 'icon', 'guide', 'el-icon-guide', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (175, 'icon', 'male', 'el-icon-male', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (176, 'icon', 'female', 'el-icon-female', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (177, 'icon', 'thumb', 'el-icon-thumb', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (178, 'icon', 'cpu', 'el-icon-cpu', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (179, 'icon', 'link', 'el-icon-link', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (180, 'icon', 'connection', 'el-icon-connection', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (181, 'icon', 'open', 'el-icon-open', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (182, 'icon', 'turn-off', 'el-icon-turn-off', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (183, 'icon', 'set-up', 'el-icon-set-up', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (184, 'icon', 'chat-round', 'el-icon-chat-round', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (185, 'icon', 'chat-line-round', 'el-icon-chat-line-round', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (186, 'icon', 'chat-square', 'el-icon-chat-square', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (187, 'icon', 'chat-dot-round', 'el-icon-chat-dot-round', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (188, 'icon', 'chat-dot-square', 'el-icon-chat-dot-square', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (189, 'icon', 'chat-line-square', 'el-icon-chat-line-square', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (190, 'icon', 'message', 'el-icon-message', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (191, 'icon', 'postcard', 'el-icon-postcard', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (192, 'icon', 'position', 'el-icon-position', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (193, 'icon', 'turn-off-microphone', 'el-icon-turn-off-microphone', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (194, 'icon', 'microphone', 'el-icon-microphone', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (195, 'icon', 'close-notification', 'el-icon-close-notification', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (196, 'icon', 'bangzhu', 'el-icon-bangzhu', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (197, 'icon', 'time', 'el-icon-time', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (198, 'icon', 'odometer', 'el-icon-odometer', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (199, 'icon', 'crop', 'el-icon-crop', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (200, 'icon', 'aim', 'el-icon-aim', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (201, 'icon', 'switch-button', 'el-icon-switch-button', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (202, 'icon', 'full-screen', 'el-icon-full-screen', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (203, 'icon', 'copy-document', 'el-icon-copy-document', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (204, 'icon', 'mic', 'el-icon-mic', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (205, 'icon', 'stopwatch', 'el-icon-stopwatch', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (206, 'icon', 'medal-1', 'el-icon-medal-1', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (207, 'icon', 'medal', 'el-icon-medal', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (208, 'icon', 'trophy', 'el-icon-trophy', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (209, 'icon', 'trophy-1', 'el-icon-trophy-1', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (210, 'icon', 'first-aid-kit', 'el-icon-first-aid-kit', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (211, 'icon', 'discover', 'el-icon-discover', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (212, 'icon', 'place', 'el-icon-place', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (213, 'icon', 'location', 'el-icon-location', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (214, 'icon', 'location-outline', 'el-icon-location-outline', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (215, 'icon', 'location-information', 'el-icon-location-information', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (216, 'icon', 'add-location', 'el-icon-add-location', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (217, 'icon', 'delete-location', 'el-icon-delete-location', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (218, 'icon', 'map-location', 'el-icon-map-location', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (219, 'icon', 'alarm-clock', 'el-icon-alarm-clock', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (220, 'icon', 'timer', 'el-icon-timer', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (221, 'icon', 'watch-1', 'el-icon-watch-1', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (222, 'icon', 'watch', 'el-icon-watch', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (223, 'icon', 'lock', 'el-icon-lock', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (224, 'icon', 'unlock', 'el-icon-unlock', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (225, 'icon', 'key', 'el-icon-key', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (226, 'icon', 'service', 'el-icon-service', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (227, 'icon', 'mobile-phone', 'el-icon-mobile-phone', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (228, 'icon', 'bicycle', 'el-icon-bicycle', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (229, 'icon', 'truck', 'el-icon-truck', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (230, 'icon', 'ship', 'el-icon-ship', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (231, 'icon', 'basketball', 'el-icon-basketball', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (232, 'icon', 'football', 'el-icon-football', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (233, 'icon', 'soccer', 'el-icon-soccer', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (234, 'icon', 'baseball', 'el-icon-baseball', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (235, 'icon', 'wind-power', 'el-icon-wind-power', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (236, 'icon', 'light-rain', 'el-icon-light-rain', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (237, 'icon', 'lightning', 'el-icon-lightning', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (238, 'icon', 'heavy-rain', 'el-icon-heavy-rain', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (239, 'icon', 'sunrise', 'el-icon-sunrise', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (240, 'icon', 'sunrise-1', 'el-icon-sunrise-1', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (241, 'icon', 'sunset', 'el-icon-sunset', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (242, 'icon', 'sunny', 'el-icon-sunny', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (243, 'icon', 'cloudy', 'el-icon-cloudy', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (244, 'icon', 'partly-cloudy', 'el-icon-partly-cloudy', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (245, 'icon', 'cloudy-and-sunny', 'el-icon-cloudy-and-sunny', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (246, 'icon', 'moon', 'el-icon-moon', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (247, 'icon', 'moon-night', 'el-icon-moon-night', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (248, 'icon', 'dish', 'el-icon-dish', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (249, 'icon', 'dish-1', 'el-icon-dish-1', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (250, 'icon', 'food', 'el-icon-food', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (251, 'icon', 'chicken', 'el-icon-chicken', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (252, 'icon', 'fork-spoon', 'el-icon-fork-spoon', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (253, 'icon', 'knife-fork', 'el-icon-knife-fork', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (254, 'icon', 'burger', 'el-icon-burger', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (255, 'icon', 'tableware', 'el-icon-tableware', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (256, 'icon', 'sugar', 'el-icon-sugar', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (257, 'icon', 'dessert', 'el-icon-dessert', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (258, 'icon', 'ice-cream', 'el-icon-ice-cream', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (259, 'icon', 'hot-water', 'el-icon-hot-water', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (260, 'icon', 'water-cup', 'el-icon-water-cup', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (261, 'icon', 'coffee-cup', 'el-icon-coffee-cup', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (262, 'icon', 'cold-drink', 'el-icon-cold-drink', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (263, 'icon', 'goblet', 'el-icon-goblet', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (264, 'icon', 'goblet-full', 'el-icon-goblet-full', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (265, 'icon', 'goblet-square', 'el-icon-goblet-square', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (266, 'icon', 'goblet-square-full', 'el-icon-goblet-square-full', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (267, 'icon', 'refrigerator', 'el-icon-refrigerator', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (268, 'icon', 'grape', 'el-icon-grape', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (269, 'icon', 'watermelon', 'el-icon-watermelon', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (270, 'icon', 'cherry', 'el-icon-cherry', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (271, 'icon', 'apple', 'el-icon-apple', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (272, 'icon', 'pear', 'el-icon-pear', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (273, 'icon', 'orange', 'el-icon-orange', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (274, 'icon', 'ice-tea', 'el-icon-ice-tea', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (275, 'icon', 'ice-drink', 'el-icon-ice-drink', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (276, 'icon', 'milk-tea', 'el-icon-milk-tea', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (277, 'icon', 'potato-strips', 'el-icon-potato-strips', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (278, 'icon', 'lollipop', 'el-icon-lollipop', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');
INSERT INTO `dict_item` VALUES (279, 'icon', 'ice-cream-square', 'el-icon-ice-cream-square', NULL, '2024-07-30 23:06:45', '2024-07-30 23:06:45');

-- ----------------------------
-- Table structure for house
-- ----------------------------
DROP TABLE IF EXISTS `house`;
CREATE TABLE `house`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '房屋地址',
  `house_type_id` bigint NOT NULL COMMENT '房屋类型ID',
  `area` decimal(10, 2) NOT NULL COMMENT '房屋面积',
  `status` enum('VACANT','OCCUPIED','RENTED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'VACANT' COMMENT '房屋状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_house_type_id`(`house_type_id` ASC) USING BTREE,
  CONSTRAINT `fk_house_house_type` FOREIGN KEY (`house_type_id`) REFERENCES `house_type` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '房屋信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for house_type
-- ----------------------------
DROP TABLE IF EXISTS `house_type`;
CREATE TABLE `house_type`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型名称',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型描述',
  `property_fee` decimal(10, 2) NOT NULL COMMENT '物业费月单价',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '房屋类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of house_type
-- ----------------------------
INSERT INTO `house_type` VALUES (1, '高层A户型', '三室两厅', 30.00, '2025-02-07 09:57:35', '2025-02-07 09:57:35');
INSERT INTO `house_type` VALUES (2, '高层B户型', '两室两厅', 250.00, '2025-02-07 09:57:35', '2025-02-07 09:57:35');
INSERT INTO `house_type` VALUES (3, '洋房C户型', '四室两厅', 400.00, '2025-02-07 09:57:35', '2025-02-07 09:57:35');

-- ----------------------------
-- Table structure for housekeeper
-- ----------------------------
DROP TABLE IF EXISTS `housekeeper`;
CREATE TABLE `housekeeper`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系电话',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '身份证号',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '个人描述',
  `work_years` int NOT NULL DEFAULT 0 COMMENT '工作年限',
  `rating` decimal(2, 1) NULL DEFAULT 5.0 COMMENT '评分',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_housekeeper_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '家政人员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of housekeeper
-- ----------------------------
INSERT INTO `housekeeper` VALUES (1, 8, '李阿姨', '13800138001', '110101196001011234', 'ACTIVE', '从事家政工作15年，擅长家居清洁、烹饪和老人照顾，有丰富的经验和耐心。', 15, 4.8, '2025-03-16 21:19:23', '2025-03-16 21:19:23');
INSERT INTO `housekeeper` VALUES (2, 9, '王师傅', '13800138002', '110101197001011235', 'ACTIVE', '专业水电维修10年经验，擅长各类家电维修和安装，服务认真负责。', 10, 4.9, '2025-03-16 21:19:23', '2025-03-16 21:19:23');
INSERT INTO `housekeeper` VALUES (3, 10, '张阿姨', '13800138003', '110101196501011236', 'ACTIVE', '擅长照顾婴幼儿和老人，有护理证书，性格温和有耐心，做事细心。', 8, 4.7, '2025-03-16 21:19:23', '2025-03-16 21:19:23');
INSERT INTO `housekeeper` VALUES (4, 11, '赵师傅', '13800138004', '110101198001011237', 'ACTIVE', '专业保洁5年经验，擅长深度清洁和除螨服务，使用环保清洁产品。', 5, 4.5, '2025-03-16 21:19:23', '2025-03-16 21:19:23');
INSERT INTO `housekeeper` VALUES (5, 12, '刘阿姨', '13800138005', '110101197501011238', 'DISABLED', '擅长中式烹饪，曾在高档餐厅工作，可提供上门做饭和家宴服务。', 12, 4.6, '2025-03-16 21:19:23', '2025-03-16 21:19:23');

-- ----------------------------
-- Table structure for housekeeper_service
-- ----------------------------
DROP TABLE IF EXISTS `housekeeper_service`;
CREATE TABLE `housekeeper_service`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `housekeeper_id` bigint NOT NULL COMMENT '家政人员ID',
  `service_id` bigint NOT NULL COMMENT '服务项目ID',
  `price` decimal(10, 2) NOT NULL COMMENT '服务价格',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_housekeeper_id`(`housekeeper_id` ASC) USING BTREE,
  INDEX `idx_service_id`(`service_id` ASC) USING BTREE,
  CONSTRAINT `fk_housekeeper_service_housekeeper` FOREIGN KEY (`housekeeper_id`) REFERENCES `housekeeper` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_housekeeper_service_service` FOREIGN KEY (`service_id`) REFERENCES `housekeeping_service` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '家政人员服务项目关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of housekeeper_service
-- ----------------------------
INSERT INTO `housekeeper_service` VALUES (2, 1, 2, 160.00, '2025-03-16 21:19:23', '2025-03-16 21:19:23');
INSERT INTO `housekeeper_service` VALUES (3, 1, 3, 110.00, '2025-03-16 21:19:23', '2025-03-16 21:19:23');
INSERT INTO `housekeeper_service` VALUES (4, 2, 1, 90.00, '2025-03-16 21:19:23', '2025-03-16 21:19:23');
INSERT INTO `housekeeper_service` VALUES (5, 3, 3, 105.00, '2025-03-16 21:19:23', '2025-03-16 21:19:23');
INSERT INTO `housekeeper_service` VALUES (6, 3, 4, 130.00, '2025-03-16 21:19:23', '2025-03-16 21:19:23');
INSERT INTO `housekeeper_service` VALUES (7, 4, 1, 80.00, '2025-03-16 21:19:23', '2025-03-16 21:19:23');
INSERT INTO `housekeeper_service` VALUES (8, 4, 2, 150.00, '2025-03-16 21:19:23', '2025-03-16 21:19:23');
INSERT INTO `housekeeper_service` VALUES (9, 5, 5, 120.00, '2025-03-16 21:19:23', '2025-03-16 21:19:23');
INSERT INTO `housekeeper_service` VALUES (10, 1, 4, 120.00, '2025-03-16 21:51:40', '2025-03-16 21:51:40');

-- ----------------------------
-- Table structure for housekeeping_order
-- ----------------------------
DROP TABLE IF EXISTS `housekeeping_order`;
CREATE TABLE `housekeeping_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `owner_id` bigint NOT NULL COMMENT '业主ID',
  `housekeeper_id` bigint NOT NULL COMMENT '家政人员ID',
  `service_id` bigint NOT NULL COMMENT '服务项目ID',
  `appointment_time` datetime NOT NULL COMMENT '预约时间',
  `service_duration` int NULL DEFAULT NULL COMMENT '服务时长(分钟)',
  `amount` decimal(10, 2) NOT NULL COMMENT '订单金额',
  `status` enum('PENDING','CONFIRMED','IN_PROGRESS','COMPLETED','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '订单状态',
  `payment_status` enum('UNPAID','PAID','REFUNDED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'UNPAID' COMMENT '支付状态',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注',
  `evaluation_rating` int NULL DEFAULT NULL COMMENT '业主评价星级1-5',
  `evaluation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '业主评价内容',
  `pay_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '支付方式: ALIPAY-支付宝, WECHAT-微信, CASH-现金',
  `alipay_order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '支付宝订单号（商户侧）',
  `alipay_trade_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '支付宝交易号（平台侧）',
  `pay_time` datetime NULL COMMENT '支付时间',
  `notify_time` datetime NULL COMMENT '支付宝异步通知时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_owner_id`(`owner_id` ASC) USING BTREE,
  INDEX `idx_housekeeper_id`(`housekeeper_id` ASC) USING BTREE,
  INDEX `idx_service_id`(`service_id` ASC) USING BTREE,
  CONSTRAINT `fk_housekeeping_order_housekeeper` FOREIGN KEY (`housekeeper_id`) REFERENCES `housekeeper` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_housekeeping_order_owner` FOREIGN KEY (`owner_id`) REFERENCES `owner` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_housekeeping_order_service` FOREIGN KEY (`service_id`) REFERENCES `housekeeping_service` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '家政服务订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of housekeeping_order（无种子数据，订单由业务产生）
-- ----------------------------

-- ----------------------------
-- Table structure for housekeeping_service
-- ----------------------------
DROP TABLE IF EXISTS `housekeeping_service`;
CREATE TABLE `housekeeping_service`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '服务名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '服务描述',
  `base_price` decimal(10, 2) NOT NULL COMMENT '基础价格',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '计价单位(小时/次/平米)',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '服务类别',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '家政服务项目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of housekeeping_service
-- ----------------------------
INSERT INTO `housekeeping_service` VALUES (1, '日常保洁', '包括房间清扫、擦拭家具、清洁厨卫等基础保洁服务', 80.00, '小时', '清洁服务', '2025-03-16 21:19:23', '2025-03-16 21:44:51');
INSERT INTO `housekeeping_service` VALUES (2, '深度保洁', '包括家电清洗、玻璃清洁、地板打蜡等深度清洁服务', 150.00, '次', '清洁服务', '2025-03-16 21:19:23', '2025-03-16 21:19:23');
INSERT INTO `housekeeping_service` VALUES (3, '老人照护', '为老人提供生活照料、陪伴聊天、协助吃药等服务', 100.00, '小时', '照护服务', '2025-03-16 21:19:23', '2025-03-16 21:19:23');
INSERT INTO `housekeeping_service` VALUES (4, '育儿嫂服务', '为婴幼儿提供照料、喂养、早教等专业服务', 120.00, '小时', '照护服务', '2025-03-16 21:19:23', '2025-03-16 21:19:23');
INSERT INTO `housekeeping_service` VALUES (5, '上门做饭', '根据客户需求提供上门采购食材、烹饪服务', 100.00, '次', '烹饪服务', '2025-03-16 21:19:23', '2025-03-16 21:19:23');

-- ----------------------------
-- Table structure for maintenance_staff
-- ----------------------------
DROP TABLE IF EXISTS `maintenance_staff`;
CREATE TABLE `maintenance_staff`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `position` enum('MAINTENANCE','SECURITY') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '职位，后勤或安保',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_maintenance_staff_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '后勤人员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of maintenance_staff
-- ----------------------------
INSERT INTO `maintenance_staff` VALUES (1, 15, 'MAINTENANCE', '负责小区日常维修工作', '2025-02-07 09:57:35', '2025-04-29 23:19:57');
INSERT INTO `maintenance_staff` VALUES (2, 4, 'MAINTENANCE', '负责小区安保工作', '2025-02-07 09:57:35', '2025-04-29 23:11:37');
INSERT INTO `maintenance_staff` VALUES (4, 14, 'MAINTENANCE', '111', '2025-04-29 23:10:58', '2025-04-29 23:10:58');

-- ----------------------------
-- Table structure for news
-- ----------------------------
DROP TABLE IF EXISTS `news`;
CREATE TABLE `news`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '新闻标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '新闻内容',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `img` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面',
  `publisher` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发布人信息',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '新闻表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of news
-- ----------------------------
INSERT INTO `news` VALUES (1, '佰惠区廷管家家政入社区生活：一站式服务新生态，助力社区经济 ', '<p style=\"text-align: left;\"><br></p><p style=\"text-align: left;\"><br></p><p style=\"text-align: left;\"><br></p><p style=\"text-align: left;\">随着社会老龄化进程加速、双职工家庭增多以及人们生活品质追求的提升，家政服务进社区的趋势将更加明显。家政服务将成为社区综合服务体系的重要组成部分，与社区养老、托幼等服务深度整合，共同打造“一站式”社区生活服务平台，满足居民全生命周期的服务需求。家政服务进社区，不仅能解决居民实际生活问题，更能借此增强社区凝聚力，提升社区自治能力，使之成为创新社区治理模式、推动社区和谐发展的重要工具。人工智能、物联网等前沿科技将进一步赋能家政服务，实现智能清扫机器人、远程健康监测、个性化饮食推荐等高级服务，让家政服务更加精细化、智能化。</p><p style=\"text-align: left;\">通过家政服务进社区，不仅能解决居民实际生活问题，更能借此增强社区凝聚力，提升社区自治能力，使之成为创新社区治理模式、推动社区和谐发展的重要工具。</p><p style=\"text-align: left;\"><img src=\"https://q3.itc.cn/q_70/images01/20240408/9c0567f9b3204fd58cccccc04dfebfdf.jpeg\" alt=\"\" data-href=\"\" style=\"height: auto;\"></p><p style=\"text-align: left;\">佰惠区廷管家家政服务与社区经济紧密相连，其优质的服务有助于增强社区凝聚力，促进邻里互助与资源共享，同时通过带动相关消费和服务需求，激活了本地市场的活力，为社区经济的增长注入动力。</p><p style=\"text-align: left;\"><img src=\"https://q4.itc.cn/q_70/images01/20240408/0c406f335ada446da5b5b40ab163f9a7.jpeg\" alt=\"\" data-href=\"\" style=\"height: auto;\"></p><p style=\"text-align: left;\">社区经济强调本地化就业与技能提升。廷管家家政可能参与到社区的家政服务人员培训体系中，通过提供专业的技能培训课程或合作开展职业认证，既提高了家政从业人员的职业素质，又为社区居民提供了就业或再就业的机会，进一步强化了公司在社区经济生态中的社会责任感与影响力。</p><p style=\"text-align: left;\">家政服务进社区是顺应时代发展、满足居民需求的重要举措。通过构建完善的家政服务体系，利用数字化手段提升服务效率，开展家政技能教育培训，强化政策支持与引导，我们有望在社区内打造出一个便捷、和谐、互助的生活新生态，让每一个居民都能享受到高品质的家政服务，提升生活的幸福感与满意度</p>', '2025-02-07 09:57:35', '2025-02-10 10:37:10', '/img/1740159047142.png', '管理员');
INSERT INTO `news` VALUES (2, '业主委员会换届选举结果公示', '<p>新一届业主委员会选举工作已经结束</p>', '2025-02-07 09:57:35', '2025-02-10 10:37:16', '/img/1740159057354.jpeg', '活动组');

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读',
  `id` bigint NOT NULL AUTO_INCREMENT,
  `receiver_id` bigint NOT NULL COMMENT '接收人ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知内容',
  `receiver_type` enum('OWNER','ADMIN','MAINTENANCE','VOLUNTEER') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接收人类型',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `type` enum('NOTICE','PROPERTY','ACTIVITY') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notification
-- ----------------------------
INSERT INTO `notification` VALUES (1, 1, 2, '物业费催缴通知', '您的2024年1月物业费尚未缴纳', 'OWNER', '2025-02-07 09:57:35', '2025-02-08 21:23:36', NULL);
INSERT INTO `notification` VALUES (1, 3, 5, 'test', 'test', 'OWNER', '2025-02-10 10:59:29', '2025-02-10 10:59:29', NULL);
INSERT INTO `notification` VALUES (1, 6, 5, '测试', '测试', 'OWNER', '2025-02-10 11:02:34', '2025-02-10 11:02:34', NULL);
INSERT INTO `notification` VALUES (0, 7, 6, '测试', '测试', 'OWNER', '2025-02-10 11:02:34', '2025-02-10 11:02:34', NULL);
INSERT INTO `notification` VALUES (1, 8, 5, '11', '11', 'OWNER', '2025-02-10 11:05:58', '2025-02-10 11:05:58', NULL);
INSERT INTO `notification` VALUES (0, 14, 4, 'test', '1', 'MAINTENANCE', '2025-02-10 11:14:02', '2025-02-10 11:14:02', NULL);

-- ----------------------------
-- Table structure for owner
-- ----------------------------
DROP TABLE IF EXISTS `owner`;
CREATE TABLE `owner`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '身份证号',
  `family_members` json NULL COMMENT '家庭成员信息',
  `house_id` bigint NOT NULL COMMENT '关联房屋ID',
  `workplace` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工作单位',
  `check_in_time` datetime NOT NULL COMMENT '入住时间',
  `remarks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注信息',
  `pay_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '支付方式: ALIPAY-支付宝, WECHAT-微信, CASH-现金',
  `alipay_order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '支付宝订单号（商户侧）',
  `alipay_trade_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '支付宝交易号（平台侧）',
  `notify_time` datetime NULL COMMENT '支付宝异步通知时间',
  `plate_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '车牌号码',
  `pet_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '宠物信息',
  `decoration_status` enum('NONE','SIMPLE','LUXURY') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'NONE' COMMENT '装修情况',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` enum('ENABLED','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'ENABLED' COMMENT '审核状态',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_house_id`(`house_id` ASC) USING BTREE,
  CONSTRAINT `fk_owner_house` FOREIGN KEY (`house_id`) REFERENCES `house` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_owner_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '业主信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of owner
-- ----------------------------
INSERT INTO `owner` (
  `id`, `user_id`, `id_card`, `family_members`, `house_id`, `workplace`,
  `check_in_time`, `remarks`, `pay_type`, `alipay_order_no`, `alipay_trade_no`,
  `notify_time`, `plate_number`, `pet_info`, `decoration_status`,
  `created_at`, `updated_at`, `status`
) VALUES
(1, 2, '440101199001011234', '[{\"name\": \"小王\", \"phone\": \"15252311111\", \"idCard\": \"320821200104093915\", \"relation\": \"子女\"}]', 1, '广州科技公司', '2023-01-01 10:00:00', NULL, NULL, NULL, NULL, NULL, '粤A123456', '金毛一只', 'LUXURY', '2025-02-07 09:57:35', '2025-02-08 21:54:52', 'ENABLED'),
(2, 3, '440101199001011235', NULL, 3, '广州医院', '2023-02-01 14:00:00', NULL, NULL, NULL, NULL, NULL, '粤B12345', NULL, 'NONE', '2025-02-07 09:57:35', '2025-02-08 21:54:54', 'DISABLED'),
(3, 6, '320821200104093915', '[{\"name\": \"小王\", \"phone\": \"15252311111\", \"idCard\": \"320821200104093915\", \"relation\": \"子女\"}]', 2, '小米', '2025-02-04 00:00:00', '', NULL, NULL, NULL, NULL, '124', '', 'SIMPLE', '2025-02-08 21:55:05', '2025-02-08 21:55:05', 'ENABLED'),
(6, 5, '320821200104093915', '[]', 4, '华为', '2025-02-11 00:00:00', '', NULL, NULL, NULL, NULL, '', '', 'NONE', '2025-02-17 10:30:54', '2025-02-17 10:33:23', 'ENABLED');

-- ----------------------------
-- Table structure for parking_fee
-- ----------------------------
DROP TABLE IF EXISTS `parking_fee`;
CREATE TABLE `parking_fee`  (
  `parking_space_id` bigint NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plate_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '车牌号码',
  `owner_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '车主姓名',
  `owner_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '车主电话',
  `entry_time` datetime NOT NULL COMMENT '停车时间',
  `exit_time` datetime NULL DEFAULT NULL COMMENT '出场时间',
  `fee_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '停车费用',
  `status` enum('PAID','UNPAID') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'UNPAID' COMMENT '收费状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_plate_number`(`plate_number` ASC) USING BTREE,
  INDEX `idx_entry_time`(`entry_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '停车费记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of parking_fee
-- ----------------------------
INSERT INTO `parking_fee` (
  `parking_space_id`, `id`, `plate_number`, `owner_name`, `owner_phone`,
  `entry_time`, `exit_time`, `fee_amount`, `status`, `created_at`,
  `updated_at`, `order_no`
) VALUES
(2, 1, '粤C12345', '赵六', '13800138006', '2024-01-01 10:00:00', '2024-01-01 12:00:00', 20.00, 'PAID', '2025-02-07 09:57:35', '2025-05-07 11:53:21', NULL),
(2, 2, '粤D12345', '钱七', '13800138007', '2024-01-01 14:00:00', '2025-02-09 12:41:03', 48595.00, 'PAID', '2025-02-07 09:57:35', '2025-05-07 11:53:23', NULL),
(3, 3, '京A11111', '1111', '15252393509', '2025-02-09 12:30:37', '2025-05-07 11:54:45', 10440.00, 'PAID', '2025-02-09 12:30:36', '2025-05-07 12:07:58', NULL),
(2, 4, '苏A12345', '小王', '15245678900', '2025-04-25 00:00:00', '2025-04-28 15:07:16', 440.00, 'PAID', '2025-04-28 15:07:11', '2025-05-07 11:53:28', NULL),
(2, 5, '苏B12345', '小王', '15212345678', '2025-04-22 00:00:00', '2025-04-28 15:13:20', 800.00, 'PAID', '2025-04-28 15:13:13', '2025-05-07 11:53:19', NULL);

-- ----------------------------
-- Table structure for parking_space
-- ----------------------------
DROP TABLE IF EXISTS `parking_space`;
CREATE TABLE `parking_space`  (
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '车位编号',
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` enum('PUBLIC','PRIVATE') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '车位类型',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '车位位置',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '车位表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of parking_space
-- ----------------------------
INSERT INTO `parking_space` VALUES ('PA011', 1, 'PRIVATE', 'A区-01', '2025-02-07 09:57:35', '2025-02-07 12:03:39');
INSERT INTO `parking_space` VALUES ('PA02', 2, 'PUBLIC', 'A区-02', '2025-02-07 09:57:35', '2025-02-09 12:28:42');
INSERT INTO `parking_space` VALUES ('PB01', 3, 'PUBLIC', 'B区-01', '2025-02-07 09:57:35', '2025-02-07 12:03:44');
INSERT INTO `parking_space` VALUES ('PA03', 6, 'PRIVATE', 'A', '2025-02-09 12:09:29', '2025-02-09 12:09:29');

-- ----------------------------
-- Table structure for pet_sitter
-- ----------------------------
DROP TABLE IF EXISTS `pet_sitter`;
CREATE TABLE `pet_sitter`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系电话',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '身份证号',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `service_price` decimal(10, 2) NOT NULL COMMENT '服务价格',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '宠物代喂人员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_sitter
-- ----------------------------
INSERT INTO `pet_sitter` VALUES (1, '张阿姨', '13800138010', '440101196001011234', 'ACTIVE', '有多年养宠物经验', 50.00, '2025-02-07 09:57:35', '2025-02-07 09:57:35');
INSERT INTO `pet_sitter` VALUES (2, '李阿姨', '13800138011', '440101196001011235', 'ACTIVE', '擅长照顾猫咪', 45.00, '2025-02-07 09:57:35', '2025-02-07 09:57:35');

-- ----------------------------
-- Table structure for pet_sitting_request
-- ----------------------------
DROP TABLE IF EXISTS `pet_sitting_request`;
CREATE TABLE `pet_sitting_request`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `owner_id` bigint NOT NULL COMMENT '业主ID',
  `sitter_id` bigint NOT NULL COMMENT '代喂人员ID',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '要求描述',
  `expected_time` datetime NOT NULL COMMENT '期待服务时间',
  `status` enum('PENDING','CONTACTED','COMPLETED','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '联系状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_owner_id`(`owner_id` ASC) USING BTREE,
  INDEX `idx_sitter_id`(`sitter_id` ASC) USING BTREE,
  CONSTRAINT `fk_pet_sitting_request_owner` FOREIGN KEY (`owner_id`) REFERENCES `owner` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_pet_sitting_request_sitter` FOREIGN KEY (`sitter_id`) REFERENCES `pet_sitter` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '宠物代喂服务请求表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_sitting_request
-- ----------------------------
INSERT INTO `pet_sitting_request` VALUES (1, 1, 1, '帮忙照看金毛三天', '2024-01-10 09:00:00', 'CONTACTED', '2025-02-07 09:57:35', '2025-02-08 16:50:05');
INSERT INTO `pet_sitting_request` VALUES (2, 2, 2, '喂食两次', '2024-01-12 09:00:00', 'PENDING', '2025-02-07 09:57:35', '2025-02-07 09:57:35');
INSERT INTO `pet_sitting_request` VALUES (3, 1, 2, '见面聊！！！！！！！！！！！！1', '2025-02-19 00:00:00', 'COMPLETED', '2025-02-08 16:43:28', '2025-02-08 16:43:28');
INSERT INTO `pet_sitting_request` VALUES (4, 6, 1, '5858585858585858585858581', '2025-02-27 00:00:00', 'COMPLETED', '2025-02-17 10:37:08', '2025-02-17 10:37:08');

-- ----------------------------
-- Table structure for private_parking
-- ----------------------------
DROP TABLE IF EXISTS `private_parking`;
CREATE TABLE `private_parking`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parking_space_id` bigint NOT NULL COMMENT '车位ID',
  `owner_id` bigint NOT NULL COMMENT '业主ID',
  `plate_info` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '车牌信息',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parking_space_id`(`parking_space_id` ASC) USING BTREE,
  INDEX `idx_owner_id`(`owner_id` ASC) USING BTREE,
  CONSTRAINT `fk_private_parking_owner` FOREIGN KEY (`owner_id`) REFERENCES `owner` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_private_parking_parking_space` FOREIGN KEY (`parking_space_id`) REFERENCES `parking_space` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '私人车位使用记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of private_parking
-- ----------------------------
INSERT INTO `private_parking` VALUES (1, 1, 1, '粤A12345', '2025-02-07 09:57:35', '2025-02-07 09:57:35');
INSERT INTO `private_parking` VALUES (2, 2, 2, '粤B12345', '2025-02-07 09:57:35', '2025-02-07 09:57:35');

-- ----------------------------
-- Table structure for property_fee
-- ----------------------------
DROP TABLE IF EXISTS `property_fee`;
CREATE TABLE `property_fee`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fee_date` date NOT NULL COMMENT '物业费所属年月',
  `amount` decimal(10, 2) NOT NULL COMMENT '物业费金额',
  `status` enum('PAID','UNPAID') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'UNPAID' COMMENT '缴费状态',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '缴费时间',
  `remarks` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '缴费备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `house_id` bigint NULL DEFAULT 1 COMMENT '房屋id',
  `owner_id` bigint NULL DEFAULT 1 COMMENT '业主id',
  `order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_fee_date`(`fee_date` ASC) USING BTREE,
  INDEX `ower_id_id`(`owner_id` ASC) USING BTREE,
  INDEX `house_id_id`(`house_id` ASC) USING BTREE,
  CONSTRAINT `house_id_id` FOREIGN KEY (`house_id`) REFERENCES `house` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ower_id_id` FOREIGN KEY (`owner_id`) REFERENCES `owner` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物业费记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of property_fee
-- ----------------------------
INSERT INTO `property_fee` (
  `id`, `fee_date`, `amount`, `status`, `payment_time`, `remarks`,
  `created_at`, `updated_at`, `house_id`, `owner_id`, `order_no`
) VALUES
(14, '2025-01-01', 3615.00, 'UNPAID', NULL, NULL, '2025-03-16 13:12:58', '2025-03-16 13:12:58', 1, 1, NULL),
(15, '2025-01-01', 22250.00, 'UNPAID', NULL, NULL, '2025-03-16 13:12:58', '2025-03-16 13:12:58', 3, 2, NULL),
(16, '2025-02-01', 3615.00, 'PAID', NULL, NULL, '2025-03-16 13:13:07', '2025-03-16 13:13:06', 1, 1, NULL),
(17, '2025-02-01', 22250.00, 'UNPAID', NULL, NULL, '2025-03-16 13:13:07', '2025-03-16 13:13:06', 3, 2, NULL),
(18, '2023-11-01', 3615.00, 'UNPAID', NULL, NULL, '2025-03-16 13:13:16', '2025-03-16 13:13:15', 1, 1, NULL),
(19, '2023-11-01', 22250.00, 'UNPAID', NULL, NULL, '2025-03-16 13:13:16', '2025-03-16 13:13:15', 3, 2, NULL),
(20, '2025-03-01', 3615.00, 'PAID', '2025-05-07 11:36:50', NULL, '2025-03-17 21:07:19', '2025-03-17 21:07:19', 1, 1, NULL),
(21, '2025-03-01', 22250.00, 'UNPAID', NULL, NULL, '2025-03-17 21:07:19', '2025-03-17 21:07:19', 3, 2, NULL),
(22, '2025-03-01', 3615.00, 'UNPAID', NULL, NULL, '2025-03-17 21:07:19', '2025-03-17 21:07:19', 2, 3, NULL),
(23, '2025-03-01', 2400.00, 'PAID', NULL, NULL, '2025-03-17 21:07:19', '2025-03-17 21:21:29', 4, 6, NULL);

-- ----------------------------
-- Table structure for repair_record
-- ----------------------------
DROP TABLE IF EXISTS `repair_record`;
CREATE TABLE `repair_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `applicant_id` bigint NOT NULL COMMENT '申请人ID',
  `repair_type_id` bigint NOT NULL COMMENT '维修项目ID',
  `house_id` bigint NOT NULL COMMENT '维修房屋ID',
  `expected_time` datetime NOT NULL COMMENT '期待上门时间',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '维修描述',
  `maintainer_id` bigint NULL DEFAULT NULL COMMENT '维修人员ID',
  `status` enum('PENDING','IN_PROGRESS','COMPLETED','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '维修状态',
  `result_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '维修结果描述',
  `actual_time` datetime NULL DEFAULT NULL COMMENT '实际维修时间',
  `evaluation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '维修结果评价',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_applicant_id`(`applicant_id` ASC) USING BTREE,
  INDEX `idx_maintainer_id`(`maintainer_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '维修记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of repair_record
-- ----------------------------
INSERT INTO `repair_record` VALUES (1, 1, 1, 1, '2024-01-02 14:00:00', '厨房水管漏水', 2, 'COMPLETED', NULL, NULL, NULL, '2025-02-07 09:57:35', '2025-02-08 11:38:50');
INSERT INTO `repair_record` VALUES (2, 2, 2, 3, '2024-01-03 10:00:00', '客厅电路跳闸', 1, 'COMPLETED', '{\"resultDescription\":\"HAO\"}', '2025-02-09 14:08:15', NULL, '2025-02-07 09:57:35', '2025-02-08 11:38:51');
INSERT INTO `repair_record` VALUES (3, 1, 1, 1, '2025-03-26 00:00:00', '哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈', 1, 'COMPLETED', '好', '2025-02-09 16:29:39', '11', '2025-02-08 11:54:06', '2025-02-08 11:54:05');
INSERT INTO `repair_record` VALUES (4, 6, 1, 4, '2025-02-18 00:00:00', '1114561546', 1, 'COMPLETED', 'ok', '2025-04-28 11:26:05', NULL, '2025-02-17 10:36:46', '2025-02-17 10:36:45');
INSERT INTO `repair_record` VALUES (5, 1, 3, 1, '2025-02-28 00:00:00', '111111111111', NULL, 'PENDING', NULL, NULL, NULL, '2025-02-22 02:08:05', '2025-04-02 15:26:57');
INSERT INTO `repair_record` VALUES (6, 1, 2, 1, '2025-04-30 00:00:00', '家里电路不稳定！希望有人上门维修！！', 1, 'COMPLETED', '一切OK！', '2025-04-29 21:25:53', '处理的不错！！！！！！！！！', '2025-04-29 21:17:53', '2025-04-29 21:17:53');

-- ----------------------------
-- Table structure for repair_type
-- ----------------------------
DROP TABLE IF EXISTS `repair_type`;
CREATE TABLE `repair_type`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '维修项目类型',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `price` decimal(10, 2) NOT NULL COMMENT '定价',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '维修项目类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of repair_type
-- ----------------------------
INSERT INTO `repair_type` VALUES (1, '水管维修', '修理水管漏水问题', 110.00, '2025-02-07 09:57:35', '2025-02-07 09:57:35');
INSERT INTO `repair_type` VALUES (2, '电路维修', '修理电路故障', 150.00, '2025-02-07 09:57:35', '2025-02-07 09:57:35');
INSERT INTO `repair_type` VALUES (3, '门锁维修', '更换或修理门锁', 80.00, '2025-02-07 09:57:35', '2025-02-07 09:57:35');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `role` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单角色',
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单名称',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单路径',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `pid` int NULL DEFAULT NULL COMMENT '菜单父id',
  `page_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '页面路径',
  `sort_num` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 92 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES ('MAINTENANCE,ADMIN', 1, '数据看板', '/showView', 'el-icon-data-board', '系统数据概览', NULL, 'ShowView', 1);
INSERT INTO `sys_menu` VALUES ('ADMIN,MAINTENANCE,KEEPER', 2, '个人工作台', NULL, 'el-icon-user', '个人相关功能', NULL, NULL, 2);
INSERT INTO `sys_menu` VALUES ('ADMIN', 3, '系统配置', NULL, 'el-icon-setting', '系统基础配置', NULL, NULL, 3);
INSERT INTO `sys_menu` VALUES ('ADMIN', 4, '用户管理', NULL, 'el-icon-user-solid', '用户权限管理', NULL, NULL, 4);
INSERT INTO `sys_menu` VALUES ('ADMIN', 5, '资产管理', NULL, 'el-icon-office-building', '物业资产管理', NULL, NULL, 5);
INSERT INTO `sys_menu` VALUES ('ADMIN,KEEPER,MAINTENANCE', 6, '社区服务', NULL, 'el-icon-service', '物业服务管理', NULL, NULL, 6);
INSERT INTO `sys_menu` VALUES ('ADMIN,MAINTENANCE', 7, '信息发布', NULL, 'el-icon-message', '社区信息管理', NULL, NULL, 7);
INSERT INTO `sys_menu` VALUES ('ADMIN,MAINTENANCE,KEEPER', 8, '基本信息', '/personInfo', 'el-icon-info', NULL, 2, 'PersonInfo', 1);
INSERT INTO `sys_menu` VALUES ('ADMIN,MAINTENANCE,KEEPER', 9, '安全设置', '/password', 'el-icon-lock', NULL, 2, 'Password', 2);
INSERT INTO `sys_menu` VALUES ('ADMIN', 11, '菜单配置', '/menu', 'el-icon-menu', NULL, 3, 'Menu', 1);
INSERT INTO `sys_menu` VALUES ('ADMIN', 12, '图标库', '/iconItem', 'el-icon-picture-outline', NULL, 3, 'IconItem', 2);
INSERT INTO `sys_menu` VALUES ('ADMIN', 13, '用户列表', '/userManager', 'el-icon-user', NULL, 4, 'UserManager', 1);
INSERT INTO `sys_menu` VALUES ('ADMIN', 14, '业主档案', '/ownerManager', 'el-icon-s-custom', NULL, 4, 'OwnerManager', 2);
INSERT INTO `sys_menu` VALUES ('ADMIN', 15, '房屋类型', '/houseTypeManager', 'el-icon-house', NULL, 5, 'HouseTypeManager', 1);
INSERT INTO `sys_menu` VALUES ('ADMIN,MAINTENANCE', 16, '房屋档案', '/houseManager', 'el-icon-office-building', NULL, 5, 'HouseManager', 2);
INSERT INTO `sys_menu` VALUES ('ADMIN', 17, '车位资源', '/parkingSpaceManager', 'el-icon-truck', NULL, 5, 'ParkingSpaceManager', 3);
INSERT INTO `sys_menu` VALUES ('ADMIN', 18, '车位缴费', '/parkingFeeManager', 'el-icon-money', NULL, 5, 'ParkingFeeManager', 4);
INSERT INTO `sys_menu` VALUES ('ADMIN', 19, '专属车位', '/privateParkingManager', 'el-icon-s-order', NULL, 5, 'PrivateParkingManager', 5);
INSERT INTO `sys_menu` VALUES ('ADMIN', 20, '物业费用', '/propertyFeeManager', 'el-icon-wallet', NULL, 5, 'PropertyFeeManager', 6);
INSERT INTO `sys_menu` VALUES ('ADMIN', 21, '维修分类', '/repairTypeManager', 'el-icon-s-cooperation', NULL, 6, 'RepairTypeManager', 1);
INSERT INTO `sys_menu` VALUES ('ADMIN,MAINTENANCE', 22, '维修工单', '/repairRecordManager', 'el-icon-document', NULL, 6, 'RepairRecordManager', 2);
INSERT INTO `sys_menu` VALUES ('ADMIN', 25, '志愿活动', '/volunteerActivityManager', 'el-icon-coordinate', NULL, 6, 'VolunteerActivityManager', 3);
INSERT INTO `sys_menu` VALUES ('ADMIN', 26, '活动报名', '/volunteerRegistrationManager', 'el-icon-document-checked', NULL, 6, 'VolunteerRegistrationManager', 4);
INSERT INTO `sys_menu` VALUES ('ADMIN,MAINTENANCE', 27, '投诉反馈', '/complaintManager', 'el-icon-warning', NULL, 7, 'ComplaintManager', 1);
INSERT INTO `sys_menu` VALUES ('ADMIN', 28, '社区公告', '/announcementManager', 'el-icon-bell', NULL, 7, 'AnnouncementManager', 2);
INSERT INTO `sys_menu` VALUES ('ADMIN', 29, '社区动态', '/newsManager', 'el-icon-news', NULL, 7, 'NewsManager', 3);
INSERT INTO `sys_menu` VALUES ('ADMIN', 30, '消息通知', '/notificationManager', 'el-icon-message-solid', NULL, 7, 'NotificationManager', 4);
INSERT INTO `sys_menu` VALUES ('ADMIN', 85, '轮播图管理', 'carouselManager', 'el-icon-menu', NULL, 3, 'CarouselManager', 1);
INSERT INTO `sys_menu` VALUES ('ADMIN', 86, '家政人员', '/housekeeperManager', 'el-icon-user', NULL, 4, 'HousekeeperManager', 2);
INSERT INTO `sys_menu` VALUES ('KEEPER,ADMIN', 90, '服务订单', '/housekeepingOrderManager', 'el-icon-s-release', NULL, 6, 'HousekeepingOrderManager', 1);
INSERT INTO `sys_menu` VALUES ('ADMIN', 91, '后勤人员管理', '/maintenanceStaffManager', 'el-icon-user', NULL, 4, 'MaintenanceStaffManager', 4);

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config`  (
  `id` bigint NOT NULL DEFAULT 1,
  `property_fee_auto_calc_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否开启物业费自动计算',
  `property_fee_auto_calc_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0 0 1 * *' COMMENT '物业费自动计算时间(cron表达式)',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  CONSTRAINT `ck_single_row` CHECK (`id` = 1)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统参数表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_config
-- ----------------------------
INSERT INTO `system_config` VALUES (1, 1, '0 0 1 * *', '2025-02-07 09:57:35');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `role` enum('OWNER','ADMIN','MAINTENANCE','KEEPER') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色',
  `sex` enum('MALE','FEMALE','OTHER') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '性别',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` enum('ENABLED','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ENABLED' COMMENT '账号状态',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '管理员', '13800138000', '$2a$10$yB98kY.B/jzz3126O0nWtuMCIfdP8sZmnSZsIJyGh1sSWXPNZeNZ.', 'ADMIN', 'FEMALE', '2025-02-07 09:57:35', '2025-02-09 11:03:35', 'ENABLED');
INSERT INTO `user` VALUES (2, 'owner1', '张三1', '13800138001', '$2a$10$k1JaYOfIpA9ocuYFTHDKiO6LhJz1ZmAhEMvL6.Pk1.phF1mQroCz2', 'OWNER', 'FEMALE', '2025-02-07 09:57:35', '2025-02-09 11:03:37', 'ENABLED');
INSERT INTO `user` VALUES (3, 'owner2', '李四1', '13800138002', '$2a$10$k1JaYOfIpA9ocuYFTHDKiO6LhJz1ZmAhEMvL6.Pk1.phF1mQroCz2', 'OWNER', 'FEMALE', '2025-02-07 09:57:35', '2025-02-09 11:03:38', 'ENABLED');
INSERT INTO `user` VALUES (4, 'maintenance1', '王五', '13800138003', '$2a$10$yB98kY.B/jzz3126O0nWtuMCIfdP8sZmnSZsIJyGh1sSWXPNZeNZ.', 'MAINTENANCE', 'MALE', '2025-02-07 09:57:35', '2025-04-28 10:58:05', 'ENABLED');
INSERT INTO `user` VALUES (5, 'FTFX', 'JX', '15252393509', '$2a$10$k1JaYOfIpA9ocuYFTHDKiO6LhJz1ZmAhEMvL6.Pk1.phF1mQroCz2', 'OWNER', 'MALE', '2025-02-08 21:46:15', '2025-02-17 10:18:03', 'ENABLED');
INSERT INTO `user` VALUES (6, 'admin1', 'jx', '15252393500', '$2a$10$jhrJv/Rj28BdoGjIpMeae.k5g5WQmNlHk1gm4Gm.DtdCKEar8vH0a', 'OWNER', 'MALE', '2025-02-09 11:19:58', '2025-02-09 11:19:58', 'ENABLED');
INSERT INTO `user` VALUES (8, 'keeper1', '李阿姨', '13800338001', '$2a$10$k1JaYOfIpA9ocuYFTHDKiO6LhJz1ZmAhEMvL6.Pk1.phF1mQroCz2', 'KEEPER', 'FEMALE', '2025-03-16 21:19:22', '2025-03-16 23:02:53', 'ENABLED');
INSERT INTO `user` VALUES (9, 'keeper2', '王师傅', '13800338002', '$2a$10$k1JaYOfIpA9ocuYFTHDKiO6LhJz1ZmAhEMvL6.Pk1.phF1mQroCz2', 'KEEPER', 'MALE', '2025-03-16 21:19:22', '2025-03-16 23:02:49', 'ENABLED');
INSERT INTO `user` VALUES (10, 'keeper3', '张阿姨', '13800338003', '$2a$10$k1JaYOfIpA9ocuYFTHDKiO6LhJz1ZmAhEMvL6.Pk1.phF1mQroCz2', 'KEEPER', 'FEMALE', '2025-03-16 21:19:22', '2025-03-16 23:02:56', 'ENABLED');
INSERT INTO `user` VALUES (11, 'keeper4', '赵师傅', '13800338004', '$2a$10$k1JaYOfIpA9ocuYFTHDKiO6LhJz1ZmAhEMvL6.Pk1.phF1mQroCz2', 'KEEPER', 'MALE', '2025-03-16 21:19:22', '2025-03-16 23:02:57', 'ENABLED');
INSERT INTO `user` VALUES (12, 'keeper5', '刘阿姨', '13800338005', '$2a$10$k1JaYOfIpA9ocuYFTHDKiO6LhJz1ZmAhEMvL6.Pk1.phF1mQroCz2', 'KEEPER', 'FEMALE', '2025-03-16 21:19:22', '2025-03-16 23:02:59', 'DISABLED');
INSERT INTO `user` VALUES (13, 'test001', '赵小齐', '15252123456', '$2a$10$w8u5ZfPIzjM.TkOoV/F3Aub1Fvv1lT85wbNv/ovMddM5L247QEKBC', 'MAINTENANCE', 'MALE', '2025-04-29 22:54:49', '2025-04-29 22:54:49', 'ENABLED');
INSERT INTO `user` VALUES (14, 'ftfx11', 'jx', '15252393501', '$2a$10$gL095sarYZOJK4KMfrWRZOa7Ma.Mx7ugBpEG3i8Ndepbc7uTMmW2C', 'MAINTENANCE', 'MALE', '2025-04-29 23:10:58', '2025-04-29 23:10:58', 'ENABLED');
INSERT INTO `user` VALUES (15, '小白白', '阿白', '16841586341', '$2a$10$lWqRPHC.2P3ELlsbkpw8me4Bu5DtMAw1vyDEPEAim3QBiZSuqgoCW', 'MAINTENANCE', 'OTHER', '2025-04-29 23:19:56', '2025-04-29 23:19:56', 'ENABLED');

-- ----------------------------
-- Table structure for volunteer_activity
-- ----------------------------
DROP TABLE IF EXISTS `volunteer_activity`;
CREATE TABLE `volunteer_activity`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动名称',
  `start_time` datetime NOT NULL COMMENT '活动开始时间',
  `end_time` datetime NOT NULL COMMENT '活动结束时间',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动地点',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动描述',
  `max_participants` int NOT NULL COMMENT '最大人数',
  `current_participants` int NOT NULL DEFAULT 0 COMMENT '当前报名人数',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系人',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系电话',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '小区活动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of volunteer_activity（2026 测试数据：5 条小区活动）
-- ----------------------------
INSERT INTO `volunteer_activity` VALUES (1, '亲子趣味运动会', '2026-03-22 09:00:00', '2026-03-22 12:00:00', '小区中央广场', '面向业主家庭的轻量趣味运动会，设置亲子接力、两人三足、跳绳接力等项目，强调参与与协作，不计专业名次。请穿着运动鞋与宽松衣物，现场备有饮用水和简易急救包。建议提前 15 分钟到场热身与领取号码贴，儿童须由家长全程陪同。', 40, 0, '王磊', '13800001001', '2026-03-01 10:00:00', '2026-03-01 10:00:00');
INSERT INTO `volunteer_activity` VALUES (2, '春季健康讲座', '2026-03-28 14:30:00', '2026-03-28 16:30:00', '社区活动中心多功能厅', '邀请社区卫生服务中心医师主讲「春季过敏与慢病日常管理」，含互动答疑与免费血压测量（名额有限，现场排队）。适合中老年业主及家庭照护者参加，可自备既往体检报告复印件便于咨询。讲座结束后发放简明健康折页。', 60, 0, '李医生', '13800001002', '2026-03-01 10:00:00', '2026-03-01 10:00:00');
INSERT INTO `volunteer_activity` VALUES (3, '小区义务植树', '2026-04-05 09:00:00', '2026-04-05 11:30:00', '小区东侧绿化带', '结合春季绿化补植，组织业主认领树苗、培土浇水，物业提供工具与手套（建议自带水壶与防晒用品）。活动以安全为第一，儿童需在家长看护下参与。结束后统一合影留念，并公示树木养护小贴士。', 35, 0, '张工', '13800001003', '2026-03-01 10:00:00', '2026-03-01 10:00:00');
INSERT INTO `volunteer_activity` VALUES (4, '周末观影', '2026-04-12 19:00:00', '2026-04-12 21:00:00', '社区活动中心影音室', '周末放映一部合家欢主题影片（国语对白），现场提供简易座椅编号，请提前入场就座，观影期间请勿大声喧哗与接打电话。禁止携带气味较大食物，离场时请带走垃圾，共同保持环境整洁。', 45, 0, '周管理员', '13800001004', '2026-03-01 10:00:00', '2026-03-01 10:00:00');
INSERT INTO `volunteer_activity` VALUES (5, '跳蚤市场', '2026-04-19 10:00:00', '2026-04-19 17:00:00', '小区中央广场', '业主二手闲置与手工小物市集，每户可预约一个摊位（约 1.2m 桌位），禁售食品、危险品与违禁品。请自备标价签与零钱，服从现场工作人员疏导。遇雨天将顺延并提前在公告栏与微信群通知。', 50, 0, '陈晨', '13800001005', '2026-03-01 10:00:00', '2026-03-01 10:00:00');

-- ----------------------------
-- Table structure for volunteer_registration
-- ----------------------------
DROP TABLE IF EXISTS `volunteer_registration`;
CREATE TABLE `volunteer_registration`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `volunteer_id` bigint NOT NULL COMMENT '报名人ID',
  `status` enum('REGISTERED','CHECKED_IN') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'REGISTERED' COMMENT '报名状态',
  `register_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_activity_volunteer`(`activity_id` ASC, `volunteer_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '小区活动报名表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of volunteer_registration（可按需自行插入测试报名）
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;