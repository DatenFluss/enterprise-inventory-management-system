CREATE TABLE item_requests (
    id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    source_warehouse_id BIGINT NOT NULL,
    target_department_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    comments TEXT,
    response_comments TEXT,
    processor_id BIGINT,
    request_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_date TIMESTAMP,
    FOREIGN KEY (requester_id) REFERENCES users(id),
    FOREIGN KEY (source_warehouse_id) REFERENCES warehouses(id),
    FOREIGN KEY (target_department_id) REFERENCES departments(id),
    FOREIGN KEY (processor_id) REFERENCES users(id)
);

CREATE TABLE request_items (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL,
    inventory_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    comments TEXT,
    FOREIGN KEY (request_id) REFERENCES item_requests(id) ON DELETE CASCADE,
    FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id)
);

-- Create timestamp update function and triggers
CREATE OR REPLACE FUNCTION update_timestamp()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for users table
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE PROCEDURE update_timestamp();

-- Trigger for inventory_items table
CREATE TRIGGER trg_inventory_items_updated_at
    BEFORE UPDATE ON inventory_items
    FOR EACH ROW EXECUTE PROCEDURE update_timestamp();

-- Trigger for departments table
CREATE TRIGGER trg_departments_updated_at
    BEFORE UPDATE ON departments
    FOR EACH ROW EXECUTE PROCEDURE update_timestamp();

-- Trigger for warehouses table
CREATE TRIGGER trg_warehouses_updated_at
    BEFORE UPDATE ON warehouses
    FOR EACH ROW EXECUTE PROCEDURE update_timestamp(); 