-- Update permission names to match controller checks
UPDATE permissions SET name = 'VIEW_INVENTORY' WHERE name = 'VIEW_ITEMS';
UPDATE permissions SET name = 'MANAGE_INVENTORY' WHERE name = 'MANAGE_ITEMS';

-- Re-grant permissions to roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_EMPLOYEE'
AND p.name = 'VIEW_INVENTORY'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_MANAGER'
AND p.name IN ('VIEW_INVENTORY', 'MANAGE_INVENTORY')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ENTERPRISE_OWNER'
AND p.name IN ('VIEW_INVENTORY', 'MANAGE_INVENTORY')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
AND p.name IN ('VIEW_INVENTORY', 'MANAGE_INVENTORY')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
); 