package com.finance.admin.investment.dto;

import com.finance.admin.investment.model.Investment;
import jakarta.validation.constraints.*;
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
public class CreateInvestmentRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    private Long entityId;

    @NotBlank(message = "Investment name is required")
    @Size(max = 255, message = "Investment name must not exceed 255 characters")
    private String investmentName;

    @NotNull(message = "Investment type is required")
    private Investment.InvestmentType investmentType;

    @Size(max = 100, message = "Investment category must not exceed 100 characters")
    private String investmentCategory;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 1000, message = "Investment objective must not exceed 1000 characters")
    private String investmentObjective;

    private Investment.RiskRating riskRating;

    @NotNull(message = "Initial amount is required")
    @DecimalMin(value = "0.01", message = "Initial amount must be greater than zero")
    @Digits(integer = 13, fraction = 2, message = "Initial amount must have at most 13 integer digits and 2 decimal places")
    private BigDecimal initialAmount;

    @DecimalMin(value = "0.0", message = "Current value must be non-negative")
    @Digits(integer = 13, fraction = 2, message = "Current value must have at most 13 integer digits and 2 decimal places")
    private BigDecimal currentValue;

    @NotNull(message = "Purchase date is required")
    @PastOrPresent(message = "Purchase date cannot be in the future")
    private LocalDate purchaseDate;

    @Future(message = "Maturity date must be in the future")
    private LocalDate maturityDate;

    @DecimalMin(value = "0.0", message = "Expected return rate must be non-negative")
    @DecimalMax(value = "100.0", message = "Expected return rate cannot exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Expected return rate must have at most 3 integer digits and 2 decimal places")
    private BigDecimal expectedReturnRate;

    @DecimalMin(value = "0.0", message = "Expected return amount must be non-negative")
    @Digits(integer = 13, fraction = 2, message = "Expected return amount must have at most 13 integer digits and 2 decimal places")
    private BigDecimal expectedReturnAmount;

    @DecimalMin(value = "0.0", message = "Units purchased must be non-negative")
    @Digits(integer = 11, fraction = 4, message = "Units purchased must have at most 11 integer digits and 4 decimal places")
    private BigDecimal unitsPurchased;

    @DecimalMin(value = "0.0", message = "Purchase price per unit must be non-negative")
    @Digits(integer = 11, fraction = 4, message = "Purchase price per unit must have at most 11 integer digits and 4 decimal places")
    private BigDecimal purchasePricePerUnit;

    @DecimalMin(value = "0.0", message = "Transaction fees must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Transaction fees must have at most 8 integer digits and 2 decimal places")
    private BigDecimal transactionFees;

    @DecimalMin(value = "0.0", message = "Management fees must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Management fees must have at most 8 integer digits and 2 decimal places")
    private BigDecimal managementFees;

    @DecimalMin(value = "0.0", message = "Performance fees must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Performance fees must have at most 8 integer digits and 2 decimal places")
    private BigDecimal performanceFees;

    private Investment.InvestmentStatus status;
} 