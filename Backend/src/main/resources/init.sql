-- Finance Admin Management System - Database Initialization Script
-- This script runs when the PostgreSQL container starts for the first time

-- Ensure the database exists (it should already be created by POSTGRES_DB)
-- CREATE DATABASE IF NOT EXISTS finance_admin;

-- Set default timezone
SET timezone = 'UTC';

-- Enable necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Grant all privileges to admin user (should already be done but ensuring)
GRANT ALL PRIVILEGES ON DATABASE finance_admin TO admin;

-- Set default search path
ALTER DATABASE finance_admin SET search_path TO public;

-- Create schemas if needed (optional for future use)
-- CREATE SCHEMA IF NOT EXISTS audit;
-- CREATE SCHEMA IF NOT EXISTS security;

-- Log initialization completion
DO $$
BEGIN
    RAISE NOTICE 'Finance Admin Management System database initialized successfully';
END $$; 