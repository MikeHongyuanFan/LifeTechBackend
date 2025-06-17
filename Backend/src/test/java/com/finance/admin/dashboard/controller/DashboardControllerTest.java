package com.finance.admin.dashboard.controller;

import com.finance.admin.dashboard.dto.ClientStatsResponse;
import com.finance.admin.dashboard.dto.DashboardSummaryResponse;
import com.finance.admin.dashboard.dto.InvestmentStatsResponse;
import com.finance.admin.dashboard.dto.UpcomingBirthdaysResponse;
import com.finance.admin.dashboard.service.DashboardService;
import com.finance.admin.config.BaseUnitTest;
import com.finance.admin.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for DashboardController
 * Tests all dashboard management endpoints with various scenarios
 */
@DisplayName("Dashboard Controller Tests")
public class DashboardControllerTest extends BaseUnitTest {

    private MockMvc mockMvc;
    
    @Mock
    private DashboardService dashboardService;
    
    private DashboardController dashboardController;
    
    // Test data
    private DashboardSummaryResponse mockSummaryResponse;
    private ClientStatsResponse mockClientStatsResponse;
    private InvestmentStatsResponse mockInvestmentStatsResponse;
    private UpcomingBirthdaysResponse mockBirthdaysResponse;

    @BeforeEach
    protected void setUp() {
        super.setUp();
        MockitoAnnotations.openMocks(this);
        
        dashboardController = new DashboardController(dashboardService);
        
        // Setup MockMvc with PageableHandlerMethodArgumentResolver and GlobalExceptionHandler
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        // Setup test data
        setupTestData();
    }

    private void setupTestData() {
        // Setup mock summary response
        mockSummaryResponse = DashboardSummaryResponse.builder()
                .totalClients(150L)
                .activeClients(140L)
                .inactiveClients(10L)
                .newClientsThisMonth(15L)
                .newClientsThisQuarter(45L)
                .clientGrowthRate(10.0)
                .totalInvestmentValue(new BigDecimal("2500000.00"))
                .totalInvestingClients(120L)
                .averageInvestmentPerClient(new BigDecimal("20833.33"))
                .totalReturns(new BigDecimal("125000.00"))
                .averageReturnRate(5.0)
                .newInvestmentsThisPeriod(8L)
                .totalEnquiries(50L)
                .pendingEnquiries(12L)
                .resolvedEnquiries(35L)
                .enquiryResponseTime(2.5)
                .enquiryConversionRate(70.0)
                .upcomingBirthdays7Days(3L)
                .upcomingBirthdays30Days(12L)
                .upcomingBirthdayClients(createMockBirthdayClients())
                .lastUpdated(LocalDateTime.now())
                .systemStatus("OPERATIONAL")
                .build();

        // Setup mock client stats response
        mockClientStatsResponse = ClientStatsResponse.builder()
                .totalClients(150L)
                .activeClients(140L)
                .inactiveClients(8L)
                .pendingClients(2L)
                .newClientsThisMonth(15L)
                .newClientsThisQuarter(45L)
                .newClientsThisYear(120L)
                .monthlyGrowthRate(10.0)
                .quarterlyGrowthRate(30.0)
                .yearlyGrowthRate(400.0)
                .clientsByType(Map.of("INDIVIDUAL", 105L, "CORPORATE", 30L, "TRUST", 15L))
                .clientsByStatus(Map.of("ACTIVE", 140L, "INACTIVE", 8L, "PENDING", 2L))
                .clientsByLocation(new HashMap<>())
                .activeClientsLast30Days(138L)
                .activeClientsLast90Days(142L)
                .averageLoginFrequency(2.5)
                .lastClientRegistration(LocalDate.now().minusDays(1))
                .monthlyTrends(new ArrayList<>())
                .quarterlyTrends(new ArrayList<>())
                .periodStart(LocalDate.now().minusYears(1))
                .periodEnd(LocalDate.now())
                .build();

        // Setup mock investment stats response
        mockInvestmentStatsResponse = InvestmentStatsResponse.builder()
                .totalInvestmentValue(new BigDecimal("2500000.00"))
                .totalInvestmentValueLastMonth(new BigDecimal("2400000.00"))
                .monthOverMonthGrowth(new BigDecimal("100000.00"))
                .monthOverMonthGrowthPercentage(4.17)
                .yearOverYearGrowth(new BigDecimal("500000.00"))
                .yearOverYearGrowthPercentage(25.0)
                .investmentValueByAssetClass(Map.of("PROPERTY", new BigDecimal("1000000"), "EQUITY", new BigDecimal("750000")))
                .investmentValueByClientSegment(Map.of("HIGH_NET_WORTH", new BigDecimal("1500000"), "RETAIL", new BigDecimal("1000000")))
                .totalInvestingClients(120L)
                .newInvestingClientsThisPeriod(8L)
                .averageInvestmentPerClient(new BigDecimal("20833.33"))
                .medianInvestmentPerClient(new BigDecimal("15000.00"))
                .totalInvestments(200L)
                .activeInvestments(180L)
                .maturedInvestments(20L)
                .newInvestmentsThisPeriod(15L)
                .investmentsByStatus(Map.of("ACTIVE", 180L, "MATURED", 20L))
                .investmentsByType(Map.of("PROPERTY", 80L, "EQUITY", 60L, "FIXED_INCOME", 40L, "ALTERNATIVE", 20L))
                .totalReturnsGenerated(new BigDecimal("125000.00"))
                .averageReturnRate(5.0)
                .bestPerformingInvestmentReturn(new BigDecimal("15000.00"))
                .underperformingInvestmentLoss(new BigDecimal("2500.00"))
                .topPerformingInvestments(new ArrayList<>())
                .underperformingInvestments(new ArrayList<>())
                .sharpeRatio(1.2)
                .volatility(12.5)
                .valueAtRisk(new BigDecimal("25000.00"))
                .periodStart(LocalDate.now().minusYears(1))
                .periodEnd(LocalDate.now())
                .build();

        // Setup mock birthdays response
        mockBirthdaysResponse = UpcomingBirthdaysResponse.builder()
                .totalUpcomingBirthdays(12L)
                .birthdaysNext7Days(3L)
                .birthdaysNext30Days(12L)
                .greetingsSentToday(2L)
                .pendingGreetings(5L)
                .upcomingBirthdays(createMockBirthdayClientsDetailed())
                .recentGreetings(new ArrayList<>())
                .build();
    }

    private List<DashboardSummaryResponse.UpcomingBirthdayClient> createMockBirthdayClients() {
        List<DashboardSummaryResponse.UpcomingBirthdayClient> clients = new ArrayList<>();
        
        clients.add(DashboardSummaryResponse.UpcomingBirthdayClient.builder()
                .clientId(1L)
                .clientName("John Smith")
                .email("john.smith@example.com")
                .birthday(LocalDateTime.now().plusDays(2))
                .daysUntilBirthday(2)
                .greetingSent(false)
                .build());
                
        clients.add(DashboardSummaryResponse.UpcomingBirthdayClient.builder()
                .clientId(2L)
                .clientName("Jane Doe")
                .email("jane.doe@example.com")
                .birthday(LocalDateTime.now().plusDays(5))
                .daysUntilBirthday(5)
                .greetingSent(false)
                .build());
                
        return clients;
    }

    private List<UpcomingBirthdaysResponse.BirthdayClient> createMockBirthdayClientsDetailed() {
        List<UpcomingBirthdaysResponse.BirthdayClient> clients = new ArrayList<>();
        
        clients.add(UpcomingBirthdaysResponse.BirthdayClient.builder()
                .clientId(1L)
                .firstName("John")
                .lastName("Smith")
                .fullName("John Smith")
                .email("john.smith@example.com")
                .dateOfBirth(LocalDate.of(1988, 3, 15))
                .nextBirthday(LocalDate.now().plusDays(2))
                .daysUntilBirthday(2)
                .age(35)
                .greetingSent(false)
                .clientStatus("ACTIVE")
                .preferredContactMethod("EMAIL")
                .emailOptIn(true)
                .smsOptIn(false)
                .build());
                
        return clients;
    }

    // ================ GET /api/admin/dashboard/summary Tests ================

    @Test
    @DisplayName("Should get dashboard summary successfully")
    void testGetDashboardSummary_Success() throws Exception {
        // Given
        when(dashboardService.getDashboardSummary()).thenReturn(mockSummaryResponse);

        // When & Then
        mockMvc.perform(get("/api/admin/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Dashboard summary retrieved successfully"))
                .andExpect(jsonPath("$.data.totalClients").value(150))
                .andExpect(jsonPath("$.data.activeClients").value(140))
                .andExpect(jsonPath("$.data.totalInvestmentValue").value(2500000.00))
                .andExpect(jsonPath("$.data.systemStatus").value("OPERATIONAL"));

        verify(dashboardService).getDashboardSummary();
    }

    @Test
    @DisplayName("Should handle service exception when getting dashboard summary")
    void testGetDashboardSummary_ServiceException() throws Exception {
        // Given
        when(dashboardService.getDashboardSummary())
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/admin/dashboard/summary"))
                .andExpect(status().isInternalServerError());

        verify(dashboardService).getDashboardSummary();
    }

    // ================ GET /api/admin/dashboard/clients/stats Tests ================

    @Test
    @DisplayName("Should get client statistics successfully")
    void testGetClientStats_Success() throws Exception {
        // Given
        LocalDate startDate = LocalDate.now().minusMonths(6);
        LocalDate endDate = LocalDate.now();
        
        when(dashboardService.getClientStats(startDate, endDate)).thenReturn(mockClientStatsResponse);

        // When & Then
        mockMvc.perform(get("/api/admin/dashboard/clients/stats")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Client statistics retrieved successfully"))
                .andExpect(jsonPath("$.data.totalClients").value(150))
                .andExpect(jsonPath("$.data.monthlyGrowthRate").value(10.0));

        verify(dashboardService).getClientStats(startDate, endDate);
    }

    @Test
    @DisplayName("Should get client statistics with null dates")
    void testGetClientStats_NullDates() throws Exception {
        // Given
        when(dashboardService.getClientStats(null, null)).thenReturn(mockClientStatsResponse);

        // When & Then
        mockMvc.perform(get("/api/admin/dashboard/clients/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalClients").value(150));

        verify(dashboardService).getClientStats(null, null);
    }

    @Test
    @DisplayName("Should handle invalid date format")
    void testGetClientStats_InvalidDateFormat() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/dashboard/clients/stats")
                .param("startDate", "invalid-date"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(dashboardService);
    }

    // ================ GET /api/admin/dashboard/investments/stats Tests ================

    @Test
    @DisplayName("Should get investment statistics successfully")
    void testGetInvestmentStats_Success() throws Exception {
        // Given
        LocalDate startDate = LocalDate.now().minusYears(1);
        LocalDate endDate = LocalDate.now();
        
        when(dashboardService.getInvestmentStats(startDate, endDate)).thenReturn(mockInvestmentStatsResponse);

        // When & Then
        mockMvc.perform(get("/api/admin/dashboard/investments/stats")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Investment statistics retrieved successfully"))
                .andExpect(jsonPath("$.data.totalInvestmentValue").value(2500000.00))
                .andExpect(jsonPath("$.data.averageReturnRate").value(5.0));

        verify(dashboardService).getInvestmentStats(startDate, endDate);
    }

    @Test
    @DisplayName("Should get investment statistics with default date range")
    void testGetInvestmentStats_DefaultDates() throws Exception {
        // Given
        when(dashboardService.getInvestmentStats(null, null)).thenReturn(mockInvestmentStatsResponse);

        // When & Then
        mockMvc.perform(get("/api/admin/dashboard/investments/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalInvestments").value(200));

        verify(dashboardService).getInvestmentStats(null, null);
    }

    // ================ GET /api/admin/dashboard/birthdays Tests ================

    @Test
    @DisplayName("Should get upcoming birthdays successfully")
    void testGetUpcomingBirthdays_Success() throws Exception {
        // Given
        when(dashboardService.getUpcomingBirthdays(30)).thenReturn(mockBirthdaysResponse);

        // When & Then
        mockMvc.perform(get("/api/admin/dashboard/birthdays")
                .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Upcoming birthdays retrieved successfully"))
                .andExpect(jsonPath("$.data.totalUpcomingBirthdays").value(12))
                .andExpect(jsonPath("$.data.birthdaysNext7Days").value(3));

        verify(dashboardService).getUpcomingBirthdays(30);
    }

    @Test
    @DisplayName("Should get upcoming birthdays with default days parameter")
    void testGetUpcomingBirthdays_DefaultDays() throws Exception {
        // Given
        when(dashboardService.getUpcomingBirthdays(30)).thenReturn(mockBirthdaysResponse);

        // When & Then
        mockMvc.perform(get("/api/admin/dashboard/birthdays"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.birthdaysNext30Days").value(12));

        verify(dashboardService).getUpcomingBirthdays(30);
    }

    @Test
    @DisplayName("Should handle invalid days parameter")
    void testGetUpcomingBirthdays_InvalidDays() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/dashboard/birthdays")
                .param("days", "invalid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(dashboardService);
    }

    // ================ POST /api/admin/dashboard/birthdays/send-greetings Tests ================

    @Test
    @DisplayName("Should send birthday greetings successfully")
    void testSendBirthdayGreetings_Success() throws Exception {
        // Given
        Map<String, Object> result = createMockGreetingResult(true, 2, 0);
        when(dashboardService.sendBirthdayGreetings(null)).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/admin/dashboard/birthdays/send-greetings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Birthday greetings sent successfully"))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.successCount").value(2));

        verify(dashboardService).sendBirthdayGreetings(null);
    }

    @Test
    @DisplayName("Should send birthday greetings for specific client")
    void testSendBirthdayGreetings_SpecificClient() throws Exception {
        // Given
        Long clientId = 1L;
        Map<String, Object> result = createMockGreetingResult(true, 1, 0);
        when(dashboardService.sendBirthdayGreetings(clientId)).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/admin/dashboard/birthdays/send-greetings")
                .param("clientId", clientId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(1));

        verify(dashboardService).sendBirthdayGreetings(clientId);
    }

    @Test
    @DisplayName("Should handle no birthdays today scenario")
    void testSendBirthdayGreetings_NoBirthdays() throws Exception {
        // Given
        Map<String, Object> result = Map.of(
                "success", true,
                "message", "No birthdays today",
                "greetingsSent", 0,
                "timestamp", LocalDateTime.now()
        );
        when(dashboardService.sendBirthdayGreetings(null)).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/admin/dashboard/birthdays/send-greetings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.greetingsSent").value(0));

        verify(dashboardService).sendBirthdayGreetings(null);
    }

    // ================ GET /api/admin/dashboard/enquiries/recent Tests ================

    @Test
    @DisplayName("Should get recent enquiries successfully")
    void testGetRecentEnquiries_Success() throws Exception {
        // Given
        Object mockEnquiries = new PageImpl<>(Arrays.asList("enquiry1", "enquiry2"), PageRequest.of(0, 20), 2);
        when(dashboardService.getRecentEnquiries(any(Pageable.class), eq(null), eq(null)))
                .thenReturn(mockEnquiries);

        // When & Then
        mockMvc.perform(get("/api/admin/dashboard/enquiries/recent")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Recent enquiries retrieved successfully"));

        verify(dashboardService).getRecentEnquiries(any(Pageable.class), eq(null), eq(null));
    }

    @Test
    @DisplayName("Should get recent enquiries with filters")
    void testGetRecentEnquiries_WithFilters() throws Exception {
        // Given
        Object mockEnquiries = new PageImpl<>(Arrays.asList("enquiry1"), PageRequest.of(0, 10), 1);
        when(dashboardService.getRecentEnquiries(any(Pageable.class), eq("OPEN"), eq("INVESTMENT_INQUIRY")))
                .thenReturn(mockEnquiries);

        // When & Then
        mockMvc.perform(get("/api/admin/dashboard/enquiries/recent")
                .param("page", "0")
                .param("size", "10")
                .param("status", "OPEN")
                .param("type", "INVESTMENT_INQUIRY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(dashboardService).getRecentEnquiries(any(Pageable.class), eq("OPEN"), eq("INVESTMENT_INQUIRY"));
    }

    // ================ GET /api/admin/dashboard/enquiries/stats Tests ================

    @Test
    @DisplayName("Should get enquiry statistics successfully")
    void testGetEnquiryStats_Success() throws Exception {
        // Given
        Map<String, Object> stats = createMockEnquiryStats();
        when(dashboardService.getEnquiryStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/admin/dashboard/enquiries/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Enquiry statistics retrieved successfully"))
                .andExpect(jsonPath("$.data.totalEnquiries").value(50))
                .andExpect(jsonPath("$.data.openEnquiries").value(12));

        verify(dashboardService).getEnquiryStats();
    }

    // ================ POST /api/admin/dashboard/export/annual-report Tests ================

    @Test
    @DisplayName("Should export annual report successfully")
    void testExportAnnualReport_Success() throws Exception {
        // Given
        LocalDate year = LocalDate.of(2023, 1, 1);
        Map<String, Object> result = createMockExportResult();
        when(dashboardService.exportAnnualReport(year, "PDF")).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/admin/dashboard/export/annual-report")
                .param("year", year.toString())
                .param("format", "PDF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reportId").exists());

        verify(dashboardService).exportAnnualReport(year, "PDF");
    }

    @Test
    @DisplayName("Should export annual report with default format")
    void testExportAnnualReport_DefaultFormat() throws Exception {
        // Given
        LocalDate year = LocalDate.of(2023, 1, 1);
        Map<String, Object> result = createMockExportResult();
        when(dashboardService.exportAnnualReport(year, "PDF")).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/admin/dashboard/export/annual-report")
                .param("year", year.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.success").value(true));

        verify(dashboardService).exportAnnualReport(year, "PDF");
    }

    @Test
    @DisplayName("Should handle missing year parameter")
    void testExportAnnualReport_MissingYear() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/admin/dashboard/export/annual-report")
                .param("format", "PDF"))
                .andExpect(status().isInternalServerError()); // GlobalExceptionHandler returns 500 for missing required params

        verifyNoInteractions(dashboardService);
    }

    // ================ GET /api/admin/dashboard/export/formats Tests ================

    @Test
    @DisplayName("Should get export formats successfully")
    void testGetExportFormats_Success() throws Exception {
        // Given
        Object formats = Arrays.asList("PDF", "EXCEL", "CSV", "JSON");
        when(dashboardService.getAvailableExportFormats()).thenReturn(formats);

        // When & Then
        mockMvc.perform(get("/api/admin/dashboard/export/formats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(dashboardService).getAvailableExportFormats();
    }

    // ================ POST /api/admin/dashboard/export/custom Tests ================

    @Test
    @DisplayName("Should generate custom export successfully")
    void testGenerateCustomExport_Success() throws Exception {
        // Given
        Map<String, Object> filters = Map.of(
                "startDate", "2023-01-01",
                "endDate", "2023-12-31",
                "clientType", "INDIVIDUAL"
        );
        Map<String, Object> result = createMockExportResult();
        when(dashboardService.generateCustomExport(any(Map.class))).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/admin/dashboard/export/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.exportId").exists());

        verify(dashboardService).generateCustomExport(any(Map.class));
    }

    @Test
    @DisplayName("Should handle empty filters for custom export")
    void testGenerateCustomExport_EmptyFilters() throws Exception {
        // Given
        Map<String, Object> filters = new HashMap<>();
        Map<String, Object> result = createMockExportResult();
        when(dashboardService.generateCustomExport(any(Map.class))).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/admin/dashboard/export/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.success").value(true));

        verify(dashboardService).generateCustomExport(any(Map.class));
    }

    // ================ GET /api/admin/dashboard/config Tests ================

    @Test
    @DisplayName("Should get dashboard configuration successfully")
    void testGetDashboardConfig_Success() throws Exception {
        // Given
        Object config = createMockDashboardConfig();
        when(dashboardService.getDashboardConfig()).thenReturn(config);

        // When & Then
        mockMvc.perform(get("/api/admin/dashboard/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.refreshInterval").value(30));

        verify(dashboardService).getDashboardConfig();
    }

    // ================ PUT /api/admin/dashboard/config Tests ================

    @Test
    @DisplayName("Should update dashboard configuration successfully")
    void testUpdateDashboardConfig_Success() throws Exception {
        // Given
        Map<String, Object> config = Map.of(
                "refreshInterval", 60,
                "defaultDateRange", "7_DAYS"
        );
        Object result = createMockConfigUpdateResult();
        when(dashboardService.updateDashboardConfig(any(Map.class))).thenReturn(result);

        // When & Then
        mockMvc.perform(put("/api/admin/dashboard/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.success").value(true));

        verify(dashboardService).updateDashboardConfig(any(Map.class));
    }

    // ================ Error Handling Tests ================

    @Test
    @DisplayName("Should handle service unavailable scenario")
    void testServiceUnavailable() throws Exception {
        // Given
        when(dashboardService.getDashboardSummary())
                .thenThrow(new RuntimeException("Service temporarily unavailable"));

        // When & Then
        mockMvc.perform(get("/api/admin/dashboard/summary"))
                .andExpect(status().isInternalServerError());

        verify(dashboardService).getDashboardSummary();
    }

    @Test
    @DisplayName("Should handle large page size for enquiries")
    void testGetRecentEnquiries_LargePageSize() throws Exception {
        // Given
        Object mockEnquiries = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 1000), 0);
        when(dashboardService.getRecentEnquiries(any(Pageable.class), eq(null), eq(null)))
                .thenReturn(mockEnquiries);

        // When & Then
        mockMvc.perform(get("/api/admin/dashboard/enquiries/recent")
                .param("size", "1000"))
                .andExpect(status().isOk());

        verify(dashboardService).getRecentEnquiries(any(Pageable.class), eq(null), eq(null));
    }

    @Test
    @DisplayName("Should handle future date for client statistics")
    void testGetClientStats_FutureDate() throws Exception {
        // Given
        LocalDate futureDate = LocalDate.now().plusYears(1);
        when(dashboardService.getClientStats(null, futureDate)).thenReturn(mockClientStatsResponse);

        // When & Then
        mockMvc.perform(get("/api/admin/dashboard/clients/stats")
                .param("endDate", futureDate.toString()))
                .andExpect(status().isOk());

        verify(dashboardService).getClientStats(null, futureDate);
    }

    // ================ Helper Methods ================

    private Map<String, Object> createMockGreetingResult(boolean success, int successCount, int failureCount) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("totalRecipients", successCount + failureCount);
        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("timestamp", LocalDateTime.now());
        return result;
    }

    private Map<String, Object> createMockEnquiryStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEnquiries", 50L);
        stats.put("openEnquiries", 12L);
        stats.put("inProgressEnquiries", 8L);
        stats.put("pendingEnquiries", 5L);
        stats.put("resolvedEnquiries", 20L);
        stats.put("closedEnquiries", 5L);
        stats.put("averageResponseTime", 2.5);
        stats.put("averageResolutionTime", 24.0);
        stats.put("resolutionRate", 70.0);
        return stats;
    }

    private Map<String, Object> createMockExportResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("reportId", "RPT-20231201-001");
        result.put("exportId", "EXP-20231201-001");
        result.put("downloadUrl", "/api/admin/dashboard/downloads/12345");
        result.put("generatedAt", LocalDateTime.now());
        return result;
    }

    private Object createMockDashboardConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("refreshInterval", 30);
        config.put("defaultDateRange", "30_DAYS");
        config.put("enableRealTimeUpdates", true);
        config.put("widgets", Arrays.asList("CLIENT_STATS", "INVESTMENT_STATS", "BIRTHDAYS", "ENQUIRIES"));
        return config;
    }

    private Object createMockConfigUpdateResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Dashboard configuration updated successfully");
        result.put("updatedAt", LocalDateTime.now());
        return result;
    }
} 