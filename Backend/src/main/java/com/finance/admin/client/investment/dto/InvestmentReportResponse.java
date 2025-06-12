package com.finance.admin.client.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentReportResponse {
    
    private Long id;
    private String reportName;
    private ReportType reportType;
    private ReportPeriod reportPeriod;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDateTime generatedDate;
    private String fileFormat;
    private Long fileSizeBytes;
    private String downloadUrl;
    private Boolean isDownloaded;
    private Integer downloadCount;
    private LocalDateTime lastDownloadDate;
    
    // Report Summary
    private ReportSummary summary;
    
    public enum ReportType {
        MONTHLY_STATEMENT("Monthly Investment Statement"),
        ANNUAL_SUMMARY("Annual Tax Summary Report"),
        PERFORMANCE_ANALYSIS("Performance Analysis Report"),
        PORTFOLIO_COMPOSITION("Portfolio Composition Report"),
        TRANSACTION_HISTORY("Transaction History Report"),
        TAX_SUMMARY("Tax Summary Report"),
        COMPLIANCE_REPORT("Compliance Report"),
        CUSTOM_REPORT("Custom Report");
        
        private final String displayName;
        
        ReportType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum ReportPeriod {
        MONTHLY("Monthly"),
        QUARTERLY("Quarterly"),
        SEMI_ANNUAL("Semi-Annual"),
        ANNUAL("Annual"),
        CUSTOM("Custom Period");
        
        private final String displayName;
        
        ReportPeriod(String displayName) {
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
    public static class ReportSummary {
        private Integer totalInvestments;
        private BigDecimal totalValue;
        private BigDecimal totalReturns;
        private BigDecimal returnPercentage;
        private String reportDescription;
    }
} 