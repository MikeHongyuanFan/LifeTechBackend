package com.finance.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Base integration test class that provides common setup for all integration tests.
 * This class handles Spring Boot context loading, database setup, and common configuration.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {com.finance.admin.AdminManagementApplication.class, TestConfig.class}
)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@AutoConfigureWebMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected ObjectMapper objectMapper;

    protected MockMvc mockMvc;

    // Mock external dependencies that are not needed for tests
    @MockBean
    protected RedisTemplate<String, Object> redisTemplate;

    /**
     * Set up common test configuration before each test
     */
    @BeforeEach
    protected void setUp() {
        // Configure MockMvc with the full web application context
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
        
        // Configure ObjectMapper for JSON serialization/deserialization
        objectMapper.findAndRegisterModules();
    }

    /**
     * Get the configured MockMvc instance
     */
    protected MockMvc getMockMvc() {
        return mockMvc;
    }

    /**
     * Get the configured ObjectMapper instance
     */
    protected ObjectMapper getObjectMapper() {
        return objectMapper;
    }
} 