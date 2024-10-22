-- =========================================
-- 1. Insert Permissions
-- =========================================

INSERT INTO permissions (id, name) VALUES (1, 'VIEW_USERS');
INSERT INTO permissions (id, name) VALUES (2, 'MANAGE_USERS');
INSERT INTO permissions (id, name) VALUES (3, 'VIEW_ENTERPRISES');
INSERT INTO permissions (id, name) VALUES (4, 'MANAGE_ENTERPRISES');
INSERT INTO permissions (id, name) VALUES (5, 'VIEW_ITEMS');
INSERT INTO permissions (id, name) VALUES (6, 'MANAGE_ITEMS');
-- Add other permissions as needed

-- =========================================
-- 2. Insert Roles
-- =========================================

INSERT INTO roles (id, name) VALUES (1, 'EMPLOYEE');
INSERT INTO roles (id, name) VALUES (2, 'MANAGER');
INSERT INTO roles (id, name) VALUES (3, 'ENTERPRISE_OWNER');
INSERT INTO roles (id, name) VALUES (4, 'ADMIN');
-- Adjust IDs and RoleName values as needed

-- =========================================
-- 3. Insert Role-Permission Mappings
-- =========================================

-- EMPLOYEE Role Permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 1); -- VIEW_USERS
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 3); -- VIEW_ENTERPRISES
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 5); -- VIEW_ITEMS

-- MANAGER Role Permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES (2, 1); -- VIEW_USERS
INSERT INTO role_permissions (role_id, permission_id) VALUES (2, 2); -- MANAGE_USERS
INSERT INTO role_permissions (role_id, permission_id) VALUES (2, 3); -- VIEW_ENTERPRISES
INSERT INTO role_permissions (role_id, permission_id) VALUES (2, 4); -- MANAGE_ENTERPRISES
INSERT INTO role_permissions (role_id, permission_id) VALUES (2, 5); -- VIEW_ITEMS
INSERT INTO role_permissions (role_id, permission_id) VALUES (2, 6); -- MANAGE_ITEMS

-- ENTERPRISE_OWNER Role Permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES (3, 1); -- VIEW_USERS
INSERT INTO role_permissions (role_id, permission_id) VALUES (3, 2); -- MANAGE_USERS
INSERT INTO role_permissions (role_id, permission_id) VALUES (3, 3); -- VIEW_ENTERPRISES
INSERT INTO role_permissions (role_id, permission_id) VALUES (3, 4); -- MANAGE_ENTERPRISES
INSERT INTO role_permissions (role_id, permission_id) VALUES (3, 5); -- VIEW_ITEMS
INSERT INTO role_permissions (role_id, permission_id) VALUES (3, 6); -- MANAGE_ITEMS

-- ADMIN Role Permissions (Assuming Admin has all permissions)
INSERT INTO role_permissions (role_id, permission_id) VALUES (4, 1);
INSERT INTO role_permissions (role_id, permission_id) VALUES (4, 2);
INSERT INTO role_permissions (role_id, permission_id) VALUES (4, 3);
INSERT INTO role_permissions (role_id, permission_id) VALUES (4, 4);
INSERT INTO role_permissions (role_id, permission_id) VALUES (4, 5);
INSERT INTO role_permissions (role_id, permission_id) VALUES (4, 6);

-- =========================================
-- 4. Insert Initial Users (Optional)
-- =========================================

-- Insert an Admin user (replace password hash with actual hashed password)
INSERT INTO users (id, username, password, email, active, role_id) VALUES
    (1, 'admin', '$2a$10$hashedpassword', 'admin@example.com', true, 4);

-- Insert other initial users as needed

-- =========================================
-- 5. Reset Sequences (If IDs are Manually Set)
-- =========================================

-- Adjust sequences for permissions table
SELECT setval('permissions_id_seq', (SELECT MAX(id) FROM permissions));

-- Adjust sequences for roles table
SELECT setval('roles_id_seq', (SELECT MAX(id) FROM roles));

-- Adjust sequences for users table (if you added users)
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
