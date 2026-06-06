-- Create delivery_partners table
CREATE TABLE delivery_partners (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    vehicle_type VARCHAR(100),
    vehicle_number VARCHAR(100),
    availability_status VARCHAR(50) DEFAULT 'FREE', -- FREE, ASSIGNED, OFFLINE
    current_lat DOUBLE PRECISION,
    current_lng DOUBLE PRECISION,
    last_location_update TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_dp_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Add delivery_partner_id to orders table
ALTER TABLE orders ADD COLUMN delivery_partner_id BIGINT;
ALTER TABLE orders ADD CONSTRAINT fk_order_delivery_partner FOREIGN KEY (delivery_partner_id) REFERENCES delivery_partners(id);
