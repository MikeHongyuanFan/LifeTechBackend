package com.finance.admin.auth.controller;

import com.finance.admin.auth.dto.LoginRequest;
import com.finance.admin.auth.dto.LoginResponse;
import com.finance.admin.auth.dto.MfaVerificationRequest;
import com.finance.admin.auth.service.AuthenticationService;
import com.finance.admin.common.dto.ApiResponse;
import com.finance.admin.common.exception.AuthenticationException;
import com.finance.admin.common.exception.AccountLockedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Admin user authentication operations")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    @Operation(summary = "Admin user login", description = "Authenticate admin user with username/email and password")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        
        String clientIp = request.getRemoteAddr();
        logger.info("Login attempt from IP: {}", clientIp);
        logger.info("X-Forwarded-For: {}", request.getHeader("X-Forwarded-For"));
        logger.info("Remote Address: {}", request.getRemoteAddr());
        
        try {
            LoginResponse loginResponse = authenticationService.authenticateUser(loginRequest, request);
            
            if (loginResponse.isRequiresMfa()) {
                logger.info("MFA required for user: {}", loginRequest.getUsername());
                return ResponseEntity.ok(ApiResponse.success("MFA verification required", loginResponse));
            } else {
                logger.info("Login successful for user: {}", loginRequest.getUsername());
                return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
            }
            
        } catch (AuthenticationException ex) {
            logger.error("Authentication failed for user: {}, Error: {}", loginRequest.getUsername(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ex.getMessage()));
        } catch (AccountLockedException ex) {
            logger.error("Account locked for user: {}, Error: {}", loginRequest.getUsername(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(ApiResponse.error(ex.getMessage()));
        } catch (Exception ex) {
            logger.error("Unexpected error during login for user: {}, Error: {}", loginRequest.getUsername(), ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }

    @PostMapping("/mfa/verify")
    @Operation(summary = "Verify MFA code", description = "Complete authentication by verifying MFA code")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyMfa(
            @Valid @RequestBody MfaVerificationRequest mfaRequest,
            HttpServletRequest request) {
        
        logger.info("MFA verification attempt");
        
        try {
            LoginResponse loginResponse = authenticationService.verifyMfa(mfaRequest, request);
            logger.info("MFA verification successful");
            return ResponseEntity.ok(ApiResponse.success("MFA verification successful", loginResponse));
            
        } catch (AuthenticationException ex) {
            logger.error("MFA verification failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ex.getMessage()));
        } catch (Exception ex) {
            logger.error("Unexpected error during MFA verification: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Admin user logout", description = "Logout admin user and invalidate token")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestHeader("Authorization") String authHeader) {
        
        logger.info("Logout attempt");
        
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                authenticationService.logout(token);
            }
            
            logger.info("Logout successful");
            return ResponseEntity.ok(ApiResponse.success("Logout successful"));
            
        } catch (Exception ex) {
            logger.error("Logout failed: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Authentication service health check")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Authentication service is healthy"));
    }
} 
