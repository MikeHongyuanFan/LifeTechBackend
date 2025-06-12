package com.finance.admin.dashboard.controller;

import com.finance.admin.dashboard.dto.DashboardSummaryResponse;
import com.finance.admin.dashboard.dto.ClientStatsResponse;
import com.finance.admin.dashboard.dto.InvestmentStatsResponse;
import com.finance.admin.dashboard.dto.UpcomingBirthdaysResponse;
import com.finance.admin.dashboard.service.DashboardService;
import com.finance.admin.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Dashboard Controller for Sprint 2.2
 * Provides overview of clients data summary, real-time visualization,
 * birthday congratulations, recent enquiries, filtering, and export functionality
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Get comprehensive dashboard summary
     * Includes client stats, investment stats, and upcoming birthdays
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary() {
        log.info("Fetching dashboard summary");
        
        DashboardSummaryResponse summary = dashboardService.getDashboardSummary();
        
        return ResponseEntity.ok(ApiResponse.success("Dashboard summary retrieved successfully", summary));
    }

    /**
     * Get detailed client statistics
     * Total clients, active/inactive counts, growth rates
     */
    @GetMapping("/clients/stats")
    public ResponseEntity<ApiResponse<ClientStatsResponse>> getClientStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching client statistics from {} to {}", startDate, endDate);
        
        ClientStatsResponse stats = dashboardService.getClientStats(startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success("Client statistics retrieved successfully", stats));
    }

    /**
     * Get investment statistics and real-time visualization data
     * Total investment value, client numbers, returns
     */
    @GetMapping("/investments/stats")
    public ResponseEntity<ApiResponse<InvestmentStatsResponse>> getInvestmentStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching investment statistics from {} to {}", startDate, endDate);
        
        InvestmentStatsResponse stats = dashboardService.getInvestmentStats(startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success("Investment statistics retrieved successfully", stats));
    }

    /**
     * Get upcoming birthdays for birthday congratulations
     */
    @GetMapping("/birthdays")
    public ResponseEntity<ApiResponse<UpcomingBirthdaysResponse>> getUpcomingBirthdays(
            @RequestParam(defaultValue = "30") int days) {
        
        log.info("Fetching upcoming birthdays for next {} days", days);
        
        UpcomingBirthdaysResponse birthdays = dashboardService.getUpcomingBirthdays(days);
        
        return ResponseEntity.ok(ApiResponse.success("Upcoming birthdays retrieved successfully", birthdays));
    }

    /**
     * Send birthday congratulations emails
     */
    @PostMapping("/birthdays/send-greetings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendBirthdayGreetings(
            @RequestParam(required = false) Long clientId) {
        
        log.info("Sending birthday greetings for client: {}", clientId);
        
        Map<String, Object> result = dashboardService.sendBirthdayGreetings(clientId);
        
        return ResponseEntity.ok(ApiResponse.success("Birthday greetings sent successfully", result));
    }

    /**
     * Get recent enquiries list in descending time order
     */
    @GetMapping("/enquiries/recent")
    public ResponseEntity<ApiResponse<Object>> getRecentEnquiries(
            Pageable pageable,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        
        log.info("Fetching recent enquiries with status: {} and type: {}", status, type);
        
        Object enquiries = dashboardService.getRecentEnquiries(pageable, status, type);
        
        return ResponseEntity.ok(ApiResponse.success("Recent enquiries retrieved successfully", enquiries));
    }

    /**
     * Get enquiry statistics
     */
    @GetMapping("/enquiries/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEnquiryStats() {
        log.info("Fetching enquiry statistics");
        
        Map<String, Object> stats = dashboardService.getEnquiryStats();
        
        return ResponseEntity.ok(ApiResponse.success("Enquiry statistics retrieved successfully", stats));
    }

    /**
     * Export annual investment report
     */
    @PostMapping("/export/annual-report")
    public ResponseEntity<ApiResponse<Map<String, Object>>> exportAnnualReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate year,
            @RequestParam(defaultValue = "PDF") String format) {
        
        log.info("Exporting annual report for year: {} in format: {}", year, format);
        
        Map<String, Object> result = dashboardService.exportAnnualReport(year, format);
        
        return ResponseEntity.ok(ApiResponse.success("Annual report exported successfully", result));
    }

    /**
     * Get available export formats
     */
    @GetMapping("/export/formats")
    public ResponseEntity<ApiResponse<Object>> getExportFormats() {
        log.info("Fetching available export formats");
        
        Object formats = dashboardService.getAvailableExportFormats();
        
        return ResponseEntity.ok(ApiResponse.success("Export formats retrieved successfully", formats));
    }

    /**
     * Generate custom export with filters
     */
    @PostMapping("/export/custom")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateCustomExport(
            @RequestBody Map<String, Object> filters) {
        
        log.info("Generating custom export with filters: {}", filters);
        
        Map<String, Object> result = dashboardService.generateCustomExport(filters);
        
        return ResponseEntity.ok(ApiResponse.success("Custom export generated successfully", result));
    }

    /**
     * Get dashboard configuration for user
     */
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<Object>> getDashboardConfig() {
        log.info("Fetching dashboard configuration");
        
        Object config = dashboardService.getDashboardConfig();
        
        return ResponseEntity.ok(ApiResponse.success("Dashboard configuration retrieved successfully", config));
    }

    /**
     * Update dashboard configuration
     */
    @PutMapping("/config")
    public ResponseEntity<ApiResponse<Object>> updateDashboardConfig(
            @RequestBody Map<String, Object> config) {
        
        log.info("Updating dashboard configuration");
        
        Object result = dashboardService.updateDashboardConfig(config);
        
        return ResponseEntity.ok(ApiResponse.success("Dashboard configuration updated successfully", result));
    }
} 