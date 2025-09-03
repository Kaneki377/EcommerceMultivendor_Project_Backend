-- Fix OrderItem table structure
-- Xóa commission_amount column vì chúng ta dùng bảng AffiliateCommission riêng

-- Kiểm tra xem column có tồn tại không
SELECT COLUMN_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'yt_ecommerce'
  AND TABLE_NAME = 'order_item'
  AND COLUMN_NAME = 'commission_amount';

-- Nếu có thì xóa
ALTER TABLE order_item DROP COLUMN IF EXISTS commission_amount;

-- Kiểm tra cấu trúc bảng sau khi xóa
DESCRIBE order_item;
