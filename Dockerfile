FROM openjdk:17-slim

WORKDIR /app

COPY target/*.jar app.jar

# Set environment variables
ENV SERVER_PORT=8090
ENV APP_JWT_SECRET=myVerySecretJWTKeyForTycoonAdminSystemThatIsAtLeast256BitsLong
ENV APP_JWT_EXPIRATION=3600000
ENV APP_JWT_REFRESH_EXPIRATION=604800000

# Expose the port
EXPOSE 8090

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
