-- Add MANAGE_WAREHOUSE permission if it doesn't exist
INSERT INTO permissions (name)
SELECT 'MANAGE_WAREHOUSE'
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE name = 'MANAGE_WAREHOUSE'
);

-- Grant MANAGE_WAREHOUSE permission to ROLE_WAREHOUSE_OPERATOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_WAREHOUSE_OPERATOR'
AND p.name = 'MANAGE_WAREHOUSE'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
); 