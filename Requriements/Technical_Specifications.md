# Finance System - Technical Specifications

## 1. System Overview

The Finance System is a comprehensive financial management platform designed to provide secure, scalable, and efficient financial services. This document outlines the technical specifications for implementing the system using Spring Boot Java framework with modern enterprise architecture patterns.

## Technology Stack Overview

### Backend Technologies
- **Framework**: Java Spring Boot 3.x with Spring Framework 6.x
- **Security**: Spring Security 6.x with JWT authentication
- **Data Access**: Spring Data JPA with Hibernate
- **Database**: PostgreSQL (primary), Redis (caching), MongoDB (document storage)
- **API Documentation**: OpenAPI 3.0 with Swagger UI
- **Message Queue**: Spring AMQP with RabbitMQ
- **Build Tool**: Maven or Gradle
- **Java Version**: Java 17+ (LTS)

### Spring Boot Dependencies
```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    
    <!-- API Documentation -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.0.2</version>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Database Schema Design

### Core User Management Tables

```sql
-- Users table with hierarchical permission levels
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    user_level VARCHAR(20) CHECK (user_level IN ('guest', 'member', 'loan_user', 'fund_customer')) DEFAULT 'guest',
    status VARCHAR(20) CHECK (status IN ('active', 'suspended', 'banned', 'pending_verification')) DEFAULT 'pending_verification',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    email_verified_at TIMESTAMP,
    kyc_status VARCHAR(20) CHECK (kyc_status IN ('not_started', 'pending', 'approved', 'rejected')) DEFAULT 'not_started',
    kyc_approved_at TIMESTAMP
);

-- User profiles for additional information
CREATE TABLE user_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(50),
    date_of_birth DATE,
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    profile_picture_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- KYC documents for identity verification
CREATE TABLE kyc_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    document_type VARCHAR(20) CHECK (document_type IN ('identity_proof', 'address_proof', 'income_proof')) NOT NULL,
    file_url TEXT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size INTEGER,
    mime_type VARCHAR(100),
    status VARCHAR(20) CHECK (status IN ('pending', 'approved', 'rejected')) DEFAULT 'pending',
    reviewed_by UUID REFERENCES admin_users(id),
    reviewed_at TIMESTAMP,
    rejection_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Financial Product Management Tables

```sql
-- Fund products catalog
CREATE TABLE fund_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    fund_type VARCHAR(20) CHECK (fund_type IN ('real_estate', 'income', 'growth', 'balanced')) NOT NULL,
    minimum_investment DECIMAL(15,2) NOT NULL,
    management_fee DECIMAL(5,4) NOT NULL,
    performance_fee DECIMAL(5,4),
    risk_level INTEGER CHECK (risk_level >= 1 AND risk_level <= 10),
    status VARCHAR(20) CHECK (status IN ('active', 'inactive', 'closed')) DEFAULT 'active',
    inception_date DATE NOT NULL,
    nav_per_unit DECIMAL(10,4) NOT NULL,
    total_assets DECIMAL(15,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User fund investments
CREATE TABLE fund_investments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    fund_id UUID REFERENCES fund_products(id),
    investment_amount DECIMAL(15,2) NOT NULL,
    units_purchased DECIMAL(15,6) NOT NULL,
    purchase_nav DECIMAL(10,4) NOT NULL,
    purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) CHECK (status IN ('active', 'redeemed', 'pending_redemption')) DEFAULT 'active',
    redemption_date TIMESTAMP,
    redemption_amount DECIMAL(15,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Fund performance tracking
CREATE TABLE fund_performance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fund_id UUID REFERENCES fund_products(id),
    nav_date DATE NOT NULL,
    nav_value DECIMAL(10,4) NOT NULL,
    daily_return DECIMAL(8,6),
    total_assets DECIMAL(15,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(fund_id, nav_date)
);
```

### Credit and Loan Management Tables

```sql
-- Loan applications
CREATE TABLE loan_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    loan_type VARCHAR(20) CHECK (loan_type IN ('personal', 'business', 'mortgage', 'auto')) NOT NULL,
    requested_amount DECIMAL(15,2) NOT NULL,
    loan_purpose TEXT,
    employment_status VARCHAR(100),
    annual_income DECIMAL(15,2),
    existing_debts DECIMAL(15,2),
    application_status VARCHAR(20) CHECK (application_status IN ('draft', 'submitted', 'under_review', 'approved', 'rejected', 'withdrawn')) DEFAULT 'draft',
    credit_score INTEGER,
    risk_assessment JSONB,
    approved_amount DECIMAL(15,2),
    interest_rate DECIMAL(5,4),
    loan_term_months INTEGER,
    reviewed_by UUID REFERENCES admin_users(id),
    reviewed_at TIMESTAMP,
    rejection_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Active loans
CREATE TABLE loans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID REFERENCES loan_applications(id),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    principal_amount DECIMAL(15,2) NOT NULL,
    interest_rate DECIMAL(5,4) NOT NULL,
    loan_term_months INTEGER NOT NULL,
    monthly_payment DECIMAL(10,2) NOT NULL,
    outstanding_balance DECIMAL(15,2) NOT NULL,
    next_payment_date DATE NOT NULL,
    loan_status VARCHAR(20) CHECK (loan_status IN ('active', 'paid_off', 'defaulted', 'restructured')) DEFAULT 'active',
    disbursement_date DATE NOT NULL,
    maturity_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Loan payments
CREATE TABLE loan_payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loan_id UUID REFERENCES loans(id),
    payment_amount DECIMAL(10,2) NOT NULL,
    principal_amount DECIMAL(10,2) NOT NULL,
    interest_amount DECIMAL(10,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(20) CHECK (payment_method IN ('bank_transfer', 'credit_card', 'debit_card', 'check')) NOT NULL,
    payment_status VARCHAR(20) CHECK (payment_status IN ('pending', 'completed', 'failed', 'refunded')) DEFAULT 'pending',
    transaction_reference VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Administrative and Audit Tables

```sql
-- Admin users with role-based access
CREATE TABLE admin_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) CHECK (role IN ('super_admin', 'system_admin', 'financial_admin', 'customer_service', 'compliance_officer', 'analyst')) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    last_login TIMESTAMP,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Comprehensive audit log
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID, -- Can reference users or admin_users
    user_type VARCHAR(10) CHECK (user_type IN ('client', 'admin')) NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID,
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    session_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- System notifications and alerts
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(10) CHECK (type IN ('email', 'push', 'sms', 'in_app')) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) CHECK (status IN ('pending', 'sent', 'delivered', 'failed')) DEFAULT 'pending',
    scheduled_at TIMESTAMP,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Spring Boot Entity Models

### User Entity Example

```java
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    @Email
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    @JsonIgnore
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_level")
    private UserLevel userLevel = UserLevel.GUEST;
    
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.PENDING_VERIFICATION;
    
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status")
    private KycStatus kycStatus = KycStatus.NOT_STARTED;
    
    @Column(name = "kyc_approved_at")
    private LocalDateTime kycApprovedAt;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile profile;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<KycDocument> kycDocuments = new ArrayList<>();
    
    // Constructors, getters, setters
}

public enum UserLevel {
    GUEST, MEMBER, LOAN_USER, FUND_CUSTOMER
}

public enum UserStatus {
    ACTIVE, SUSPENDED, BANNED, PENDING_VERIFICATION
}

public enum KycStatus {
    NOT_STARTED, PENDING, APPROVED, REJECTED
}
```

## API Controller Structure

### Authentication Controller

```java
@RestController
@RequestMapping("/api/auth")
@Validated
@Tag(name = "Authentication", description = "User authentication operations")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<ApiResponse<UserDto>> register(
            @Valid @RequestBody RegisterRequest request) {
        UserDto user = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", user));
    }
    
    @PostMapping("/login")
    @Operation(summary = "User login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }
    
    @PostMapping("/logout")
    @Operation(summary = "User logout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> logout(
            HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse tokenResponse = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", tokenResponse));
    }
    
    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset email sent"));
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successful"));
    }
}
```

### Fund Management Controller

```java
@RestController
@RequestMapping("/api/funds")
@Validated
@Tag(name = "Fund Management", description = "Fund operations")
public class FundController {
    
    @Autowired
    private FundService fundService;
    
    @GetMapping
    @Operation(summary = "Get all funds")
    public ResponseEntity<ApiResponse<PageResponse<FundDto>>> getAllFunds(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) BigDecimal minInvestment,
            @RequestParam(required = false) Integer riskLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        FundFilterCriteria criteria = FundFilterCriteria.builder()
                .type(type)
                .minInvestment(minInvestment)
                .riskLevel(riskLevel)
                .build();
                
        PageResponse<FundDto> funds = fundService.getAllFunds(criteria, page, limit);
        return ResponseEntity.ok(ApiResponse.success("Funds retrieved successfully", funds));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get fund details")
    public ResponseEntity<ApiResponse<FundDetailDto>> getFundDetails(
            @PathVariable UUID id,
            Authentication authentication) {
        FundDetailDto fundDetail = fundService.getFundDetails(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Fund details retrieved", fundDetail));
    }
    
    @PostMapping("/{id}/invest")
    @Operation(summary = "Invest in fund")
    @PreAuthorize("hasAuthority('FUND_INVEST')")
    public ResponseEntity<ApiResponse<InvestmentDto>> investInFund(
            @PathVariable UUID id,
            @Valid @RequestBody InvestmentRequest request,
            Authentication authentication) {
        InvestmentDto investment = fundService.investInFund(id, request, authentication);
        return ResponseEntity.ok(ApiResponse.success("Investment successful", investment));
    }
}
```

## Security Configuration

### Spring Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/funds").permitAll()
                .requestMatchers("/api/funds/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> 
                ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}
```

### JWT Configuration

```java
@Component
public class JwtTokenProvider {
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration}")
    private int jwtExpiration;
    
    @Value("${app.jwt.refresh-expiration}")
    private int refreshExpiration;
    
    public String generateToken(UserPrincipal userPrincipal) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpiration);
        
        return Jwts.builder()
                .setSubject(userPrincipal.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .claim("email", userPrincipal.getEmail())
                .claim("role", userPrincipal.getRole())
                .claim("userLevel", userPrincipal.getUserLevel())
                .claim("permissions", userPrincipal.getPermissions())
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
    
    public String generateRefreshToken(UserPrincipal userPrincipal) {
        Date expiryDate = new Date(System.currentTimeMillis() + refreshExpiration);
        
        return Jwts.builder()
                .setSubject(userPrincipal.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
    
    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return UUID.fromString(claims.getSubject());
    }
    
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException | 
                 UnsupportedJwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}
```

## Application Configuration

### Application Properties

```yaml
spring:
  application:
    name: finance-system
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  datasource:
    url: jdbc:postgresql://localhost:5432/finance_db
    username: ${DB_USERNAME:finance_user}
    password: ${DB_PASSWORD:finance_password}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000

# JWT Configuration
app:
  jwt:
    secret: ${JWT_SECRET:mySecretKey}
    expiration: 86400000 # 24 hours
    refresh-expiration: 604800000 # 7 days

# API Documentation
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    
# Logging
logging:
  level:
    root: INFO
    com.finance: DEBUG
    org.springframework.security: INFO
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    
# Rate Limiting
bucket4j:
  enabled: true
  filters:
    - cache-name: rate-limit-buckets
      url: /api/auth/login
      rate-limits:
        - bandwidths:
            - capacity: 5
              time: 1
              unit: minutes
    - cache-name: rate-limit-buckets
      url: /api/auth/register
      rate-limits:
        - bandwidths:
            - capacity: 3
              time: 1
              unit: hours
```

## Service Layer Architecture

### Service Interface Example

```java
@Service
@Transactional
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    @Override
    public UserDto createUser(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email already exists");
        }
        
        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUserLevel(UserLevel.GUEST);
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        
        User savedUser = userRepository.save(user);
        
        // Send verification email
        emailService.sendVerificationEmail(savedUser);
        
        return UserMapper.toDto(savedUser);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(new UserProfile());
                
        return UserProfileMapper.toDto(user, profile);
    }
    
    @Override
    public UserProfileDto updateUserProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(new UserProfile());
        
        profile.setUser(user);
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setPhone(request.getPhone());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setAddress(request.getAddress());
        profile.setCity(request.getCity());
        profile.setState(request.getState());
        profile.setPostalCode(request.getPostalCode());
        profile.setCountry(request.getCountry());
        
        UserProfile savedProfile = userProfileRepository.save(profile);
        
        return UserProfileMapper.toDto(user, savedProfile);
    }
}
```

## Integration Specifications

### Third-Party Service Integrations

```yaml
Banking Integration:
  Provider: Plaid/Yodlee
  Purpose: Account verification, transaction history
  Authentication: OAuth 2.0
  Implementation: Spring WebClient with OAuth2 support
  Configuration: External API credentials in application.yml

Payment Processing:
  Provider: Stripe/Square
  Purpose: Investment transactions, loan payments
  Authentication: API Keys
  Implementation: Spring Boot Stripe starter or REST template
  Webhook Support: Spring @EventListener for webhook processing

Credit Bureau:
  Provider: Experian/TransUnion
  Purpose: Credit scoring, identity verification
  Authentication: API Keys + IP Whitelisting
  Implementation: Spring WebClient with custom interceptors
  Rate Limits: Bucket4j for rate limiting
  Data Retention: 7 days using @Scheduled cleanup

Market Data:
  Provider: Alpha Vantage/IEX Cloud
  Purpose: Fund NAV, market analytics
  Authentication: API Keys
  Implementation: Scheduled tasks with @Scheduled annotation
  Caching: Spring Cache with Redis
  Historical Data: 5 years with data archival strategy

Email Service:
  Provider: SendGrid/AWS SES
  Purpose: Transactional emails, notifications
  Implementation: Spring Boot Mail starter with templates
  Templates: Thymeleaf template engine
  Queue: RabbitMQ for async email processing

SMS Service:
  Provider: Twilio
  Purpose: 2FA, notifications
  Implementation: Twilio Java SDK with Spring configuration
  Rate Limits: Based on plan, managed with Bucket4j
  International: Support through Twilio configuration
```

## Performance Requirements

### Response Time Standards

```yaml
API Response Times:
  Authentication: < 200ms
  User Profile: < 300ms
  Fund Listings: < 500ms
  Investment Operations: < 1000ms
  Loan Applications: < 2000ms
  Admin Operations: < 1000ms
  Reports Generation: < 5000ms

Database Performance:
  Query Response: < 100ms (95th percentile)
  Transaction Processing: < 50ms
  Bulk Operations: < 5000ms
  Connection Pool: HikariCP with optimized settings

JVM Performance:
  Heap Size: 2-4GB for production
  GC: G1GC for low latency
  Connection Pooling: HikariCP (default with Spring Boot)
  Thread Pool: Tomcat default with tuning

Caching Strategy:
  Spring Cache with Redis
  Entity-level caching with Hibernate L2 cache
  API response caching for static data
  Cache eviction policies for real-time data
```

### Scalability Targets

```yaml
Concurrent Users:
  Phase 1: 1,000 concurrent users
  Phase 2: 10,000 concurrent users
  Phase 3: 100,000 concurrent users

Spring Boot Configuration:
  Server:
    Thread Pool: 200 max threads
    Connection Pool: 20-50 connections
  JVM:
    Heap: 4-8GB for high load
    MetaSpace: 512MB
  Database:
    Connection Pool: 50-100 connections
    Read Replicas: 2-3 for scaling reads

Microservices Architecture:
  Service Discovery: Spring Cloud Netflix Eureka
  Load Balancing: Spring Cloud LoadBalancer
  Circuit Breaker: Resilience4j
  API Gateway: Spring Cloud Gateway
  Distributed Tracing: Spring Cloud Sleuth with Zipkin
```

This technical specification now provides a comprehensive Spring Boot Java foundation for implementing the Finance System with proper security, scalability, and integration capabilities using the Spring ecosystem. 