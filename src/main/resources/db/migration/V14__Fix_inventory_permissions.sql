-- Update permission names
UPDATE permissions SET name = 'VIEW_INVENTORY' WHERE name = 'VIEW_ITEMS';
UPDATE permissions SET name = 'MANAGE_INVENTORY' WHERE name = 'MANAGE_ITEMS';

-- Add missing permissions if they don't exist
INSERT INTO permissions (name)
SELECT 'VIEW_INVENTORY'
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE name = 'VIEW_INVENTORY'
);

INSERT INTO permissions (name)
SELECT 'MANAGE_INVENTORY'
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE name = 'MANAGE_INVENTORY'
);

INSERT INTO permissions (name)
SELECT 'VIEW_REQUESTS'
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE name = 'VIEW_REQUESTS'
);

INSERT INTO permissions (name)
SELECT 'MANAGE_DEPARTMENT'
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE name = 'MANAGE_DEPARTMENT'
);

INSERT INTO permissions (name)
SELECT 'MANAGE_WAREHOUSE'
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE name = 'MANAGE_WAREHOUSE'
);

-- Update role permissions for EMPLOYEE
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'EMPLOYEE'
AND p.name IN ('VIEW_INVENTORY', 'VIEW_REQUESTS')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- Update role permissions for MANAGER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'MANAGER'
AND p.name IN ('VIEW_INVENTORY', 'MANAGE_INVENTORY', 'VIEW_REQUESTS', 'MANAGE_DEPARTMENT')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- Update role permissions for ENTERPRISE_OWNER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ENTERPRISE_OWNER'
AND p.name IN ('VIEW_INVENTORY', 'MANAGE_INVENTORY', 'VIEW_REQUESTS', 'MANAGE_DEPARTMENT', 'MANAGE_WAREHOUSE')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- Update role permissions for ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ADMIN'
AND p.name IN ('VIEW_INVENTORY', 'MANAGE_INVENTORY', 'VIEW_REQUESTS', 'MANAGE_DEPARTMENT', 'MANAGE_WAREHOUSE')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
); 