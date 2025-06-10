# LifeTech Backend - Database Schema Requirements

## Overview
This document outlines the complete database schema requirements for the LifeTech Finance Management System based on Sprint 1-3 requirements. The schema is designed to support comprehensive financial operations, client management, investment tracking, and compliance requirements.

## Database Schema Components

### 1. Admin Management (Sprint 1.1)

#### admin_users Table
**Purpose**: Manages administrative users with role-based access control
```sql
CREATE TABLE admin_users (
    id BIGSERIAL PRIMARY KEY,
    employee_id VARCHAR(20) UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    email_primary VARCHAR(255) UNIQUE NOT NULL,
    email_secondary VARCHAR(255),
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    phone_primary VARCHAR(20),
    phone_secondary VARCHAR(20),
    department VARCHAR(100),
    job_title VARCHAR(100),
    password_hash TEXT NOT NULL,
    password_salt VARCHAR(255),
    role VARCHAR(50) NOT NULL DEFAULT 'ADMIN',
    permissions JSONB,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    last_login TIMESTAMP,
    password_changed_at TIMESTAMP,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES admin_users(id),
    updated_by BIGINT REFERENCES admin_users(id)
);
```

**Key Fields**:
- `employee_id`: Unique identifier for HR system integration
- `role`: Super Admin, Admin, Manager, Analyst, etc.
- `permissions`: JSONB for granular permission control
- `failed_login_attempts`: Security tracking
- `locked_until`: Account lockout management

#### admin_sessions Table
**Purpose**: Tracks admin user sessions for security monitoring
```sql
CREATE TABLE admin_sessions (
    id BIGSERIAL PRIMARY KEY,
    admin_user_id BIGINT REFERENCES admin_users(id),
    session_token VARCHAR(255) UNIQUE NOT NULL,
    ip_address INET,
    user_agent TEXT,
    login_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    logout_timestamp TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT true
);
```

#### admin_audit_logs Table
**Purpose**: Comprehensive audit trail for all administrative actions
```sql
CREATE TABLE admin_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    admin_user_id BIGINT REFERENCES admin_users(id),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50),
    resource_id VARCHAR(100),
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN DEFAULT true,
    error_message TEXT
);
```

### 2. Client Management (Sprint 1.2)

#### clients Table
**Purpose**: Core client information and profile management
```sql
CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
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
    address_country VARCHAR(100) DEFAULT 'Australia',
    mailing_address_same BOOLEAN DEFAULT true,
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
    password_hash TEXT,
    password_salt VARCHAR(255),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    kyc_status VARCHAR(20) DEFAULT 'PENDING',
    last_login TIMESTAMP,
    email_verified BOOLEAN DEFAULT false,
    phone_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES admin_users(id),
    updated_by BIGINT REFERENCES admin_users(id)
);
```

**Key Fields**:
- `membership_number`: Auto-generated unique client identifier
- `tfn_encrypted`: Encrypted Tax File Number
- `bank_account_number_encrypted`: Encrypted banking details
- `risk_profile`: Conservative, Moderate, Aggressive
- `kyc_status`: KYC verification status

#### client_login_history Table
**Purpose**: Tracks client login activities for security and analytics
```sql
CREATE TABLE client_login_history (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES clients(id),
    login_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    logout_timestamp TIMESTAMP,
    ip_address INET,
    user_agent TEXT,
    session_duration INTERVAL,
    login_successful BOOLEAN DEFAULT true,
    failure_reason VARCHAR(255),
    device_fingerprint VARCHAR(255),
    location_city VARCHAR(100),
    location_country VARCHAR(100)
);
```

### 3. KYC Verification (Sprint 1.3)

#### kyc_documents Table
**Purpose**: Manages KYC document uploads and verification
```sql
CREATE TABLE kyc_documents (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES clients(id),
    document_type VARCHAR(50) NOT NULL,
    document_category VARCHAR(50) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_path TEXT NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100),
    checksum VARCHAR(64),
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    uploaded_by BIGINT REFERENCES admin_users(id),
    status VARCHAR(20) DEFAULT 'PENDING',
    version INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT true,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Document Types**:
- `IDENTITY_PROOF`: Driver's License, Passport, National ID
- `ADDRESS_PROOF`: Utility Bills, Bank Statements, Council Rates
- `FINANCIAL_PROOF`: Tax Returns, Payslips, Financial Statements
- `ADDITIONAL_DOCS`: Source of Funds, Beneficial Ownership

#### kyc_reviews Table
**Purpose**: Tracks KYC document review process and decisions
```sql
CREATE TABLE kyc_reviews (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT REFERENCES kyc_documents(id),
    client_id BIGINT REFERENCES clients(id),
    reviewer_id BIGINT REFERENCES admin_users(id),
    review_status VARCHAR(20) NOT NULL,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    review_notes TEXT,
    rejection_reason TEXT,
    risk_score INTEGER,
    compliance_flags JSONB,
    next_action VARCHAR(50),
    escalated BOOLEAN DEFAULT false,
    escalated_to BIGINT REFERENCES admin_users(id),
    escalation_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Review Status Types**:
- `PENDING`: Awaiting review
- `APPROVED`: Document approved
- `REJECTED`: Document rejected
- `ADDITIONAL_REQUIRED`: More documents needed

### 4. Investment Management (Sprint 1.4)

#### investments Table
**Purpose**: Core investment records and portfolio management
```sql
CREATE TABLE investments (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES clients(id),
    entity_id BIGINT REFERENCES entities(id),
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
    return_type VARCHAR(50),
    distribution_frequency VARCHAR(20),
    purchase_price_per_unit DECIMAL(10,4),
    number_of_units DECIMAL(15,6),
    transaction_fees DECIMAL(10,2),
    management_fees DECIMAL(10,2),
    performance_fees DECIMAL(10,2),
    unrealized_gains_losses DECIMAL(15,2),
    realized_gains_losses DECIMAL(15,2),
    total_return_to_date DECIMAL(15,2),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES admin_users(id),
    updated_by BIGINT REFERENCES admin_users(id)
);
```

**Investment Types**:
- `PROPERTY`: Residential, Commercial, Industrial
- `EQUITY`: Listed shares, Private equity
- `FIXED_INCOME`: Bonds, Term deposits
- `ALTERNATIVE`: Commodities, Cryptocurrency
- `MANAGED_FUNDS`: Mutual funds, ETFs, Hedge funds

**Investment Status**:
- `PENDING`: Committed but not funded
- `ACTIVE`: Live investment
- `MATURED`: Reached maturity
- `PARTIAL_EXIT`: Partial divestment
- `FULLY_EXITED`: Complete divestment

#### entities Table
**Purpose**: Manages investment entities and structures
```sql
CREATE TABLE entities (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES clients(id),
    entity_name VARCHAR(255) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    registration_number VARCHAR(50),
    abn VARCHAR(20),
    acn VARCHAR(20),
    tfn_encrypted TEXT,
    registration_date DATE,
    registered_address TEXT,
    primary_contact_person VARCHAR(255),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(255),
    mailing_address TEXT,
    physical_address TEXT,
    ownership_structure TEXT,
    beneficial_owners JSONB,
    directors_trustees JSONB,
    authorized_signatories JSONB,
    tax_residency_status VARCHAR(50),
    gst_registration BOOLEAN DEFAULT false,
    reporting_requirements TEXT,
    compliance_obligations TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES admin_users(id)
);
```

**Entity Types**:
- `INDIVIDUAL`: Personal accounts
- `JOINT`: Multiple ownership
- `COMPANY`: Corporate entities
- `TRUST`: Family/Unit/Discretionary trusts
- `SMSF`: Self-Managed Super Funds
- `PARTNERSHIP`: Business partnerships

#### investment_performance Table
**Purpose**: Tracks investment performance metrics over time
```sql
CREATE TABLE investment_performance (
    id BIGSERIAL PRIMARY KEY,
    investment_id BIGINT REFERENCES investments(id),
    performance_date DATE NOT NULL,
    market_value DECIMAL(15,2) NOT NULL,
    daily_return DECIMAL(8,6),
    total_return_absolute DECIMAL(15,2),
    total_return_percentage DECIMAL(8,4),
    annualized_return DECIMAL(8,4),
    benchmark_return DECIMAL(8,4),
    alpha DECIMAL(8,4),
    beta DECIMAL(8,4),
    sharpe_ratio DECIMAL(8,4),
    volatility DECIMAL(8,4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(investment_id, performance_date)
);
```

### 5. Advanced Financial Operations (Sprint 2.1)

#### transactions Table
**Purpose**: Comprehensive transaction management system
```sql
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES clients(id),
    investment_id BIGINT REFERENCES investments(id),
    transaction_type VARCHAR(50) NOT NULL,
    transaction_category VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'AUD',
    exchange_rate DECIMAL(10,6),
    amount_base_currency DECIMAL(15,2),
    units DECIMAL(15,6),
    price_per_unit DECIMAL(10,4),
    transaction_date DATE NOT NULL,
    settlement_date DATE,
    description TEXT,
    reference_number VARCHAR(100),
    counterparty VARCHAR(255),
    fees DECIMAL(10,2) DEFAULT 0,
    tax_amount DECIMAL(10,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',
    batch_id VARCHAR(50),
    parent_transaction_id BIGINT REFERENCES transactions(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES admin_users(id)
);
```

**Transaction Types**:
- `PURCHASE`: Investment purchases
- `SALE`: Investment sales
- `DIVIDEND`: Dividend payments
- `DISTRIBUTION`: Distribution payments
- `DEPOSIT`: Cash deposits
- `WITHDRAWAL`: Cash withdrawals
- `TRANSFER`: Inter-account transfers
- `FEE`: Fee deductions

#### currency_rates Table
**Purpose**: Manages foreign exchange rates for multi-currency support
```sql
CREATE TABLE currency_rates (
    id BIGSERIAL PRIMARY KEY,
    from_currency VARCHAR(3) NOT NULL,
    to_currency VARCHAR(3) NOT NULL,
    rate DECIMAL(10,6) NOT NULL,
    rate_date DATE NOT NULL,
    rate_type VARCHAR(20) DEFAULT 'SPOT',
    source VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(from_currency, to_currency, rate_date, rate_type)
);
```

### 6. Notification & Communication (Sprint 2.3)

#### notifications Table
**Purpose**: Manages multi-channel notification system
```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT,
    recipient_type VARCHAR(20) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    template_id BIGINT REFERENCES notification_templates(id),
    subject VARCHAR(255),
    content TEXT NOT NULL,
    priority VARCHAR(20) DEFAULT 'NORMAL',
    status VARCHAR(20) DEFAULT 'PENDING',
    scheduled_at TIMESTAMP,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP,
    failure_reason TEXT,
    retry_count INTEGER DEFAULT 0,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Notification Channels**:
- `EMAIL`: Email notifications
- `SMS`: Text messages
- `PUSH`: Push notifications
- `IN_APP`: In-application notifications

#### notification_templates Table
**Purpose**: Manages notification templates and personalization
```sql
CREATE TABLE notification_templates (
    id BIGSERIAL PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL,
    template_type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    subject_template TEXT,
    content_template TEXT NOT NULL,
    variables JSONB,
    language VARCHAR(5) DEFAULT 'en',
    is_active BOOLEAN DEFAULT true,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES admin_users(id)
);
```

#### notification_preferences Table
**Purpose**: Manages user notification preferences
```sql
CREATE TABLE notification_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    user_type VARCHAR(20) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    frequency VARCHAR(20) DEFAULT 'IMMEDIATE',
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    timezone VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, user_type, notification_type, channel)
);
```

### 7. Client Portal & Self-Service (Sprint 3.1)

#### client_sessions Table
**Purpose**: Manages client portal sessions and security
```sql
CREATE TABLE client_sessions (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES clients(id),
    session_token VARCHAR(255) UNIQUE NOT NULL,
    device_id VARCHAR(255),
    device_type VARCHAR(50),
    ip_address INET,
    user_agent TEXT,
    location_city VARCHAR(100),
    location_country VARCHAR(100),
    login_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    logout_timestamp TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT true,
    mfa_verified BOOLEAN DEFAULT false,
    trusted_device BOOLEAN DEFAULT false
);
```

#### client_goals Table
**Purpose**: Manages client financial goals and planning
```sql
CREATE TABLE client_goals (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES clients(id),
    goal_name VARCHAR(255) NOT NULL,
    goal_type VARCHAR(50) NOT NULL,
    description TEXT,
    target_amount DECIMAL(15,2) NOT NULL,
    current_amount DECIMAL(15,2) DEFAULT 0,
    target_date DATE,
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    investment_strategy VARCHAR(100),
    risk_tolerance VARCHAR(20),
    auto_invest_amount DECIMAL(10,2),
    auto_invest_frequency VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Goal Types**:
- `RETIREMENT`: Retirement planning
- `EDUCATION`: Education funding
- `HOME_PURCHASE`: Home buying goals
- `EMERGENCY_FUND`: Emergency savings
- `CUSTOM`: Custom financial goals

#### support_tickets Table
**Purpose**: Manages client support and communication
```sql
CREATE TABLE support_tickets (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES clients(id),
    ticket_number VARCHAR(20) UNIQUE NOT NULL,
    subject VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    priority VARCHAR(20) DEFAULT 'NORMAL',
    status VARCHAR(20) DEFAULT 'OPEN',
    assigned_to BIGINT REFERENCES admin_users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    first_response_at TIMESTAMP,
    resolution_notes TEXT
);
```

### 8. Compliance & Reporting

#### compliance_reports Table
**Purpose**: Manages regulatory compliance and reporting
```sql
CREATE TABLE compliance_reports (
    id BIGSERIAL PRIMARY KEY,
    report_type VARCHAR(50) NOT NULL,
    report_name VARCHAR(255) NOT NULL,
    reporting_period_start DATE NOT NULL,
    reporting_period_end DATE NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    generated_by BIGINT REFERENCES admin_users(id),
    file_path TEXT,
    file_size BIGINT,
    status VARCHAR(20) DEFAULT 'GENERATED',
    submitted_at TIMESTAMP,
    submission_reference VARCHAR(100),
    metadata JSONB
);
```

## Database Indexes and Constraints

### Performance Indexes
```sql
-- Client Management Indexes
CREATE INDEX idx_clients_email ON clients(email_primary);
CREATE INDEX idx_clients_membership ON clients(membership_number);
CREATE INDEX idx_clients_status ON clients(status);

-- Investment Management Indexes
CREATE INDEX idx_investments_client ON investments(client_id);
CREATE INDEX idx_investments_type ON investments(investment_type);
CREATE INDEX idx_investments_status ON investments(status);
CREATE INDEX idx_investments_date ON investments(purchase_date);

-- Transaction Indexes
CREATE INDEX idx_transactions_client ON transactions(client_id);
CREATE INDEX idx_transactions_investment ON transactions(investment_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);

-- KYC Indexes
CREATE INDEX idx_kyc_documents_client ON kyc_documents(client_id);
CREATE INDEX idx_kyc_documents_status ON kyc_documents(status);
CREATE INDEX idx_kyc_reviews_client ON kyc_reviews(client_id);

-- Notification Indexes
CREATE INDEX idx_notifications_recipient ON notifications(recipient_id, recipient_type);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_scheduled ON notifications(scheduled_at);
```

### Foreign Key Constraints
```sql
-- Ensure referential integrity
ALTER TABLE clients ADD CONSTRAINT fk_clients_created_by 
    FOREIGN KEY (created_by) REFERENCES admin_users(id);

ALTER TABLE investments ADD CONSTRAINT fk_investments_client 
    FOREIGN KEY (client_id) REFERENCES clients(id);

ALTER TABLE kyc_documents ADD CONSTRAINT fk_kyc_documents_client 
    FOREIGN KEY (client_id) REFERENCES clients(id);

ALTER TABLE transactions ADD CONSTRAINT fk_transactions_client 
    FOREIGN KEY (client_id) REFERENCES clients(id);
```

## Data Security and Encryption

### Sensitive Data Fields
- **Encrypted Fields**: TFN, Bank Account Numbers, SSN
- **Hashed Fields**: Passwords, Security Tokens
- **Audit Fields**: All modification tracking
- **Access Control**: Role-based permissions

### Compliance Requirements
- **GDPR**: Right to be forgotten, Data portability
- **Australian Privacy Act**: Data protection compliance
- **Financial Regulations**: AML, KYC compliance
- **Audit Requirements**: 7-year data retention

## Database Maintenance

### Backup Strategy
- **Daily Backups**: Full database backup
- **Point-in-Time Recovery**: Transaction log backups
- **Archival**: Long-term compliance storage
- **Disaster Recovery**: Multi-region backup

### Performance Monitoring
- **Query Performance**: Slow query monitoring
- **Index Usage**: Index optimization
- **Storage Growth**: Capacity planning
- **Connection Monitoring**: Connection pool management

---

*This database schema supports the complete LifeTech Finance Management System requirements across Sprint 1-3, providing a robust foundation for financial operations, client management, and regulatory compliance.* 