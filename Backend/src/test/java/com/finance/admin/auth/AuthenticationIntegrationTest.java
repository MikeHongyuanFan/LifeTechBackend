package com.finance.admin.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.admin.auth.dto.LoginRequest;
import com.finance.admin.auth.dto.LoginResponse;
import com.finance.admin.auth.dto.MfaVerificationRequest;
import com.finance.admin.auth.entity.AdminRole;
import com.finance.admin.auth.entity.AdminUser;
import com.finance.admin.auth.entity.AdminUserStatus;
import com.finance.admin.auth.repository.AdminUserRepository;
import com.finance.admin.auth.service.MfaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MfaService mfaService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private AdminUser testUser;
    private AdminUser mfaUser;
    private AdminUser lockedUser;

    @BeforeEach
    void setUp() {
        // Clear Redis cache
        try {
            redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
        } catch (Exception e) {
            // If Redis is not available in test environment, just log and continue
            System.out.println("Redis not available in test environment: " + e.getMessage());
        }
        
        // Create test users
        createTestUsers();
    }

    private void createTestUsers() {
        // Regular user
        testUser = new AdminUser("testadmin", "test@finance.com", passwordEncoder.encode("password123"));
        testUser.setStatus(AdminUserStatus.ACTIVE);
        testUser.getRoles().add(AdminRole.SYSTEM_ADMIN);
        testUser.addAllowedIp("127.0.0.1");
        adminUserRepository.save(testUser);

        // MFA-enabled user
        mfaUser = new AdminUser("mfaadmin", "mfa@finance.com", passwordEncoder.encode("password123"));
        mfaUser.setStatus(AdminUserStatus.ACTIVE);
        mfaUser.setMfaEnabled(true);
        mfaUser.setMfaSecret(mfaService.generateSecret());
        mfaUser.getRoles().add(AdminRole.SUPER_ADMIN);
        mfaUser.addAllowedIp("127.0.0.1");
        adminUserRepository.save(mfaUser);

        // Locked user
        lockedUser = new AdminUser("lockedadmin", "locked@finance.com", passwordEncoder.encode("password123"));
        lockedUser.setStatus(AdminUserStatus.ACTIVE);
        lockedUser.setFailedLoginAttempts(5);
        lockedUser.lockAccount(LocalDateTime.now().plusMinutes(15));
        lockedUser.getRoles().add(AdminRole.ANALYST);
        adminUserRepository.save(lockedUser);
    }

    @Test
    void testSuccessfulLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testadmin", "password123");

        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.user.username").value("testadmin"))
                .andExpect(jsonPath("$.data.user.roles").isArray())
                .andReturn();

        // Verify user's last login was updated
        AdminUser updatedUser = adminUserRepository.findByUsername("testadmin").orElse(null);
        assertNotNull(updatedUser);
        assertNotNull(updatedUser.getLastLogin());
        assertEquals(0, updatedUser.getFailedLoginAttempts());
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testadmin", "wrongpassword");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Invalid username or password"));

        // Verify failed attempts were incremented
        AdminUser updatedUser = adminUserRepository.findByUsername("testadmin").orElse(null);
        assertNotNull(updatedUser);
        assertEquals(1, updatedUser.getFailedLoginAttempts());
    }

    @Test
    void testLoginWithNonExistentUser() throws Exception {
        LoginRequest loginRequest = new LoginRequest("nonexistent", "password123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    @Test
    void testMfaRequiredLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest("mfaadmin", "password123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requiresMfa").value(true))
                .andExpect(jsonPath("$.data.mfaToken").exists())
                .andExpect(jsonPath("$.data.accessToken").doesNotExist());
    }

    @Test
    void testMfaVerificationSuccess() throws Exception {
        // First, login to get MFA token
        LoginRequest loginRequest = new LoginRequest("mfaadmin", "password123");
        
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);
        String mfaToken = loginResponse.getMfaToken();

        // Generate valid MFA code
        String mfaCode = mfaService.generateCode(mfaUser.getMfaSecret(), 
                System.currentTimeMillis() / 30000);

        // Verify MFA
        MfaVerificationRequest mfaRequest = new MfaVerificationRequest(mfaToken, mfaCode);

        mockMvc.perform(post("/auth/verify-mfa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mfaRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.user.username").value("mfaadmin"));
    }

    @Test
    void testMfaVerificationWithInvalidCode() throws Exception {
        // First, login to get MFA token
        LoginRequest loginRequest = new LoginRequest("mfaadmin", "password123");
        
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);
        String mfaToken = loginResponse.getMfaToken();

        // Use invalid MFA code
        MfaVerificationRequest mfaRequest = new MfaVerificationRequest(mfaToken, "123456");

        mockMvc.perform(post("/auth/verify-mfa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mfaRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Invalid MFA code"));
    }

    @Test
    void testLoginWithLockedAccount() throws Exception {
        LoginRequest loginRequest = new LoginRequest("lockedadmin", "password123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Account is temporarily locked"));
    }

    @Test
    void testIpWhitelistRestriction() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testadmin", "password123");

        // Try login from non-whitelisted IP
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("X-Forwarded-For", "192.168.1.100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("IP address not allowed for this user"));
    }

    @Test
    void testAccountLockingAfterMultipleFailedAttempts() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testadmin", "wrongpassword");

        // Make 5 failed attempts
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest))
                    .header("X-Forwarded-For", "127.0.0.1"))
                    .andExpect(status().isBadRequest());
        }

        // Verify account is locked
        AdminUser updatedUser = adminUserRepository.findByUsername("testadmin").orElse(null);
        assertNotNull(updatedUser);
        assertEquals(5, updatedUser.getFailedLoginAttempts());
        assertNotNull(updatedUser.getLockedUntil());
        assertTrue(updatedUser.getLockedUntil().isAfter(LocalDateTime.now()));
    }

    @Test
    void testSuccessfulLogout() throws Exception {
        // First login to get a token
        LoginRequest loginRequest = new LoginRequest("testadmin", "password123");
        
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);
        String accessToken = loginResponse.getAccessToken();

        // Logout
        mockMvc.perform(post("/auth/logout")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));

        // Verify token is blacklisted in Redis
        assertTrue(redisTemplate.hasKey("blacklisted_token:" + accessToken));
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Authentication service is healthy"));
    }

    @Test
    void testValidationErrors() throws Exception {
        // Test with empty username
        LoginRequest invalidRequest = new LoginRequest("", "password123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isBadRequest());

        // Test with short password
        invalidRequest = new LoginRequest("testadmin", "123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testIpBlocking() throws Exception {
        LoginRequest loginRequest = new LoginRequest("nonexistent", "wrongpassword");

        // Make multiple failed attempts from same IP to trigger IP blocking
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest))
                    .header("X-Forwarded-For", "192.168.1.200"))
                    .andExpect(status().isBadRequest());
        }

        // Verify IP is blocked
        assertTrue(redisTemplate.hasKey("blocked_ip:192.168.1.200"));

        // Next attempt should be blocked
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("X-Forwarded-For", "192.168.1.200"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("IP address is temporarily blocked"));
    }
} 
