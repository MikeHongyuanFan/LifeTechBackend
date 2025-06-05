-- Reset admin user if exists
DELETE FROM admin_user_roles WHERE admin_user_id = '00000000-0000-0000-0000-000000000001';
DELETE FROM admin_user_allowed_ips WHERE admin_user_id = '00000000-0000-0000-0000-000000000001';
DELETE FROM admin_users WHERE id = '00000000-0000-0000-0000-000000000001';

-- Default admin user (password: Admin@123)
INSERT INTO admin_users (id, username, email, password_hash, status, created_at, updated_at, version, failed_login_attempts, locked_until)
VALUES ('00000000-0000-0000-0000-000000000001', 'admin', 'admin@tycoon.com', 
        '$2a$10$KgGYPhatXQPRuXHYj1xwX.H8yO8F7Pdb3OL2.zWb5X7azSbMC.K4.', 
        'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, NULL)
ON CONFLICT (id) DO UPDATE SET 
    status = 'ACTIVE',
    failed_login_attempts = 0,
    locked_until = NULL,
    updated_at = CURRENT_TIMESTAMP;

-- Assign SUPER_ADMIN role
INSERT INTO admin_user_roles (admin_user_id, role)
VALUES ('00000000-0000-0000-0000-000000000001', 'SUPER_ADMIN')
ON CONFLICT DO NOTHING;

-- Add allowed IPs (disabled for testing)
DELETE FROM admin_user_allowed_ips WHERE admin_user_id = '00000000-0000-0000-0000-000000000001';

-- Reset any IP blocks
DELETE FROM blocked_ips; 