package com.finance.admin.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.admin.auth.controller.AuthController;
import com.finance.admin.auth.dto.LoginRequest;
import com.finance.admin.auth.dto.LoginResponse;
import com.finance.admin.auth.dto.MfaVerificationRequest;
import com.finance.admin.auth.entity.AdminRole;
import com.finance.admin.auth.entity.AdminUser;
import com.finance.admin.auth.entity.AdminUserStatus;
import com.finance.admin.auth.repository.AdminUserRepository;
import com.finance.admin.auth.service.AuthenticationService;
import com.finance.admin.auth.service.MfaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Authentication API endpoints
 * Using minimal standalone MockMvc configuration
 */
public class AuthenticationIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MfaService mfaService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    private AuthController authController;
    private AdminUser testUser;
    private AdminUser mfaUser;
    private AdminUser lockedUser;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Create controller and inject dependencies
        authController = new AuthController();
        objectMapper = new ObjectMapper();
        
        // Use reflection to inject the mocked service
        Field authServiceField = AuthController.class.getDeclaredField("authenticationService");
        authServiceField.setAccessible(true);
        authServiceField.set(authController, authenticationService);
        
        // Setup MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        
        // Create test users
        createTestUsers();
    }

    private void createTestUsers() {
        // Regular user
        testUser = new AdminUser("testadmin", "test@finance.com", "encodedPassword123");
        testUser.setId(UUID.randomUUID());
        testUser.setStatus(AdminUserStatus.ACTIVE);
        testUser.getRoles().add(AdminRole.SYSTEM_ADMIN);
        testUser.addAllowedIp("127.0.0.1");

        // MFA-enabled user
        mfaUser = new AdminUser("mfaadmin", "mfa@finance.com", "encodedPassword123");
        mfaUser.setId(UUID.randomUUID());
        mfaUser.setStatus(AdminUserStatus.ACTIVE);
        mfaUser.setMfaEnabled(true);
        mfaUser.setMfaSecret("testSecret");
        mfaUser.getRoles().add(AdminRole.SUPER_ADMIN);
        mfaUser.addAllowedIp("127.0.0.1");

        // Locked user
        lockedUser = new AdminUser("lockedadmin", "locked@finance.com", "encodedPassword123");
        lockedUser.setId(UUID.randomUUID());
        lockedUser.setStatus(AdminUserStatus.ACTIVE);
        lockedUser.setFailedLoginAttempts(5);
        lockedUser.lockAccount(LocalDateTime.now().plusMinutes(15));
        lockedUser.getRoles().add(AdminRole.ANALYST);
    }

    private LoginResponse.UserInfo createUserInfo(AdminUser user) {
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setMfaEnabled(user.getMfaEnabled());
        return userInfo;
    }

    @Test
    void testSuccessfulLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testadmin", "password123");
        
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setSuccess(true);
        loginResponse.setAccessToken("test-access-token");
        loginResponse.setRefreshToken("test-refresh-token");
        loginResponse.setUser(createUserInfo(testUser));

        when(authenticationService.authenticateUser(any(LoginRequest.class), any(HttpServletRequest.class)))
                .thenReturn(loginResponse);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("test-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("test-refresh-token"));
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testadmin", "wrongpassword");

        when(authenticationService.authenticateUser(any(LoginRequest.class), any(HttpServletRequest.class)))
                .thenThrow(new RuntimeException("Invalid username or password"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginWithNonExistentUser() throws Exception {
        LoginRequest loginRequest = new LoginRequest("nonexistent", "password123");

        when(authenticationService.authenticateUser(any(LoginRequest.class), any(HttpServletRequest.class)))
                .thenThrow(new RuntimeException("Invalid username or password"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMfaRequiredLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest("mfaadmin", "password123");
        
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setSuccess(true);
        loginResponse.setRequiresMfa(true);
        loginResponse.setMfaToken("test-mfa-token");

        when(authenticationService.authenticateUser(any(LoginRequest.class), any(HttpServletRequest.class)))
                .thenReturn(loginResponse);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requiresMfa").value(true))
                .andExpect(jsonPath("$.data.mfaToken").value("test-mfa-token"));
    }

    @Test
    void testMfaVerificationSuccess() throws Exception {
        MfaVerificationRequest mfaRequest = new MfaVerificationRequest("test-mfa-token", "123456");
        
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setSuccess(true);
        loginResponse.setAccessToken("test-access-token");
        loginResponse.setRefreshToken("test-refresh-token");
        loginResponse.setUser(createUserInfo(mfaUser));

        when(authenticationService.verifyMfa(any(MfaVerificationRequest.class), any(HttpServletRequest.class)))
                .thenReturn(loginResponse);

        mockMvc.perform(post("/auth/verify-mfa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mfaRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("test-access-token"));
    }

    @Test
    void testMfaVerificationWithInvalidCode() throws Exception {
        MfaVerificationRequest mfaRequest = new MfaVerificationRequest("test-mfa-token", "invalid");

        when(authenticationService.verifyMfa(any(MfaVerificationRequest.class), any(HttpServletRequest.class)))
                .thenThrow(new RuntimeException("Invalid MFA code"));

        mockMvc.perform(post("/auth/verify-mfa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mfaRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSuccessfulLogout() throws Exception {
        // Note: logout method in AuthenticationService is void, not boolean
        // So we can't mock a return value, but we can verify it doesn't throw
        mockMvc.perform(post("/auth/logout")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Authentication service is healthy"));
    }
} 
