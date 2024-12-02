-- Ensure ROLE_ENTERPRISE_OWNER exists
INSERT INTO roles (name)
SELECT 'ROLE_ENTERPRISE_OWNER'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'ROLE_ENTERPRISE_OWNER'
);

-- Update old role names if they exist
UPDATE roles SET name = 'ROLE_ENTERPRISE_OWNER' WHERE name = 'OWNER';
UPDATE roles SET name = 'ROLE_ENTERPRISE_OWNER' WHERE name = 'ENTERPRISE_OWNER';

-- Ensure all necessary permissions exist
INSERT INTO permissions (name)
SELECT permission
FROM unnest(ARRAY[
    'VIEW_ENTERPRISE',
    'MANAGE_ENTERPRISE',
    'VIEW_INVENTORY',
    'MANAGE_INVENTORY',
    'VIEW_REQUESTS',
    'MANAGE_REQUESTS',
    'VIEW_DEPARTMENTS',
    'MANAGE_DEPARTMENTS',
    'VIEW_WAREHOUSES',
    'MANAGE_WAREHOUSES'
]) AS permission
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE name = permission
);

-- Grant all necessary permissions to ROLE_ENTERPRISE_OWNER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ENTERPRISE_OWNER'
AND p.name IN (
    'VIEW_ENTERPRISE',
    'MANAGE_ENTERPRISE',
    'VIEW_INVENTORY',
    'MANAGE_INVENTORY',
    'VIEW_REQUESTS',
    'MANAGE_REQUESTS',
    'VIEW_DEPARTMENTS',
    'MANAGE_DEPARTMENTS',
    'VIEW_WAREHOUSES',
    'MANAGE_WAREHOUSES'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
); 