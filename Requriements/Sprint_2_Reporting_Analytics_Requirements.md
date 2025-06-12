# Sprint 2.2: Dashboard Module Requirements

## Overview
The Dashboard module provides an overview of clients data summary with real-time visualization of investment status, automated birthday congratulations, recent enquiries management, email integration, filtering capabilities, and export functionality.

## Module: Dashboard (Sprint 2.2)

### 1. Overview of Clients Data Summary

#### 1.1 Core Functionality
- **Description**: Display total number of general information: clients, enquiries and upcoming birthday client list
- **Priority**: High
- **Sprint**: 2.2

#### 1.2 Requirements

**General Information Display:**
- **Client Statistics:**
  - Total number of clients
  - Active clients count
  - Inactive clients count
  - New clients this month/quarter
  - Client growth rate

- **Enquiry Statistics:**
  - Total enquiries count
  - Pending enquiries
  - Resolved enquiries
  - Enquiry response time metrics
  - Enquiry conversion rate

- **Upcoming Birthday Client List:**
  - Clients with birthdays in next 7 days
  - Clients with birthdays in next 30 days
  - Birthday reminder notifications
  - Automated birthday greeting status

**Dashboard Layout:**
- Clean, intuitive interface design
- Real-time data updates
- Responsive design for mobile/tablet access
- Quick navigation to detailed views
- Customizable widget arrangement

### 2. Real-time Visualization of Investment Status

#### 2.1 Core Functionality
- **Description**: Total investment value, total number of client numbers, returns
- **Priority**: High
- **Sprint**: 2.2

#### 2.2 Requirements

**Investment Metrics Display:**
- **Total Investment Value:**
  - Current total portfolio value
  - Investment value by asset class
  - Investment value by client segment
  - Month-over-month growth
  - Year-over-year performance

- **Client Investment Statistics:**
  - Total number of investing clients
  - Average investment per client
  - Investment distribution analysis
  - Client investment activity trends
  - New investments this period

- **Returns Analysis:**
  - Total returns generated
  - Average return rate
  - Best performing investments
  - Underperforming investments
  - Risk-adjusted returns

**Visualization Components:**
- Interactive charts and graphs
- Real-time data updates (< 30 seconds)
- Drill-down capabilities
- Export functionality for charts
- Mobile-responsive visualizations

### 3. Birthday Congratulations

#### 3.1 Core Functionality
- **Description**: Send birthday congratulation email to client automatically
- **Priority**: Medium
- **Sprint**: 2.2

#### 3.2 Requirements

**Automated Birthday System:**
- **Email Automation:**
  - Automatic birthday email sending
  - Customizable email templates
  - Personalized birthday messages
  - Email delivery confirmation
  - Failed delivery retry mechanism

- **Birthday Management:**
  - Daily birthday check process
  - Birthday notification to admin users
  - Birthday email scheduling
  - Birthday campaign tracking
  - Opt-out management for clients

**Email Template Features:**
- Professional email design
- Company branding integration
- Personalization tokens (name, age, etc.)
- HTML and plain text versions
- Mobile-responsive email design

### 4. Recent Enquiries List

#### 4.1 Core Functionality
- **Description**: Show recent enquires list in descending time order
- **Priority**: Medium
- **Sprint**: 2.2

#### 4.2 Requirements

**Enquiry List Features:**
- **Display Information:**
  - Enquiry date and time
  - Client name and contact details
  - Enquiry type and category
  - Enquiry status (New, In Progress, Resolved)
  - Assigned staff member
  - Priority level

- **List Management:**
  - Descending chronological order
  - Pagination for large lists
  - Quick status updates
  - Bulk actions capability
  - Search and filter options

**Enquiry Tracking:**
- Response time tracking
- Escalation alerts for overdue enquiries
- Performance metrics by staff
- Client satisfaction tracking
- Follow-up reminders

### 5. Email Integration

#### 5.1 Core Functionality
- **Description**: Sync enquiries from email inbox
- **Priority**: Medium
- **Sprint**: 2.2

#### 5.2 Requirements

**Email Synchronization:**
- **Inbox Integration:**
  - Connect to email accounts (IMAP/POP3)
  - Automatic enquiry detection
  - Email parsing and categorization
  - Duplicate enquiry prevention
  - Email thread tracking

- **Enquiry Creation:**
  - Auto-create enquiries from emails
  - Extract client information
  - Categorize enquiry types
  - Assign priority levels
  - Route to appropriate staff

**Email Management:**
- Email archiving after processing
- Email attachment handling
- Spam and irrelevant email filtering
- Email response templates
- Integration with email clients

### 6. Filter

#### 6.1 Core Functionality
- **Description**: Filter by time range, type; export annual investment report
- **Priority**: Medium
- **Sprint**: 2.2

#### 6.2 Requirements

**Filter Options:**
- **Time Range Filters:**
  - Daily, weekly, monthly, quarterly, yearly
  - Custom date range selection
  - Relative date filters (last 30 days, etc.)
  - Financial year filtering
  - Calendar year filtering

- **Type Filters:**
  - Client type (Individual, Corporate, Trust)
  - Investment type (Property, Equity, Fixed Income)
  - Enquiry type (General, Investment, Support)
  - Status filters (Active, Inactive, Pending)
  - Geographic filters

**Filter Functionality:**
- Multiple filter combinations
- Save filter presets
- Quick filter shortcuts
- Filter result counts
- Clear all filters option

### 7. Export

#### 7.1 Core Functionality
- **Description**: Export annual report of annual investment total summary
- **Priority**: Medium
- **Sprint**: 2.2

#### 7.2 Requirements

**Export Capabilities:**
- **Annual Investment Report:**
  - Comprehensive investment summary
  - Performance analysis
  - Client portfolio breakdown
  - Risk assessment summary
  - Regulatory compliance data

- **Export Formats:**
  - PDF reports with professional formatting
  - Excel spreadsheets with data tables
  - CSV files for data analysis
  - PowerPoint presentations
  - JSON/XML for system integration

**Report Features:**
- Customizable report templates
- Company branding and logos
- Interactive charts in exports
- Automated report generation
- Scheduled report delivery

## API Endpoints Required

### Dashboard Data APIs
```
GET    /api/admin/dashboard/summary           - Get dashboard summary data
GET    /api/admin/dashboard/clients/stats     - Get client statistics
GET    /api/admin/dashboard/investments/stats - Get investment statistics
GET    /api/admin/dashboard/enquiries/stats   - Get enquiry statistics
GET    /api/admin/dashboard/birthdays         - Get upcoming birthdays
```

### Birthday Management APIs
```
GET    /api/admin/birthdays/upcoming          - Get upcoming birthdays
POST   /api/admin/birthdays/send-greetings    - Send birthday greetings
GET    /api/admin/birthdays/templates         - Get email templates
PUT    /api/admin/birthdays/templates/{id}    - Update email template
GET    /api/admin/birthdays/history           - Get birthday email history
```

### Enquiry Management APIs
```
GET    /api/admin/enquiries/recent            - Get recent enquiries
GET    /api/admin/enquiries                   - Get all enquiries (filtered)
PUT    /api/admin/enquiries/{id}/status       - Update enquiry status
POST   /api/admin/enquiries/bulk-update       - Bulk update enquiries
GET    /api/admin/enquiries/sync              - Sync from email
```

### Email Integration APIs
```
POST   /api/admin/email/configure             - Configure email integration
GET    /api/admin/email/status                - Get email sync status
POST   /api/admin/email/sync                  - Manual email sync
GET    /api/admin/email/templates             - Get email templates
PUT    /api/admin/email/templates/{id}        - Update email template
```

### Filter and Export APIs
```
GET    /api/admin/filters/presets             - Get saved filter presets
POST   /api/admin/filters/presets             - Save filter preset
DELETE /api/admin/filters/presets/{id}        - Delete filter preset
POST   /api/admin/export/annual-report        - Generate annual report
GET    /api/admin/export/formats              - Get available export formats
POST   /api/admin/export/custom               - Generate custom export
```

## Database Schema

### Dashboard Configuration Table
```sql
CREATE TABLE dashboard_config (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    widget_type VARCHAR(50) NOT NULL,
    widget_position INTEGER NOT NULL,
    widget_size VARCHAR(20) NOT NULL,
    widget_settings JSONB,
    is_visible BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Birthday Greetings Table
```sql
CREATE TABLE birthday_greetings (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    sent_date DATE NOT NULL,
    email_template_id BIGINT,
    delivery_status VARCHAR(20) DEFAULT 'PENDING',
    delivery_timestamp TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Email Integration Table
```sql
CREATE TABLE email_integration (
    id BIGSERIAL PRIMARY KEY,
    email_account VARCHAR(255) NOT NULL,
    server_type VARCHAR(10) NOT NULL, -- IMAP, POP3
    server_host VARCHAR(255) NOT NULL,
    server_port INTEGER NOT NULL,
    username VARCHAR(255) NOT NULL,
    password_encrypted TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    last_sync_timestamp TIMESTAMP,
    sync_status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Filter Presets Table
```sql
CREATE TABLE filter_presets (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    preset_name VARCHAR(100) NOT NULL,
    filter_criteria JSONB NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Technical Requirements

### Performance Requirements
- Dashboard loading time: < 3 seconds
- Real-time data updates: < 30 seconds
- Export generation: < 60 seconds for standard reports
- Email sync: < 5 minutes for full inbox sync
- Filter application: < 2 seconds

### Security Requirements
- Role-based access to dashboard widgets
- Encrypted storage of email credentials
- Audit logging for all export activities
- Data privacy compliance for client information
- Secure email transmission protocols

### Integration Requirements
- Email server integration (IMAP/POP3/Exchange)
- Database integration for real-time data
- File storage integration for exports
- Notification system integration
- Authentication system integration

## Success Criteria

### Functional Success
- ✅ Dashboard displays accurate client and investment summaries
- ✅ Real-time data updates working correctly
- ✅ Automated birthday email system operational
- ✅ Email integration syncing enquiries successfully
- ✅ Filter and export functionality working as expected

### Performance Success
- ✅ All performance requirements met
- ✅ System handles concurrent user load
- ✅ Export generation within acceptable timeframes
- ✅ Email sync processing efficiently

### User Experience Success
- ✅ Intuitive dashboard interface
- ✅ Mobile-responsive design
- ✅ Quick access to key information
- ✅ Efficient workflow for enquiry management
- ✅ Reliable export and reporting functionality 