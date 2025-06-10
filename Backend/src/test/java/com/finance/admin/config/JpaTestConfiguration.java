package com.finance.admin.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;
import java.util.UUID;

/**
 * Test configuration for JPA auditing that includes entity scanning
 * Use this configuration for integration tests that need JPA auditing functionality
 */
@TestConfiguration
@EnableJpaAuditing
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
public class JpaTestConfiguration {

    @Bean
    public AuditorAware<UUID> auditorAware() {
        return () -> Optional.of(UUID.randomUUID());
    }
} 