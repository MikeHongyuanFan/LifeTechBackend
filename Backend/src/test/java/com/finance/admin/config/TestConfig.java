package com.finance.admin.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * Comprehensive test configuration that provides all necessary beans for test scenarios
 * This configuration is specifically designed for H2 in-memory database testing
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {
    "com.finance.admin.user.repository",
    "com.finance.admin.auth.repository",
    "com.finance.admin.audit.repository",
    "com.finance.admin.client.repository",
    "com.finance.admin.investment.repository"
})
@EntityScan(basePackages = {
    "com.finance.admin.user.entity",
    "com.finance.admin.auth.entity", 
    "com.finance.admin.audit.entity",
    "com.finance.admin.client.model",
    "com.finance.admin.investment.model",
    "com.finance.admin.common.entity"
})
@AutoConfigureDataJpa
@TestPropertySource(locations = "classpath:application-test.yml")
@Profile("test")
public class TestConfig {

    /**
     * Primary DataSource bean for tests using H2 in-memory database
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("testdb-" + System.currentTimeMillis())
                .addScript("classpath:test-schema.sql")
                .addScript("classpath:test-data.sql")
                .build();
    }

    /**
     * Auditor aware for test environment
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("TEST_USER");
    }

    /**
     * Password encoder for test environment
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10); // Lower rounds for faster tests
    }
}
