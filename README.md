# Finance Admin Management System

This project is the administrative backend for the Finance Financial Platform.

## Overview

The Finance Admin Management System is a comprehensive Spring Boot application designed to provide secure administrative capabilities for the Finance platform. It includes user management, role-based access control, audit logging, and various administrative functions.

## Features

- **User Management**: Create, update, delete, and manage user accounts
- **Role-Based Access Control**: Comprehensive permission system with multiple admin roles
- **Authentication & Authorization**: JWT-based authentication with MFA support
- **Audit Logging**: Complete audit trail for all administrative actions
- **Security**: Advanced security features including account lockout, password policies
- **API Documentation**: Swagger/OpenAPI integration for API documentation
- **Monitoring**: Health checks and metrics via Spring Boot Actuator
- **Caching**: Redis-based caching for improved performance
- **Message Queue**: RabbitMQ integration for asynchronous processing
- **Database Support**: PostgreSQL for relational data, MongoDB for document storage

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security 6**
- **Spring Data JPA**
- **Spring Data MongoDB**
- **PostgreSQL**
- **MongoDB**
- **Redis**
- **RabbitMQ**
- **JWT (JSON Web Tokens)**
- **Docker & Docker Compose**
- **Maven**

## Quick Start

### Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- Maven 3.6+

### Running with Docker Compose

1. Clone the repository
2. Navigate to the project root
3. Run the following command:

```bash
docker-compose up -d
```

This will start all required services:
- PostgreSQL: localhost:5433/finance_admin (username: admin, password: password)
- MongoDB: localhost:27017/finance_admin_docs
- Redis: localhost:6380
- RabbitMQ: localhost:5672 (Management UI: localhost:15672)
- Spring Boot App: localhost:8090

### API Documentation

Once the application is running, you can access:
- Swagger UI: http://localhost:8090/swagger-ui.html
- API Docs: http://localhost:8090/v3/api-docs
- Health Check: http://localhost:8090/actuator/health

### Default Admin User

- Username: `admin`
- Password: `admin123`
- Email: `admin@finance.com`

## Development

For detailed development setup instructions, see the [Backend README](Backend/README.md).

## License

This project is proprietary software for Finance Financial Platform.
