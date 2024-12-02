-- Add VIEW_ENTERPRISE permission to ROLE_WAREHOUSE_OPERATOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_WAREHOUSE_OPERATOR'
AND p.name = 'VIEW_ENTERPRISE'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
); 