-- 为 owner1 绑定房屋，使物业费 ≈ 1500 元/月（100㎡ × 15元/㎡/月）
-- 数据库：wanda_property

-- 1. 新增房屋类型：15 元/㎡/月
INSERT INTO house_type (name, description, property_fee, created_at, updated_at)
VALUES ('普通住宅', '住宅 15元/㎡/月', 15.00, NOW(), NOW());
SET @house_type_id = LAST_INSERT_ID();

-- 2. 新增房屋：100㎡，地址 万达小区 1栋1单元101
INSERT INTO house (address, house_type_id, area, status, created_at, updated_at)
VALUES ('万达小区 1栋1单元101', @house_type_id, 100.00, 'OCCUPIED', NOW(), NOW());
SET @house_id = LAST_INSERT_ID();

-- 3. 将 owner1 绑定到该房屋（通过 username 找到对应用户再更新业主表）
UPDATE owner o
INNER JOIN user u ON u.id = o.user_id
SET o.house_id = @house_id
WHERE u.username = 'owner1';
