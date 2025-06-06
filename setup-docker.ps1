# Finance Admin Management System - Docker Setup Script
# This script helps set up and run the Finance Admin Management System with Docker

Write-Host "=======================================" -ForegroundColor Cyan
Write-Host "Finance Admin Management System Setup" -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host ""

# Function to check if Docker is installed
function Test-DockerInstalled {
    try {
        $dockerVersion = docker --version 2>$null
        if ($dockerVersion) {
            Write-Host "✓ Docker is installed: $dockerVersion" -ForegroundColor Green
            return $true
        }
    }
    catch {
        # Docker command not found
    }
    
    Write-Host "✗ Docker is not installed or not in PATH" -ForegroundColor Red
    return $false
}

# Function to check if Docker is running
function Test-DockerRunning {
    try {
        docker info 2>$null | Out-Null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ Docker is running" -ForegroundColor Green
            return $true
        }
    }
    catch {
        # Docker is not running
    }
    
    Write-Host "✗ Docker is not running" -ForegroundColor Red
    return $false
}

# Check Docker installation
Write-Host "1. Checking Docker installation..." -ForegroundColor Yellow

if (-not (Test-DockerInstalled)) {
    Write-Host ""
    Write-Host "Docker Desktop is not installed. Please install it first:" -ForegroundColor Red
    Write-Host "1. Go to https://www.docker.com/products/docker-desktop/" -ForegroundColor White
    Write-Host "2. Download Docker Desktop for Windows" -ForegroundColor White
    Write-Host "3. Run the installer and follow the setup instructions" -ForegroundColor White
    Write-Host "4. Restart your computer if prompted" -ForegroundColor White
    Write-Host "5. Start Docker Desktop" -ForegroundColor White
    Write-Host "6. Run this script again" -ForegroundColor White
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}

# Check if Docker is running
Write-Host "2. Checking if Docker is running..." -ForegroundColor Yellow

if (-not (Test-DockerRunning)) {
    Write-Host ""
    Write-Host "Docker is installed but not running. Please:" -ForegroundColor Red
    Write-Host "1. Start Docker Desktop from the Start menu" -ForegroundColor White
    Write-Host "2. Wait for Docker to start (you'll see the Docker icon in the system tray)" -ForegroundColor White
    Write-Host "3. Run this script again" -ForegroundColor White
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}

# Clean up any existing containers
Write-Host "3. Cleaning up existing containers..." -ForegroundColor Yellow
try {
    docker compose down -v 2>$null
    Write-Host "✓ Cleaned up existing containers" -ForegroundColor Green
}
catch {
    Write-Host "! No existing containers to clean up" -ForegroundColor Yellow
}

# Build and start the services
Write-Host "4. Building and starting Finance Admin Management System..." -ForegroundColor Yellow
Write-Host "This may take several minutes on first run..." -ForegroundColor Cyan

try {
    docker compose up --build -d
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "=======================================" -ForegroundColor Green
        Write-Host "✓ Finance Admin Management System Started Successfully!" -ForegroundColor Green
        Write-Host "=======================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "Services are starting up. Please wait 2-3 minutes for all services to be ready." -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Access URLs:" -ForegroundColor White
        Write-Host "• Application: http://localhost:8090/api/admin" -ForegroundColor White
        Write-Host "• Swagger UI: http://localhost:8090/api/admin/swagger-ui.html" -ForegroundColor White
        Write-Host "• Health Check: http://localhost:8090/api/admin/actuator/health" -ForegroundColor White
        Write-Host "• RabbitMQ Management: http://localhost:15672 (guest/guest)" -ForegroundColor White
        Write-Host ""
        Write-Host "Database Access:" -ForegroundColor White
        Write-Host "• PostgreSQL: localhost:5433/finance_admin (admin/password)" -ForegroundColor White
        Write-Host "• MongoDB: localhost:27017/finance_admin_docs" -ForegroundColor White
        Write-Host "• Redis: localhost:6380" -ForegroundColor White
        Write-Host ""
        Write-Host "Default Admin User:" -ForegroundColor White
        Write-Host "• Username: admin" -ForegroundColor White
        Write-Host "• Password: admin123" -ForegroundColor White
        Write-Host "• Email: admin@finance.com" -ForegroundColor White
        Write-Host ""
        Write-Host "Useful Commands:" -ForegroundColor White
        Write-Host "• View logs: docker compose logs -f" -ForegroundColor White
        Write-Host "• Stop services: docker compose down" -ForegroundColor White
        Write-Host "• Restart services: docker compose restart" -ForegroundColor White
        Write-Host ""
    }
    else {
        Write-Host "✗ Failed to start services. Check the error messages above." -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "✗ An error occurred while starting the services: $_" -ForegroundColor Red
    exit 1
}

# Check service health
Write-Host "5. Checking service health..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

try {
    $healthCheck = docker compose ps --format json | ConvertFrom-Json
    Write-Host ""
    Write-Host "Service Status:" -ForegroundColor White
    foreach ($service in $healthCheck) {
        $status = $service.State
        $name = $service.Service
        if ($status -eq "running") {
            Write-Host "✓ $name : $status" -ForegroundColor Green
        } else {
            Write-Host "! $name : $status" -ForegroundColor Yellow
        }
    }
}
catch {
    Write-Host "! Could not check service status" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Setup complete! The Finance Admin Management System is ready to use." -ForegroundColor Green
Write-Host ""
Read-Host "Press Enter to exit" 