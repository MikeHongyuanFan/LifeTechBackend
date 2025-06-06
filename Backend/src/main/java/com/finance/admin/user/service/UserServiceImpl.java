package com.finance.admin.user.service;

import com.finance.admin.common.exception.ResourceNotFoundException;
import com.finance.admin.notification.service.NotificationService;
import com.finance.admin.user.dto.*;
import com.finance.admin.user.entity.User;
import com.finance.admin.user.entity.UserStatus;
import com.finance.admin.user.mapper.UserMapper;
import com.finance.admin.user.repository.UserRepository;
import com.finance.admin.user.specification.UserSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificationService notificationService;

    // CRUD Operations
    @Override
    public UserResponse createUser(UserCreateRequest request) {
        logger.info("Creating new user with username: {}", request.getUsername());

        // Validate unique constraints
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        // Create user entity
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save user
        User savedUser = userRepository.save(user);
        logger.info("User created successfully with ID: {}", savedUser.getId());

        // Send welcome email if requested
        if (request.getSendWelcomeEmail() != null && request.getSendWelcomeEmail()) {
            try {
                notificationService.sendWelcomeEmail(savedUser);
            } catch (Exception e) {
                logger.warn("Failed to send welcome email to user: {}", savedUser.getEmail(), e);
            }
        }

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        logger.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Check unique constraints if updating username or email
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername()) 
            && userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail()) 
            && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        UserStatus oldStatus = user.getStatus();
        userMapper.updateEntity(user, request);

        // Handle status change with reason
        if (request.getStatus() != null && request.getStatus() != oldStatus) {
            user.updateStatus(request.getStatus(), getCurrentUserId(), request.getStatusChangeReason());
            
            try {
                notificationService.sendAccountStatusChangeNotification(user, oldStatus, request.getStatus(), request.getStatusChangeReason());
            } catch (Exception e) {
                logger.warn("Failed to send status change notification to user: {}", user.getEmail(), e);
            }
        }

        User savedUser = userRepository.save(user);
        logger.info("User updated successfully with ID: {}", savedUser.getId());

        return userMapper.toResponse(savedUser);
    }

    @Override
    public void deleteUser(UUID id) {
        logger.info("Deleting user with ID: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        userRepository.delete(user);
        logger.info("User deleted successfully with ID: {}", id);
    }

    @Override
    public void softDeleteUser(UUID id, String reason) {
        logger.info("Soft deleting user with ID: {}, reason: {}", id, reason);
        
        UserUpdateRequest request = new UserUpdateRequest();
        request.setStatus(UserStatus.DEACTIVATED);
        request.setStatusChangeReason(reason);
        
        updateUser(id, request);
    }

    // Search and Filtering
    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(UserSearchCriteria criteria, Pageable pageable) {
        logger.debug("Searching users with criteria: {}", criteria);

        Specification<User> spec = UserSpecifications.withCriteria(criteria);
        Page<User> users = userRepository.findAll(spec, pageable);
        
        return users.map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByStatus(UserStatus status) {
        List<User> users = userRepository.findByStatusIn(Arrays.asList(status));
        return userMapper.toResponseList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByStatusPage(UserStatus status, Pageable pageable) {
        Page<User> users = userRepository.findByStatus(status, pageable);
        return users.map(userMapper::toResponse);
    }

    // Bulk Operations
    @Override
    public void bulkUpdateUserStatus(List<UUID> userIds, UserStatus status, String reason) {
        logger.info("Bulk updating status for {} users to: {}", userIds.size(), status);

        int updatedCount = userRepository.bulkUpdateStatus(userIds, status, LocalDateTime.now(), getCurrentUserId());
        logger.info("Successfully updated status for {} users", updatedCount);

        // Send notifications asynchronously
        try {
            List<User> users = userRepository.findAllById(userIds);
            Map<String, Object> operationDetails = Map.of(
                "operation", "bulk_status_update",
                "newStatus", status.name(),
                "reason", reason,
                "affectedUsers", updatedCount
            );
            notificationService.sendBulkOperationNotification(users, "Status Update", operationDetails);
        } catch (Exception e) {
            logger.warn("Failed to send bulk operation notifications", e);
        }
    }

    @Override
    public void bulkDeleteUsers(List<UUID> userIds, String reason) {
        logger.info("Bulk deleting {} users, reason: {}", userIds.size(), reason);
        
        bulkUpdateUserStatus(userIds, UserStatus.DEACTIVATED, reason);
    }

    @Override
    public void bulkUpdateEmailVerification(List<UUID> userIds, Boolean verified) {
        logger.info("Bulk updating email verification for {} users to: {}", userIds.size(), verified);
        
        int updatedCount = userRepository.bulkUpdateEmailVerification(userIds, verified);
        logger.info("Successfully updated email verification for {} users", updatedCount);
    }

    @Override
    public void bulkUpdatePhoneVerification(List<UUID> userIds, Boolean verified) {
        logger.info("Bulk updating phone verification for {} users to: {}", userIds.size(), verified);
        
        int updatedCount = userRepository.bulkUpdatePhoneVerification(userIds, verified);
        logger.info("Successfully updated phone verification for {} users", updatedCount);
    }

    @Override
    public void bulkResetFailedLoginAttempts(List<UUID> userIds) {
        logger.info("Bulk resetting failed login attempts for {} users", userIds.size());
        
        int updatedCount = userRepository.resetFailedLoginAttempts(userIds);
        logger.info("Successfully reset failed login attempts for {} users", updatedCount);
    }

    @Override
    public Map<String, Object> bulkCreateUsers(List<UserCreateRequest> requests) {
        logger.info("Bulk creating {} users", requests.size());

        Map<String, Object> result = new HashMap<>();
        List<UserResponse> successful = new ArrayList<>();
        List<Map<String, Object>> failed = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            try {
                UserResponse created = createUser(requests.get(i));
                successful.add(created);
            } catch (Exception e) {
                Map<String, Object> error = new HashMap<>();
                error.put("index", i);
                error.put("request", requests.get(i));
                error.put("error", e.getMessage());
                failed.add(error);
                logger.warn("Failed to create user at index {}: {}", i, e.getMessage());
            }
        }

        result.put("successful", successful);
        result.put("failed", failed);
        result.put("totalRequested", requests.size());
        result.put("successCount", successful.size());
        result.put("failureCount", failed.size());

        logger.info("Bulk user creation completed: {} successful, {} failed", successful.size(), failed.size());
        return result;
    }

    // Helper method to get current user ID (would be implemented based on security context)
    private UUID getCurrentUserId() {
        // In a real implementation, this would get the current authenticated user ID
        // For now, return a default admin user ID or null
        return null;
    }

    // Utility Methods
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalUserCount() {
        return userRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveUserCount() {
        return userRepository.countByStatus(UserStatus.ACTIVE);
    }

    // Status Management
    @Override
    public UserResponse updateUserStatus(UUID id, UserStatus status, String reason) {
        logger.info("Updating user status for ID: {} to {}", id, status);
        
        UserUpdateRequest request = new UserUpdateRequest();
        request.setStatus(status);
        request.setStatusChangeReason(reason);
        
        return updateUser(id, request);
    }

    @Override
    public UserResponse lockUser(UUID id, String reason) {
        return updateUserStatus(id, UserStatus.LOCKED, reason);
    }

    @Override
    public UserResponse unlockUser(UUID id) {
        return updateUserStatus(id, UserStatus.ACTIVE, "Account unlocked by administrator");
    }

    @Override
    public UserResponse suspendUser(UUID id, String reason) {
        return updateUserStatus(id, UserStatus.SUSPENDED, reason);
    }

    @Override
    public UserResponse activateUser(UUID id) {
        return updateUserStatus(id, UserStatus.ACTIVE, "Account activated by administrator");
    }

    @Override
    public UserResponse banUser(UUID id, String reason) {
        return updateUserStatus(id, UserStatus.BANNED, reason);
    }

    // Account Verification
    @Override
    public UserResponse verifyEmail(UUID id) {
        logger.info("Verifying email for user ID: {}", id);
        
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmailVerified(true);
        
        return updateUser(id, request);
    }

    @Override
    public UserResponse verifyPhone(UUID id) {
        logger.info("Verifying phone for user ID: {}", id);
        
        UserUpdateRequest request = new UserUpdateRequest();
        request.setPhoneVerified(true);
        
        return updateUser(id, request);
    }

    @Override
    public void sendEmailVerification(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        String verificationToken = generateVerificationToken();
        notificationService.sendEmailVerification(user, verificationToken);
    }

    @Override
    public void sendPasswordReset(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        String resetToken = generateResetToken();
        notificationService.sendPasswordResetEmail(user, resetToken);
    }

    // Analytics and Reporting
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalUsers", getTotalUserCount());
        stats.put("activeUsers", getActiveUserCount());
        stats.put("usersByStatus", getUserStatusStatistics());
        stats.put("averageLoginCount", getAverageLoginCount());
        
        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
        stats.put("newUsersLastWeek", userRepository.countNewUsersAfter(lastWeek));
        stats.put("activeUsersLastWeek", userRepository.countActiveUsersAfter(lastWeek));
        
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UserStatus, Long> getUserStatusStatistics() {
        List<Object[]> results = userRepository.getUserStatusStatistics();
        Map<UserStatus, Long> statusCounts = new HashMap<>();
        
        for (Object[] result : results) {
            UserStatus status = (UserStatus) result[0];
            Long count = (Long) result[1];
            statusCounts.put(status, count);
        }
        
        return statusCounts;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getRecentUsers(int limit) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        Page<User> users = userRepository.findAll(pageable);
        return userMapper.toResponseList(users.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getActiveUsers(LocalDateTime since) {
        List<User> users = userRepository.findUsersWithLoginBetween(since, LocalDateTime.now());
        return userMapper.toResponseList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getInactiveUsers(LocalDateTime cutoffDate) {
        List<User> users = userRepository.findInactiveUsers(cutoffDate);
        return userMapper.toResponseList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getLockedUsers() {
        return getUsersByStatus(UserStatus.LOCKED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersWithExcessiveFailedAttempts(Integer maxAttempts) {
        List<User> users = userRepository.findUsersWithExcessiveFailedAttempts(maxAttempts);
        return userMapper.toResponseList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageLoginCount() {
        return userRepository.getAverageLoginCount();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByTimezone(String timezone) {
        List<User> users = userRepository.findByTimezone(timezone);
        return userMapper.toResponseList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByLocale(String locale) {
        List<User> users = userRepository.findByLocale(locale);
        return userMapper.toResponseList(users);
    }

    // Account Management
    @Override
    public void recordLoginSuccess(UUID id, String ipAddress) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        user.incrementLoginCount();
        user.setLastLoginIp(ipAddress);
        user.resetFailedLoginAttempts();
        
        userRepository.save(user);
        logger.info("Recorded successful login for user: {}", user.getUsername());
    }

    @Override
    public void recordLoginFailure(UUID id, String ipAddress) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        user.incrementFailedLoginAttempts();
        user.setLastLoginIp(ipAddress);
        
        // Auto-lock account if too many failed attempts
        if (user.getFailedLoginAttempts() >= 5) {
            user.updateStatus(UserStatus.LOCKED, null, "Account locked due to excessive failed login attempts");
            notificationService.sendAccountLockedNotification(user, "Too many failed login attempts");
        }
        
        userRepository.save(user);
        logger.warn("Recorded failed login attempt for user: {} (attempt {})", user.getUsername(), user.getFailedLoginAttempts());
    }

    @Override
    public void resetPassword(UUID id, String newPassword) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.resetFailedLoginAttempts();
        
        userRepository.save(user);
        logger.info("Password reset for user: {}", user.getUsername());
    }

    @Override
    public void updateLastActivity(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // Cleanup Operations
    @Override
    public void cleanupUnverifiedUsers(LocalDateTime cutoffDate) {
        logger.info("Cleaning up unverified users older than: {}", cutoffDate);
        
        List<User> unverifiedUsers = userRepository.findUnverifiedEmailsOlderThan(cutoffDate);
        List<UUID> userIds = unverifiedUsers.stream().map(User::getId).collect(Collectors.toList());
        
        if (!userIds.isEmpty()) {
            bulkUpdateUserStatus(userIds, UserStatus.DEACTIVATED, "Account deactivated due to unverified email");
            logger.info("Cleaned up {} unverified users", userIds.size());
        }
    }

    @Override
    public void unlockExpiredLockedUsers(LocalDateTime cutoffDate) {
        logger.info("Unlocking users locked before: {}", cutoffDate);
        
        List<User> lockedUsers = userRepository.findLockedUsersOlderThan(cutoffDate);
        List<UUID> userIds = lockedUsers.stream().map(User::getId).collect(Collectors.toList());
        
        if (!userIds.isEmpty()) {
            bulkUpdateUserStatus(userIds, UserStatus.ACTIVE, "Account unlocked after lock expiry");
            logger.info("Unlocked {} expired locked users", userIds.size());
        }
    }

    // Helper methods
    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }
} 
