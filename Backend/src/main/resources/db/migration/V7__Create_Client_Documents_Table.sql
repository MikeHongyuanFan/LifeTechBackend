-- Migration script for Sprint 3.3: Legal Document Center
-- Creates client_documents table for document management functionality

CREATE TABLE client_documents (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    document_category VARCHAR(50) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    upload_date TIMESTAMP,
    uploaded_by_client BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    document_status VARCHAR(50) DEFAULT 'UPLOADED',
    version_number INTEGER DEFAULT 1,
    description TEXT,
    expiry_date TIMESTAMP,
    access_count INTEGER DEFAULT 0,
    last_accessed_date TIMESTAMP,
    tags VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint (assuming clients table exists)
    CONSTRAINT fk_client_documents_client_id 
        FOREIGN KEY (client_id) REFERENCES clients(id) 
        ON DELETE CASCADE,
    
    -- Indexes for performance
    INDEX idx_client_documents_client_id (client_id),
    INDEX idx_client_documents_type (document_type),
    INDEX idx_client_documents_category (document_category),
    INDEX idx_client_documents_status (document_status),
    INDEX idx_client_documents_upload_date (upload_date),
    INDEX idx_client_documents_active (is_active),
    INDEX idx_client_documents_uploaded_by_client (uploaded_by_client),
    INDEX idx_client_documents_expiry (expiry_date),
    
    -- Composite indexes for common queries
    INDEX idx_client_documents_client_active (client_id, is_active),
    INDEX idx_client_documents_client_type (client_id, document_type),
    INDEX idx_client_documents_client_category (client_id, document_category),
    INDEX idx_client_documents_client_status (client_id, document_status)
);

-- Add comments for documentation
COMMENT ON TABLE client_documents IS 'Stores client document information for Legal Document Center (Sprint 3.3)';
COMMENT ON COLUMN client_documents.client_id IS 'Reference to the client who owns this document';
COMMENT ON COLUMN client_documents.document_name IS 'Display name of the document';
COMMENT ON COLUMN client_documents.document_type IS 'Type of document (enum: INVESTMENT_AGREEMENT, KYC_DOCUMENT, etc.)';
COMMENT ON COLUMN client_documents.document_category IS 'Category of document (enum: LEGAL, COMPLIANCE, KYC, etc.)';
COMMENT ON COLUMN client_documents.file_path IS 'Physical path to the stored file';
COMMENT ON COLUMN client_documents.file_size IS 'Size of the file in bytes';
COMMENT ON COLUMN client_documents.mime_type IS 'MIME type of the file';
COMMENT ON COLUMN client_documents.upload_date IS 'Date when the document was uploaded';
COMMENT ON COLUMN client_documents.uploaded_by_client IS 'Whether the document was uploaded by client (true) or system (false)';
COMMENT ON COLUMN client_documents.is_active IS 'Whether the document is active (soft delete flag)';
COMMENT ON COLUMN client_documents.document_status IS 'Processing status (enum: UPLOADED, UNDER_REVIEW, APPROVED, REJECTED, etc.)';
COMMENT ON COLUMN client_documents.version_number IS 'Version number for document versioning';
COMMENT ON COLUMN client_documents.description IS 'Optional description of the document';
COMMENT ON COLUMN client_documents.expiry_date IS 'Optional expiry date for the document';
COMMENT ON COLUMN client_documents.access_count IS 'Number of times the document has been accessed';
COMMENT ON COLUMN client_documents.last_accessed_date IS 'Date when the document was last accessed';
COMMENT ON COLUMN client_documents.tags IS 'Comma-separated tags for document organization'; 