-- Drop existing indexes if they exist
DROP INDEX IF EXISTS idx_item_requests_requester;
DROP INDEX IF EXISTS idx_item_requests_item;
DROP INDEX IF EXISTS idx_item_requests_department;
DROP INDEX IF EXISTS idx_item_requests_warehouse;
DROP INDEX IF EXISTS idx_item_requests_status;

-- Add new columns if they don't exist
ALTER TABLE item_requests 
    ADD COLUMN IF NOT EXISTS item_id BIGINT REFERENCES inventory_items(id),
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- Create new indexes for better performance
CREATE INDEX IF NOT EXISTS idx_item_requests_requester ON item_requests(requester_id);
CREATE INDEX IF NOT EXISTS idx_item_requests_item ON item_requests(item_id);
CREATE INDEX IF NOT EXISTS idx_item_requests_department ON item_requests(department_id);
CREATE INDEX IF NOT EXISTS idx_item_requests_warehouse ON item_requests(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_item_requests_status ON item_requests(status); 