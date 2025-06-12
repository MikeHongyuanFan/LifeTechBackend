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
public class CompletedInvestmentResponse {
    
    private Long id;
    private String investmentName;
    private String investmentType;
    private String investmentCategory;
    private String description;
    private String riskRating;
    
    // Financial Details
    private BigDecimal initialAmount;
    private BigDecimal finalValue;
    private BigDecimal totalReturns;
    private BigDecimal totalReturnPercentage;
    private BigDecimal expectedReturnRate;
    private BigDecimal expectedReturnAmount;
    
    // Dates
    private LocalDate purchaseDate;
    private LocalDate maturityDate;
    private LocalDate completionDate;
    private Integer investmentDuration; // in days
    
    // Performance Analysis
    private PerformanceOutcome performanceOutcome;
    private BigDecimal performanceVsExpected;
    private BigDecimal annualizedReturn;
    
    // Exit Details
    private String exitReason;
    private String exitMethod; // "Maturity", "Early Exit", "Forced Exit"
    private BigDecimal exitFees;
    
    public enum PerformanceOutcome {
        EXCEEDED_EXPECTATIONS("Exceeded Expectations"),
        MET_EXPECTATIONS("Met Expectations"),
        BELOW_EXPECTATIONS("Below Expectations"),
        LOSS("Loss Incurred");
        
        private final String description;
        
        PerformanceOutcome(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 