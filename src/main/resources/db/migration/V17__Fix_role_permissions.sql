-- Re-grant permissions to roles with correct role names
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

-- Clean up any role-permission mappings for old role names
DELETE FROM role_permissions 
WHERE role_id IN (
    SELECT id FROM roles 
    WHERE name IN ('EMPLOYEE', 'MANAGER', 'ENTERPRISE_OWNER', 'ADMIN')
); 