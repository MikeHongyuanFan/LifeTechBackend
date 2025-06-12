package com.finance.admin.client.investment.controller;

import com.finance.admin.client.investment.dto.*;
import com.finance.admin.client.investment.service.ClientInvestmentService;
import com.finance.admin.investment.model.Investment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/client/investments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Client Investment Overview", description = "Client-side APIs for investment overview and management")
@PreAuthorize("hasRole('CLIENT')")
@SecurityRequirement(name = "bearerAuth")
public class ClientInvestmentController {

    private final ClientInvestmentService clientInvestmentService;

    @GetMapping("/summary")
    @Operation(summary = "Get investment summary", 
               description = "Get comprehensive investment summary including total invested amount, returns, and balance")
    public ResponseEntity<InvestmentSummaryResponse> getInvestmentSummary(Authentication authentication) {
        log.info("Client {} requesting investment summary", authentication.getName());
        
        Long clientId = extractClientId(authentication);
        InvestmentSummaryResponse summary = clientInvestmentService.getInvestmentSummary(clientId);
        
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/charts")
    @Operation(summary = "Get chart data", 
               description = "Get investment and return visualization data for charts and graphs")
    public ResponseEntity<InvestmentChartsResponse> getInvestmentCharts(
            Authentication authentication,
            @Parameter(description = "Time range for chart data") 
            @RequestParam(defaultValue = "12") int months) {
        
        log.info("Client {} requesting investment charts data for {} months", authentication.getName(), months);
        
        Long clientId = extractClientId(authentication);
        InvestmentChartsResponse charts = clientInvestmentService.getInvestmentCharts(clientId, months);
        
        return ResponseEntity.ok(charts);
    }

    @GetMapping("/performance")
    @Operation(summary = "Get performance metrics", 
               description = "Get detailed performance metrics and analytics")
    public ResponseEntity<InvestmentPerformanceResponse> getInvestmentPerformance(Authentication authentication) {
        log.info("Client {} requesting investment performance metrics", authentication.getName());
        
        Long clientId = extractClientId(authentication);
        InvestmentPerformanceResponse performance = clientInvestmentService.getInvestmentPerformance(clientId);
        
        return ResponseEntity.ok(performance);
    }

    @PostMapping("/filter")
    @Operation(summary = "Filter investments", 
               description = "Filter investments by time range, product type, status, and return type")
    public ResponseEntity<FilteredInvestmentsResponse> filterInvestments(
            Authentication authentication,
            @Valid @RequestBody InvestmentFilterRequest filterRequest) {
        
        log.info("Client {} filtering investments with criteria: {}", authentication.getName(), filterRequest);
        
        Long clientId = extractClientId(authentication);
        FilteredInvestmentsResponse filtered = clientInvestmentService.filterInvestments(clientId, filterRequest);
        
        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active investments", 
               description = "Get all ongoing investments with current performance data")
    public ResponseEntity<List<ActiveInvestmentResponse>> getActiveInvestments(Authentication authentication) {
        log.info("Client {} requesting active investments", authentication.getName());
        
        Long clientId = extractClientId(authentication);
        List<ActiveInvestmentResponse> activeInvestments = clientInvestmentService.getActiveInvestments(clientId);
        
        return ResponseEntity.ok(activeInvestments);
    }

    @GetMapping("/completed")
    @Operation(summary = "Get investment history", 
               description = "Get all completed investments with final performance results")
    public ResponseEntity<List<CompletedInvestmentResponse>> getCompletedInvestments(Authentication authentication) {
        log.info("Client {} requesting completed investments", authentication.getName());
        
        Long clientId = extractClientId(authentication);
        List<CompletedInvestmentResponse> completedInvestments = clientInvestmentService.getCompletedInvestments(clientId);
        
        return ResponseEntity.ok(completedInvestments);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all investments", 
               description = "Get complete investment portfolio overview with categorization")
    public ResponseEntity<AllInvestmentsResponse> getAllInvestments(Authentication authentication) {
        log.info("Client {} requesting all investments", authentication.getName());
        
        Long clientId = extractClientId(authentication);
        AllInvestmentsResponse allInvestments = clientInvestmentService.getAllInvestments(clientId);
        
        return ResponseEntity.ok(allInvestments);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get investment details", 
               description = "Get detailed information for a specific investment")
    public ResponseEntity<InvestmentDetailResponse> getInvestmentDetails(
            Authentication authentication,
            @Parameter(description = "Investment ID") @PathVariable Long id) {
        
        log.info("Client {} requesting details for investment {}", authentication.getName(), id);
        
        Long clientId = extractClientId(authentication);
        InvestmentDetailResponse details = clientInvestmentService.getInvestmentDetails(clientId, id);
        
        return ResponseEntity.ok(details);
    }

    @GetMapping("/reports")
    @Operation(summary = "Get available reports", 
               description = "Get list of available monthly and annual reports")
    public ResponseEntity<List<InvestmentReportResponse>> getAvailableReports(Authentication authentication) {
        log.info("Client {} requesting available reports", authentication.getName());
        
        Long clientId = extractClientId(authentication);
        List<InvestmentReportResponse> reports = clientInvestmentService.getAvailableReports(clientId);
        
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/reports/{reportId}/download")
    @Operation(summary = "Download report", 
               description = "Download a specific investment report in PDF format")
    public ResponseEntity<byte[]> downloadReport(
            Authentication authentication,
            @Parameter(description = "Report ID") @PathVariable Long reportId) {
        
        log.info("Client {} downloading report {}", authentication.getName(), reportId);
        
        Long clientId = extractClientId(authentication);
        return clientInvestmentService.downloadReport(clientId, reportId);
    }

    @PostMapping("/reports/generate")
    @Operation(summary = "Generate custom report", 
               description = "Generate a custom investment report for specified date range")
    public ResponseEntity<InvestmentReportResponse> generateReport(
            Authentication authentication,
            @Valid @RequestBody ReportGenerationRequest request) {
        
        log.info("Client {} generating custom report for period {} to {}", 
                authentication.getName(), request.getStartDate(), request.getEndDate());
        
        Long clientId = extractClientId(authentication);
        InvestmentReportResponse report = clientInvestmentService.generateReport(clientId, request);
        
        return ResponseEntity.ok(report);
    }

    // ================== Sprint 3.2 Enhanced Investment Management APIs ==================
    
    @PostMapping("/compare")
    @Operation(summary = "Compare investments", 
               description = "Compare multiple investments with detailed analytics and insights")
    public ResponseEntity<InvestmentComparisonResponse> compareInvestments(
            Authentication authentication,
            @Valid @RequestBody InvestmentComparisonRequest request) {
        
        log.info("Client {} comparing investments: {}", authentication.getName(), request.getInvestmentIds());
        
        Long clientId = extractClientId(authentication);
        InvestmentComparisonResponse comparison = clientInvestmentService.compareInvestments(clientId, request);
        
        return ResponseEntity.ok(comparison);
    }
    
    @GetMapping("/analytics")
    @Operation(summary = "Get investment analytics", 
               description = "Get comprehensive investment analytics including performance, risk, trends, and recommendations")
    public ResponseEntity<InvestmentAnalyticsResponse> getInvestmentAnalytics(Authentication authentication) {
        log.info("Client {} requesting investment analytics", authentication.getName());
        
        Long clientId = extractClientId(authentication);
        InvestmentAnalyticsResponse analytics = clientInvestmentService.getInvestmentAnalytics(clientId);
        
        return ResponseEntity.ok(analytics);
    }
    
    @PostMapping("/advanced-filter")
    @Operation(summary = "Advanced investment filtering", 
               description = "Advanced filtering with performance, risk, and custom metrics")
    public ResponseEntity<FilteredInvestmentsResponse> advancedFilterInvestments(
            Authentication authentication,
            @Valid @RequestBody EnhancedInvestmentFilterRequest filterRequest) {
        
        log.info("Client {} using advanced filtering with criteria: {}", authentication.getName(), filterRequest);
        
        Long clientId = extractClientId(authentication);
        FilteredInvestmentsResponse filtered = clientInvestmentService.advancedFilterInvestments(clientId, filterRequest);
        
        return ResponseEntity.ok(filtered);
    }
    
    @GetMapping("/active/detailed")
    @Operation(summary = "Get detailed active investments", 
               description = "Get active investments with enhanced performance tracking and analytics")
    public ResponseEntity<List<ActiveInvestmentResponse>> getDetailedActiveInvestments(
            Authentication authentication,
            @RequestParam(defaultValue = "false") Boolean includeProjections,
            @RequestParam(defaultValue = "false") Boolean includeRiskMetrics) {
        
        log.info("Client {} requesting detailed active investments", authentication.getName());
        
        Long clientId = extractClientId(authentication);
        List<ActiveInvestmentResponse> activeInvestments = clientInvestmentService
            .getDetailedActiveInvestments(clientId, includeProjections, includeRiskMetrics);
        
        return ResponseEntity.ok(activeInvestments);
    }
    
    @GetMapping("/history/analytics")
    @Operation(summary = "Get investment history with analytics", 
               description = "Get completed investments with performance analysis and lessons learned")
    public ResponseEntity<List<CompletedInvestmentResponse>> getInvestmentHistoryWithAnalytics(
            Authentication authentication,
            @RequestParam(defaultValue = "false") Boolean includePerformanceAnalysis,
            @RequestParam(defaultValue = "false") Boolean includeLessonsLearned) {
        
        log.info("Client {} requesting investment history with analytics", authentication.getName());
        
        Long clientId = extractClientId(authentication);
        List<CompletedInvestmentResponse> historyWithAnalytics = clientInvestmentService
            .getInvestmentHistoryWithAnalytics(clientId, includePerformanceAnalysis, includeLessonsLearned);
        
        return ResponseEntity.ok(historyWithAnalytics);
    }
    
    @GetMapping("/insights")
    @Operation(summary = "Get investment insights", 
               description = "Get AI-powered insights and recommendations for portfolio optimization")
    public ResponseEntity<List<InvestmentAnalyticsResponse.InvestmentInsight>> getInvestmentInsights(Authentication authentication) {
        log.info("Client {} requesting investment insights", authentication.getName());
        
        Long clientId = extractClientId(authentication);
        List<InvestmentAnalyticsResponse.InvestmentInsight> insights = clientInvestmentService.getInvestmentInsights(clientId);
        
        return ResponseEntity.ok(insights);
    }
    
    @GetMapping("/recommendations")
    @Operation(summary = "Get investment recommendations", 
               description = "Get personalized investment recommendations based on portfolio analysis")
    public ResponseEntity<List<InvestmentAnalyticsResponse.InvestmentRecommendation>> getInvestmentRecommendations(Authentication authentication) {
        log.info("Client {} requesting investment recommendations", authentication.getName());
        
        Long clientId = extractClientId(authentication);
        List<InvestmentAnalyticsResponse.InvestmentRecommendation> recommendations = 
            clientInvestmentService.getInvestmentRecommendations(clientId);
        
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/portfolio/optimization")
    @Operation(summary = "Get portfolio optimization suggestions", 
               description = "Get suggestions for optimizing portfolio allocation and performance")
    public ResponseEntity<InvestmentAnalyticsResponse> getPortfolioOptimization(Authentication authentication) {
        log.info("Client {} requesting portfolio optimization", authentication.getName());
        
        Long clientId = extractClientId(authentication);
        InvestmentAnalyticsResponse optimization = clientInvestmentService.getPortfolioOptimization(clientId);
        
        return ResponseEntity.ok(optimization);
    }
    
    @GetMapping("/{id}/tracking")
    @Operation(summary = "Get investment performance tracking", 
               description = "Get detailed performance tracking for a specific investment over time")
    public ResponseEntity<InvestmentDetailResponse> getInvestmentTracking(
            Authentication authentication,
            @Parameter(description = "Investment ID") @PathVariable Long id,
            @RequestParam(defaultValue = "12") int months) {
        
        log.info("Client {} requesting performance tracking for investment {} over {} months", 
                authentication.getName(), id, months);
        
        Long clientId = extractClientId(authentication);
        InvestmentDetailResponse tracking = clientInvestmentService.getInvestmentTracking(clientId, id, months);
        
        return ResponseEntity.ok(tracking);
    }
    
    @GetMapping("/alerts")
    @Operation(summary = "Get investment alerts", 
               description = "Get alerts for investments requiring attention or action")
    public ResponseEntity<List<InvestmentAlert>> getInvestmentAlerts(Authentication authentication) {
        log.info("Client {} requesting investment alerts", authentication.getName());
        
        Long clientId = extractClientId(authentication);
        List<InvestmentAlert> alerts = clientInvestmentService.getInvestmentAlerts(clientId);
        
        return ResponseEntity.ok(alerts);
    }

    // Utility method to extract client ID from authentication
    private Long extractClientId(Authentication authentication) {
        // This would typically extract the client ID from the JWT token or user details
        // For now, we'll assume the authentication name contains the client ID
        // This should be replaced with proper JWT token parsing in a real implementation
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            log.error("Failed to extract client ID from authentication: {}", authentication.getName());
            throw new IllegalArgumentException("Invalid client authentication");
        }
    }
} 