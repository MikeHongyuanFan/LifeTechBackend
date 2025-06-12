package com.finance.admin.client.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportGenerationRequest {
    
    private InvestmentReportResponse.ReportType reportType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String customReportName;
    private Boolean includeCharts;
    private Boolean includeDetailedBreakdown;
    private String fileFormat; // "PDF", "EXCEL", "CSV"
    
    // Optional filters for custom reports
    private List<String> investmentTypes;
    private List<String> riskRatings;
    private Boolean includeCompletedOnly;
    private Boolean includeActiveOnly;
} 