CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(255) NOT NULL,
    product_name VARCHAR(255),
    product_id BIGINT,
    field VARCHAR(255),
    old_value TEXT,
    new_value TEXT,
    performed_by VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL
);
