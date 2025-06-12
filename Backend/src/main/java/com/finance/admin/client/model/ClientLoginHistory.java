package com.finance.admin.client.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "client_login_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ClientLoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    private Client client;

    @Column(name = "client_id", insertable = false, updatable = false)
    private Long clientId;

    @CreatedDate
    @Column(name = "login_timestamp", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime loginTimestamp = LocalDateTime.now();

    @Column(name = "logout_timestamp")
    private LocalDateTime logoutTimestamp;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "session_duration")
    private Duration sessionDuration;

    @Column(name = "login_successful")
    @Builder.Default
    private Boolean loginSuccessful = true;

    @Column(name = "failure_reason")
    private String failureReason;

    // Utility methods
    public void logout() {
        this.logoutTimestamp = LocalDateTime.now();
        if (this.loginTimestamp != null) {
            this.sessionDuration = Duration.between(this.loginTimestamp, this.logoutTimestamp);
        }
    }

    public boolean isActive() {
        return this.logoutTimestamp == null && Boolean.TRUE.equals(this.loginSuccessful);
    }

    public Duration getSessionDurationSoFar() {
        if (this.loginTimestamp == null) {
            return Duration.ZERO;
        }
        LocalDateTime endTime = this.logoutTimestamp != null ? this.logoutTimestamp : LocalDateTime.now();
        return Duration.between(this.loginTimestamp, endTime);
    }
} 