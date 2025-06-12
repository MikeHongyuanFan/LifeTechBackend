# Sprint 3: Client App Core Features Requirements

## Overview
Sprint 3 focuses on implementing core client-facing features for the LifeTech Client App, including Investment Overview with visual analytics, Legal Document Center for document management, and enhanced Personal Profile functionality.

## Module 1: Investment Overview (Sprint 3.1)

### 1.1 Summary
- **Description**: View total invested amount, returns, balance
- **Priority**: High
- **Sprint**: 3.1

#### Requirements:
**Investment Summary Display:**
- Total invested amount across all investments
- Total returns generated to date
- Current balance/portfolio value
- Net profit/loss calculation
- Portfolio performance metrics

**Key Metrics:**
- Investment count by status (active/completed)
- Average return rate
- Best performing investment
- Portfolio growth over time
- Risk distribution analysis

### 1.2 Visual Charts
- **Description**: Investment and return graphs, visualisation
- **Priority**: High
- **Sprint**: 3.1

#### Requirements:
**Chart Types:**
- Portfolio value over time (line chart)
- Investment allocation by type (pie chart)
- Return performance comparison (bar chart)
- Monthly/quarterly performance trends
- Asset allocation visualization
**Interactive Features:**
- Drill-down capability
- Time range selection
- Investment type filtering
- Touch/click interactions for mobile
- Export chart as image

### 1.3 Filter
- **Description**: Time range, product type, etc.
- **Priority**: Medium
- **Sprint**: 3.1

#### Requirements:
**Filter Options:**
- Time range: Last 30 days, 90 days, 1 year, custom range
- Investment product type
- Investment status (active, completed, pending)
- Return type (capital growth, income, hybrid)

### 1.4 Monthly/Annual Reports
- **Description**: View/download investment reports
- **Priority**: Medium
- **Sprint**: 3.1

#### Requirements:
**Report Features:**
- Monthly investment statement
- Annual tax summary report
- Performance analysis report
- Portfolio composition report
- Transaction history report

**Download Options:**
- PDF format
- Excel/CSV export
- Email delivery option
- Report history access
- Secure download links

## Module 2: Investment Management (Sprint 3.2)

### 2.1 Active Investments
- **Description**: View ongoing investments: product name, rate, amount, start/end date, returns two category: ongoing /completed
- **Priority**: High
- **Sprint**: 3.2

#### Requirements:
**Investment Display:**
- Investment product name
- Current rate of return
- Investment amount
- Start date and maturity date
- Current returns generated
- Investment status indicator

**Categories:**
- **Ongoing Investments**: Active investments with live performance data
- **Completed Investments**: Matured investments with final returns

### 2.2 Investment History
- **Description**: View completed investments list and details
- **Priority**: Medium
- **Sprint**: 3.2

#### Requirements:
**History Features:**
- Chronological list of completed investments
- Final performance summary
- Total returns realized
- Investment duration
- Exit details and reasons

### 2.3 All Investment Product List
- **Description**: See all investment summary list and product details
- **Priority**: Medium
- **Sprint**: 3.2

#### Requirements:
**Product Listing:**
- Complete investment portfolio overview
- Individual investment details view
- Performance comparison
- Investment categorization
- Search and sorting capabilities

## Module 3: Legal Document Center (Sprint 3.3)

### 3.1 View Documents
- **Description**: View legal documents
- **Priority**: High
- **Sprint**: 3.3

#### Requirements:
**Document Categories:**
- Investment agreements
- Terms and conditions
- Regulatory disclosures
- Tax documentation
- Company policies
- Product disclosure statements

**Document Features:**
- Secure document viewer
- Document search functionality
- Document categorization
- Version control tracking
- Download permissions

### 3.2 Upload Files
- **Description**: Upload documents (e.g. updated bank info or KYC)
- **Priority**: High
- **Sprint**: 3.3

#### Requirements:
**Upload Capabilities:**
- Bank account information updates
- KYC documentation
- Tax file number updates
- Identity verification documents
- Address proof documents

**Upload Features:**
- Drag and drop interface
- Multiple file format support (PDF, JPG, PNG, DOC)
- File size validation
- Upload progress indicator
- Secure file transmission

### 3.3 Manage Files
- **Description**: Replace or delete uploaded documents
- **Priority**: Medium
- **Sprint**: 3.3

#### Requirements:
**File Management:**
- View uploaded documents list
- Replace existing documents
- Delete outdated documents
- Document status tracking
- Upload history

## Module 4: Personal Profile (Sprint 3.4)

### 4.1 View Info
- **Description**: Display: name, DOB, TFN, bank, tax info
- **Priority**: High
- **Sprint**: 3.4

#### Requirements:
**Profile Information Display:**
- Full name
- Date of birth
- Tax File Number (TFN)
- Bank account details
- Tax residency information
- Contact information
- Address details

### 4.2 Edit Profile
- **Description**: Modify editable fields (e.g. bank account)
- **Priority**: High
- **Sprint**: 3.4

#### Requirements:
**Editable Fields:**
- Bank account information
- Contact phone number
- Email address
- Residential address
- Emergency contact

**Non-Editable Fields:**
- Name (requires admin approval)
- Date of birth (requires admin approval)
- Tax File Number (requires verification)

### 4.3 KYC Status Tracking
- **Description**: View current KYC progress (passed/pending/missing)
- **Priority**: Medium
- **Sprint**: 3.4

#### Requirements:
**KYC Status Display:**
- Overall KYC status indicator
- Individual document status
- Missing document alerts
- Progress completion percentage
- Next steps guidance

**Status Types:**
- **PASSED**: KYC completed successfully
- **PENDING**: Under review
- **MISSING**: Missing required documents
- **REJECTED**: Requires resubmission

## API Endpoints Required

### Investment Overview APIs
```
GET    /api/client/investments/summary           - Get investment summary
GET    /api/client/investments/charts            - Get chart data
GET    /api/client/investments/performance       - Get performance metrics
GET    /api/client/reports                       - Get available reports
GET    /api/client/reports/{id}/download         - Download specific report
POST   /api/client/investments/filter            - Filter investments
```

### Investment Management APIs
```
GET    /api/client/investments/active            - Get active investments
GET    /api/client/investments/completed         - Get investment history
GET    /api/client/investments/all               - Get all investments
GET    /api/client/investments/{id}              - Get investment details
```

### Document Management APIs
```
GET    /api/client/documents                     - Get client documents
POST   /api/client/documents/upload              - Upload document
PUT    /api/client/documents/{id}                - Replace document
DELETE /api/client/documents/{id}                - Delete document
GET    /api/client/documents/{id}/download       - Download document
GET    /api/client/documents/categories          - Get document categories
```

### Profile Management APIs
```
GET    /api/client/profile                       - Get client profile
PUT    /api/client/profile                       - Update profile
GET    /api/client/profile/kyc-status            - Get KYC status
POST   /api/client/profile/bank-account          - Update bank account
PUT    /api/client/profile/contact-info          - Update contact information
```

## Database Schema Updates

### client_documents Table
```sql
CREATE TABLE client_documents (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id),
    document_name VARCHAR(255) NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    document_category VARCHAR(50) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    uploaded_by_client BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### client_reports Table
```sql
CREATE TABLE client_reports (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id),
    report_type VARCHAR(50) NOT NULL,
    report_name VARCHAR(255) NOT NULL,
    report_period_start DATE,
    report_period_end DATE,
    file_path VARCHAR(500),
    generated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_downloaded BOOLEAN DEFAULT FALSE,
    download_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Security Requirements

### Document Security
- Encrypted file storage
- Secure file transmission (HTTPS)
- Access control and permissions
- Audit logging for document access
- File type validation and scanning

### Data Privacy
- PII data encryption
- Secure API authentication
- Role-based access control
- Data retention policies
- GDPR compliance measures

## Technical Requirements

### Frontend Requirements
- Responsive design for mobile/tablet
- Progressive Web App (PWA) capabilities
- Offline data caching
- Touch-friendly interface
- Performance optimization

### Performance Requirements
- Page load time < 3 seconds
- Chart rendering < 2 seconds
- Document upload progress indication
- Efficient data pagination
- Image optimization

### Integration Requirements
- Real-time data synchronization
- Email notification integration
- SMS notification capability
- Third-party document scanning
- Bank account validation services

## Testing Requirements

### Functional Testing
- Investment calculation accuracy
- Document upload/download functionality
- Profile update validation
- KYC status tracking
- Report generation testing

### Security Testing
- Authentication and authorization
- Data encryption validation
- File upload security testing
- API security testing
- XSS and SQL injection prevention

### Performance Testing
- Load testing for concurrent users
- Chart rendering performance
- File upload/download speed
- Mobile device performance
- Database query optimization 