-- Update role names to include ROLE_ prefix
UPDATE roles SET name = 'ROLE_' || name WHERE name NOT LIKE 'ROLE_%';

-- Update role permissions for the new role names
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_EMPLOYEE'
AND p.name IN ('VIEW_INVENTORY', 'VIEW_REQUESTS')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_MANAGER'
AND p.name IN ('VIEW_INVENTORY', 'MANAGE_INVENTORY', 'VIEW_REQUESTS', 'MANAGE_DEPARTMENT')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ENTERPRISE_OWNER'
AND p.name IN ('VIEW_INVENTORY', 'MANAGE_INVENTORY', 'VIEW_REQUESTS', 'MANAGE_DEPARTMENT', 'MANAGE_WAREHOUSE')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
AND p.name IN ('VIEW_INVENTORY', 'MANAGE_INVENTORY', 'VIEW_REQUESTS', 'MANAGE_DEPARTMENT', 'MANAGE_WAREHOUSE')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
); 