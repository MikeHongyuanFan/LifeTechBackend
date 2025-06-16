package com.finance.admin.client.wallet.model;

import com.finance.admin.client.model.Client;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "client_wallet_integrations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ClientWalletIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "platform_name", nullable = false, length = 100)
    private String platformName;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform_type", nullable = false, length = 50)
    private PlatformType platformType;

    @Column(name = "account_identifier", length = 255)
    private String accountIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "integration_status", length = 20)
    @Builder.Default
    private IntegrationStatus integrationStatus = IntegrationStatus.CONNECTED;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @Column(name = "api_credentials_encrypted", columnDefinition = "TEXT")
    private String apiCredentialsEncrypted;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_frequency", length = 20)
    @Builder.Default
    private SyncFrequency syncFrequency = SyncFrequency.DAILY;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Version
    @Column(name = "version")
    private Long version;

    public enum PlatformType {
        BANK_ACCOUNT,
        BROKERAGE,
        CRYPTOCURRENCY,
        SUPERANNUATION,
        PROPERTY,
        INSURANCE,
        PEER_TO_PEER,
        OTHER
    }

    public enum IntegrationStatus {
        CONNECTED,
        DISCONNECTED,
        ERROR,
        PENDING,
        EXPIRED
    }

    public enum SyncFrequency {
        REAL_TIME,
        HOURLY,
        DAILY,
        WEEKLY,
        MANUAL
    }
} 