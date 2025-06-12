# Sprint 1.4: Investment Product Management Module Requirements

## Overview
The Investment Product Management module enables comprehensive management of client investments through data entry operations. This module provides investment record management with manual data entry capabilities for ongoing and completed investment records.

## Module: Investment Product Management (Sprint 1.4) - Only Data Entry

### 1. Investment Lists

#### 1.1 Core Functionality
- **Description**: View ongoing and completed investment records
- **Priority**: High
- **Sprint**: 1.4
- **Note**: Only data entry functionality

#### 1.2 Requirements

**Investment List Features:**
- **Investment Record Views:**
  - All active investments across all clients
  - Individual client investment portfolios
  - Investment history and completed investments
  - Investment status tracking
  - Basic investment information display

- **Investment Categories:**
  - **Property Investments**: Residential, Commercial, Industrial
  - **Equity Investments**: Listed shares, Private equity
  - **Fixed Income**: Bonds, Term deposits, Government securities
  - **Alternative Investments**: Commodities, Cryptocurrency, Collectibles
  - **Managed Funds**: Mutual funds, ETFs, Hedge funds
  - **Direct Investments**: Business investments, Startups

**Investment Status Types:**
- **PENDING**: Investment committed but not yet funded
- **ACTIVE**: Investment is live and generating returns
- **MATURED**: Investment has reached maturity/completion
- **PARTIAL_EXIT**: Partial divestment has occurred
- **FULLY_EXITED**: Investment completely divested
- **SUSPENDED**: Investment temporarily halted
- **DEFAULTED**: Investment has failed/defaulted

**List Management Features:**
- View investment records in tabular format
- Basic sorting by date, amount, status
- Simple search functionality
- Export to Excel/CSV for basic reporting
- Pagination for large datasets

### 2. Add Investment

#### 2.1 Core Functionality
- **Description**: Manually add new investment record in specific client's profile, input investment info (payment date, investment amount, and return details)
- **Priority**: High
- **Sprint**: 1.4
- **Note**: Manual data entry only

#### 2.2 Requirements

**Investment Data Entry Fields:**
- **Basic Information:**
  - Investment Name/Title
  - Investment Type/Category
  - Investment Description
  - Client Association (link to client profile)

- **Financial Details:**
  - Payment Date
  - Investment Amount
  - Expected Return Rate (%)
  - Expected Return Amount
  - Investment Term/Duration
  - Maturity Date

- **Return Information:**
  - Return Type (Capital Growth, Income, Hybrid)
  - Distribution Frequency (Monthly, Quarterly, Annually)
  - Current Value (manual entry)
  - Realized Gains/Losses (manual entry)
  - Total Return to Date (manual entry)

- **Transaction Details:**
  - Purchase Price per Unit
  - Number of Units/Shares
  - Transaction Fees
  - Management Fees
  - Performance Fees

**Data Entry Features:**
- **Form-based Investment Creation**: Structured forms for different investment types
- **Client Selection**: Dropdown to select client for investment association
- **Data Validation**: Basic validation for required fields and data formats
- **Save and Continue**: Ability to save partial entries
- **Confirmation**: Review screen before final submission

**Technical Requirements:**
- Simple form-based interface
- Basic field validation
- Database storage of investment records
- Audit trail for investment creation
- Integration with client management system

### 3. Investment Edit

#### 3.1 Core Functionality
- **Description**: Edit/delete investment records
- **Priority**: Medium
- **Sprint**: 1.4
- **Note**: Basic edit and delete functionality

#### 3.2 Requirements

**Edit Capabilities:**
- **Update Investment Information**: Modify existing investment details
- **Edit Financial Data**: Update amounts, dates, and return information
- **Status Changes**: Update investment status
- **Performance Updates**: Manual entry of current values and returns
- **Fee Adjustments**: Update fee information

**Delete Functionality:**
- **Soft Delete**: Mark investments as inactive rather than permanent deletion
- **Delete Confirmation**: Confirmation dialog before deletion
- **Audit Trail**: Record deletion activities in audit logs
- **Restore Capability**: Ability to restore soft-deleted records

**Edit Features:**
- Pre-populated forms with existing data
- Field-level editing permissions
- Change tracking and audit logging
- Validation on updates
- Confirmation before saving changes

**Data Management:**
- Version history for investment records
- Change log with timestamps
- User attribution for modifications
- Data integrity checks
- Rollback capabilities for critical errors

## API Endpoints Required

### Investment Management APIs
```
POST   /api/admin/investments                     - Create new investment
GET    /api/admin/investments                     - Get all investments (paginated)
GET    /api/admin/investments/{id}                - Get specific investment
PUT    /api/admin/investments/{id}                - Update investment
DELETE /api/admin/investments/{id}                - Soft delete investment
GET    /api/admin/clients/{clientId}/investments  - Get client investments
POST   /api/admin/investments/search              - Search investments
GET    /api/admin/investments/export              - Export investment data
```

### Investment Data Entry APIs
```
GET    /api/admin/investments/types               - Get investment types
GET    /api/admin/investments/statuses            - Get investment statuses
POST   /api/admin/investments/validate            - Validate investment data
GET    /api/admin/investments/{id}/history        - Get investment change history
PUT    /api/admin/investments/{id}/restore        - Restore soft-deleted investment
```

## Database Schema

### Investments Table
```sql
CREATE TABLE investments (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id),
    investment_name VARCHAR(255) NOT NULL,
    investment_type VARCHAR(50) NOT NULL,
    investment_description TEXT,
    
    -- Financial Details
    payment_date DATE NOT NULL,
    investment_amount DECIMAL(15,2) NOT NULL,
    expected_return_rate DECIMAL(5,2),
    expected_return_amount DECIMAL(15,2),
    investment_term_months INTEGER,
    maturity_date DATE,
    
    -- Return Information
    return_type VARCHAR(20) DEFAULT 'HYBRID',
    distribution_frequency VARCHAR(20),
    current_value DECIMAL(15,2),
    realized_gains_losses DECIMAL(15,2) DEFAULT 0,
    total_return_to_date DECIMAL(15,2) DEFAULT 0,
    
    -- Transaction Details
    purchase_price_per_unit DECIMAL(10,4),
    number_of_units DECIMAL(15,4),
    transaction_fees DECIMAL(10,2) DEFAULT 0,
    management_fees DECIMAL(10,2) DEFAULT 0,
    performance_fees DECIMAL(10,2) DEFAULT 0,
    
    -- Status and Metadata
    status VARCHAR(20) DEFAULT 'PENDING',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NOT NULL,
    updated_by VARCHAR(36),
    version INTEGER DEFAULT 1
);

CREATE INDEX idx_investments_client ON investments(client_id);
CREATE INDEX idx_investments_type ON investments(investment_type);
CREATE INDEX idx_investments_status ON investments(status);
CREATE INDEX idx_investments_payment_date ON investments(payment_date);
```

### Investment History Table
```sql
CREATE TABLE investment_history (
    id BIGSERIAL PRIMARY KEY,
    investment_id BIGINT NOT NULL REFERENCES investments(id),
    change_type VARCHAR(20) NOT NULL, -- CREATE, UPDATE, DELETE, RESTORE
    changed_fields JSONB,
    old_values JSONB,
    new_values JSONB,
    changed_by VARCHAR(36) NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_reason TEXT
);

CREATE INDEX idx_investment_history_investment ON investment_history(investment_id);
CREATE INDEX idx_investment_history_date ON investment_history(changed_at);
```

## Technical Requirements

### Data Entry Interface
- Simple, intuitive form-based interface
- Client-side validation for immediate feedback
- Auto-save functionality for long forms
- Responsive design for mobile/tablet access
- Clear error messaging and validation feedback

### Data Management
- Comprehensive audit logging for all operations
- Data validation and integrity checks
- Backup and recovery procedures
- Data export capabilities
- Integration with existing client management system

### Performance Requirements
- Form loading time: < 2 seconds
- Data save operations: < 3 seconds
- Investment list loading: < 5 seconds
- Search operations: < 3 seconds
- Export generation: < 30 seconds for standard datasets

### Security Requirements
- Role-based access control for investment data
- Audit logging for all data entry operations
- Data encryption for sensitive financial information
- User authentication and authorization
- Data privacy compliance

## User Interface Requirements

### Investment List Interface
- Tabular display with sortable columns
- Search and filter capabilities
- Pagination controls
- Export buttons
- Quick action buttons (Edit, Delete, View)

### Investment Entry Form
- Multi-section form with logical grouping
- Progress indicators for multi-step forms
- Field validation with real-time feedback
- Save draft functionality
- Clear navigation between sections

### Investment Edit Interface
- Pre-populated forms with existing data
- Highlighted changed fields
- Change confirmation dialogs
- Version comparison capabilities
- Audit trail display

## Success Criteria

### Functional Success
- ✅ Investment records can be created successfully
- ✅ Investment data can be edited and updated
- ✅ Investment records can be soft deleted and restored
- ✅ Investment lists display correctly with proper filtering
- ✅ Data validation prevents invalid entries

### Data Integrity Success
- ✅ All investment data is accurately stored
- ✅ Audit trails capture all changes
- ✅ Data relationships maintained correctly
- ✅ No data loss during operations
- ✅ Backup and recovery procedures tested

### User Experience Success
- ✅ Intuitive data entry interface
- ✅ Quick and responsive form interactions
- ✅ Clear error messaging and validation
- ✅ Efficient workflow for investment management
- ✅ Mobile-friendly interface design

## Limitations and Scope

### Current Scope (Sprint 1.4)
- **Data Entry Only**: Focus on manual data entry and basic CRUD operations
- **No Automated Calculations**: Return calculations are manual entries
- **No Real-time Data**: No integration with market data feeds
- **Basic Reporting**: Simple export functionality only
- **No Advanced Analytics**: No performance analysis or complex calculations

### Future Enhancements (Later Sprints)
- Automated return calculations
- Real-time market data integration
- Advanced performance analytics
- Automated report generation
- Portfolio optimization tools
- Risk assessment capabilities
- Integration with external financial systems 