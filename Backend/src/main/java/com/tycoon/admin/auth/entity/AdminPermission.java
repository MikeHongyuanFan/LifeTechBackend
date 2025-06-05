package com.tycoon.admin.auth.entity;

public enum AdminPermission {
    // User Management Permissions
    USER_READ("user:read"),
    USER_WRITE("user:write"),
    USER_DELETE("user:delete"),
    
    // Admin Management Permissions
    ADMIN_READ("admin:read"),
    ADMIN_WRITE("admin:write"),
    ADMIN_DELETE("admin:delete"),
    
    // Role Management Permissions
    ROLE_READ("role:read"),
    ROLE_WRITE("role:write"),
    ROLE_DELETE("role:delete"),
    
    // Audit Permissions
    AUDIT_READ("audit:read"),
    AUDIT_WRITE("audit:write"),
    
    // System Configuration Permissions
    SYSTEM_READ("system:read"),
    SYSTEM_WRITE("system:write"),
    
    // Financial Operations Permissions
    FINANCIAL_READ("financial:read"),
    FINANCIAL_WRITE("financial:write"),
    
    // Compliance Permissions
    COMPLIANCE_READ("compliance:read"),
    COMPLIANCE_WRITE("compliance:write"),
    
    // Customer Service Permissions
    CUSTOMER_SERVICE_READ("customer_service:read"),
    CUSTOMER_SERVICE_WRITE("customer_service:write");

    private final String permission;

    AdminPermission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
} 