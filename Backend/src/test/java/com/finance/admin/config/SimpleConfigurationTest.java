package com.finance.admin.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simple test to verify basic configuration loading without conflicts
 * Using the successful minimal configuration pattern
 */
@SpringBootTest
@ContextConfiguration(classes = SimpleConfigurationTest.TestConfig.class)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
public class SimpleConfigurationTest {

    @EnableAutoConfiguration
    @EntityScan(basePackages = {
        "com.finance.admin.user.entity",
        "com.finance.admin.auth.entity", 
        "com.finance.admin.audit.entity",
        "com.finance.admin.common.entity"
    })
    static class TestConfig {
        // Minimal configuration for basic testing
    }

    @Test
    void contextLoads() {
        // This test verifies that the Spring application context loads successfully
        assertTrue(true, "Application context loaded successfully");
    }

    @Test
    void testBasicConfiguration() {
        // Basic test to ensure configuration is working
        assertTrue(true, "Basic configuration test passed");
    }
} 