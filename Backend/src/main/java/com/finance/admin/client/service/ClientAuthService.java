package com.finance.admin.client.service;

import com.finance.admin.client.dto.ClientLoginRequest;
import com.finance.admin.client.model.Client;
import com.finance.admin.client.model.ClientLoginHistory;
import com.finance.admin.client.model.ClientSession;
import com.finance.admin.client.repository.ClientRepository;
import com.finance.admin.client.repository.ClientLoginHistoryRepository;
import com.finance.admin.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClientAuthService {

    private final ClientRepository clientRepository;
    private final ClientLoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final BlockchainService blockchainService;
    private final ClientSessionService sessionService;

    @Value("${app.security.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.security.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;

    /**
     * Authenticate client for portal access
     */
    public Map<String, Object> authenticateClient(ClientLoginRequest request) {
        log.info("Authenticating client: {}", request.getEmailOrPhone());

        // Find client by email or phone
        Client client = findClientByEmailOrPhone(request.getEmailOrPhone());

        // Check if account is locked
        if (isAccountLocked(client)) {
            recordFailedLogin(client, request, "Account locked due to too many failed attempts");
            throw new RuntimeException("Account is temporarily locked. Please try again later.");
        }

        // Verify password (simulated since we don't have password field in client entity yet)
        boolean passwordValid = verifyPassword(client, request.getPassword());

        if (!passwordValid) {
            recordFailedLogin(client, request, "Invalid password");
            
            // Check if should lock account
            checkAndLockAccount(client);
            
            throw new RuntimeException("Invalid credentials");
        }

        // Check if client is active
        if (!client.isActive()) {
            recordFailedLogin(client, request, "Account is not active");
            throw new RuntimeException("Account is not active");
        }

        // Record successful login
        ClientLoginHistory loginHistory = recordSuccessfulLogin(client, request);

        // Generate JWT tokens
        String accessToken = jwtTokenService.generateAccessToken(client);
        String refreshToken = jwtTokenService.generateRefreshToken(client);

        // Create session with remember me if requested
        ClientSession session = sessionService.createSession(client, accessToken, request.isRememberMe(), 
            request.getIpAddress(), request.getUserAgent());

        // Create blockchain audit log
        try {
            blockchainService.createAuditLog("CLIENT_LOGIN", client.getId(), 
                "Client login from IP: " + request.getIpAddress());
        } catch (Exception e) {
            log.warn("Failed to create blockchain audit log for client login: {}", e.getMessage());
        }

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Login successful");
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", jwtTokenService.getAccessTokenExpirationSeconds());
        response.put("client", createClientSummary(client));
        response.put("loginHistory", createLoginHistorySummary(loginHistory));
        
        // Include remember me token if enabled
        if (request.isRememberMe() && session.getRememberMeToken() != null) {
            response.put("rememberMeToken", session.getRememberMeToken());
            response.put("rememberMeExpiresAt", session.getRememberMeExpiresAt());
        }

        log.info("Client {} authenticated successfully", client.getId());
        return response;
    }

    /**
     * Logout client and invalidate session
     */
    public Map<String, Object> logoutClient(String token, String ipAddress) {
        try {
            // Extract client ID from token
            Long clientId = jwtTokenService.getClientIdFromToken(token);
            
            // Find active login sessions and close them
            List<ClientLoginHistory> activeSessions = loginHistoryRepository
                .findByClientIdAndLogoutTimestampIsNull(clientId);
            
            activeSessions.forEach(session -> {
                session.logout();
                loginHistoryRepository.save(session);
            });

            // Invalidate token
            jwtTokenService.invalidateToken(token);

            // Create blockchain audit log
            try {
                blockchainService.createAuditLog("CLIENT_LOGOUT", clientId, 
                    "Client logout from IP: " + ipAddress);
            } catch (Exception e) {
                log.warn("Failed to create blockchain audit log for client logout: {}", e.getMessage());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Logout successful");
            response.put("timestamp", LocalDateTime.now());

            log.info("Client {} logged out successfully", clientId);
            return response;

        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true); // Return success even if token is invalid
            response.put("message", "Logout completed");
            return response;
        }
    }

    /**
     * Initiate password reset process
     */
    public Map<String, Object> initiatePasswordReset(String email, String ipAddress) {
        log.info("Password reset request for email: {}", email);

        try {
            Client client = clientRepository.findByEmailPrimary(email)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

            // Generate reset token
            String resetToken = jwtTokenService.generatePasswordResetToken(client);

            // In a real implementation, you would send email here
            // For now, we'll just log it
            log.info("Password reset token generated for client {}: {}", client.getId(), resetToken);

            // Create blockchain audit log
            try {
                blockchainService.createAuditLog("PASSWORD_RESET_REQUEST", client.getId(), 
                    "Password reset requested from IP: " + ipAddress);
            } catch (Exception e) {
                log.warn("Failed to create blockchain audit log: {}", e.getMessage());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password reset instructions sent to your email");
            response.put("resetToken", resetToken); // In production, this would be sent via email
            
            return response;

        } catch (ResourceNotFoundException e) {
            // Don't reveal if email exists or not for security
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "If the email exists, password reset instructions have been sent");
            return response;
        }
    }

    /**
     * Reset password using reset token
     */
    public Map<String, Object> resetPassword(String resetToken, String newPassword, String ipAddress) {
        log.info("Password reset confirmation");

        try {
            // Validate reset token
            Long clientId = jwtTokenService.validatePasswordResetToken(resetToken);
            
            Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

            // Hash and save new password (in a real implementation)
            // For now, we'll just log it
            String hashedPassword = passwordEncoder.encode(newPassword);
            log.info("New password would be saved for client {}", clientId);

            // Invalidate all existing sessions
            List<ClientLoginHistory> activeSessions = loginHistoryRepository
                .findByClientIdAndLogoutTimestampIsNull(clientId);
            activeSessions.forEach(session -> {
                session.logout();
                loginHistoryRepository.save(session);
            });

            // Create blockchain audit log
            try {
                blockchainService.createAuditLog("PASSWORD_RESET", clientId, 
                    "Password reset completed from IP: " + ipAddress);
            } catch (Exception e) {
                log.warn("Failed to create blockchain audit log: {}", e.getMessage());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password reset successful");
            response.put("timestamp", LocalDateTime.now());

            return response;

        } catch (Exception e) {
            log.error("Password reset failed: {}", e.getMessage());
            throw new RuntimeException("Invalid or expired reset token");
        }
    }

    /**
     * Get client profile information
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getClientProfile(String token) {
        try {
            Long clientId = jwtTokenService.getClientIdFromToken(token);
            
            Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("client", createClientSummary(client));
            
            return response;

        } catch (Exception e) {
            log.error("Failed to get client profile: {}", e.getMessage());
            throw new RuntimeException("Invalid or expired token");
        }
    }

    /**
     * Refresh authentication token
     */
    public Map<String, Object> refreshToken(String refreshToken, String ipAddress) {
        try {
            Long clientId = jwtTokenService.validateRefreshToken(refreshToken);
            
            Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

            // Generate new tokens
            String newAccessToken = jwtTokenService.generateAccessToken(client);
            String newRefreshToken = jwtTokenService.generateRefreshToken(client);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accessToken", newAccessToken);
            response.put("refreshToken", newRefreshToken);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", jwtTokenService.getAccessTokenExpirationSeconds());

            return response;

        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new RuntimeException("Invalid or expired refresh token");
        }
    }

    /**
     * Validate current session
     */
    @Transactional(readOnly = true)
    public Map<String, Object> validateSession(String token) {
        try {
            Long clientId = jwtTokenService.getClientIdFromToken(token);
            boolean isValid = jwtTokenService.validateToken(token);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("clientId", clientId);
            response.put("timestamp", LocalDateTime.now());

            return response;

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("error", "Invalid token");
            return response;
        }
    }

    // Private helper methods

    private Client findClientByEmailOrPhone(String emailOrPhone) {
        // Try to find by email first
        Optional<Client> clientOpt = clientRepository.findByEmailPrimary(emailOrPhone);
        
        if (clientOpt.isEmpty()) {
            // Try to find by secondary email
            clientOpt = clientRepository.findByEmailPrimaryOrEmailSecondary(emailOrPhone, emailOrPhone);
        }

        return clientOpt.orElseThrow(() -> new ResourceNotFoundException("Client not found"));
    }

    private boolean verifyPassword(Client client, String password) {
        // In a real implementation, you would compare against stored hashed password
        // For now, we'll simulate password verification
        return true; // Simulate successful password verification
    }

    private boolean isAccountLocked(Client client) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(lockoutDurationMinutes);
        
        List<ClientLoginHistory> recentFailures = loginHistoryRepository
            .findFailedLoginAttemptsSince(client.getId(), cutoffTime);

        return recentFailures.size() >= maxFailedAttempts;
    }

    private void checkAndLockAccount(Client client) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(lockoutDurationMinutes);
        
        List<ClientLoginHistory> recentFailures = loginHistoryRepository
            .findFailedLoginAttemptsSince(client.getId(), cutoffTime);

        if (recentFailures.size() >= maxFailedAttempts - 1) {
            log.warn("Account for client {} will be locked after this failed attempt", client.getId());
        }
    }

    private ClientLoginHistory recordSuccessfulLogin(Client client, ClientLoginRequest request) {
        ClientLoginHistory loginHistory = ClientLoginHistory.builder()
            .clientId(client.getId())
            .ipAddress(request.getIpAddress())
            .userAgent(request.getUserAgent())
            .loginSuccessful(true)
            .build();

        return loginHistoryRepository.save(loginHistory);
    }

    private void recordFailedLogin(Client client, ClientLoginRequest request, String reason) {
        ClientLoginHistory loginHistory = ClientLoginHistory.builder()
            .clientId(client.getId())
            .ipAddress(request.getIpAddress())
            .userAgent(request.getUserAgent())
            .loginSuccessful(false)
            .failureReason(reason)
            .build();

        loginHistoryRepository.save(loginHistory);
    }

    private Map<String, Object> createClientSummary(Client client) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", client.getId());
        summary.put("membershipNumber", client.getMembershipNumber());
        summary.put("fullName", client.getFullName());
        summary.put("emailPrimary", client.getEmailPrimary());
        summary.put("status", client.getStatus());
        summary.put("hasBlockchainIdentity", client.getBlockchainIdentityHash() != null);
        return summary;
    }

    private Map<String, Object> createLoginHistorySummary(ClientLoginHistory loginHistory) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("loginTime", loginHistory.getLoginTimestamp());
        summary.put("ipAddress", loginHistory.getIpAddress());
        summary.put("userAgent", loginHistory.getUserAgent());
        return summary;
    }

    /**
     * Enable remember me for current session
     */
    public Map<String, Object> enableRememberMe(String token, String ipAddress, String userAgent) {
        try {
            Long clientId = jwtTokenService.getClientIdFromToken(token);
            
            Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

            // Create or update session with remember me
            sessionService.createSession(client, token, true, ipAddress, userAgent);

            // Create blockchain audit log
            try {
                blockchainService.createAuditLog("REMEMBER_ME_ENABLED", clientId, 
                    "Remember me enabled from IP: " + ipAddress);
            } catch (Exception e) {
                log.warn("Failed to create blockchain audit log: {}", e.getMessage());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Remember me enabled successfully");
            response.put("timestamp", LocalDateTime.now());

            return response;

        } catch (Exception e) {
            log.error("Failed to enable remember me: {}", e.getMessage());
            throw new RuntimeException("Invalid or expired token");
        }
    }

    /**
     * Disable remember me for current session
     */
    public Map<String, Object> disableRememberMe(String token) {
        try {
            Long clientId = jwtTokenService.getClientIdFromToken(token);
            
            // Find and remove remember me token for this session
            sessionService.findActiveSession(token).ifPresent(session -> {
                if (session.getRememberMeToken() != null) {
                    sessionService.invalidateRememberMeToken(session.getRememberMeToken());
                }
            });

            // Create blockchain audit log
            try {
                blockchainService.createAuditLog("REMEMBER_ME_DISABLED", clientId, 
                    "Remember me disabled");
            } catch (Exception e) {
                log.warn("Failed to create blockchain audit log: {}", e.getMessage());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Remember me disabled successfully");

            return response;

        } catch (Exception e) {
            log.error("Failed to disable remember me: {}", e.getMessage());
            throw new RuntimeException("Invalid or expired token");
        }
    }

    /**
     * Login using remember me token
     */
    public Map<String, Object> loginWithRememberMe(String rememberMeToken, String ipAddress, String userAgent) {
        try {
            // Find session by remember me token
            ClientSession session = sessionService.findByRememberMeToken(rememberMeToken)
                .orElseThrow(() -> new RuntimeException("Invalid or expired remember me token"));

            Client client = clientRepository.findById(session.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

            // Check if client is still active
            if (!client.isActive()) {
                throw new RuntimeException("Account is not active");
            }

            // Generate new session tokens
            String accessToken = jwtTokenService.generateAccessToken(client);
            String refreshToken = jwtTokenService.generateRefreshToken(client);

            // Create new session (this will also extend remember me)
            ClientSession newSession = sessionService.createSession(client, accessToken, true, ipAddress, userAgent);

            // Record successful login
            ClientLoginHistory loginHistory = ClientLoginHistory.builder()
                .clientId(client.getId())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .loginSuccessful(true)
                .build();
            loginHistoryRepository.save(loginHistory);

            // Create blockchain audit log
            try {
                blockchainService.createAuditLog("REMEMBER_ME_LOGIN", client.getId(), 
                    "Remember me login from IP: " + ipAddress);
            } catch (Exception e) {
                log.warn("Failed to create blockchain audit log: {}", e.getMessage());
            }

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful via remember me");
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("rememberMeToken", newSession.getRememberMeToken());
            response.put("tokenType", "Bearer");
            response.put("expiresIn", jwtTokenService.getAccessTokenExpirationSeconds());
            response.put("client", createClientSummary(client));

            log.info("Client {} authenticated successfully via remember me", client.getId());
            return response;

        } catch (Exception e) {
            log.error("Remember me login failed: {}", e.getMessage());
            throw new RuntimeException("Invalid or expired remember me token");
        }
    }

    /**
     * Get active sessions for client
     */
    public Map<String, Object> getActiveSessions(String token) {
        try {
            Long clientId = jwtTokenService.getClientIdFromToken(token);
            
            List<ClientSession> activeSessions = sessionService.getActiveSessionsForClient(clientId);
            
            List<Map<String, Object>> sessionList = activeSessions.stream()
                .map(this::createSessionSummary)
                .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sessions", sessionList);
            response.put("totalSessions", sessionList.size());

            return response;

        } catch (Exception e) {
            log.error("Failed to get active sessions: {}", e.getMessage());
            throw new RuntimeException("Invalid or expired token");
        }
    }

    /**
     * Terminate a specific session
     */
    public Map<String, Object> terminateSession(String token, UUID sessionId) {
        try {
            Long clientId = jwtTokenService.getClientIdFromToken(token);
            
            // Verify the session belongs to the client
            List<ClientSession> activeSessions = sessionService.getActiveSessionsForClient(clientId);
            ClientSession targetSession = activeSessions.stream()
                .filter(session -> session.getId().equals(sessionId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Session not found"));

            // Invalidate the session
            sessionService.invalidateSession(targetSession.getSessionToken());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Session terminated successfully");

            return response;

        } catch (Exception e) {
            log.error("Failed to terminate session: {}", e.getMessage());
            throw new RuntimeException("Failed to terminate session");
        }
    }

    private Map<String, Object> createSessionSummary(ClientSession session) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", session.getId());
        summary.put("deviceFingerprint", session.getDeviceFingerprint());
        summary.put("ipAddress", session.getIpAddress());
        summary.put("userAgent", session.getUserAgent());
        summary.put("createdAt", session.getCreatedAt());
        summary.put("lastAccessed", session.getLastAccessed());
        summary.put("expiresAt", session.getExpiresAt());
        summary.put("hasRememberMe", session.getRememberMeToken() != null);
        return summary;
    }
} 