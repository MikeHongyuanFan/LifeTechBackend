#!/bin/bash

# Test runner script for Finance Admin Management System

echo "=== Running Finance Admin Management System Tests ==="

# Set test profile
export SPRING_PROFILES_ACTIVE=test

# Set Java options
export JAVA_OPTS="-Xmx512m -Xms256m"

# Run different test types
echo "1. Running Unit Tests..."
./mvnw test -Dspring.profiles.active=test -Dtest="*Test" -DfailIfNoTests=false

echo "2. Running Integration Tests..."
./mvnw test -Dspring.profiles.active=test -Dtest="*IntegrationTest" -DfailIfNoTests=false

echo "3. Running All Tests..."
./mvnw test -Dspring.profiles.active=test

echo "=== Test Run Complete ===" 