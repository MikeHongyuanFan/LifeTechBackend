package com.finance.admin.client.wallet.repository;

import com.finance.admin.client.wallet.model.ClientWalletIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientWalletIntegrationRepository extends JpaRepository<ClientWalletIntegration, Long> {

    /**
     * Find all integrations for a specific client
     */
    List<ClientWalletIntegration> findByClientIdAndIsActiveTrue(Long clientId);

    /**
     * Find all integrations for a client including inactive ones
     */
    List<ClientWalletIntegration> findByClientIdOrderByCreatedAtDesc(Long clientId);

    /**
     * Find integration by client and platform
     */
    Optional<ClientWalletIntegration> findByClientIdAndPlatformNameAndIsActiveTrue(Long clientId, String platformName);

    /**
     * Find integrations by platform type
     */
    List<ClientWalletIntegration> findByClientIdAndPlatformTypeAndIsActiveTrue(
            Long clientId, ClientWalletIntegration.PlatformType platformType);

    /**
     * Find integrations by status
     */
    List<ClientWalletIntegration> findByClientIdAndIntegrationStatusAndIsActiveTrue(
            Long clientId, ClientWalletIntegration.IntegrationStatus status);

    /**
     * Find integrations that need syncing
     */
    @Query("SELECT wi FROM ClientWalletIntegration wi WHERE wi.client.id = :clientId " +
           "AND wi.isActive = true " +
           "AND wi.integrationStatus = 'CONNECTED' " +
           "AND (wi.lastSyncAt IS NULL OR wi.lastSyncAt < :syncThreshold)")
    List<ClientWalletIntegration> findIntegrationsNeedingSync(
            @Param("clientId") Long clientId, 
            @Param("syncThreshold") LocalDateTime syncThreshold);

    /**
     * Count active integrations for a client
     */
    long countByClientIdAndIsActiveTrue(Long clientId);

    /**
     * Check if client has integration with specific platform
     */
    boolean existsByClientIdAndPlatformNameAndIsActiveTrue(Long clientId, String platformName);

    /**
     * Find all connected integrations across all clients (for admin purposes)
     */
    @Query("SELECT wi FROM ClientWalletIntegration wi WHERE wi.integrationStatus = 'CONNECTED' AND wi.isActive = true")
    List<ClientWalletIntegration> findAllConnectedIntegrations();
} 