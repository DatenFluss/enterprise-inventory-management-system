-- Add WAREHOUSE_OPERATOR role
INSERT INTO roles (name) VALUES ('WAREHOUSE_OPERATOR');

-- Assign permissions to WAREHOUSE_OPERATOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'WAREHOUSE_OPERATOR' AND p.name IN 
    ('VIEW_WAREHOUSES', 'MANAGE_WAREHOUSES', 'VIEW_AVAILABLE_ITEMS', 'ACCESS_ALL_INVENTORY'); 