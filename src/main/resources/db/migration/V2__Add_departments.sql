CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    enterprise_id BIGINT NOT NULL,
    manager_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (enterprise_id) REFERENCES enterprises(id),
    FOREIGN KEY (manager_id) REFERENCES users(id),
    UNIQUE (name, enterprise_id)
);

ALTER TABLE users
ADD COLUMN department_id BIGINT,
ADD CONSTRAINT fk_user_department
    FOREIGN KEY (department_id) REFERENCES departments(id); 