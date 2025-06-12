package com.finance.admin.client.investment.dto;

import com.finance.admin.investment.model.Investment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnhancedInvestmentFilterRequest {
    
    // Basic Filters (inherited from original filter)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
    
    private List<Investment.InvestmentType> investmentTypes;
    private List<Investment.InvestmentStatus> investmentStatuses;
    private List<Investment.RiskRating> riskRatings;
    
    // Enhanced Financial Filters
    @Min(value = 0, message = "Minimum amount must be non-negative")
    private BigDecimal minInitialAmount;
    private BigDecimal maxInitialAmount;
    
    private BigDecimal minCurrentValue;
    private BigDecimal maxCurrentValue;
    
    @Min(value = -100, message = "Minimum return percentage cannot be less than -100%")
    @Max(value = 1000, message = "Maximum return percentage cannot exceed 1000%")
    private BigDecimal minReturnPercentage;
    private BigDecimal maxReturnPercentage;
    
    // Performance Filters
    private PerformanceRating performanceRating;
    private List<String> performanceCategories;
    
    // Risk-Based Filters
    private BigDecimal minSharpeRatio;
    private BigDecimal maxVolatility;
    private BigDecimal maxDrawdown;
    
    // Time-Based Filters
    private Integer minDaysHeld;
    private Integer maxDaysHeld;
    private Integer daysToMaturityMin;
    private Integer daysToMaturityMax;
    
    // Maturity and Duration Filters
    private Boolean approachingMaturity; // Within 90 days
    private Boolean longTermInvestments; // > 1 year
    private Boolean shortTermInvestments; // < 1 year
    
    // Category and Sector Filters
    private List<String> investmentCategories;
    private List<String> sectors;
    private List<String> geographicRegions;
    
    // Advanced Analytics Filters
    private Boolean outperformingBenchmark;
    private Boolean underperformingExpectations;
    private Boolean highVolatilityInvestments;
    private Boolean correlatedInvestments;
    
    // Portfolio Composition Filters
    private BigDecimal minPortfolioWeight; // Minimum % of total portfolio
    private BigDecimal maxPortfolioWeight; // Maximum % of total portfolio
    
    // Fee and Cost Filters
    private BigDecimal maxTotalFees;
    private BigDecimal maxManagementFees;
    private BigDecimal maxPerformanceFees;
    
    // Liquidity Filters
    private LiquidityLevel liquidityLevel;
    private Boolean liquidInvestmentsOnly;
    
    // ESG and Sustainability Filters
    private Boolean esgCompliant;
    private List<String> sustainabilityRatings;
    
    // Custom Metrics Filters
    private List<CustomMetricFilter> customMetrics;
    
    // Sorting and Display Options
    private SortBy sortBy;
    private SortDirection sortDirection;
    private List<SortBy> secondarySortBy;
    
    // Pagination
    @Min(value = 0, message = "Page number must be non-negative")
    private Integer page;
    
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private Integer size;
    
    // View Options
    private ViewType viewType;
    private Boolean includeHistoricalData;
    private Boolean includeProjections;
    private Boolean includeBenchmarkComparison;
    
    public enum PerformanceRating {
        EXCELLENT("Excellent"),
        GOOD("Good"),
        AVERAGE("Average"),
        POOR("Poor"),
        CRITICAL("Critical");
        
        private final String displayName;
        
        PerformanceRating(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum LiquidityLevel {
        HIGH("High Liquidity"),
        MEDIUM("Medium Liquidity"),
        LOW("Low Liquidity"),
        ILLIQUID("Illiquid");
        
        private final String displayName;
        
        LiquidityLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum SortBy {
        PURCHASE_DATE("Purchase Date"),
        MATURITY_DATE("Maturity Date"),
        INVESTMENT_NAME("Investment Name"),
        INVESTMENT_TYPE("Investment Type"),
        INITIAL_AMOUNT("Initial Amount"),
        CURRENT_VALUE("Current Value"),
        RETURN_AMOUNT("Return Amount"),
        RETURN_PERCENTAGE("Return Percentage"),
        ANNUALIZED_RETURN("Annualized Return"),
        SHARPE_RATIO("Sharpe Ratio"),
        VOLATILITY("Volatility"),
        RISK_RATING("Risk Rating"),
        PORTFOLIO_WEIGHT("Portfolio Weight"),
        DAYS_TO_MATURITY("Days to Maturity"),
        PERFORMANCE_RANKING("Performance Ranking"),
        LAST_UPDATED("Last Updated");
        
        private final String displayName;
        
        SortBy(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum SortDirection {
        ASC("Ascending"),
        DESC("Descending");
        
        private final String displayName;
        
        SortDirection(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum ViewType {
        SUMMARY("Summary View"),
        DETAILED("Detailed View"),
        COMPARISON("Comparison View"),
        ANALYTICS("Analytics View");
        
        private final String displayName;
        
        ViewType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomMetricFilter {
        private String metricName;
        private String operator; // EQUALS, GREATER_THAN, LESS_THAN, BETWEEN
        private BigDecimal value;
        private BigDecimal minValue;
        private BigDecimal maxValue;
    }
} 