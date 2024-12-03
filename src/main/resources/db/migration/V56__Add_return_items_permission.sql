-- Add RETURN_ITEMS permission
INSERT INTO permissions (name)
SELECT 'RETURN_ITEMS'
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE name = 'RETURN_ITEMS'
);

-- Grant RETURN_ITEMS permission to ROLE_EMPLOYEE
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_EMPLOYEE'
AND p.name = 'RETURN_ITEMS'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
); 