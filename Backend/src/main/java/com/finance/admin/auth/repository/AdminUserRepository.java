package com.finance.admin.auth.repository;

import com.finance.admin.auth.entity.AdminRole;
import com.finance.admin.auth.entity.AdminUser;
import com.finance.admin.auth.entity.AdminUserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public interface AdminUserRepository extends JpaRepository<AdminUser, UUID>, JpaSpecificationExecutor<AdminUser> {

    Optional<AdminUser> findByUsername(String username);

    Optional<AdminUser> findByEmail(String email);

    Optional<AdminUser> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<AdminUser> findByStatus(AdminUserStatus status);

    Page<AdminUser> findByStatus(AdminUserStatus status, Pageable pageable);

    @Query("SELECT au FROM AdminUser au JOIN au.roles r WHERE r = :role")
    List<AdminUser> findByRole(@Param("role") AdminRole role);

    @Query("SELECT au FROM AdminUser au JOIN au.roles r WHERE r = :role")
    Page<AdminUser> findByRole(@Param("role") AdminRole role, Pageable pageable);

    @Query("SELECT au FROM AdminUser au WHERE au.lockedUntil IS NOT NULL AND au.lockedUntil > :now")
    List<AdminUser> findLockedUsers(@Param("now") LocalDateTime now);

    @Query("SELECT au FROM AdminUser au WHERE au.lastLogin < :since")
    List<AdminUser> findInactiveUsers(@Param("since") LocalDateTime since);

    @Query("SELECT au FROM AdminUser au WHERE au.passwordExpiresAt < :now")
    List<AdminUser> findUsersWithExpiredPasswords(@Param("now") LocalDateTime now);

    @Query("SELECT au FROM AdminUser au WHERE au.failedLoginAttempts >= :maxAttempts")
    List<AdminUser> findUsersWithExcessiveFailedAttempts(@Param("maxAttempts") int maxAttempts);

    @Modifying
    @Query("UPDATE AdminUser au SET au.lastLogin = :loginTime WHERE au.id = :userId")
    void updateLastLogin(@Param("userId") UUID userId, @Param("loginTime") LocalDateTime loginTime);

    @Modifying
    @Query("UPDATE AdminUser au SET au.failedLoginAttempts = au.failedLoginAttempts + 1 WHERE au.id = :userId")
    void incrementFailedAttempts(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE AdminUser au SET au.failedLoginAttempts = 0, au.lockedUntil = NULL WHERE au.id = :userId")
    void resetFailedAttempts(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE AdminUser au SET au.lockedUntil = :until WHERE au.id = :userId")
    void lockUser(@Param("userId") UUID userId, @Param("until") LocalDateTime until);

    @Modifying
    @Query("UPDATE AdminUser au SET au.status = :status WHERE au.id = :userId")
    void updateUserStatus(@Param("userId") UUID userId, @Param("status") AdminUserStatus status);

    @Query("SELECT COUNT(au) FROM AdminUser au WHERE au.status = :status")
    long countByStatus(@Param("status") AdminUserStatus status);

    @Query("SELECT au.status, COUNT(au) FROM AdminUser au GROUP BY au.status")
    List<Object[]> countByStatusGrouped();

    @Query("SELECT au FROM AdminUser au WHERE au.createdAt BETWEEN :startDate AND :endDate")
    List<AdminUser> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT au FROM AdminUser au WHERE au.lastLogin BETWEEN :startDate AND :endDate")
    List<AdminUser> findUsersWithLastLoginBetween(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);

    // Search functionality
    @Query("SELECT au FROM AdminUser au WHERE " +
           "LOWER(au.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(au.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<AdminUser> findByUsernameOrEmailContaining(@Param("search") String search, Pageable pageable);

    @Query("SELECT au FROM AdminUser au WHERE au.mfaEnabled = :mfaEnabled")
    List<AdminUser> findByMfaEnabled(@Param("mfaEnabled") boolean mfaEnabled);
} 
