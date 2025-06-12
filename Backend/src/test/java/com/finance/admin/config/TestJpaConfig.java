package com.finance.admin.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Test configuration for JPA that enables entity scanning and repositories
 */
@Configuration
@Primary
@EntityScan(basePackages = {
    "com.finance.admin.user.entity",
    "com.finance.admin.auth.entity", 
    "com.finance.admin.audit.entity",
    "com.finance.admin.common.entity",
    "com.finance.admin.security.entity"
})
@EnableJpaRepositories(basePackages = {
    "com.finance.admin.user.repository",
    "com.finance.admin.auth.repository",
    "com.finance.admin.audit.repository"
})
public class TestJpaConfig {
    // Configuration for JPA tests
} 
