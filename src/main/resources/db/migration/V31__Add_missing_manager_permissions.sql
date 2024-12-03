-- Add missing permissions if they don't exist
INSERT INTO permissions (name)
SELECT permission
FROM unnest(ARRAY[
    'VIEW_SUBORDINATES',
    'VIEW_DEPARTMENT_INVITES',
    'VIEW_PENDING_REQUESTS'
]) AS permission
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE name = permission
);

-- Grant missing permissions to ROLE_MANAGER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_MANAGER'
AND p.name IN (
    'VIEW_SUBORDINATES',
    'VIEW_DEPARTMENT_INVITES',
    'VIEW_PENDING_REQUESTS'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
); 