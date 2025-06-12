package com.finance.admin.client.document.service;

import com.finance.admin.client.document.dto.ClientDocumentResponse;
import com.finance.admin.client.document.dto.DocumentListResponse;
import com.finance.admin.client.document.dto.DocumentUploadRequest;
import com.finance.admin.client.document.model.ClientDocument;
import com.finance.admin.client.document.repository.ClientDocumentRepository;
import com.finance.admin.client.model.Client;
import com.finance.admin.client.repository.ClientRepository;
import com.finance.admin.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClientDocumentService - Sprint 3.3 Legal Document Center
 * Tests all three main features: View Documents, Upload Files, Manage Files
 */
@ExtendWith(MockitoExtension.class)
class ClientDocumentServiceTest {

    @Mock
    private ClientDocumentRepository documentRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientDocumentService documentService;

    private Client testClient;
    private ClientDocument testDocument;
    private MockMultipartFile testFile;
    private DocumentUploadRequest testUploadRequest;

    @BeforeEach
    void setUp() {
        // Set up test configuration
        ReflectionTestUtils.setField(documentService, "uploadPath", "/tmp/test-documents");
        ReflectionTestUtils.setField(documentService, "maxFileSize", 52428800L); // 50MB

        // Create test client
        testClient = Client.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .emailPrimary("john.doe@example.com")
                .build();

        // Create test document
        testDocument = ClientDocument.builder()
                .id(1L)
                .client(testClient)
                .documentName("Test Document.pdf")
                .documentType(ClientDocument.DocumentType.KYC_DOCUMENT)
                .documentCategory(ClientDocument.DocumentCategory.KYC)
                .filePath("/tmp/test-documents/client_1/test-file.pdf")
                .fileSize(1024L)
                .mimeType("application/pdf")
                .uploadDate(LocalDateTime.now())
                .uploadedByClient(true)
                .isActive(true)
                .documentStatus(ClientDocument.DocumentStatus.UPLOADED)
                .versionNumber(1)
                .accessCount(0)
                .description("Test document description")
                .tags("test,kyc")
                .build();

        // Create test file
        testFile = new MockMultipartFile(
                "file", 
                "test-document.pdf", 
                "application/pdf", 
                "test content".getBytes()
        );

        // Create test upload request
        testUploadRequest = DocumentUploadRequest.builder()
                .file(testFile)
                .documentName("Test Upload Document")
                .documentType(ClientDocument.DocumentType.BANK_STATEMENT)
                .documentCategory(ClientDocument.DocumentCategory.FINANCIAL)
                .description("Test upload description")
                .tags("test,upload")
                .build();
    }

    // ================ Sprint 3.3.1 - View Documents Tests ================

    @Test
    void getClientDocuments_WithValidClient_ReturnsDocumentList() {
        // Arrange
        Long clientId = 1L;
        List<ClientDocument> documents = Arrays.asList(testDocument);
        Page<ClientDocument> documentPage = new PageImpl<>(documents);
        
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(documentRepository.findWithFilters(eq(clientId), isNull(), isNull(), 
                                               isNull(), isNull(), any(Pageable.class)))
                .thenReturn(documentPage);
        when(documentRepository.countByClientIdAndIsActiveTrue(clientId)).thenReturn(1L);
        when(documentRepository.getTotalFileSizeForClient(clientId)).thenReturn(1024L);
        when(documentRepository.findRecentUploads(eq(clientId), any(LocalDateTime.class)))
                .thenReturn(documents);
        when(documentRepository.findExpiringSoon(eq(clientId), any(LocalDateTime.class), 
                                                any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(documentRepository.findExpired(eq(clientId), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(documentRepository.findByClientIdAndUploadedByClientTrueAndIsActiveTrue(clientId))
                .thenReturn(documents);
        when(documentRepository.findByClientIdAndIsActiveTrue(clientId)).thenReturn(documents);

        // Act
        DocumentListResponse response = documentService.getClientDocuments(
                clientId, null, null, null, null, null, 0, 20, "uploadDate", "DESC");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getDocuments()).hasSize(1);
        assertThat(response.getDocuments().get(0).getDocumentName()).isEqualTo("Test Document.pdf");
        assertThat(response.getStatistics()).isNotNull();
        assertThat(response.getStatistics().getTotalDocuments()).isEqualTo(1L);
        assertThat(response.getPagination()).isNotNull();
        assertThat(response.getPagination().getTotalElements()).isEqualTo(1L);
        
        verify(clientRepository).existsById(clientId);
        verify(documentRepository).findWithFilters(eq(clientId), isNull(), isNull(), 
                                                  isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void getClientDocuments_WithFilters_ReturnsFilteredDocuments() {
        // Arrange
        Long clientId = 1L;
        List<ClientDocument> documents = Arrays.asList(testDocument);
        Page<ClientDocument> documentPage = new PageImpl<>(documents);
        
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(documentRepository.findWithFilters(eq(clientId), eq(ClientDocument.DocumentType.KYC_DOCUMENT), 
                                               eq(ClientDocument.DocumentCategory.KYC), 
                                               eq(ClientDocument.DocumentStatus.UPLOADED), 
                                               eq(true), any(Pageable.class)))
                .thenReturn(documentPage);
        when(documentRepository.countByClientIdAndIsActiveTrue(clientId)).thenReturn(1L);
        when(documentRepository.getTotalFileSizeForClient(clientId)).thenReturn(1024L);
        when(documentRepository.findRecentUploads(eq(clientId), any(LocalDateTime.class)))
                .thenReturn(documents);
        when(documentRepository.findExpiringSoon(eq(clientId), any(LocalDateTime.class), 
                                                any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(documentRepository.findExpired(eq(clientId), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(documentRepository.findByClientIdAndUploadedByClientTrueAndIsActiveTrue(clientId))
                .thenReturn(documents);
        when(documentRepository.findByClientIdAndIsActiveTrue(clientId)).thenReturn(documents);

        // Act
        DocumentListResponse response = documentService.getClientDocuments(
                clientId, ClientDocument.DocumentType.KYC_DOCUMENT, 
                ClientDocument.DocumentCategory.KYC, ClientDocument.DocumentStatus.UPLOADED, 
                true, null, 0, 10, "documentName", "ASC");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getDocuments()).hasSize(1);
        
        verify(documentRepository).findWithFilters(eq(clientId), eq(ClientDocument.DocumentType.KYC_DOCUMENT), 
                                                  eq(ClientDocument.DocumentCategory.KYC), 
                                                  eq(ClientDocument.DocumentStatus.UPLOADED), 
                                                  eq(true), any(Pageable.class));
    }

    @Test
    void getClientDocuments_WithSearchTerm_ReturnsSearchResults() {
        // Arrange
        Long clientId = 1L;
        String searchTerm = "test";
        List<ClientDocument> documents = Arrays.asList(testDocument);
        
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(documentRepository.searchDocuments(clientId, searchTerm)).thenReturn(documents);
        when(documentRepository.countByClientIdAndIsActiveTrue(clientId)).thenReturn(1L);
        when(documentRepository.getTotalFileSizeForClient(clientId)).thenReturn(1024L);
        when(documentRepository.findRecentUploads(eq(clientId), any(LocalDateTime.class)))
                .thenReturn(documents);
        when(documentRepository.findExpiringSoon(eq(clientId), any(LocalDateTime.class), 
                                                any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(documentRepository.findExpired(eq(clientId), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(documentRepository.findByClientIdAndUploadedByClientTrueAndIsActiveTrue(clientId))
                .thenReturn(documents);
        when(documentRepository.findByClientIdAndIsActiveTrue(clientId)).thenReturn(documents);

        // Act
        DocumentListResponse response = documentService.getClientDocuments(
                clientId, null, null, null, null, searchTerm, 0, 20, "uploadDate", "DESC");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getDocuments()).hasSize(1);
        
        verify(documentRepository).searchDocuments(clientId, searchTerm);
        verify(documentRepository, never()).findWithFilters(any(), any(), any(), any(), any(), any());
    }

    @Test
    void getClientDocuments_WithInvalidClient_ThrowsResourceNotFoundException() {
        // Arrange
        Long clientId = 999L;
        when(clientRepository.existsById(clientId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> documentService.getClientDocuments(
                clientId, null, null, null, null, null, 0, 20, "uploadDate", "DESC"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client not found");
        
        verify(clientRepository).existsById(clientId);
        verify(documentRepository, never()).findWithFilters(any(), any(), any(), any(), any(), any());
    }

    @Test
    void getDocumentById_WithValidDocumentAndClient_ReturnsDocument() {
        // Arrange
        Long clientId = 1L;
        Long documentId = 1L;
        
        when(documentRepository.findByIdAndClientId(documentId, clientId))
                .thenReturn(Optional.of(testDocument));
        when(documentRepository.save(testDocument)).thenReturn(testDocument);

        // Act
        ClientDocumentResponse response = documentService.getDocumentById(clientId, documentId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(documentId);
        assertThat(response.getDocumentName()).isEqualTo("Test Document.pdf");
        assertThat(response.getAccessCount()).isEqualTo(1); // Should be incremented
        
        verify(documentRepository).findByIdAndClientId(documentId, clientId);
        verify(documentRepository).save(testDocument);
    }

    @Test
    void getDocumentById_WithInvalidDocument_ThrowsResourceNotFoundException() {
        // Arrange
        Long clientId = 1L;
        Long documentId = 999L;
        
        when(documentRepository.findByIdAndClientId(documentId, clientId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> documentService.getDocumentById(clientId, documentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Document not found");
        
        verify(documentRepository).findByIdAndClientId(documentId, clientId);
        verify(documentRepository, never()).save(any());
    }

    @Test
    void downloadDocument_WithValidDocument_ReturnsFileContent() throws IOException {
        // Arrange
        Long clientId = 1L;
        Long documentId = 1L;
        
        // Create test file
        Path testFilePath = Paths.get(System.getProperty("java.io.tmpdir"), "test-download.pdf");
        Files.write(testFilePath, "test file content".getBytes());
        testDocument.setFilePath(testFilePath.toString());
        
        when(documentRepository.findByIdAndClientId(documentId, clientId))
                .thenReturn(Optional.of(testDocument));
        when(documentRepository.save(testDocument)).thenReturn(testDocument);

        try {
            // Act
            ResponseEntity<byte[]> response = documentService.downloadDocument(clientId, documentId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isNotNull();
            assertThat(new String(response.getBody())).isEqualTo("test file content");
            assertThat(response.getHeaders().getContentType().toString()).contains("application/pdf");
            
            verify(documentRepository).findByIdAndClientId(documentId, clientId);
            verify(documentRepository).save(testDocument);
        } finally {
            // Clean up test file
            Files.deleteIfExists(testFilePath);
        }
    }

    @Test
    void downloadDocument_WithInvalidDocument_ThrowsResourceNotFoundException() {
        // Arrange
        Long clientId = 1L;
        Long documentId = 999L;
        
        when(documentRepository.findByIdAndClientId(documentId, clientId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> documentService.downloadDocument(clientId, documentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Document not found");
        
        verify(documentRepository).findByIdAndClientId(documentId, clientId);
    }

    @Test
    void getDocumentCategories_WithValidClient_ReturnsCategories() {
        // Arrange
        Long clientId = 1L;
        
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(documentRepository.findByClientIdAndDocumentTypeAndIsActiveTrue(eq(clientId), any()))
                .thenReturn(Collections.emptyList());
        when(documentRepository.findByClientIdAndDocumentCategoryAndIsActiveTrue(eq(clientId), any()))
                .thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> categories = documentService.getDocumentCategories(clientId);

        // Assert
        assertThat(categories).isNotNull();
        assertThat(categories).containsKeys("documentTypes", "documentCategories", "availableStatuses");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> documentTypes = (List<Map<String, Object>>) categories.get("documentTypes");
        assertThat(documentTypes).isNotEmpty();
        assertThat(documentTypes.get(0)).containsKeys("value", "displayName", "documentCount");
        
        verify(clientRepository).existsById(clientId);
    }

    // ================ Sprint 3.3.2 - Upload Files Tests ================

    @Test
    void uploadDocument_WithValidRequest_ReturnsUploadedDocument() {
        // Arrange
        Long clientId = 1L;
        
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));
        when(documentRepository.save(any(ClientDocument.class))).thenReturn(testDocument);

        // Act
        ClientDocumentResponse response = documentService.uploadDocument(clientId, testUploadRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getDocumentName()).isEqualTo("Test Document.pdf");
        assertThat(response.getUploadedByClient()).isTrue();
        assertThat(response.getDocumentStatus()).isEqualTo(ClientDocument.DocumentStatus.UPLOADED);
        
        verify(clientRepository).existsById(clientId);
        verify(clientRepository).findById(clientId);
        verify(documentRepository).save(any(ClientDocument.class));
    }

    @Test
    void uploadDocument_WithReplaceExisting_ReplacesOriginalDocument() {
        // Arrange
        Long clientId = 1L;
        Long originalDocumentId = 2L;
        
        ClientDocument originalDocument = ClientDocument.builder()
                .id(originalDocumentId)
                .client(testClient)
                .documentName("Original Document")
                .versionNumber(1)
                .isActive(true)
                .documentStatus(ClientDocument.DocumentStatus.UPLOADED)
                .build();
        
        testUploadRequest.setReplaceExisting(true);
        testUploadRequest.setReplacingDocumentId(originalDocumentId);
        
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));
        when(documentRepository.findByIdAndClientId(originalDocumentId, clientId))
                .thenReturn(Optional.of(originalDocument));
        when(documentRepository.save(any(ClientDocument.class))).thenReturn(testDocument);

        // Act
        ClientDocumentResponse response = documentService.uploadDocument(clientId, testUploadRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(originalDocument.getIsActive()).isFalse();
        assertThat(originalDocument.getDocumentStatus()).isEqualTo(ClientDocument.DocumentStatus.REPLACED);
        
        verify(documentRepository).findByIdAndClientId(originalDocumentId, clientId);
        verify(documentRepository, times(2)).save(any(ClientDocument.class)); // Original + new document
    }

    @Test
    void uploadDocument_WithInvalidClient_ThrowsResourceNotFoundException() {
        // Arrange
        Long clientId = 999L;
        
        when(clientRepository.existsById(clientId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> documentService.uploadDocument(clientId, testUploadRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client not found");
        
        verify(clientRepository).existsById(clientId);
        verify(documentRepository, never()).save(any());
    }

    @Test
    void uploadDocument_WithInvalidFile_ThrowsValidationException() {
        // Arrange
        Long clientId = 1L;
        DocumentUploadRequest invalidRequest = DocumentUploadRequest.builder()
                .file(null) // Invalid - null file
                .documentName("Test Document")
                .documentType(ClientDocument.DocumentType.KYC_DOCUMENT)
                .documentCategory(ClientDocument.DocumentCategory.KYC)
                .build();
        
        when(clientRepository.existsById(clientId)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> documentService.uploadDocument(clientId, invalidRequest))
                .isInstanceOf(IllegalArgumentException.class);
        
        verify(clientRepository).existsById(clientId);
        verify(documentRepository, never()).save(any());
    }

    // ================ Sprint 3.3.3 - Manage Files Tests ================

    @Test
    void replaceDocument_WithValidRequest_ReplacesDocument() {
        // Arrange
        Long clientId = 1L;
        Long documentId = 1L;
        
        when(documentRepository.findByIdAndClientId(documentId, clientId))
                .thenReturn(Optional.of(testDocument));
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));
        when(documentRepository.save(any(ClientDocument.class))).thenReturn(testDocument);

        // Act
        ClientDocumentResponse response = documentService.replaceDocument(clientId, documentId, testUploadRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(testUploadRequest.getReplaceExisting()).isTrue();
        assertThat(testUploadRequest.getReplacingDocumentId()).isEqualTo(documentId);
        
        verify(documentRepository, atLeastOnce()).findByIdAndClientId(documentId, clientId);
    }

    @Test
    void replaceDocument_WithInvalidDocument_ThrowsResourceNotFoundException() {
        // Arrange
        Long clientId = 1L;
        Long documentId = 999L;
        
        when(documentRepository.findByIdAndClientId(documentId, clientId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> documentService.replaceDocument(clientId, documentId, testUploadRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Document not found");
        
        verify(documentRepository).findByIdAndClientId(documentId, clientId);
    }

    @Test
    void deleteDocument_WithClientUploadedDocument_DeletesSuccessfully() {
        // Arrange
        Long clientId = 1L;
        Long documentId = 1L;
        testDocument.setUploadedByClient(true);
        
        when(documentRepository.findByIdAndClientId(documentId, clientId))
                .thenReturn(Optional.of(testDocument));
        when(documentRepository.save(testDocument)).thenReturn(testDocument);

        // Act
        Map<String, Object> response = documentService.deleteDocument(clientId, documentId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.get("success")).isEqualTo(true);
        assertThat(response.get("documentId")).isEqualTo(documentId);
        assertThat(response.get("documentName")).isEqualTo("Test Document.pdf");
        assertThat(response.get("message")).isEqualTo("Document deleted successfully");
        
        assertThat(testDocument.getIsActive()).isFalse();
        assertThat(testDocument.getDocumentStatus()).isEqualTo(ClientDocument.DocumentStatus.ARCHIVED);
        
        verify(documentRepository).findByIdAndClientId(documentId, clientId);
        verify(documentRepository).save(testDocument);
    }

    @Test
    void deleteDocument_WithSystemDocument_ThrowsIllegalStateException() {
        // Arrange
        Long clientId = 1L;
        Long documentId = 1L;
        testDocument.setUploadedByClient(false); // System document
        
        when(documentRepository.findByIdAndClientId(documentId, clientId))
                .thenReturn(Optional.of(testDocument));

        // Act & Assert
        assertThatThrownBy(() -> documentService.deleteDocument(clientId, documentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("System documents cannot be deleted");
        
        verify(documentRepository).findByIdAndClientId(documentId, clientId);
        verify(documentRepository, never()).save(any());
    }

    @Test
    void deleteDocument_WithInvalidDocument_ThrowsResourceNotFoundException() {
        // Arrange
        Long clientId = 1L;
        Long documentId = 999L;
        
        when(documentRepository.findByIdAndClientId(documentId, clientId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> documentService.deleteDocument(clientId, documentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Document not found");
        
        verify(documentRepository).findByIdAndClientId(documentId, clientId);
        verify(documentRepository, never()).save(any());
    }

    @Test
    void getDocumentHistory_WithValidClient_ReturnsHistory() {
        // Arrange
        Long clientId = 1L;
        Integer days = 30;
        List<ClientDocument> recentDocuments = Arrays.asList(testDocument);
        
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(documentRepository.findRecentUploads(eq(clientId), any(LocalDateTime.class)))
                .thenReturn(recentDocuments);

        // Act
        List<ClientDocumentResponse> history = documentService.getDocumentHistory(clientId, days);

        // Assert
        assertThat(history).isNotNull();
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getDocumentName()).isEqualTo("Test Document.pdf");
        
        verify(clientRepository).existsById(clientId);
        verify(documentRepository).findRecentUploads(eq(clientId), any(LocalDateTime.class));
    }

    @Test
    void getDocumentHistory_WithNullDays_UsesDefaultDays() {
        // Arrange
        Long clientId = 1L;
        Integer days = null; // Should default to 30
        List<ClientDocument> recentDocuments = Arrays.asList(testDocument);
        
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(documentRepository.findRecentUploads(eq(clientId), any(LocalDateTime.class)))
                .thenReturn(recentDocuments);

        // Act
        List<ClientDocumentResponse> history = documentService.getDocumentHistory(clientId, days);

        // Assert
        assertThat(history).isNotNull();
        assertThat(history).hasSize(1);
        
        verify(documentRepository).findRecentUploads(eq(clientId), any(LocalDateTime.class));
    }

    // ================ Helper Method Tests ================

    @Test
    void validateClientExists_WithValidClient_DoesNotThrow() {
        // Arrange
        Long clientId = 1L;
        when(clientRepository.existsById(clientId)).thenReturn(true);

        // Act & Assert - Call via public method that uses validateClientExists
        assertThatCode(() -> documentService.getDocumentCategories(clientId))
                .doesNotThrowAnyException();
        
        verify(clientRepository).existsById(clientId);
    }

    @Test
    void validateClientExists_WithInvalidClient_ThrowsResourceNotFoundException() {
        // Arrange
        Long clientId = 999L;
        when(clientRepository.existsById(clientId)).thenReturn(false);

        // Act & Assert - Call via public method that uses validateClientExists
        assertThatThrownBy(() -> documentService.getDocumentCategories(clientId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client not found with ID: 999");
        
        verify(clientRepository).existsById(clientId);
    }
} 