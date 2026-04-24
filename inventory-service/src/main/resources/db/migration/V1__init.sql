CREATE TABLE inventory (
    product_id UUID PRIMARY KEY,
    available INTEGER NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE idempotency_keys (
    idempotency_key VARCHAR(255) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed some test data for existing products (IDs from V1 of products-service)
INSERT INTO inventory (product_id, available, version) VALUES ('550e8400-e29b-41d4-a716-446655440000', 100, 0);
INSERT INTO inventory (product_id, available, version) VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 50, 0);
