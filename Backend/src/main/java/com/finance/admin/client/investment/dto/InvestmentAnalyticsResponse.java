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
public class InvestmentAnalyticsResponse {
    
    private PerformanceAnalytics performance;
    private RiskAnalytics risk;
    private TrendAnalytics trends;
    private DiversificationAnalytics diversification;
    private List<InvestmentInsight> insights;
    private List<InvestmentRecommendation> recommendations;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PerformanceAnalytics {
        private BigDecimal totalPortfolioReturn;
        private BigDecimal annualizedReturn;
        private BigDecimal monthlyAverageReturn;
        private BigDecimal bestMonthReturn;
        private BigDecimal worstMonthReturn;
        private Integer positiveReturnMonths;
        private Integer negativeReturnMonths;
        private BigDecimal winLossRatio;
        private BigDecimal averageWin;
        private BigDecimal averageLoss;
        private String performanceTrend; // IMPROVING, DECLINING, STABLE
        private Map<String, BigDecimal> sectorPerformance;
        private List<PerformanceMetric> monthlyPerformance;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class PerformanceMetric {
            private LocalDate period;
            private BigDecimal returnAmount;
            private BigDecimal returnPercentage;
            private String performanceRating;
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RiskAnalytics {
        private BigDecimal portfolioVolatility;
        private BigDecimal valueAtRisk; // VaR at 95% confidence
        private BigDecimal conditionalValueAtRisk; // CVaR
        private BigDecimal maxDrawdown;
        private BigDecimal currentDrawdown;
        private Integer drawdownDuration;
        private BigDecimal sharpeRatio;
        private BigDecimal sortinoRatio;
        private BigDecimal calmarRatio;
        private String riskLevel; // LOW, MODERATE, HIGH, VERY_HIGH
        private Map<String, BigDecimal> riskByCategory;
        private RiskDistribution riskDistribution;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class RiskDistribution {
            private BigDecimal lowRiskPercentage;
            private BigDecimal moderateRiskPercentage;
            private BigDecimal highRiskPercentage;
            private String riskBalance; // BALANCED, CONSERVATIVE, AGGRESSIVE
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrendAnalytics {
        private String overallTrend; // UPWARD, DOWNWARD, SIDEWAYS
        private BigDecimal trendStrength; // 0-100 scale
        private List<TrendPeriod> trendPeriods;
        private SeasonalityAnalysis seasonality;
        private VolatilityTrend volatilityTrend;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class TrendPeriod {
            private LocalDate startDate;
            private LocalDate endDate;
            private String trendDirection;
            private BigDecimal trendReturn;
            private String description;
        }
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class SeasonalityAnalysis {
            private Map<String, BigDecimal> monthlyPatterns;
            private Map<String, BigDecimal> quarterlyPatterns;
            private String bestPerformingMonth;
            private String worstPerformingMonth;
            private String seasonalityInsight;
        }
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class VolatilityTrend {
            private String currentVolatilityLevel;
            private String volatilityTrend;
            private BigDecimal averageVolatility;
            private BigDecimal currentVolatility;
            private String volatilityInsight;
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DiversificationAnalytics {
        private BigDecimal diversificationRatio;
        private Integer numberOfInvestments;
        private Integer numberOfSectors;
        private BigDecimal concentrationIndex;
        private List<ConcentrationRisk> concentrationRisks;
        private CorrelationAnalysis correlation;
        private String diversificationGrade; // A+, A, B+, B, C+, C, D
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class ConcentrationRisk {
            private String category;
            private BigDecimal percentage;
            private String riskLevel;
            private String recommendation;
        }
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class CorrelationAnalysis {
            private BigDecimal averageCorrelation;
            private List<HighCorrelationPair> highCorrelations;
            private String correlationInsight;
            
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            public static class HighCorrelationPair {
                private String investment1;
                private String investment2;
                private BigDecimal correlation;
                private String riskImplication;
            }
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvestmentInsight {
        private String title;
        private String description;
        private String category; // PERFORMANCE, RISK, OPPORTUNITY, WARNING
        private String severity; // INFO, WARNING, CRITICAL
        private LocalDate identifiedDate;
        private List<String> affectedInvestments;
        private String recommendation;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvestmentRecommendation {
        private String title;
        private String description;
        private String action; // BUY, SELL, HOLD, REBALANCE
        private String priority; // HIGH, MEDIUM, LOW
        private String rationale;
        private BigDecimal potentialImpact;
        private LocalDate recommendationDate;
        private String targetInvestment;
        private BigDecimal suggestedAmount;
        private String timeframe; // IMMEDIATE, SHORT_TERM, LONG_TERM
    }
} 