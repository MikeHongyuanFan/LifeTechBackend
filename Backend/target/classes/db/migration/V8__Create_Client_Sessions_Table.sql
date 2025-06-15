-- Create client_sessions table for remember me functionality and session management
CREATE TABLE client_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id BIGINT NOT NULL,
    session_token VARCHAR(255) UNIQUE NOT NULL,
    remember_me_token VARCHAR(255) UNIQUE,
    device_fingerprint VARCHAR(255),
    ip_address INET,
    user_agent TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    expires_at TIMESTAMP NOT NULL,
    remember_me_expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_accessed TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Add foreign key constraint to clients table
    CONSTRAINT fk_client_sessions_client_id 
        FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_client_sessions_client_id ON client_sessions(client_id);
CREATE INDEX idx_client_sessions_session_token ON client_sessions(session_token);
CREATE INDEX idx_client_sessions_remember_me_token ON client_sessions(remember_me_token);
CREATE INDEX idx_client_sessions_active ON client_sessions(is_active);
CREATE INDEX idx_client_sessions_expires_at ON client_sessions(expires_at);

-- Add comments for documentation
COMMENT ON TABLE client_sessions IS 'Stores client session information including remember me tokens';
COMMENT ON COLUMN client_sessions.session_token IS 'JWT session token for client authentication';
COMMENT ON COLUMN client_sessions.remember_me_token IS 'Long-lived token for remember me functionality';
COMMENT ON COLUMN client_sessions.device_fingerprint IS 'Unique identifier for client device';
COMMENT ON COLUMN client_sessions.expires_at IS 'When the session token expires';
COMMENT ON COLUMN client_sessions.remember_me_expires_at IS 'When the remember me token expires (typically 30 days)'; 