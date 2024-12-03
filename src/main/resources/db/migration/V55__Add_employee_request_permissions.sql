-- Add new permissions
INSERT INTO permissions (name) VALUES
    ('REQUEST_ITEMS'),
    ('VIEW_OWN_ITEM_REQUESTS'),
    ('MANAGE_EMPLOYEE_REQUESTS');

-- Add permissions to employee role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ROLE_EMPLOYEE'
AND p.name IN ('REQUEST_ITEMS', 'VIEW_OWN_ITEM_REQUESTS');

-- Add permission to manager role to handle employee requests
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ROLE_MANAGER'
AND p.name = 'MANAGE_EMPLOYEE_REQUESTS'; 