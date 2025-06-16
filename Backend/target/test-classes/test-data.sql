-- Test Data for Integration Tests
-- Password: admin123 (BCrypt encoded - same as production data.sql)

-- Insert test admin users (without role column)
INSERT INTO admin_users (id, username, email, password, status, mfa_enabled, created_at, updated_at, failed_login_attempts)
VALUES 
('00000000-0000-0000-0000-000000000001', 'testadmin', 'testadmin@finance.com',
 '$2a$10$uNaSM60qZodktvknl5nnS.sXx1HMmwTCsrodGkZ7SqvrPzuEetpBC',
 'ACTIVE', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

('00000000-0000-0000-0000-000000000002', 'mfaadmin', 'mfaadmin@finance.com',
 '$2a$10$uNaSM60qZodktvknl5nnS.sXx1HMmwTCsrodGkZ7SqvrPzuEetpBC',
 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

('00000000-0000-0000-0000-000000000003', 'lockedadmin', 'lockedadmin@finance.com',
 '$2a$10$uNaSM60qZodktvknl5nnS.sXx1HMmwTCsrodGkZ7SqvrPzuEetpBC',
 'ACTIVE', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 5);

-- Assign roles to admin users
INSERT INTO admin_user_roles (admin_user_id, role)
VALUES 
('00000000-0000-0000-0000-000000000001', 'SYSTEM_ADMIN'),
('00000000-0000-0000-0000-000000000002', 'SYSTEM_ADMIN'),
('00000000-0000-0000-0000-000000000003', 'ANALYST');

-- Insert test users (regular users)
INSERT INTO users (id, username, email, password, first_name, last_name, status, email_verified, phone_verified, created_at, updated_at, failed_login_attempts)
VALUES 
('10000000-0000-0000-0000-000000000001', 'testuser', 'testuser@finance.com',
 '$2a$10$uNaSM60qZodktvknl5nnS.sXx1HMmwTCsrodGkZ7SqvrPzuEetpBC',
 'Test', 'User', 'ACTIVE', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

('10000000-0000-0000-0000-000000000002', 'inactiveuser', 'inactive@finance.com',
 '$2a$10$uNaSM60qZodktvknl5nnS.sXx1HMmwTCsrodGkZ7SqvrPzuEetpBC',
 'Inactive', 'User', 'INACTIVE', false, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Insert test clients
INSERT INTO clients (id, membership_number, first_name, last_name, email_primary, phone_primary, address_street, address_city, address_state, address_postal_code, address_country, status, created_at, updated_at)
VALUES 
(1, 'MEM001', 'John', 'Doe', 'john.doe@example.com', '0412345678', '123 Test Street', 'Sydney', 'NSW', '2000', 'Australia', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'MEM002', 'Jane', 'Smith', 'jane.smith@example.com', '0423456789', '456 Demo Avenue', 'Melbourne', 'VIC', '3000', 'Australia', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'MEM003', 'Bob', 'Wilson', 'bob.wilson@example.com', '0434567890', '789 Sample Road', 'Brisbane', 'QLD', '4000', 'Australia', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test entities
INSERT INTO entities (id, client_id, entity_name, entity_type, registration_number, abn, acn, registration_date, registered_street, registered_city, registered_state, registered_postal_code, registered_country, contact_person, contact_phone, contact_email, tax_residency_status, gst_registered, status, created_at, updated_at)
VALUES 
(1, 1, 'John Doe Pty Ltd', 'COMPANY', 'ACN123456789', '12345678901', '123456789', '2020-01-15', '123 Test Street', 'Sydney', 'NSW', '2000', 'Australia', 'John Doe', '0412345678', 'john.doe@example.com', 'RESIDENT', true, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(2, 2, 'Smith Family Trust', 'FAMILY_TRUST', 'TFN987654321', '98765432109', null, '2019-06-20', '456 Demo Avenue', 'Melbourne', 'VIC', '3000', 'Australia', 'Jane Smith', '0423456789', 'jane.smith@example.com', 'RESIDENT', false, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(3, 3, 'Wilson SMSF', 'SMSF', 'SMSF456789123', '45678912345', null, '2018-03-10', '789 Sample Road', 'Brisbane', 'QLD', '4000', 'Australia', 'Bob Wilson', '0434567890', 'bob.wilson@example.com', 'RESIDENT', true, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test investments
INSERT INTO investments (id, client_id, entity_id, investment_name, investment_type, investment_category, description, risk_rating, initial_amount, current_value, purchase_date, expected_return_rate, status, created_at, updated_at)
VALUES 
(1, 1, 1, 'ASX200 ETF Investment', 'EQUITY', 'Listed shares', 'Diversified Australian equity ETF', 'MEDIUM', 50000.00, 55000.00, '2023-01-15', 7.50, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(2, 2, 2, 'Commercial Property Investment', 'PROPERTY', 'Commercial', 'Office building investment in Melbourne CBD', 'MEDIUM', 250000.00, 275000.00, '2022-06-01', 6.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(3, 3, 3, 'Government Bonds', 'FIXED_INCOME', 'Bonds', 'Australian Government Treasury Bonds', 'LOW', 100000.00, 102000.00, '2023-03-01', 3.50, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(4, 1, null, 'Tech Stock Portfolio', 'EQUITY', 'Listed shares', 'Individual technology stocks', 'HIGH', 25000.00, 32000.00, '2023-02-15', 12.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test audit logs
INSERT INTO audit_logs (id, user_id, user_type, action_type, entity_type, entity_id, start_time, end_time, status, ip_address, created_at, updated_at)
VALUES 
('a0000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'ADMIN', 'CREATE', 'CLIENT', '1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUCCESS', '127.0.0.1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('a0000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', 'ADMIN', 'UPDATE', 'INVESTMENT', '1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUCCESS', '127.0.0.1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('a0000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000002', 'ADMIN', 'VIEW', 'ENTITY', '2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUCCESS', '192.168.1.100', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test certificate templates
INSERT INTO certificate_templates (id, template_name, template_type, template_content, is_active, is_default, version, created_by, created_at, updated_at)
VALUES 
(1, 'Default Share Certificate', 'SHARE_CERTIFICATE', 'Default template for share certificates with company branding', TRUE, TRUE, 1, '00000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Investment Certificate Template', 'INVESTMENT_CERTIFICATE', 'Standard investment certificate template', TRUE, TRUE, 1, '00000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Bond Certificate Template', 'BOND_CERTIFICATE', 'Template for bond certificates', TRUE, TRUE, 1, '00000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test certificates
INSERT INTO certificates (id, certificate_number, investment_id, client_id, template_id, certificate_type, issue_date, expiry_date, status, investment_amount, number_of_shares, share_price, created_by, created_at, updated_at)
VALUES 
(1, 'SHR-202401-0001', 1, 1, 1, 'SHARE_CERTIFICATE', '2023-01-15', '2025-01-15', 'ACTIVE', 50000.00, 2000.00, 25.00, '00000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'INV-202401-0001', 2, 2, 2, 'INVESTMENT_CERTIFICATE', '2022-06-01', '2024-06-01', 'ACTIVE', 250000.00, NULL, NULL, '00000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'BND-202401-0001', 3, 3, 3, 'BOND_CERTIFICATE', '2023-03-01', '2025-03-01', 'ACTIVE', 100000.00, 100.00, 1000.00, '00000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Test Client Documents (updated table name)
INSERT INTO client_documents (id, client_id, document_name, document_type, document_category, document_status, file_size, file_path, description, tags, uploaded_by_client, is_active, created_at, updated_at, expiry_date, access_count)
VALUES 
(1, 1, 'Test Document 1', 'IDENTITY_VERIFICATION', 'KYC', 'UPLOADED', 1024, '/test/path1.pdf', 'Test description 1', 'test,document', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30' DAY, 0),
(2, 1, 'Test Document 2', 'KYC_DOCUMENT', 'INVESTMENT', 'APPROVED', 2048, '/test/path2.pdf', 'Test description 2', 'test,financial', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '60' DAY, 5),
(3, 1, 'Test Document 3', 'OTHER', 'LEGAL', 'EXPIRED', 3072, '/test/path3.pdf', 'Test description 3', 'test,legal', false, true, CURRENT_TIMESTAMP - INTERVAL '30' DAY, CURRENT_TIMESTAMP - INTERVAL '30' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY, 10);

-- Test Client Sessions (updated table name)
INSERT INTO client_sessions (id, client_id, session_token, created_at, expires_at, is_active)
VALUES 
('10000000-0000-0000-0000-000000000001', 1, 'test-session-token', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '1' HOUR, true); 