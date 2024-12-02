-- Add missing columns
ALTER TABLE inventory_items
    ADD COLUMN IF NOT EXISTS min_quantity INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS price DECIMAL(10,2);

-- Update column constraints
ALTER TABLE inventory_items
    ALTER COLUMN quantity SET DEFAULT 0,
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP; 