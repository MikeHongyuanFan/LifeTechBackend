package com.finance.admin.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Test configuration for JPA that enables entity scanning and repositories
 * This configuration resolves the "JPA metamodel must not be empty" error
 */
@TestConfiguration
@EntityScan(basePackages = {
    "com.finance.admin.user.entity",
    "com.finance.admin.auth.entity", 
    "com.finance.admin.audit.entity",
    "com.finance.admin.common.entity"
})
@EnableJpaRepositories(basePackages = {
    "com.finance.admin.user.repository",
    "com.finance.admin.auth.repository",
    "com.finance.admin.audit.repository"
})
@Profile("test")
public class TestJpaConfig {
    // This configuration enables proper entity scanning for tests
} 