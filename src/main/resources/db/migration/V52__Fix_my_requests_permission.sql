-- Add VIEW_MY_REQUESTS permission if it doesn't exist
INSERT INTO permissions (name)
SELECT 'VIEW_MY_REQUESTS'
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE name = 'VIEW_MY_REQUESTS'
);

-- Get the permission ID
WITH permission_id AS (
    SELECT id FROM permissions WHERE name = 'VIEW_MY_REQUESTS'
),
-- Get role IDs
role_ids AS (
    SELECT id FROM roles 
    WHERE name IN ('ROLE_MANAGER', 'ROLE_EMPLOYEE', 'ROLE_DEPARTMENT_MANAGER')
)
-- Add permission to roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM role_ids r
CROSS JOIN permission_id p
WHERE NOT EXISTS (
    SELECT 1 
    FROM role_permissions rp 
    WHERE rp.role_id = r.id 
    AND rp.permission_id = p.id
); 