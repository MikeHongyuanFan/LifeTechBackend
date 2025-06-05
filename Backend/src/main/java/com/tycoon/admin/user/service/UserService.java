package com.tycoon.admin.user.service;

import com.tycoon.admin.user.dto.*;
import com.tycoon.admin.user.entity.User;
import com.tycoon.admin.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface UserService {

    // CRUD Operations
    UserResponse createUser(UserCreateRequest request);
    UserResponse getUserById(UUID id);
    UserResponse getUserByUsername(String username);
    UserResponse getUserByEmail(String email);
    UserResponse updateUser(UUID id, UserUpdateRequest request);
    void deleteUser(UUID id);
    void softDeleteUser(UUID id, String reason);

    // Search and Filtering
    Page<UserResponse> searchUsers(UserSearchCriteria criteria, Pageable pageable);
    Page<UserResponse> getAllUsers(Pageable pageable);
    List<UserResponse> getUsersByStatus(UserStatus status);
    Page<UserResponse> getUsersByStatusPage(UserStatus status, Pageable pageable);

    // Bulk Operations
    void bulkUpdateUserStatus(List<UUID> userIds, UserStatus status, String reason);
    void bulkDeleteUsers(List<UUID> userIds, String reason);
    void bulkUpdateEmailVerification(List<UUID> userIds, Boolean verified);
    void bulkUpdatePhoneVerification(List<UUID> userIds, Boolean verified);
    void bulkResetFailedLoginAttempts(List<UUID> userIds);
    Map<String, Object> bulkCreateUsers(List<UserCreateRequest> requests);

    // Status Management
    UserResponse updateUserStatus(UUID id, UserStatus status, String reason);
    UserResponse lockUser(UUID id, String reason);
    UserResponse unlockUser(UUID id);
    UserResponse suspendUser(UUID id, String reason);
    UserResponse activateUser(UUID id);
    UserResponse banUser(UUID id, String reason);

    // Account Verification
    UserResponse verifyEmail(UUID id);
    UserResponse verifyPhone(UUID id);
    void sendEmailVerification(UUID id);
    void sendPasswordReset(UUID id);

    // Analytics and Reporting
    Map<String, Object> getUserStatistics();
    Map<UserStatus, Long> getUserStatusStatistics();
    List<UserResponse> getRecentUsers(int limit);
    List<UserResponse> getActiveUsers(LocalDateTime since);
    List<UserResponse> getInactiveUsers(LocalDateTime cutoffDate);
    List<UserResponse> getLockedUsers();
    List<UserResponse> getUsersWithExcessiveFailedAttempts(Integer maxAttempts);
    Double getAverageLoginCount();

    // Utility Methods
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    long getTotalUserCount();
    long getActiveUserCount();
    List<UserResponse> getUsersByTimezone(String timezone);
    List<UserResponse> getUsersByLocale(String locale);

    // Account Management
    void recordLoginSuccess(UUID id, String ipAddress);
    void recordLoginFailure(UUID id, String ipAddress);
    void resetPassword(UUID id, String newPassword);
    void updateLastActivity(UUID id);

    // Cleanup Operations
    void cleanupUnverifiedUsers(LocalDateTime cutoffDate);
    void unlockExpiredLockedUsers(LocalDateTime cutoffDate);
} 