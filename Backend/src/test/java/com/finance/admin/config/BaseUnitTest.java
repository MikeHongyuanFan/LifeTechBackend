package com.finance.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base unit test class for pure unit tests that don't require Spring context.
 * This class provides common setup for lightweight testing scenarios.
 */
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseUnitTest {

    protected ObjectMapper objectMapper;

    /**
     * Set up common test configuration before each test
     */
    @BeforeEach
    protected void setUp() {
        // Configure ObjectMapper for JSON serialization/deserialization
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    /**
     * Get the configured ObjectMapper instance
     */
    protected ObjectMapper getObjectMapper() {
        return objectMapper;
    }
} 