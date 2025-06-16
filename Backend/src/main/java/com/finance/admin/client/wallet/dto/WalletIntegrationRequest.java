package com.finance.admin.client.wallet.dto;

import com.finance.admin.client.wallet.model.ClientWalletIntegration;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletIntegrationRequest {

    @NotBlank(message = "Platform name is required")
    private String platformName;

    @NotNull(message = "Platform type is required")
    private ClientWalletIntegration.PlatformType platformType;

    private String accountIdentifier;

    private String apiKey;
    private String apiSecret;
    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private ClientWalletIntegration.SyncFrequency syncFrequency = ClientWalletIntegration.SyncFrequency.DAILY;

    private String description;
} 