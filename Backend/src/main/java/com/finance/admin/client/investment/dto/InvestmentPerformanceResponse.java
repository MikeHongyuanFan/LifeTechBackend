package com.finance.admin.client.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentPerformanceResponse {
    
    // Overall Performance
    private BigDecimal portfolioReturn;
    private BigDecimal portfolioReturnPercentage;
    private BigDecimal portfolioVolatility;
    private BigDecimal sharpeRatio;
    
    // Benchmarking
    private BigDecimal marketBenchmarkReturn;
    private BigDecimal outperformanceVsBenchmark;
    private String performanceRating;
    
    // Risk Metrics
    private BigDecimal valueAtRisk;
    private BigDecimal maxDrawdown;
    private BigDecimal beta;
    private String riskProfile;
    
    // Time-based Performance
    private Map<String, BigDecimal> monthlyReturns;
    private Map<String, BigDecimal> quarterlyReturns;
    private Map<String, BigDecimal> yearlyReturns;
    
    // Asset Performance Ranking
    private List<AssetPerformanceRanking> topPerformers;
    private List<AssetPerformanceRanking> bottomPerformers;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssetPerformanceRanking {
        private String investmentName;
        private String investmentType;
        private BigDecimal returnPercentage;
        private BigDecimal absoluteReturn;
        private String performanceGrade;
    }
} 