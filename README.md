# Tycoon Admin Management System

This project is the administrative backend for the Tycoon Financial Platform.

## Project Structure

- `Backend/` - Spring Boot backend application
- `Requirements/` - Project requirements and specifications

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose

## Setup Instructions

### 1. Start the Development Environment

Start all required services (PostgreSQL, MongoDB, Redis, RabbitMQ) using Docker Compose:

```bash
docker-compose up -d
```

### 2. Build the Application

```bash
cd Backend
mvn clean install
```

### 3. Run the Application

```bash
cd Backend
mvn spring-boot:run
```

The application will be available at: http://localhost:8090/api/admin

### 4. API Documentation

Swagger UI is available at: http://localhost:8090/api/admin/swagger-ui.html

## Development

### Database Access

- PostgreSQL: localhost:5433/tycoon_admin (username: admin, password: password)
- MongoDB: localhost:27017/tycoon_admin_docs
- Redis: localhost:6380
- RabbitMQ Management UI: http://localhost:15672 (username: guest, password: guest)

## Testing

Run tests with:

```bash
mvn test
```

## Configuration

The application configuration is in `Backend/src/main/resources/application.yml`.

For local development, the default profile is `dev`.
