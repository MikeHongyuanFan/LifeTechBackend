# Sprint 1.4: Investment Product Management Module Requirements

## Overview
The Investment Product Management module enables comprehensive management of client investments, including investment records, financial tracking, entity management, and portfolio oversight. This module forms the core of the investment management platform.

## Module: Investment Product Management (Sprint 1.4)

### 1. Investment Lists & Portfolio Management

#### 1.1 Core Functionality
- **Description**: View and manage ongoing and completed investment records for all clients
- **Priority**: High
- **Sprint**: 1.4

#### 1.2 Requirements

**Investment List Features:**
- **Comprehensive Investment Views:**
  - All active investments across all clients
  - Individual client investment portfolios
  - Investment history and completed investments
  - Investment performance summaries
  - Filter by investment type, status, date range

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
- Sortable columns (amount, date, returns, status)
- Advanced filtering and search capabilities
- Export to Excel/CSV for reporting
- Bulk operations on multiple investments
- Real-time data updates and synchronization

### 2. Investment Record Creation & Management

#### 2.1 Core Functionality
- **Description**: Manually add and maintain detailed investment records in client profiles
- **Priority**: High
- **Sprint**: 1.4

#### 2.2 Requirements

**Investment Data Fields:**
- **Basic Information:**
  - Investment Name/Title
  - Investment Type/Category
  - Investment Description
  - Investment Objective
  - Risk Rating (Conservative, Moderate, Aggressive)

- **Financial Details:**
  - Initial Investment Amount
  - Payment Date
  - Expected Return Rate (%)
  - Expected Return Amount
  - Investment Term/Duration
  - Maturity Date

- **Return Information:**
  - Return Type (Capital Growth, Income, Hybrid)
  - Distribution Frequency (Monthly, Quarterly, Annually)
  - Current Value
  - Unrealized Gains/Losses
  - Realized Gains/Losses
  - Total Return to Date

- **Transaction Details:**
  - Purchase Price per Unit
  - Number of Units/Shares
  - Transaction Fees
  - Management Fees
  - Performance Fees
  - Tax Implications

**Investment Management Features:**
- **Create New Investments**: Structured forms for different investment types
- **Edit Existing Investments**: Update investment details and performance
- **Investment Validation**: Ensure data integrity and business rules
- **Investment Approval Workflow**: Multi-level approval for large investments
- **Investment Documentation**: Attach contracts, certificates, reports

### 3. Entity Management & Structure

#### 3.1 Core Functionality
- **Description**: Manage associated entities in client profiles including company structures and trust arrangements
- **Priority**: Medium
- **Sprint**: 1.4

#### 3.2 Requirements

**Entity Types:**
- **Individual Clients**: Personal investment accounts
- **Joint Accounts**: Multiple individual ownership
- **Companies**: Corporate investment entities
- **Trusts**: Family trusts, Unit trusts, Discretionary trusts
- **Self-Managed Super Funds (SMSF)**: Retirement investment vehicles
- **Partnerships**: Business partnership investments

**Entity Information Fields:**
- **Entity Identification:**
  - Entity Name
  - Entity Type
  - Registration Number (ABN, ACN, TFN)
  - Registration Date
  - Registered Address

- **Contact Information:**
  - Primary Contact Person
  - Phone Numbers
  - Email Addresses
  - Mailing Address
  - Physical Address

- **Legal Structure:**
  - Ownership Structure
  - Beneficial Owners
  - Directors/Trustees
  - Authorized Signatories
  - Power of Attorney arrangements

- **Tax & Compliance:**
  - Tax File Number
  - GST Registration
  - Tax Residency Status
  - Reporting Requirements
  - Compliance Obligations

**Entity Management Features:**
- **Add/Edit Entities**: Comprehensive entity profile management
- **Entity Relationships**: Link entities to clients and investments
- **Entity Hierarchy**: Manage complex ownership structures
- **Document Management**: Store entity documents and certificates
- **Compliance Tracking**: Monitor entity compliance requirements

### 4. Investment Performance Tracking

#### 4.1 Core Functionality
- **Description**: Track and analyze investment performance across portfolios
- **Priority**: High
- **Sprint**: 1.4

#### 4.2 Requirements

**Performance Metrics:**
- **Return Calculations:**
  - Total Return (Absolute and Percentage)
  - Annualized Return
  - Capital Growth vs Income Return
  - Risk-Adjusted Returns (Sharpe Ratio)
  - Benchmark Comparison

- **Portfolio Analytics:**
  - Asset Allocation Analysis
  - Diversification Metrics
  - Concentration Risk Analysis
  - Sector/Geographic Distribution
  - Risk Profile Assessment

- **Time-based Analysis:**
  - Performance over multiple periods
  - Rolling returns analysis
  - Volatility measurements
  - Correlation analysis
  - Historical performance trends

**Performance Reporting:**
- **Client Reports**: Customized performance reports for clients
- **Management Reports**: Internal investment analysis reports
- **Regulatory Reports**: Compliance and regulatory filing support
- **Tax Reports**: Capital gains and income tax reporting
- **Ad-hoc Analysis**: Custom performance analysis tools

## API Endpoints Required

### Investment Management APIs
```
POST   /api/admin/investments                     - Create new investment
GET    /api/admin/investments                     - Get all investments (paginated)
GET    /api/admin/investments/{id}                - Get specific investment
PUT    /api/admin/investments/{id}                - Update investment
DELETE /api/admin/investments/{id}                - Deactivate investment
GET    /api/admin/clients/{clientId}/investments  - Get client investments
POST   /api/admin/investments/bulk-import         - Bulk import investments
PUT    /api/admin/investments/bulk-update         - Bulk update investments
```

### Portfolio Management APIs
```
GET    /api/admin/portfolios/{clientId}           - Get client portfolio
GET    /api/admin/portfolios/summary              - Portfolio summary dashboard
POST   /api/admin/portfolios/rebalance            - Portfolio rebalancing
GET    /api/admin/portfolios/performance          - Performance analytics
GET    /api/admin/portfolios/risk-analysis        - Risk analysis report
```

### Entity Management APIs
```
POST   /api/admin/entities                        - Create new entity
GET    /api/admin/entities                        - Get all entities
GET    /api/admin/entities/{id}                   - Get specific entity
PUT    /api/admin/entities/{id}                   - Update entity
DELETE /api/admin/entities/{id}                   - Deactivate entity
GET    /api/admin/clients/{clientId}/entities     - Get client entities
POST   /api/admin/entities/{id}/documents         - Upload entity documents
```

### Performance Tracking APIs
```
GET    /api/admin/performance/summary             - Performance dashboard
GET    /api/admin/performance/{clientId}          - Client performance
GET    /api/admin/performance/benchmarks          - Benchmark comparisons
POST   /api/admin/performance/calculate           - Trigger performance calculation
GET    /api/admin/performance/reports/{type}      - Generate performance reports
```

## Database Schema Requirements

### investments Table
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
    actual_return_amount DECIMAL(15,2),
    units_purchased DECIMAL(15,4),
    purchase_price_per_unit DECIMAL(15,4),
    current_price_per_unit DECIMAL(15,4),
    transaction_fees DECIMAL(10,2),
    management_fees DECIMAL(10,2),
    performance_fees DECIMAL(10,2),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES admin_users(id),
    updated_by BIGINT REFERENCES admin_users(id)
);
```

### entities Table
```sql
CREATE TABLE entities (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES clients(id),
    entity_name VARCHAR(255) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    registration_number VARCHAR(50),
    abn VARCHAR(11),
    acn VARCHAR(9),
    tfn_encrypted TEXT,
    registration_date DATE,
    registered_street VARCHAR(255),
    registered_city VARCHAR(100),
    registered_state VARCHAR(100),
    registered_postal_code VARCHAR(20),
    registered_country VARCHAR(100),
    contact_person VARCHAR(255),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(255),
    tax_residency_status VARCHAR(50),
    gst_registered BOOLEAN DEFAULT false,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES admin_users(id)
);
```

### investment_performance Table
```sql
CREATE TABLE investment_performance (
    id BIGSERIAL PRIMARY KEY,
    investment_id BIGINT REFERENCES investments(id),
    performance_date DATE NOT NULL,
    market_value DECIMAL(15,2),
    book_value DECIMAL(15,2),
    unrealized_gain_loss DECIMAL(15,2),
    realized_gain_loss DECIMAL(15,2),
    income_received DECIMAL(15,2),
    total_return DECIMAL(15,2),
    return_percentage DECIMAL(8,4),
    benchmark_return DECIMAL(8,4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### investment_transactions Table
```sql
CREATE TABLE investment_transactions (
    id BIGSERIAL PRIMARY KEY,
    investment_id BIGINT REFERENCES investments(id),
    transaction_type VARCHAR(20) NOT NULL, -- BUY, SELL, DIVIDEND, INTEREST, FEE
    transaction_date DATE NOT NULL,
    units DECIMAL(15,4),
    price_per_unit DECIMAL(15,4),
    transaction_amount DECIMAL(15,2),
    fees DECIMAL(10,2),
    tax_amount DECIMAL(10,2),
    description TEXT,
    reference_number VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES admin_users(id)
);
```

## Security & Compliance Requirements

### Data Security
- **Financial Data Protection**: Encrypt sensitive financial information
- **Access Control**: Role-based access to investment data
- **Audit Trail**: Complete logging of all investment operations
- **Data Backup**: Regular automated backups of investment data
- **Disaster Recovery**: Comprehensive disaster recovery procedures

### Regulatory Compliance
- **Financial Services Compliance**: Adhere to ASIC regulations
- **Tax Compliance**: Accurate tax reporting and calculations
- **Privacy Compliance**: Protect client financial privacy
- **Record Keeping**: Maintain required financial records
- **Reporting Standards**: Comply with financial reporting standards

### Risk Management
- **Investment Risk Assessment**: Monitor and assess investment risks
- **Concentration Risk**: Alert for over-concentration in single investments
- **Compliance Monitoring**: Monitor compliance with investment mandates
- **Performance Monitoring**: Track investment performance against benchmarks
- **Fraud Detection**: Detect and prevent fraudulent investment activities

## Integration Requirements

### External Services
- **Market Data Feeds**: Real-time market data for investment valuation
- **Banking Integration**: Connect with banking systems for transactions
- **Tax Calculation**: Integration with tax calculation services
- **Compliance Systems**: Connect with regulatory compliance systems
- **Reporting Services**: External reporting and analytics platforms

### Internal Systems
- **Client Management**: Full integration with client profiles
- **KYC System**: Integration with KYC verification status
- **Document Management**: Link investment documents and contracts
- **Audit System**: Integration with audit and logging systems
- **Notification System**: Automated investment alerts and notifications

## Performance Requirements

### System Performance
- **Data Processing**: Process large volumes of investment data efficiently
- **Real-time Updates**: Near real-time investment value updates
- **Report Generation**: Generate complex reports within 30 seconds
- **Search Performance**: Investment searches returning results within 2 seconds
- **Scalability**: Support for 100,000+ investment records

### Data Management
- **Historical Data**: Maintain 10+ years of investment history
- **Data Accuracy**: 99.99% accuracy in investment calculations
- **Data Synchronization**: Real-time synchronization across systems
- **Backup & Recovery**: Daily backups with 1-hour RTO
- **Archive Management**: Automated archiving of old investment data

## Testing Requirements

### Functional Testing
- Investment creation and management workflows
- Portfolio calculation and performance tracking
- Entity management and relationship mapping
- API endpoint testing with various scenarios
- Integration testing with external data sources

### Financial Testing
- Investment calculation accuracy testing
- Performance metric validation
- Tax calculation verification
- Currency conversion testing
- Regulatory compliance testing

### Security Testing
- Financial data security testing
- Access control verification
- Audit trail validation
- Data encryption testing
- Fraud detection testing

## Success Criteria

1. **Accuracy**: 99.99% accuracy in investment calculations and reporting
2. **Performance**: All investment operations complete within acceptable timeframes
3. **Compliance**: 100% compliance with financial regulations and standards
4. **Security**: Zero security breaches in financial data handling
5. **User Satisfaction**: High satisfaction from investment managers and clients
6. **Scalability**: System supports projected growth in investment volume

## Dependencies

- Client Management Module (Sprint 1.2) - Required for client data
- KYC Verification Module (Sprint 1.3) - Required for compliance
- Admin User Management (Sprint 1.1) - Required for user access control
- External market data providers - Required for investment valuation
- Banking integration services - Required for transaction processing

## Estimated Development Time
- **Backend Development**: 5-6 weeks
- **Frontend Development**: 4-5 weeks
- **Integration Development**: 3-4 weeks
- **Testing & QA**: 2-3 weeks
- **Total Estimated Time**: 14-18 weeks

## Future Enhancements
- **Automated Investment Processing**: AI-powered investment recommendations
- **Advanced Analytics**: Predictive analytics for investment performance
- **Mobile App Integration**: Mobile access for investment monitoring
- **Robo-Advisory**: Automated portfolio management and rebalancing
- **ESG Integration**: Environmental, Social, and Governance investment tracking
- **Cryptocurrency Support**: Native support for digital asset investments
- **Alternative Investment Platforms**: Integration with alternative investment platforms 