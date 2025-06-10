#!/bin/bash

echo "Setting up LifeTech System..."

# Check if Redis port is specified, default to 6380
REDIS_PORT=${REDIS_PORT:-6380}
echo "Using Redis port: $REDIS_PORT"

# Check if PostgreSQL port is specified, default to 5433
POSTGRES_PORT=${POSTGRES_PORT:-5433}
echo "Using PostgreSQL port: $POSTGRES_PORT"

# Function to check if a process is running on a specific port
check_port_in_use() {
    local port=$1
    if lsof -i :$port -t >/dev/null; then
        return 0  # Port is in use
    else
        return 1  # Port is not in use
    fi
}

# Function to start Docker services
start_docker_services() {
    # Stop and remove existing containers
    echo "Cleaning up existing containers..."
    docker-compose down -v

    # Clean Docker system
    echo "Cleaning Docker system..."
    docker system prune -f

    # Build and start containers
    echo "Starting Docker containers..."
    docker-compose up --build -d postgres mongodb redis rabbitmq

    # Function to check service health
    check_service_health() {
        local service=$1
        local max_attempts=$2
        local attempt=1

        echo "Waiting for $service to be healthy..."
        while [ $attempt -le $max_attempts ]; do
            if docker-compose ps $service | grep -q "healthy"; then
                echo "$service is healthy!"
                return 0
            fi
            
            # Show logs if service is taking too long
            if [ $attempt -eq 5 ]; then
                echo "Service $service is taking longer than expected. Showing logs:"
                docker-compose logs --tail=50 $service
            fi
            
            echo "Attempt $attempt/$max_attempts: $service is not ready yet..."
            sleep 5
            attempt=$((attempt + 1))
        done
        
        echo "$service failed to become healthy after $max_attempts attempts"
        echo "Last few lines of logs:"
        docker-compose logs --tail=100 $service
        return 1
    }

    # Wait for services to be healthy
    check_service_health "postgres" 12 || exit 1
    check_service_health "mongodb" 12 || exit 1
    check_service_health "redis" 12 || exit 1
    check_service_health "rabbitmq" 12 || exit 1

    echo "Infrastructure services are healthy!"
}

# Function to build and start the Spring Boot application
start_spring_boot() {
    echo "Building and starting Spring Boot application..."
    
    # Kill any existing Spring Boot process
    if pgrep -f "spring-boot" > /dev/null; then
        echo "Stopping existing Spring Boot application..."
        pkill -f "spring-boot"
        sleep 2
    fi
    
    # Set environment variables for database connection
    export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:$POSTGRES_PORT/finance_admin"
    export SPRING_DATASOURCE_USERNAME="admin"
    export SPRING_DATASOURCE_PASSWORD="password"
    export SPRING_REDIS_HOST="localhost"
    export SPRING_REDIS_PORT="$REDIS_PORT"
    
    # Build the Spring Boot application
    echo "Building Spring Boot application..."
    cd $(dirname "$0")
    ./mvnw clean package -DskipTests
    
    # Check if build was successful
    if [ $? -ne 0 ]; then
        echo "Maven build failed. Please fix the build issues before continuing."
        exit 1
    fi
    
    # Start the Spring Boot application with environment variables
    echo "Starting Spring Boot application..."
    nohup ./mvnw spring-boot:run > app.log 2>&1 &
    
    # Wait for the application to start
    echo "Waiting for Spring Boot application to start..."
    max_attempts=30
    attempt=1
    while [ $attempt -le $max_attempts ]; do
        if curl -s http://localhost:8090/api/admin/actuator/health | grep -q "UP"; then
            echo "Spring Boot application is healthy!"
            return 0
        fi
        
        if [ $attempt -eq 10 ]; then
            echo "Application is taking longer than expected. Showing logs:"
            tail -n 50 app.log
        fi
        
        echo "Attempt $attempt/$max_attempts: Application is not ready yet..."
        sleep 5
        attempt=$((attempt + 1))
    done
    
    echo "Application failed to become healthy after $max_attempts attempts"
    echo "Last few lines of logs:"
    tail -n 100 app.log
    return 1
}

# Main execution
echo "Starting all services..."

# Start Docker services
start_docker_services

# Start Spring Boot application
start_spring_boot

# Show service status
echo "Current service status:"
docker-compose ps

# Show application health details
echo "Application health details:"
curl -s http://localhost:8090/api/admin/actuator/health | python -m json.tool 2>/dev/null || curl -s http://localhost:8090/api/admin/actuator/health

echo "Setup completed successfully!"
echo "Swagger UI is available at: http://localhost:8090/api/admin/swagger-ui/index.html"
echo "API documentation is available at: http://localhost:8090/api/admin/openapi.json"
echo "Application logs are available in: $(dirname "$0")/app.log"
