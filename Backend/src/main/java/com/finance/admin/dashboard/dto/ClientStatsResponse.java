package com.finance.admin.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Client Statistics Response DTO
 * Contains detailed client statistics and analytics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientStatsResponse {

    // Basic Counts
    private Long totalClients;
    private Long activeClients;
    private Long inactiveClients;
    private Long pendingClients;

    // Growth Metrics
    private Long newClientsThisMonth;
    private Long newClientsThisQuarter;
    private Long newClientsThisYear;
    private Double monthlyGrowthRate;
    private Double quarterlyGrowthRate;
    private Double yearlyGrowthRate;

    // Client Segmentation
    private Map<String, Long> clientsByType; // Individual, Corporate, Trust, etc.
    private Map<String, Long> clientsByStatus;
    private Map<String, Long> clientsByLocation;

    // Activity Metrics
    private Long activeClientsLast30Days;
    private Long activeClientsLast90Days;
    private Double averageLoginFrequency;
    private LocalDate lastClientRegistration;

    // Trend Data
    private List<ClientTrendData> monthlyTrends;
    private List<ClientTrendData> quarterlyTrends;

    // Analysis Period
    private LocalDate periodStart;
    private LocalDate periodEnd;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientTrendData {
        private String period; // "2024-01", "Q1-2024", etc.
        private Long newClients;
        private Long totalClients;
        private Long activeClients;
        private Double growthRate;
    }
} 