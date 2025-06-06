package com.finance.admin.user.entity;

/**
 * Enum representing the various states a user account can be in
 */
public enum UserStatus {
    ACTIVE("Active", "User account is active and can perform all operations"),
    SUSPENDED("Suspended", "User account is temporarily suspended"),
    BANNED("Banned", "User account is permanently banned"),
    PENDING_VERIFICATION("Pending Verification", "User account is pending email verification"),
    DEACTIVATED("Deactivated", "User account has been deactivated by the user"),
    LOCKED("Locked", "User account is locked due to security reasons");

    private final String displayName;
    private final String description;

    UserStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean canLogin() {
        return this == ACTIVE;
    }

    public boolean isRestricted() {
        return this == SUSPENDED || this == BANNED || this == LOCKED;
    }
} 
