FROM openjdk:17-jdk-slim

WORKDIR /app

COPY Backend/target/admin-management-*.jar app.jar

# Set environment variables
ENV SERVER_PORT=8090
ENV SPRING_PROFILES_ACTIVE=docker
ENV APP_JWT_SECRET=myVerySecretJWTKeyForFinanceAdminSystemThatIsAtLeast256BitsLong
ENV APP_JWT_EXPIRATION=3600000
ENV APP_JWT_REFRESH_EXPIRATION=604800000

# Expose the port
EXPOSE 8090

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8090/api/admin/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
