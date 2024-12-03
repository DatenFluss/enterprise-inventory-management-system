-- Add request-related permissions if they don't exist
INSERT INTO permissions (name)
SELECT permission
FROM unnest(ARRAY[
    'VIEW_REQUESTS',
    'MANAGE_REQUESTS',
    'VIEW_PENDING_REQUESTS',
    'VIEW_OWN_REQUESTS'
]) AS permission
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE name = permission
);

-- Grant permissions to ROLE_MANAGER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_MANAGER'
AND p.name IN ('VIEW_REQUESTS', 'MANAGE_REQUESTS', 'VIEW_PENDING_REQUESTS')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- Grant permissions to ROLE_WAREHOUSE_OPERATOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_WAREHOUSE_OPERATOR'
AND p.name IN ('VIEW_REQUESTS', 'MANAGE_REQUESTS', 'VIEW_PENDING_REQUESTS')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- Grant permissions to ROLE_EMPLOYEE
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_EMPLOYEE'
AND p.name IN ('VIEW_REQUESTS', 'VIEW_OWN_REQUESTS')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- Grant permissions to ROLE_ENTERPRISE_OWNER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ENTERPRISE_OWNER'
AND p.name IN ('VIEW_REQUESTS', 'MANAGE_REQUESTS', 'VIEW_PENDING_REQUESTS')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
); 