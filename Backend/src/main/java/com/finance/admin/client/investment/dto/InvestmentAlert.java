package com.finance.admin.client.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentAlert {
    
    private Long id;
    private String title;
    private String message;
    private AlertType alertType;
    private AlertSeverity severity;
    private AlertCategory category;
    private LocalDateTime alertDate;
    private Boolean isRead;
    private Boolean isActionRequired;
    
    // Investment Details
    private Long investmentId;
    private String investmentName;
    private String investmentType;
    
    // Alert Specific Data
    private BigDecimal thresholdValue;
    private BigDecimal currentValue;
    private String recommendedAction;
    private LocalDateTime actionDeadline;
    
    // Additional Context
    private String detailedDescription;
    private String impact; // LOW, MEDIUM, HIGH, CRITICAL
    private String[] suggestedActions;
    
    public enum AlertType {
        PERFORMANCE_WARNING("Performance Warning"),
        MATURITY_APPROACHING("Maturity Approaching"),
        HIGH_VOLATILITY("High Volatility Detected"),
        UNDERPERFORMING("Underperforming Investment"),
        RISK_THRESHOLD("Risk Threshold Exceeded"),
        OPPORTUNITY("Investment Opportunity"),
        COMPLIANCE_ISSUE("Compliance Issue"),
        MARKET_CHANGE("Market Change Impact"),
        REBALANCING_NEEDED("Portfolio Rebalancing Needed"),
        DIVIDEND_PAYMENT("Dividend/Return Payment"),
        DOCUMENT_REQUIRED("Document Required"),
        PRICE_ALERT("Price Alert");
        
        private final String displayName;
        
        AlertType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum AlertSeverity {
        INFO("Information"),
        LOW("Low Priority"),
        MEDIUM("Medium Priority"),
        HIGH("High Priority"),
        CRITICAL("Critical");
        
        private final String displayName;
        
        AlertSeverity(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum AlertCategory {
        PERFORMANCE("Performance"),
        RISK("Risk Management"),
        OPPORTUNITY("Opportunities"),
        COMPLIANCE("Compliance"),
        ADMINISTRATIVE("Administrative"),
        MARKET("Market Related");
        
        private final String displayName;
        
        AlertCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
} 