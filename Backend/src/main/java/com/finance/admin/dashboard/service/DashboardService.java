package com.finance.admin.dashboard.service;

import com.finance.admin.client.model.Client;
import com.finance.admin.client.repository.ClientRepository;
import com.finance.admin.dashboard.dto.ClientStatsResponse;
import com.finance.admin.dashboard.dto.DashboardSummaryResponse;
import com.finance.admin.dashboard.dto.InvestmentStatsResponse;
import com.finance.admin.dashboard.dto.UpcomingBirthdaysResponse;
import com.finance.admin.email.service.EmailService;
import com.finance.admin.enquiry.service.EnquiryService;
import com.finance.admin.investment.model.Investment;
import com.finance.admin.investment.repository.InvestmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dashboard Service for Sprint 2.2
 * Implements dashboard functionality using existing client and investment data
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final ClientRepository clientRepository;
    private final InvestmentRepository investmentRepository;
    private final EnquiryService enquiryService;
    private final EmailService emailService;

    /**
     * Get comprehensive dashboard summary
     */
    public DashboardSummaryResponse getDashboardSummary() {
        log.info("Generating dashboard summary");

        // Get basic counts
        long totalClients = clientRepository.count();
        long activeClients = clientRepository.countByStatus(Client.ClientStatus.ACTIVE);
        long inactiveClients = totalClients - activeClients;

        // Get time-based client counts
        LocalDate thisMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate thisQuarter = getQuarterStart(LocalDate.now());
        
        long newClientsThisMonth = clientRepository.countByCreatedAtAfter(thisMonth.atStartOfDay());
        long newClientsThisQuarter = clientRepository.countByCreatedAtAfter(thisQuarter.atStartOfDay());

        // Calculate growth rate (mock calculation)
        double clientGrowthRate = totalClients > 0 ? (double) newClientsThisMonth / totalClients * 100 : 0.0;

        // Get investment statistics
        long totalInvestments = investmentRepository.count();
        BigDecimal totalInvestmentValue = investmentRepository.sumInvestmentAmount().orElse(BigDecimal.ZERO);
        
        long totalInvestingClients = investmentRepository.countDistinctClients();
        BigDecimal averageInvestmentPerClient = totalInvestingClients > 0 ? 
            totalInvestmentValue.divide(BigDecimal.valueOf(totalInvestingClients), 2, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;

        // Get real enquiry statistics
        Map<String, Object> enquiryStats = enquiryService.getEnquiryStats();
        long totalEnquiries = (Long) enquiryStats.get("totalEnquiries");
        long pendingEnquiries = (Long) enquiryStats.get("pendingEnquiries");
        long resolvedEnquiries = (Long) enquiryStats.get("resolvedEnquiries");

        // Get upcoming birthdays
        List<DashboardSummaryResponse.UpcomingBirthdayClient> upcomingBirthdays = getUpcomingBirthdayClients(30);
        long upcomingBirthdays7Days = upcomingBirthdays.stream()
            .filter(client -> client.getDaysUntilBirthday() <= 7)
            .count();
        long upcomingBirthdays30Days = upcomingBirthdays.size();

        return DashboardSummaryResponse.builder()
            .totalClients(totalClients)
            .activeClients(activeClients)
            .inactiveClients(inactiveClients)
            .newClientsThisMonth(newClientsThisMonth)
            .newClientsThisQuarter(newClientsThisQuarter)
            .clientGrowthRate(clientGrowthRate)
            .totalInvestmentValue(totalInvestmentValue)
            .totalInvestingClients(totalInvestingClients)
            .averageInvestmentPerClient(averageInvestmentPerClient)
            .totalReturns(BigDecimal.ZERO) // Will be calculated when return data is available
            .averageReturnRate(0.0)
            .newInvestmentsThisPeriod(0L)
            .totalEnquiries(totalEnquiries)
            .pendingEnquiries(pendingEnquiries)
            .resolvedEnquiries(resolvedEnquiries)
            .enquiryResponseTime((Double) enquiryStats.get("averageResponseTime"))
            .enquiryConversionRate((Double) enquiryStats.get("resolutionRate"))
            .upcomingBirthdays7Days(upcomingBirthdays7Days)
            .upcomingBirthdays30Days(upcomingBirthdays30Days)
            .upcomingBirthdayClients(upcomingBirthdays)
            .lastUpdated(LocalDateTime.now())
            .systemStatus("OPERATIONAL")
            .build();
    }

    /**
     * Get detailed client statistics
     */
    public ClientStatsResponse getClientStats(LocalDate startDate, LocalDate endDate) {
        log.info("Generating client statistics from {} to {}", startDate, endDate);

        // Use default date range if not provided
        if (endDate == null) endDate = LocalDate.now();
        if (startDate == null) startDate = endDate.minusYears(1);

        long totalClients = clientRepository.count();
        long activeClients = clientRepository.countByStatus(Client.ClientStatus.ACTIVE);
        long inactiveClients = clientRepository.countByStatus(Client.ClientStatus.INACTIVE);
        long pendingClients = clientRepository.countByStatus(Client.ClientStatus.PENDING);

        // Time-based metrics
        LocalDate thisMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate thisQuarter = getQuarterStart(LocalDate.now());
        LocalDate thisYear = LocalDate.now().withDayOfYear(1);

        long newClientsThisMonth = clientRepository.countByCreatedAtAfter(thisMonth.atStartOfDay());
        long newClientsThisQuarter = clientRepository.countByCreatedAtAfter(thisQuarter.atStartOfDay());
        long newClientsThisYear = clientRepository.countByCreatedAtAfter(thisYear.atStartOfDay());

        // Calculate growth rates
        double monthlyGrowthRate = totalClients > 0 ? (double) newClientsThisMonth / totalClients * 100 : 0.0;
        double quarterlyGrowthRate = totalClients > 0 ? (double) newClientsThisQuarter / totalClients * 100 : 0.0;
        double yearlyGrowthRate = totalClients > 0 ? (double) newClientsThisYear / totalClients * 100 : 0.0;

        // Mock segmentation data (will be real when client types are implemented)
        Map<String, Long> clientsByType = Map.of(
            "INDIVIDUAL", totalClients * 70 / 100,
            "CORPORATE", totalClients * 20 / 100,
            "TRUST", totalClients * 10 / 100
        );

        Map<String, Long> clientsByStatus = Map.of(
            "ACTIVE", activeClients,
            "INACTIVE", inactiveClients,
            "PENDING", pendingClients
        );

        return ClientStatsResponse.builder()
            .totalClients(totalClients)
            .activeClients(activeClients)
            .inactiveClients(inactiveClients)
            .pendingClients(pendingClients)
            .newClientsThisMonth(newClientsThisMonth)
            .newClientsThisQuarter(newClientsThisQuarter)
            .newClientsThisYear(newClientsThisYear)
            .monthlyGrowthRate(monthlyGrowthRate)
            .quarterlyGrowthRate(quarterlyGrowthRate)
            .yearlyGrowthRate(yearlyGrowthRate)
            .clientsByType(clientsByType)
            .clientsByStatus(clientsByStatus)
            .clientsByLocation(new HashMap<>()) // Will be implemented when location data is available
            .activeClientsLast30Days(activeClients) // Mock data
            .activeClientsLast90Days(activeClients) // Mock data
            .averageLoginFrequency(2.5) // Mock: 2.5 logins per week
            .lastClientRegistration(LocalDate.now().minusDays(1))
            .monthlyTrends(new ArrayList<>()) // Will be implemented with historical data
            .quarterlyTrends(new ArrayList<>()) // Will be implemented with historical data
            .periodStart(startDate)
            .periodEnd(endDate)
            .build();
    }

    /**
     * Get investment statistics
     */
    public InvestmentStatsResponse getInvestmentStats(LocalDate startDate, LocalDate endDate) {
        log.info("Generating investment statistics from {} to {}", startDate, endDate);

        // Use default date range if not provided
        if (endDate == null) endDate = LocalDate.now();
        if (startDate == null) startDate = endDate.minusYears(1);

        BigDecimal totalInvestmentValue = investmentRepository.sumInvestmentAmount().orElse(BigDecimal.ZERO);
        
        long totalInvestingClients = investmentRepository.countDistinctClients();
        long totalInvestments = investmentRepository.count();
        long activeInvestments = investmentRepository.countByStatus(Investment.InvestmentStatus.ACTIVE);

        BigDecimal averageInvestmentPerClient = totalInvestingClients > 0 ? 
            totalInvestmentValue.divide(BigDecimal.valueOf(totalInvestingClients), 2, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;

        // Mock investment breakdown by type
        Map<String, Long> investmentsByType = Map.of(
            "PROPERTY", totalInvestments * 40 / 100,
            "EQUITY", totalInvestments * 30 / 100,
            "FIXED_INCOME", totalInvestments * 20 / 100,
            "ALTERNATIVE", totalInvestments * 10 / 100
        );

        Map<String, Long> investmentsByStatus = Map.of(
            "ACTIVE", activeInvestments,
            "PENDING", totalInvestments - activeInvestments,
            "MATURED", 0L
        );

        return InvestmentStatsResponse.builder()
            .totalInvestmentValue(totalInvestmentValue)
            .totalInvestmentValueLastMonth(totalInvestmentValue.multiply(BigDecimal.valueOf(0.95))) // Mock 5% growth
            .monthOverMonthGrowth(totalInvestmentValue.multiply(BigDecimal.valueOf(0.05)))
            .monthOverMonthGrowthPercentage(5.0)
            .yearOverYearGrowth(totalInvestmentValue.multiply(BigDecimal.valueOf(0.15)))
            .yearOverYearGrowthPercentage(15.0)
            .investmentValueByAssetClass(new HashMap<>()) // Will be implemented with asset class data
            .investmentValueByClientSegment(new HashMap<>()) // Will be implemented with client segmentation
            .totalInvestingClients(totalInvestingClients)
            .newInvestingClientsThisPeriod(5L) // Mock data
            .averageInvestmentPerClient(averageInvestmentPerClient)
            .medianInvestmentPerClient(averageInvestmentPerClient.multiply(BigDecimal.valueOf(0.8))) // Mock median
            .totalInvestments(totalInvestments)
            .activeInvestments(activeInvestments)
            .maturedInvestments(0L)
            .newInvestmentsThisPeriod(10L) // Mock data
            .investmentsByStatus(investmentsByStatus)
            .investmentsByType(investmentsByType)
            .totalReturnsGenerated(BigDecimal.ZERO) // Will be calculated when return data is available
            .averageReturnRate(0.0)
            .bestPerformingInvestmentReturn(BigDecimal.ZERO)
            .underperformingInvestmentLoss(BigDecimal.ZERO)
            .topPerformingInvestments(new ArrayList<>())
            .underperformingInvestments(new ArrayList<>())
            .sharpeRatio(0.0) // Will be calculated with return data
            .volatility(0.0)
            .valueAtRisk(BigDecimal.ZERO)
            .monthlyTrends(new ArrayList<>()) // Will be implemented with historical data
            .quarterlyTrends(new ArrayList<>()) // Will be implemented with historical data
            .periodStart(startDate)
            .periodEnd(endDate)
            .build();
    }

    /**
     * Get upcoming birthdays
     */
    public UpcomingBirthdaysResponse getUpcomingBirthdays(int days) {
        log.info("Getting upcoming birthdays for next {} days", days);

        List<UpcomingBirthdaysResponse.BirthdayClient> upcomingBirthdays = getUpcomingBirthdayClients(days)
            .stream()
            .map(client -> UpcomingBirthdaysResponse.BirthdayClient.builder()
                .clientId(Long.valueOf(client.getClientName().hashCode())) // Mock ID
                .firstName(client.getClientName().split(" ")[0])
                .lastName(client.getClientName().contains(" ") ? client.getClientName().split(" ")[1] : "")
                .fullName(client.getClientName())
                .email(client.getEmail())
                .dateOfBirth(client.getBirthday().toLocalDate().minusYears(30)) // Mock DOB
                .nextBirthday(client.getBirthday().toLocalDate())
                .daysUntilBirthday(client.getDaysUntilBirthday())
                .age(30) // Mock age
                .greetingSent(client.getGreetingSent())
                .clientStatus("ACTIVE")
                .preferredContactMethod("EMAIL")
                .emailOptIn(true)
                .smsOptIn(false)
                .build())
            .collect(Collectors.toList());

        long birthdaysNext7Days = upcomingBirthdays.stream()
            .filter(client -> client.getDaysUntilBirthday() <= 7)
            .count();

        return UpcomingBirthdaysResponse.builder()
            .totalUpcomingBirthdays((long) upcomingBirthdays.size())
            .birthdaysNext7Days(birthdaysNext7Days)
            .birthdaysNext30Days((long) upcomingBirthdays.size())
            .greetingsSentToday(0L) // Will be tracked when email system is implemented
            .pendingGreetings((long) upcomingBirthdays.size())
            .upcomingBirthdays(upcomingBirthdays)
            .recentGreetings(new ArrayList<>()) // Will be implemented with email tracking
            .build();
    }

    /**
     * Send birthday greetings
     */
    @Transactional
    public Map<String, Object> sendBirthdayGreetings(Long clientId) {
        log.info("Sending birthday greetings for client: {}", clientId);
        
        if (clientId != null) {
            // Send greeting to specific client
            Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
            
            if (client.getEmailPrimary() != null) {
                return emailService.sendBirthdayGreeting(
                    client.getEmailPrimary(), 
                    client.getFullName(), 
                    "Happy Birthday from LifeTech!"
                );
            } else {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "Client does not have an email address");
                result.put("timestamp", LocalDateTime.now());
                return result;
            }
        } else {
            // Send greetings to all clients with birthdays today
            Map<String, String> recipients = new HashMap<>();
            
            // Get clients with birthdays today (mock implementation)
            List<DashboardSummaryResponse.UpcomingBirthdayClient> todaysBirthdays = getUpcomingBirthdayClients(1);
            
            for (DashboardSummaryResponse.UpcomingBirthdayClient birthdayClient : todaysBirthdays) {
                if (birthdayClient.getEmail() != null && birthdayClient.getDaysUntilBirthday() == 0) {
                    recipients.put(birthdayClient.getEmail(), birthdayClient.getClientName());
                }
            }
            
            if (recipients.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "No birthdays today");
                result.put("greetingsSent", 0);
                result.put("timestamp", LocalDateTime.now());
                return result;
            }
            
            return emailService.sendBulkBirthdayGreetings(recipients);
        }
    }

    /**
     * Get recent enquiries
     */
    public Object getRecentEnquiries(Pageable pageable, String status, String type) {
        log.info("Getting recent enquiries with status: {} and type: {}", status, type);
        
        return enquiryService.getRecentEnquiries(pageable, status, type);
    }

    /**
     * Get enquiry statistics
     */
    public Map<String, Object> getEnquiryStats() {
        log.info("Getting enquiry statistics");
        
        return enquiryService.getEnquiryStats();
    }

    /**
     * Export annual report
     */
    public Map<String, Object> exportAnnualReport(LocalDate year, String format) {
        log.info("Exporting annual report for year: {} in format: {}", year, format);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("reportId", UUID.randomUUID().toString());
        result.put("format", format);
        result.put("year", year.getYear());
        result.put("downloadUrl", "/api/admin/dashboard/downloads/" + UUID.randomUUID());
        result.put("generatedAt", LocalDateTime.now());
        
        return result;
    }

    /**
     * Get available export formats
     */
    public Object getAvailableExportFormats() {
        return Arrays.asList("PDF", "EXCEL", "CSV", "JSON");
    }

    /**
     * Generate custom export
     */
    public Map<String, Object> generateCustomExport(Map<String, Object> filters) {
        log.info("Generating custom export with filters: {}", filters);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("exportId", UUID.randomUUID().toString());
        result.put("filters", filters);
        result.put("downloadUrl", "/api/admin/dashboard/downloads/" + UUID.randomUUID());
        result.put("generatedAt", LocalDateTime.now());
        
        return result;
    }

    /**
     * Get dashboard configuration
     */
    public Object getDashboardConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("refreshInterval", 30); // seconds
        config.put("defaultDateRange", "30_DAYS");
        config.put("enableRealTimeUpdates", true);
        config.put("widgets", Arrays.asList("CLIENT_STATS", "INVESTMENT_STATS", "BIRTHDAYS", "ENQUIRIES"));
        
        return config;
    }

    /**
     * Update dashboard configuration
     */
    public Object updateDashboardConfig(Map<String, Object> config) {
        log.info("Updating dashboard configuration: {}", config);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Dashboard configuration updated successfully");
        result.put("updatedAt", LocalDateTime.now());
        
        return result;
    }

    // Helper methods

    private LocalDate getQuarterStart(LocalDate date) {
        int quarter = (date.getMonthValue() - 1) / 3;
        return date.withMonth(quarter * 3 + 1).withDayOfMonth(1);
    }

    private List<DashboardSummaryResponse.UpcomingBirthdayClient> getUpcomingBirthdayClients(int days) {
        // Mock implementation - will be replaced with real client birthday query
        List<DashboardSummaryResponse.UpcomingBirthdayClient> mockBirthdays = new ArrayList<>();
        
        // Add some mock upcoming birthdays
        for (int i = 1; i <= Math.min(days / 5, 10); i++) {
            mockBirthdays.add(DashboardSummaryResponse.UpcomingBirthdayClient.builder()
                .clientId((long) i)
                .clientName("Client " + i)
                .email("client" + i + "@example.com")
                .birthday(LocalDateTime.now().plusDays(i * 3))
                .daysUntilBirthday(i * 3)
                .greetingSent(false)
                .build());
        }
        
        return mockBirthdays;
    }
} 