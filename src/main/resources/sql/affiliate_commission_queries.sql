-- ===================================================================
-- QUERIES ĐỂ TRACKING VÀ TÍNH COMMISSION CHO KOC
-- ===================================================================

-- 1. Xem tất cả order items được tạo thông qua affiliate links
SELECT
    o.id as order_id,
    o.order_id as order_code,
    oi.id as order_item_id,
    oi.selling_price,
    oi.quantity,
    (oi.selling_price * oi.quantity) as item_total,
    o.order_status,
    o.payment_status,
    o.order_date,

    -- Thông tin sản phẩm
    p.title as product_title,
    p.images as product_image,

    -- Thông tin KOC
    k.id as koc_id,
    k.koc_code,
    c.full_name as koc_name,

    -- Thông tin campaign
    ac.id as campaign_id,
    ac.campaign_code,
    ac.name as campaign_name,
    ac.commission_percent,

    -- Thông tin affiliate link
    al.id as affiliate_link_id,
    al.short_token,
    al.total_click,

    -- Tính commission cho item này
    ROUND((oi.selling_price * oi.quantity) * ac.commission_percent / 100) as commission_amount

FROM order_item oi
JOIN orders o ON oi.order_id = o.id
JOIN affiliate_link al ON oi.affiliate_link_id = al.id
JOIN product p ON oi.product_id = p.id
JOIN koc k ON al.koc_id = k.id
JOIN customer c ON k.customer_id = c.id
JOIN affiliate_campaign ac ON al.campaign_id = ac.id
ORDER BY o.order_date DESC;

-- ===================================================================

-- 2. Dashboard data cho KOC cụ thể
SELECT 
    k.koc_code,
    c.full_name as koc_name,
    
    -- Tổng số orders
    COUNT(o.id) as total_orders,
    
    -- Tổng giá trị orders
    SUM(o.total_selling_price) as total_order_value,
    
    -- Commission pending (orders chưa DELIVERED)
    SUM(CASE 
        WHEN o.order_status != 'DELIVERED' 
        THEN ROUND(o.total_selling_price * ac.commission_percent / 100) 
        ELSE 0 
    END) as pending_commission,
    
    -- Commission confirmed (orders đã DELIVERED)
    SUM(CASE 
        WHEN o.order_status = 'DELIVERED' 
        THEN ROUND(o.total_selling_price * ac.commission_percent / 100) 
        ELSE 0 
    END) as confirmed_commission,
    
    -- Tổng commission
    SUM(ROUND(o.total_selling_price * ac.commission_percent / 100)) as total_commission
    
FROM koc k
JOIN customer c ON k.customer_id = c.id
LEFT JOIN affiliate_link al ON k.id = al.koc_id
LEFT JOIN orders o ON al.id = o.affiliate_link_id
LEFT JOIN affiliate_campaign ac ON al.campaign_id = ac.id
WHERE k.id = ? -- Parameter: KOC ID
GROUP BY k.id, k.koc_code, c.full_name;

-- ===================================================================

-- 3. Chi tiết commission theo từng order của KOC
SELECT 
    o.id as order_id,
    o.order_id as order_code,
    o.order_date,
    o.order_status,
    o.payment_status,
    o.total_selling_price as order_value,
    
    -- Campaign info
    ac.name as campaign_name,
    ac.commission_percent,
    
    -- Product info (nếu có)
    p.title as product_title,
    p.images as product_image,
    
    -- Commission calculation
    ROUND(o.total_selling_price * ac.commission_percent / 100) as commission_amount,
    
    -- Commission status
    CASE 
        WHEN o.order_status = 'CANCELLED' THEN 'CANCELLED'
        WHEN o.order_status = 'DELIVERED' THEN 'CONFIRMED'
        ELSE 'PENDING'
    END as commission_status
    
FROM orders o
JOIN affiliate_link al ON o.affiliate_link_id = al.id
JOIN affiliate_campaign ac ON al.campaign_id = ac.id
LEFT JOIN product p ON al.product_id = p.id
WHERE al.koc_id = ? -- Parameter: KOC ID
ORDER BY o.order_date DESC;

-- ===================================================================

-- 4. Thống kê commission theo tháng cho KOC
SELECT 
    YEAR(o.order_date) as year,
    MONTH(o.order_date) as month,
    COUNT(o.id) as total_orders,
    SUM(o.total_selling_price) as total_order_value,
    SUM(ROUND(o.total_selling_price * ac.commission_percent / 100)) as total_commission,
    
    -- Commission theo status
    SUM(CASE 
        WHEN o.order_status = 'DELIVERED' 
        THEN ROUND(o.total_selling_price * ac.commission_percent / 100) 
        ELSE 0 
    END) as confirmed_commission,
    
    SUM(CASE 
        WHEN o.order_status != 'DELIVERED' AND o.order_status != 'CANCELLED'
        THEN ROUND(o.total_selling_price * ac.commission_percent / 100) 
        ELSE 0 
    END) as pending_commission
    
FROM orders o
JOIN affiliate_link al ON o.affiliate_link_id = al.id
JOIN affiliate_campaign ac ON al.campaign_id = ac.id
WHERE al.koc_id = ? -- Parameter: KOC ID
GROUP BY YEAR(o.order_date), MONTH(o.order_date)
ORDER BY year DESC, month DESC;

-- ===================================================================

-- 5. Top KOCs theo commission (cho admin dashboard)
SELECT 
    k.id as koc_id,
    k.koc_code,
    c.full_name as koc_name,
    c.email,
    
    COUNT(o.id) as total_orders,
    SUM(o.total_selling_price) as total_order_value,
    SUM(ROUND(o.total_selling_price * ac.commission_percent / 100)) as total_commission,
    
    -- Commission đã confirmed
    SUM(CASE 
        WHEN o.order_status = 'DELIVERED' 
        THEN ROUND(o.total_selling_price * ac.commission_percent / 100) 
        ELSE 0 
    END) as confirmed_commission
    
FROM koc k
JOIN customer c ON k.customer_id = c.id
LEFT JOIN affiliate_link al ON k.id = al.koc_id
LEFT JOIN orders o ON al.id = o.affiliate_link_id
LEFT JOIN affiliate_campaign ac ON al.campaign_id = ac.id
GROUP BY k.id, k.koc_code, c.full_name, c.email
HAVING total_orders > 0
ORDER BY confirmed_commission DESC
LIMIT 10;

-- ===================================================================

-- 6. Tìm orders cần cập nhật commission status
-- (Chạy định kỳ để cập nhật commission khi order status thay đổi)
SELECT 
    o.id as order_id,
    o.order_status,
    al.koc_id,
    ROUND(o.total_selling_price * ac.commission_percent / 100) as commission_amount
    
FROM orders o
JOIN affiliate_link al ON o.affiliate_link_id = al.id
JOIN affiliate_campaign ac ON al.campaign_id = ac.id
WHERE o.affiliate_link_id IS NOT NULL
  AND o.order_status IN ('DELIVERED', 'CANCELLED')
  AND NOT EXISTS (
      SELECT 1 FROM affiliate_commission acom 
      WHERE acom.order_id = o.id
  );
