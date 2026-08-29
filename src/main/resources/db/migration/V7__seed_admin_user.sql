INSERT INTO users (name, email, password, role, created_at, updated_at)
VALUES (
    'Admin',
    'admin@salestrack.com',
    '$2b$10$FhLaJ1u6csIAgWuYg5m6Ber7OWxGMh0YSbPLz/BLXl4mlbflWG4VS',
    'ADMIN',
    NOW(),
    NOW()
);