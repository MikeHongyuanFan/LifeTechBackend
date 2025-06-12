package com.finance.admin.client.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiveInvestmentResponse {
    
    private Long id;
    private String investmentName;
    private String investmentType;
    private String investmentCategory;
    private String description;
    private String riskRating;
    
    // Financial Details
    private BigDecimal initialAmount;
    private BigDecimal currentValue;
    private BigDecimal currentReturns;
    private BigDecimal currentReturnPercentage;
    private BigDecimal expectedReturnRate;
    private BigDecimal expectedReturnAmount;
    
    // Dates
    private LocalDate purchaseDate;
    private LocalDate maturityDate;
    private Integer daysToMaturity;
    private Integer daysSincePurchase;
    
    // Performance Indicators
    private PerformanceStatus performanceStatus;
    private BigDecimal performanceVsExpected;
    private String performanceIndicator; // "Outperforming", "Meeting Expectations", "Underperforming"
    
    // Risk Metrics
    private BigDecimal volatilityIndex;
    private String riskLevel;
    
    // Additional Info
    private String status;
    private Boolean isApproachingMaturity; // True if within 90 days of maturity
    private String maturityAlert;
    
    public enum PerformanceStatus {
        EXCELLENT("Excellent - Significantly outperforming expectations"),
        GOOD("Good - Meeting or exceeding expectations"),
        AVERAGE("Average - Performing close to expectations"),
        POOR("Poor - Underperforming expectations"),
        CRITICAL("Critical - Significantly underperforming");
        
        private final String description;
        
        PerformanceStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 