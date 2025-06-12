package com.finance.admin.client.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentDetailResponse {
    
    // Basic Information
    private Long id;
    private String investmentName;
    private String investmentType;
    private String investmentCategory;
    private String description;
    private String investmentObjective;
    private String riskRating;
    private String status;
    
    // Financial Details
    private BigDecimal initialAmount;
    private BigDecimal currentValue;
    private BigDecimal expectedReturnRate;
    private BigDecimal expectedReturnAmount;
    private BigDecimal actualReturnAmount;
    private BigDecimal currentReturnPercentage;
    
    // Unit Information
    private BigDecimal unitsPurchased;
    private BigDecimal purchasePricePerUnit;
    private BigDecimal currentPricePerUnit;
    
    // Fee Information
    private BigDecimal transactionFees;
    private BigDecimal managementFees;
    private BigDecimal performanceFees;
    private BigDecimal totalFees;
    
    // Date Information
    private LocalDate purchaseDate;
    private LocalDate maturityDate;
    private Integer daysToMaturity;
    private Integer daysSincePurchase;
    
    // Performance Metrics
    private List<PerformanceDataPoint> performanceHistory;
    private PerformanceMetrics performanceMetrics;
    
    // Audit Information
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PerformanceDataPoint {
        private LocalDate date;
        private BigDecimal value;
        private BigDecimal returnAmount;
        private BigDecimal returnPercentage;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PerformanceMetrics {
        private BigDecimal volatility;
        private BigDecimal sharpeRatio;
        private BigDecimal annualizedReturn;
        private BigDecimal maxDrawdown;
        private BigDecimal beta;
        private String performanceGrade;
        private String riskAdjustedRating;
    }
} 