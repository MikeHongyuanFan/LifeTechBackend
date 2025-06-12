# Sprint 1.2: Client Management Module Requirements

## Overview
The Client Management module enables admin users to create, manage, and track client profiles within the Finance Admin Management System. This module provides comprehensive client lifecycle management from registration to ongoing maintenance.

## Module: Client Management (Sprint 1.2)

### 1. Create New Client Profile

#### 1.1 Core Functionality
- **Description**: Allow admin users to create new client profiles with comprehensive information collection
- **Priority**: High
- **Sprint**: 1.2

#### 1.2 Requirements

**Functional Requirements:**
- Admin users can create new client profiles through a structured form
- Support for both individual and entity clients
- Mandatory field validation before profile creation
- Auto-generation of unique client membership numbers
- Integration with existing user authentication system

**Data Fields Required:**
- **Personal Information:**
  - Full Name (First, Middle, Last)
  - Date of Birth
  - Phone Number (Primary, Secondary)
  - Email Address (Primary, Secondary)
  - Physical Address (Street, City, State, Postal Code, Country)
  - Mailing Address (if different)

- **Financial Information:**
  - Tax File Number (TFN)
  - Tax Residency Status
  - Bank Account Details (BSB, Account Number, Account Name)
  - Investment Target/Goals
  - Risk Profile Assessment

- **System Fields:**
  - Client ID (Auto-generated)
  - Membership Number (Auto-generated)
  - Created Date/Time
  - Created By (Admin User)
  - Status (Active, Inactive, Pending)

**Technical Requirements:**
- RESTful API endpoints for client creation
- Input validation and sanitization
- Encrypted storage of sensitive information (TFN, Bank Details)
- Audit logging for all client creation activities

**Blockchain Integration:**
- **Identity Verification**: Utilize blockchain for tamper-proof identity verification through on-chain storage of hashed client identifiers. Client identity hashes (SHA-256 of TFN + DOB + email) are stored on-chain to create immutable identity anchors that can be used for verification purposes without exposing sensitive data.
- **Tamper-proof Audit Logging**: Implement blockchain-based audit logging for all client profile updates and critical operations. Each significant client data modification triggers a blockchain transaction that creates an immutable audit trail, ensuring complete transparency and preventing unauthorized alterations to client records.

### 2. Portal Login System

#### 2.1 Core Functionality
- **Description**: Secure authentication system for client portal access
- **Priority**: High
- **Sprint**: 1.2

#### 2.2 Requirements

**Authentication Methods:**
- Email + Password login
- Mobile Number + Password login (optional)
- Multi-factor authentication support

**Password Management:**
- Password reset functionality via email
- Secure password requirements enforcement
- Password expiry and rotation policies

**Security Features:**
- Account lockout after failed attempts
- Session management and timeout
- IP address logging and monitoring
- Encrypted credential storage

**Technical Implementation:**
- JWT token-based authentication
- bcrypt password hashing
- Rate limiting for login attempts
- HTTPS enforcement

### 3. Client Information Management

#### 3.1 Core Functionality
- **Description**: Comprehensive client data management and editing capabilities
- **Priority**: High
- **Sprint**: 1.2

#### 3.2 Requirements

**Edit Capabilities:**
- Full profile editing by admin users
- Field-level access control
- Version history tracking
- Bulk update functionality

**Data Validation:**
- Real-time validation during editing
- Data format verification (email, phone, TFN)
- Duplicate detection and prevention
- Mandatory field enforcement

**Change Management:**
- Audit trail for all modifications
- Approval workflow for sensitive changes
- Change notification system
- Rollback capabilities

### 4. Search & Filter System

#### 4.1 Core Functionality
- **Description**: Advanced search and filtering capabilities for client management
- **Priority**: Medium
- **Sprint**: 1.2

#### 4.2 Requirements

**Search Capabilities:**
- Full-text search across client data
- Advanced search with multiple criteria
- Auto-complete suggestions
- Search result highlighting

**Filter Options:**
- Filter by Name (First, Last, Full)
- Filter by Membership Number
- Filter by Client ID
- Filter by Status (Active, Inactive, Pending)
- Filter by Registration Date Range
- Filter by Location/State
- Filter by Investment Amount Range

**Performance Requirements:**
- Search results within 2 seconds
- Pagination for large result sets
- Export filtered results to CSV/Excel
- Saved search functionality

### 5. Login Tracking & Activity Monitoring

#### 5.1 Core Functionality
- **Description**: Monitor and track client login activities and profile access
- **Priority**: Medium
- **Sprint**: 1.2

#### 5.2 Requirements

**Tracking Features:**
- Last login timestamp display
- Login frequency analytics
- Failed login attempt tracking
- Session duration monitoring

**Admin Dashboard Integration:**
- Client activity overview
- Inactive client identification
- Login pattern analysis
- Security incident alerting

**Data Retention:**
- 12-month login history retention
- Audit log integration
- Privacy compliance (GDPR, Privacy Act)
- Data anonymization options

## API Endpoints Required

### Client Management APIs
```
POST   /api/admin/clients              - Create new client
GET    /api/admin/clients              - Get all clients (with pagination)
GET    /api/admin/clients/{id}         - Get client by ID
PUT    /api/admin/clients/{id}         - Update client information
DELETE /api/admin/clients/{id}         - Deactivate client
GET    /api/admin/clients/search       - Search clients
POST   /api/admin/clients/bulk-update  - Bulk update clients
```

### Authentication APIs
```
POST   /api/client/auth/login          - Client login
POST   /api/client/auth/logout         - Client logout
POST   /api/client/auth/forgot-password - Password reset request
POST   /api/client/auth/reset-password  - Password reset confirmation
GET    /api/client/auth/profile        - Get client profile
```

### Activity Tracking APIs
```
GET    /api/admin/clients/{id}/activity     - Get client activity log
GET    /api/admin/clients/activity/summary  - Activity summary dashboard
```

### Blockchain Integration APIs
```
POST   /api/admin/clients/{id}/anchor-identity - Anchor client identity hash on blockchain
```

## Database Schema Requirements

### clients Table
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
    address_country VARCHAR(100),
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
    blockchain_identity_hash VARCHAR(128),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES admin_users(id),
    updated_by BIGINT REFERENCES admin_users(id)
);
```

### client_login_history Table
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
    failure_reason VARCHAR(255)
);
```

## Security Requirements

### Data Protection
- Encrypt sensitive fields (TFN, Bank Account Numbers)
- Implement field-level access control
- Regular security audits and penetration testing
- GDPR compliance for data handling

### Authentication Security
- Strong password policy enforcement
- Multi-factor authentication support
- Session management and timeout
- Brute force protection

### Audit Requirements
- Complete audit trail for all client operations
- Immutable log storage
- Real-time monitoring and alerting
- Compliance reporting capabilities

### Blockchain Security
- **Immutable Audit Trails**: Leverage blockchain technology to create tamper-proof audit logs for all client profile modifications and critical operations. Each audit entry is cryptographically signed and stored on-chain, providing indisputable evidence of all system activities.
- **On-chain Identity Storage**: Secure storage of client identity hashes (SHA-256 of TFN + DOB + email) on blockchain ensures immutable identity verification without exposing sensitive personal information. This creates a permanent, verifiable record of client identity that cannot be altered or deleted.

## Testing Requirements

### Unit Tests
- API endpoint testing
- Data validation testing  
- Authentication flow testing
- Search and filter functionality

### Integration Tests
- Database integration testing
- Authentication system integration
- Email notification testing
- Audit logging verification

### User Acceptance Tests
- Client creation workflow
- Search and filter functionality
- Login and authentication flows
- Admin dashboard integration

## Success Criteria

1. **Functionality**: All client management features working as specified
2. **Performance**: Search results within 2 seconds, page loads under 3 seconds
3. **Security**: Pass security audit with no critical vulnerabilities
4. **Usability**: Admin users can complete tasks with minimal training
5. **Reliability**: 99.9% uptime with proper error handling
6. **Compliance**: Meet all regulatory requirements for data handling

## Dependencies

- Admin User Management Module (Sprint 1.1) ✅ Completed
- Database infrastructure setup
- Email notification system
- Audit logging framework
- Authentication and authorization system

## Estimated Development Time
- **Backend Development**: 3-4 weeks
- **Frontend Development**: 2-3 weeks  
- **Testing & QA**: 1-2 weeks
- **Total Estimated Time**: 6-9 weeks

## Notes
- This module forms the foundation for KYC Verification (Sprint 1.3)
- Integration points with Investment Management (Sprint 1.4)
- Consider future mobile app requirements in API design 