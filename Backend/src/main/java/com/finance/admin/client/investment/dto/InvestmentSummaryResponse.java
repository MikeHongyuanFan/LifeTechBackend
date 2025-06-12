package com.finance.admin.client.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentSummaryResponse {
    
    // Core Summary Metrics
    private BigDecimal totalInvestedAmount;
    private BigDecimal totalCurrentValue;
    private BigDecimal totalReturns;
    private BigDecimal totalBalance;
    private BigDecimal netProfitLoss;
    private BigDecimal overallReturnPercentage;
    
    // Investment Count Statistics
    private Integer totalInvestments;
    private Integer activeInvestments;
    private Integer completedInvestments;
    private Integer pendingInvestments;
    
    // Performance Metrics
    private BigDecimal averageReturnRate;
    private String bestPerformingInvestment;
    private BigDecimal bestPerformanceReturn;
    private String worstPerformingInvestment;
    private BigDecimal worstPerformanceReturn;
    
    // Time-based Analysis
    private BigDecimal portfolioGrowthThisMonth;
    private BigDecimal portfolioGrowthThisYear;
    private BigDecimal monthlyAverageReturn;
    private BigDecimal yearlyAverageReturn;
    
    // Risk Distribution
    private Map<String, RiskDistribution> riskDistribution;
    private Map<String, AssetAllocation> assetAllocation;
    private Map<String, StatusDistribution> statusDistribution;
    
    // Recent Activity
    private LocalDateTime lastInvestmentDate;
    private LocalDateTime lastReturnDate;
    private Integer investmentsMaturityNext30Days;
    private Integer investmentsMaturityNext90Days;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RiskDistribution {
        private Integer count;
        private BigDecimal amount;
        private BigDecimal percentage;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssetAllocation {
        private Integer count;
        private BigDecimal amount;
        private BigDecimal percentage;
        private BigDecimal currentValue;
        private BigDecimal returns;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusDistribution {
        private Integer count;
        private BigDecimal amount;
        private BigDecimal percentage;
    }
} 