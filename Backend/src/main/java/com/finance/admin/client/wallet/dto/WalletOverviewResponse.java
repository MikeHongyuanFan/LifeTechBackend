package com.finance.admin.client.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletOverviewResponse {

    private WalletSummary summary;
    private List<PlatformBalance> platformBalances;
    private List<AssetAllocation> assetAllocations;
    private PerformanceMetrics performance;
    private List<RecentTransaction> recentTransactions;
    private LocalDateTime lastUpdated;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalletSummary {
        private BigDecimal totalValue;
        private BigDecimal totalCash;
        private BigDecimal totalInvestments;
        private BigDecimal totalShares;
        private BigDecimal totalCryptocurrency;
        private BigDecimal totalProperty;
        private BigDecimal totalSuperannuation;
        private BigDecimal dayChange;
        private BigDecimal dayChangePercentage;
        private int connectedPlatforms;
        private int totalAssets;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlatformBalance {
        private Long integrationId;
        private String platformName;
        private String platformType;
        private BigDecimal balance;
        private String currency;
        private BigDecimal changeAmount;
        private BigDecimal changePercentage;
        private String accountIdentifier;
        private LocalDateTime lastSync;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetAllocation {
        private String assetType;
        private BigDecimal value;
        private BigDecimal percentage;
        private String currency;
        private BigDecimal changeAmount;
        private BigDecimal changePercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics {
        private BigDecimal totalReturn;
        private BigDecimal totalReturnPercentage;
        private BigDecimal dayReturn;
        private BigDecimal dayReturnPercentage;
        private BigDecimal weekReturn;
        private BigDecimal weekReturnPercentage;
        private BigDecimal monthReturn;
        private BigDecimal monthReturnPercentage;
        private BigDecimal yearReturn;
        private BigDecimal yearReturnPercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentTransaction {
        private String transactionId;
        private String type;
        private String description;
        private BigDecimal amount;
        private String currency;
        private String platformName;
        private LocalDateTime transactionDate;
        private String status;
    }
} 