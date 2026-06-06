-- Create marketing_campaigns table
CREATE TABLE marketing_campaigns (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    channels VARCHAR(255) NOT NULL, -- e.g. "EMAIL,PUSH,SMS"
    budget NUMERIC(10, 2),
    status VARCHAR(50) DEFAULT 'DRAFT', -- DRAFT, SCHEDULED, ACTIVE, COMPLETED
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create ab_tests table
CREATE TABLE ab_tests (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    variant_a_name VARCHAR(255) NOT NULL,
    variant_b_name VARCHAR(255) NOT NULL,
    winner VARCHAR(50), -- VARIANT_A, VARIANT_B, INCONCLUSIVE
    status VARCHAR(50) DEFAULT 'RUNNING', -- RUNNING, COMPLETED
    impressions_a INTEGER DEFAULT 0,
    impressions_b INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
