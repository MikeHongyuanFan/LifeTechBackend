package com.finance.admin.client.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllInvestmentsResponse {
    
    private List<ActiveInvestmentResponse> activeInvestments;
    private List<CompletedInvestmentResponse> completedInvestments;
    private List<FilteredInvestmentsResponse.InvestmentSummaryItem> pendingInvestments;
    
    private InvestmentSummaryResponse portfolioSummary;
    private InvestmentCategorization categorization;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvestmentCategorization {
        private CategoryBreakdown byType;
        private CategoryBreakdown byRisk;
        private CategoryBreakdown byStatus;
        private CategoryBreakdown byMaturity;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryBreakdown {
        private String categoryName;
        private Integer count;
        private java.math.BigDecimal totalValue;
        private java.math.BigDecimal percentage;
    }
} 