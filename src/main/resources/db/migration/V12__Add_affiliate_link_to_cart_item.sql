-- Add affiliate_link_id column to cart_item table
ALTER TABLE cart_item 
ADD COLUMN affiliate_link_id BIGINT,
ADD CONSTRAINT fk_cart_item_affiliate_link 
    FOREIGN KEY (affiliate_link_id) REFERENCES affiliate_link(id);

-- Create index for better performance
CREATE INDEX idx_cart_item_affiliate_link_id ON cart_item(affiliate_link_id);
