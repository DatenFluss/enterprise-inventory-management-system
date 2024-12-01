-- Base tables
CREATE TABLE enterprises (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    contact_email VARCHAR(255) UNIQUE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(100),
    active BOOLEAN DEFAULT TRUE,
    enterprise_id BIGINT,
    role_id BIGINT NOT NULL,
    manager_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (enterprise_id) REFERENCES enterprises(id) ON DELETE SET NULL,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT,
    FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Initial data
INSERT INTO roles (name) VALUES
    ('UNAFFILIATED'),
    ('OWNER'),
    ('MANAGER'),
    ('EMPLOYEE'),
    ('ADMIN');

INSERT INTO permissions (name) VALUES
    ('ACCESS_ADMIN_PANEL'),
    ('PROMOTE_USER'),
    ('ACCESS_ALL_INVENTORY'),
    ('DEACTIVATE_USER'),
    ('REQUEST_ITEM'),
    ('VIEW_AVAILABLE_ITEMS'),
    ('PERFORM_ALL_OPERATIONS'),
    ('VIEW_DEPARTMENTS'),
    ('MANAGE_DEPARTMENTS'),
    ('VIEW_WAREHOUSES'),
    ('MANAGE_WAREHOUSES');

-- Assign permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'OWNER' AND p.name IN 
    ('ACCESS_ADMIN_PANEL', 'PROMOTE_USER', 'ACCESS_ALL_INVENTORY',
     'VIEW_DEPARTMENTS', 'MANAGE_DEPARTMENTS', 'VIEW_WAREHOUSES', 'MANAGE_WAREHOUSES');

-- Create indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email); 