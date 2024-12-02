CREATE TABLE enterprise_invites (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    enterprise_id BIGINT NOT NULL,
    inviter_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (enterprise_id) REFERENCES enterprises(id),
    FOREIGN KEY (inviter_id) REFERENCES users(id)
); 