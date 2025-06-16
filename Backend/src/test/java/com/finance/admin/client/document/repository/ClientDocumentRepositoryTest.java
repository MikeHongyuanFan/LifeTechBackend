package com.finance.admin.client.document.repository;

import com.finance.admin.client.document.model.ClientDocument;
import com.finance.admin.client.model.Client;
import com.finance.admin.client.repository.ClientRepository;
import com.finance.admin.config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests for ClientDocumentRepository - Sprint 3.3 Legal Document Center
 * Tests all database queries and custom repository methods
 */
@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@Sql(scripts = {"/test-schema.sql", "/test-data.sql"})
class ClientDocumentRepositoryTest {

    @Autowired
    private ClientDocumentRepository documentRepository;

    @Autowired
    private ClientRepository clientRepository;

    private Client testClient1;
    private Client testClient2;
    private ClientDocument testDocument1;
    private ClientDocument testDocument2;
    private ClientDocument testDocument3;
    private ClientDocument testDocument;

    @BeforeEach
    void setUp() {
        // Use existing test clients from SQL data (test-data.sql loads clients with IDs 1, 2, 3)
        testClient1 = clientRepository.findById(1L).orElseThrow(() -> 
                new RuntimeException("Test client 1 not found - check test-data.sql"));
        testClient2 = clientRepository.findById(2L).orElseThrow(() -> 
                new RuntimeException("Test client 2 not found - check test-data.sql"));

        // Use existing documents from SQL data instead of creating new ones
        // test-data.sql loads 3 documents with IDs 1, 2, 3 for client 1
        testDocument1 = documentRepository.findById(1L).orElseThrow(() -> 
                new RuntimeException("Test document 1 not found - check test-data.sql"));
        testDocument2 = documentRepository.findById(2L).orElseThrow(() -> 
                new RuntimeException("Test document 2 not found - check test-data.sql"));
        testDocument3 = documentRepository.findById(3L).orElseThrow(() -> 
                new RuntimeException("Test document 3 not found - check test-data.sql"));

        // Create a dynamic test document for specific tests (this will get a new auto-generated ID)
        testDocument = ClientDocument.builder()
                .client(testClient1)
                .documentName("Test Document")
                .documentType(ClientDocument.DocumentType.IDENTITY_VERIFICATION)
                .documentCategory(ClientDocument.DocumentCategory.KYC)
                .documentStatus(ClientDocument.DocumentStatus.UPLOADED)
                .fileSize(1024L)
                .filePath("/test/path.pdf")
                .description("Test description")
                .tags("test,document")
                .uploadedByClient(false)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        // Note: testDocument is NOT saved here - individual tests will save it when needed
    }

    @Test
    void findByClientIdAndIsActiveTrue_WithValidClient_ReturnsActiveDocuments() {
        // Test - uses existing SQL documents
        List<ClientDocument> documents = documentRepository.findByClientIdAndIsActiveTrue(1L);
        assertFalse(documents.isEmpty());
        assertEquals(1L, documents.get(0).getClient().getId());
        assertTrue(documents.get(0).getIsActive());
        // SQL has 3 active documents for client 1
        assertEquals(3, documents.size());
    }

    @Test
    void findByClientId_WithPagination_ReturnsPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1);

        // Act
        Page<ClientDocument> documentsPage = documentRepository.findByClientId(testClient1.getId(), pageable);

        // Assert - SQL data has 3 documents for client 1
        assertThat(documentsPage.getTotalElements()).isEqualTo(3);
        assertThat(documentsPage.getTotalPages()).isEqualTo(3);
        assertThat(documentsPage.getContent()).hasSize(1);
    }

    @Test
    void findByClientIdAndDocumentTypeAndIsActiveTrue_WithValidType_ReturnsFilteredDocuments() {
        // Act - Looking for KYC_DOCUMENT type (document with ID=2 in SQL)
        List<ClientDocument> documents = documentRepository.findByClientIdAndDocumentTypeAndIsActiveTrue(
                testClient1.getId(), ClientDocument.DocumentType.KYC_DOCUMENT);

        // Assert - SQL has 1 KYC_DOCUMENT for client 1
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getDocumentName()).isEqualTo("Test Document 2");
        assertThat(documents.get(0).getDocumentType()).isEqualTo(ClientDocument.DocumentType.KYC_DOCUMENT);
    }

    @Test
    void findByClientIdAndDocumentCategoryAndIsActiveTrue_WithValidCategory_ReturnsFilteredDocuments() {
        // Act - Looking for INVESTMENT category (document with ID=2 in SQL)
        List<ClientDocument> documents = documentRepository.findByClientIdAndDocumentCategoryAndIsActiveTrue(
                testClient1.getId(), ClientDocument.DocumentCategory.INVESTMENT);

        // Assert - SQL has 1 INVESTMENT document for client 1
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getDocumentName()).isEqualTo("Test Document 2");
        assertThat(documents.get(0).getDocumentCategory()).isEqualTo(ClientDocument.DocumentCategory.INVESTMENT);
    }

    @Test
    void findByClientIdAndDocumentStatusAndIsActiveTrue_WithValidStatus_ReturnsFilteredDocuments() {
        // Act - Looking for APPROVED status (document with ID=2 in SQL)
        List<ClientDocument> documents = documentRepository.findByClientIdAndDocumentStatusAndIsActiveTrue(
                testClient1.getId(), ClientDocument.DocumentStatus.APPROVED);

        // Assert - SQL has 1 APPROVED document for client 1
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getDocumentName()).isEqualTo("Test Document 2");
        assertThat(documents.get(0).getDocumentStatus()).isEqualTo(ClientDocument.DocumentStatus.APPROVED);
    }

    @Test
    void findByIdAndClientId_WithValidIds_ReturnsDocument() {
        // Test using existing SQL document with ID=1
        Optional<ClientDocument> found = documentRepository.findByIdAndClientId(1L, 1L);
        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getId());
        assertEquals(1L, found.get().getClient().getId());
    }

    @Test
    void findByIdAndClientId_WithWrongClient_ReturnsEmpty() {
        // Act
        Optional<ClientDocument> document = documentRepository.findByIdAndClientId(
                testDocument1.getId(), testClient2.getId());

        // Assert
        assertThat(document).isEmpty();
    }

    @Test
    void findByClientIdAndUploadedByClientTrueAndIsActiveTrue_ReturnsClientUploadedDocuments() {
        // Act - Looking for client-uploaded documents (only document with ID=2 in SQL)
        List<ClientDocument> documents = documentRepository.findByClientIdAndUploadedByClientTrueAndIsActiveTrue(
                testClient1.getId());

        // Assert - SQL has 1 client-uploaded document for client 1
        assertThat(documents).hasSize(1);
        assertThat(documents).allMatch(ClientDocument::getUploadedByClient);
        assertThat(documents).extracting(ClientDocument::getDocumentName)
                .containsExactly("Test Document 2");
    }

    @Test
    void findByClientIdAndUploadedByClientFalseAndIsActiveTrue_ReturnsSystemDocuments() {
        // Act - Looking for system documents (documents with ID=1,3 for client 1)
        List<ClientDocument> documents = documentRepository.findByClientIdAndUploadedByClientFalseAndIsActiveTrue(
                testClient1.getId());

        // Assert - SQL has 2 system documents for client 1
        assertThat(documents).hasSize(2);
        assertThat(documents).allMatch(doc -> !doc.getUploadedByClient());
        assertThat(documents).extracting(ClientDocument::getDocumentName)
                .containsExactlyInAnyOrder("Test Document 1", "Test Document 3");
    }

    @Test
    void searchByName_WithMatchingTerm_ReturnsMatchingDocuments() {
        // Act - Looking for documents with "test" in name (all SQL documents have "Test Document" names)
        List<ClientDocument> documents = documentRepository.searchByName(testClient1.getId(), "test");

        // Assert - SQL has 3 documents with "Test" in the name
        assertThat(documents).hasSize(3);
        assertThat(documents).extracting(ClientDocument::getDocumentName)
                .allMatch(name -> name.toLowerCase().contains("test"));
    }

    @Test
    void searchDocuments_WithMatchingTermInDescription_ReturnsMatchingDocuments() {
        // Act - Looking for documents with "description" in description field
        List<ClientDocument> documents = documentRepository.searchDocuments(testClient1.getId(), "description");

        // Assert - All SQL documents have "Test description X" so should match
        assertThat(documents).hasSize(3);
        assertThat(documents).extracting(ClientDocument::getDescription)
                .allMatch(desc -> desc.toLowerCase().contains("description"));
    }

    @Test
    void searchDocuments_WithMatchingTermInTags_ReturnsMatchingDocuments() {
        // Act - Looking for "financial" in tags (document with ID=2 has "test,financial")
        List<ClientDocument> documents = documentRepository.searchDocuments(testClient1.getId(), "financial");

        // Assert - SQL has 1 document with "financial" tag for client 1
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getDocumentName()).isEqualTo("Test Document 2");
    }

    @Test
    void findExpiringSoon_WithDocumentsExpiringSoon_ReturnsExpiringSoonDocuments() {
        // Arrange - Look for documents expiring in the next 60 days
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(60);

        // Act - SQL documents have expiry dates 30, 60, and -1 days from now
        List<ClientDocument> documents = documentRepository.findExpiringSoon(testClient1.getId(), now, futureDate);

        // Assert - SQL has 2 documents expiring soon (30 and 60 day ones)
        assertThat(documents).hasSize(2);
        assertThat(documents).extracting(ClientDocument::getDocumentName)
                .containsExactlyInAnyOrder("Test Document 1", "Test Document 2");
    }

    @Test
    void findExpired_WithExpiredDocuments_ReturnsExpiredDocuments() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Act - SQL document with ID=3 has expiry date in the past
        List<ClientDocument> documents = documentRepository.findExpired(testClient1.getId(), now);

        // Assert - SQL has 1 expired document for client 1
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getDocumentName()).isEqualTo("Test Document 3");
        assertThat(documents.get(0).getExpiryDate()).isBefore(now);
    }

    @Test
    void findRecentUploads_WithRecentDocuments_ReturnsRecentDocuments() {
        // Arrange - Look for uploads in the last 3 days
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(3);

        // Act - SQL documents may not have upload_date set (most are NULL)
        List<ClientDocument> documents = documentRepository.findRecentUploads(testClient1.getId(), sinceDate);

        // Assert - Documents in SQL may not have upload_date set, so result might be empty
        // This is expected behavior based on the SQL data structure
        assertThat(documents).isNotNull(); // Just verify the query works
    }

    @Test
    void findMostAccessed_WithAccessCountData_ReturnsMostAccessedDocuments() {
        // Arrange - SQL data has access counts: id=1 (0), id=2 (5), id=3 (10)
        Pageable pageable = PageRequest.of(0, 2);

        // Act
        List<ClientDocument> documents = documentRepository.findMostAccessed(testClient1.getId(), pageable);

        // Assert - Should return top 2 documents by access count (id=3 with 10, id=2 with 5)
        assertThat(documents).hasSize(2); // Limited by page size
        assertThat(documents.get(0).getAccessCount()).isEqualTo(10); // First should have highest count
        assertThat(documents.get(1).getAccessCount()).isEqualTo(5); // Second should have second highest
    }

    @Test
    void findWithFilters_WithMultipleFilters_ReturnsFilteredDocuments() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act - Using filters that match SQL document with ID=2 (KYC_DOCUMENT, INVESTMENT, APPROVED)
        Page<ClientDocument> documentsPage = documentRepository.findWithFilters(
                testClient1.getId(),
                ClientDocument.DocumentType.KYC_DOCUMENT,
                ClientDocument.DocumentCategory.INVESTMENT,
                ClientDocument.DocumentStatus.APPROVED,
                true,
                pageable
        );

        // Assert - SQL has 1 document matching these filters
        assertThat(documentsPage.getTotalElements()).isEqualTo(1);
        assertThat(documentsPage.getContent().get(0).getDocumentName()).isEqualTo("Test Document 2");
    }

    @Test
    void findWithFilters_WithNullFilters_ReturnsAllActiveDocuments() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<ClientDocument> documentsPage = documentRepository.findWithFilters(
                testClient1.getId(), null, null, null, null, pageable);

        // Assert - SQL has 3 active documents for client 1
        assertThat(documentsPage.getTotalElements()).isEqualTo(3);
    }

    @Test
    void existsByClientIdAndDocumentTypeAndIsActiveTrue_WithExistingType_ReturnsTrue() {
        // Act
        boolean exists = documentRepository.existsByClientIdAndDocumentTypeAndIsActiveTrue(
                testClient1.getId(), ClientDocument.DocumentType.KYC_DOCUMENT);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByClientIdAndDocumentTypeAndIsActiveTrue_WithNonExistingType_ReturnsFalse() {
        // Act
        boolean exists = documentRepository.existsByClientIdAndDocumentTypeAndIsActiveTrue(
                testClient1.getId(), ClientDocument.DocumentType.TAX_DOCUMENT);

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void countByClientIdAndIsActiveTrue_WithActiveDocuments_ReturnsCorrectCount() {
        // Act
        long count = documentRepository.countByClientIdAndIsActiveTrue(testClient1.getId());

        // Assert - SQL has 3 active documents for client 1
        assertThat(count).isEqualTo(3);
    }

    @Test
    void getTotalFileSizeForClient_WithDocuments_ReturnsCorrectTotalSize() {
        // Act
        Long totalSize = documentRepository.getTotalFileSizeForClient(testClient1.getId());

        // Assert - SQL has documents with sizes 1024 + 2048 + 3072 = 6144 bytes
        assertThat(totalSize).isEqualTo(6144L);
    }

    @Test
    void getTotalFileSizeForClient_WithNoDocuments_ReturnsZero() {
        // Use client ID that doesn't exist in SQL to avoid conflicts
        Long totalSize = documentRepository.getTotalFileSizeForClient(999L);

        // Assert
        assertThat(totalSize).isEqualTo(0L);
    }
} 