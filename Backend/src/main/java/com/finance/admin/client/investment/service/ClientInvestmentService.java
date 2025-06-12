package com.finance.admin.client.investment.service;

import com.finance.admin.client.investment.dto.*;
import com.finance.admin.investment.model.Investment;
import com.finance.admin.investment.repository.InvestmentRepository;
import com.finance.admin.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ClientInvestmentService {

    private final InvestmentRepository investmentRepository;

    public InvestmentSummaryResponse getInvestmentSummary(Long clientId) {
        log.info("Getting investment summary for client: {}", clientId);
        
        List<Investment> investments = investmentRepository.findByClientId(clientId);
        
        if (investments.isEmpty()) {
            return createEmptyInvestmentSummary();
        }
        
        return buildInvestmentSummary(investments);
    }

    public InvestmentChartsResponse getInvestmentCharts(Long clientId, int months) {
        log.info("Getting investment charts data for client: {} for {} months", clientId, months);
        
        List<Investment> investments = investmentRepository.findByClientId(clientId);
        LocalDate startDate = LocalDate.now().minusMonths(months);
        
        return buildInvestmentCharts(investments, startDate);
    }

    public InvestmentPerformanceResponse getInvestmentPerformance(Long clientId) {
        log.info("Getting investment performance for client: {}", clientId);
        
        List<Investment> investments = investmentRepository.findByClientId(clientId);
        
        return buildPerformanceResponse(investments);
    }

    public FilteredInvestmentsResponse filterInvestments(Long clientId, InvestmentFilterRequest filterRequest) {
        log.info("Filtering investments for client: {} with filters: {}", clientId, filterRequest);
        
        List<Investment> allInvestments = investmentRepository.findByClientId(clientId);
        List<Investment> filteredInvestments = applyFilters(allInvestments, filterRequest);
        
        return buildFilteredResponse(filteredInvestments, filterRequest);
    }

    public List<ActiveInvestmentResponse> getActiveInvestments(Long clientId) {
        log.info("Getting active investments for client: {}", clientId);
        
        List<Investment> activeInvestments = investmentRepository
            .findByClientIdAndStatus(clientId, Investment.InvestmentStatus.ACTIVE);
        
        return activeInvestments.stream()
            .map(this::mapToActiveInvestmentResponse)
            .collect(Collectors.toList());
    }

    public List<CompletedInvestmentResponse> getCompletedInvestments(Long clientId) {
        log.info("Getting completed investments for client: {}", clientId);
        
        List<Investment> completedInvestments = investmentRepository
            .findByClientIdAndStatus(clientId, Investment.InvestmentStatus.MATURED);
        
        return completedInvestments.stream()
            .map(this::mapToCompletedInvestmentResponse)
            .collect(Collectors.toList());
    }

    public AllInvestmentsResponse getAllInvestments(Long clientId) {
        log.info("Getting all investments for client: {}", clientId);
        
        List<Investment> allInvestments = investmentRepository.findByClientId(clientId);
        
        return AllInvestmentsResponse.builder()
            .activeInvestments(getActiveInvestments(clientId))
            .completedInvestments(getCompletedInvestments(clientId))
            .portfolioSummary(getInvestmentSummary(clientId))
            .categorization(buildCategorization(allInvestments))
            .build();
    }

    public InvestmentDetailResponse getInvestmentDetails(Long clientId, Long investmentId) {
        log.info("Getting investment details for client: {} and investment: {}", clientId, investmentId);
        
        Investment investment = investmentRepository.findById(investmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));
        
        // Verify the investment belongs to the client
        if (!investment.getClient().getId().equals(clientId)) {
            throw new ResourceNotFoundException("Investment not found for this client");
        }
        
        return mapToInvestmentDetailResponse(investment);
    }

    public List<InvestmentReportResponse> getAvailableReports(Long clientId) {
        log.info("Getting available reports for client: {}", clientId);
        
        // This would typically query a reports table
        // For now, return mock data
        return generateMockReports(clientId);
    }

    public ResponseEntity<byte[]> downloadReport(Long clientId, Long reportId) {
        log.info("Downloading report {} for client: {}", reportId, clientId);
        
        // This would typically generate or retrieve the actual report file
        // For now, return a mock PDF response
        byte[] reportContent = generateMockReportContent(reportId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "investment_report_" + reportId + ".pdf");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(reportContent);
    }

    @Transactional
    public InvestmentReportResponse generateReport(Long clientId, ReportGenerationRequest request) {
        log.info("Generating custom report for client: {} with request: {}", clientId, request);
        
        // This would typically generate a new report and save it to database
        // For now, return a mock response
        return InvestmentReportResponse.builder()
            .id(System.currentTimeMillis())
            .reportName(request.getCustomReportName() != null ? 
                      request.getCustomReportName() : "Custom Report")
            .reportType(request.getReportType())
            .periodStart(request.getStartDate())
            .periodEnd(request.getEndDate())
            .generatedDate(LocalDateTime.now())
            .fileFormat(request.getFileFormat())
            .isDownloaded(false)
            .downloadCount(0)
            .build();
    }

    // Private helper methods

    private InvestmentSummaryResponse createEmptyInvestmentSummary() {
        return InvestmentSummaryResponse.builder()
            .totalInvestedAmount(BigDecimal.ZERO)
            .totalCurrentValue(BigDecimal.ZERO)
            .totalReturns(BigDecimal.ZERO)
            .totalBalance(BigDecimal.ZERO)
            .netProfitLoss(BigDecimal.ZERO)
            .overallReturnPercentage(BigDecimal.ZERO)
            .totalInvestments(0)
            .activeInvestments(0)
            .completedInvestments(0)
            .pendingInvestments(0)
            .build();
    }

    private InvestmentSummaryResponse buildInvestmentSummary(List<Investment> investments) {
        BigDecimal totalInvested = investments.stream()
            .map(Investment::getInitialAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCurrentValue = investments.stream()
            .map(inv -> inv.getCurrentValue() != null ? inv.getCurrentValue() : inv.getInitialAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReturns = totalCurrentValue.subtract(totalInvested);
        BigDecimal overallReturnPercentage = totalInvested.compareTo(BigDecimal.ZERO) > 0 ?
            totalReturns.divide(totalInvested, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) :
            BigDecimal.ZERO;

        // Count by status
        Map<Investment.InvestmentStatus, Long> statusCounts = investments.stream()
            .collect(Collectors.groupingBy(Investment::getStatus, Collectors.counting()));

        return InvestmentSummaryResponse.builder()
            .totalInvestedAmount(totalInvested)
            .totalCurrentValue(totalCurrentValue)
            .totalReturns(totalReturns)
            .totalBalance(totalCurrentValue)
            .netProfitLoss(totalReturns)
            .overallReturnPercentage(overallReturnPercentage)
            .totalInvestments(investments.size())
            .activeInvestments(statusCounts.getOrDefault(Investment.InvestmentStatus.ACTIVE, 0L).intValue())
            .completedInvestments(statusCounts.getOrDefault(Investment.InvestmentStatus.MATURED, 0L).intValue())
            .pendingInvestments(statusCounts.getOrDefault(Investment.InvestmentStatus.PENDING, 0L).intValue())
            .riskDistribution(buildRiskDistribution(investments))
            .assetAllocation(buildAssetAllocation(investments))
            .statusDistribution(buildStatusDistribution(investments))
            .build();
    }

    private InvestmentChartsResponse buildInvestmentCharts(List<Investment> investments, LocalDate startDate) {
        return InvestmentChartsResponse.builder()
            .portfolioValueTimeSeries(buildPortfolioTimeSeries(investments, startDate))
            .investmentAllocationByType(buildTypeAllocation(investments))
            .returnPerformanceComparison(buildPerformanceComparison(investments))
            .monthlyPerformanceTrends(buildMonthlyTrends(investments))
            .assetAllocationBreakdown(buildAssetBreakdown(investments))
            .riskDistribution(buildRiskPieChart(investments))
            .chartMetrics(buildChartMetrics(investments))
            .build();
    }

    private InvestmentPerformanceResponse buildPerformanceResponse(List<Investment> investments) {
        BigDecimal portfolioReturn = investments.stream()
            .map(Investment::getCurrentReturn)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalInvested = investments.stream()
            .map(Investment::getInitialAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal portfolioReturnPercentage = totalInvested.compareTo(BigDecimal.ZERO) > 0 ?
            portfolioReturn.divide(totalInvested, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) :
            BigDecimal.ZERO;

        return InvestmentPerformanceResponse.builder()
            .portfolioReturn(portfolioReturn)
            .portfolioReturnPercentage(portfolioReturnPercentage)
            .portfolioVolatility(calculatePortfolioVolatility(investments))
            .sharpeRatio(calculateSharpeRatio(investments))
            .performanceRating(determinePerformanceRating(portfolioReturnPercentage))
            .topPerformers(getTopPerformers(investments))
            .bottomPerformers(getBottomPerformers(investments))
            .build();
    }

    // Additional helper methods for calculations and mappings

    private ActiveInvestmentResponse mapToActiveInvestmentResponse(Investment investment) {
        LocalDate now = LocalDate.now();
        Integer daysToMaturity = investment.getMaturityDate() != null ?
            (int) ChronoUnit.DAYS.between(now, investment.getMaturityDate()) : null;
        Integer daysSincePurchase = (int) ChronoUnit.DAYS.between(investment.getPurchaseDate(), now);

        return ActiveInvestmentResponse.builder()
            .id(investment.getId())
            .investmentName(investment.getInvestmentName())
            .investmentType(investment.getInvestmentType().getDisplayName())
            .investmentCategory(investment.getInvestmentCategory())
            .description(investment.getDescription())
            .riskRating(investment.getRiskRating().getDisplayName())
            .initialAmount(investment.getInitialAmount())
            .currentValue(investment.getCurrentValue())
            .currentReturns(investment.getCurrentReturn())
            .currentReturnPercentage(investment.getCurrentReturnPercentage())
            .expectedReturnRate(investment.getExpectedReturnRate())
            .expectedReturnAmount(investment.getExpectedReturnAmount())
            .purchaseDate(investment.getPurchaseDate())
            .maturityDate(investment.getMaturityDate())
            .daysToMaturity(daysToMaturity)
            .daysSincePurchase(daysSincePurchase)
            .status(investment.getStatus().name())
            .isApproachingMaturity(daysToMaturity != null && daysToMaturity <= 90)
            .build();
    }

    private CompletedInvestmentResponse mapToCompletedInvestmentResponse(Investment investment) {
        Integer duration = investment.getMaturityDate() != null ?
            (int) ChronoUnit.DAYS.between(investment.getPurchaseDate(), investment.getMaturityDate()) : null;

        return CompletedInvestmentResponse.builder()
            .id(investment.getId())
            .investmentName(investment.getInvestmentName())
            .investmentType(investment.getInvestmentType().getDisplayName())
            .investmentCategory(investment.getInvestmentCategory())
            .description(investment.getDescription())
            .riskRating(investment.getRiskRating().getDisplayName())
            .initialAmount(investment.getInitialAmount())
            .finalValue(investment.getCurrentValue())
            .totalReturns(investment.getCurrentReturn())
            .totalReturnPercentage(investment.getCurrentReturnPercentage())
            .expectedReturnRate(investment.getExpectedReturnRate())
            .expectedReturnAmount(investment.getExpectedReturnAmount())
            .purchaseDate(investment.getPurchaseDate())
            .maturityDate(investment.getMaturityDate())
            .completionDate(investment.getMaturityDate())
            .investmentDuration(duration)
            .build();
    }

    private InvestmentDetailResponse mapToInvestmentDetailResponse(Investment investment) {
        return InvestmentDetailResponse.builder()
            .id(investment.getId())
            .investmentName(investment.getInvestmentName())
            .investmentType(investment.getInvestmentType().getDisplayName())
            .investmentCategory(investment.getInvestmentCategory())
            .description(investment.getDescription())
            .investmentObjective(investment.getInvestmentObjective())
            .riskRating(investment.getRiskRating().getDisplayName())
            .status(investment.getStatus().name())
            .initialAmount(investment.getInitialAmount())
            .currentValue(investment.getCurrentValue())
            .expectedReturnRate(investment.getExpectedReturnRate())
            .expectedReturnAmount(investment.getExpectedReturnAmount())
            .actualReturnAmount(investment.getActualReturnAmount())
            .currentReturnPercentage(investment.getCurrentReturnPercentage())
            .unitsPurchased(investment.getUnitsPurchased())
            .purchasePricePerUnit(investment.getPurchasePricePerUnit())
            .currentPricePerUnit(investment.getCurrentPricePerUnit())
            .transactionFees(investment.getTransactionFees())
            .managementFees(investment.getManagementFees())
            .performanceFees(investment.getPerformanceFees())
            .totalFees(investment.getTotalFees())
            .purchaseDate(investment.getPurchaseDate())
            .maturityDate(investment.getMaturityDate())
            .createdAt(investment.getCreatedAt())
            .updatedAt(investment.getUpdatedAt())
            .build();
    }

    // Mock helper methods - these would be replaced with real implementations

    private Map<String, InvestmentSummaryResponse.RiskDistribution> buildRiskDistribution(List<Investment> investments) {
        Map<String, InvestmentSummaryResponse.RiskDistribution> riskDist = new HashMap<>();
        // Implementation would calculate actual risk distribution
        return riskDist;
    }

    private Map<String, InvestmentSummaryResponse.AssetAllocation> buildAssetAllocation(List<Investment> investments) {
        Map<String, InvestmentSummaryResponse.AssetAllocation> assetAlloc = new HashMap<>();
        // Implementation would calculate actual asset allocation
        return assetAlloc;
    }

    private Map<String, InvestmentSummaryResponse.StatusDistribution> buildStatusDistribution(List<Investment> investments) {
        Map<String, InvestmentSummaryResponse.StatusDistribution> statusDist = new HashMap<>();
        // Implementation would calculate actual status distribution
        return statusDist;
    }

    // Additional mock methods for chart data, performance calculations, etc.
    private List<InvestmentChartsResponse.TimeSeriesDataPoint> buildPortfolioTimeSeries(List<Investment> investments, LocalDate startDate) {
        return new ArrayList<>(); // Mock implementation
    }

    private List<InvestmentChartsResponse.PieChartDataPoint> buildTypeAllocation(List<Investment> investments) {
        return new ArrayList<>(); // Mock implementation
    }

    private List<InvestmentChartsResponse.BarChartDataPoint> buildPerformanceComparison(List<Investment> investments) {
        return new ArrayList<>(); // Mock implementation
    }

    private List<InvestmentChartsResponse.TimeSeriesDataPoint> buildMonthlyTrends(List<Investment> investments) {
        return new ArrayList<>(); // Mock implementation
    }

    private List<InvestmentChartsResponse.PieChartDataPoint> buildAssetBreakdown(List<Investment> investments) {
        return new ArrayList<>(); // Mock implementation
    }

    private List<InvestmentChartsResponse.PieChartDataPoint> buildRiskPieChart(List<Investment> investments) {
        return new ArrayList<>(); // Mock implementation
    }

    private InvestmentChartsResponse.ChartMetrics buildChartMetrics(List<Investment> investments) {
        return InvestmentChartsResponse.ChartMetrics.builder().build(); // Mock implementation
    }

    private BigDecimal calculatePortfolioVolatility(List<Investment> investments) {
        return BigDecimal.ZERO; // Mock implementation
    }

    private BigDecimal calculateSharpeRatio(List<Investment> investments) {
        return BigDecimal.ZERO; // Mock implementation
    }

    private String determinePerformanceRating(BigDecimal returnPercentage) {
        if (returnPercentage.compareTo(new BigDecimal("15")) > 0) return "Excellent";
        if (returnPercentage.compareTo(new BigDecimal("10")) > 0) return "Good";
        if (returnPercentage.compareTo(new BigDecimal("5")) > 0) return "Average";
        return "Poor";
    }

    private List<InvestmentPerformanceResponse.AssetPerformanceRanking> getTopPerformers(List<Investment> investments) {
        return new ArrayList<>(); // Mock implementation
    }

    private List<InvestmentPerformanceResponse.AssetPerformanceRanking> getBottomPerformers(List<Investment> investments) {
        return new ArrayList<>(); // Mock implementation
    }

    private List<Investment> applyFilters(List<Investment> investments, InvestmentFilterRequest filterRequest) {
        return investments; // Mock implementation - would apply actual filters
    }

    private FilteredInvestmentsResponse buildFilteredResponse(List<Investment> investments, InvestmentFilterRequest filterRequest) {
        return FilteredInvestmentsResponse.builder().build(); // Mock implementation
    }

    private AllInvestmentsResponse.InvestmentCategorization buildCategorization(List<Investment> investments) {
        return AllInvestmentsResponse.InvestmentCategorization.builder().build(); // Mock implementation
    }

    private List<InvestmentReportResponse> generateMockReports(Long clientId) {
        return Arrays.asList(
            InvestmentReportResponse.builder()
                .id(1L)
                .reportName("Monthly Statement - " + LocalDate.now().getMonth())
                .reportType(InvestmentReportResponse.ReportType.MONTHLY_STATEMENT)
                .generatedDate(LocalDateTime.now().minusDays(1))
                .build()
        );
    }

    private byte[] generateMockReportContent(Long reportId) {
        return "Mock PDF content".getBytes();
    }

    // ================== Sprint 3.2 Enhanced Investment Management Methods ==================

    public InvestmentComparisonResponse compareInvestments(Long clientId, InvestmentComparisonRequest request) {
        log.info("Comparing investments for client: {} with request: {}", clientId, request);
        
        List<Investment> investments = request.getInvestmentIds().stream()
            .map(id -> investmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found: " + id)))
            .filter(inv -> inv.getClient().getId().equals(clientId))
            .collect(Collectors.toList());
        
        return buildInvestmentComparison(investments, request);
    }

    public InvestmentAnalyticsResponse getInvestmentAnalytics(Long clientId) {
        log.info("Getting investment analytics for client: {}", clientId);
        
        List<Investment> investments = investmentRepository.findByClientId(clientId);
        
        return buildInvestmentAnalytics(investments);
    }

    public FilteredInvestmentsResponse advancedFilterInvestments(Long clientId, EnhancedInvestmentFilterRequest filterRequest) {
        log.info("Advanced filtering investments for client: {} with filters: {}", clientId, filterRequest);
        
        List<Investment> allInvestments = investmentRepository.findByClientId(clientId);
        List<Investment> filteredInvestments = applyAdvancedFilters(allInvestments, filterRequest);
        
        return buildAdvancedFilteredResponse(filteredInvestments, filterRequest);
    }

    public List<ActiveInvestmentResponse> getDetailedActiveInvestments(Long clientId, Boolean includeProjections, Boolean includeRiskMetrics) {
        log.info("Getting detailed active investments for client: {}", clientId);
        
        List<Investment> activeInvestments = investmentRepository
            .findByClientIdAndStatus(clientId, Investment.InvestmentStatus.ACTIVE);
        
        return activeInvestments.stream()
            .map(inv -> mapToDetailedActiveInvestmentResponse(inv, includeProjections, includeRiskMetrics))
            .collect(Collectors.toList());
    }

    public List<CompletedInvestmentResponse> getInvestmentHistoryWithAnalytics(Long clientId, Boolean includePerformanceAnalysis, Boolean includeLessonsLearned) {
        log.info("Getting investment history with analytics for client: {}", clientId);
        
        List<Investment> completedInvestments = investmentRepository
            .findByClientIdAndStatus(clientId, Investment.InvestmentStatus.MATURED);
        
        return completedInvestments.stream()
            .map(inv -> mapToAnalyticalCompletedInvestmentResponse(inv, includePerformanceAnalysis, includeLessonsLearned))
            .collect(Collectors.toList());
    }

    public List<InvestmentAnalyticsResponse.InvestmentInsight> getInvestmentInsights(Long clientId) {
        log.info("Getting investment insights for client: {}", clientId);
        
        List<Investment> investments = investmentRepository.findByClientId(clientId);
        
        return generateInvestmentInsights(investments);
    }

    public List<InvestmentAnalyticsResponse.InvestmentRecommendation> getInvestmentRecommendations(Long clientId) {
        log.info("Getting investment recommendations for client: {}", clientId);
        
        List<Investment> investments = investmentRepository.findByClientId(clientId);
        
        return generateInvestmentRecommendations(investments);
    }

    public InvestmentAnalyticsResponse getPortfolioOptimization(Long clientId) {
        log.info("Getting portfolio optimization for client: {}", clientId);
        
        List<Investment> investments = investmentRepository.findByClientId(clientId);
        
        return buildPortfolioOptimization(investments);
    }

    public InvestmentDetailResponse getInvestmentTracking(Long clientId, Long investmentId, int months) {
        log.info("Getting investment tracking for client: {} and investment: {} over {} months", clientId, investmentId, months);
        
        Investment investment = investmentRepository.findById(investmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));
        
        if (!investment.getClient().getId().equals(clientId)) {
            throw new ResourceNotFoundException("Investment not found for this client");
        }
        
        return mapToInvestmentTrackingResponse(investment, months);
    }

    public List<InvestmentAlert> getInvestmentAlerts(Long clientId) {
        log.info("Getting investment alerts for client: {}", clientId);
        
        List<Investment> investments = investmentRepository.findByClientId(clientId);
        
        return generateInvestmentAlerts(investments, clientId);
    }

    // Enhanced helper methods for Sprint 3.2

    private InvestmentComparisonResponse buildInvestmentComparison(List<Investment> investments, InvestmentComparisonRequest request) {
        List<InvestmentComparisonResponse.InvestmentComparisonItem> comparisonItems = investments.stream()
            .map(this::mapToComparisonItem)
            .collect(Collectors.toList());
        
        return InvestmentComparisonResponse.builder()
            .investments(comparisonItems)
            .summary(buildComparisonSummary(comparisonItems))
            .insights(buildComparisonInsights(comparisonItems))
            .build();
    }

    private InvestmentAnalyticsResponse buildInvestmentAnalytics(List<Investment> investments) {
        return InvestmentAnalyticsResponse.builder()
            .performance(buildPerformanceAnalytics(investments))
            .risk(buildRiskAnalytics(investments))
            .trends(buildTrendAnalytics(investments))
            .diversification(buildDiversificationAnalytics(investments))
            .insights(generateInvestmentInsights(investments))
            .recommendations(generateInvestmentRecommendations(investments))
            .build();
    }

    private List<Investment> applyAdvancedFilters(List<Investment> investments, EnhancedInvestmentFilterRequest filterRequest) {
        return investments.stream()
            .filter(inv -> applyFinancialFilters(inv, filterRequest))
            .filter(inv -> applyPerformanceFilters(inv, filterRequest))
            .filter(inv -> applyTimeFilters(inv, filterRequest))
            .filter(inv -> applyRiskFilters(inv, filterRequest))
            .collect(Collectors.toList());
    }

    private ActiveInvestmentResponse mapToDetailedActiveInvestmentResponse(Investment investment, Boolean includeProjections, Boolean includeRiskMetrics) {
        ActiveInvestmentResponse response = mapToActiveInvestmentResponse(investment);
        
        if (includeProjections) {
            response.setPerformanceStatus(calculatePerformanceStatus(investment));
        }
        
        if (includeRiskMetrics) {
            response.setVolatilityIndex(calculateVolatility(investment));
            response.setRiskLevel(determineRiskLevel(investment));
        }
        
        return response;
    }

    private CompletedInvestmentResponse mapToAnalyticalCompletedInvestmentResponse(Investment investment, Boolean includePerformanceAnalysis, Boolean includeLessonsLearned) {
        CompletedInvestmentResponse response = mapToCompletedInvestmentResponse(investment);
        
        if (includePerformanceAnalysis) {
            response.setPerformanceOutcome(calculatePerformanceOutcome(investment));
            response.setAnnualizedReturn(calculateAnnualizedReturn(investment));
        }
        
        if (includeLessonsLearned) {
            response.setExitReason(determineExitReason(investment));
            response.setExitMethod(determineExitMethod(investment));
        }
        
        return response;
    }

    // Mock implementations for complex calculations
    private InvestmentComparisonResponse.InvestmentComparisonItem mapToComparisonItem(Investment investment) {
        return InvestmentComparisonResponse.InvestmentComparisonItem.builder()
            .id(investment.getId())
            .investmentName(investment.getInvestmentName())
            .investmentType(investment.getInvestmentType().getDisplayName())
            .riskRating(investment.getRiskRating().getDisplayName())
            .initialAmount(investment.getInitialAmount())
            .currentValue(investment.getCurrentValue())
            .totalReturn(investment.getCurrentReturn())
            .returnPercentage(investment.getCurrentReturnPercentage())
            .purchaseDate(investment.getPurchaseDate())
            .maturityDate(investment.getMaturityDate())
            .build();
    }

    private InvestmentComparisonResponse.ComparisonSummary buildComparisonSummary(List<InvestmentComparisonResponse.InvestmentComparisonItem> items) {
        return InvestmentComparisonResponse.ComparisonSummary.builder().build();
    }

    private InvestmentComparisonResponse.ComparisonInsights buildComparisonInsights(List<InvestmentComparisonResponse.InvestmentComparisonItem> items) {
        return InvestmentComparisonResponse.ComparisonInsights.builder().build();
    }

    private InvestmentAnalyticsResponse.PerformanceAnalytics buildPerformanceAnalytics(List<Investment> investments) {
        return InvestmentAnalyticsResponse.PerformanceAnalytics.builder().build();
    }

    private InvestmentAnalyticsResponse.RiskAnalytics buildRiskAnalytics(List<Investment> investments) {
        return InvestmentAnalyticsResponse.RiskAnalytics.builder().build();
    }

    private InvestmentAnalyticsResponse.TrendAnalytics buildTrendAnalytics(List<Investment> investments) {
        return InvestmentAnalyticsResponse.TrendAnalytics.builder().build();
    }

    private InvestmentAnalyticsResponse.DiversificationAnalytics buildDiversificationAnalytics(List<Investment> investments) {
        return InvestmentAnalyticsResponse.DiversificationAnalytics.builder().build();
    }

    private List<InvestmentAnalyticsResponse.InvestmentInsight> generateInvestmentInsights(List<Investment> investments) {
        return new ArrayList<>();
    }

    private List<InvestmentAnalyticsResponse.InvestmentRecommendation> generateInvestmentRecommendations(List<Investment> investments) {
        return new ArrayList<>();
    }

    private InvestmentAnalyticsResponse buildPortfolioOptimization(List<Investment> investments) {
        return InvestmentAnalyticsResponse.builder().build();
    }

    private FilteredInvestmentsResponse buildAdvancedFilteredResponse(List<Investment> investments, EnhancedInvestmentFilterRequest filterRequest) {
        return FilteredInvestmentsResponse.builder().build();
    }

    private InvestmentDetailResponse mapToInvestmentTrackingResponse(Investment investment, int months) {
        InvestmentDetailResponse response = mapToInvestmentDetailResponse(investment);
        response.setPerformanceHistory(generatePerformanceHistory(investment, months));
        response.setPerformanceMetrics(calculatePerformanceMetrics(investment));
        return response;
    }

    private List<InvestmentAlert> generateInvestmentAlerts(List<Investment> investments, Long clientId) {
        List<InvestmentAlert> alerts = new ArrayList<>();
        alerts.addAll(checkMaturityAlerts(investments));
        alerts.addAll(checkPerformanceAlerts(investments));
        return alerts;
    }

    // Filter and calculation helper methods (mock implementations)
    private boolean applyFinancialFilters(Investment investment, EnhancedInvestmentFilterRequest filter) { return true; }
    private boolean applyPerformanceFilters(Investment investment, EnhancedInvestmentFilterRequest filter) { return true; }
    private boolean applyTimeFilters(Investment investment, EnhancedInvestmentFilterRequest filter) { return true; }
    private boolean applyRiskFilters(Investment investment, EnhancedInvestmentFilterRequest filter) { return true; }
    
    private ActiveInvestmentResponse.PerformanceStatus calculatePerformanceStatus(Investment investment) { return ActiveInvestmentResponse.PerformanceStatus.GOOD; }
    private BigDecimal calculateVolatility(Investment investment) { return BigDecimal.ZERO; }
    private String determineRiskLevel(Investment investment) { return "Medium"; }
    private CompletedInvestmentResponse.PerformanceOutcome calculatePerformanceOutcome(Investment investment) { return CompletedInvestmentResponse.PerformanceOutcome.MET_EXPECTATIONS; }
    private BigDecimal calculateAnnualizedReturn(Investment investment) { return BigDecimal.ZERO; }
    private String determineExitReason(Investment investment) { return "Maturity"; }
    private String determineExitMethod(Investment investment) { return "Normal Maturity"; }
    
    private List<InvestmentDetailResponse.PerformanceDataPoint> generatePerformanceHistory(Investment investment, int months) { return new ArrayList<>(); }
    private InvestmentDetailResponse.PerformanceMetrics calculatePerformanceMetrics(Investment investment) { return InvestmentDetailResponse.PerformanceMetrics.builder().build(); }
    
    private List<InvestmentAlert> checkMaturityAlerts(List<Investment> investments) { return new ArrayList<>(); }
    private List<InvestmentAlert> checkPerformanceAlerts(List<Investment> investments) { return new ArrayList<>(); }
} 