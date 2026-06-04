-- Add capacity column to warehouses table
ALTER TABLE warehouses ADD COLUMN capacity INTEGER DEFAULT 1000;

-- Create stock_transfers table
CREATE TABLE stock_transfers (
    id BIGSERIAL PRIMARY KEY,
    transfer_number VARCHAR(255) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL,
    from_warehouse_id BIGINT NOT NULL,
    to_warehouse_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT fk_transfer_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_transfer_from_wh FOREIGN KEY (from_warehouse_id) REFERENCES warehouses(id),
    CONSTRAINT fk_transfer_to_wh FOREIGN KEY (to_warehouse_id) REFERENCES warehouses(id)
);
