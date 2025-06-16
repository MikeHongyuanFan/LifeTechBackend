-- Create client_wallet_integrations table for Sprint 4.4
-- This table stores integrations between clients and external financial platforms

CREATE TABLE client_wallet_integrations (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id),
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
    version INTEGER DEFAULT 1
);

-- Create indexes for performance
CREATE INDEX idx_client_wallet_integrations_client_id ON client_wallet_integrations(client_id);
CREATE INDEX idx_client_wallet_integrations_platform_name ON client_wallet_integrations(platform_name);
CREATE INDEX idx_client_wallet_integrations_platform_type ON client_wallet_integrations(platform_type);
CREATE INDEX idx_client_wallet_integrations_status ON client_wallet_integrations(integration_status);
CREATE INDEX idx_client_wallet_integrations_active ON client_wallet_integrations(is_active);
CREATE INDEX idx_client_wallet_integrations_created_at ON client_wallet_integrations(created_at);

-- Create unique constraint to prevent duplicate integrations
CREATE UNIQUE INDEX idx_client_wallet_integrations_unique_platform 
    ON client_wallet_integrations(client_id, platform_name) 
    WHERE is_active = true;

-- Add comment for documentation
COMMENT ON TABLE client_wallet_integrations IS 'Stores integrations between clients and external financial platforms for digital wallet functionality';
COMMENT ON COLUMN client_wallet_integrations.platform_type IS 'Type of platform: BANK_ACCOUNT, BROKERAGE, CRYPTOCURRENCY, SUPERANNUATION, PROPERTY, INSURANCE, PEER_TO_PEER, OTHER';
COMMENT ON COLUMN client_wallet_integrations.integration_status IS 'Status: CONNECTED, DISCONNECTED, ERROR, PENDING, EXPIRED';
COMMENT ON COLUMN client_wallet_integrations.sync_frequency IS 'Frequency: REAL_TIME, HOURLY, DAILY, WEEKLY, MANUAL';
COMMENT ON COLUMN client_wallet_integrations.api_credentials_encrypted IS 'Encrypted API credentials for platform access';

-- Insert sample data for testing (if needed)
-- This will be handled by the application service for now 