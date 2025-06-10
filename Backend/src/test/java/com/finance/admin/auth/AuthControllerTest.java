package com.finance.admin.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.admin.auth.controller.AuthController;
import com.finance.admin.auth.dto.LoginRequest;
import com.finance.admin.auth.dto.LoginResponse;
import com.finance.admin.auth.dto.MfaVerificationRequest;
import com.finance.admin.auth.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the Authentication API endpoints
 * Using completely minimal configuration to avoid all context loading issues
 */
@SpringJUnitConfig(AuthControllerTest.TestConfig.class)
public class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private AuthenticationService authenticationService;
    private AuthController authController;

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    private LoginResponse mfaLoginResponse;
    private MfaVerificationRequest mfaVerificationRequest;

    @Configuration
    public static class TestConfig {
        
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @BeforeEach
    void setUp() {
        // Create mocks
        authenticationService = mock(AuthenticationService.class);
        objectMapper = new ObjectMapper();
        authController = new AuthController();
        
        // Use reflection to inject the mock service into the controller
        try {
            java.lang.reflect.Field serviceField = AuthController.class.getDeclaredField("authenticationService");
            serviceField.setAccessible(true);
            serviceField.set(authController, authenticationService);
        } catch (Exception e) {
            // If reflection fails, we'll handle it in individual tests
        }
        
        // Setup MockMvc with just the controller
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        
        // Setup test data
        loginRequest = new LoginRequest("testadmin", "password123");
        
        // Setup standard login response
        loginResponse = new LoginResponse();
        loginResponse.setSuccess(true);
        loginResponse.setRequiresMfa(false);
        loginResponse.setAccessToken("test-access-token");
        loginResponse.setRefreshToken("test-refresh-token");
        
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(UUID.randomUUID());
        userInfo.setUsername("testadmin");
        userInfo.setEmail("test@finance.com");
        userInfo.setRoles(new ArrayList<>(Arrays.asList("ADMIN")));
        loginResponse.setUser(userInfo);
        
        // Setup MFA login response
        mfaLoginResponse = new LoginResponse();
        mfaLoginResponse.setSuccess(true);
        mfaLoginResponse.setRequiresMfa(true);
        mfaLoginResponse.setMfaToken("test-mfa-token");
        
        // Setup MFA verification request
        mfaVerificationRequest = new MfaVerificationRequest("test-mfa-token", "123456");
    }

    @Test
    void testSuccessfulLogin() throws Exception {
        when(authenticationService.authenticateUser(any(LoginRequest.class), any(MockHttpServletRequest.class)))
                .thenReturn(loginResponse);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("test-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("test-refresh-token"))
                .andExpect(jsonPath("$.data.user.username").value("testadmin"));
    }

    @Test
    void testMfaRequiredLogin() throws Exception {
        when(authenticationService.authenticateUser(any(LoginRequest.class), any(MockHttpServletRequest.class)))
                .thenReturn(mfaLoginResponse);

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
    void testFailedLogin() throws Exception {
        when(authenticationService.authenticateUser(any(LoginRequest.class), any(MockHttpServletRequest.class)))
                .thenThrow(new RuntimeException("Invalid username or password"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    @Test
    void testMfaVerification() throws Exception {
        when(authenticationService.verifyMfa(any(MfaVerificationRequest.class), any(MockHttpServletRequest.class)))
                .thenReturn(loginResponse);

        mockMvc.perform(post("/auth/verify-mfa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mfaVerificationRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("test-access-token"))
                .andExpect(jsonPath("$.data.user.username").value("testadmin"));
    }

    @Test
    void testFailedMfaVerification() throws Exception {
        when(authenticationService.verifyMfa(any(MfaVerificationRequest.class), any(MockHttpServletRequest.class)))
                .thenThrow(new RuntimeException("Invalid MFA code"));

        mockMvc.perform(post("/auth/verify-mfa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mfaVerificationRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Invalid MFA code"));
    }

    @Test
    void testLogout() throws Exception {
        doNothing().when(authenticationService).logout(anyString());

        mockMvc.perform(post("/auth/logout")
                .header("Authorization", "Bearer test-access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    @Test
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Authentication service is healthy"));
    }

    @Test
    void testInvalidLoginRequest() throws Exception {
        LoginRequest invalidRequest = new LoginRequest("", "");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isBadRequest());
    }
}
