# Test Configuration Guide

## Overview
This guide explains the test configuration fixes implemented to resolve the JPA metamodel and Spring Boot test issues.

## Issues Fixed

### 1. JPA Metamodel Issues
**Problem**: Tests were failing with "JPA metamodel must not be empty" errors.

**Solution**: 
- Excluded JPA auto-configuration from WebMvcTest
- Added proper test configurations with mocked repositories
- Created separate configurations for unit tests vs integration tests

### 2. Database Configuration Issues
**Problem**: Tests were trying to use JPA auditing but failing to initialize the JPA context.

**Solution**:
- Created H2 in-memory database configuration for tests
- Disabled external services (Redis, MongoDB, RabbitMQ) in test profile
- Added proper test-specific application properties

### 3. Test Configuration Issues
**Problem**: WebMvcTest was loading full application context instead of focused controller testing.

**Solution**:
- Created `WebMvcTestConfig` for minimal security configuration
- Added proper `@Import` and `@ActiveProfiles` annotations
- Excluded problematic auto-configurations

## Test Configuration Files

### 1. WebMvcTestConfig.java
```java
@TestConfiguration
@EnableWebSecurity
@Profile("test")
public class WebMvcTestConfig extends WebSecurityConfigurerAdapter
```
- Provides minimal security configuration for WebMvcTest
- Disables CSRF and enables stateless sessions
- Allows authentication endpoints without security

### 2. TestConfiguration.java
```java
@TestConfiguration
@EnableJpaAuditing
public class TestConfiguration
```
- Provides common test beans and mocks
- Mocks all JPA repositories
- Provides password encoder and auditor beans

### 3. IntegrationTestConfig.java
```java
@TestConfiguration
@TestPropertySource(locations = "classpath:application-test.yml")
public class IntegrationTestConfig
```
- Configuration for full integration tests
- Mocks external services like Redis

### 4. BaseTestConfig.java
```java
@ActiveProfiles("test")
public abstract class BaseTestConfig
```
- Base class for common test setup
- Provides common mocked beans
- Sets up ObjectMapper for JSON testing

## Test Types and Configurations

### Unit Tests (WebMvcTest)
```java
@WebMvcTest(controllers = AuthController.class,
    excludeAutoConfiguration = {
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
    })
@Import(WebMvcTestConfig.class)
@ActiveProfiles("test")
```

**Features**:
- Tests only the web layer
- Excludes JPA auto-configuration
- Uses mocked services and repositories
- Fast execution

### Integration Tests (SpringBootTest)
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfig.class)
@ActiveProfiles("test")
@Transactional
```

**Features**:
- Tests full application context
- Uses H2 in-memory database
- Mocks external services
- Transactional rollback for data isolation

## Application Properties

### Test Profile (application-test.properties)
```properties
# H2 Database for testing
spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE

# Disable external services
spring.autoconfigure.exclude=RedisAutoConfiguration,MongoDataAutoConfiguration,RabbitAutoConfiguration

# Test-specific JWT configuration
app.jwt.secret=testSecretKeyForFinanceAdminSystemThatIsAtLeast256BitsLongForTestingPurposes
```

## Running Tests

### Using Maven
```bash
# Run all tests
./mvnw test -Dspring.profiles.active=test

# Run specific test class
./mvnw test -Dtest=AuthControllerTest -Dspring.profiles.active=test

# Run integration tests only
./mvnw test -Dtest="*IntegrationTest" -Dspring.profiles.active=test
```

### Using Test Runner Script
```bash
# Make script executable
chmod +x Backend/run-tests.sh

# Run all test types
./Backend/run-tests.sh
```

## Test Structure

### Controller Tests
- Use `@WebMvcTest` with specific controller
- Mock all service dependencies
- Test HTTP endpoints and responses
- Validate JSON serialization/deserialization

### Service Tests
- Use `@ExtendWith(MockitoExtension.class)`
- Mock repository dependencies
- Test business logic
- Validate exception handling

### Integration Tests
- Use `@SpringBootTest` with full context
- Test end-to-end functionality
- Validate database operations
- Test security configurations

## Common Test Patterns

### 1. Controller Test Setup
```java
@WebMvcTest(controllers = UserController.class,
    excludeAutoConfiguration = {
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
    })
@Import(WebMvcTestConfig.class)
@ActiveProfiles("test")
public class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    @MockBean
    private UserRepository userRepository;
}
```

### 2. Integration Test Setup
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfig.class)
@ActiveProfiles("test")
@Transactional
public class UserIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
}
```

### 3. Security Test Setup
```java
@Test
@WithMockUser(roles = {"ADMIN"})
void testSecuredEndpoint() throws Exception {
    mockMvc.perform(get("/admin/users"))
        .andExpect(status().isOk());
}
```

## Troubleshooting

### Common Issues and Solutions

1. **JPA Metamodel Empty Error**
   - Ensure JPA auto-configuration is excluded in WebMvcTest
   - Add `@MockBean` for all repository dependencies

2. **Security Configuration Issues**
   - Import `WebMvcTestConfig` for minimal security setup
   - Use `@WithMockUser` for authenticated tests

3. **Database Connection Issues**
   - Verify H2 dependency is in test scope
   - Check application-test.properties configuration

4. **Bean Creation Issues**
   - Mock all external service dependencies
   - Ensure test profile is active

## Best Practices

1. **Test Isolation**
   - Use `@Transactional` for database rollback
   - Clear mocks between tests
   - Use separate test data for each test

2. **Performance**
   - Use WebMvcTest for controller testing
   - Mock external services
   - Use H2 in-memory database

3. **Maintainability**
   - Extend BaseTestConfig for common setup
   - Use test data builders
   - Keep tests focused and simple

4. **Coverage**
   - Test happy path and error scenarios
   - Validate security configurations
   - Test edge cases and boundary conditions

## Dependencies Required

Add these dependencies to your `pom.xml` for testing:

```xml
<dependencies>
    <!-- Spring Boot Test Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Spring Security Test -->
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- H2 Database for Testing -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Testcontainers (optional for integration tests) -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

This configuration ensures that all tests run properly with appropriate isolation and mocking of dependencies. 