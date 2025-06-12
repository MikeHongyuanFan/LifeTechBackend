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
public class InvestmentChartsResponse {
    
    // Portfolio Value Over Time (Line Chart)
    private List<TimeSeriesDataPoint> portfolioValueTimeSeries;
    
    // Investment Allocation by Type (Pie Chart)
    private List<PieChartDataPoint> investmentAllocationByType;
    
    // Return Performance Comparison (Bar Chart)
    private List<BarChartDataPoint> returnPerformanceComparison;
    
    // Monthly/Quarterly Performance Trends (Line Chart)
    private List<TimeSeriesDataPoint> monthlyPerformanceTrends;
    private List<TimeSeriesDataPoint> quarterlyPerformanceTrends;
    
    // Asset Allocation Visualization (Pie Chart)
    private List<PieChartDataPoint> assetAllocationBreakdown;
    
    // Risk Distribution Chart
    private List<PieChartDataPoint> riskDistribution;
    
    // Performance Metrics for Charts
    private ChartMetrics chartMetrics;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeSeriesDataPoint {
        private LocalDate date;
        private BigDecimal value;
        private String label;
        private Map<String, Object> metadata;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PieChartDataPoint {
        private String label;
        private BigDecimal value;
        private BigDecimal percentage;
        private String color;
        private Integer count;
        private Map<String, Object> metadata;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BarChartDataPoint {
        private String label;
        private BigDecimal value;
        private BigDecimal comparisonValue;
        private String category;
        private String color;
        private Map<String, Object> metadata;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChartMetrics {
        private BigDecimal totalValue;
        private BigDecimal totalReturns;
        private BigDecimal averageMonthlyGrowth;
        private BigDecimal volatilityIndex;
        private LocalDate dataStartDate;
        private LocalDate dataEndDate;
        private Integer dataPointsCount;
        private BigDecimal highestValue;
        private BigDecimal lowestValue;
        private LocalDate highestValueDate;
        private LocalDate lowestValueDate;
    }
} 