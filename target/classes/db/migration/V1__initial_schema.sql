-- ============================================================
-- V1__initial_schema.sql
-- FMCG eCommerce - Initial Database Schema
-- ============================================================

-- Enable pgcrypto extension (optional, for UUID generation)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- USERS TABLE
-- ============================================================
CREATE TABLE users (
    id            BIGSERIAL    PRIMARY KEY,
    email         VARCHAR(255) UNIQUE,
    mobile        VARCHAR(20)  UNIQUE,
    name          VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255),
    role          VARCHAR(50)  NOT NULL DEFAULT 'CUSTOMER',
    status        VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    fraud_score   INTEGER      DEFAULT 0,
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email  ON users(email);
CREATE INDEX idx_users_mobile ON users(mobile);
CREATE INDEX idx_users_role   ON users(role);
CREATE INDEX idx_users_status ON users(status);

-- ============================================================
-- ADDRESSES TABLE
-- ============================================================
CREATE TABLE addresses (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    label      VARCHAR(50)  DEFAULT 'Home',
    line1      VARCHAR(255) NOT NULL,
    line2      VARCHAR(255),
    city       VARCHAR(100) NOT NULL,
    state      VARCHAR(100) NOT NULL,
    pincode    VARCHAR(10)  NOT NULL,
    lat        DECIMAL(10, 8),
    lng        DECIMAL(11, 8),
    is_default BOOLEAN      DEFAULT FALSE,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_addresses_user_id ON addresses(user_id);

-- ============================================================
-- OTP SESSIONS TABLE
-- ============================================================
CREATE TABLE otp_sessions (
    id         BIGSERIAL    PRIMARY KEY,
    identifier VARCHAR(255) NOT NULL,
    otp_hash   VARCHAR(255) NOT NULL,
    channel    VARCHAR(20)  DEFAULT 'EMAIL',
    expires_at TIMESTAMP    NOT NULL,
    attempts   INTEGER      DEFAULT 0,
    used       BOOLEAN      DEFAULT FALSE,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_otp_identifier ON otp_sessions(identifier);
CREATE INDEX idx_otp_expires    ON otp_sessions(expires_at);

-- ============================================================
-- AUTH TOKENS TABLE
-- ============================================================
CREATE TABLE auth_tokens (
    id            BIGSERIAL    PRIMARY KEY,
    user_id       BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token VARCHAR(512) NOT NULL UNIQUE,
    device_id     VARCHAR(255),
    expires_at    TIMESTAMP    NOT NULL,
    revoked       BOOLEAN      DEFAULT FALSE,
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_auth_tokens_refresh ON auth_tokens(refresh_token);
CREATE INDEX idx_auth_tokens_user    ON auth_tokens(user_id);

-- ============================================================
-- CATEGORIES TABLE
-- ============================================================
CREATE TABLE categories (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    parent_id   BIGINT       REFERENCES categories(id) ON DELETE SET NULL,
    banner_url  VARCHAR(500),
    gst_rate    DECIMAL(5,2) DEFAULT 5.00,
    sort_order  INTEGER      DEFAULT 0,
    is_active   BOOLEAN      DEFAULT TRUE,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_categories_parent ON categories(parent_id);
CREATE INDEX idx_categories_slug   ON categories(slug);

-- ============================================================
-- PRODUCTS TABLE
-- ============================================================
CREATE TABLE products (
    id                BIGSERIAL     PRIMARY KEY,
    sku               VARCHAR(100)  NOT NULL UNIQUE,
    barcode           VARCHAR(100)  UNIQUE,
    title             VARCHAR(500)  NOT NULL,
    description       TEXT,
    short_description VARCHAR(500),
    brand             VARCHAR(255),
    category_id       BIGINT        REFERENCES categories(id) ON DELETE SET NULL,
    price             DECIMAL(10,2) NOT NULL DEFAULT 0,
    mrp               DECIMAL(10,2) DEFAULT 0,
    cost_price        DECIMAL(10,2) DEFAULT 0,
    tax_rate          DECIMAL(5,2)  DEFAULT 5.00,
    unit              VARCHAR(50),
    weight            VARCHAR(50),
    status            VARCHAR(50)   DEFAULT 'ACTIVE',
    tags              TEXT,
    warehouse         VARCHAR(255),
    supplier          VARCHAR(255),
    created_at        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_sku      ON products(sku);
CREATE INDEX idx_products_barcode  ON products(barcode);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_status   ON products(status);
CREATE INDEX idx_products_brand    ON products(brand);
CREATE INDEX idx_products_title    ON products USING gin(to_tsvector('english', title));

-- ============================================================
-- PRODUCT IMAGES TABLE
-- ============================================================
CREATE TABLE product_images (
    id          BIGSERIAL    PRIMARY KEY,
    product_id  BIGINT       NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    url         VARCHAR(500) NOT NULL,
    alt         VARCHAR(255),
    is_primary  BOOLEAN      DEFAULT FALSE,
    sort_order  INTEGER      DEFAULT 0,
    uploaded_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_product_images_product ON product_images(product_id);

-- ============================================================
-- WAREHOUSES TABLE
-- ============================================================
CREATE TABLE warehouses (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    type       VARCHAR(50)  DEFAULT 'WAREHOUSE',
    address    TEXT,
    lat        DECIMAL(10,8),
    lng        DECIMAL(11,8),
    is_active  BOOLEAN      DEFAULT TRUE,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- INVENTORY TABLE
-- ============================================================
CREATE TABLE inventory (
    id            BIGSERIAL PRIMARY KEY,
    product_id    BIGINT    NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    warehouse_id  BIGINT    REFERENCES warehouses(id) ON DELETE SET NULL,
    qty_available INTEGER   DEFAULT 0,
    qty_reserved  INTEGER   DEFAULT 0,
    safety_stock  INTEGER   DEFAULT 0,
    reorder_point INTEGER   DEFAULT 10,
    batch_number  VARCHAR(100),
    expiry_date   DATE,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, warehouse_id)
);

CREATE INDEX idx_inventory_product   ON inventory(product_id);
CREATE INDEX idx_inventory_warehouse ON inventory(warehouse_id);
CREATE INDEX idx_inventory_low_stock ON inventory(qty_available);

-- ============================================================
-- STOCK MOVEMENTS TABLE
-- ============================================================
CREATE TABLE stock_movements (
    id            BIGSERIAL    PRIMARY KEY,
    product_id    BIGINT       NOT NULL REFERENCES products(id),
    warehouse_id  BIGINT       REFERENCES warehouses(id),
    movement_type VARCHAR(50)  NOT NULL,
    qty           INTEGER      NOT NULL,
    reason        TEXT,
    created_by    VARCHAR(255),
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stock_movements_product ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_date    ON stock_movements(created_at);

-- ============================================================
-- COUPONS TABLE
-- ============================================================
CREATE TABLE coupons (
    id             BIGSERIAL     PRIMARY KEY,
    code           VARCHAR(50)   NOT NULL UNIQUE,
    type           VARCHAR(50)   NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    discount_type  VARCHAR(50)   DEFAULT 'PERCENTAGE',
    min_order      DECIMAL(10,2) DEFAULT 0,
    max_uses       INTEGER       DEFAULT -1,
    used_count     INTEGER       DEFAULT 0,
    valid_from     TIMESTAMP,
    valid_until    TIMESTAMP,
    is_active      BOOLEAN       DEFAULT TRUE,
    created_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_coupons_code   ON coupons(code);
CREATE INDEX idx_coupons_active ON coupons(is_active, valid_until);

-- ============================================================
-- PROMOTIONS TABLE
-- ============================================================
CREATE TABLE promotions (
    id             BIGSERIAL     PRIMARY KEY,
    name           VARCHAR(255)  NOT NULL,
    type           VARCHAR(50)   NOT NULL,
    description    TEXT,
    discount_value DECIMAL(10,2),
    discount_type  VARCHAR(50),
    start_date     TIMESTAMP,
    end_date       TIMESTAMP,
    status         VARCHAR(50)   DEFAULT 'ACTIVE',
    usage_count    INTEGER       DEFAULT 0,
    max_uses       INTEGER       DEFAULT -1,
    min_order      DECIMAL(10,2) DEFAULT 0,
    created_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- CARTS TABLE
-- ============================================================
CREATE TABLE carts (
    id                BIGSERIAL     PRIMARY KEY,
    user_id           BIGINT        NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    applied_coupon_id BIGINT        REFERENCES coupons(id) ON DELETE SET NULL,
    coupon_discount   DECIMAL(10,2) DEFAULT 0,
    created_at        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_carts_user ON carts(user_id);

-- ============================================================
-- CART ITEMS TABLE
-- ============================================================
CREATE TABLE cart_items (
    id         BIGSERIAL     PRIMARY KEY,
    cart_id    BIGINT        NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id BIGINT        NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku        VARCHAR(100),
    qty        INTEGER       NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    added_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(cart_id, product_id)
);

CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);

-- ============================================================
-- ORDERS TABLE
-- ============================================================
CREATE TABLE orders (
    id                        BIGSERIAL     PRIMARY KEY,
    order_number              VARCHAR(50)   NOT NULL UNIQUE,
    user_id                   BIGINT        NOT NULL REFERENCES users(id),
    status                    VARCHAR(50)   NOT NULL DEFAULT 'PENDING',
    subtotal                  DECIMAL(10,2) NOT NULL DEFAULT 0,
    discount_amount           DECIMAL(10,2) DEFAULT 0,
    delivery_fee              DECIMAL(10,2) DEFAULT 0,
    tax_amount                DECIMAL(10,2) DEFAULT 0,
    total                     DECIMAL(10,2) NOT NULL DEFAULT 0,
    payment_method            VARCHAR(50),
    payment_status            VARCHAR(50)   DEFAULT 'PENDING',
    address_id                BIGINT        REFERENCES addresses(id) ON DELETE SET NULL,
    delivery_address_snapshot TEXT,
    applied_coupon_id         BIGINT        REFERENCES coupons(id) ON DELETE SET NULL,
    delivery_partner_name     VARCHAR(255),
    cancellation_reason       TEXT,
    created_at                TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_user    ON orders(user_id);
CREATE INDEX idx_orders_status  ON orders(status);
CREATE INDEX idx_orders_number  ON orders(order_number);
CREATE INDEX idx_orders_created ON orders(created_at);
CREATE INDEX idx_orders_payment ON orders(payment_status);

-- ============================================================
-- ORDER ITEMS TABLE
-- ============================================================
CREATE TABLE order_items (
    id                     BIGSERIAL     PRIMARY KEY,
    order_id               BIGINT        NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id             BIGINT        REFERENCES products(id) ON DELETE SET NULL,
    product_title          VARCHAR(500)  NOT NULL,
    sku                    VARCHAR(100),
    qty                    INTEGER       NOT NULL,
    unit_price             DECIMAL(10,2) NOT NULL,
    discount               DECIMAL(10,2) DEFAULT 0,
    substituted_product_id BIGINT        REFERENCES products(id) ON DELETE SET NULL
);

CREATE INDEX idx_order_items_order   ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);

-- ============================================================
-- ORDER STATUS HISTORY TABLE
-- ============================================================
CREATE TABLE order_status_history (
    id         BIGSERIAL    PRIMARY KEY,
    order_id   BIGINT       NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    status     VARCHAR(50)  NOT NULL,
    changed_by VARCHAR(255),
    notes      TEXT,
    changed_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_history_order ON order_status_history(order_id);

-- ============================================================
-- LOYALTY ACCOUNTS TABLE
-- ============================================================
CREATE TABLE loyalty_accounts (
    id              BIGSERIAL   PRIMARY KEY,
    user_id         BIGINT      NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    points_balance  INTEGER     DEFAULT 0,
    tier            VARCHAR(50) DEFAULT 'SILVER',
    tier_updated_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_loyalty_user ON loyalty_accounts(user_id);

-- ============================================================
-- LOYALTY TRANSACTIONS TABLE
-- ============================================================
CREATE TABLE loyalty_transactions (
    id            BIGSERIAL    PRIMARY KEY,
    user_id       BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    order_id      BIGINT,
    action        VARCHAR(50)  NOT NULL,
    points_earned INTEGER      DEFAULT 0,
    points_burned INTEGER      DEFAULT 0,
    description   VARCHAR(500),
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_loyalty_tx_user ON loyalty_transactions(user_id);

-- ============================================================
-- NOTIFICATIONS TABLE
-- ============================================================
CREATE TABLE notifications (
    id           BIGSERIAL    PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title        VARCHAR(255) NOT NULL,
    message      TEXT         NOT NULL,
    type         VARCHAR(50)  DEFAULT 'SYSTEM',
    reference_id VARCHAR(100),
    is_read      BOOLEAN      DEFAULT FALSE,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user   ON notifications(user_id);
CREATE INDEX idx_notifications_unread ON notifications(user_id, is_read);

-- ============================================================
-- SEED DATA: Default admin user (password: Admin@123)
-- BCrypt hash for 'Admin@123'
-- ============================================================
INSERT INTO users (email, name, password_hash, role, status)
VALUES (
    'admin@fmcg.com',
    'Super Admin',
    '$2a$10$rBzOzAXq0P9V6qJKFGCT7.O6sNqmLmkx9ULJj7gVvMKVAh4fRCJE2',
    'ADMIN',
    'ACTIVE'
)
ON CONFLICT (email) DO NOTHING;

-- ============================================================
-- SEED DATA: Sample warehouses
-- ============================================================
INSERT INTO warehouses (name, type, address, is_active) VALUES
('Mumbai Hub',      'WAREHOUSE', 'Andheri East, Mumbai, MH',   true),
('Delhi Central',   'WAREHOUSE', 'Connaught Place, New Delhi',  true),
('Bangalore Store', 'STORE',     'Koramangala, Bangalore, KA',  true)
ON CONFLICT DO NOTHING;

-- ============================================================
-- SEED DATA: Root categories
-- ============================================================
INSERT INTO categories (name, slug, description, gst_rate, sort_order, is_active) VALUES
('Groceries',           'groceries',          'Staples, grains, pulses, oils',           5.0,  1, true),
('Dairy & Eggs',        'dairy-eggs',         'Milk, cheese, butter, eggs',              5.0,  2, true),
('Fruits & Vegetables', 'fruits-vegetables',  'Fresh farm produce',                      0.0,  3, true),
('Beverages',           'beverages',          'Juices, water, soft drinks, tea, coffee', 12.0, 4, true),
('Snacks',              'snacks',             'Chips, biscuits, namkeen',                12.0, 5, true),
('Health & Wellness',   'health-wellness',    'Vitamins, supplements, organic',          5.0,  6, true),
('Personal Care',       'personal-care',      'Shampoo, soap, skincare',                 18.0, 7, true),
('Household',           'household',          'Cleaning, detergents, kitchen items',     18.0, 8, true)
ON CONFLICT (slug) DO NOTHING;
