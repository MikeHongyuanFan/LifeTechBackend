package com.finance.admin.client.investment.service;

import com.finance.admin.client.investment.dto.*;
import com.finance.admin.client.model.Client;
import com.finance.admin.config.BaseUnitTest;
import com.finance.admin.investment.model.Investment;
import com.finance.admin.investment.repository.InvestmentRepository;
import com.finance.admin.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClientInvestmentService - Sprint 3.1 & 3.2 comprehensive testing
 */
@DisplayName("Client Investment Service - Complete Tests")
public class ClientInvestmentServiceTest extends BaseUnitTest {

    @Mock
    private InvestmentRepository investmentRepository;

    private ClientInvestmentService clientInvestmentService;
    private Long testClientId;
    private List<Investment> testInvestments;
    private Investment testInvestment1;
    private Investment testInvestment2;
    private Client testClient;

    @BeforeEach
    protected void setUp() {
        super.setUp();
        MockitoAnnotations.openMocks(this);
        
        clientInvestmentService = new ClientInvestmentService(investmentRepository);
        testClientId = 1L;
        
        setupTestData();
    }

    private void setupTestData() {
        // Create test client
        testClient = new Client();
        testClient.setId(testClientId);
        testClient.setFirstName("John");
        testClient.setLastName("Doe");
        testClient.setEmailPrimary("john.doe@test.com");

        // Create test investments
        testInvestment1 = createTestInvestment(1L, "Tech Growth Fund", 
            Investment.InvestmentType.MANAGED_FUNDS_MUTUAL, Investment.RiskRating.MODERATE,
            new BigDecimal("10000"), new BigDecimal("11500"), Investment.InvestmentStatus.ACTIVE);

        testInvestment2 = createTestInvestment(2L, "Conservative Bonds", 
            Investment.InvestmentType.FIXED_INCOME_BONDS, Investment.RiskRating.CONSERVATIVE,
            new BigDecimal("5000"), new BigDecimal("5200"), Investment.InvestmentStatus.MATURED);

        testInvestments = Arrays.asList(testInvestment1, testInvestment2);
    }

    private Investment createTestInvestment(Long id, String name, Investment.InvestmentType type,
                                         Investment.RiskRating risk, BigDecimal initialAmount,
                                         BigDecimal currentValue, Investment.InvestmentStatus status) {
        Investment investment = new Investment();
        investment.setId(id);
        investment.setInvestmentName(name);
        investment.setInvestmentType(type);
        investment.setRiskRating(risk);
        investment.setInitialAmount(initialAmount);
        investment.setCurrentValue(currentValue);
        investment.setStatus(status);
        investment.setPurchaseDate(LocalDate.now().minusMonths(6));
        investment.setMaturityDate(LocalDate.now().plusMonths(6));
        investment.setClient(testClient);
        return investment;
    }

    // ================ Sprint 3.1 Basic API Tests ================

    @Test
    @DisplayName("Should get investment summary successfully")
    void testGetInvestmentSummary() {
        // Given
        when(investmentRepository.findByClientId(testClientId)).thenReturn(testInvestments);

        // When
        InvestmentSummaryResponse response = clientInvestmentService.getInvestmentSummary(testClientId);

        // Then
        assertNotNull(response);
        assertNotNull(response.getTotalInvestedAmount());
        assertNotNull(response.getTotalCurrentValue());
        assertNotNull(response.getTotalReturns());
        assertNotNull(response.getOverallReturnPercentage());
        verify(investmentRepository).findByClientId(testClientId);
    }

    @Test
    @DisplayName("Should get investment charts data successfully")
    void testGetInvestmentCharts() {
        // Given
        int months = 12;
        when(investmentRepository.findByClientId(testClientId)).thenReturn(testInvestments);

        // When
        InvestmentChartsResponse response = clientInvestmentService.getInvestmentCharts(testClientId, months);

        // Then
        assertNotNull(response);
        assertNotNull(response.getPortfolioValueTimeSeries());
        assertNotNull(response.getInvestmentAllocationByType());
        assertNotNull(response.getReturnPerformanceComparison());
        verify(investmentRepository).findByClientId(testClientId);
    }

    @Test
    @DisplayName("Should get investment performance successfully")
    void testGetInvestmentPerformance() {
        // Given
        when(investmentRepository.findByClientId(testClientId)).thenReturn(testInvestments);

        // When
        InvestmentPerformanceResponse response = clientInvestmentService.getInvestmentPerformance(testClientId);

        // Then
        assertNotNull(response);
        assertNotNull(response.getPortfolioReturn());
        assertNotNull(response.getTopPerformers());
        assertNotNull(response.getBottomPerformers());
        verify(investmentRepository).findByClientId(testClientId);
    }

    @Test
    @DisplayName("Should filter investments successfully")
    void testFilterInvestments() {
        // Given
        InvestmentFilterRequest filterRequest = InvestmentFilterRequest.builder()
            .startDate(LocalDate.now().minusYears(1))
            .endDate(LocalDate.now())
            .investmentTypes(Arrays.asList(Investment.InvestmentType.MANAGED_FUNDS_MUTUAL))
            .investmentStatuses(Arrays.asList(Investment.InvestmentStatus.ACTIVE))
            .returnTypes(Arrays.asList(InvestmentFilterRequest.ReturnType.CAPITAL_GROWTH))
            .build();

        when(investmentRepository.findByClientId(testClientId)).thenReturn(testInvestments);

        // When
        FilteredInvestmentsResponse response = clientInvestmentService.filterInvestments(testClientId, filterRequest);

        // Then
        assertNotNull(response);
        verify(investmentRepository).findByClientId(testClientId);
    }

    @Test
    @DisplayName("Should get active investments successfully")
    void testGetActiveInvestments() {
        // Given
        List<Investment> activeInvestments = Arrays.asList(testInvestment1);
        when(investmentRepository.findByClientIdAndStatus(testClientId, Investment.InvestmentStatus.ACTIVE))
            .thenReturn(activeInvestments);

        // When
        List<ActiveInvestmentResponse> response = clientInvestmentService.getActiveInvestments(testClientId);

        // Then
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Tech Growth Fund", response.get(0).getInvestmentName());
        verify(investmentRepository).findByClientIdAndStatus(testClientId, Investment.InvestmentStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should get completed investments successfully")
    void testGetCompletedInvestments() {
        // Given
        List<Investment> completedInvestments = Arrays.asList(testInvestment2);
        when(investmentRepository.findByClientIdAndStatus(testClientId, Investment.InvestmentStatus.MATURED))
            .thenReturn(completedInvestments);

        // When
        List<CompletedInvestmentResponse> response = clientInvestmentService.getCompletedInvestments(testClientId);

        // Then
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Conservative Bonds", response.get(0).getInvestmentName());
        verify(investmentRepository).findByClientIdAndStatus(testClientId, Investment.InvestmentStatus.MATURED);
    }

    @Test
    @DisplayName("Should get all investments successfully")
    void testGetAllInvestments() {
        // Given
        when(investmentRepository.findByClientId(testClientId)).thenReturn(testInvestments);

        // When
        AllInvestmentsResponse response = clientInvestmentService.getAllInvestments(testClientId);

        // Then
        assertNotNull(response);
        assertNotNull(response.getActiveInvestments());
        assertNotNull(response.getCompletedInvestments());
        assertNotNull(response.getPortfolioSummary());
        verify(investmentRepository, times(2)).findByClientId(testClientId);
    }

    @Test
    @DisplayName("Should get investment details successfully")
    void testGetInvestmentDetails() {
        // Given
        Long investmentId = 1L;
        when(investmentRepository.findById(investmentId)).thenReturn(Optional.of(testInvestment1));

        // When
        InvestmentDetailResponse response = clientInvestmentService.getInvestmentDetails(testClientId, investmentId);

        // Then
        assertNotNull(response);
        assertEquals("Tech Growth Fund", response.getInvestmentName());
        assertEquals(new BigDecimal("10000"), response.getInitialAmount());
        verify(investmentRepository).findById(investmentId);
    }

    @Test
    @DisplayName("Should throw exception when getting details for non-existent investment")
    void testGetInvestmentDetailsWithNonExistentInvestment() {
        // Given
        Long investmentId = 999L;
        when(investmentRepository.findById(investmentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
            clientInvestmentService.getInvestmentDetails(testClientId, investmentId));
    }

    @Test
    @DisplayName("Should throw exception when getting details for wrong client")
    void testGetInvestmentDetailsWithWrongClient() {
        // Given
        Long investmentId = 1L;
        Long wrongClientId = 999L;
        when(investmentRepository.findById(investmentId)).thenReturn(Optional.of(testInvestment1));

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
            clientInvestmentService.getInvestmentDetails(wrongClientId, investmentId));
    }

    @Test
    @DisplayName("Should get available reports successfully")
    void testGetAvailableReports() {
        // Given
        when(investmentRepository.findByClientId(testClientId)).thenReturn(testInvestments);

        // When
        List<InvestmentReportResponse> response = clientInvestmentService.getAvailableReports(testClientId);

        // Then
        assertNotNull(response);
    }

    @Test
    @DisplayName("Should download report successfully")
    void testDownloadReport() {
        // Given
        Long reportId = 1L;
        when(investmentRepository.findByClientId(testClientId)).thenReturn(testInvestments);

        // When
        ResponseEntity<byte[]> response = clientInvestmentService.downloadReport(testClientId, reportId);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    @DisplayName("Should generate report successfully")
    void testGenerateReport() {
        // Given
        ReportGenerationRequest request = ReportGenerationRequest.builder()
            .reportType(InvestmentReportResponse.ReportType.MONTHLY_STATEMENT)
            .startDate(LocalDate.now().minusMonths(1))
            .endDate(LocalDate.now())
            .includeDetailedBreakdown(true)
            .includeCharts(true)
            .build();

        when(investmentRepository.findByClientId(testClientId)).thenReturn(testInvestments);

        // When
        InvestmentReportResponse response = clientInvestmentService.generateReport(testClientId, request);

        // Then
        assertNotNull(response);
    }

    // ================ Sprint 3.2 Enhanced Functionality Tests ================

    @Test
    @DisplayName("Should compare investments successfully")
    void testCompareInvestments() {
        // Given
        InvestmentComparisonRequest request = InvestmentComparisonRequest.builder()
            .investmentIds(Arrays.asList(1L, 2L))
            .primaryMetric(InvestmentComparisonRequest.ComparisonMetric.TOTAL_RETURN)
            .includeHistoricalData(true)
            .includeBenchmarkComparison(true)
            .build();

        when(investmentRepository.findById(1L)).thenReturn(Optional.of(testInvestment1));
        when(investmentRepository.findById(2L)).thenReturn(Optional.of(testInvestment2));

        // When
        InvestmentComparisonResponse response = clientInvestmentService.compareInvestments(testClientId, request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getInvestments());
        assertEquals(2, response.getInvestments().size());
        
        InvestmentComparisonResponse.InvestmentComparisonItem item1 = response.getInvestments().get(0);
        assertEquals("Tech Growth Fund", item1.getInvestmentName());
        assertEquals(new BigDecimal("10000"), item1.getInitialAmount());
        assertEquals(new BigDecimal("11500"), item1.getCurrentValue());

        verify(investmentRepository).findById(1L);
        verify(investmentRepository).findById(2L);
    }

    @Test
    @DisplayName("Should throw exception when comparing non-existent investment")
    void testCompareInvestmentsWithNonExistentInvestment() {
        // Given
        InvestmentComparisonRequest request = InvestmentComparisonRequest.builder()
            .investmentIds(Arrays.asList(999L))
            .build();

        when(investmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
            clientInvestmentService.compareInvestments(testClientId, request));
    }

    @Test
    @DisplayName("Should get investment analytics successfully")
    void testGetInvestmentAnalytics() {
        // Given
        when(investmentRepository.findByClientId(testClientId)).thenReturn(testInvestments);

        // When
        InvestmentAnalyticsResponse response = clientInvestmentService.getInvestmentAnalytics(testClientId);

        // Then
        assertNotNull(response);
        assertNotNull(response.getPerformance());
        assertNotNull(response.getRisk());
        assertNotNull(response.getTrends());
        assertNotNull(response.getDiversification());
        assertNotNull(response.getInsights());
        assertNotNull(response.getRecommendations());

        verify(investmentRepository).findByClientId(testClientId);
    }

    @Test
    @DisplayName("Should apply advanced filters successfully")
    void testAdvancedFilterInvestments() {
        // Given
        EnhancedInvestmentFilterRequest filterRequest = EnhancedInvestmentFilterRequest.builder()
            .minInitialAmount(new BigDecimal("5000"))
            .maxInitialAmount(new BigDecimal("15000"))
            .investmentStatuses(Arrays.asList(Investment.InvestmentStatus.ACTIVE))
            .riskRatings(Arrays.asList(Investment.RiskRating.MODERATE))
            .sortBy(EnhancedInvestmentFilterRequest.SortBy.RETURN_PERCENTAGE)
            .sortDirection(EnhancedInvestmentFilterRequest.SortDirection.DESC)
            .page(0)
            .size(10)
            .build();

        when(investmentRepository.findByClientId(testClientId)).thenReturn(testInvestments);

        // When
        FilteredInvestmentsResponse response = clientInvestmentService.advancedFilterInvestments(testClientId, filterRequest);

        // Then
        assertNotNull(response);
        verify(investmentRepository).findByClientId(testClientId);
    }

    @Test
    @DisplayName("Should get detailed active investments successfully")
    void testGetDetailedActiveInvestments() {
        // Given
        List<Investment> activeInvestments = Arrays.asList(testInvestment1);
        when(investmentRepository.findByClientIdAndStatus(testClientId, Investment.InvestmentStatus.ACTIVE))
            .thenReturn(activeInvestments);

        // When
        List<ActiveInvestmentResponse> response = clientInvestmentService
            .getDetailedActiveInvestments(testClientId, true, true);

        // Then
        assertNotNull(response);
        assertEquals(1, response.size());
        
        ActiveInvestmentResponse activeInvestment = response.get(0);
        assertEquals("Tech Growth Fund", activeInvestment.getInvestmentName());
        assertEquals(new BigDecimal("10000"), activeInvestment.getInitialAmount());

        verify(investmentRepository).findByClientIdAndStatus(testClientId, Investment.InvestmentStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should get investment history with analytics successfully")
    void testGetInvestmentHistoryWithAnalytics() {
        // Given
        List<Investment> completedInvestments = Arrays.asList(testInvestment2);
        when(investmentRepository.findByClientIdAndStatus(testClientId, Investment.InvestmentStatus.MATURED))
            .thenReturn(completedInvestments);

        // When
        List<CompletedInvestmentResponse> response = clientInvestmentService
            .getInvestmentHistoryWithAnalytics(testClientId, true, true);

        // Then
        assertNotNull(response);
        assertEquals(1, response.size());
        
        CompletedInvestmentResponse completedInvestment = response.get(0);
        assertEquals("Conservative Bonds", completedInvestment.getInvestmentName());
        assertEquals(new BigDecimal("5000"), completedInvestment.getInitialAmount());

        verify(investmentRepository).findByClientIdAndStatus(testClientId, Investment.InvestmentStatus.MATURED);
    }

    @Test
    @DisplayName("Should get investment insights successfully")
    void testGetInvestmentInsights() {
        // Given
        when(investmentRepository.findByClientId(testClientId)).thenReturn(testInvestments);

        // When
        List<InvestmentAnalyticsResponse.InvestmentInsight> insights = 
            clientInvestmentService.getInvestmentInsights(testClientId);

        // Then
        assertNotNull(insights);
        verify(investmentRepository).findByClientId(testClientId);
    }

    @Test
    @DisplayName("Should get investment recommendations successfully")
    void testGetInvestmentRecommendations() {
        // Given
        when(investmentRepository.findByClientId(testClientId)).thenReturn(testInvestments);

        // When
        List<InvestmentAnalyticsResponse.InvestmentRecommendation> recommendations = 
            clientInvestmentService.getInvestmentRecommendations(testClientId);

        // Then
        assertNotNull(recommendations);
        verify(investmentRepository).findByClientId(testClientId);
    }

    @Test
    @DisplayName("Should get portfolio optimization successfully")
    void testGetPortfolioOptimization() {
        // Given
        when(investmentRepository.findByClientId(testClientId)).thenReturn(testInvestments);

        // When
        InvestmentAnalyticsResponse response = clientInvestmentService.getPortfolioOptimization(testClientId);

        // Then
        assertNotNull(response);
        verify(investmentRepository).findByClientId(testClientId);
    }

    @Test
    @DisplayName("Should get investment tracking successfully")
    void testGetInvestmentTracking() {
        // Given
        Long investmentId = 1L;
        int months = 6;
        when(investmentRepository.findById(investmentId)).thenReturn(Optional.of(testInvestment1));

        // When
        InvestmentDetailResponse response = clientInvestmentService
            .getInvestmentTracking(testClientId, investmentId, months);

        // Then
        assertNotNull(response);
        assertEquals("Tech Growth Fund", response.getInvestmentName());
        verify(investmentRepository).findById(investmentId);
    }

    @Test
    @DisplayName("Should throw exception when tracking non-existent investment")
    void testGetInvestmentTrackingWithNonExistentInvestment() {
        // Given
        Long investmentId = 999L;
        when(investmentRepository.findById(investmentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
            clientInvestmentService.getInvestmentTracking(testClientId, investmentId, 6));
    }

    @Test
    @DisplayName("Should throw exception when tracking investment of different client")
    void testGetInvestmentTrackingWithWrongClient() {
        // Given
        Long investmentId = 1L;
        Long wrongClientId = 999L;
        when(investmentRepository.findById(investmentId)).thenReturn(Optional.of(testInvestment1));

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
            clientInvestmentService.getInvestmentTracking(wrongClientId, investmentId, 6));
    }

    @Test
    @DisplayName("Should get investment alerts successfully")
    void testGetInvestmentAlerts() {
        // Given
        when(investmentRepository.findByClientId(testClientId)).thenReturn(testInvestments);

        // When
        List<InvestmentAlert> alerts = clientInvestmentService.getInvestmentAlerts(testClientId);

        // Then
        assertNotNull(alerts);
        verify(investmentRepository).findByClientId(testClientId);
    }

    @Test
    @DisplayName("Should handle empty investment list gracefully")
    void testMethodsWithEmptyInvestmentList() {
        // Given
        when(investmentRepository.findByClientId(testClientId)).thenReturn(new ArrayList<>());

        // When & Then - should not throw exceptions
        assertNotNull(clientInvestmentService.getInvestmentSummary(testClientId));
        assertNotNull(clientInvestmentService.getInvestmentCharts(testClientId, 12));
        assertNotNull(clientInvestmentService.getInvestmentPerformance(testClientId));
        assertNotNull(clientInvestmentService.getAllInvestments(testClientId));
        assertNotNull(clientInvestmentService.getAvailableReports(testClientId));
        assertNotNull(clientInvestmentService.getInvestmentAnalytics(testClientId));
        assertNotNull(clientInvestmentService.getInvestmentInsights(testClientId));
        assertNotNull(clientInvestmentService.getInvestmentRecommendations(testClientId));
        assertNotNull(clientInvestmentService.getPortfolioOptimization(testClientId));
        assertNotNull(clientInvestmentService.getInvestmentAlerts(testClientId));
    }

    // ================ DTO Validation Tests ================

    @Test
    @DisplayName("Should validate InvestmentFilterRequest properly")
    void testInvestmentFilterRequestValidation() {
        // Test valid request
        InvestmentFilterRequest validRequest = InvestmentFilterRequest.builder()
            .startDate(LocalDate.now().minusMonths(12))
            .endDate(LocalDate.now())
            .investmentTypes(Arrays.asList(Investment.InvestmentType.MANAGED_FUNDS_MUTUAL))
            .investmentStatuses(Arrays.asList(Investment.InvestmentStatus.ACTIVE))
            .returnTypes(Arrays.asList(InvestmentFilterRequest.ReturnType.CAPITAL_GROWTH))
            .build();
        
        assertNotNull(validRequest.getStartDate());
        assertNotNull(validRequest.getEndDate());
        assertNotNull(validRequest.getInvestmentTypes());
        assertEquals(1, validRequest.getReturnTypes().size());
    }

    @Test
    @DisplayName("Should validate ReportGenerationRequest properly")
    void testReportGenerationRequestValidation() {
        // Test valid request
        ReportGenerationRequest validRequest = ReportGenerationRequest.builder()
            .reportType(InvestmentReportResponse.ReportType.MONTHLY_STATEMENT)
            .startDate(LocalDate.now().minusMonths(1))
            .endDate(LocalDate.now())
            .includeDetailedBreakdown(true)
            .includeCharts(true)
            .fileFormat("PDF")
            .build();
        
        assertNotNull(validRequest);
        assertEquals(InvestmentReportResponse.ReportType.MONTHLY_STATEMENT, validRequest.getReportType());
        assertTrue(validRequest.getIncludeDetailedBreakdown());
        assertEquals("PDF", validRequest.getFileFormat());
    }

    @Test
    @DisplayName("Should validate InvestmentComparisonRequest properly")
    void testInvestmentComparisonRequestValidation() {
        // Test valid request
        InvestmentComparisonRequest validRequest = InvestmentComparisonRequest.builder()
            .investmentIds(Arrays.asList(1L, 2L))
            .primaryMetric(InvestmentComparisonRequest.ComparisonMetric.TOTAL_RETURN)
            .build();
        
        assertNotNull(validRequest.getInvestmentIds());
        assertEquals(2, validRequest.getInvestmentIds().size());
        assertEquals(InvestmentComparisonRequest.ComparisonMetric.TOTAL_RETURN, validRequest.getPrimaryMetric());
    }

    @Test
    @DisplayName("Should validate EnhancedInvestmentFilterRequest properly")
    void testEnhancedInvestmentFilterRequestValidation() {
        // Test valid request
        EnhancedInvestmentFilterRequest filterRequest = EnhancedInvestmentFilterRequest.builder()
            .minInitialAmount(new BigDecimal("1000"))
            .maxInitialAmount(new BigDecimal("50000"))
            .minReturnPercentage(new BigDecimal("5"))
            .maxReturnPercentage(new BigDecimal("20"))
            .page(0)
            .size(10)
            .sortBy(EnhancedInvestmentFilterRequest.SortBy.RETURN_PERCENTAGE)
            .sortDirection(EnhancedInvestmentFilterRequest.SortDirection.DESC)
            .build();
        
        assertNotNull(filterRequest);
        assertEquals(new BigDecimal("1000"), filterRequest.getMinInitialAmount());
        assertEquals(10, filterRequest.getSize());
        assertEquals(EnhancedInvestmentFilterRequest.SortBy.RETURN_PERCENTAGE, filterRequest.getSortBy());
    }

    @Test
    @DisplayName("Should validate InvestmentAlert properly")
    void testInvestmentAlertValidation() {
        // Test valid alert
        InvestmentAlert alert = InvestmentAlert.builder()
            .id(1L)
            .title("Performance Warning")
            .message("Investment underperforming")
            .alertType(InvestmentAlert.AlertType.PERFORMANCE_WARNING)
            .severity(InvestmentAlert.AlertSeverity.MEDIUM)
            .category(InvestmentAlert.AlertCategory.PERFORMANCE)
            .alertDate(LocalDateTime.now())
            .isRead(false)
            .isActionRequired(true)
            .investmentId(1L)
            .investmentName("Tech Growth Fund")
            .recommendedAction("Review investment strategy")
            .build();
        
        assertNotNull(alert);
        assertEquals("Performance Warning", alert.getTitle());
        assertEquals(InvestmentAlert.AlertType.PERFORMANCE_WARNING, alert.getAlertType());
        assertEquals(InvestmentAlert.AlertSeverity.MEDIUM, alert.getSeverity());
        assertTrue(alert.getIsActionRequired());
    }
} 