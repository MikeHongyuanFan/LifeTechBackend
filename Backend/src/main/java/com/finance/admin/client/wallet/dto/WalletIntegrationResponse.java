package com.finance.admin.client.wallet.dto;

import com.finance.admin.client.wallet.model.ClientWalletIntegration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletIntegrationResponse {

    private Long id;
    private String platformName;
    private ClientWalletIntegration.PlatformType platformType;
    private String accountIdentifier;
    private ClientWalletIntegration.IntegrationStatus integrationStatus;
    private LocalDateTime lastSyncAt;
    private ClientWalletIntegration.SyncFrequency syncFrequency;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Summary information
    private String platformDisplayName;
    private String platformIcon;
    private String connectionStatusMessage;
    private Boolean needsReconnection;
    private LocalDateTime nextSyncAt;
} 