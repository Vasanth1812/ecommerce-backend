-- Add item_code to products table
ALTER TABLE products ADD COLUMN item_code VARCHAR(255) UNIQUE;
