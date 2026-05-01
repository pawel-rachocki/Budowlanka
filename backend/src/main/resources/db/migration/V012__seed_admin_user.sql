-- DEV ONLY — change password before production deploy!
INSERT INTO users (email, password_hash, role, email_verified)
VALUES (
    'admin@budowlanka.local',
    '$2a$10$GX5NIM8cDGNzjvQ5jTJZEuGri6yWcf/D/vPjh6IlFfshd15IhTRj.',
    'ADMIN',
    true
)
ON CONFLICT (email) DO NOTHING;
