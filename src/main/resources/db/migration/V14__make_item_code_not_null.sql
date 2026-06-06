-- 1. Backfill any existing products that have a NULL item_code to prevent constraint violations
UPDATE products 
SET item_code = 'ITEM-' || id 
WHERE item_code IS NULL;

-- 2. Enforce the NOT NULL constraint on the column
ALTER TABLE products ALTER COLUMN item_code SET NOT NULL;
