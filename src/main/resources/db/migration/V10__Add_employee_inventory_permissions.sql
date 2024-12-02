-- Add VIEW_INVENTORY permission if it doesn't exist
INSERT INTO permissions (name)
SELECT 'VIEW_INVENTORY'
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE name = 'VIEW_INVENTORY'
);

-- Add VIEW_REQUESTS permission if it doesn't exist
INSERT INTO permissions (name)
SELECT 'VIEW_REQUESTS'
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE name = 'VIEW_REQUESTS'
);

-- Grant permissions to EMPLOYEE role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'EMPLOYEE'
AND p.name IN ('VIEW_INVENTORY', 'VIEW_REQUESTS')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
); 