FROM openjdk:17-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/*.jar app.jar

# Create directory for custom classes
RUN mkdir -p /app/custom-classes/com/tycoon/admin/config

# Set environment variables
ENV SERVER_PORT=8090
ENV SPRING_PROFILES_ACTIVE=docker
ENV MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
ENV MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS=always
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dspring.output.ansi.enabled=ALWAYS"

# Expose the port
EXPOSE 8090

# Add health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
  CMD curl -f http://localhost:8090/api/admin/actuator/health || exit 1

# Run the application with optimizations for containers
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
