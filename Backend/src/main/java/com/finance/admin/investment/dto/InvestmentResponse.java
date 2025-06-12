package com.finance.admin.investment.dto;

import com.finance.admin.investment.model.Investment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentResponse {

    private Long id;
    private Long clientId;
    private String clientName;
    private Long entityId;
    private String entityName;
    private String investmentName;
    private Investment.InvestmentType investmentType;
    private String investmentTypeDisplayName;
    private String investmentCategory;
    private String description;
    private String investmentObjective;
    private Investment.RiskRating riskRating;
    private String riskRatingDisplayName;
    private BigDecimal initialAmount;
    private BigDecimal currentValue;
    private LocalDate purchaseDate;
    private LocalDate maturityDate;
    private BigDecimal expectedReturnRate;
    private BigDecimal expectedReturnAmount;
    private BigDecimal actualReturnAmount;
    private BigDecimal unitsPurchased;
    private BigDecimal purchasePricePerUnit;
    private BigDecimal currentPricePerUnit;
    private BigDecimal transactionFees;
    private BigDecimal managementFees;
    private BigDecimal performanceFees;
    private BigDecimal totalFees;
    private Investment.InvestmentStatus status;
    private String statusDisplayName;
    
    // Calculated fields
    private BigDecimal currentReturn;
    private BigDecimal currentReturnPercentage;
    private Boolean isMatured;
    private Boolean isActive;
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private String createdByName;
    private String updatedByName;
} 