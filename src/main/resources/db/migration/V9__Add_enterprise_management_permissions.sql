-- Add enterprise management permissions
INSERT INTO permissions (name) VALUES
    ('VIEW_ENTERPRISE'),
    ('MANAGE_ENTERPRISE');

-- Assign permissions to OWNER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'OWNER' AND p.name IN ('VIEW_ENTERPRISE', 'MANAGE_ENTERPRISE');

-- Assign permissions to ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN' AND p.name IN ('VIEW_ENTERPRISE', 'MANAGE_ENTERPRISE'); 