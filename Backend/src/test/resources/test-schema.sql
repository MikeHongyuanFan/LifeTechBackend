-- Test Database Schema
-- Drop all tables first to ensure clean state
DROP TABLE IF EXISTS client_digital_certificates CASCADE;
DROP TABLE IF EXISTS client_wallet_integrations CASCADE;
DROP TABLE IF EXISTS certificates CASCADE;
DROP TABLE IF EXISTS certificate_templates CASCADE;
DROP TABLE IF EXISTS client_sessions CASCADE;
DROP TABLE IF EXISTS client_documents CASCADE;
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS enquiries CASCADE;
DROP TABLE IF EXISTS investments CASCADE;
DROP TABLE IF EXISTS entities CASCADE;
DROP TABLE IF EXISTS clients CASCADE;
DROP TABLE IF EXISTS admin_user_roles CASCADE;
DROP TABLE IF EXISTS admin_users CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create admin_users table
CREATE TABLE admin_users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_login TIMESTAMP,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    password_expires_at TIMESTAMP,
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_secret VARCHAR(255),
    session_timeout_minutes INTEGER DEFAULT 30,
    force_password_change BOOLEAN NOT NULL DEFAULT FALSE,
    last_password_change TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

-- Create admin_user_roles table
CREATE TABLE admin_user_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_user_id VARCHAR(36) NOT NULL,
    role VARCHAR(50) NOT NULL,
    FOREIGN KEY (admin_user_id) REFERENCES admin_users(id) ON DELETE CASCADE,
    UNIQUE (admin_user_id, role)
);

-- Create admin_user_allowed_ips table
CREATE TABLE IF NOT EXISTS admin_user_allowed_ips (
    admin_user_id VARCHAR(36) NOT NULL,
    ip_address VARCHAR(45),
    FOREIGN KEY (admin_user_id) REFERENCES admin_users(id) ON DELETE CASCADE
);

-- Create clients table
CREATE TABLE clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    membership_number VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    email_primary VARCHAR(255) UNIQUE NOT NULL,
    email_secondary VARCHAR(255),
    phone_primary VARCHAR(20),
    phone_secondary VARCHAR(20),
    address_street VARCHAR(255),
    address_city VARCHAR(100),
    address_state VARCHAR(100),
    address_postal_code VARCHAR(20),
    address_country VARCHAR(100),
    mailing_address_same BOOLEAN DEFAULT TRUE,
    mailing_street VARCHAR(255),
    mailing_city VARCHAR(100),
    mailing_state VARCHAR(100),
    mailing_postal_code VARCHAR(20),
    mailing_country VARCHAR(100),
    tfn_encrypted TEXT,
    tax_residency_status VARCHAR(50),
    bank_bsb VARCHAR(10),
    bank_account_number_encrypted TEXT,
    bank_account_name VARCHAR(255),
    investment_target DECIMAL(15,2),
    risk_profile VARCHAR(50),
    blockchain_identity_hash VARCHAR(128),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Create entities table
CREATE TABLE entities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    entity_name VARCHAR(255) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    registration_number VARCHAR(50),
    abn VARCHAR(11),
    acn VARCHAR(9),
    tfn_encrypted TEXT,
    registration_date DATE,
    registered_street VARCHAR(255),
    registered_city VARCHAR(100),
    registered_state VARCHAR(100),
    registered_postal_code VARCHAR(20),
    registered_country VARCHAR(100),
    contact_person VARCHAR(255),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(255),
    tax_residency_status VARCHAR(50),
    gst_registered BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

-- Create investments table
CREATE TABLE investments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    entity_id BIGINT,
    investment_name VARCHAR(255) NOT NULL,
    investment_type VARCHAR(100) NOT NULL,
    investment_category VARCHAR(100),
    description TEXT,
    investment_objective TEXT,
    risk_rating VARCHAR(20),
    initial_amount DECIMAL(15,2) NOT NULL,
    current_value DECIMAL(15,2),
    purchase_date DATE NOT NULL,
    maturity_date DATE,
    expected_return_rate DECIMAL(5,2),
    expected_return_amount DECIMAL(15,2),
    actual_return_amount DECIMAL(15,2),
    units_purchased DECIMAL(15,4),
    purchase_price_per_unit DECIMAL(15,4),
    current_price_per_unit DECIMAL(15,4),
    transaction_fees DECIMAL(10,2),
    management_fees DECIMAL(10,2),
    performance_fees DECIMAL(10,2),
    total_fees DECIMAL(10,2),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    FOREIGN KEY (entity_id) REFERENCES entities(id) ON DELETE SET NULL
);

-- Create audit_logs table (needed for test data)
CREATE TABLE audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    version BIGINT,
    action_type VARCHAR(100) NOT NULL,
    end_time TIMESTAMP NOT NULL,
    entity_id VARCHAR(36),
    entity_type VARCHAR(100) NOT NULL,
    error_message VARCHAR(255),
    ip_address VARCHAR(45),
    method_name VARCHAR(100),
    new_values TEXT,
    old_values TEXT,
    parameters VARCHAR(255),
    session_id VARCHAR(255),
    start_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    user_agent VARCHAR(255),
    user_id VARCHAR(36),
    user_type VARCHAR(10) NOT NULL
);

-- Create users table (for regular users, not admin)
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone_number VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    profile_image_url VARCHAR(255),
    date_of_birth TIMESTAMP,
    timezone VARCHAR(10),
    locale VARCHAR(10),
    login_count BIGINT DEFAULT 0,
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(45),
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    last_failed_login_at TIMESTAMP,
    account_locked_at TIMESTAMP,
    password_changed_at TIMESTAMP,
    status_changed_at TIMESTAMP,
    status_changed_by VARCHAR(36),
    status_change_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    version BIGINT DEFAULT 0
);

-- Create enquiries table
CREATE TABLE enquiries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT,
    enquiry_number VARCHAR(20) UNIQUE NOT NULL,
    subject VARCHAR(255) NOT NULL,
    description TEXT,
    enquiry_type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    status VARCHAR(30) DEFAULT 'OPEN',
    contact_name VARCHAR(100),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    assigned_to VARCHAR(36),
    response TEXT,
    response_date TIMESTAMP,
    resolved_date TIMESTAMP,
    due_date TIMESTAMP,
    source VARCHAR(50),
    category VARCHAR(100),
    tags VARCHAR(500),
    internal_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    FOREIGN KEY (client_id) REFERENCES clients(id),
    FOREIGN KEY (assigned_to) REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- Create indexes for better performance
CREATE INDEX idx_admin_users_username ON admin_users(username);
CREATE INDEX idx_admin_users_email ON admin_users(email);
CREATE INDEX idx_admin_users_status ON admin_users(status);

CREATE INDEX idx_clients_email ON clients(email_primary);
CREATE INDEX idx_clients_status ON clients(status);
CREATE INDEX idx_clients_membership ON clients(membership_number);

CREATE INDEX idx_entities_client_id ON entities(client_id);
CREATE INDEX idx_entities_entity_type ON entities(entity_type);
CREATE INDEX idx_entities_status ON entities(status);

CREATE INDEX idx_investments_client_id ON investments(client_id);
CREATE INDEX idx_investments_entity_id ON investments(entity_id);
CREATE INDEX idx_investments_investment_type ON investments(investment_type);
CREATE INDEX idx_investments_status ON investments(status);

-- Create audit_logs indexes
CREATE INDEX idx_audit_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_action_type ON audit_logs(action_type);
CREATE INDEX idx_audit_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_created_at ON audit_logs(created_at);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_user_created_at ON users(created_at);
CREATE INDEX idx_user_last_login ON users(last_login_at);

-- NOTE: client_documents table is automatically created by Hibernate from ClientDocument entity
-- Removed manual table creation to avoid conflict with JPA auto-DDL

-- Create certificate_templates table
CREATE TABLE certificate_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_name VARCHAR(255) NOT NULL,
    template_type VARCHAR(50) NOT NULL,
    template_content TEXT NOT NULL,
    template_variables TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    is_default BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 1,
    template_file_path VARCHAR(500),
    template_format VARCHAR(20) DEFAULT 'PDF',
    company_logo_path VARCHAR(500),
    background_image_path VARCHAR(500),
    primary_color VARCHAR(7) DEFAULT '#2196F3',
    secondary_color VARCHAR(7) DEFAULT '#4CAF50',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NOT NULL
);

-- Create certificates table
CREATE TABLE certificates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    certificate_number VARCHAR(50) UNIQUE NOT NULL,
    investment_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    template_id BIGINT,
    certificate_type VARCHAR(50) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    investment_amount DECIMAL(15,2),
    number_of_shares DECIMAL(15,4),
    share_price DECIMAL(10,4),
    file_path VARCHAR(500),
    file_size BIGINT,
    file_hash VARCHAR(128),
    digital_signature TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NOT NULL,
    updated_by VARCHAR(36),
    version INTEGER DEFAULT 1,
    FOREIGN KEY (investment_id) REFERENCES investments(id),
    FOREIGN KEY (client_id) REFERENCES clients(id),
    FOREIGN KEY (template_id) REFERENCES certificate_templates(id)
);

-- Create indexes for certificates
CREATE INDEX idx_certificates_investment ON certificates(investment_id);
CREATE INDEX idx_certificates_client ON certificates(client_id);
CREATE INDEX idx_certificates_number ON certificates(certificate_number);
CREATE INDEX idx_certificates_type ON certificates(certificate_type);
CREATE INDEX idx_certificates_status ON certificates(status);
CREATE INDEX idx_certificates_issue_date ON certificates(issue_date);

-- Create indexes for certificate templates
CREATE INDEX idx_certificate_templates_type ON certificate_templates(template_type);
CREATE INDEX idx_certificate_templates_active ON certificate_templates(is_active);
CREATE INDEX idx_certificate_templates_default ON certificate_templates(is_default);

-- Client Document Table
CREATE TABLE client_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    document_category VARCHAR(50) NOT NULL,
    document_status VARCHAR(50) DEFAULT 'UPLOADED',
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    upload_date TIMESTAMP,
    uploaded_by_client BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    version_number INTEGER DEFAULT 1,
    description VARCHAR(1000),
    expiry_date TIMESTAMP,
    access_count INTEGER DEFAULT 0,
    last_accessed_date TIMESTAMP,
    tags VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

-- Client Session Table
CREATE TABLE client_sessions (
    id VARCHAR(36) PRIMARY KEY,
    client_id BIGINT NOT NULL,
    session_token VARCHAR(255) UNIQUE NOT NULL,
    remember_me_token VARCHAR(255) UNIQUE,
    device_fingerprint VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at TIMESTAMP NOT NULL,
    remember_me_expires_at TIMESTAMP,
    last_accessed TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

-- Removed client_auth_session table as there's no corresponding entity

-- Client Wallet Integrations Table (Sprint 4.4)
CREATE TABLE client_wallet_integrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    platform_name VARCHAR(100) NOT NULL,
    platform_type VARCHAR(50) NOT NULL,
    account_identifier VARCHAR(255),
    integration_status VARCHAR(20) DEFAULT 'CONNECTED',
    last_sync_at TIMESTAMP,
    api_credentials_encrypted TEXT,
    sync_frequency VARCHAR(20) DEFAULT 'DAILY',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(36),
    version INTEGER DEFAULT 1,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

-- Client Digital Certificates Table (Sprint 4.4)
CREATE TABLE client_digital_certificates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    certificate_number VARCHAR(100) UNIQUE NOT NULL,
    certificate_type VARCHAR(50) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    number_of_shares DECIMAL(15,4),
    issue_date DATE NOT NULL,
    digital_signature TEXT,
    blockchain_hash VARCHAR(255),
    certificate_data TEXT,
    is_valid BOOLEAN DEFAULT TRUE,
    share_class VARCHAR(50),
    nominal_value DECIMAL(10,4),
    current_market_value DECIMAL(15,2),
    transfer_restrictions TEXT,
    voting_rights BOOLEAN,
    dividend_entitlement BOOLEAN,
    last_transfer_date DATE,
    certificate_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(36),
    version INTEGER DEFAULT 1,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

-- Create indexes for wallet integrations
CREATE INDEX idx_client_wallet_integrations_client_id ON client_wallet_integrations(client_id);
CREATE INDEX idx_client_wallet_integrations_platform_name ON client_wallet_integrations(platform_name);
CREATE INDEX idx_client_wallet_integrations_status ON client_wallet_integrations(integration_status);
CREATE INDEX idx_client_wallet_integrations_active ON client_wallet_integrations(is_active);

-- Create indexes for digital certificates
CREATE INDEX idx_client_digital_certificates_client_id ON client_digital_certificates(client_id);
CREATE INDEX idx_client_digital_certificates_certificate_number ON client_digital_certificates(certificate_number);
CREATE INDEX idx_client_digital_certificates_company_name ON client_digital_certificates(company_name);
CREATE INDEX idx_client_digital_certificates_is_valid ON client_digital_certificates(is_valid); 