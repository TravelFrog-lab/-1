-- 新增三个投票主题（含详情说明），数据库：wanda_property
-- 执行前请确认 vote、vote_option 表已存在

-- 1. 是否翻新小区里的游乐场
INSERT INTO vote (title, description, vote_type, start_time, end_time, eligibility, creator_id, result_after_end_only, created_at, updated_at)
VALUES (
  '是否翻新小区里的游乐场',
  '小区儿童游乐设施使用多年，部分器材老化、存在安全隐患，且无法满足当前住户子女的娱乐需求。为提升居住品质、保障儿童安全，业委会提议对游乐场进行翻新改造，现征求全体业主意见。',
  'SINGLE',
  NOW(),
  DATE_ADD(NOW(), INTERVAL 2 MONTH),
  'OWNER',
  NULL,
  0,
  NOW(),
  NOW()
);
SET @v1 = LAST_INSERT_ID();
INSERT INTO vote_option (vote_id, option_text, sort_order) VALUES (@v1, '同意', 0), (@v1, '不同意', 1);

-- 2. 是否新建电动车停车棚并加装充电设备
INSERT INTO vote (title, description, vote_type, start_time, end_time, eligibility, creator_id, result_after_end_only, created_at, updated_at)
VALUES (
  '是否新建电动车停车棚并加装充电设备',
  '近年来电动车数量增多，楼道充电、飞线充电等现象屡禁不止，存在消防隐患。为规范停放、杜绝入户充电，业委会提议在小区内新建集中式电动车棚并加装合规充电设备，既方便业主充电又保障小区安全。',
  'SINGLE',
  NOW(),
  DATE_ADD(NOW(), INTERVAL 2 MONTH),
  'OWNER',
  NULL,
  0,
  NOW(),
  NOW()
);
SET @v2 = LAST_INSERT_ID();
INSERT INTO vote_option (vote_id, option_text, sort_order) VALUES (@v2, '同意', 0), (@v2, '不同意', 1);

-- 3. 是否翻修地下停车库
INSERT INTO vote (title, description, vote_type, start_time, end_time, eligibility, creator_id, result_after_end_only, created_at, updated_at)
VALUES (
  '是否翻修地下停车库',
  '地下车库地面破损、照明不足、排水不畅等问题日益突出，影响停车体验与车辆安全。业委会提议对地下车库进行翻修，包括地面整修、照明改造与排水疏通，以改善停车环境。',
  'SINGLE',
  NOW(),
  DATE_ADD(NOW(), INTERVAL 2 MONTH),
  'OWNER',
  NULL,
  0,
  NOW(),
  NOW()
);
SET @v3 = LAST_INSERT_ID();
INSERT INTO vote_option (vote_id, option_text, sort_order) VALUES (@v3, '同意', 0), (@v3, '不同意', 1);
