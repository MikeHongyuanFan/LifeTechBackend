package com.finance.admin.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.finance.admin.auth.security.AdminUserPrincipal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("requiresMfa")
    private boolean requiresMfa;
    
    @JsonProperty("mfaToken")
    private String mfaToken;
    
    @JsonProperty("accessToken")
    private String accessToken;
    
    @JsonProperty("refreshToken")
    private String refreshToken;
    
    @JsonProperty("tokenType")
    private String tokenType = "Bearer";
    
    @JsonProperty("expiresIn")
    private long expiresIn;
    
    @JsonProperty("user")
    private UserInfo user;

    public LoginResponse() {}

    public static LoginResponse success(String accessToken, String refreshToken, AdminUserPrincipal userPrincipal) {
        LoginResponse response = new LoginResponse();
        response.success = true;
        response.requiresMfa = false;
        response.accessToken = accessToken;
        response.refreshToken = refreshToken;
        response.expiresIn = 3600; // 1 hour in seconds
        response.user = new UserInfo(userPrincipal);
        return response;
    }

    public static LoginResponse mfaRequired(String mfaToken) {
        LoginResponse response = new LoginResponse();
        response.success = true;
        response.requiresMfa = true;
        response.mfaToken = mfaToken;
        return response;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isRequiresMfa() {
        return requiresMfa;
    }

    public void setRequiresMfa(boolean requiresMfa) {
        this.requiresMfa = requiresMfa;
    }

    public String getMfaToken() {
        return mfaToken;
    }

    public void setMfaToken(String mfaToken) {
        this.mfaToken = mfaToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfo {
        
        @JsonProperty("id")
        private UUID id;
        
        @JsonProperty("username")
        private String username;
        
        @JsonProperty("email")
        private String email;
        
        @JsonProperty("roles")
        private List<String> roles;
        
        @JsonProperty("permissions")
        private List<String> permissions;
        
        @JsonProperty("lastLogin")
        private LocalDateTime lastLogin;
        
        @JsonProperty("sessionTimeout")
        private Integer sessionTimeout;
        
        @JsonProperty("mfaEnabled")
        private boolean mfaEnabled;

        public UserInfo() {}

        public UserInfo(AdminUserPrincipal userPrincipal) {
            this.id = userPrincipal.getId();
            this.username = userPrincipal.getUsername();
            this.email = userPrincipal.getEmail();
            this.roles = userPrincipal.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .filter(auth -> auth.startsWith("ROLE_"))
                    .collect(Collectors.toList());
            this.permissions = userPrincipal.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .filter(auth -> !auth.startsWith("ROLE_"))
                    .collect(Collectors.toList());
            this.lastLogin = userPrincipal.getLastLogin();
            this.sessionTimeout = userPrincipal.getSessionTimeoutMinutes();
            this.mfaEnabled = userPrincipal.isMfaEnabled();
        }

        // Getters and Setters
        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

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

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }

        public List<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<String> permissions) {
            this.permissions = permissions;
        }

        public LocalDateTime getLastLogin() {
            return lastLogin;
        }

        public void setLastLogin(LocalDateTime lastLogin) {
            this.lastLogin = lastLogin;
        }

        public Integer getSessionTimeout() {
            return sessionTimeout;
        }

        public void setSessionTimeout(Integer sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
        }

        public boolean isMfaEnabled() {
            return mfaEnabled;
        }

        public void setMfaEnabled(boolean mfaEnabled) {
            this.mfaEnabled = mfaEnabled;
        }
    }
} 
