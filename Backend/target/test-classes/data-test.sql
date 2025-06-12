-- Insert test admin user
-- Password: admin123 (BCrypt encoded)
INSERT INTO "admin_users" (id, username, email, password_hash, role, status, mfa_enabled, created_at, updated_at, last_login_at, failed_login_attempts, account_locked_until)
VALUES ('00000000-0000-0000-0000-000000000001', 'testadmin', 'testadmin@finance.com',
       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIo0jQDjT6VmRq',
       'SUPER_ADMIN', 'ACTIVE', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 0, NULL);

-- Assign SUPER_ADMIN role
INSERT INTO "admin_user_roles" (admin_user_id, role)
VALUES ('00000000-0000-0000-0000-000000000001', 'SUPER_ADMIN');

-- Insert test MFA user
INSERT INTO "admin_users" (id, username, email, password_hash, role, status, mfa_enabled, created_at, updated_at, last_login_at, failed_login_attempts, account_locked_until)
VALUES ('00000000-0000-0000-0000-000000000002', 'mfaadmin', 'mfaadmin@finance.com',
       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIo0jQDjT6VmRq',
       'ADMIN', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 0, NULL);

-- Assign ADMIN role to MFA user
INSERT INTO "admin_user_roles" (admin_user_id, role)
VALUES ('00000000-0000-0000-0000-000000000002', 'ADMIN'); 