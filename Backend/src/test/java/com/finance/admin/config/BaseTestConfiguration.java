package com.finance.admin.config;

import com.finance.admin.audit.repository.AuditLogRepository;
import com.finance.admin.auth.repository.AdminUserRepository;
import com.finance.admin.user.repository.UserRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;



/**
 * Base test configuration for Spring Boot tests
 * This configuration provides necessary beans and mocks for testing
 */
@TestConfiguration
public class BaseTestConfiguration {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AdminUserRepository adminUserRepository;

    @MockBean
    private AuditLogRepository auditLogRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


} 