-- Add VIEW_OWN_REQUESTS permission if it doesn't exist
INSERT INTO permissions (name)
SELECT 'VIEW_OWN_REQUESTS'
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE name = 'VIEW_OWN_REQUESTS'
);

-- Get the permission ID
WITH permission_id AS (
    SELECT id FROM permissions WHERE name = 'VIEW_OWN_REQUESTS'
),
manager_role_id AS (
    SELECT id FROM roles WHERE name = 'ROLE_MANAGER'
)
-- Add permission to manager role if not already present
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM manager_role_id r
CROSS JOIN permission_id p
WHERE NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id
    AND rp.permission_id = p.id
); 