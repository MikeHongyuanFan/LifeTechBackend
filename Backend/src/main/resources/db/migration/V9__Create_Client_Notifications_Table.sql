-- Create client_notifications table for notification management
CREATE TABLE client_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id BIGINT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    delivery_method VARCHAR(20) DEFAULT 'IN_APP',
    delivery_status VARCHAR(20) DEFAULT 'PENDING',
    priority_level VARCHAR(20) DEFAULT 'NORMAL',
    scheduled_at TIMESTAMP,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP,
    expires_at TIMESTAMP,
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Add foreign key constraint to clients table
    CONSTRAINT fk_client_notifications_client_id 
        FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_client_notifications_client_id ON client_notifications(client_id);
CREATE INDEX idx_client_notifications_type ON client_notifications(notification_type);
CREATE INDEX idx_client_notifications_category ON client_notifications(category);
CREATE INDEX idx_client_notifications_is_read ON client_notifications(is_read);
CREATE INDEX idx_client_notifications_delivery_status ON client_notifications(delivery_status);
CREATE INDEX idx_client_notifications_created_at ON client_notifications(created_at);
CREATE INDEX idx_client_notifications_scheduled_at ON client_notifications(scheduled_at);

-- Create notification preferences table
CREATE TABLE client_notification_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id BIGINT NOT NULL UNIQUE,
    general_notices JSON DEFAULT '{"welcomeMessages": true, "birthdays": true, "announcements": true, "deliveryMethod": "EMAIL_AND_PUSH"}',
    kyc_alerts JSON DEFAULT '{"documentRequests": true, "verificationResults": true, "statusUpdates": true, "deliveryMethod": "EMAIL_AND_SMS"}',
    investment_alerts JSON DEFAULT '{"newOpportunities": true, "maturityNotifications": true, "returnDistributions": true, "deliveryMethod": "PUSH_AND_EMAIL"}',
    report_reminders JSON DEFAULT '{"monthlyReports": true, "annualStatements": true, "taxDocuments": true, "deliveryMethod": "EMAIL"}',
    global_settings JSON DEFAULT '{"enabled": true, "quietHours": {"start": "22:00", "end": "08:00"}, "timezone": "UTC"}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Add foreign key constraint to clients table
    CONSTRAINT fk_client_notification_preferences_client_id 
        FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

-- Create index for client notification preferences
CREATE INDEX idx_client_notification_preferences_client_id ON client_notification_preferences(client_id);

-- Create notification templates table
CREATE TABLE notification_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_name VARCHAR(100) NOT NULL UNIQUE,
    notification_type VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    title_template VARCHAR(255) NOT NULL,
    message_template TEXT NOT NULL,
    default_delivery_method VARCHAR(20) DEFAULT 'IN_APP',
    default_priority VARCHAR(20) DEFAULT 'NORMAL',
    is_active BOOLEAN DEFAULT TRUE,
    variables JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for notification templates
CREATE INDEX idx_notification_templates_type ON notification_templates(notification_type);
CREATE INDEX idx_notification_templates_category ON notification_templates(category);
CREATE INDEX idx_notification_templates_active ON notification_templates(is_active);

-- Insert default notification templates
INSERT INTO notification_templates (template_name, notification_type, category, title_template, message_template, variables) VALUES
('WELCOME_MESSAGE', 'GENERAL', 'WELCOME', 'Welcome to LifeTech!', 'Dear {{clientName}}, welcome to LifeTech! Your account has been successfully created. We''re excited to help you on your investment journey.', '{"clientName": "string"}'),
('BIRTHDAY_GREETING', 'GENERAL', 'BIRTHDAY', 'Happy Birthday!', 'Happy Birthday {{clientName}}! 🎉 Wishing you a wonderful year ahead filled with prosperity and success.', '{"clientName": "string"}'),
('KYC_DOCUMENT_REQUEST', 'KYC', 'DOCUMENT_REQUEST', 'Document Required for Verification', 'Dear {{clientName}}, we need additional documents to complete your verification. Please upload: {{documentType}}.', '{"clientName": "string", "documentType": "string"}'),
('KYC_VERIFICATION_COMPLETE', 'KYC', 'VERIFICATION_RESULT', 'Verification Complete', 'Congratulations {{clientName}}! Your KYC verification has been completed successfully. You can now access all platform features.', '{"clientName": "string"}'),
('INVESTMENT_OPPORTUNITY', 'INVESTMENT', 'NEW_OPPORTUNITY', 'New Investment Opportunity', 'Dear {{clientName}}, a new investment opportunity is available: {{investmentName}}. Expected return: {{expectedReturn}}%.', '{"clientName": "string", "investmentName": "string", "expectedReturn": "number"}'),
('INVESTMENT_MATURITY', 'INVESTMENT', 'MATURITY', 'Investment Maturity Notice', 'Your investment {{investmentName}} has matured. Total return: ${{totalReturn}}. Funds will be credited to your account.', '{"investmentName": "string", "totalReturn": "number"}'),
('RETURN_DISTRIBUTION', 'RETURN', 'DISTRIBUTION', 'Return Distribution', 'Dear {{clientName}}, your investment {{investmentName}} has generated a return of ${{returnAmount}}. This has been credited to your account.', '{"clientName": "string", "investmentName": "string", "returnAmount": "number"}'),
('MONTHLY_REPORT_READY', 'REPORT', 'MONTHLY_REPORT', 'Monthly Report Available', 'Your monthly investment report for {{month}} {{year}} is now available for download in your account.', '{"month": "string", "year": "number"}'),
('ANNUAL_STATEMENT_READY', 'REPORT', 'ANNUAL_STATEMENT', 'Annual Statement Available', 'Your annual investment statement for {{year}} is now available for download. Please review your portfolio performance.', '{"year": "number"}'); 