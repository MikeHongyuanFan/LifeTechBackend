-- Create client_digital_certificates table for Sprint 4.4
-- This table stores digital share certificates for clients in their digital wallet

CREATE TABLE client_digital_certificates (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id),
    certificate_number VARCHAR(100) UNIQUE NOT NULL,
    certificate_type VARCHAR(50) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    number_of_shares DECIMAL(15,4),
    issue_date DATE NOT NULL,
    digital_signature TEXT,
    blockchain_hash VARCHAR(255),
    certificate_data JSON,
    is_valid BOOLEAN DEFAULT TRUE,
    
    -- Additional fields for enhanced functionality
    share_class VARCHAR(50),
    nominal_value DECIMAL(10,4),
    current_market_value DECIMAL(15,2),
    transfer_restrictions TEXT,
    voting_rights BOOLEAN,
    dividend_entitlement BOOLEAN,
    last_transfer_date DATE,
    certificate_url VARCHAR(500),
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(36),
    version INTEGER DEFAULT 1
);

-- Create indexes for performance
CREATE INDEX idx_client_digital_certificates_client_id ON client_digital_certificates(client_id);
CREATE INDEX idx_client_digital_certificates_certificate_number ON client_digital_certificates(certificate_number);
CREATE INDEX idx_client_digital_certificates_certificate_type ON client_digital_certificates(certificate_type);
CREATE INDEX idx_client_digital_certificates_company_name ON client_digital_certificates(company_name);
CREATE INDEX idx_client_digital_certificates_issue_date ON client_digital_certificates(issue_date);
CREATE INDEX idx_client_digital_certificates_is_valid ON client_digital_certificates(is_valid);
CREATE INDEX idx_client_digital_certificates_blockchain_hash ON client_digital_certificates(blockchain_hash);
CREATE INDEX idx_client_digital_certificates_created_at ON client_digital_certificates(created_at);

-- Create composite indexes for common queries
CREATE INDEX idx_client_digital_certificates_client_valid ON client_digital_certificates(client_id, is_valid);
CREATE INDEX idx_client_digital_certificates_client_company ON client_digital_certificates(client_id, company_name);
CREATE INDEX idx_client_digital_certificates_client_type ON client_digital_certificates(client_id, certificate_type);

-- Add comment for documentation
COMMENT ON TABLE client_digital_certificates IS 'Stores digital share certificates for clients in their digital wallet';
COMMENT ON COLUMN client_digital_certificates.certificate_type IS 'Type: ORDINARY_SHARES, PREFERENCE_SHARES, CONVERTIBLE_SHARES, REDEEMABLE_SHARES, BONUS_SHARES, RIGHTS_SHARES, TREASURY_SHARES, OTHER';
COMMENT ON COLUMN client_digital_certificates.digital_signature IS 'Digital signature for certificate authenticity verification';
COMMENT ON COLUMN client_digital_certificates.blockchain_hash IS 'Hash stored on blockchain for immutable verification';
COMMENT ON COLUMN client_digital_certificates.certificate_data IS 'Additional certificate data in JSON format';
COMMENT ON COLUMN client_digital_certificates.certificate_url IS 'URL to download the certificate PDF';
COMMENT ON COLUMN client_digital_certificates.current_market_value IS 'Current market value of the shares';
COMMENT ON COLUMN client_digital_certificates.transfer_restrictions IS 'Any restrictions on share transfers';

-- Insert sample data for testing (if needed)
-- This will be handled by the application service for now 