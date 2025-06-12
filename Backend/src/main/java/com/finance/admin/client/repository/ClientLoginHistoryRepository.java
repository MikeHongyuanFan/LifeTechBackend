package com.finance.admin.client.repository;

import com.finance.admin.client.model.ClientLoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientLoginHistoryRepository extends JpaRepository<ClientLoginHistory, Long> {

    // Find login history by client
    Page<ClientLoginHistory> findByClientIdOrderByLoginTimestampDesc(Long clientId, Pageable pageable);
    List<ClientLoginHistory> findByClientIdOrderByLoginTimestampDesc(Long clientId);

    // Find active sessions
    List<ClientLoginHistory> findByClientIdAndLogoutTimestampIsNull(Long clientId);
    
    @Query("SELECT clh FROM ClientLoginHistory clh WHERE clh.logoutTimestamp IS NULL AND clh.loginSuccessful = true")
    List<ClientLoginHistory> findAllActiveSessions();

    // Find last successful login
    @Query("SELECT clh FROM ClientLoginHistory clh WHERE clh.clientId = :clientId AND clh.loginSuccessful = true ORDER BY clh.loginTimestamp DESC")
    Page<ClientLoginHistory> findLastSuccessfulLogin(@Param("clientId") Long clientId, Pageable pageable);

    // Find failed login attempts
    List<ClientLoginHistory> findByClientIdAndLoginSuccessfulFalseOrderByLoginTimestampDesc(Long clientId);
    
    @Query("SELECT clh FROM ClientLoginHistory clh WHERE clh.clientId = :clientId AND clh.loginSuccessful = false AND clh.loginTimestamp >= :since")
    List<ClientLoginHistory> findFailedLoginAttemptsSince(@Param("clientId") Long clientId, @Param("since") LocalDateTime since);

    // Statistics queries
    @Query("SELECT COUNT(clh) FROM ClientLoginHistory clh WHERE clh.clientId = :clientId AND clh.loginTimestamp >= :startDate")
    long countLoginsSince(@Param("clientId") Long clientId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(clh) FROM ClientLoginHistory clh WHERE clh.loginSuccessful = true AND clh.loginTimestamp >= :startDate")
    long countSuccessfulLoginsSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(clh) FROM ClientLoginHistory clh WHERE clh.loginSuccessful = false AND clh.loginTimestamp >= :startDate")
    long countFailedLoginsSince(@Param("startDate") LocalDateTime startDate);

    // Activity monitoring
    @Query("SELECT DISTINCT clh.clientId FROM ClientLoginHistory clh WHERE clh.loginTimestamp >= :since AND clh.loginSuccessful = true")
    List<Long> findActiveClientIdsSince(@Param("since") LocalDateTime since);

    // Session duration analysis
    @Query(value = "SELECT AVG(CAST(session_duration AS DOUBLE PRECISION)) FROM client_login_history WHERE client_id = :clientId AND session_duration IS NOT NULL", nativeQuery = true)
    Double getAverageSessionDuration(@Param("clientId") Long clientId);

    // IP address tracking
    List<ClientLoginHistory> findByIpAddressAndLoginTimestampBetween(String ipAddress, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT DISTINCT clh.ipAddress FROM ClientLoginHistory clh WHERE clh.clientId = :clientId")
    List<String> findDistinctIpAddressesByClientId(@Param("clientId") Long clientId);

    // Cleanup old records
    void deleteByLoginTimestampBefore(LocalDateTime cutoffDate);
} 