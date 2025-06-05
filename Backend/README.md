# Tycoon Admin Management System

A comprehensive Spring Boot backend system for the Tycoon Financial Platform's administrative management terminal.

## 🏗️ Architecture Overview

This system implements a production-ready admin management platform with the following key features:

- **JWT-based Authentication** with Multi-Factor Authentication (MFA)
- **Role-Based Access Control (RBAC)** with 6 admin roles
- **IP Whitelisting** and session management
- **Real-time Audit Logging** with AOP
- **User Account Administration** with bulk operations
- **KYC Document Review System** with state machine workflow
- **User Level Management** with automated assessment

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Security**: Spring Security 6.x with JWT
- **Database**: PostgreSQL (primary), MongoDB (documents), Redis (cache)
- **Message Queue**: RabbitMQ with Spring AMQP
- **Testing**: JUnit 5, Spring Boot Test, MockMvc, Testcontainers
- **Documentation**: OpenAPI 3.0 + Swagger UI
- **Build Tool**: Maven
- **Java Version**: 17+

## 📁 Project Structure

```
Backend/
├── src/main/java/com/tycoon/admin/
│   ├── AdminManagementApplication.java          # Main application class
│   ├── auth/                                    # Authentication module (AR-001)
│   │   ├── controller/AuthController.java      # Login, MFA, logout endpoints
│   │   ├── dto/                                # Authentication DTOs
│   │   ├── entity/                             # Admin user entities
│   │   ├── repository/AdminUserRepository.java # User data access
│   │   ├── security/                           # JWT & security components
│   │   └── service/                            # Authentication services
│   ├── role/                                   # Role management (AR-002)
│   ├── audit/                                  # Activity logging (AR-003)
│   ├── user/                                   # User administration (AR-004)
│   ├── kyc/                                    # KYC document review (AR-005)
│   ├── level/                                  # User level management (AR-006)
│   ├── common/                                 # Shared components
│   │   ├── dto/ApiResponse.java               # Standard API response
│   │   ├── entity/BaseEntity.java            # Base entity with audit fields
│   │   ├── exception/                         # Custom exceptions
│   │   └── util/                              # Utility classes
│   └── config/                                # Configuration classes
├── src/main/resources/
│   ├── application.yml                        # Main configuration
│   └── application-test.yml                   # Test configuration
└── src/test/java/                            # Integration tests
    └── com/tycoon/admin/auth/
        └── AuthenticationIntegrationTest.java # Comprehensive auth tests
```

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- Redis 6+
- MongoDB 4.4+
- RabbitMQ 3.8+

### Database Setup

1. **PostgreSQL**:
```sql
CREATE DATABASE tycoon_admin;
CREATE USER admin WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE tycoon_admin TO admin;
```

2. **MongoDB**:
```bash
# Create database and user
use tycoon_admin_docs
db.createUser({
  user: "admin",
  pwd: "password",
  roles: ["readWrite"]
})
```

3. **Redis**: Default configuration (localhost:6379)

4. **RabbitMQ**: Default configuration (localhost:5672, guest/guest)

### Running the Application

1. **Clone and build**:
```bash
cd Backend
mvn clean install
```

2. **Run with development profile**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

3. **Access the application**:
- API Base URL: `http://localhost:8080/api/admin`
- Swagger UI: `http://localhost:8080/api/admin/swagger-ui.html`
- Health Check: `http://localhost:8080/api/admin/actuator/health`

## 🔐 Authentication & Security

### Admin Roles

| Role | Description | Permissions |
|------|-------------|-------------|
| `SUPER_ADMIN` | Full system access | All permissions |
| `SYSTEM_ADMIN` | System administration | User/Admin/Role/System management |
| `FINANCIAL_ADMIN` | Financial operations | Financial data management |
| `CUSTOMER_SERVICE_ADMIN` | Customer support | User assistance and support |
| `COMPLIANCE_OFFICER` | Regulatory compliance | Audit and compliance management |
| `ANALYST` | Read-only access | View-only permissions for analysis |

### Security Features

- **JWT Authentication**: Stateless token-based authentication
- **Multi-Factor Authentication**: TOTP-based MFA support
- **IP Whitelisting**: Per-user IP address restrictions
- **Account Locking**: Automatic lockout after failed attempts
- **Session Management**: Configurable session timeouts
- **Token Blacklisting**: Logout invalidates tokens

### API Authentication

1. **Login**:
```bash
POST /auth/login
{
  "username": "admin",
  "password": "password123"
}
```

2. **MFA Verification** (if enabled):
```bash
POST /auth/verify-mfa
{
  "mfaToken": "uuid-token",
  "mfaCode": "123456"
}
```

3. **Use Bearer Token**:
```bash
Authorization: Bearer <jwt-token>
```

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthenticationIntegrationTest

# Run with coverage
mvn test jacoco:report
```

### Test Coverage

The project includes comprehensive integration tests covering:

- ✅ **Authentication flows**: Login, MFA, logout
- ✅ **Security boundaries**: Role-based access, IP restrictions
- ✅ **Error handling**: Invalid credentials, locked accounts
- ✅ **Edge cases**: Token blacklisting, session management

### Test Configuration

Tests use:
- **H2 Database**: In-memory database for fast testing
- **Embedded Redis**: TestContainers for Redis integration
- **MockMvc**: For web layer testing
- **@Transactional**: Automatic rollback for clean tests

## 📊 Monitoring & Observability

### Health Checks

- **Application Health**: `/actuator/health`
- **Database Connectivity**: Automatic health indicators
- **Redis Connection**: Cache health monitoring
- **Custom Health**: Authentication service status

### Metrics

- **Prometheus Metrics**: `/actuator/prometheus`
- **Application Metrics**: `/actuator/metrics`
- **Custom Metrics**: Login attempts, failed authentications

### Logging

- **Structured Logging**: JSON format for production
- **Security Events**: Authentication, authorization failures
- **Audit Trail**: All admin actions logged
- **Performance Monitoring**: Request/response times

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_USERNAME` | PostgreSQL username | `admin` |
| `DB_PASSWORD` | PostgreSQL password | `password` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `JWT_SECRET` | JWT signing secret | Generated |
| `ALLOWED_IPS` | Global IP whitelist | `127.0.0.1,::1` |

### Profiles

- **dev**: Development with debug logging
- **test**: Testing with H2 database
- **prod**: Production with optimized settings

## 📈 Performance Considerations

### Database Optimization

- **Connection Pooling**: HikariCP with optimized settings
- **Query Optimization**: Indexed columns for frequent queries
- **Batch Operations**: Bulk insert/update support
- **Read Replicas**: Support for read-only operations

### Caching Strategy

- **Redis Caching**: Session data, temporary tokens
- **Application Cache**: Role permissions, configuration
- **Cache Invalidation**: Automatic cleanup and TTL

### Security Performance

- **Rate Limiting**: IP-based request throttling
- **Token Optimization**: Efficient JWT validation
- **Password Hashing**: BCrypt with optimal rounds

## 🚀 Deployment

### Docker Support

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/admin-management-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Production Checklist

- [ ] Configure external databases
- [ ] Set up SSL/TLS certificates
- [ ] Configure load balancer
- [ ] Set up monitoring and alerting
- [ ] Configure backup strategies
- [ ] Review security settings

## 📚 API Documentation

### Swagger UI

Access interactive API documentation at:
`http://localhost:8080/api/admin/swagger-ui.html`

### Key Endpoints

| Endpoint | Method | Description | Roles |
|----------|--------|-------------|-------|
| `/auth/login` | POST | User authentication | Public |
| `/auth/verify-mfa` | POST | MFA verification | Public |
| `/auth/logout` | POST | User logout | Authenticated |
| `/users/**` | * | User management | SUPER_ADMIN, SYSTEM_ADMIN |
| `/roles/**` | * | Role management | SUPER_ADMIN, SYSTEM_ADMIN |
| `/audit/**` | * | Audit logs | SUPER_ADMIN, COMPLIANCE_OFFICER |

## 🤝 Contributing

### Development Guidelines

1. **Code Style**: Follow Spring Boot conventions
2. **Testing**: Maintain >80% test coverage
3. **Security**: Security-first development approach
4. **Documentation**: Update API docs for changes
5. **Performance**: Consider performance impact

### Git Workflow

1. Create feature branch from `main`
2. Implement changes with tests
3. Run full test suite
4. Submit pull request with description
5. Code review and merge

## 📄 License

This project is proprietary software for Tycoon Financial Platform.

## 📞 Support

For technical support or questions:
- **Email**: fanhongyuan897@gmail.com
- **Documentation**: Requriements folder
- **Issues**: Internal issue tracker

---

**Built with ❤️ by Mike StarX development team
