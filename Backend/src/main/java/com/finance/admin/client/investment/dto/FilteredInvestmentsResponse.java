package com.finance.admin.client.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilteredInvestmentsResponse {
    
    private List<InvestmentSummaryItem> investments;
    private FilterSummary filterSummary;
    private PaginationInfo pagination;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvestmentSummaryItem {
        private Long id;
        private String investmentName;
        private String investmentType;
        private String investmentCategory;
        private String status;
        private String riskRating;
        private BigDecimal initialAmount;
        private BigDecimal currentValue;
        private BigDecimal returns;
        private BigDecimal returnPercentage;
        private LocalDate purchaseDate;
        private LocalDate maturityDate;
        private Integer daysToMaturity;
        private Boolean isMatured;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FilterSummary {
        private Integer totalCount;
        private BigDecimal totalAmount;
        private BigDecimal totalCurrentValue;
        private BigDecimal totalReturns;
        private BigDecimal averageReturnPercentage;
        private String appliedFilters;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaginationInfo {
        private Integer currentPage;
        private Integer totalPages;
        private Integer pageSize;
        private Long totalElements;
        private Boolean hasNext;
        private Boolean hasPrevious;
    }
} 