package com.finance.admin.client.investment.dto;

import com.finance.admin.investment.model.Investment;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentFilterRequest {
    
    // Time Range Filters
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
    
    private TimeRangePreset timeRangePreset;
    
    // Investment Product Type Filters
    private List<Investment.InvestmentType> investmentTypes;
    private List<String> investmentCategories;
    
    // Investment Status Filters
    private List<Investment.InvestmentStatus> investmentStatuses;
    
    // Return Type Filters
    private List<ReturnType> returnTypes;
    
    // Risk Rating Filters
    private List<Investment.RiskRating> riskRatings;
    
    // Amount Range Filters
    @Min(value = 0, message = "Minimum amount must be non-negative")
    private BigDecimal minAmount;
    
    @Min(value = 0, message = "Maximum amount must be non-negative")
    private BigDecimal maxAmount;
    
    // Return Performance Filters
    private BigDecimal minReturnPercentage;
    private BigDecimal maxReturnPercentage;
    
    // Maturity Filters
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate maturityStartDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate maturityEndDate;
    
    private Boolean includeMaturedInvestments;
    private Boolean includeActiveOnly;
    
    // Sorting Options
    private SortBy sortBy;
    private SortDirection sortDirection;
    
    // Pagination
    @Min(value = 0, message = "Page number must be non-negative")
    private Integer page;
    
    @Min(value = 1, message = "Page size must be at least 1")
    private Integer size;
    
    public enum TimeRangePreset {
        LAST_30_DAYS("Last 30 Days"),
        LAST_90_DAYS("Last 90 Days"),
        LAST_6_MONTHS("Last 6 Months"),
        LAST_12_MONTHS("Last 12 Months"),
        YEAR_TO_DATE("Year to Date"),
        LAST_YEAR("Last Year"),
        ALL_TIME("All Time"),
        CUSTOM("Custom Range");
        
        private final String displayName;
        
        TimeRangePreset(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum ReturnType {
        CAPITAL_GROWTH("Capital Growth"),
        INCOME("Income"),
        HYBRID("Hybrid");
        
        private final String displayName;
        
        ReturnType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum SortBy {
        PURCHASE_DATE("Purchase Date"),
        INVESTMENT_NAME("Investment Name"),
        INVESTMENT_TYPE("Investment Type"),
        INITIAL_AMOUNT("Initial Amount"),
        CURRENT_VALUE("Current Value"),
        RETURN_PERCENTAGE("Return Percentage"),
        MATURITY_DATE("Maturity Date"),
        STATUS("Status"),
        RISK_RATING("Risk Rating");
        
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
    
    // Utility method to get effective date range based on preset
    public LocalDate getEffectiveStartDate() {
        if (timeRangePreset != null && startDate == null) {
            return switch (timeRangePreset) {
                case LAST_30_DAYS -> LocalDate.now().minusDays(30);
                case LAST_90_DAYS -> LocalDate.now().minusDays(90);
                case LAST_6_MONTHS -> LocalDate.now().minusMonths(6);
                case LAST_12_MONTHS -> LocalDate.now().minusMonths(12);
                case YEAR_TO_DATE -> LocalDate.now().withDayOfYear(1);
                case LAST_YEAR -> LocalDate.now().minusYears(1).withDayOfYear(1);
                default -> null;
            };
        }
        return startDate;
    }
    
    public LocalDate getEffectiveEndDate() {
        if (timeRangePreset != null && endDate == null) {
            return switch (timeRangePreset) {
                case LAST_YEAR -> LocalDate.now().minusYears(1).withMonth(12).withDayOfMonth(31);
                case YEAR_TO_DATE -> LocalDate.now();
                default -> LocalDate.now();
            };
        }
        return endDate;
    }
} 