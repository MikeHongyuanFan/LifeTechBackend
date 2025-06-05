# Tycoon Admin Management System - Setup Guide

## 📋 Overview

This guide provides step-by-step instructions for setting up the Tycoon Admin Management System, including all required infrastructure services and the Spring Boot application.

## 🛠️ Prerequisites

### System Requirements
- **Operating System**: macOS, Linux, or Windows with WSL2
- **Java**: JDK 17 or higher
- **Docker**: Docker Desktop or Docker Engine with Docker Compose
- **Maven**: 3.8+ (or use included Maven Wrapper)
- **Memory**: Minimum 8GB RAM recommended
- **Storage**: Minimum 5GB free disk space

### Required Ports
Ensure the following ports are available:
- **5433**: PostgreSQL Database
- **6380**: Redis Cache
- **27017**: MongoDB
- **5672**: RabbitMQ AMQP
- **15672**: RabbitMQ Management UI
- **8090**: Spring Boot Application

## 🚀 Quick Setup

### Option 1: Automated Setup (Recommended)

Navigate to the Backend directory and run the setup script:

```bash
cd /path/to/TacoonStartsFromNew/Backend
chmod +x setup.sh
./setup.sh
```

The script will automatically:
1. Start all Docker services (PostgreSQL, MongoDB, Redis, RabbitMQ)
2. Wait for services to become healthy
3. Build the Spring Boot application
4. Start the application with proper environment variables
5. Verify application health

### Option 2: Manual Setup

If you prefer manual setup or need to troubleshoot, follow these steps:

#### Step 1: Start Infrastructure Services

```bash
cd Backend
docker-compose up --build -d postgres mongodb redis rabbitmq
```

#### Step 2: Verify Services Health

Check that all services are healthy:

```bash
docker-compose ps
```

Wait until all services show "healthy" status. This may take 2-3 minutes.

#### Step 3: Build Spring Boot Application

```bash
./mvnw clean package -DskipTests
```

#### Step 4: Start Spring Boot Application

```bash
# Set environment variables
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/tycoon_admin"
export SPRING_DATASOURCE_USERNAME="admin"
export SPRING_DATASOURCE_PASSWORD="password"
export SPRING_REDIS_HOST="localhost"
export SPRING_REDIS_PORT="6380"

# Start application
./mvnw spring-boot:run
```

## ✅ Verification

### 1. Application Health Check

```bash
curl http://localhost:8090/api/admin/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "redis": {"status": "UP"},
    "mongo": {"status": "UP"},
    "rabbit": {"status": "UP"}
  }
}
```

### 2. Test API Endpoint

```bash
curl http://localhost:8090/api/admin/test/health
```

Expected response:
```json
{
  "success": true,
  "message": "Test endpoint is working",
  "data": "OK",
  "timestamp": "2025-06-05T15:39:46.257"
}
```

### 3. Swagger UI Access

Open your browser and navigate to:
```
http://localhost:8090/api/admin/swagger-ui/index.html
```

You should see the interactive API documentation with all endpoints listed.

### 4. OpenAPI Specification

Verify the OpenAPI JSON is properly formatted:
```bash
curl http://localhost:8090/api/admin/v3/api-docs
```

Should return proper JSON (not base64-encoded string).

## 📖 Access URLs

| Service | URL | Description |
|---------|-----|-------------|
| **Application** | `http://localhost:8090/api/admin` | Main application base URL |
| **Swagger UI** | `http://localhost:8090/api/admin/swagger-ui/index.html` | Interactive API documentation |
| **OpenAPI JSON** | `http://localhost:8090/api/admin/v3/api-docs` | OpenAPI specification |
| **Health Check** | `http://localhost:8090/api/admin/actuator/health` | Application health status |
| **Test Endpoint** | `http://localhost:8090/api/admin/test/health` | Simple test endpoint |
| **RabbitMQ Management** | `http://localhost:15672` | RabbitMQ admin interface |

### Default Credentials

| Service | Username | Password |
|---------|----------|----------|
| **PostgreSQL** | `admin` | `password` |
| **RabbitMQ** | `guest` | `guest` |

## 🔧 Configuration

### Environment Variables

The application supports the following environment variables:

```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/tycoon_admin
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=password

# Redis Configuration
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6380

# Security Configuration
JWT_SECRET=myVerySecretJWTKeyForTycoonAdminSystemThatIsAtLeast256BitsLong
ALLOWED_IPS=127.0.0.1,::1,0:0:0:0:0:0:0:1,localhost

# Logging Level
LOG_LEVEL=DEBUG
```

### Application Profiles

- **dev** (default): Development configuration with detailed logging
- **test**: Testing configuration with H2 in-memory database
- **docker**: Production-like configuration for Docker deployment

## 🐛 Troubleshooting

### Common Issues

#### 1. Port Already in Use

**Error**: `Port 8090 was already in use`

**Solution**:
```bash
# Find and kill process using port 8090
lsof -ti:8090 | xargs kill -9

# Or use different port
export SERVER_PORT=8091
```

#### 2. Docker Services Not Starting

**Error**: Services stuck in "starting" state

**Solution**:
```bash
# Clean up Docker system
docker-compose down -v
docker system prune -f

# Restart services
docker-compose up --build -d
```

#### 3. Database Connection Failed

**Error**: `Connection to localhost:5433 refused`

**Solution**:
```bash
# Check PostgreSQL container status
docker-compose logs postgres

# Restart PostgreSQL if needed
docker-compose restart postgres
```

#### 4. Swagger UI Shows Base64 String

**Error**: OpenAPI specification appears as encoded string

**Solution**:
This issue has been resolved in the current version. If you encounter it:
1. Restart the application
2. Clear browser cache
3. Verify OpenAPI endpoint returns JSON: `curl http://localhost:8090/api/admin/v3/api-docs`

#### 5. Application Startup Fails

**Error**: Various Spring Boot startup errors

**Solution**:
```bash
# Check application logs
tail -f Backend/app.log

# Restart with debug logging
export LOG_LEVEL=DEBUG
./mvnw spring-boot:run
```

### Log Files

- **Application Logs**: `Backend/app.log`
- **Docker Logs**: `docker-compose logs [service-name]`

### Health Check Commands

```bash
# Check all Docker services
docker-compose ps

# Check specific service logs
docker-compose logs postgres
docker-compose logs redis
docker-compose logs mongodb
docker-compose logs rabbitmq

# Check application process
ps aux | grep spring-boot

# Check port usage
lsof -i :8090
netstat -tulpn | grep 8090
```

## 🔄 Stopping Services

### Stop Application Only
```bash
pkill -f spring-boot
```

### Stop All Services
```bash
cd Backend
docker-compose down

# Stop and remove volumes (clean reset)
docker-compose down -v
```

## 📝 Development Notes

### Key Components

1. **Authentication Service**: JWT-based authentication with MFA support
2. **Role Management**: Hierarchical role-based access control
3. **Audit Logging**: Comprehensive activity tracking with retention policies
4. **API Documentation**: Auto-generated OpenAPI 3.1 specification

### Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Swagger UI    │────│  Spring Boot     │────│   PostgreSQL    │
│   (Port 8090)   │    │  Application     │    │   (Port 5433)   │
└─────────────────┘    │  (Port 8090)     │    └─────────────────┘
                       │                  │    
                       │                  │    ┌─────────────────┐
                       │                  │────│     Redis       │
                       │                  │    │   (Port 6380)   │
                       │                  │    └─────────────────┘
                       │                  │    
                       │                  │    ┌─────────────────┐
                       │                  │────│    MongoDB      │
                       │                  │    │   (Port 27017)  │
                       │                  │    └─────────────────┘
                       │                  │    
                       │                  │    ┌─────────────────┐
                       │                  │────│   RabbitMQ      │
                       └──────────────────┘    │   (Port 5672)   │
                                              └─────────────────┘
```

## 🆘 Support

If you encounter issues not covered in this guide:

1. **Check Logs**: Review application and Docker service logs
2. **Verify Prerequisites**: Ensure all required software is installed
3. **Port Conflicts**: Check for port conflicts with other services
4. **Clean Setup**: Try a complete clean setup with `docker-compose down -v`

For additional support, refer to the project documentation or contact the development team.

---

**Last Updated**: June 5, 2025  
**Version**: 1.0.0  
**Tested On**: macOS 14.3.1 (Darwin 23.3.0) 