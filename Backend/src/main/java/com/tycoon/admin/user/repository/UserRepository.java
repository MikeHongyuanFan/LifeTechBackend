package com.tycoon.admin.user.repository;

import com.tycoon.admin.user.entity.User;
import com.tycoon.admin.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    // Basic finders
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Status-based queries
    Page<User> findByStatus(UserStatus status, Pageable pageable);
    List<User> findByStatusIn(List<UserStatus> statuses);
    long countByStatus(UserStatus status);

    // Analytics queries
    @Query("SELECT u FROM User u WHERE u.lastLoginAt BETWEEN :startDate AND :endDate")
    List<User> findUsersWithLoginBetween(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT u FROM User u WHERE u.lastLoginAt IS NULL OR u.lastLoginAt < :cutoffDate")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :maxAttempts")
    List<User> findUsersWithExcessiveFailedAttempts(@Param("maxAttempts") Integer maxAttempts);

    @Query("SELECT u FROM User u WHERE u.accountLockedAt IS NOT NULL AND u.accountLockedAt < :cutoffDate")
    List<User> findLockedUsersOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Bulk operations
    @Modifying
    @Query("UPDATE User u SET u.status = :status, u.statusChangedAt = :timestamp, u.statusChangedBy = :changedBy WHERE u.id IN :userIds")
    int bulkUpdateStatus(@Param("userIds") List<UUID> userIds, 
                        @Param("status") UserStatus status, 
                        @Param("timestamp") LocalDateTime timestamp,
                        @Param("changedBy") UUID changedBy);

    @Modifying
    @Query("UPDATE User u SET u.emailVerified = :verified WHERE u.id IN :userIds")
    int bulkUpdateEmailVerification(@Param("userIds") List<UUID> userIds, @Param("verified") Boolean verified);

    @Modifying
    @Query("UPDATE User u SET u.phoneVerified = :verified WHERE u.id IN :userIds")
    int bulkUpdatePhoneVerification(@Param("userIds") List<UUID> userIds, @Param("verified") Boolean verified);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.lastFailedLoginAt = NULL WHERE u.id IN :userIds")
    int resetFailedLoginAttempts(@Param("userIds") List<UUID> userIds);

    // Search queries
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Statistical queries
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    long countNewUsersAfter(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLoginAt >= :startDate")
    long countActiveUsersAfter(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT u.status, COUNT(u) FROM User u GROUP BY u.status")
    List<Object[]> getUserStatusStatistics();

    @Query("SELECT AVG(u.loginCount) FROM User u WHERE u.loginCount > 0")
    Double getAverageLoginCount();

    @Query("SELECT u FROM User u WHERE u.timezone = :timezone")
    List<User> findByTimezone(@Param("timezone") String timezone);

    @Query("SELECT u FROM User u WHERE u.locale = :locale")
    List<User> findByLocale(@Param("locale") String locale);

    // Email/Phone verification queries
    List<User> findByEmailVerifiedFalse();
    List<User> findByPhoneVerifiedFalse();

    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :cutoffDate")
    List<User> findUnverifiedEmailsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
} 