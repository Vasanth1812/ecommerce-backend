-- ============================================================
-- V2__sample_products.sql
-- FMCG eCommerce - Sample Products, Inventory & Coupons
-- ============================================================

-- ============================================================
-- SAMPLE PRODUCTS (10 products across categories)
-- category_id references:
--   1 = Groceries, 2 = Dairy & Eggs, 3 = Fruits & Vegetables,
--   4 = Beverages, 5 = Snacks, 6 = Health & Wellness
-- ============================================================
INSERT INTO products (
    sku, barcode, title, description, brand,
    category_id, price, mrp, cost_price, tax_rate,
    unit, weight, status
) VALUES
(
    'RICE-BAS-001', '8901234567890',
    'Organic Basmati Rice',
    'Premium long grain basmati rice, aged for superior flavour and aroma.',
    'Fortune', 1, 499.00, 699.00, 380.00, 5.0, 'kg', '5 kg', 'ACTIVE'
),
(
    'DAIRY-MLK-001', '8901234567891',
    'Full Cream Milk 1L',
    'Fresh full cream pasteurized milk, rich in calcium and protein.',
    'Amul', 2, 68.00, 75.00, 48.00, 5.0, 'L', '1 L', 'ACTIVE'
),
(
    'FRUIT-APL-001', '8901234567892',
    'Fresh Red Apples',
    'Fresh Himalayan red apples, hand-picked and naturally ripened.',
    'Local Farm', 3, 199.00, 249.00, 140.00, 0.0, 'kg', '1 kg', 'ACTIVE'
),
(
    'BEV-COF-001', '8901234567893',
    'Cold Brew Coffee 250ml',
    'Ready to drink cold brew coffee, slow-steeped for 12 hours.',
    'Bru', 4, 249.00, 299.00, 170.00, 12.0, 'bottle', '250 ml', 'ACTIVE'
),
(
    'SNK-ALM-001', '8901234567894',
    'Premium Almonds 250g',
    'California roasted almonds lightly salted, a healthy protein-rich snack.',
    'Happilo', 5, 349.00, 429.00, 240.00, 12.0, 'pack', '250 g', 'ACTIVE'
),
(
    'HEALTH-HNY-001', '8901234567895',
    'Natural Honey 500g',
    '100% pure natural honey, no added sugar or preservatives.',
    'Dabur', 6, 349.00, 449.00, 230.00, 5.0, 'bottle', '500 ml', 'ACTIVE'
),
(
    'DAIRY-YOG-001', '8901234567896',
    'Greek Yogurt 400g',
    'Thick creamy Greek yogurt, high protein, low sugar, probiotic-rich.',
    'Epigamia', 2, 129.00, 159.00, 89.00, 5.0, 'cup', '400 g', 'ACTIVE'
),
(
    'BEV-GT-001', '8901234567897',
    'Green Tea 25 Bags',
    'Premium green tea bags, packed with antioxidants for a refreshing brew.',
    'Tetley', 4, 199.00, 249.00, 130.00, 12.0, 'pack', '25 bags', 'ACTIVE'
),
(
    'DAIRY-BUT-001', '8901234567898',
    'Salted Butter 100g',
    'Fresh salted butter made from pasteurized cream, rich and creamy.',
    'Amul', 2, 55.00, 65.00, 38.00, 5.0, 'pack', '100 g', 'ACTIVE'
),
(
    'BEV-OJ-001', '8901234567899',
    'Fresh Orange Juice 1L',
    '100% fresh squeezed orange juice, no added sugar or preservatives.',
    'Real', 4, 149.00, 179.00, 98.00, 12.0, 'L', '1 L', 'ACTIVE'
)
ON CONFLICT (sku) DO NOTHING;

-- ============================================================
-- INVENTORY: Add stock for all sample products
-- warehouse_id = 1 references 'Mumbai Hub' seeded in V1
-- ============================================================
INSERT INTO inventory (product_id, warehouse_id, qty_available, qty_reserved, safety_stock, reorder_point)
SELECT p.id, 1, 100, 0, 10, 10
FROM products p
WHERE p.sku IN (
    'RICE-BAS-001',
    'DAIRY-MLK-001',
    'FRUIT-APL-001',
    'BEV-COF-001',
    'SNK-ALM-001',
    'HEALTH-HNY-001',
    'DAIRY-YOG-001',
    'BEV-GT-001',
    'DAIRY-BUT-001',
    'BEV-OJ-001'
)
ON CONFLICT (product_id, warehouse_id) DO NOTHING;

-- ============================================================
-- SAMPLE COUPONS
-- ============================================================
INSERT INTO coupons (
    code, type, discount_value, discount_type,
    min_order, max_uses, valid_from, valid_until, is_active
) VALUES
(
    'WELCOME100', 'FIXED',
    100.00, 'FIXED',
    499.00, 1000,
    NOW(), NOW() + INTERVAL '1 year',
    true
),
(
    'SAVE10', 'PERCENTAGE',
    10.00, 'PERCENTAGE',
    299.00, -1,
    NOW(), NOW() + INTERVAL '1 year',
    true
),
(
    'FREEDEL', 'FREE_DELIVERY',
    0.00, 'FREE_DELIVERY',
    199.00, -1,
    NOW(), NOW() + INTERVAL '6 months',
    true
)
ON CONFLICT (code) DO NOTHING;
