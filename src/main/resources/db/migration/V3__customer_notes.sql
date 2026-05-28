-- ============================================================
-- V3__customer_notes.sql
-- Add customer_notes table for admin notes on customers
-- ============================================================

CREATE TABLE IF NOT EXISTS customer_notes (
    id          BIGSERIAL    PRIMARY KEY,
    customer_id BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    note        TEXT         NOT NULL,
    created_by  VARCHAR(255),
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_customer_notes_customer ON customer_notes(customer_id);
CREATE INDEX idx_customer_notes_created  ON customer_notes(created_at);
