CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    quantity INTEGER NOT NULL DEFAULT 0,
    min_quantity INTEGER NOT NULL DEFAULT 0,
    price DECIMAL(10,2),
    warehouse_id BIGINT REFERENCES warehouses(id),
    department_id BIGINT REFERENCES departments(id),
    enterprise_id BIGINT NOT NULL REFERENCES enterprises(id),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_items_warehouse ON items(warehouse_id);
CREATE INDEX idx_items_department ON items(department_id);
CREATE INDEX idx_items_enterprise ON items(enterprise_id);
CREATE INDEX idx_items_name ON items(name); 