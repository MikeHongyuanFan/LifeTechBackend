package com.tycoon.admin.auth.entity;

public enum AdminUserStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended"),
    LOCKED("Locked"),
    EXPIRED("Expired"),
    PENDING_ACTIVATION("Pending Activation");

    private final String displayName;

    AdminUserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isBlocked() {
        return this == SUSPENDED || this == LOCKED || this == EXPIRED;
    }
} 