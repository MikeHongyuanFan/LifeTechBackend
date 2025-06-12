-- Test Database Schema for H2
-- Drop tables if they exist
DROP TABLE IF EXISTS admin_user_roles CASCADE;
DROP TABLE IF EXISTS admin_users CASCADE;
DROP TABLE IF EXISTS clients CASCADE;
DROP TABLE IF EXISTS entities CASCADE;
DROP TABLE IF EXISTS investments CASCADE;
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create admin_users table
CREATE TABLE admin_users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
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
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    email_verified_at TIMESTAMP,
    phone_verified_at TIMESTAMP,
    last_login TIMESTAMP,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    version BIGINT DEFAULT 0
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