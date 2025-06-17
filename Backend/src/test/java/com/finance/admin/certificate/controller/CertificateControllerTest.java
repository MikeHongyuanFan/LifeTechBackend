package com.finance.admin.certificate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.admin.certificate.dto.CertificateResponse;
import com.finance.admin.certificate.dto.CreateCertificateRequest;
import com.finance.admin.certificate.model.Certificate;
import com.finance.admin.certificate.service.CertificateService;
import com.finance.admin.certificate.service.CertificateExpiryMonitoringService;
import com.finance.admin.certificate.service.CertificateEmailService;
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
 * Comprehensive unit tests for CertificateController
 * Tests all certificate management endpoints with various scenarios
 */
@DisplayName("Certificate Controller Tests")
public class CertificateControllerTest extends BaseUnitTest {

    private MockMvc mockMvc;
    
    @Mock
    private CertificateService certificateService;
    
    @Mock
    private CertificateExpiryMonitoringService expiryMonitoringService;
    
    @Mock
    private CertificateEmailService certificateEmailService;
    
    private CertificateController certificateController;
    private ObjectMapper objectMapper;
    
    // Test data
    private CreateCertificateRequest mockCreateRequest;
    private CertificateResponse mockCertificateResponse;
    private Certificate mockCertificate;

    @BeforeEach
    protected void setUp() {
        super.setUp();
        MockitoAnnotations.openMocks(this);
        
        certificateController = new CertificateController(
            certificateService, expiryMonitoringService, certificateEmailService);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        
        // Setup MockMvc with proper configuration
        mockMvc = MockMvcBuilders.standaloneSetup(certificateController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        // Setup test data
        setupTestData();
    }

    private void setupTestData() {
        // Setup mock create request
        mockCreateRequest = CreateCertificateRequest.builder()
                .investmentId(1L)
                .clientId(100L)
                .certificateType(Certificate.CertificateType.SHARE_CERTIFICATE)
                .templateId(1L)
                .issueDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusYears(5))
                .investmentAmount(new BigDecimal("50000.00"))
                .numberOfShares(new BigDecimal("1000"))
                .sharePrice(new BigDecimal("50.00"))
                .generateImmediately(true)
                .sendNotification(true)
                .notes("Test certificate creation")
                .build();

        // Setup mock certificate response
        mockCertificateResponse = CertificateResponse.builder()
                .id(1L)
                .certificateNumber("CERT-2024-001")
                .certificateType(Certificate.CertificateType.SHARE_CERTIFICATE)
                .certificateTypeDisplay("Share Certificate")
                .status(Certificate.CertificateStatus.ACTIVE)
                .statusDisplay("Active")
                .issueDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusYears(5))
                .version(1)
                .investmentId(1L)
                .investmentName("Property Investment Fund")
                .investmentType("PROPERTY")
                .investmentAmount(new BigDecimal("50000.00"))
                .numberOfShares(new BigDecimal("1000"))
                .sharePrice(new BigDecimal("50.00"))
                .clientId(100L)
                .clientName("John Smith")
                .clientEmail("john.smith@example.com")
                .membershipNumber("MEM-001")
                .templateId(1L)
                .templateName("Standard Share Certificate")
                .filePath("/certificates/CERT-2024-001.pdf")
                .fileSize(2048L)
                .fileHash("abc123hash")
                .hasFile(true)
                .downloadUrl("/api/admin/certificates/1/download")
                .isActive(true)
                .isExpired(false)
                .daysUntilExpiry(1825L)
                .certificateAge("0 days")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Setup mock certificate entity
        mockCertificate = Certificate.builder()
                .id(1L)
                .certificateNumber("CERT-2024-001")
                .certificateType(Certificate.CertificateType.SHARE_CERTIFICATE)
                .status(Certificate.CertificateStatus.ACTIVE)
                .issueDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusYears(5))
                .investmentAmount(new BigDecimal("50000.00"))
                .numberOfShares(new BigDecimal("1000"))
                .sharePrice(new BigDecimal("50.00"))
                .filePath("/certificates/CERT-2024-001.pdf")
                .fileSize(2048L)
                .fileHash("abc123hash")
                .version(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // =========================== CREATE CERTIFICATE TESTS ===========================
    
    @Test
    @DisplayName("Should create certificate successfully")
    void testCreateCertificate_Success() throws Exception {
        when(certificateService.createCertificate(any(CreateCertificateRequest.class)))
                .thenReturn(mockCertificateResponse);

        mockMvc.perform(post("/api/admin/certificates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.certificateNumber").value("CERT-2024-001"))
                .andExpect(jsonPath("$.certificateType").value("SHARE_CERTIFICATE"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.clientId").value(100L))
                .andExpect(jsonPath("$.investmentId").value(1L))
                .andExpect(jsonPath("$.investmentAmount").value(50000.00))
                .andExpect(jsonPath("$.hasFile").value(true));

        verify(certificateService).createCertificate(any(CreateCertificateRequest.class));
    }

    @Test
    @DisplayName("Should handle validation errors for create certificate")
    void testCreateCertificate_ValidationError() throws Exception {
        CreateCertificateRequest invalidRequest = CreateCertificateRequest.builder()
                .certificateType(Certificate.CertificateType.SHARE_CERTIFICATE)
                // Missing required fields: investmentId, clientId
                .build();

        mockMvc.perform(post("/api/admin/certificates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle service exception during certificate creation")
    void testCreateCertificate_ServiceException() throws Exception {
        when(certificateService.createCertificate(any(CreateCertificateRequest.class)))
                .thenThrow(new RuntimeException("Investment not found"));

        mockMvc.perform(post("/api/admin/certificates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockCreateRequest)))
                .andExpect(status().isInternalServerError());
    }

    // =========================== GET ALL CERTIFICATES TESTS ===========================

    @Test
    @DisplayName("Should get all certificates with pagination")
    void testGetAllCertificates_Success() throws Exception {
        PageImpl<CertificateResponse> certificatePage = new PageImpl<>(
                Collections.singletonList(mockCertificateResponse),
                PageRequest.of(0, 20), 1);

        when(certificateService.getAllCertificates(any(Pageable.class)))
                .thenReturn(certificatePage);

        mockMvc.perform(get("/api/admin/certificates")
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.number").value(0));

        verify(certificateService).getAllCertificates(any(Pageable.class));
    }

    @Test
    @DisplayName("Should get all certificates with default pagination")
    void testGetAllCertificates_DefaultPagination() throws Exception {
        PageImpl<CertificateResponse> certificatePage = new PageImpl<>(
                Collections.singletonList(mockCertificateResponse),
                PageRequest.of(0, 20), 1);

        when(certificateService.getAllCertificates(any(Pageable.class)))
                .thenReturn(certificatePage);

        mockMvc.perform(get("/api/admin/certificates")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(certificateService).getAllCertificates(any(Pageable.class));
    }

    // =========================== GET CERTIFICATE BY ID TESTS ===========================

    @Test
    @DisplayName("Should get certificate by ID successfully")
    void testGetCertificateById_Success() throws Exception {
        when(certificateService.getCertificateById(1L))
                .thenReturn(mockCertificateResponse);

        mockMvc.perform(get("/api/admin/certificates/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.certificateNumber").value("CERT-2024-001"))
                .andExpect(jsonPath("$.clientName").value("John Smith"));

        verify(certificateService).getCertificateById(1L);
    }

    @Test
    @DisplayName("Should handle certificate not found by ID")
    void testGetCertificateById_NotFound() throws Exception {
        when(certificateService.getCertificateById(999L))
                .thenThrow(new RuntimeException("Certificate not found with ID: 999"));

        mockMvc.perform(get("/api/admin/certificates/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(certificateService).getCertificateById(999L);
    }

    @Test
    @DisplayName("Should handle invalid certificate ID format")
    void testGetCertificateById_InvalidIdFormat() throws Exception {
        mockMvc.perform(get("/api/admin/certificates/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // =========================== GET CERTIFICATE BY NUMBER TESTS ===========================

    @Test
    @DisplayName("Should get certificate by certificate number successfully")
    void testGetCertificateByCertificateNumber_Success() throws Exception {
        when(certificateService.getCertificateByCertificateNumber("CERT-2024-001"))
                .thenReturn(mockCertificateResponse);

        mockMvc.perform(get("/api/admin/certificates/number/CERT-2024-001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.certificateNumber").value("CERT-2024-001"))
                .andExpect(jsonPath("$.clientName").value("John Smith"));

        verify(certificateService).getCertificateByCertificateNumber("CERT-2024-001");
    }

    @Test
    @DisplayName("Should handle certificate not found by number")
    void testGetCertificateByCertificateNumber_NotFound() throws Exception {
        when(certificateService.getCertificateByCertificateNumber("INVALID-CERT"))
                .thenThrow(new RuntimeException("Certificate not found with number: INVALID-CERT"));

        mockMvc.perform(get("/api/admin/certificates/number/INVALID-CERT")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(certificateService).getCertificateByCertificateNumber("INVALID-CERT");
    }

    // =========================== SEARCH CERTIFICATES TESTS ===========================

    @Test
    @DisplayName("Should search certificates with all filters")
    void testSearchCertificates_WithAllFilters() throws Exception {
        PageImpl<CertificateResponse> searchResults = new PageImpl<>(
                Collections.singletonList(mockCertificateResponse),
                PageRequest.of(0, 20), 1);

        when(certificateService.searchCertificates(
                eq(100L), eq(1L), eq(Certificate.CertificateType.SHARE_CERTIFICATE),
                eq(Certificate.CertificateStatus.ACTIVE), any(LocalDate.class),
                any(LocalDate.class), eq("John"), any(Pageable.class)))
                .thenReturn(searchResults);

        mockMvc.perform(get("/api/admin/certificates/search")
                .param("clientId", "100")
                .param("investmentId", "1")
                .param("certificateType", "SHARE_CERTIFICATE")
                .param("status", "ACTIVE")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-12-31")
                .param("searchTerm", "John")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].certificateNumber").value("CERT-2024-001"));

        verify(certificateService).searchCertificates(
                eq(100L), eq(1L), eq(Certificate.CertificateType.SHARE_CERTIFICATE),
                eq(Certificate.CertificateStatus.ACTIVE), any(LocalDate.class),
                any(LocalDate.class), eq("John"), any(Pageable.class));
    }

    @Test
    @DisplayName("Should search certificates with minimal filters")
    void testSearchCertificates_MinimalFilters() throws Exception {
        PageImpl<CertificateResponse> searchResults = new PageImpl<>(
                Collections.singletonList(mockCertificateResponse),
                PageRequest.of(0, 20), 1);

        when(certificateService.searchCertificates(
                isNull(), isNull(), isNull(), isNull(), 
                isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(searchResults);

        mockMvc.perform(get("/api/admin/certificates/search")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(certificateService).searchCertificates(
                isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), any(Pageable.class));
    }

    // =========================== GET CERTIFICATES BY CLIENT TESTS ===========================

    @Test
    @DisplayName("Should get certificates by client ID successfully")
    void testGetCertificatesByClientId_Success() throws Exception {
        PageImpl<CertificateResponse> certificatePage = new PageImpl<>(
                Collections.singletonList(mockCertificateResponse),
                PageRequest.of(0, 20), 1);

        when(certificateService.getCertificatesByClientId(eq(100L), any(Pageable.class)))
                .thenReturn(certificatePage);

        mockMvc.perform(get("/api/admin/certificates/client/100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].clientId").value(100L));

        verify(certificateService).getCertificatesByClientId(eq(100L), any(Pageable.class));
    }

    @Test
    @DisplayName("Should handle invalid client ID")
    void testGetCertificatesByClientId_InvalidClient() throws Exception {
        when(certificateService.getCertificatesByClientId(eq(999L), any(Pageable.class)))
                .thenThrow(new RuntimeException("Client not found with ID: 999"));

        mockMvc.perform(get("/api/admin/certificates/client/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(certificateService).getCertificatesByClientId(eq(999L), any(Pageable.class));
    }

    // =========================== GENERATE PDF TESTS ===========================

    @Test
    @DisplayName("Should generate certificate PDF successfully")
    void testGenerateCertificatePdf_Success() throws Exception {
        when(certificateService.generateCertificatePdf(1L))
                .thenReturn(mockCertificateResponse);

        mockMvc.perform(post("/api/admin/certificates/1/generate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.hasFile").value(true))
                .andExpect(jsonPath("$.filePath").value("/certificates/CERT-2024-001.pdf"));

        verify(certificateService).generateCertificatePdf(1L);
    }

    @Test
    @DisplayName("Should handle PDF generation failure")
    void testGenerateCertificatePdf_Failure() throws Exception {
        when(certificateService.generateCertificatePdf(1L))
                .thenThrow(new RuntimeException("PDF generation failed"));

        mockMvc.perform(post("/api/admin/certificates/1/generate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(certificateService).generateCertificatePdf(1L);
    }

    // =========================== BATCH GENERATE TESTS ===========================

    @Test
    @DisplayName("Should generate batch certificates successfully")
    void testGenerateBatchCertificates_Success() throws Exception {
        List<Long> certificateIds = Arrays.asList(1L, 2L, 3L);
        Map<String, CertificateResponse> batchResults = new HashMap<>();
        batchResults.put("CERT-2024-001", mockCertificateResponse);

        when(certificateService.generateBatchCertificates(certificateIds))
                .thenReturn(batchResults);

        mockMvc.perform(post("/api/admin/certificates/batch-generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(certificateIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['CERT-2024-001'].id").value(1L));

        verify(certificateService).generateBatchCertificates(certificateIds);
    }

    @Test
    @DisplayName("Should handle empty batch generation request")
    void testGenerateBatchCertificates_EmptyList() throws Exception {
        List<Long> emptyList = Collections.emptyList();

        when(certificateService.generateBatchCertificates(emptyList))
                .thenReturn(Collections.emptyMap());

        mockMvc.perform(post("/api/admin/certificates/batch-generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyList)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(certificateService).generateBatchCertificates(emptyList);
    }

    // =========================== UPDATE STATUS TESTS ===========================

    @Test
    @DisplayName("Should update certificate status successfully")
    void testUpdateCertificateStatus_Success() throws Exception {
        CertificateResponse updatedResponse = CertificateResponse.builder()
                .id(mockCertificateResponse.getId())
                .certificateNumber(mockCertificateResponse.getCertificateNumber())
                .certificateType(mockCertificateResponse.getCertificateType())
                .certificateTypeDisplay(mockCertificateResponse.getCertificateTypeDisplay())
                .status(Certificate.CertificateStatus.INACTIVE)
                .statusDisplay("Inactive")
                .issueDate(mockCertificateResponse.getIssueDate())
                .expiryDate(mockCertificateResponse.getExpiryDate())
                .version(mockCertificateResponse.getVersion())
                .investmentId(mockCertificateResponse.getInvestmentId())
                .investmentName(mockCertificateResponse.getInvestmentName())
                .investmentType(mockCertificateResponse.getInvestmentType())
                .investmentAmount(mockCertificateResponse.getInvestmentAmount())
                .numberOfShares(mockCertificateResponse.getNumberOfShares())
                .sharePrice(mockCertificateResponse.getSharePrice())
                .clientId(mockCertificateResponse.getClientId())
                .clientName(mockCertificateResponse.getClientName())
                .clientEmail(mockCertificateResponse.getClientEmail())
                .membershipNumber(mockCertificateResponse.getMembershipNumber())
                .templateId(mockCertificateResponse.getTemplateId())
                .templateName(mockCertificateResponse.getTemplateName())
                .filePath(mockCertificateResponse.getFilePath())
                .fileSize(mockCertificateResponse.getFileSize())
                .fileHash(mockCertificateResponse.getFileHash())
                .hasFile(mockCertificateResponse.isHasFile())
                .downloadUrl(mockCertificateResponse.getDownloadUrl())
                .isActive(mockCertificateResponse.isActive())
                .isExpired(mockCertificateResponse.isExpired())
                .daysUntilExpiry(mockCertificateResponse.getDaysUntilExpiry())
                .certificateAge(mockCertificateResponse.getCertificateAge())
                .createdAt(mockCertificateResponse.getCreatedAt())
                .updatedAt(mockCertificateResponse.getUpdatedAt())
                .build();

        when(certificateService.updateCertificateStatus(1L, Certificate.CertificateStatus.INACTIVE))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/admin/certificates/1/status")
                .param("status", "INACTIVE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.statusDisplay").value("Inactive"));

        verify(certificateService).updateCertificateStatus(1L, Certificate.CertificateStatus.INACTIVE);
    }

    @Test
    @DisplayName("Should handle invalid status value")
    void testUpdateCertificateStatus_InvalidStatus() throws Exception {
        mockMvc.perform(put("/api/admin/certificates/1/status")
                .param("status", "INVALID_STATUS")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // =========================== DELETE CERTIFICATE TESTS ===========================

    @Test
    @DisplayName("Should delete certificate successfully")
    void testDeleteCertificate_Success() throws Exception {
        doNothing().when(certificateService).deleteCertificate(1L);

        mockMvc.perform(delete("/api/admin/certificates/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(certificateService).deleteCertificate(1L);
    }

    @Test
    @DisplayName("Should handle delete of non-existent certificate")
    void testDeleteCertificate_NotFound() throws Exception {
        doThrow(new RuntimeException("Certificate not found with ID: 999"))
                .when(certificateService).deleteCertificate(999L);

        mockMvc.perform(delete("/api/admin/certificates/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(certificateService).deleteCertificate(999L);
    }

    // =========================== GET STATISTICS TESTS ===========================

    @Test
    @DisplayName("Should get certificate statistics successfully")
    void testGetCertificateStats_Success() throws Exception {
        Map<String, Object> mockStats = createMockCertificateStats();

        when(certificateService.getCertificateStats())
                .thenReturn(mockStats);

        mockMvc.perform(get("/api/admin/certificates/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCertificates").value(100))
                .andExpect(jsonPath("$.activeCertificates").value(85))
                .andExpect(jsonPath("$.pendingCertificates").value(10))
                .andExpect(jsonPath("$.expiredCertificates").value(5))
                .andExpect(jsonPath("$.newCertificatesLast30Days").value(15));

        verify(certificateService).getCertificateStats();
    }

    // =========================== DOWNLOAD CERTIFICATE TESTS ===========================

    @Test
    @DisplayName("Should download certificate successfully")
    void testDownloadCertificate_Success() throws Exception {
        when(certificateService.getCertificateById(1L))
                .thenReturn(mockCertificateResponse);

        mockMvc.perform(get("/api/admin/certificates/1/download")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", 
                        "attachment; filename=\"CERT-2024-001.pdf\""));

        verify(certificateService).getCertificateById(1L);
    }

    @Test
    @DisplayName("Should handle download of certificate without file")
    void testDownloadCertificate_NoFile() throws Exception {
        CertificateResponse responseWithoutFile = CertificateResponse.builder()
                .id(mockCertificateResponse.getId())
                .certificateNumber(mockCertificateResponse.getCertificateNumber())
                .certificateType(mockCertificateResponse.getCertificateType())
                .certificateTypeDisplay(mockCertificateResponse.getCertificateTypeDisplay())
                .status(mockCertificateResponse.getStatus())
                .statusDisplay(mockCertificateResponse.getStatusDisplay())
                .issueDate(mockCertificateResponse.getIssueDate())
                .expiryDate(mockCertificateResponse.getExpiryDate())
                .version(mockCertificateResponse.getVersion())
                .investmentId(mockCertificateResponse.getInvestmentId())
                .investmentName(mockCertificateResponse.getInvestmentName())
                .investmentType(mockCertificateResponse.getInvestmentType())
                .investmentAmount(mockCertificateResponse.getInvestmentAmount())
                .numberOfShares(mockCertificateResponse.getNumberOfShares())
                .sharePrice(mockCertificateResponse.getSharePrice())
                .clientId(mockCertificateResponse.getClientId())
                .clientName(mockCertificateResponse.getClientName())
                .clientEmail(mockCertificateResponse.getClientEmail())
                .membershipNumber(mockCertificateResponse.getMembershipNumber())
                .templateId(mockCertificateResponse.getTemplateId())
                .templateName(mockCertificateResponse.getTemplateName())
                .hasFile(false)
                .filePath(null)
                .fileSize(mockCertificateResponse.getFileSize())
                .fileHash(mockCertificateResponse.getFileHash())
                .downloadUrl(mockCertificateResponse.getDownloadUrl())
                .isActive(mockCertificateResponse.isActive())
                .isExpired(mockCertificateResponse.isExpired())
                .daysUntilExpiry(mockCertificateResponse.getDaysUntilExpiry())
                .certificateAge(mockCertificateResponse.getCertificateAge())
                .createdAt(mockCertificateResponse.getCreatedAt())
                .updatedAt(mockCertificateResponse.getUpdatedAt())
                .build();

        when(certificateService.getCertificateById(1L))
                .thenReturn(responseWithoutFile);

        mockMvc.perform(get("/api/admin/certificates/1/download")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(certificateService).getCertificateById(1L);
    }

    // =========================== VERIFY CERTIFICATE TESTS ===========================

    @Test
    @DisplayName("Should verify certificate successfully")
    void testVerifyCertificate_Success() throws Exception {
        when(certificateService.getCertificateById(1L))
                .thenReturn(mockCertificateResponse);

        mockMvc.perform(post("/api/admin/certificates/1/verify")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.certificateNumber").value("CERT-2024-001"))
                .andExpect(jsonPath("$.isValid").value(true))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.clientName").value("John Smith"))
                .andExpect(jsonPath("$.verifiedAt").exists());

        verify(certificateService).getCertificateById(1L);
    }

    @Test
    @DisplayName("Should verify expired certificate")
    void testVerifyCertificate_Expired() throws Exception {
        CertificateResponse expiredResponse = CertificateResponse.builder()
                .id(mockCertificateResponse.getId())
                .certificateNumber(mockCertificateResponse.getCertificateNumber())
                .certificateType(mockCertificateResponse.getCertificateType())
                .certificateTypeDisplay(mockCertificateResponse.getCertificateTypeDisplay())
                .status(Certificate.CertificateStatus.EXPIRED)
                .statusDisplay("Expired")
                .issueDate(mockCertificateResponse.getIssueDate())
                .expiryDate(mockCertificateResponse.getExpiryDate())
                .version(mockCertificateResponse.getVersion())
                .investmentId(mockCertificateResponse.getInvestmentId())
                .investmentName(mockCertificateResponse.getInvestmentName())
                .investmentType(mockCertificateResponse.getInvestmentType())
                .investmentAmount(mockCertificateResponse.getInvestmentAmount())
                .numberOfShares(mockCertificateResponse.getNumberOfShares())
                .sharePrice(mockCertificateResponse.getSharePrice())
                .clientId(mockCertificateResponse.getClientId())
                .clientName(mockCertificateResponse.getClientName())
                .clientEmail(mockCertificateResponse.getClientEmail())
                .membershipNumber(mockCertificateResponse.getMembershipNumber())
                .templateId(mockCertificateResponse.getTemplateId())
                .templateName(mockCertificateResponse.getTemplateName())
                .filePath(mockCertificateResponse.getFilePath())
                .fileSize(mockCertificateResponse.getFileSize())
                .fileHash(mockCertificateResponse.getFileHash())
                .hasFile(mockCertificateResponse.isHasFile())
                .downloadUrl(mockCertificateResponse.getDownloadUrl())
                .isActive(false)
                .isExpired(true)
                .daysUntilExpiry(mockCertificateResponse.getDaysUntilExpiry())
                .certificateAge(mockCertificateResponse.getCertificateAge())
                .createdAt(mockCertificateResponse.getCreatedAt())
                .updatedAt(mockCertificateResponse.getUpdatedAt())
                .build();

        when(certificateService.getCertificateById(1L))
                .thenReturn(expiredResponse);

        mockMvc.perform(post("/api/admin/certificates/1/verify")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isValid").value(false))
                .andExpect(jsonPath("$.status").value("EXPIRED"));

        verify(certificateService).getCertificateById(1L);
    }

    // =========================== REVOKE CERTIFICATE TESTS ===========================

    @Test
    @DisplayName("Should revoke certificate successfully")
    void testRevokeCertificate_Success() throws Exception {
        CertificateResponse revokedResponse = CertificateResponse.builder()
                .id(mockCertificateResponse.getId())
                .certificateNumber(mockCertificateResponse.getCertificateNumber())
                .certificateType(mockCertificateResponse.getCertificateType())
                .certificateTypeDisplay(mockCertificateResponse.getCertificateTypeDisplay())
                .status(Certificate.CertificateStatus.REVOKED)
                .statusDisplay("Revoked")
                .issueDate(mockCertificateResponse.getIssueDate())
                .expiryDate(mockCertificateResponse.getExpiryDate())
                .version(mockCertificateResponse.getVersion())
                .investmentId(mockCertificateResponse.getInvestmentId())
                .investmentName(mockCertificateResponse.getInvestmentName())
                .investmentType(mockCertificateResponse.getInvestmentType())
                .investmentAmount(mockCertificateResponse.getInvestmentAmount())
                .numberOfShares(mockCertificateResponse.getNumberOfShares())
                .sharePrice(mockCertificateResponse.getSharePrice())
                .clientId(mockCertificateResponse.getClientId())
                .clientName(mockCertificateResponse.getClientName())
                .clientEmail(mockCertificateResponse.getClientEmail())
                .membershipNumber(mockCertificateResponse.getMembershipNumber())
                .templateId(mockCertificateResponse.getTemplateId())
                .templateName(mockCertificateResponse.getTemplateName())
                .filePath(mockCertificateResponse.getFilePath())
                .fileSize(mockCertificateResponse.getFileSize())
                .fileHash(mockCertificateResponse.getFileHash())
                .hasFile(mockCertificateResponse.isHasFile())
                .downloadUrl(mockCertificateResponse.getDownloadUrl())
                .isActive(false)
                .isExpired(mockCertificateResponse.isExpired())
                .daysUntilExpiry(mockCertificateResponse.getDaysUntilExpiry())
                .certificateAge(mockCertificateResponse.getCertificateAge())
                .createdAt(mockCertificateResponse.getCreatedAt())
                .updatedAt(mockCertificateResponse.getUpdatedAt())
                .build();

        when(certificateService.revokeCertificate(1L, "Security breach"))
                .thenReturn(revokedResponse);

        mockMvc.perform(post("/api/admin/certificates/1/revoke")
                .param("reason", "Security breach")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("REVOKED"));

        verify(certificateService).revokeCertificate(1L, "Security breach");
    }

    @Test
    @DisplayName("Should revoke certificate with default reason")
    void testRevokeCertificate_DefaultReason() throws Exception {
        CertificateResponse revokedResponse = CertificateResponse.builder()
                .id(mockCertificateResponse.getId())
                .certificateNumber(mockCertificateResponse.getCertificateNumber())
                .certificateType(mockCertificateResponse.getCertificateType())
                .certificateTypeDisplay(mockCertificateResponse.getCertificateTypeDisplay())
                .status(Certificate.CertificateStatus.REVOKED)
                .statusDisplay("Revoked")
                .issueDate(mockCertificateResponse.getIssueDate())
                .expiryDate(mockCertificateResponse.getExpiryDate())
                .version(mockCertificateResponse.getVersion())
                .investmentId(mockCertificateResponse.getInvestmentId())
                .investmentName(mockCertificateResponse.getInvestmentName())
                .investmentType(mockCertificateResponse.getInvestmentType())
                .investmentAmount(mockCertificateResponse.getInvestmentAmount())
                .numberOfShares(mockCertificateResponse.getNumberOfShares())
                .sharePrice(mockCertificateResponse.getSharePrice())
                .clientId(mockCertificateResponse.getClientId())
                .clientName(mockCertificateResponse.getClientName())
                .clientEmail(mockCertificateResponse.getClientEmail())
                .membershipNumber(mockCertificateResponse.getMembershipNumber())
                .templateId(mockCertificateResponse.getTemplateId())
                .templateName(mockCertificateResponse.getTemplateName())
                .filePath(mockCertificateResponse.getFilePath())
                .fileSize(mockCertificateResponse.getFileSize())
                .fileHash(mockCertificateResponse.getFileHash())
                .hasFile(mockCertificateResponse.isHasFile())
                .downloadUrl(mockCertificateResponse.getDownloadUrl())
                .isActive(false)
                .isExpired(mockCertificateResponse.isExpired())
                .daysUntilExpiry(mockCertificateResponse.getDaysUntilExpiry())
                .certificateAge(mockCertificateResponse.getCertificateAge())
                .createdAt(mockCertificateResponse.getCreatedAt())
                .updatedAt(mockCertificateResponse.getUpdatedAt())
                .build();

        when(certificateService.revokeCertificate(1L, "Revoked by administrator"))
                .thenReturn(revokedResponse);

        mockMvc.perform(post("/api/admin/certificates/1/revoke")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVOKED"));

        verify(certificateService).revokeCertificate(1L, "Revoked by administrator");
    }

    // =========================== EXPIRY MONITORING TESTS ===========================

    @Test
    @DisplayName("Should get expiry statistics successfully")
    void testGetExpiryStatistics_Success() throws Exception {
        CertificateExpiryMonitoringService.ExpiryStatistics mockStats = 
                createMockExpiryStatistics();

        when(expiryMonitoringService.getExpiryStatistics())
                .thenReturn(mockStats);

        mockMvc.perform(get("/api/admin/certificates/expiry/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(expiryMonitoringService).getExpiryStatistics();
    }

    @Test
    @DisplayName("Should get expiry report successfully")
    void testGetExpiryReport_Success() throws Exception {
        CertificateExpiryMonitoringService.ExpiryReport mockReport = 
                createMockExpiryReport();

        when(expiryMonitoringService.getDetailedExpiryReport())
                .thenReturn(mockReport);

        mockMvc.perform(get("/api/admin/certificates/expiry/report")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(expiryMonitoringService).getDetailedExpiryReport();
    }

    @Test
    @DisplayName("Should get certificates expiring within specified days")
    void testGetCertificatesExpiringWithinDays_Success() throws Exception {
        List<Certificate> expiringCerts = Collections.singletonList(mockCertificate);

        when(expiryMonitoringService.getCertificatesExpiringWithinDays(30))
                .thenReturn(expiringCerts);
        when(certificateService.getCertificateById(1L))
                .thenReturn(mockCertificateResponse);

        mockMvc.perform(get("/api/admin/certificates/expiry/within/30")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(expiryMonitoringService).getCertificatesExpiringWithinDays(30);
        verify(certificateService).getCertificateById(1L);
    }

    @Test
    @DisplayName("Should get overdue certificates successfully")
    void testGetOverdueCertificates_Success() throws Exception {
        List<Certificate> overdueCerts = Collections.singletonList(mockCertificate);

        when(expiryMonitoringService.getOverdueCertificates())
                .thenReturn(overdueCerts);
        when(certificateService.getCertificateById(1L))
                .thenReturn(mockCertificateResponse);

        mockMvc.perform(get("/api/admin/certificates/expiry/overdue")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(expiryMonitoringService).getOverdueCertificates();
        verify(certificateService).getCertificateById(1L);
    }

    @Test
    @DisplayName("Should trigger expiry check successfully")
    void testTriggerExpiryCheck_Success() throws Exception {
        doNothing().when(expiryMonitoringService).performManualExpiryCheck();

        mockMvc.perform(post("/api/admin/certificates/expiry/check")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Expiry check completed successfully"));

        verify(expiryMonitoringService).performManualExpiryCheck();
    }

    @Test
    @DisplayName("Should renew certificate successfully")
    void testRenewCertificate_Success() throws Exception {
        Certificate renewedCert = Certificate.builder()
                .id(mockCertificate.getId())
                .certificateNumber(mockCertificate.getCertificateNumber())
                .certificateType(mockCertificate.getCertificateType())
                .status(mockCertificate.getStatus())
                .issueDate(mockCertificate.getIssueDate())
                .expiryDate(LocalDate.now().plusMonths(12))
                .investmentAmount(mockCertificate.getInvestmentAmount())
                .numberOfShares(mockCertificate.getNumberOfShares())
                .sharePrice(mockCertificate.getSharePrice())
                .filePath(mockCertificate.getFilePath())
                .fileSize(mockCertificate.getFileSize())
                .fileHash(mockCertificate.getFileHash())
                .version(mockCertificate.getVersion())
                .createdAt(mockCertificate.getCreatedAt())
                .updatedAt(mockCertificate.getUpdatedAt())
                .build();

        when(expiryMonitoringService.renewCertificate(1L, 12))
                .thenReturn(renewedCert);
        when(certificateService.getCertificateById(1L))
                .thenReturn(mockCertificateResponse);

        mockMvc.perform(post("/api/admin/certificates/1/renew")
                .param("extensionMonths", "12")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(expiryMonitoringService).renewCertificate(1L, 12);
        verify(certificateService).getCertificateById(1L);
    }

    @Test
    @DisplayName("Should renew certificate with default extension")
    void testRenewCertificate_DefaultExtension() throws Exception {
        Certificate renewedCert = Certificate.builder()
                .id(mockCertificate.getId())
                .certificateNumber(mockCertificate.getCertificateNumber())
                .certificateType(mockCertificate.getCertificateType())
                .status(mockCertificate.getStatus())
                .issueDate(mockCertificate.getIssueDate())
                .expiryDate(LocalDate.now().plusMonths(12))
                .investmentAmount(mockCertificate.getInvestmentAmount())
                .numberOfShares(mockCertificate.getNumberOfShares())
                .sharePrice(mockCertificate.getSharePrice())
                .filePath(mockCertificate.getFilePath())
                .fileSize(mockCertificate.getFileSize())
                .fileHash(mockCertificate.getFileHash())
                .version(mockCertificate.getVersion())
                .createdAt(mockCertificate.getCreatedAt())
                .updatedAt(mockCertificate.getUpdatedAt())
                .build();

        when(expiryMonitoringService.renewCertificate(1L, 12))
                .thenReturn(renewedCert);
        when(certificateService.getCertificateById(1L))
                .thenReturn(mockCertificateResponse);

        mockMvc.perform(post("/api/admin/certificates/1/renew")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(expiryMonitoringService).renewCertificate(1L, 12);
    }

    // =========================== EMAIL FUNCTIONALITY TESTS ===========================

    @Test
    @DisplayName("Should send test email successfully")
    void testSendTestEmail_Success() throws Exception {
        doNothing().when(certificateEmailService)
                .sendTestEmail("test@example.com", "Test Email");

        mockMvc.perform(post("/api/admin/certificates/test-email")
                .param("toEmail", "test@example.com")
                .param("subject", "Test Email")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Test email sent successfully to test@example.com"));

        verify(certificateEmailService).sendTestEmail("test@example.com", "Test Email");
    }

    @Test
    @DisplayName("Should send test email with default subject")
    void testSendTestEmail_DefaultSubject() throws Exception {
        doNothing().when(certificateEmailService)
                .sendTestEmail(eq("test@example.com"), contains("Test Email from LifeTech"));

        mockMvc.perform(post("/api/admin/certificates/test-email")
                .param("toEmail", "test@example.com")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(certificateEmailService).sendTestEmail(eq("test@example.com"), contains("Test Email from LifeTech"));
    }

    @Test
    @DisplayName("Should handle email service failure")
    void testSendTestEmail_ServiceFailure() throws Exception {
        doThrow(new RuntimeException("SMTP server not available"))
                .when(certificateEmailService)
                .sendTestEmail("test@example.com", "Test Email");

        mockMvc.perform(post("/api/admin/certificates/test-email")
                .param("toEmail", "test@example.com")
                .param("subject", "Test Email")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to send test email: SMTP server not available"));

        verify(certificateEmailService).sendTestEmail("test@example.com", "Test Email");
    }

    // =========================== HELPER METHODS ===========================

    private Map<String, Object> createMockCertificateStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCertificates", 100L);
        stats.put("activeCertificates", 85L);
        stats.put("pendingCertificates", 10L);
        stats.put("expiredCertificates", 5L);
        stats.put("newCertificatesLast30Days", 15L);
        
        Map<String, Long> typeStats = new HashMap<>();
        typeStats.put("Share Certificate", 50L);
        typeStats.put("Investment Certificate", 30L);
        typeStats.put("Unit Certificate", 20L);
        stats.put("certificatesByType", typeStats);
        
        stats.put("certificatesExpiringNext30Days", 8L);
        return stats;
    }

    private CertificateExpiryMonitoringService.ExpiryStatistics createMockExpiryStatistics() {
        // Mock implementation - in real tests, this would be based on the actual ExpiryStatistics class
        return mock(CertificateExpiryMonitoringService.ExpiryStatistics.class);
    }

    private CertificateExpiryMonitoringService.ExpiryReport createMockExpiryReport() {
        // Mock implementation - in real tests, this would be based on the actual ExpiryReport class
        return mock(CertificateExpiryMonitoringService.ExpiryReport.class);
    }
} 