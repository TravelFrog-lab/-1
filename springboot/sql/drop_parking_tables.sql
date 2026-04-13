-- 下线停车业务后删除相关表（请在备份库后执行）
-- 顺序：先删依赖车位表的子表

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS private_parking;
DROP TABLE IF EXISTS parking_fee;
DROP TABLE IF EXISTS parking_space;

SET FOREIGN_KEY_CHECKS = 1;
