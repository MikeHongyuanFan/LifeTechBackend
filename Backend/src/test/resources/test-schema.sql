-- Test Database Schema for H2
-- Drop tables if they exist
DROP TABLE IF EXISTS admin_user_roles CASCADE;
DROP TABLE IF EXISTS admin_users CASCADE;
DROP TABLE IF EXISTS clients CASCADE;
DROP TABLE IF EXISTS entities CASCADE;
DROP TABLE IF EXISTS investments CASCADE;
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS enquiries CASCADE;

-- Create admin_users table
CREATE TABLE admin_users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_secret VARCHAR(255),
    force_password_change BOOLEAN NOT NULL DEFAULT FALSE,
    password_expires_at TIMESTAMP,
    last_password_change TIMESTAMP,
    session_timeout_minutes INTEGER DEFAULT 30,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    version BIGINT DEFAULT 0
);

-- Create admin_user_roles table
CREATE TABLE admin_user_roles (
    admin_user_id VARCHAR(36) NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (admin_user_id, role),
    FOREIGN KEY (admin_user_id) REFERENCES admin_users(id) ON DELETE CASCADE
);

-- Create clients table
CREATE TABLE clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    date_of_birth DATE,
    address_street VARCHAR(255),
    address_city VARCHAR(100),
    address_state VARCHAR(100),
    address_postal_code VARCHAR(20),
    address_country VARCHAR(100),
    tax_file_number_encrypted TEXT,
    employment_status VARCHAR(50),
    annual_income DECIMAL(15,2),
    net_worth DECIMAL(15,2),
    risk_tolerance VARCHAR(20),
    investment_experience VARCHAR(50),
    investment_objectives TEXT,
    kyc_status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    kyc_completed_at TIMESTAMP,
    kyc_documents TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
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

-- Create audit_logs table
CREATE TABLE audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    user_type VARCHAR(10) NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(36),
    old_values TEXT,
    new_values TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    session_id VARCHAR(255),
    method_name VARCHAR(100),
    parameters TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    version BIGINT DEFAULT 0
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
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

CREATE INDEX idx_clients_email ON clients(email);
CREATE INDEX idx_clients_status ON clients(status);
CREATE INDEX idx_clients_kyc_status ON clients(kyc_status);

CREATE INDEX idx_entities_client_id ON entities(client_id);
CREATE INDEX idx_entities_entity_type ON entities(entity_type);
CREATE INDEX idx_entities_status ON entities(status);

CREATE INDEX idx_investments_client_id ON investments(client_id);
CREATE INDEX idx_investments_entity_id ON investments(entity_id);
CREATE INDEX idx_investments_investment_type ON investments(investment_type);
CREATE INDEX idx_investments_status ON investments(status);

CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action_type ON audit_logs(action_type);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_start_time ON audit_logs(start_time);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_user_created_at ON users(created_at);
CREATE INDEX idx_user_last_login ON users(last_login_at);

-- NOTE: client_documents table is automatically created by Hibernate from ClientDocument entity
-- Removed manual table creation to avoid conflict with JPA auto-DDL

-- Create certificate_templates table
CREATE TABLE IF NOT EXISTS certificate_templates (
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
CREATE TABLE IF NOT EXISTS certificates (
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
CREATE INDEX IF NOT EXISTS idx_certificates_investment ON certificates(investment_id);
CREATE INDEX IF NOT EXISTS idx_certificates_client ON certificates(client_id);
CREATE INDEX IF NOT EXISTS idx_certificates_number ON certificates(certificate_number);
CREATE INDEX IF NOT EXISTS idx_certificates_type ON certificates(certificate_type);
CREATE INDEX IF NOT EXISTS idx_certificates_status ON certificates(status);
CREATE INDEX IF NOT EXISTS idx_certificates_issue_date ON certificates(issue_date);

-- Create indexes for certificate templates
CREATE INDEX IF NOT EXISTS idx_certificate_templates_type ON certificate_templates(template_type);
CREATE INDEX IF NOT EXISTS idx_certificate_templates_active ON certificate_templates(is_active);
CREATE INDEX IF NOT EXISTS idx_certificate_templates_default ON certificate_templates(is_default); 