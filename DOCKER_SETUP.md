# Finance Admin Management System - Docker Setup Guide

## Quick Start

### Prerequisites

1. **Install Docker Desktop**
   - Go to [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop/)
   - Download and install Docker Desktop
   - Restart your computer if prompted
   - Start Docker Desktop and wait for it to fully load

2. **Verify Docker Installation**
   ```powershell
   docker --version
   docker compose version
   ```

### Automated Setup (Recommended)

Run the automated setup script:

```powershell
.\setup-docker.ps1
```

This script will:
- Check if Docker is installed and running
- Clean up any existing containers
- Build and start all services
- Display access URLs and credentials

### Manual Setup

If you prefer to run commands manually:

1. **Clean up existing containers** (optional):
   ```bash
   docker compose down -v
   ```

2. **Build and start all services**:
   ```bash
   docker compose up --build -d
   ```

3. **Check service status**:
   ```bash
   docker compose ps
   ```

4. **View logs**:
   ```bash
   docker compose logs -f
   ```

## Services Overview

The system consists of the following services:

| Service | Image | Port | Purpose |
|---------|-------|------|---------|
| **finance-admin-app** | Custom (Spring Boot) | 8090 | Main application |
| **finance-admin-postgres** | postgres:16-alpine | 5433 | Primary database |
| **finance-admin-mongodb** | mongo:8.0 | 27017 | Document storage |
| **finance-admin-redis** | redis:7.4-alpine | 6380 | Cache & sessions |
| **finance-admin-rabbitmq** | rabbitmq:3.13-management | 5672, 15672 | Message queue |

## Access Information

### Application URLs

- **Main Application**: http://localhost:8090/api/admin
- **Swagger UI**: http://localhost:8090/api/admin/swagger-ui.html
- **Health Check**: http://localhost:8090/api/admin/actuator/health
- **API Documentation**: http://localhost:8090/api/admin/v3/api-docs

### Management Interfaces

- **RabbitMQ Management**: http://localhost:15672
  - Username: `guest`
  - Password: `guest`

### Database Connections

- **PostgreSQL**: 
  - Host: `localhost`
  - Port: `5433`
  - Database: `finance_admin`
  - Username: `admin`
  - Password: `password`

- **MongoDB**:
  - Host: `localhost`
  - Port: `27017`
  - Database: `finance_admin_docs`
  - Username: `admin`
  - Password: `password`

- **Redis**:
  - Host: `localhost`
  - Port: `6380`

### Default Admin User

- **Username**: `admin`
- **Password**: `admin123`
- **Email**: `admin@finance.com`

## Docker Compose Commands

### Basic Operations

```bash
# Start all services
docker compose up -d

# Stop all services
docker compose down

# Restart all services
docker compose restart

# Stop and remove all data
docker compose down -v
```

### Development Commands

```bash
# View logs for all services
docker compose logs -f

# View logs for specific service
docker compose logs -f app

# Rebuild and restart application only
docker compose up --build -d app

# Execute command in running container
docker compose exec app bash
```

### Troubleshooting Commands

```bash
# Check service status
docker compose ps

# Check resource usage
docker stats

# Remove unused images and containers
docker system prune

# Restart specific service
docker compose restart app
```

## Updating Docker Images

To update to the latest versions of the base images:

1. Stop all services:
   ```bash
   docker compose down
   ```

2. Pull latest images:
   ```bash
   docker compose pull
   ```

3. Rebuild and start:
   ```bash
   docker compose up --build -d
   ```

## Development Workflow

### Making Code Changes

1. Make your code changes in the `Backend/` directory
2. Rebuild the application container:
   ```bash
   docker compose up --build -d app
   ```
3. Check logs for any issues:
   ```bash
   docker compose logs -f app
   ```

### Database Changes

For database schema changes:

1. Update your JPA entities
2. Rebuild the application:
   ```bash
   docker compose up --build -d app
   ```
3. The application will automatically apply schema changes on startup

### Resetting Data

To reset all data and start fresh:

```bash
docker compose down -v
docker compose up --build -d
```

## Troubleshooting

### Common Issues

1. **Port already in use**
   - Check if services are already running: `docker compose ps`
   - Stop existing services: `docker compose down`

2. **Docker not responding**
   - Restart Docker Desktop
   - Check Docker status: `docker info`

3. **Application fails to start**
   - Check logs: `docker compose logs app`
   - Verify all dependencies are healthy: `docker compose ps`

4. **Database connection issues**
   - Check database health: `docker compose logs postgres`
   - Verify database is ready: `docker compose exec postgres pg_isready`

### Getting Help

- Check application logs: `docker compose logs -f app`
- Check all service statuses: `docker compose ps`
- Restart problematic services: `docker compose restart <service-name>`

## Performance Optimization

### For Development

The current configuration is optimized for development with:
- Hot reloading disabled (use rebuild for changes)
- Debug logging enabled
- Development profiles active

### For Production

For production deployment, consider:
- Using environment-specific docker-compose files
- Enabling SSL/TLS
- Setting up proper logging aggregation
- Configuring resource limits
- Setting up monitoring and alerting

## Security Notes

- Default passwords are used for development
- All services run with development configurations
- For production, ensure to:
  - Change all default passwords
  - Use environment variables for secrets
  - Enable SSL/TLS
  - Configure proper firewall rules
  - Regular security updates 