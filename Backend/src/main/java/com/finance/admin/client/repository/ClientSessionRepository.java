package com.finance.admin.client.repository;

import com.finance.admin.client.model.ClientSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientSessionRepository extends JpaRepository<ClientSession, UUID> {

    /**
     * Find active session by session token
     */
    Optional<ClientSession> findBySessionTokenAndIsActiveTrue(String sessionToken);

    /**
     * Find session by remember me token
     */
    Optional<ClientSession> findByRememberMeTokenAndIsActiveTrue(String rememberMeToken);

    /**
     * Find all active sessions for a client
     */
    List<ClientSession> findByClientIdAndIsActiveTrue(Long clientId);

    /**
     * Find sessions by client ID and device fingerprint
     */
    List<ClientSession> findByClientIdAndDeviceFingerprintAndIsActiveTrue(
        Long clientId, String deviceFingerprint);

    /**
     * Find expired sessions
     */
    @Query("SELECT cs FROM ClientSession cs WHERE cs.expiresAt < :now AND cs.isActive = true")
    List<ClientSession> findExpiredSessions(@Param("now") LocalDateTime now);

    /**
     * Find expired remember me tokens
     */
    @Query("SELECT cs FROM ClientSession cs WHERE cs.rememberMeExpiresAt < :now AND cs.isActive = true AND cs.rememberMeToken IS NOT NULL")
    List<ClientSession> findExpiredRememberMeTokens(@Param("now") LocalDateTime now);

    /**
     * Invalidate all sessions for a client
     */
    @Modifying
    @Query("UPDATE ClientSession cs SET cs.isActive = false WHERE cs.clientId = :clientId")
    void invalidateAllSessionsForClient(@Param("clientId") Long clientId);

    /**
     * Invalidate session by token
     */
    @Modifying
    @Query("UPDATE ClientSession cs SET cs.isActive = false WHERE cs.sessionToken = :sessionToken")
    void invalidateSessionByToken(@Param("sessionToken") String sessionToken);

    /**
     * Invalidate session by remember me token
     */
    @Modifying
    @Query("UPDATE ClientSession cs SET cs.isActive = false WHERE cs.rememberMeToken = :rememberMeToken")
    void invalidateSessionByRememberMeToken(@Param("rememberMeToken") String rememberMeToken);

    /**
     * Cleanup expired sessions
     */
    @Modifying
    @Query("UPDATE ClientSession cs SET cs.isActive = false WHERE cs.expiresAt < :now")
    int cleanupExpiredSessions(@Param("now") LocalDateTime now);

    /**
     * Cleanup expired remember me tokens
     */
    @Modifying
    @Query("UPDATE ClientSession cs SET cs.isActive = false WHERE cs.rememberMeExpiresAt < :now AND cs.rememberMeToken IS NOT NULL")
    int cleanupExpiredRememberMeTokens(@Param("now") LocalDateTime now);

    /**
     * Count active sessions for a client
     */
    long countByClientIdAndIsActiveTrue(Long clientId);

    /**
     * Find sessions by IP address for security monitoring
     */
    List<ClientSession> findByIpAddressAndIsActiveTrue(String ipAddress);

    /**
     * Find recent sessions for a client (for security monitoring)
     */
    @Query("SELECT cs FROM ClientSession cs WHERE cs.clientId = :clientId AND cs.createdAt > :since ORDER BY cs.createdAt DESC")
    List<ClientSession> findRecentSessionsForClient(@Param("clientId") Long clientId, @Param("since") LocalDateTime since);
} 