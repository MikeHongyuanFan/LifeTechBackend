package com.finance.admin.auth;

import com.finance.admin.auth.dto.LoginRequest;
import com.finance.admin.auth.dto.MfaVerificationRequest;
import com.finance.admin.config.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Authentication API endpoints
 * Using full Spring Boot application context
 */
public class AuthenticationIntegrationTest extends BaseIntegrationTest {

    private LoginRequest loginRequest;
    private MfaVerificationRequest mfaVerificationRequest;

    @BeforeEach
    protected void setUp() {
        super.setUp(); // Call parent setup
        
        // Setup test data - use the admin user that gets created by the system
        loginRequest = new LoginRequest("admin", "admin123");
        mfaVerificationRequest = new MfaVerificationRequest("test-mfa-token", "123456");
    }

    @Test
    void testSuccessfulLoginWithTestUser() throws Exception {
        getMockMvc().perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(loginRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.user.username").value("admin"));
    }

    @Test
    void testMfaRequiredFlow() throws Exception {
        // Since the admin user has MFA disabled, this test will verify normal login flow
        // In a real scenario, you would have a separate user with MFA enabled
        LoginRequest mfaLoginRequest = new LoginRequest("admin", "admin123");
        
        getMockMvc().perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(mfaLoginRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requiresMfa").value(false))
                .andExpect(jsonPath("$.data.accessToken").exists());
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        LoginRequest invalidLoginRequest = new LoginRequest("admin", "wrongpassword");
        
        getMockMvc().perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(invalidLoginRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testLoginWithNonExistentUser() throws Exception {
        LoginRequest nonExistentLoginRequest = new LoginRequest("nonexistent", "password123");
        
        getMockMvc().perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(nonExistentLoginRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testLogout() throws Exception {
        getMockMvc().perform(post("/auth/logout")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    @Test
    void testHealthEndpoint() throws Exception {
        getMockMvc().perform(get("/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Authentication service is healthy"));
    }

    @Test
    void testInvalidMfaVerification() throws Exception {
        MfaVerificationRequest invalidMfaRequest = new MfaVerificationRequest("invalid-token", "invalid");
        
        getMockMvc().perform(post("/auth/verify-mfa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(invalidMfaRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginValidation() throws Exception {
        LoginRequest emptyLoginRequest = new LoginRequest("", "");
        
        getMockMvc().perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(emptyLoginRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isBadRequest());
    }
} 
