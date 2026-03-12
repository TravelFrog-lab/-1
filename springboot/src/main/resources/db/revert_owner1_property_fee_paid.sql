-- 将 owner1 的物业费中所有「已缴费」记录回退为「未缴费」，便于演示
-- 执行前请确认数据库 wanda_property 已存在且 username='owner1' 对应用户存在

UPDATE property_fee pf
INNER JOIN owner o ON o.id = pf.owner_id
INNER JOIN user u ON u.id = o.user_id
SET pf.status = 'UNPAID',
    pf.pay_type = NULL,
    pf.alipay_order_no = NULL,
    pf.alipay_trade_no = NULL,
    pf.payment_time = NULL,
    pf.notify_time = NULL,
    pf.updated_at = NOW()
WHERE u.username = 'owner1'
  AND pf.status = 'PAID';
