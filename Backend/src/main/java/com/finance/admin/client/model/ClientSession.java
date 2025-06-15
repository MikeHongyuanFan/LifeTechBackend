package com.finance.admin.client.model;

import com.finance.admin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "client_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientSession extends BaseEntity {

    // ID is inherited from BaseEntity (UUID type)

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "session_token", unique = true, nullable = false)
    private String sessionToken;

    @Column(name = "remember_me_token", unique = true)
    private String rememberMeToken;

    @Column(name = "device_fingerprint")
    private String deviceFingerprint;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "remember_me_expires_at")
    private LocalDateTime rememberMeExpiresAt;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    // Relationship with Client
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private Client client;

    /**
     * Check if the session is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if the remember me token is expired
     */
    public boolean isRememberMeExpired() {
        return rememberMeExpiresAt != null && LocalDateTime.now().isAfter(rememberMeExpiresAt);
    }

    /**
     * Check if the session is valid (active and not expired)
     */
    public boolean isValid() {
        return isActive && !isExpired();
    }

    /**
     * Check if remember me is valid
     */
    public boolean isRememberMeValid() {
        return rememberMeToken != null && !isRememberMeExpired();
    }

    /**
     * Invalidate the session
     */
    public void invalidate() {
        this.isActive = false;
    }

    /**
     * Update last accessed time
     */
    public void updateLastAccessed() {
        this.lastAccessed = LocalDateTime.now();
    }

    /**
     * Create device fingerprint from user agent and IP
     */
    public void generateDeviceFingerprint() {
        if (userAgent != null && ipAddress != null) {
            this.deviceFingerprint = generateFingerprint(userAgent, ipAddress);
        }
    }

    private String generateFingerprint(String userAgent, String ipAddress) {
        // Simple fingerprint generation - in production, use more sophisticated method
        return Integer.toHexString((userAgent + ipAddress).hashCode());
    }
} 