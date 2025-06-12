package com.finance.admin.client.document.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.admin.client.document.dto.ClientDocumentResponse;
import com.finance.admin.client.document.dto.DocumentListResponse;
import com.finance.admin.client.document.model.ClientDocument;
import com.finance.admin.client.document.service.ClientDocumentService;
import com.finance.admin.client.document.util.JwtUtils;
import com.finance.admin.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ClientDocumentController - Sprint 3.3 Legal Document Center
 * Tests all REST endpoints for document viewing, uploading, and management
 */
@WebMvcTest(ClientDocumentController.class)
@EnableMethodSecurity
class ClientDocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientDocumentService documentService;

    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private ClientDocumentResponse testDocumentResponse;
    private DocumentListResponse testDocumentListResponse;
    private MockMultipartFile testFile;

    @BeforeEach
    void setUp() {
        // Create test document response
        testDocumentResponse = ClientDocumentResponse.builder()
                .id(1L)
                .documentName("Test Document.pdf")
                .documentType(ClientDocument.DocumentType.KYC_DOCUMENT)
                .documentTypeDisplayName("KYC Document")
                .documentCategory(ClientDocument.DocumentCategory.KYC)
                .documentCategoryDisplayName("Know Your Customer")
                .fileSize(1024L)
                .fileSizeFormatted("1.0 KB")
                .mimeType("application/pdf")
                .uploadDate(LocalDateTime.now())
                .uploadedByClient(true)
                .isActive(true)
                .documentStatus(ClientDocument.DocumentStatus.UPLOADED)
                .documentStatusDisplayName("Uploaded")
                .versionNumber(1)
                .description("Test document description")
                .tags("test,kyc")
                .tagList(Arrays.asList("test", "kyc"))
                .accessCount(0)
                .downloadUrl("/api/client/documents/1/download")
                .canDownload(true)
                .canDelete(true)
                .canReplace(true)
                .securityInfo(ClientDocumentResponse.DocumentSecurityInfo.builder()
                        .isEncrypted(false)
                        .requiresAuthentication(true)
                        .accessLevel("CLIENT")
                        .isPublic(false)
                        .build())
                .build();

        // Create test document list response
        DocumentListResponse.DocumentStatistics statistics = DocumentListResponse.DocumentStatistics.builder()
                .totalDocuments(1L)
                .totalFileSize(1024L)
                .totalFileSizeFormatted("1.0 KB")
                .recentUploads(1)
                .expiringSoon(0)
                .expiredDocuments(0)
                .uploadedByClient(1)
                .averageAccessCount(0)
                .build();

        DocumentListResponse.PaginationInfo pagination = DocumentListResponse.PaginationInfo.builder()
                .currentPage(0)
                .totalPages(1)
                .totalElements(1L)
                .pageSize(20)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        testDocumentListResponse = DocumentListResponse.builder()
                .documents(Arrays.asList(testDocumentResponse))
                .statistics(statistics)
                .categoryCount(Map.of("KYC", 1))
                .typeCount(Map.of("KYC_DOCUMENT", 1))
                .statusCount(Map.of("UPLOADED", 1))
                .pagination(pagination)
                .build();

        // Create test file
        testFile = new MockMultipartFile(
                "file", 
                "test-document.pdf", 
                "application/pdf", 
                "test content".getBytes()
        );
    }

    // ================ Sprint 3.3.1 - View Documents Tests ================

    @Test
    @WithMockUser(roles = "CLIENT")
    void getClientDocuments_WithNoFilters_ReturnsDocumentList() throws Exception {
        // Arrange
        Long clientId = 1L;
        when(jwtUtils.getClientIdFromRequest(any(HttpServletRequest.class))).thenReturn(clientId);
        when(documentService.getClientDocuments(eq(clientId), isNull(), isNull(), isNull(), 
                                               isNull(), isNull(), eq(0), eq(20), 
                                               eq("uploadDate"), eq("DESC")))
                .thenReturn(testDocumentListResponse);

        // Act & Assert
        mockMvc.perform(get("/api/client/documents")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.documents").isArray())
                .andExpect(jsonPath("$.documents[0].id").value(1))
                .andExpect(jsonPath("$.documents[0].documentName").value("Test Document.pdf"))
                .andExpect(jsonPath("$.statistics.totalDocuments").value(1))
                .andExpect(jsonPath("$.pagination.totalElements").value(1));

        verify(jwtUtils).getClientIdFromRequest(any(HttpServletRequest.class));
        verify(documentService).getClientDocuments(eq(clientId), isNull(), isNull(), isNull(), 
                                                  isNull(), isNull(), eq(0), eq(20), 
                                                  eq("uploadDate"), eq("DESC"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void getClientDocuments_WithFilters_ReturnsFilteredDocuments() throws Exception {
        // Arrange
        Long clientId = 1L;
        when(jwtUtils.getClientIdFromRequest(any(HttpServletRequest.class))).thenReturn(clientId);
        when(documentService.getClientDocuments(eq(clientId), eq(ClientDocument.DocumentType.KYC_DOCUMENT), 
                                               eq(ClientDocument.DocumentCategory.KYC), 
                                               eq(ClientDocument.DocumentStatus.UPLOADED), 
                                               eq(true), eq("test"), eq(1), eq(10), 
                                               eq("documentName"), eq("ASC")))
                .thenReturn(testDocumentListResponse);

        // Act & Assert
        mockMvc.perform(get("/api/client/documents")
                        .param("documentType", "KYC_DOCUMENT")
                        .param("documentCategory", "KYC")
                        .param("documentStatus", "UPLOADED")
                        .param("uploadedByClient", "true")
                        .param("searchTerm", "test")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sortBy", "documentName")
                        .param("sortDirection", "ASC")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documents").isArray());

        verify(documentService).getClientDocuments(eq(clientId), eq(ClientDocument.DocumentType.KYC_DOCUMENT), 
                                                  eq(ClientDocument.DocumentCategory.KYC), 
                                                  eq(ClientDocument.DocumentStatus.UPLOADED), 
                                                  eq(true), eq("test"), eq(1), eq(10), 
                                                  eq("documentName"), eq("ASC"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void getDocumentById_WithValidId_ReturnsDocument() throws Exception {
        // Arrange
        Long clientId = 1L;
        Long documentId = 1L;
        when(jwtUtils.getClientIdFromRequest(any(HttpServletRequest.class))).thenReturn(clientId);
        when(documentService.getDocumentById(clientId, documentId)).thenReturn(testDocumentResponse);

        // Act & Assert
        mockMvc.perform(get("/api/client/documents/{documentId}", documentId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.documentName").value("Test Document.pdf"))
                .andExpect(jsonPath("$.documentType").value("KYC_DOCUMENT"));

        verify(jwtUtils).getClientIdFromRequest(any(HttpServletRequest.class));
        verify(documentService).getDocumentById(clientId, documentId);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void getDocumentById_WithInvalidId_ReturnsNotFound() throws Exception {
        // Arrange
        Long clientId = 1L;
        Long documentId = 999L;
        when(jwtUtils.getClientIdFromRequest(any(HttpServletRequest.class))).thenReturn(clientId);
        when(documentService.getDocumentById(clientId, documentId))
                .thenThrow(new ResourceNotFoundException("Document not found"));

        // Act & Assert
        mockMvc.perform(get("/api/client/documents/{documentId}", documentId)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(documentService).getDocumentById(clientId, documentId);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void downloadDocument_WithValidId_ReturnsFile() throws Exception {
        // Arrange
        Long clientId = 1L;
        Long documentId = 1L;
        byte[] fileContent = "test file content".getBytes();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "test-document.pdf");
        headers.setContentLength(fileContent.length);
        
        ResponseEntity<byte[]> responseEntity = ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
        
        when(jwtUtils.getClientIdFromRequest(any(HttpServletRequest.class))).thenReturn(clientId);
        when(documentService.downloadDocument(clientId, documentId)).thenReturn(responseEntity);

        // Act & Assert
        mockMvc.perform(get("/api/client/documents/{documentId}/download", documentId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(fileContent))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"test-document.pdf\""));

        verify(documentService).downloadDocument(clientId, documentId);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void getDocumentCategories_ReturnsCategories() throws Exception {
        // Arrange
        Long clientId = 1L;
        Map<String, Object> categories = new HashMap<>();
        categories.put("documentTypes", Arrays.asList(
                Map.of("value", "KYC_DOCUMENT", "displayName", "KYC Document", "documentCount", 1)
        ));
        categories.put("documentCategories", Arrays.asList(
                Map.of("value", "KYC", "displayName", "Know Your Customer", "documentCount", 1)
        ));
        categories.put("availableStatuses", Arrays.asList("UPLOADED", "UNDER_REVIEW", "APPROVED"));
        
        when(jwtUtils.getClientIdFromRequest(any(HttpServletRequest.class))).thenReturn(clientId);
        when(documentService.getDocumentCategories(clientId)).thenReturn(categories);

        // Act & Assert
        mockMvc.perform(get("/api/client/documents/categories")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.documentTypes").isArray())
                .andExpect(jsonPath("$.documentCategories").isArray())
                .andExpect(jsonPath("$.availableStatuses").isArray());

        verify(documentService).getDocumentCategories(clientId);
    }

    // ================ Sprint 3.3.2 - Upload Files Tests ================

    @Test
    @WithMockUser(roles = "CLIENT")
    void uploadDocument_WithValidFile_ReturnsUploadedDocument() throws Exception {
        // Arrange
        Long clientId = 1L;
        when(jwtUtils.getClientIdFromRequest(any(HttpServletRequest.class))).thenReturn(clientId);
        when(documentService.uploadDocument(eq(clientId), any())).thenReturn(testDocumentResponse);

        MockMultipartFile documentNamePart = new MockMultipartFile("documentName", "", "text/plain", "Test Upload Document".getBytes());
        MockMultipartFile documentTypePart = new MockMultipartFile("documentType", "", "text/plain", "BANK_STATEMENT".getBytes());
        MockMultipartFile documentCategoryPart = new MockMultipartFile("documentCategory", "", "text/plain", "FINANCIAL".getBytes());
        MockMultipartFile descriptionPart = new MockMultipartFile("description", "", "text/plain", "Test upload description".getBytes());
        MockMultipartFile tagsPart = new MockMultipartFile("tags", "", "text/plain", "test,upload".getBytes());

        // Act & Assert
        mockMvc.perform(multipart("/api/client/documents/upload")
                        .file(testFile)
                        .file(documentNamePart)
                        .file(documentTypePart)
                        .file(documentCategoryPart)
                        .file(descriptionPart)
                        .file(tagsPart)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.documentName").value("Test Document.pdf"));

        verify(jwtUtils).getClientIdFromRequest(any(HttpServletRequest.class));
        verify(documentService).uploadDocument(eq(clientId), any());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void uploadDocument_WithMissingFile_ReturnsBadRequest() throws Exception {
        MockMultipartFile documentNamePart = new MockMultipartFile("documentName", "", "text/plain", "Test Document".getBytes());
        MockMultipartFile documentTypePart = new MockMultipartFile("documentType", "", "text/plain", "KYC_DOCUMENT".getBytes());
        MockMultipartFile documentCategoryPart = new MockMultipartFile("documentCategory", "", "text/plain", "KYC".getBytes());

        // Act & Assert - Missing file part should cause bad request
        mockMvc.perform(multipart("/api/client/documents/upload")
                        .file(documentNamePart)
                        .file(documentTypePart)
                        .file(documentCategoryPart)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(documentService, never()).uploadDocument(any(), any());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void uploadDocument_WithInvalidDocumentType_ReturnsBadRequest() throws Exception {
        MockMultipartFile documentNamePart = new MockMultipartFile("documentName", "", "text/plain", "Test Document".getBytes());
        MockMultipartFile documentTypePart = new MockMultipartFile("documentType", "", "text/plain", "INVALID_TYPE".getBytes());
        MockMultipartFile documentCategoryPart = new MockMultipartFile("documentCategory", "", "text/plain", "KYC".getBytes());

        // Act & Assert
        mockMvc.perform(multipart("/api/client/documents/upload")
                        .file(testFile)
                        .file(documentNamePart)
                        .file(documentTypePart)
                        .file(documentCategoryPart)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(documentService, never()).uploadDocument(any(), any());
    }

    // ================ Sprint 3.3.3 - Manage Files Tests ================

    @Test
    @WithMockUser(roles = "CLIENT")
    void replaceDocument_WithValidFile_ReturnsReplacedDocument() throws Exception {
        // Arrange
        Long clientId = 1L;
        Long documentId = 1L;
        when(jwtUtils.getClientIdFromRequest(any(HttpServletRequest.class))).thenReturn(clientId);
        when(documentService.replaceDocument(eq(clientId), eq(documentId), any()))
                .thenReturn(testDocumentResponse);

        MockMultipartFile documentNamePart = new MockMultipartFile("documentName", "", "text/plain", "Replaced Document".getBytes());
        MockMultipartFile documentTypePart = new MockMultipartFile("documentType", "", "text/plain", "BANK_STATEMENT".getBytes());
        MockMultipartFile documentCategoryPart = new MockMultipartFile("documentCategory", "", "text/plain", "FINANCIAL".getBytes());
        MockMultipartFile descriptionPart = new MockMultipartFile("description", "", "text/plain", "Replaced document description".getBytes());

        // Act & Assert
        mockMvc.perform(multipart("/api/client/documents/{documentId}", documentId)
                        .file(testFile)
                        .file(documentNamePart)
                        .file(documentTypePart)
                        .file(documentCategoryPart)
                        .file(descriptionPart)
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));

        verify(documentService).replaceDocument(eq(clientId), eq(documentId), any());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deleteDocument_WithValidId_ReturnsSuccessResponse() throws Exception {
        // Arrange
        Long clientId = 1L;
        Long documentId = 1L;
        Map<String, Object> deleteResponse = Map.of(
                "documentId", documentId,
                "documentName", "Test Document.pdf",
                "success", true,
                "message", "Document deleted successfully",
                "deletedAt", LocalDateTime.now().toString()
        );
        
        when(jwtUtils.getClientIdFromRequest(any(HttpServletRequest.class))).thenReturn(clientId);
        when(documentService.deleteDocument(clientId, documentId)).thenReturn(deleteResponse);

        // Act & Assert
        mockMvc.perform(delete("/api/client/documents/{documentId}", documentId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.documentId").value(1))
                .andExpect(jsonPath("$.message").value("Document deleted successfully"));

        verify(documentService).deleteDocument(clientId, documentId);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deleteDocument_WithSystemDocument_ReturnsForbidden() throws Exception {
        // Arrange
        Long clientId = 1L;
        Long documentId = 1L;
        when(jwtUtils.getClientIdFromRequest(any(HttpServletRequest.class))).thenReturn(clientId);
        when(documentService.deleteDocument(clientId, documentId))
                .thenThrow(new IllegalStateException("System documents cannot be deleted"));

        // Act & Assert
        mockMvc.perform(delete("/api/client/documents/{documentId}", documentId)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(documentService).deleteDocument(clientId, documentId);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void getDocumentHistory_WithDefaultDays_ReturnsHistory() throws Exception {
        // Arrange
        Long clientId = 1L;
        List<ClientDocumentResponse> history = Arrays.asList(testDocumentResponse);
        
        when(jwtUtils.getClientIdFromRequest(any(HttpServletRequest.class))).thenReturn(clientId);
        when(documentService.getDocumentHistory(clientId, 30)).thenReturn(history);

        // Act & Assert
        mockMvc.perform(get("/api/client/documents/history")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].documentName").value("Test Document.pdf"));

        verify(documentService).getDocumentHistory(clientId, 30);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void getDocumentHistory_WithCustomDays_ReturnsHistory() throws Exception {
        // Arrange
        Long clientId = 1L;
        Integer days = 7;
        List<ClientDocumentResponse> history = Arrays.asList(testDocumentResponse);
        
        when(jwtUtils.getClientIdFromRequest(any(HttpServletRequest.class))).thenReturn(clientId);
        when(documentService.getDocumentHistory(clientId, days)).thenReturn(history);

        // Act & Assert
        mockMvc.perform(get("/api/client/documents/history")
                        .param("days", days.toString())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(documentService).getDocumentHistory(clientId, days);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void searchDocuments_WithSearchTerm_ReturnsSearchResults() throws Exception {
        // Arrange
        Long clientId = 1L;
        String searchTerm = "test";
        
        when(jwtUtils.getClientIdFromRequest(any(HttpServletRequest.class))).thenReturn(clientId);
        when(documentService.getClientDocuments(eq(clientId), isNull(), isNull(), isNull(), 
                                               isNull(), eq(searchTerm), eq(0), eq(20), 
                                               eq("uploadDate"), eq("DESC")))
                .thenReturn(testDocumentListResponse);

        // Act & Assert
        mockMvc.perform(get("/api/client/documents/search")
                        .param("searchTerm", searchTerm)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.documents").isArray());

        verify(documentService).getClientDocuments(eq(clientId), isNull(), isNull(), isNull(), 
                                                  isNull(), eq(searchTerm), eq(0), eq(20), 
                                                  eq("uploadDate"), eq("DESC"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void searchDocuments_WithCustomPagination_ReturnsPagedResults() throws Exception {
        // Arrange
        Long clientId = 1L;
        String searchTerm = "document";
        
        when(jwtUtils.getClientIdFromRequest(any(HttpServletRequest.class))).thenReturn(clientId);
        when(documentService.getClientDocuments(eq(clientId), isNull(), isNull(), isNull(), 
                                               isNull(), eq(searchTerm), eq(1), eq(10), 
                                               eq("uploadDate"), eq("DESC")))
                .thenReturn(testDocumentListResponse);

        // Act & Assert
        mockMvc.perform(get("/api/client/documents/search")
                        .param("searchTerm", searchTerm)
                        .param("page", "1")
                        .param("size", "10")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documents").isArray());

        verify(documentService).getClientDocuments(eq(clientId), isNull(), isNull(), isNull(), 
                                                  isNull(), eq(searchTerm), eq(1), eq(10), 
                                                  eq("uploadDate"), eq("DESC"));
    }

    // ================ Security Tests ================

    @Test
    void getClientDocuments_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/client/documents"))
                .andExpect(status().isUnauthorized());

        verify(documentService, never()).getClientDocuments(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "USER") // Wrong role (not CLIENT or ADMIN)
    void getClientDocuments_WithWrongRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/client/documents")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(documentService, never()).getClientDocuments(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void uploadDocument_WithoutCsrf_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/api/client/documents/upload")
                        .file(testFile)
                        .param("documentName", "Test Document")
                        .param("documentType", "KYC_DOCUMENT")
                        .param("documentCategory", "KYC"))
                .andExpect(status().isForbidden());

        verify(documentService, never()).uploadDocument(any(), any());
    }
} 