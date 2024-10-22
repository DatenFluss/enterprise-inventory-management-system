DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS enterprises CASCADE;
DROP TABLE IF EXISTS inventory_items CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;

-- 1. Create the 'enterprises' table
CREATE TABLE IF NOT EXISTS enterprises (
                             id BIGSERIAL PRIMARY KEY,
                             name VARCHAR(255) NOT NULL,
                             address VARCHAR(255),
                             contact_email VARCHAR(255) UNIQUE,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Create the 'roles' table
CREATE TABLE IF NOT EXISTS roles (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE
);

-- 3. Create the 'permissions' table
CREATE TABLE IF NOT EXISTS permissions (
                             id BIGSERIAL PRIMARY KEY,
                             name VARCHAR(100) NOT NULL UNIQUE
);

-- 4. Create the 'role_permissions' table
CREATE TABLE IF NOT EXISTS role_permissions (
                                  role_id BIGINT NOT NULL,
                                  permission_id BIGINT NOT NULL,
                                  PRIMARY KEY (role_id, permission_id),
                                  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
                                  FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- 5. Create the 'users' table
CREATE TABLE IF NOT EXISTS users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       active BOOLEAN DEFAULT TRUE,
                       enterprise_id BIGINT,
                       role_id BIGINT NOT NULL,
                       FOREIGN KEY (enterprise_id) REFERENCES enterprises(id) ON DELETE SET NULL,
                       FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Create the 'inventory_items' table
CREATE TABLE IF NOT EXISTS inventory_items (
                                 id BIGSERIAL PRIMARY KEY,
                                 name VARCHAR(255) NOT NULL,
                                 description TEXT,
                                 quantity INT NOT NULL,
                                 enterprise_id BIGINT NOT NULL,
                                 FOREIGN KEY (enterprise_id) REFERENCES enterprises(id) ON DELETE CASCADE,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. Create the 'item_requests' table
CREATE TABLE IF NOT EXISTS item_requests (
                               id BIGSERIAL PRIMARY KEY,
                               requester_id BIGINT NOT NULL,
                               inventory_item_id BIGINT NOT NULL,
                               quantity INT NOT NULL,
                               status VARCHAR(20) NOT NULL,
                               comments TEXT,
                               request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
                               FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_inventory_items_name ON inventory_items(name);

