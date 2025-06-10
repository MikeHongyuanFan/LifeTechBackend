package com.finance.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.admin.audit.repository.AuditLogRepository;
import com.finance.admin.auth.repository.AdminUserRepository;
import com.finance.admin.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base test configuration class that provides common setup for all tests
 */
@ActiveProfiles("test")
public abstract class BaseTestConfig {

    @MockBean
    protected UserRepository userRepository;

    @MockBean
    protected AdminUserRepository adminUserRepository;

    @MockBean
    protected AuditLogRepository auditLogRepository;

    @MockBean
    protected RedisTemplate<String, Object> redisTemplate;

    protected ObjectMapper objectMapper;

    @BeforeEach
    protected void baseSetUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }
} 