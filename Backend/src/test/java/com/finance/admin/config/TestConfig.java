package com.finance.admin.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AuditorAware;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * Minimal test configuration for @DataJpaTest
 * Only provides essential beans without loading full application context
 */
@TestConfiguration
@EnableJpaRepositories(basePackages = "com.finance.admin")
@ActiveProfiles("test")
public class TestConfig {

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private JavaMailSender javaMailSender;

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

    /**
     * Test-specific security configuration to avoid servlet context issues
     */
    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/h2-console/**", "/swagger-ui/**", "/swagger-ui/index.html", "/v3/api-docs/**", "/actuator/health").permitAll()
                .anyRequest().authenticated()
            );

        // Allow H2 console frame
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}
