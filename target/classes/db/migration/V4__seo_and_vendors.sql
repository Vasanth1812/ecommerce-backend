-- Create vendors table
CREATE TABLE vendors (
    id BIGSERIAL PRIMARY KEY,
    business_name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(255) NOT NULL,
    gst_number VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    commission_rate DECIMAL(10, 2),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

-- Create product_seo table
CREATE TABLE product_seo (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    meta_title VARCHAR(255),
    meta_description VARCHAR(500),
    slug VARCHAR(255) UNIQUE,
    canonical_url VARCHAR(255),
    og_image VARCHAR(255),
    CONSTRAINT fk_product_seo_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Create product_seo_keywords collection table
CREATE TABLE product_seo_keywords (
    product_seo_id BIGINT NOT NULL,
    keyword VARCHAR(255) NOT NULL,
    CONSTRAINT fk_product_seo_keywords_seo FOREIGN KEY (product_seo_id) REFERENCES product_seo(id) ON DELETE CASCADE
);
