-- Reset admin user if exists
DELETE FROM admin_user_roles WHERE admin_user_id = '00000000-0000-0000-0000-000000000001';
DELETE FROM admin_user_allowed_ips WHERE admin_user_id = '00000000-0000-0000-0000-000000000001';
DELETE FROM admin_users WHERE id = '00000000-0000-0000-0000-000000000001';

-- Insert default admin user
-- Password: admin123 (BCrypt encoded)
-- This user will be created only if no admin users exist
INSERT INTO admin_users (id, username, email, password_hash, role, status, mfa_enabled, created_at, updated_at, last_login_at, failed_login_attempts, account_locked_until)
SELECT gen_random_uuid(), 'admin', 'admin@finance.com',
       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIo0jQDjT6VmRq',
       'SUPER_ADMIN', 'ACTIVE', false, NOW(), NOW(), NULL, 0, NULL
WHERE NOT EXISTS (SELECT 1 FROM admin_users WHERE role = 'SUPER_ADMIN');

-- Assign SUPER_ADMIN role
INSERT INTO admin_user_roles (admin_user_id, role)
VALUES ('00000000-0000-0000-0000-000000000001', 'SUPER_ADMIN')
ON CONFLICT DO NOTHING;

-- Add allowed IPs (disabled for testing)
DELETE FROM admin_user_allowed_ips WHERE admin_user_id = '00000000-0000-0000-0000-000000000001';

-- Reset any IP blocks
DELETE FROM blocked_ips; 