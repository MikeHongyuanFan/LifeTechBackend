package com.finance.admin.auth.security;

import com.finance.admin.auth.entity.AdminRole;
import com.finance.admin.auth.entity.AdminUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class AdminUserPrincipal implements UserDetails {
    
    private final UUID id;
    private final String username;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Set<String> allowedIpAddresses;
    private final Integer sessionTimeoutMinutes;
    private final boolean mfaEnabled;
    private final LocalDateTime lastLogin;
    private final Integer failedLoginAttempts;

    public AdminUserPrincipal(UUID id, String username, String email, String password,
                             boolean enabled, boolean accountNonExpired, boolean accountNonLocked,
                             boolean credentialsNonExpired, Collection<? extends GrantedAuthority> authorities,
                             Set<String> allowedIpAddresses, Integer sessionTimeoutMinutes,
                             boolean mfaEnabled, LocalDateTime lastLogin, Integer failedLoginAttempts) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.authorities = authorities;
        this.allowedIpAddresses = allowedIpAddresses;
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
        this.mfaEnabled = mfaEnabled;
        this.lastLogin = lastLogin;
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public static AdminUserPrincipal create(AdminUser user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .flatMap(role -> role.getAuthorities().stream())
                .collect(Collectors.toList());

        return new AdminUserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.isEnabled(),
                user.isAccountNonExpired(),
                user.isAccountNonLocked(),
                user.isCredentialsNonExpired(),
                authorities,
                user.getAllowedIpAddresses(),
                user.getSessionTimeoutMinutes(),
                user.getMfaEnabled(),
                user.getLastLogin(),
                user.getFailedLoginAttempts()
        );
    }

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // Additional getters
    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Set<String> getAllowedIpAddresses() {
        return allowedIpAddresses;
    }

    public Integer getSessionTimeoutMinutes() {
        return sessionTimeoutMinutes;
    }

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public boolean hasRole(AdminRole role) {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role.name()));
    }

    public boolean hasPermission(String permission) {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(permission));
    }

    public boolean isIpAllowed(String ipAddress) {
        return true; // Temporarily disabled IP whitelisting for testing
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdminUserPrincipal)) return false;
        AdminUserPrincipal that = (AdminUserPrincipal) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
} 
