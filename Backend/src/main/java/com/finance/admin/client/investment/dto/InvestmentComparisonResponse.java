package com.finance.admin.client.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentComparisonResponse {
    
    private List<InvestmentComparisonItem> investments;
    private ComparisonSummary summary;
    private List<ComparisonChart> charts;
    private BenchmarkComparison benchmark;
    private ComparisonInsights insights;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvestmentComparisonItem {
        private Long id;
        private String investmentName;
        private String investmentType;
        private String riskRating;
        
        // Financial Metrics
        private BigDecimal initialAmount;
        private BigDecimal currentValue;
        private BigDecimal totalReturn;
        private BigDecimal returnPercentage;
        private BigDecimal annualizedReturn;
        
        // Risk Metrics
        private BigDecimal volatility;
        private BigDecimal sharpeRatio;
        private BigDecimal maxDrawdown;
        private BigDecimal beta;
        
        // Time Metrics
        private LocalDate purchaseDate;
        private LocalDate maturityDate;
        private Integer daysHeld;
        private Integer daysToMaturity;
        
        // Performance Rankings
        private Integer returnRank;
        private Integer riskRank;
        private Integer overallRank;
        private String performanceGrade;
        
        // Additional Metrics
        private BigDecimal liquidityScore;
        private String investmentCategory;
        private Map<String, BigDecimal> customMetrics;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComparisonSummary {
        private InvestmentComparisonItem topPerformer;
        private InvestmentComparisonItem worstPerformer;
        private InvestmentComparisonItem lowestRisk;
        private InvestmentComparisonItem highestRisk;
        private BigDecimal averageReturn;
        private BigDecimal averageRisk;
        private String recommendedInvestment;
        private String comparisonInsight;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComparisonChart {
        private String chartType;
        private String title;
        private List<ChartDataSeries> dataSeries;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class ChartDataSeries {
            private String investmentName;
            private List<DataPoint> dataPoints;
            private String color;
            
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            public static class DataPoint {
                private LocalDate date;
                private BigDecimal value;
                private String label;
            }
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BenchmarkComparison {
        private String benchmarkName;
        private BigDecimal benchmarkReturn;
        private List<BenchmarkPerformance> investmentVsBenchmark;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class BenchmarkPerformance {
            private String investmentName;
            private BigDecimal outperformance;
            private Boolean outperformingBenchmark;
            private String performanceDescription;
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComparisonInsights {
        private List<String> keyFindings;
        private List<String> recommendations;
        private String riskAssessment;
        private String performanceTrend;
        private String diversificationAnalysis;
        private List<ActionableInsight> actionableInsights;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class ActionableInsight {
            private String title;
            private String description;
            private String action;
            private String priority; // HIGH, MEDIUM, LOW
            private String category; // PERFORMANCE, RISK, DIVERSIFICATION, COST
        }
    }
} 