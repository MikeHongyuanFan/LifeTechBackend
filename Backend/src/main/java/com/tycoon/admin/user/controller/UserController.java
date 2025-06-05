package com.tycoon.admin.user.controller;

import com.tycoon.admin.common.dto.ApiResponse;
import com.tycoon.admin.user.dto.*;
import com.tycoon.admin.user.entity.UserStatus;
import com.tycoon.admin.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "APIs for comprehensive user account administration")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    // CRUD Operations

    @PostMapping
    @Operation(summary = "Create new user", description = "Create a new user account with comprehensive details")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_CREATE')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        logger.info("Creating user with username: {}", request.getUsername());
        
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("User created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve user details by unique identifier")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", response));
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username", description = "Retrieve user details by username")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(@PathVariable String username) {
        UserResponse response = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", response));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieve user details by email address")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        UserResponse response = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user account details")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_UPDATE')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request) {
        
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Permanently delete user account")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    @PostMapping("/{id}/soft-delete")
    @Operation(summary = "Soft delete user", description = "Deactivate user account without permanent deletion")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_DELETE')")
    public ResponseEntity<ApiResponse<Void>> softDeleteUser(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "Account deactivated by administrator") String reason) {
        
        userService.softDeleteUser(id, reason);
        return ResponseEntity.ok(ApiResponse.success("User account deactivated successfully"));
    }

    // Search and Filtering

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve paginated list of all users")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<UserResponse> response = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response));
    }

    @PostMapping("/search")
    @Operation(summary = "Search users", description = "Search users with advanced filtering criteria")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> searchUsers(
            @RequestBody UserSearchCriteria criteria,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<UserResponse> response = userService.searchUsers(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success("User search completed successfully", response));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get users by status", description = "Retrieve users filtered by account status")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsersByStatus(
            @PathVariable UserStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<UserResponse> response = userService.getUsersByStatusPage(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response));
    }

    // Bulk Operations

    @PostMapping("/bulk/status-update")
    @Operation(summary = "Bulk update user status", description = "Update status for multiple users simultaneously")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_BULK_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> bulkUpdateUserStatus(
            @RequestBody List<UUID> userIds,
            @RequestParam UserStatus status,
            @RequestParam(required = false, defaultValue = "Bulk status update") String reason) {
        
        userService.bulkUpdateUserStatus(userIds, status, reason);
        return ResponseEntity.ok(ApiResponse.success("Bulk user status update completed successfully"));
    }

    @PostMapping("/bulk/delete")
    @Operation(summary = "Bulk delete users", description = "Delete multiple users simultaneously")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_BULK_DELETE')")
    public ResponseEntity<ApiResponse<Void>> bulkDeleteUsers(
            @RequestBody List<UUID> userIds,
            @RequestParam(required = false, defaultValue = "Bulk deletion") String reason) {
        
        userService.bulkDeleteUsers(userIds, reason);
        return ResponseEntity.ok(ApiResponse.success("Bulk user deletion completed successfully"));
    }

    @PostMapping("/bulk/email-verification")
    @Operation(summary = "Bulk update email verification", description = "Update email verification status for multiple users")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_BULK_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> bulkUpdateEmailVerification(
            @RequestBody List<UUID> userIds,
            @RequestParam Boolean verified) {
        
        userService.bulkUpdateEmailVerification(userIds, verified);
        return ResponseEntity.ok(ApiResponse.success("Bulk email verification update completed successfully"));
    }

    @PostMapping("/bulk/phone-verification")
    @Operation(summary = "Bulk update phone verification", description = "Update phone verification status for multiple users")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_BULK_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> bulkUpdatePhoneVerification(
            @RequestBody List<UUID> userIds,
            @RequestParam Boolean verified) {
        
        userService.bulkUpdatePhoneVerification(userIds, verified);
        return ResponseEntity.ok(ApiResponse.success("Bulk phone verification update completed successfully"));
    }

    @PostMapping("/bulk/reset-failed-attempts")
    @Operation(summary = "Bulk reset failed login attempts", description = "Reset failed login attempts for multiple users")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_BULK_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> bulkResetFailedLoginAttempts(@RequestBody List<UUID> userIds) {
        userService.bulkResetFailedLoginAttempts(userIds);
        return ResponseEntity.ok(ApiResponse.success("Bulk failed login attempts reset completed successfully"));
    }

    @PostMapping("/bulk/create")
    @Operation(summary = "Bulk create users", description = "Create multiple users simultaneously with detailed error reporting")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_BULK_CREATE')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> bulkCreateUsers(
            @Valid @RequestBody List<UserCreateRequest> requests) {
        
        Map<String, Object> response = userService.bulkCreateUsers(requests);
        return ResponseEntity.ok(ApiResponse.success("Bulk user creation completed", response));
    }

    // Status Management

    @PostMapping("/{id}/lock")
    @Operation(summary = "Lock user account", description = "Lock user account for security reasons")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_LOCK')")
    public ResponseEntity<ApiResponse<UserResponse>> lockUser(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "Account locked by administrator") String reason) {
        
        UserResponse response = userService.lockUser(id, reason);
        return ResponseEntity.ok(ApiResponse.success("User account locked successfully", response));
    }

    @PostMapping("/{id}/unlock")
    @Operation(summary = "Unlock user account", description = "Unlock previously locked user account")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_UNLOCK')")
    public ResponseEntity<ApiResponse<UserResponse>> unlockUser(@PathVariable UUID id) {
        UserResponse response = userService.unlockUser(id);
        return ResponseEntity.ok(ApiResponse.success("User account unlocked successfully", response));
    }

    @PostMapping("/{id}/suspend")
    @Operation(summary = "Suspend user account", description = "Temporarily suspend user account")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_SUSPEND')")
    public ResponseEntity<ApiResponse<UserResponse>> suspendUser(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "Account suspended by administrator") String reason) {
        
        UserResponse response = userService.suspendUser(id, reason);
        return ResponseEntity.ok(ApiResponse.success("User account suspended successfully", response));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate user account", description = "Activate user account")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_ACTIVATE')")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(@PathVariable UUID id) {
        UserResponse response = userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User account activated successfully", response));
    }

    @PostMapping("/{id}/ban")
    @Operation(summary = "Ban user account", description = "Permanently ban user account")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_BAN')")
    public ResponseEntity<ApiResponse<UserResponse>> banUser(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "Account banned by administrator") String reason) {
        
        UserResponse response = userService.banUser(id, reason);
        return ResponseEntity.ok(ApiResponse.success("User account banned successfully", response));
    }

    // Account Verification

    @PostMapping("/{id}/verify-email")
    @Operation(summary = "Verify user email", description = "Mark user email as verified")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_VERIFY')")
    public ResponseEntity<ApiResponse<UserResponse>> verifyEmail(@PathVariable UUID id) {
        UserResponse response = userService.verifyEmail(id);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", response));
    }

    @PostMapping("/{id}/verify-phone")
    @Operation(summary = "Verify user phone", description = "Mark user phone as verified")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_VERIFY')")
    public ResponseEntity<ApiResponse<UserResponse>> verifyPhone(@PathVariable UUID id) {
        UserResponse response = userService.verifyPhone(id);
        return ResponseEntity.ok(ApiResponse.success("Phone verified successfully", response));
    }

    @PostMapping("/{id}/send-email-verification")
    @Operation(summary = "Send email verification", description = "Send email verification to user")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_VERIFY')")
    public ResponseEntity<ApiResponse<Void>> sendEmailVerification(@PathVariable UUID id) {
        userService.sendEmailVerification(id);
        return ResponseEntity.ok(ApiResponse.success("Email verification sent successfully"));
    }

    @PostMapping("/{id}/send-password-reset")
    @Operation(summary = "Send password reset", description = "Send password reset email to user")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_PASSWORD_RESET')")
    public ResponseEntity<ApiResponse<Void>> sendPasswordReset(@PathVariable UUID id) {
        userService.sendPasswordReset(id);
        return ResponseEntity.ok(ApiResponse.success("Password reset email sent successfully"));
    }

    // Analytics and Reporting

    @GetMapping("/statistics")
    @Operation(summary = "Get user statistics", description = "Retrieve comprehensive user analytics")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_ANALYTICS')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStatistics() {
        Map<String, Object> response = userService.getUserStatistics();
        return ResponseEntity.ok(ApiResponse.success("User statistics retrieved successfully", response));
    }

    @GetMapping("/statistics/status")
    @Operation(summary = "Get user status statistics", description = "Retrieve user count by status")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_ANALYTICS')")
    public ResponseEntity<ApiResponse<Map<UserStatus, Long>>> getUserStatusStatistics() {
        Map<UserStatus, Long> response = userService.getUserStatusStatistics();
        return ResponseEntity.ok(ApiResponse.success("User status statistics retrieved successfully", response));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent users", description = "Retrieve recently created users")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getRecentUsers(
            @Parameter(description = "Maximum number of users to return") 
            @RequestParam(defaultValue = "10") int limit) {
        
        List<UserResponse> response = userService.getRecentUsers(limit);
        return ResponseEntity.ok(ApiResponse.success("Recent users retrieved successfully", response));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active users", description = "Retrieve users active since specified date")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_ANALYTICS')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getActiveUsers(
            @Parameter(description = "Date to check activity from")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        
        List<UserResponse> response = userService.getActiveUsers(since);
        return ResponseEntity.ok(ApiResponse.success("Active users retrieved successfully", response));
    }

    @GetMapping("/inactive")
    @Operation(summary = "Get inactive users", description = "Retrieve users inactive since specified date")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_ANALYTICS')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getInactiveUsers(
            @Parameter(description = "Cutoff date for inactivity")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cutoffDate) {
        
        List<UserResponse> response = userService.getInactiveUsers(cutoffDate);
        return ResponseEntity.ok(ApiResponse.success("Inactive users retrieved successfully", response));
    }

    @GetMapping("/locked")
    @Operation(summary = "Get locked users", description = "Retrieve all locked user accounts")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_ANALYTICS')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getLockedUsers() {
        List<UserResponse> response = userService.getLockedUsers();
        return ResponseEntity.ok(ApiResponse.success("Locked users retrieved successfully", response));
    }

    // Utility Endpoints

    @GetMapping("/exists/username/{username}")
    @Operation(summary = "Check username exists", description = "Check if username is already taken")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameExists(@PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(ApiResponse.success("Username availability checked", exists));
    }

    @GetMapping("/exists/email/{email}")
    @Operation(summary = "Check email exists", description = "Check if email is already registered")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("Email availability checked", exists));
    }

    @GetMapping("/count")
    @Operation(summary = "Get total user count", description = "Retrieve total number of users")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_ANALYTICS')")
    public ResponseEntity<ApiResponse<Long>> getTotalUserCount() {
        long count = userService.getTotalUserCount();
        return ResponseEntity.ok(ApiResponse.success("Total user count retrieved", count));
    }

    @GetMapping("/count/active")
    @Operation(summary = "Get active user count", description = "Retrieve number of active users")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_ANALYTICS')")
    public ResponseEntity<ApiResponse<Long>> getActiveUserCount() {
        long count = userService.getActiveUserCount();
        return ResponseEntity.ok(ApiResponse.success("Active user count retrieved", count));
    }

    // Cleanup Operations

    @PostMapping("/cleanup/unverified")
    @Operation(summary = "Cleanup unverified users", description = "Deactivate users with unverified emails older than cutoff date")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_CLEANUP')")
    public ResponseEntity<ApiResponse<Void>> cleanupUnverifiedUsers(
            @Parameter(description = "Cutoff date for cleanup")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cutoffDate) {
        
        userService.cleanupUnverifiedUsers(cutoffDate);
        return ResponseEntity.ok(ApiResponse.success("Unverified users cleanup completed"));
    }

    @PostMapping("/cleanup/expired-locks")
    @Operation(summary = "Unlock expired locked users", description = "Unlock users locked before cutoff date")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_CLEANUP')")
    public ResponseEntity<ApiResponse<Void>> unlockExpiredLockedUsers(
            @Parameter(description = "Cutoff date for unlock")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cutoffDate) {
        
        userService.unlockExpiredLockedUsers(cutoffDate);
        return ResponseEntity.ok(ApiResponse.success("Expired locked users unlocked successfully"));
    }
} 