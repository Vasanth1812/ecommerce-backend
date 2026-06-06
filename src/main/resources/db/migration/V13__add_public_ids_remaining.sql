ALTER TABLE inventory ADD COLUMN public_id VARCHAR(36) UNIQUE;
UPDATE inventory SET public_id = CAST(gen_random_uuid() AS VARCHAR) WHERE public_id IS NULL;
