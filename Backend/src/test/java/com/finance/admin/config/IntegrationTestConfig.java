package com.finance.admin.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

/**
 * Configuration for integration tests that require full application context
 */
@TestConfiguration
@TestPropertySource(locations = "classpath:application-test.yml")
public class IntegrationTestConfig {

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;
} 