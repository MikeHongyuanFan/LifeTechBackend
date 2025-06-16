package com.finance.admin.client.wallet.service;

import com.finance.admin.client.model.Client;
import com.finance.admin.client.repository.ClientRepository;
import com.finance.admin.client.service.BlockchainService;
import com.finance.admin.client.wallet.dto.*;
import com.finance.admin.client.wallet.model.ClientWalletIntegration;
import com.finance.admin.client.wallet.repository.ClientWalletIntegrationRepository;
import com.finance.admin.common.exception.ResourceNotFoundException;
import com.finance.admin.client.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClientWalletService {

    private final ClientWalletIntegrationRepository integrationRepository;
    private final ClientRepository clientRepository;
    private final EncryptionService encryptionService;
    private final BlockchainService blockchainService;

    /**
     * Get comprehensive wallet overview for client
     */
    @Transactional(readOnly = true)
    public WalletOverviewResponse getWalletOverview(Long clientId) {
        log.info("Getting wallet overview for client: {}", clientId);

        // Validate client exists
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + clientId));

        // Get all integrations
        List<ClientWalletIntegration> integrations = integrationRepository.findByClientIdAndIsActiveTrue(clientId);

        // Generate mock data for wallet overview
        WalletOverviewResponse.WalletSummary summary = generateMockWalletSummary(integrations);
        List<WalletOverviewResponse.PlatformBalance> platformBalances = generateMockPlatformBalances(integrations);
        List<WalletOverviewResponse.AssetAllocation> assetAllocations = generateMockAssetAllocations(summary);
        WalletOverviewResponse.PerformanceMetrics performance = generateMockPerformanceMetrics(summary);
        List<WalletOverviewResponse.RecentTransaction> recentTransactions = generateMockRecentTransactions();

        return WalletOverviewResponse.builder()
                .summary(summary)
                .platformBalances(platformBalances)
                .assetAllocations(assetAllocations)
                .performance(performance)
                .recentTransactions(recentTransactions)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * Get all platform balances for client
     */
    @Transactional(readOnly = true)
    public List<WalletOverviewResponse.PlatformBalance> getPlatformBalances(Long clientId) {
        log.info("Getting platform balances for client: {}", clientId);

        List<ClientWalletIntegration> integrations = integrationRepository.findByClientIdAndIsActiveTrue(clientId);
        return generateMockPlatformBalances(integrations);
    }

    /**
     * Get all wallet integrations for client
     */
    @Transactional(readOnly = true)
    public List<WalletIntegrationResponse> getWalletIntegrations(Long clientId) {
        log.info("Getting wallet integrations for client: {}", clientId);

        List<ClientWalletIntegration> integrations = integrationRepository.findByClientIdOrderByCreatedAtDesc(clientId);
        return integrations.stream()
                .map(this::mapToIntegrationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create new wallet integration
     */
    public WalletIntegrationResponse createWalletIntegration(Long clientId, WalletIntegrationRequest request) {
        log.info("Creating wallet integration for client: {} with platform: {}", clientId, request.getPlatformName());

        // Validate client exists
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + clientId));

        // Check if integration already exists
        Optional<ClientWalletIntegration> existingIntegration = integrationRepository
                .findByClientIdAndPlatformNameAndIsActiveTrue(clientId, request.getPlatformName());

        if (existingIntegration.isPresent()) {
            throw new RuntimeException("Integration with platform '" + request.getPlatformName() + "' already exists");
        }

        // Encrypt API credentials if provided
        String encryptedCredentials = null;
        if (request.getApiKey() != null || request.getApiSecret() != null) {
            Map<String, String> credentials = new HashMap<>();
            credentials.put("apiKey", request.getApiKey());
            credentials.put("apiSecret", request.getApiSecret());
            credentials.put("accessToken", request.getAccessToken());
            credentials.put("refreshToken", request.getRefreshToken());

            // In a real implementation, serialize and encrypt these credentials
            encryptedCredentials = encryptionService.encrypt(credentials.toString());
        }

        // Create integration
        ClientWalletIntegration integration = ClientWalletIntegration.builder()
                .client(client)
                .platformName(request.getPlatformName())
                .platformType(request.getPlatformType())
                .accountIdentifier(request.getAccountIdentifier())
                .integrationStatus(ClientWalletIntegration.IntegrationStatus.PENDING)
                .apiCredentialsEncrypted(encryptedCredentials)
                .syncFrequency(request.getSyncFrequency())
                .isActive(true)
                .build();

        // Simulate connection process
        simulateIntegrationConnection(integration);

        ClientWalletIntegration savedIntegration = integrationRepository.save(integration);

        // Log integration to blockchain for audit trail
        try {
            String integrationHash = generateIntegrationHash(savedIntegration);
            blockchainService.createAuditLog("WALLET_INTEGRATION_CREATED", clientId, integrationHash);
        } catch (Exception e) {
            log.warn("Failed to log wallet integration to blockchain for client: {}", clientId, e);
        }

        log.info("Wallet integration created successfully for client: {} with platform: {}", 
                clientId, request.getPlatformName());

        return mapToIntegrationResponse(savedIntegration);
    }

    /**
     * Remove wallet integration
     */
    public void removeWalletIntegration(Long clientId, Long integrationId) {
        log.info("Removing wallet integration {} for client: {}", integrationId, clientId);

        ClientWalletIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Integration not found with ID: " + integrationId));

        // Verify integration belongs to client
        if (!integration.getClient().getId().equals(clientId)) {
            throw new RuntimeException("Integration does not belong to client");
        }

        // Soft delete by setting inactive
        integration.setIsActive(false);
        integration.setIntegrationStatus(ClientWalletIntegration.IntegrationStatus.DISCONNECTED);
        integrationRepository.save(integration);

        // Log removal to blockchain
        try {
            String integrationHash = generateIntegrationHash(integration);
            blockchainService.createAuditLog("WALLET_INTEGRATION_REMOVED", clientId, integrationHash);
        } catch (Exception e) {
            log.warn("Failed to log wallet integration removal to blockchain for client: {}", clientId, e);
        }

        log.info("Wallet integration removed successfully for client: {}", clientId);
    }

    /**
     * Sync wallet data for client
     */
    public void syncWalletData(Long clientId) {
        log.info("Syncing wallet data for client: {}", clientId);

        List<ClientWalletIntegration> integrations = integrationRepository
                .findByClientIdAndIntegrationStatusAndIsActiveTrue(
                        clientId, ClientWalletIntegration.IntegrationStatus.CONNECTED);

        for (ClientWalletIntegration integration : integrations) {
            try {
                // Simulate data sync process
                simulateDataSync(integration);
                integration.setLastSyncAt(LocalDateTime.now());
                integrationRepository.save(integration);
                
                log.debug("Synced data for integration: {}", integration.getPlatformName());
            } catch (Exception e) {
                log.error("Failed to sync data for integration: {}", integration.getPlatformName(), e);
                integration.setIntegrationStatus(ClientWalletIntegration.IntegrationStatus.ERROR);
                integrationRepository.save(integration);
            }
        }

        log.info("Wallet data sync completed for client: {}", clientId);
    }

    private WalletOverviewResponse.WalletSummary generateMockWalletSummary(List<ClientWalletIntegration> integrations) {
        // Mock implementation - will be expanded
        return WalletOverviewResponse.WalletSummary.builder()
                .totalValue(BigDecimal.valueOf(100000))
                .totalCash(BigDecimal.valueOf(15000))
                .totalInvestments(BigDecimal.valueOf(50000))
                .totalShares(BigDecimal.valueOf(25000))
                .totalCryptocurrency(BigDecimal.valueOf(5000))
                .totalProperty(BigDecimal.valueOf(5000))
                .totalSuperannuation(BigDecimal.valueOf(0))
                .dayChange(BigDecimal.valueOf(500))
                .dayChangePercentage(BigDecimal.valueOf(0.5))
                .connectedPlatforms(integrations.size())
                .totalAssets(10)
                .build();
    }

    private List<WalletOverviewResponse.PlatformBalance> generateMockPlatformBalances(List<ClientWalletIntegration> integrations) {
        // Mock implementation - will be expanded
        return new ArrayList<>();
    }

    private List<WalletOverviewResponse.AssetAllocation> generateMockAssetAllocations(WalletOverviewResponse.WalletSummary summary) {
        // Mock implementation - will be expanded
        return new ArrayList<>();
    }

    private WalletOverviewResponse.PerformanceMetrics generateMockPerformanceMetrics(WalletOverviewResponse.WalletSummary summary) {
        // Mock implementation - will be expanded
        return WalletOverviewResponse.PerformanceMetrics.builder()
                .totalReturn(BigDecimal.valueOf(10000))
                .totalReturnPercentage(BigDecimal.valueOf(10))
                .dayReturn(BigDecimal.valueOf(500))
                .dayReturnPercentage(BigDecimal.valueOf(0.5))
                .build();
    }

    private List<WalletOverviewResponse.RecentTransaction> generateMockRecentTransactions() {
        // Mock implementation - will be expanded
        return new ArrayList<>();
    }

    private WalletIntegrationResponse mapToIntegrationResponse(ClientWalletIntegration integration) {
        return WalletIntegrationResponse.builder()
                .id(integration.getId())
                .platformName(integration.getPlatformName())
                .platformType(integration.getPlatformType())
                .accountIdentifier(maskAccountIdentifier(integration.getAccountIdentifier()))
                .integrationStatus(integration.getIntegrationStatus())
                .lastSyncAt(integration.getLastSyncAt())
                .syncFrequency(integration.getSyncFrequency())
                .isActive(integration.getIsActive())
                .createdAt(integration.getCreatedAt())
                .updatedAt(integration.getUpdatedAt())
                .platformDisplayName(getPlatformDisplayName(integration.getPlatformName()))
                .platformIcon(getPlatformIcon(integration.getPlatformName()))
                .connectionStatusMessage(getConnectionStatusMessage(integration.getIntegrationStatus()))
                .needsReconnection(integration.getIntegrationStatus() == ClientWalletIntegration.IntegrationStatus.ERROR)
                .nextSyncAt(calculateNextSyncTime(integration))
                .build();
    }

    private void simulateIntegrationConnection(ClientWalletIntegration integration) {
        // Simulate connection success/failure based on platform type
        Random random = new Random();
        
        if (random.nextDouble() > 0.1) { // 90% success rate
            integration.setIntegrationStatus(ClientWalletIntegration.IntegrationStatus.CONNECTED);
            integration.setLastSyncAt(LocalDateTime.now());
        } else {
            integration.setIntegrationStatus(ClientWalletIntegration.IntegrationStatus.ERROR);
        }
    }

    private void simulateDataSync(ClientWalletIntegration integration) {
        // Simulate data sync process
        Random random = new Random();
        
        if (random.nextDouble() > 0.05) { // 95% success rate
            // Successful sync - no action needed
        } else {
            throw new RuntimeException("Simulated sync failure");
        }
    }

    private String generateIntegrationHash(ClientWalletIntegration integration) {
        String data = integration.getClient().getId() + ":" + 
                     integration.getPlatformName() + ":" + 
                     integration.getPlatformType() + ":" + 
                     LocalDateTime.now();
        return data.hashCode() + "";
    }

    private String maskAccountIdentifier(String accountIdentifier) {
        if (accountIdentifier == null || accountIdentifier.length() < 4) {
            return accountIdentifier;
        }
        return "****" + accountIdentifier.substring(accountIdentifier.length() - 4);
    }

    private String getPlatformDisplayName(String platformName) {
        // Map platform names to display names
        Map<String, String> displayNames = Map.of(
                "commbank", "Commonwealth Bank",
                "westpac", "Westpac Banking",
                "anz", "ANZ Banking",
                "nab", "National Australia Bank",
                "commsec", "CommSec Trading",
                "nabtrade", "NAB Trade"
        );
        return displayNames.getOrDefault(platformName.toLowerCase(), platformName);
    }

    private String getPlatformIcon(String platformName) {
        // Return icon identifier for frontend
        return platformName.toLowerCase().replaceAll("\\s+", "_") + "_icon";
    }

    private String getConnectionStatusMessage(ClientWalletIntegration.IntegrationStatus status) {
        switch (status) {
            case CONNECTED:
                return "Connected and syncing";
            case DISCONNECTED:
                return "Disconnected";
            case ERROR:
                return "Connection error - please reconnect";
            case PENDING:
                return "Connection in progress";
            case EXPIRED:
                return "Authentication expired";
            default:
                return "Unknown status";
        }
    }

    private LocalDateTime calculateNextSyncTime(ClientWalletIntegration integration) {
        if (integration.getIntegrationStatus() != ClientWalletIntegration.IntegrationStatus.CONNECTED) {
            return null;
        }

        LocalDateTime lastSync = integration.getLastSyncAt() != null ? 
                integration.getLastSyncAt() : LocalDateTime.now();

        switch (integration.getSyncFrequency()) {
            case REAL_TIME:
                return lastSync.plusMinutes(5);
            case HOURLY:
                return lastSync.plusHours(1);
            case DAILY:
                return lastSync.plusDays(1);
            case WEEKLY:
                return lastSync.plusWeeks(1);
            default:
                return null;
        }
    }
} 