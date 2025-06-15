package com.finance.admin.client.service;

import com.finance.admin.client.model.Client;
import com.finance.admin.client.model.ClientSession;
import com.finance.admin.client.repository.ClientSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClientSessionService {

    private final ClientSessionRepository sessionRepository;

    @Value("${app.security.session-timeout-minutes:60}")
    private int sessionTimeoutMinutes;

    @Value("${app.security.remember-me-timeout-days:30}")
    private int rememberMeTimeoutDays;

    @Value("${app.security.max-concurrent-sessions:5}")
    private int maxConcurrentSessions;

    /**
     * Create a new session for client
     */
    public ClientSession createSession(Client client, String sessionToken, boolean rememberMe, 
                                     String ipAddress, String userAgent) {
        
        log.info("Creating session for client: {}, rememberMe: {}", client.getId(), rememberMe);

        // Check and cleanup old sessions if needed
        cleanupOldSessionsForClient(client.getId());

        ClientSession session = ClientSession.builder()
                .clientId(client.getId())
                .sessionToken(sessionToken)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .isActive(true)
                .expiresAt(LocalDateTime.now().plusMinutes(sessionTimeoutMinutes))
                .lastAccessed(LocalDateTime.now())
                .build();

        // Generate device fingerprint
        session.generateDeviceFingerprint();

        // Set remember me token if requested
        if (rememberMe) {
            String rememberMeToken = generateRememberMeToken();
            session.setRememberMeToken(rememberMeToken);
            session.setRememberMeExpiresAt(LocalDateTime.now().plusDays(rememberMeTimeoutDays));
            log.info("Remember me token created for client: {}", client.getId());
        }

        return sessionRepository.save(session);
    }

    /**
     * Find active session by session token
     */
    @Transactional(readOnly = true)
    public Optional<ClientSession> findActiveSession(String sessionToken) {
        Optional<ClientSession> session = sessionRepository.findBySessionTokenAndIsActiveTrue(sessionToken);
        
        if (session.isPresent() && !session.get().isValid()) {
            // Session exists but is expired, invalidate it
            invalidateSession(sessionToken);
            return Optional.empty();
        }
        
        return session;
    }

    /**
     * Find session by remember me token
     */
    @Transactional(readOnly = true)
    public Optional<ClientSession> findByRememberMeToken(String rememberMeToken) {
        Optional<ClientSession> session = sessionRepository.findByRememberMeTokenAndIsActiveTrue(rememberMeToken);
        
        if (session.isPresent() && !session.get().isRememberMeValid()) {
            // Remember me token is expired, invalidate it
            invalidateRememberMeToken(rememberMeToken);
            return Optional.empty();
        }
        
        return session;
    }

    /**
     * Update session last accessed time
     */
    public void updateSessionAccess(String sessionToken) {
        Optional<ClientSession> session = sessionRepository.findBySessionTokenAndIsActiveTrue(sessionToken);
        if (session.isPresent()) {
            session.get().updateLastAccessed();
            sessionRepository.save(session.get());
        }
    }

    /**
     * Invalidate session by token
     */
    public void invalidateSession(String sessionToken) {
        log.info("Invalidating session with token: {}", sessionToken);
        sessionRepository.invalidateSessionByToken(sessionToken);
    }

    /**
     * Invalidate all sessions for a client
     */
    public void invalidateAllSessionsForClient(Long clientId) {
        log.info("Invalidating all sessions for client: {}", clientId);
        sessionRepository.invalidateAllSessionsForClient(clientId);
    }

    /**
     * Invalidate remember me token
     */
    public void invalidateRememberMeToken(String rememberMeToken) {
        log.info("Invalidating remember me token");
        sessionRepository.invalidateSessionByRememberMeToken(rememberMeToken);
    }

    /**
     * Get active sessions for a client
     */
    @Transactional(readOnly = true)
    public List<ClientSession> getActiveSessionsForClient(Long clientId) {
        return sessionRepository.findByClientIdAndIsActiveTrue(clientId);
    }

    /**
     * Get session count for a client
     */
    @Transactional(readOnly = true)
    public long getActiveSessionCount(Long clientId) {
        return sessionRepository.countByClientIdAndIsActiveTrue(clientId);
    }

    /**
     * Check if client has remember me session on this device
     */
    @Transactional(readOnly = true)
    public Optional<ClientSession> findRememberMeSessionForDevice(Long clientId, String deviceFingerprint) {
        List<ClientSession> sessions = sessionRepository.findByClientIdAndDeviceFingerprintAndIsActiveTrue(
            clientId, deviceFingerprint);
        
        return sessions.stream()
                .filter(session -> session.isRememberMeValid())
                .findFirst();
    }

    /**
     * Extend session expiry (for remember me functionality)
     */
    public void extendSession(String sessionToken) {
        Optional<ClientSession> session = sessionRepository.findBySessionTokenAndIsActiveTrue(sessionToken);
        if (session.isPresent()) {
            session.get().setExpiresAt(LocalDateTime.now().plusMinutes(sessionTimeoutMinutes));
            sessionRepository.save(session.get());
            log.info("Session extended for token: {}", sessionToken);
        }
    }

    /**
     * Cleanup old sessions for a client (keep only most recent ones)
     */
    private void cleanupOldSessionsForClient(Long clientId) {
        List<ClientSession> activeSessions = sessionRepository.findByClientIdAndIsActiveTrue(clientId);
        
        if (activeSessions.size() >= maxConcurrentSessions) {
            // Sort by last accessed time and keep only the most recent ones
            activeSessions.sort((s1, s2) -> s2.getLastAccessed().compareTo(s1.getLastAccessed()));
            
            // Invalidate older sessions
            for (int i = maxConcurrentSessions - 1; i < activeSessions.size(); i++) {
                activeSessions.get(i).invalidate();
                sessionRepository.save(activeSessions.get(i));
                log.info("Invalidated old session for client: {}", clientId);
            }
        }
    }

    /**
     * Generate secure remember me token
     */
    private String generateRememberMeToken() {
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }

    /**
     * Scheduled task to cleanup expired sessions
     */
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        
        int expiredSessions = sessionRepository.cleanupExpiredSessions(now);
        int expiredRememberMeTokens = sessionRepository.cleanupExpiredRememberMeTokens(now);
        
        if (expiredSessions > 0 || expiredRememberMeTokens > 0) {
            log.info("Cleaned up {} expired sessions and {} expired remember me tokens", 
                    expiredSessions, expiredRememberMeTokens);
        }
    }

    /**
     * Get recent sessions for security monitoring
     */
    @Transactional(readOnly = true)
    public List<ClientSession> getRecentSessionsForClient(Long clientId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return sessionRepository.findRecentSessionsForClient(clientId, since);
    }
} 