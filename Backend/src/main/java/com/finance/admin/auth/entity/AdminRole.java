package com.finance.admin.auth.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum AdminRole {
    SUPER_ADMIN(Set.of(
        AdminPermission.USER_READ, AdminPermission.USER_WRITE, AdminPermission.USER_DELETE,
        AdminPermission.ADMIN_READ, AdminPermission.ADMIN_WRITE, AdminPermission.ADMIN_DELETE,
        AdminPermission.ROLE_READ, AdminPermission.ROLE_WRITE, AdminPermission.ROLE_DELETE,
        AdminPermission.AUDIT_READ, AdminPermission.AUDIT_WRITE,
        AdminPermission.SYSTEM_READ, AdminPermission.SYSTEM_WRITE,
        AdminPermission.FINANCIAL_READ, AdminPermission.FINANCIAL_WRITE,
        AdminPermission.COMPLIANCE_READ, AdminPermission.COMPLIANCE_WRITE,
        AdminPermission.CUSTOMER_SERVICE_READ, AdminPermission.CUSTOMER_SERVICE_WRITE
    )),
    
    SYSTEM_ADMIN(Set.of(
        AdminPermission.USER_READ, AdminPermission.USER_WRITE,
        AdminPermission.ADMIN_READ, AdminPermission.ADMIN_WRITE,
        AdminPermission.ROLE_READ, AdminPermission.ROLE_WRITE,
        AdminPermission.AUDIT_READ,
        AdminPermission.SYSTEM_READ, AdminPermission.SYSTEM_WRITE
    )),
    
    FINANCIAL_ADMIN(Set.of(
        AdminPermission.USER_READ,
        AdminPermission.AUDIT_READ,
        AdminPermission.SYSTEM_READ,
        AdminPermission.FINANCIAL_READ, AdminPermission.FINANCIAL_WRITE
    )),
    
    CUSTOMER_SERVICE_ADMIN(Set.of(
        AdminPermission.USER_READ, AdminPermission.USER_WRITE,
        AdminPermission.AUDIT_READ,
        AdminPermission.CUSTOMER_SERVICE_READ, AdminPermission.CUSTOMER_SERVICE_WRITE
    )),
    
    COMPLIANCE_OFFICER(Set.of(
        AdminPermission.USER_READ,
        AdminPermission.AUDIT_READ, AdminPermission.AUDIT_WRITE,
        AdminPermission.COMPLIANCE_READ, AdminPermission.COMPLIANCE_WRITE
    )),
    
    ANALYST(Set.of(
        AdminPermission.USER_READ,
        AdminPermission.AUDIT_READ,
        AdminPermission.SYSTEM_READ,
        AdminPermission.FINANCIAL_READ,
        AdminPermission.COMPLIANCE_READ,
        AdminPermission.CUSTOMER_SERVICE_READ
    ));

    private final Set<AdminPermission> permissions;

    AdminRole(Set<AdminPermission> permissions) {
        this.permissions = permissions;
    }

    public Set<AdminPermission> getPermissions() {
        return permissions;
    }

    public List<GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = permissions.stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }

    public boolean hasPermission(AdminPermission permission) {
        return permissions.contains(permission);
    }

    public String getDisplayName() {
        return switch (this) {
            case SUPER_ADMIN -> "Super Administrator";
            case SYSTEM_ADMIN -> "System Administrator";
            case FINANCIAL_ADMIN -> "Financial Administrator";
            case CUSTOMER_SERVICE_ADMIN -> "Customer Service Administrator";
            case COMPLIANCE_OFFICER -> "Compliance Officer";
            case ANALYST -> "Read-only Analyst";
        };
    }

    public String getDescription() {
        return switch (this) {
            case SUPER_ADMIN -> "Full system access and control";
            case SYSTEM_ADMIN -> "System administration and user management";
            case FINANCIAL_ADMIN -> "Financial product and transaction management";
            case CUSTOMER_SERVICE_ADMIN -> "Customer support and user assistance";
            case COMPLIANCE_OFFICER -> "Regulatory compliance and audit management";
            case ANALYST -> "Read-only access for analysis and reporting";
        };
    }
} 
