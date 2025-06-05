# Tycoon System - Client Application Requirements

## 1. User Registration and Login Module

### 1.1 Quick Registration
- **Requirement ID**: CR-001
- **Description**: Users can register as visiting users using only their email address
- **Implementation Details**:
  - Email input validation with client-side and Spring Boot backend validation
  - Automatic verification email sending via Spring Boot Email Service
  - Account activation after email verification through Spring Boot REST API
  - Redirect to login page after successful registration
  - Integration with Spring Boot User Management Service
- **Estimated Hours**: 4
- **Priority**: Phase 1 online features

### 1.2 KYC Real-Name Authentication
- **Requirement ID**: CR-002
- **Description**: Support users to upload additional documents for identity verification
- **Implementation Details**:
  - Identity proof document upload to Spring Boot File Upload Service
  - Address proof document upload with Spring Boot validation
  - Background review system integration via Spring Boot Admin APIs
  - User level upgrade (visiting user → registered user → loan user → fund customer) through Spring Security
  - Document format validation (PDF, JPG, PNG) on both client and Spring Boot backend
  - File size limits and compression managed by Spring Boot services
- **Estimated Hours**: 8
- **Priority**: Phase 1 online features

### 1.3 Account Login
- **Requirement ID**: CR-003
- **Description**: Secure login system with email and password
- **Implementation Details**:
  - Email + password authentication via Spring Security JWT endpoints
  - Input validation and sanitization on client and Spring Boot backend
  - JWT token-based session management with Spring Security
  - Remember me functionality with refresh tokens
  - Auto-logout after inactivity based on JWT expiration
- **Estimated Hours**: 2
- **Priority**: Phase 1 online features

### 1.4 Login Error Handling
- **Requirement ID**: CR-004
- **Description**: Comprehensive error handling for login attempts
- **Implementation Details**:
  - Wrong password error messages from Spring Boot authentication service
  - Account does not exist notifications via Spring Boot user service
  - Rate limiting for failed attempts using Spring Boot Bucket4j integration
  - Account lockout after multiple failed attempts managed by Spring Security
  - Clear error message display with standardized Spring Boot error responses
- **Estimated Hours**: 2
- **Priority**: Phase 1 online features

### 1.5 Third-Party Authentication Support
- **Requirement ID**: CR-005
- **Description**: Additional security and recovery options
- **Implementation Details**:
  - Verification code for password recovery via Spring Boot SMS/Email service
  - Two-factor authentication (2FA) integration with Spring Security
  - Mobile phone binding through Spring Boot user profile service
  - Email verification for sensitive operations via Spring Boot notification service
  - Password reset functionality with Spring Security password reset flow
  - Reset link email generation through Spring Boot email templates
- **Estimated Hours**: 5
- **Priority**: Phase 1 online features

### 1.6 Legal Compliance
- **Requirement ID**: CR-006
- **Description**: Registration agreement and privacy policy access
- **Implementation Details**:
  - Registration agreement page served from Spring Boot content management service
  - Privacy policy page with version control via Spring Boot CMS
  - Terms of service acceptance tracked in Spring Boot audit system
  - Cookie policy compliance with Spring Boot session management
  - GDPR compliance features integrated with Spring Boot data services
- **Estimated Hours**: 3
- **Priority**: Phase 1 online features

### 1.7 Dynamic Permission System
- **Requirement ID**: CR-007
- **Description**: Role-based access control with dynamic loading
- **Implementation Details**:
  - Guest user permissions controlled by Spring Security authorities
  - Member permissions managed through Spring Boot authorization service
  - Loan user permissions with Spring Security method-level security
  - Fund customer permissions via Spring Boot role hierarchy
  - Real-time permission validation using Spring Security context
  - Permission inheritance system built into Spring Security configuration
  - Function block dynamic loading based on Spring Boot user service responses
- **Estimated Hours**: 12
- **Priority**: Phase 1 online features

## 2. Home Overview Module

### 2.1 Navigation Entry
- **Requirement ID**: CR-008
- **Description**: Main dashboard with welcome message and navigation
- **Implementation Details**:
  - Personalized welcome message from Spring Boot user profile service
  - User profile summary via Spring Boot REST APIs
  - Quick stats display powered by Spring Boot analytics service
  - Responsive design for mobile/desktop with Spring Boot API integration
- **Estimated Hours**: 4
- **Priority**: Phase 1 online features

### 2.2 Quick Access Panel
- **Requirement ID**: CR-009
- **Description**: Direct access to core system modules
- **Implementation Details**:
  - Wealth management quick access based on Spring Boot fund service
  - Credit solutions quick access via Spring Boot loan service
  - Professional services quick access through Spring Boot advisory service
  - Icon-based navigation with Spring Boot-powered feature flags
  - Module availability based on user permissions from Spring Security
- **Estimated Hours**: 3
- **Priority**: Phase 1 online features

### 2.3 Real-Time Content Display
- **Requirement ID**: CR-010
- **Description**: Dynamic content updates and market information
- **Implementation Details**:
  - Latest market analysis push notifications via Spring Boot WebSocket
  - Event previews and calendar from Spring Boot content service
  - Financial news feed powered by Spring Boot RSS aggregation service
  - Real-time data updates using Spring Boot Server-Sent Events
  - Content personalization based on user preferences from Spring Boot ML service
- **Estimated Hours**: 8
- **Priority**: Phase 1 online features

## 3. Wealth Management Services Module

### 3.1 Fund Products
- **Requirement ID**: CR-011
- **Description**: Comprehensive fund management system
- **Implementation Details**:
  - Real estate special investment funds catalog from Spring Boot fund service
  - Tengyun real income fund data via Spring Boot product service
  - Fund product catalog with Spring Boot pagination and filtering
  - Fund performance tracking through Spring Boot analytics service
  - Risk assessment display powered by Spring Boot risk assessment service
- **Estimated Hours**: 15
- **Priority**: Phase 1 online features

### 3.2 Fund Operations
- **Requirement ID**: CR-012
- **Description**: Complete fund transaction capabilities
- **Implementation Details**:
  - View fund details and performance via Spring Boot fund detail API
  - Buy fund functionality through Spring Boot transaction service
  - Sell fund functionality with Spring Boot redemption service
  - Transaction history from Spring Boot transaction history API
  - Portfolio management via Spring Boot portfolio service
  - Investment calculator powered by Spring Boot calculation service
- **Estimated Hours**: 20
- **Priority**: Phase 1 online features

### 3.3 Fund Account Management
- **Requirement ID**: CR-013
- **Description**: Account management for fund investments
- **Implementation Details**:
  - Account balance display from Spring Boot account service
  - Transaction history via Spring Boot audit trail service
  - Statement generation through Spring Boot reporting service
  - Account settings managed by Spring Boot user preference service
  - Bank account linking via Spring Boot payment integration service
  - Withdrawal/deposit functionality through Spring Boot payment service
- **Estimated Hours**: 12
- **Priority**: Phase 1 online features

### 3.4 Investment Portfolio
- **Requirement ID**: CR-014
- **Description**: Portfolio tracking and analysis
- **Implementation Details**:
  - Asset allocation visualization powered by Spring Boot analytics API
  - Performance analytics from Spring Boot performance service
  - Risk assessment via Spring Boot risk calculation service
  - Rebalancing recommendations through Spring Boot AI service
  - Historical performance charts from Spring Boot historical data service
- **Estimated Hours**: 18
- **Priority**: Phase 2 features

### 3.5 Market Analysis Tools
- **Requirement ID**: CR-015
- **Description**: Market research and analysis features
- **Implementation Details**:
  - Market trend analysis via Spring Boot market data service
  - Fund comparison tools powered by Spring Boot comparison service
  - Performance benchmarking through Spring Boot benchmark service
  - Research reports access from Spring Boot document service
  - Expert recommendations via Spring Boot advisory service
- **Estimated Hours**: 15
- **Priority**: Phase 2 features

## 4. Credit Solutions Module

### 4.1 Loan Application
- **Requirement ID**: CR-016
- **Description**: Online loan application system
- **Implementation Details**:
  - Loan application form with Spring Boot validation and submission
  - Document upload functionality via Spring Boot file service
  - Credit assessment integration through Spring Boot credit service
  - Application status tracking via Spring Boot workflow service
  - Pre-qualification tools powered by Spring Boot assessment service
- **Estimated Hours**: 25
- **Priority**: Phase 2 features

### 4.2 Loan Management
- **Requirement ID**: CR-017
- **Description**: Active loan management system
- **Implementation Details**:
  - Loan balance tracking from Spring Boot loan service
  - Payment scheduling via Spring Boot payment scheduler service
  - Payment history through Spring Boot payment history API
  - Early payment options managed by Spring Boot payment service
  - Interest calculation display powered by Spring Boot calculation service
- **Estimated Hours**: 20
- **Priority**: Phase 2 features

### 4.3 Credit Monitoring
- **Requirement ID**: CR-018
- **Description**: Credit score and monitoring features
- **Implementation Details**:
  - Credit score display from Spring Boot credit bureau integration
  - Credit report access via Spring Boot report service
  - Score improvement recommendations through Spring Boot advisory service
  - Alert system for score changes using Spring Boot notification service
  - Credit utilization tracking powered by Spring Boot monitoring service
- **Estimated Hours**: 15
- **Priority**: Phase 2 features

## 5. Professional Services Module

### 5.1 Financial Advisory
- **Requirement ID**: CR-019
- **Description**: Access to financial advisory services
- **Implementation Details**:
  - Advisor appointment booking via Spring Boot scheduling service
  - Video consultation integration through Spring Boot WebRTC service
  - Document sharing platform powered by Spring Boot file sharing service
  - Advisory session history from Spring Boot session tracking API
  - Personalized recommendations via Spring Boot recommendation engine
- **Estimated Hours**: 20
- **Priority**: Phase 2 features

### 5.2 Tax Services
- **Requirement ID**: CR-020
- **Description**: Tax preparation and filing services
- **Implementation Details**:
  - Tax document upload through Spring Boot tax document service
  - Tax calculation tools powered by Spring Boot tax calculation service
  - Filing status tracking via Spring Boot filing service
  - Tax optimization recommendations through Spring Boot optimization service
  - Previous year comparisons from Spring Boot historical data service
- **Estimated Hours**: 18
- **Priority**: Phase 3 features

### 5.3 Insurance Services
- **Requirement ID**: CR-021
- **Description**: Insurance product integration
- **Implementation Details**:
  - Insurance product catalog from Spring Boot insurance service
  - Quote comparison via Spring Boot comparison service
  - Policy management through Spring Boot policy service
  - Claims processing powered by Spring Boot claims service
  - Coverage recommendations via Spring Boot recommendation service
- **Estimated Hours**: 22
- **Priority**: Phase 3 features

## 6. Account and Profile Management

### 6.1 User Profile
- **Requirement ID**: CR-022
- **Description**: Comprehensive user profile management
- **Implementation Details**:
  - Personal information editing via Spring Boot user profile API
  - Contact information updates through Spring Boot contact service
  - Profile picture upload using Spring Boot file upload service
  - Privacy settings managed by Spring Boot privacy service
  - Communication preferences via Spring Boot preference service
- **Estimated Hours**: 8
- **Priority**: Phase 1 online features

### 6.2 Security Settings
- **Requirement ID**: CR-023
- **Description**: Account security management
- **Implementation Details**:
  - Password change functionality through Spring Security password service
  - Two-factor authentication setup via Spring Boot 2FA service
  - Login history viewing from Spring Boot audit service
  - Device management through Spring Boot device tracking service
  - Security alert preferences via Spring Boot notification preference service
- **Estimated Hours**: 10
- **Priority**: Phase 1 online features

### 6.3 Notification Management
- **Requirement ID**: CR-024
- **Description**: Notification preferences and history
- **Implementation Details**:
  - Push notification settings via Spring Boot push service
  - Email notification preferences through Spring Boot email service
  - SMS notification options managed by Spring Boot SMS service
  - Notification history from Spring Boot notification history API
  - Custom alert setup powered by Spring Boot alert service
- **Estimated Hours**: 6
- **Priority**: Phase 2 features

## 7. Mobile Application Features

### 7.1 Mobile Responsiveness
- **Requirement ID**: CR-025
- **Description**: Mobile-optimized user interface
- **Implementation Details**:
  - Responsive design for all screen sizes with Spring Boot API optimization
  - Touch-friendly interface elements with Spring Boot mobile API endpoints
  - Mobile navigation optimization powered by Spring Boot navigation service
  - Performance optimization for mobile with Spring Boot caching
  - Offline functionality for basic features using Spring Boot offline sync
- **Estimated Hours**: 15
- **Priority**: Phase 1 online features

### 7.2 Mobile-Specific Features
- **Requirement ID**: CR-026
- **Description**: Mobile app specific functionality
- **Implementation Details**:
  - Biometric authentication (fingerprint/face) with Spring Security integration
  - Push notifications via Spring Boot push notification service
  - Camera integration for document scanning with Spring Boot OCR service
  - GPS integration for location services through Spring Boot location service
  - Mobile payment integration via Spring Boot mobile payment service
- **Estimated Hours**: 20
- **Priority**: Phase 2 features

## 8. Integration and API Requirements

### 8.1 Third-Party Integrations
- **Requirement ID**: CR-027
- **Description**: External service integrations
- **Implementation Details**:
  - Banking API integration via Spring Boot WebClient and OAuth2
  - Payment gateway integration through Spring Boot payment service
  - Credit bureau API integration using Spring Boot credit service
  - Market data feeds via Spring Boot scheduled market data service
  - Email service integration through Spring Boot Mail starter
- **Estimated Hours**: 30
- **Priority**: Phase 1 online features

### 8.2 Data Synchronization
- **Requirement ID**: CR-028
- **Description**: Real-time data synchronization
- **Implementation Details**:
  - Real-time balance updates via Spring Boot WebSocket connections
  - Transaction synchronization through Spring Boot transaction sync service
  - Market data updates using Spring Boot SSE (Server-Sent Events)
  - Cross-device synchronization powered by Spring Boot sync service
  - Offline data caching with Spring Boot Cache and Redis integration
- **Estimated Hours**: 25
- **Priority**: Phase 1 online features

## Total Estimated Hours for Client Application: 476 hours

## Priority Distribution:
- **Phase 1 Features**: 112 hours
- **Phase 2 Features**: 243 hours  
- **Phase 3 Features**: 40 hours
- **Cross-cutting Features**: 81 hours

## Spring Boot Integration Notes:
- All client-side API calls will integrate with Spring Boot REST endpoints
- JWT token management follows Spring Security best practices
- Real-time features utilize Spring Boot WebSocket and SSE capabilities
- File uploads and downloads use Spring Boot multipart file handling
- All form validations are backed by Spring Boot Bean Validation
- Error handling follows Spring Boot error response standards
- Caching strategies align with Spring Boot Cache abstraction 