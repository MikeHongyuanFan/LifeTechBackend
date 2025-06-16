package com.finance.admin.client.controller;

import com.finance.admin.client.dto.ClientLoginRequest;
import com.finance.admin.client.service.ClientAuthService;
import com.finance.admin.common.exception.GlobalExceptionHandler;
import com.finance.admin.config.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the Client Authentication API endpoints
 * Testing all authentication-related functionality for client portal
 */
@DisplayName("Client Authentication Controller Tests")
public class ClientAuthControllerTest extends BaseUnitTest {

    private MockMvc mockMvc;
    private ClientAuthService clientAuthService;
    private ClientAuthController clientAuthController;

    // Test data
    private ClientLoginRequest loginRequest;
    private Map<String, Object> successLoginResponse;
    private Map<String, Object> successLogoutResponse;
    private Map<String, Object> profileResponse;
    private Map<String, Object> sessionResponse;
    private String validToken = "Bearer valid-jwt-token";
    private String validRefreshToken = "valid-refresh-token";
    private UUID testSessionId = UUID.randomUUID();

    @BeforeEach
    protected void setUp() {
        super.setUp(); // Call parent setup
        
        // Create mocks
        clientAuthService = mock(ClientAuthService.class);
        clientAuthController = new ClientAuthController(clientAuthService);
        
        // Setup MockMvc with the controller and global exception handler
        mockMvc = MockMvcBuilders.standaloneSetup(clientAuthController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        // Setup test data
        setupTestData();
    }

    private void setupTestData() {
        // Setup login request
        loginRequest = ClientLoginRequest.builder()
                .emailOrPhone("client@example.com")
                .password("password123")
                .rememberMe(false)
                .deviceFingerprint("test-device-fingerprint")
                .build();

        // Setup successful login response
        successLoginResponse = new HashMap<>();
        successLoginResponse.put("success", true);
        successLoginResponse.put("message", "Login successful");
        successLoginResponse.put("accessToken", "access-token-123");
        successLoginResponse.put("refreshToken", "refresh-token-123");
        successLoginResponse.put("tokenType", "Bearer");
        successLoginResponse.put("expiresIn", 3600);
        
        Map<String, Object> clientSummary = new HashMap<>();
        clientSummary.put("id", 1L);
        clientSummary.put("firstName", "John");
        clientSummary.put("lastName", "Doe");
        clientSummary.put("email", "client@example.com");
        successLoginResponse.put("client", clientSummary);

        // Setup logout response
        successLogoutResponse = new HashMap<>();
        successLogoutResponse.put("success", true);
        successLogoutResponse.put("message", "Logout successful");
        successLogoutResponse.put("timestamp", LocalDateTime.now());

        // Setup profile response
        profileResponse = new HashMap<>();
        profileResponse.put("success", true);
        profileResponse.put("client", clientSummary);

        // Setup session response
        sessionResponse = new HashMap<>();
        sessionResponse.put("success", true);
        sessionResponse.put("valid", true);
        sessionResponse.put("clientId", 1L);
    }

    // ================ POST /api/client/auth/login Tests ================

    @Test
    @DisplayName("Should login client successfully with valid credentials")
    void testLogin_ValidCredentials_Success() throws Exception {
        // Given
        when(clientAuthService.authenticateClient(any(ClientLoginRequest.class)))
                .thenReturn(successLoginResponse);

        // When & Then
        mockMvc.perform(post("/api/client/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(loginRequest))
                .header("X-Forwarded-For", "192.168.1.1")
                .header("User-Agent", "Test Browser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.accessToken").value("access-token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-123"))
                .andExpect(jsonPath("$.client.id").value(1))
                .andExpect(jsonPath("$.client.firstName").value("John"));

        verify(clientAuthService).authenticateClient(any(ClientLoginRequest.class));
    }

    @Test
    @DisplayName("Should return 401 when login with invalid credentials")
    void testLogin_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Given
        when(clientAuthService.authenticateClient(any(ClientLoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/client/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(loginRequest))
                .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isUnauthorized());

        verify(clientAuthService).authenticateClient(any(ClientLoginRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when login with missing email/phone")
    void testLogin_MissingEmailOrPhone_ReturnsBadRequest() throws Exception {
        // Given
        ClientLoginRequest invalidRequest = ClientLoginRequest.builder()
                .password("password123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/client/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 423 when account is locked")
    void testLogin_AccountLocked_ReturnsLocked() throws Exception {
        // Given
        when(clientAuthService.authenticateClient(any(ClientLoginRequest.class)))
                .thenThrow(new RuntimeException("Account is temporarily locked. Please try again later."));

        // When & Then
        mockMvc.perform(post("/api/client/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(loginRequest))
                .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isLocked());

        verify(clientAuthService).authenticateClient(any(ClientLoginRequest.class));
    }

    @Test
    @DisplayName("Should handle remember me login successfully")
    void testLogin_WithRememberMe_Success() throws Exception {
        // Given
        ClientLoginRequest rememberMeRequest = ClientLoginRequest.builder()
                .emailOrPhone("client@example.com")
                .password("password123")
                .rememberMe(true)
                .build();

        Map<String, Object> rememberMeResponse = new HashMap<>(successLoginResponse);
        rememberMeResponse.put("rememberMeToken", "remember-me-token-123");
        rememberMeResponse.put("rememberMeExpiresAt", LocalDateTime.now().plusDays(30));

        when(clientAuthService.authenticateClient(any(ClientLoginRequest.class)))
                .thenReturn(rememberMeResponse);

        // When & Then
        mockMvc.perform(post("/api/client/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(rememberMeRequest))
                .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.rememberMeToken").value("remember-me-token-123"));

        verify(clientAuthService).authenticateClient(any(ClientLoginRequest.class));
    }

    // ================ POST /api/client/auth/logout Tests ================

    @Test
    @DisplayName("Should logout client successfully")
    void testLogout_ValidToken_Success() throws Exception {
        // Given
        when(clientAuthService.logoutClient(anyString(), anyString()))
                .thenReturn(successLogoutResponse);

        // When & Then
        mockMvc.perform(post("/api/client/auth/logout")
                .header("Authorization", validToken)
                .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));

        verify(clientAuthService).logoutClient(eq(validToken), eq("192.168.1.1"));
    }

    @Test
    @DisplayName("Should handle logout with invalid token gracefully")
    void testLogout_InvalidToken_ReturnsSuccess() throws Exception {
        // Given
        when(clientAuthService.logoutClient(anyString(), anyString()))
                .thenReturn(successLogoutResponse); // Service handles gracefully

        // When & Then
        mockMvc.perform(post("/api/client/auth/logout")
                .header("Authorization", "Bearer invalid-token")
                .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(clientAuthService).logoutClient(eq("Bearer invalid-token"), eq("192.168.1.1"));
    }

    // ================ POST /api/client/auth/forgot-password Tests ================

    @Test
    @DisplayName("Should initiate password reset successfully")
    void testForgotPassword_ValidEmail_Success() throws Exception {
        // Given
        Map<String, String> request = Map.of("email", "client@example.com");
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Password reset instructions sent to your email");
        response.put("resetToken", "reset-token-123");

        when(clientAuthService.initiatePasswordReset(anyString(), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/client/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(request))
                .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset instructions sent to your email"));

        verify(clientAuthService).initiatePasswordReset(eq("client@example.com"), eq("192.168.1.1"));
    }

    @Test
    @DisplayName("Should handle forgot password for non-existent email")
    void testForgotPassword_NonExistentEmail_ReturnsGenericMessage() throws Exception {
        // Given
        Map<String, String> request = Map.of("email", "nonexistent@example.com");
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "If the email exists, password reset instructions have been sent");

        when(clientAuthService.initiatePasswordReset(anyString(), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/client/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(request))
                .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(clientAuthService).initiatePasswordReset(eq("nonexistent@example.com"), eq("192.168.1.1"));
    }

    // ================ POST /api/client/auth/reset-password Tests ================

    @Test
    @DisplayName("Should reset password successfully with valid token")
    void testResetPassword_ValidToken_Success() throws Exception {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("resetToken", "valid-reset-token");
        request.put("newPassword", "newPassword123");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Password reset successful");

        when(clientAuthService.resetPassword(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/client/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(request))
                .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset successful"));

        verify(clientAuthService).resetPassword(eq("valid-reset-token"), eq("newPassword123"), eq("192.168.1.1"));
    }

    @Test
    @DisplayName("Should return 401 when reset password with invalid token")
    void testResetPassword_InvalidToken_ReturnsUnauthorized() throws Exception {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("resetToken", "invalid-token");
        request.put("newPassword", "newPassword123");

        when(clientAuthService.resetPassword(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Invalid or expired reset token"));

        // When & Then
        mockMvc.perform(post("/api/client/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(request))
                .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isUnauthorized());

        verify(clientAuthService).resetPassword(eq("invalid-token"), eq("newPassword123"), eq("192.168.1.1"));
    }

    // ================ GET /api/client/auth/profile Tests ================

    @Test
    @DisplayName("Should get client profile successfully")
    void testGetProfile_ValidToken_Success() throws Exception {
        // Given
        when(clientAuthService.getClientProfile(anyString()))
                .thenReturn(profileResponse);

        // When & Then
        mockMvc.perform(get("/api/client/auth/profile")
                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.client.id").value(1))
                .andExpect(jsonPath("$.client.firstName").value("John"));

        verify(clientAuthService).getClientProfile(eq(validToken));
    }

    @Test
    @DisplayName("Should return 401 when get profile with invalid token")
    void testGetProfile_InvalidToken_ReturnsUnauthorized() throws Exception {
        // Given
        when(clientAuthService.getClientProfile(anyString()))
                .thenThrow(new RuntimeException("Invalid or expired token"));

        // When & Then
        mockMvc.perform(get("/api/client/auth/profile")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        verify(clientAuthService).getClientProfile(eq("Bearer invalid-token"));
    }

    // ================ POST /api/client/auth/refresh-token Tests ================

    @Test
    @DisplayName("Should refresh token successfully")
    void testRefreshToken_ValidRefreshToken_Success() throws Exception {
        // Given
        Map<String, String> request = Map.of("refreshToken", validRefreshToken);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("accessToken", "new-access-token");
        response.put("refreshToken", "new-refresh-token");
        response.put("expiresIn", 3600);

        when(clientAuthService.refreshToken(anyString(), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/client/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(request))
                .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));

        verify(clientAuthService).refreshToken(eq(validRefreshToken), eq("192.168.1.1"));
    }

    @Test
    @DisplayName("Should return 401 when refresh with invalid token")
    void testRefreshToken_InvalidRefreshToken_ReturnsUnauthorized() throws Exception {
        // Given
        Map<String, String> request = Map.of("refreshToken", "invalid-refresh-token");

        when(clientAuthService.refreshToken(anyString(), anyString()))
                .thenThrow(new RuntimeException("Invalid or expired refresh token"));

        // When & Then
        mockMvc.perform(post("/api/client/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(request))
                .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isUnauthorized());

        verify(clientAuthService).refreshToken(eq("invalid-refresh-token"), eq("192.168.1.1"));
    }

    // ================ GET /api/client/auth/validate-session Tests ================

    @Test
    @DisplayName("Should validate session successfully")
    void testValidateSession_ValidToken_Success() throws Exception {
        // Given
        when(clientAuthService.validateSession(anyString()))
                .thenReturn(sessionResponse);

        // When & Then
        mockMvc.perform(get("/api/client/auth/validate-session")
                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.valid").value(true));

        verify(clientAuthService).validateSession(eq(validToken));
    }

    @Test
    @DisplayName("Should return 401 when validate invalid session")
    void testValidateSession_InvalidToken_ReturnsUnauthorized() throws Exception {
        // Given
        when(clientAuthService.validateSession(anyString()))
                .thenThrow(new RuntimeException("Invalid or expired session"));

        // When & Then
        mockMvc.perform(get("/api/client/auth/validate-session")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        verify(clientAuthService).validateSession(eq("Bearer invalid-token"));
    }

    // ================ POST /api/client/auth/remember-me Tests ================

    @Test
    @DisplayName("Should set remember me successfully")
    void testSetRememberMe_ValidToken_Success() throws Exception {
        // Given
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Remember me enabled successfully");
        response.put("rememberMeToken", "remember-me-token-123");

        when(clientAuthService.enableRememberMe(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/client/auth/remember-me")
                .header("Authorization", validToken)
                .header("X-Forwarded-For", "192.168.1.1")
                .header("User-Agent", "Test Browser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Remember me enabled successfully"));

        verify(clientAuthService).enableRememberMe(eq(validToken), eq("192.168.1.1"), eq("Test Browser"));
    }

    // ================ DELETE /api/client/auth/remember-me Tests ================

    @Test
    @DisplayName("Should remove remember me successfully")
    void testRemoveRememberMe_ValidToken_Success() throws Exception {
        // Given
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Remember me disabled successfully");

        when(clientAuthService.disableRememberMe(anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(delete("/api/client/auth/remember-me")
                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Remember me disabled successfully"));

        verify(clientAuthService).disableRememberMe(eq(validToken));
    }

    // ================ POST /api/client/auth/login-remember-me Tests ================

    @Test
    @DisplayName("Should login with remember me token successfully")
    void testLoginWithRememberMe_ValidToken_Success() throws Exception {
        // Given
        Map<String, String> request = Map.of("rememberMeToken", "valid-remember-me-token");

        when(clientAuthService.loginWithRememberMe(anyString(), anyString(), anyString()))
                .thenReturn(successLoginResponse);

        // When & Then
        mockMvc.perform(post("/api/client/auth/login-remember-me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(request))
                .header("X-Forwarded-For", "192.168.1.1")
                .header("User-Agent", "Test Browser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.accessToken").value("access-token-123"));

        verify(clientAuthService).loginWithRememberMe(eq("valid-remember-me-token"), eq("192.168.1.1"), eq("Test Browser"));
    }

    @Test
    @DisplayName("Should return 401 when login with invalid remember me token")
    void testLoginWithRememberMe_InvalidToken_ReturnsUnauthorized() throws Exception {
        // Given
        Map<String, String> request = Map.of("rememberMeToken", "invalid-remember-me-token");

        when(clientAuthService.loginWithRememberMe(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Invalid or expired remember me token"));

        // When & Then
        mockMvc.perform(post("/api/client/auth/login-remember-me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(request))
                .header("X-Forwarded-For", "192.168.1.1")
                .header("User-Agent", "Test Browser"))
                .andExpect(status().isUnauthorized());

        verify(clientAuthService).loginWithRememberMe(eq("invalid-remember-me-token"), eq("192.168.1.1"), eq("Test Browser"));
    }

    // ================ GET /api/client/auth/sessions Tests ================

    @Test
    @DisplayName("Should get active sessions successfully")
    void testGetActiveSessions_ValidToken_Success() throws Exception {
        // Given
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        List<Map<String, Object>> sessions = Arrays.asList(
                Map.of("sessionId", testSessionId.toString(), "ipAddress", "192.168.1.1", "userAgent", "Chrome"),
                Map.of("sessionId", UUID.randomUUID().toString(), "ipAddress", "192.168.1.2", "userAgent", "Firefox")
        );
        response.put("sessions", sessions);

        when(clientAuthService.getActiveSessions(anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/client/auth/sessions")
                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.sessions").isArray())
                .andExpect(jsonPath("$.sessions[0].sessionId").exists())
                .andExpect(jsonPath("$.sessions[0].ipAddress").value("192.168.1.1"));

        verify(clientAuthService).getActiveSessions(eq(validToken));
    }

    // ================ DELETE /api/client/auth/sessions/{sessionId} Tests ================

    @Test
    @DisplayName("Should terminate session successfully")
    void testTerminateSession_ValidToken_Success() throws Exception {
        // Given
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Session terminated successfully");

        when(clientAuthService.terminateSession(anyString(), any(UUID.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(delete("/api/client/auth/sessions/{sessionId}", testSessionId)
                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Session terminated successfully"));

        verify(clientAuthService).terminateSession(eq(validToken), eq(testSessionId));
    }

    @Test
    @DisplayName("Should return 401 when terminate session with invalid token")
    void testTerminateSession_InvalidToken_ReturnsUnauthorized() throws Exception {
        // Given
        when(clientAuthService.terminateSession(anyString(), any(UUID.class)))
                .thenThrow(new RuntimeException("Invalid or expired token"));

        // When & Then
        mockMvc.perform(delete("/api/client/auth/sessions/{sessionId}", testSessionId)
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        verify(clientAuthService).terminateSession(eq("Bearer invalid-token"), eq(testSessionId));
    }

    // ================ Error Handling Tests ================

    @Test
    @DisplayName("Should handle missing Authorization header gracefully")
    void testAuthEndpoints_MissingAuthorizationHeader_ReturnsBadRequest() throws Exception {
        // When & Then - Test multiple endpoints that require auth
        mockMvc.perform(get("/api/client/auth/profile"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/client/auth/logout"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/client/auth/validate-session"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void testServiceExceptions_ReturnsInternalServerError() throws Exception {
        // Given - Service throws generic exception
        when(clientAuthService.authenticateClient(any(ClientLoginRequest.class)))
                .thenThrow(new RuntimeException("Service temporarily unavailable"));

        // When & Then
        mockMvc.perform(post("/api/client/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should extract IP address correctly from headers")
    void testIPAddressExtraction_FromXForwardedFor_Success() throws Exception {
        // Given
        when(clientAuthService.authenticateClient(any(ClientLoginRequest.class)))
                .thenReturn(successLoginResponse);

        // When & Then
        mockMvc.perform(post("/api/client/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(loginRequest))
                .header("X-Forwarded-For", "203.0.113.0, 192.168.1.1")
                .header("X-Real-IP", "203.0.113.0"))
                .andExpect(status().isOk());

        // Verify that the IP was extracted correctly (first IP from X-Forwarded-For)
        verify(clientAuthService).authenticateClient(argThat(request -> 
            "203.0.113.0".equals(request.getIpAddress())));
    }
} 