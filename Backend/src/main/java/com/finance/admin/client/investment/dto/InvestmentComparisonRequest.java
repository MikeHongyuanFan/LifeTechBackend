package com.finance.admin.client.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentComparisonRequest {
    
    @NotEmpty(message = "At least 2 investments must be selected for comparison")
    @Size(min = 2, max = 5, message = "Can compare between 2 to 5 investments at once")
    private List<Long> investmentIds;
    
    private ComparisonMetric primaryMetric;
    private List<ComparisonMetric> additionalMetrics;
    private ComparisonPeriod comparisonPeriod;
    private Boolean includeHistoricalData;
    private Boolean includeBenchmarkComparison;
    
    public enum ComparisonMetric {
        TOTAL_RETURN("Total Return"),
        RETURN_PERCENTAGE("Return Percentage"),
        ANNUALIZED_RETURN("Annualized Return"),
        VOLATILITY("Volatility"),
        SHARPE_RATIO("Sharpe Ratio"),
        RISK_ADJUSTED_RETURN("Risk Adjusted Return"),
        TIME_TO_MATURITY("Time to Maturity"),
        LIQUIDITY_SCORE("Liquidity Score"),
        INVESTMENT_AMOUNT("Investment Amount"),
        CURRENT_VALUE("Current Value");
        
        private final String displayName;
        
        ComparisonMetric(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum ComparisonPeriod {
        INCEPTION_TO_DATE("Since Inception"),
        LAST_12_MONTHS("Last 12 Months"),
        LAST_6_MONTHS("Last 6 Months"),
        LAST_3_MONTHS("Last 3 Months"),
        YEAR_TO_DATE("Year to Date"),
        CUSTOM_PERIOD("Custom Period");
        
        private final String displayName;
        
        ComparisonPeriod(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
} 