-- Add basic permissions for unaffiliated users
INSERT INTO permissions (name)
SELECT 'VIEW_INVENTORY'
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE name = 'VIEW_INVENTORY'
);

INSERT INTO permissions (name)
SELECT 'VIEW_REQUESTS'
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE name = 'VIEW_REQUESTS'
);

-- Grant permissions to UNAFFILIATED role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'UNAFFILIATED'
AND p.name IN ('VIEW_INVENTORY', 'VIEW_REQUESTS')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
); 