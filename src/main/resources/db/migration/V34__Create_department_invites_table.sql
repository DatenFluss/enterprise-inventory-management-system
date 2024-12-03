CREATE TABLE department_invites (
    id BIGSERIAL PRIMARY KEY,
    department_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(255) NOT NULL,
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
); 