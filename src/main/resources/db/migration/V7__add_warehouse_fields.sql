-- Add the missing UI fields to the warehouses table
ALTER TABLE warehouses 
    ADD COLUMN used_capacity INTEGER DEFAULT 0,
    ADD COLUMN short_location VARCHAR(255),
    ADD COLUMN city VARCHAR(100),
    ADD COLUMN state VARCHAR(100),
    ADD COLUMN pincode VARCHAR(20),
    ADD COLUMN staff_count INTEGER,
    ADD COLUMN operating_hours VARCHAR(100),
    ADD COLUMN manager_name VARCHAR(255),
    ADD COLUMN contact_number VARCHAR(50);
