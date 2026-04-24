CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Insert default admin user
-- Password is "admin" (BCrypt hashed)
INSERT INTO users (id, username, password, role)
VALUES (
    '50c7650f-e2d9-482d-88f5-9dc86ec7b252',
    'admin',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
    'ROLE_ADMIN'
);
