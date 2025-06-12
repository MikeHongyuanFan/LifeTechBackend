package com.finance.admin.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dashboard Summary Response DTO
 * Contains overview of clients data summary and key metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    // Client Statistics
    private Long totalClients;
    private Long activeClients;
    private Long inactiveClients;
    private Long newClientsThisMonth;
    private Long newClientsThisQuarter;
    private Double clientGrowthRate;

    // Investment Statistics
    private BigDecimal totalInvestmentValue;
    private Long totalInvestingClients;
    private BigDecimal averageInvestmentPerClient;
    private BigDecimal totalReturns;
    private Double averageReturnRate;
    private Long newInvestmentsThisPeriod;

    // Enquiry Statistics
    private Long totalEnquiries;
    private Long pendingEnquiries;
    private Long resolvedEnquiries;
    private Double enquiryResponseTime; // in hours
    private Double enquiryConversionRate;

    // Upcoming Events
    private Long upcomingBirthdays7Days;
    private Long upcomingBirthdays30Days;
    private List<UpcomingBirthdayClient> upcomingBirthdayClients;

    // System Information
    private LocalDateTime lastUpdated;
    private String systemStatus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingBirthdayClient {
        private Long clientId;
        private String clientName;
        private String email;
        private LocalDateTime birthday;
        private Integer daysUntilBirthday;
        private Boolean greetingSent;
    }
} 