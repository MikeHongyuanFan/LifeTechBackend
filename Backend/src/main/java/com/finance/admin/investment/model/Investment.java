package com.finance.admin.investment.model;

import com.finance.admin.client.model.Client;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@jakarta.persistence.Entity
@Table(name = "investments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id")
    private com.finance.admin.investment.model.Entity entity;

    @Column(name = "investment_name", nullable = false)
    private String investmentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_type", nullable = false)
    private InvestmentType investmentType;

    @Column(name = "investment_category")
    private String investmentCategory;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "investment_objective", columnDefinition = "TEXT")
    private String investmentObjective;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_rating")
    private RiskRating riskRating;

    @Column(name = "initial_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal initialAmount;

    @Column(name = "current_value", precision = 15, scale = 2)
    private BigDecimal currentValue;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "expected_return_rate", precision = 5, scale = 2)
    private BigDecimal expectedReturnRate;

    @Column(name = "expected_return_amount", precision = 15, scale = 2)
    private BigDecimal expectedReturnAmount;

    @Column(name = "actual_return_amount", precision = 15, scale = 2)
    private BigDecimal actualReturnAmount;

    @Column(name = "units_purchased", precision = 15, scale = 4)
    private BigDecimal unitsPurchased;

    @Column(name = "purchase_price_per_unit", precision = 15, scale = 4)
    private BigDecimal purchasePricePerUnit;

    @Column(name = "current_price_per_unit", precision = 15, scale = 4)
    private BigDecimal currentPricePerUnit;

    @Column(name = "transaction_fees", precision = 10, scale = 2)
    private BigDecimal transactionFees;

    @Column(name = "management_fees", precision = 10, scale = 2)
    private BigDecimal managementFees;

    @Column(name = "performance_fees", precision = 10, scale = 2)
    private BigDecimal performanceFees;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private InvestmentStatus status = InvestmentStatus.ACTIVE;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by")
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    // Utility methods
    public BigDecimal getTotalFees() {
        BigDecimal total = BigDecimal.ZERO;
        if (transactionFees != null) total = total.add(transactionFees);
        if (managementFees != null) total = total.add(managementFees);
        if (performanceFees != null) total = total.add(performanceFees);
        return total;
    }

    public BigDecimal getCurrentReturn() {
        if (currentValue != null && initialAmount != null) {
            return currentValue.subtract(initialAmount);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getCurrentReturnPercentage() {
        if (currentValue != null && initialAmount != null && initialAmount.compareTo(BigDecimal.ZERO) > 0) {
            return getCurrentReturn().divide(initialAmount, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }

    public boolean isActive() {
        return InvestmentStatus.ACTIVE.equals(this.status);
    }

    public boolean isMatured() {
        return maturityDate != null && LocalDate.now().isAfter(maturityDate);
    }

    // Investment Types Enum
    public enum InvestmentType {
        PROPERTY_RESIDENTIAL("Property - Residential"),
        PROPERTY_COMMERCIAL("Property - Commercial"), 
        PROPERTY_INDUSTRIAL("Property - Industrial"),
        EQUITY_LISTED_SHARES("Equity - Listed Shares"),
        EQUITY_PRIVATE("Equity - Private Equity"),
        FIXED_INCOME_BONDS("Fixed Income - Bonds"),
        FIXED_INCOME_TERM_DEPOSITS("Fixed Income - Term Deposits"),
        FIXED_INCOME_GOVERNMENT("Fixed Income - Government Securities"),
        ALTERNATIVE_COMMODITIES("Alternative - Commodities"),
        ALTERNATIVE_CRYPTOCURRENCY("Alternative - Cryptocurrency"),
        ALTERNATIVE_COLLECTIBLES("Alternative - Collectibles"),
        MANAGED_FUNDS_MUTUAL("Managed Funds - Mutual Funds"),
        MANAGED_FUNDS_ETF("Managed Funds - ETFs"),
        MANAGED_FUNDS_HEDGE("Managed Funds - Hedge Funds"),
        DIRECT_BUSINESS("Direct - Business Investments"),
        DIRECT_STARTUPS("Direct - Startups");

        private final String displayName;

        InvestmentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Risk Rating Enum
    public enum RiskRating {
        CONSERVATIVE("Conservative"),
        MODERATE("Moderate"),
        AGGRESSIVE("Aggressive");

        private final String displayName;

        RiskRating(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Investment Status Enum
    public enum InvestmentStatus {
        PENDING("Investment committed but not yet funded"),
        ACTIVE("Investment is live and generating returns"),
        MATURED("Investment has reached maturity/completion"),
        PARTIAL_EXIT("Partial divestment has occurred"),
        FULLY_EXITED("Investment completely divested"),
        SUSPENDED("Investment temporarily halted"),
        DEFAULTED("Investment has failed/defaulted");

        private final String description;

        InvestmentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
} 