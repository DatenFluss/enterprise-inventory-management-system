-- Add VIEW_INVENTORY permission if it doesn't exist
INSERT INTO permissions (name)
SELECT 'VIEW_INVENTORY'
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE name = 'VIEW_INVENTORY'
);

-- Get the permission ID
WITH permission_id AS (
    SELECT id FROM permissions WHERE name = 'VIEW_INVENTORY'
)
-- Assign to ROLE_MANAGER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_MANAGER'
AND p.name = 'VIEW_INVENTORY'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions
    WHERE role_id = r.id AND permission_id = p.id
);

-- Assign to ROLE_EMPLOYEE
WITH permission_id AS (
    SELECT id FROM permissions WHERE name = 'VIEW_INVENTORY'
)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_EMPLOYEE'
AND p.name = 'VIEW_INVENTORY'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions
    WHERE role_id = r.id AND permission_id = p.id
);

-- Assign to ROLE_WAREHOUSE_OPERATOR
WITH permission_id AS (
    SELECT id FROM permissions WHERE name = 'VIEW_INVENTORY'
)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_WAREHOUSE_OPERATOR'
AND p.name = 'VIEW_INVENTORY'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions
    WHERE role_id = r.id AND permission_id = p.id
);

-- Assign to ROLE_ENTERPRISE_OWNER
WITH permission_id AS (
    SELECT id FROM permissions WHERE name = 'VIEW_INVENTORY'
)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ENTERPRISE_OWNER'
AND p.name = 'VIEW_INVENTORY'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions
    WHERE role_id = r.id AND permission_id = p.id
); 