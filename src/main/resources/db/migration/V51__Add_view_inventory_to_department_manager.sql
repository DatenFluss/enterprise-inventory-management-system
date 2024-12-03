-- Add VIEW_INVENTORY permission to ROLE_DEPARTMENT_MANAGER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_DEPARTMENT_MANAGER'
AND p.name = 'VIEW_INVENTORY'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions
    WHERE role_id = r.id AND permission_id = p.id
); 