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
    "com.finance.admin.client.model",
    "com.finance.admin.client.document.model",
    "com.finance.admin.client.wallet.model",
    "com.finance.admin.investment.model",
    "com.finance.admin.certificate.model",
    "com.finance.admin.enquiry.model",
    "com.finance.admin.client.notification.model",
    "com.finance.admin.audit.entity"
})
@EnableJpaRepositories(basePackages = {
    "com.finance.admin.user.repository",
    "com.finance.admin.auth.repository",
    "com.finance.admin.client.repository",
    "com.finance.admin.client.document.repository",
    "com.finance.admin.client.wallet.repository",
    "com.finance.admin.investment.repository",
    "com.finance.admin.certificate.repository",
    "com.finance.admin.enquiry.repository",
    "com.finance.admin.client.notification.repository",
    "com.finance.admin.audit.repository"
})
public class TestJpaConfig {
    // Configuration for JPA tests
} 
