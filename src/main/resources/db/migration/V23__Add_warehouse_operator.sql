ALTER TABLE warehouses
ADD COLUMN operator_id BIGINT,
ADD CONSTRAINT fk_warehouse_operator
    FOREIGN KEY (operator_id)
    REFERENCES users (id);

-- Add WAREHOUSE_OPERATOR role if it doesn't exist
INSERT INTO roles (name)
SELECT 'ROLE_WAREHOUSE_OPERATOR'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'ROLE_WAREHOUSE_OPERATOR'
);

-- Add necessary permissions for warehouse operators
INSERT INTO permissions (name)
VALUES
    ('VIEW_WAREHOUSE_ITEMS'),
    ('MANAGE_WAREHOUSE_ITEMS'),
    ('UPDATE_ITEM_STATUS');

-- Associate permissions with the WAREHOUSE_OPERATOR role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ROLE_WAREHOUSE_OPERATOR'
AND p.name IN ('VIEW_WAREHOUSE_ITEMS', 'MANAGE_WAREHOUSE_ITEMS', 'UPDATE_ITEM_STATUS'); 