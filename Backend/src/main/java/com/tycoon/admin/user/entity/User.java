package com.tycoon.admin.user.entity;

import com.tycoon.admin.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_username", columnList = "username"),
    @Index(name = "idx_user_status", columnList = "status"),
    @Index(name = "idx_user_created_at", columnList = "createdAt"),
    @Index(name = "idx_user_last_login", columnList = "lastLoginAt")
})
public class User extends BaseEntity {

    @Column(unique = true, nullable = false, length = 50)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, underscores, and hyphens")
    private String username;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Column(name = "first_name", length = 50)
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Column(name = "last_name", length = 50)
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Column(name = "phone_number", length = 20)
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{10,20}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "phone_verified")
    private Boolean phoneVerified = false;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;

    @Column(length = 10)
    private String timezone;

    @Column(length = 10)
    private String locale;

    // Analytics fields
    @Column(name = "login_count")
    private Long loginCount = 0L;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip")
    private String lastLoginIp;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "last_failed_login_at")
    private LocalDateTime lastFailedLoginAt;

    @Column(name = "account_locked_at")
    private LocalDateTime accountLockedAt;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "status_changed_at")
    private LocalDateTime statusChangedAt;

    @Column(name = "status_changed_by")
    private UUID statusChangedBy;

    @Column(name = "status_change_reason", length = 500)
    private String statusChangeReason;

    // Constructors
    public User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
        this.statusChangedAt = LocalDateTime.now();
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Boolean getPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(Boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public LocalDateTime getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDateTime dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Long getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Long loginCount) {
        this.loginCount = loginCount;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLastFailedLoginAt() {
        return lastFailedLoginAt;
    }

    public void setLastFailedLoginAt(LocalDateTime lastFailedLoginAt) {
        this.lastFailedLoginAt = lastFailedLoginAt;
    }

    public LocalDateTime getAccountLockedAt() {
        return accountLockedAt;
    }

    public void setAccountLockedAt(LocalDateTime accountLockedAt) {
        this.accountLockedAt = accountLockedAt;
    }

    public LocalDateTime getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
    }

    public LocalDateTime getStatusChangedAt() {
        return statusChangedAt;
    }

    public void setStatusChangedAt(LocalDateTime statusChangedAt) {
        this.statusChangedAt = statusChangedAt;
    }

    public UUID getStatusChangedBy() {
        return statusChangedBy;
    }

    public void setStatusChangedBy(UUID statusChangedBy) {
        this.statusChangedBy = statusChangedBy;
    }

    public String getStatusChangeReason() {
        return statusChangeReason;
    }

    public void setStatusChangeReason(String statusChangeReason) {
        this.statusChangeReason = statusChangeReason;
    }

    // Utility methods
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return username;
    }

    public void incrementLoginCount() {
        this.loginCount = (this.loginCount == null) ? 1L : this.loginCount + 1L;
        this.lastLoginAt = LocalDateTime.now();
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null) ? 1 : this.failedLoginAttempts + 1;
        this.lastFailedLoginAt = LocalDateTime.now();
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lastFailedLoginAt = null;
    }

    public boolean isAccountLocked() {
        return status == UserStatus.LOCKED || 
               (failedLoginAttempts != null && failedLoginAttempts >= 5);
    }

    public void updateStatus(UserStatus newStatus, UUID changedBy, String reason) {
        this.status = newStatus;
        this.statusChangedAt = LocalDateTime.now();
        this.statusChangedBy = changedBy;
        this.statusChangeReason = reason;
        
        if (newStatus == UserStatus.LOCKED) {
            this.accountLockedAt = LocalDateTime.now();
        } else if (this.accountLockedAt != null && newStatus == UserStatus.ACTIVE) {
            this.accountLockedAt = null;
            this.resetFailedLoginAttempts();
        }
    }
} 