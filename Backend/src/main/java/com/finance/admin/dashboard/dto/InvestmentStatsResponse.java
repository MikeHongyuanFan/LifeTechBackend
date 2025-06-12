package com.finance.admin.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Investment Statistics Response DTO
 * Contains real-time investment visualization data and metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentStatsResponse {

    // Total Investment Value
    private BigDecimal totalInvestmentValue;
    private BigDecimal totalInvestmentValueLastMonth;
    private BigDecimal monthOverMonthGrowth;
    private Double monthOverMonthGrowthPercentage;
    private BigDecimal yearOverYearGrowth;
    private Double yearOverYearGrowthPercentage;

    // Investment Value by Asset Class
    private Map<String, BigDecimal> investmentValueByAssetClass;
    private Map<String, BigDecimal> investmentValueByClientSegment;

    // Client Investment Statistics
    private Long totalInvestingClients;
    private Long newInvestingClientsThisPeriod;
    private BigDecimal averageInvestmentPerClient;
    private BigDecimal medianInvestmentPerClient;

    // Investment Activity
    private Long totalInvestments;
    private Long activeInvestments;
    private Long maturedInvestments;
    private Long newInvestmentsThisPeriod;
    private Map<String, Long> investmentsByStatus;
    private Map<String, Long> investmentsByType;

    // Returns Analysis
    private BigDecimal totalReturnsGenerated;
    private Double averageReturnRate;
    private BigDecimal bestPerformingInvestmentReturn;
    private BigDecimal underperformingInvestmentLoss;
    private List<TopPerformingInvestment> topPerformingInvestments;
    private List<UnderperformingInvestment> underperformingInvestments;

    // Risk-Adjusted Returns
    private Double sharpeRatio;
    private Double volatility;
    private BigDecimal valueAtRisk;

    // Trend Data for Visualization
    private List<InvestmentTrendData> monthlyTrends;
    private List<InvestmentTrendData> quarterlyTrends;

    // Analysis Period
    private LocalDate periodStart;
    private LocalDate periodEnd;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPerformingInvestment {
        private Long investmentId;
        private String investmentName;
        private String investmentType;
        private BigDecimal investmentAmount;
        private BigDecimal currentValue;
        private BigDecimal returns;
        private Double returnPercentage;
        private String clientName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnderperformingInvestment {
        private Long investmentId;
        private String investmentName;
        private String investmentType;
        private BigDecimal investmentAmount;
        private BigDecimal currentValue;
        private BigDecimal loss;
        private Double lossPercentage;
        private String clientName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvestmentTrendData {
        private String period; // "2024-01", "Q1-2024", etc.
        private BigDecimal totalValue;
        private Long totalInvestments;
        private BigDecimal newInvestments;
        private BigDecimal returns;
        private Double averageReturn;
    }
} 