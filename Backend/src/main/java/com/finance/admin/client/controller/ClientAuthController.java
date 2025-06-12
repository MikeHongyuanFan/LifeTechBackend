package com.finance.admin.client.controller;

import com.finance.admin.client.dto.ClientLoginRequest;
import com.finance.admin.client.service.ClientAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/client/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Client Authentication", description = "APIs for client portal authentication")
public class ClientAuthController {

    private final ClientAuthService clientAuthService;

    @Operation(summary = "Client login", description = "Authenticate client for portal access")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "423", description = "Account locked"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody ClientLoginRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Client login attempt for: {}", request.getEmailOrPhone());
        
        // Extract client IP and user agent
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        // Set device information
        request.setIpAddress(ipAddress);
        request.setUserAgent(userAgent);
        
        Map<String, Object> response = clientAuthService.authenticateClient(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Client logout", description = "Logout client and invalidate session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @Parameter(description = "Authorization token") @RequestHeader("Authorization") String token,
            HttpServletRequest httpRequest) {
        
        log.info("Client logout request");
        
        String ipAddress = getClientIpAddress(httpRequest);
        Map<String, Object> response = clientAuthService.logoutClient(token, ipAddress);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Forgot password", description = "Request password reset for client")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset email sent"),
        @ApiResponse(responseCode = "404", description = "Client not found"),
        @ApiResponse(responseCode = "429", description = "Too many requests")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        String email = request.get("email");
        log.info("Password reset request for email: {}", email);
        
        String ipAddress = getClientIpAddress(httpRequest);
        Map<String, Object> response = clientAuthService.initiatePasswordReset(email, ipAddress);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reset password", description = "Reset client password using reset token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successful"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired reset token"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        String resetToken = request.get("resetToken");
        String newPassword = request.get("newPassword");
        
        log.info("Password reset confirmation with token: {}", resetToken);
        
        String ipAddress = getClientIpAddress(httpRequest);
        Map<String, Object> response = clientAuthService.resetPassword(resetToken, newPassword, ipAddress);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get client profile", description = "Get authenticated client profile information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(
            @Parameter(description = "Authorization token") @RequestHeader("Authorization") String token) {
        
        log.info("Client profile request");
        
        Map<String, Object> response = clientAuthService.getClientProfile(token);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh token", description = "Refresh authentication token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, Object>> refreshToken(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        String refreshToken = request.get("refreshToken");
        log.info("Token refresh request");
        
        String ipAddress = getClientIpAddress(httpRequest);
        Map<String, Object> response = clientAuthService.refreshToken(refreshToken, ipAddress);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Validate session", description = "Validate current client session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Session is valid"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired session")
    })
    @GetMapping("/validate-session")
    public ResponseEntity<Map<String, Object>> validateSession(
            @Parameter(description = "Authorization token") @RequestHeader("Authorization") String token) {
        
        log.info("Session validation request");
        
        Map<String, Object> response = clientAuthService.validateSession(token);
        return ResponseEntity.ok(response);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
} 