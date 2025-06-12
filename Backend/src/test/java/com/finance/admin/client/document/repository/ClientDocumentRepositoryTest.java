package com.finance.admin.client.document.repository;

import com.finance.admin.client.document.model.ClientDocument;
import com.finance.admin.client.model.Client;
import com.finance.admin.client.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Repository tests for ClientDocumentRepository - Sprint 3.3 Legal Document Center
 * Tests all database queries and custom repository methods
 */
@DataJpaTest
@ActiveProfiles("test")
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

    @BeforeEach
    void setUp() {
        // Create test clients
        testClient1 = Client.builder()
                .firstName("John")
                .lastName("Doe")
                .emailPrimary("john.doe@example.com")
                .phonePrimary("+1234567890")
                .membershipNumber("MEM001")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        testClient2 = Client.builder()
                .firstName("Jane")
                .lastName("Smith")
                .emailPrimary("jane.smith@example.com")
                .phonePrimary("+1234567891")
                .membershipNumber("MEM002")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testClient1 = clientRepository.save(testClient1);
        testClient2 = clientRepository.save(testClient2);

        // Create test documents
        testDocument1 = ClientDocument.builder()
                .client(testClient1)
                .documentName("KYC Document.pdf")
                .documentType(ClientDocument.DocumentType.KYC_DOCUMENT)
                .documentCategory(ClientDocument.DocumentCategory.KYC)
                .filePath("/tmp/test/kyc.pdf")
                .fileSize(1024L)
                .mimeType("application/pdf")
                .uploadDate(LocalDateTime.now().minusDays(5))
                .uploadedByClient(true)
                .isActive(true)
                .documentStatus(ClientDocument.DocumentStatus.UPLOADED)
                .versionNumber(1)
                .accessCount(3)
                .description("KYC verification document")
                .tags("kyc,verification,identity")
                .expiryDate(LocalDateTime.now().plusDays(365))
                .build();

        testDocument2 = ClientDocument.builder()
                .client(testClient1)
                .documentName("Bank Statement.pdf")
                .documentType(ClientDocument.DocumentType.BANK_STATEMENT)
                .documentCategory(ClientDocument.DocumentCategory.FINANCIAL)
                .filePath("/tmp/test/bank.pdf")
                .fileSize(2048L)
                .mimeType("application/pdf")
                .uploadDate(LocalDateTime.now().minusDays(2))
                .uploadedByClient(true)
                .isActive(true)
                .documentStatus(ClientDocument.DocumentStatus.APPROVED)
                .versionNumber(1)
                .accessCount(1)
                .description("Monthly bank statement")
                .tags("bank,statement,financial")
                .expiryDate(LocalDateTime.now().plusDays(30))
                .build();

        testDocument3 = ClientDocument.builder()
                .client(testClient2)
                .documentName("Tax Document.pdf")
                .documentType(ClientDocument.DocumentType.TAX_DOCUMENT)
                .documentCategory(ClientDocument.DocumentCategory.TAX)
                .filePath("/tmp/test/tax.pdf")
                .fileSize(512L)
                .mimeType("application/pdf")
                .uploadDate(LocalDateTime.now().minusDays(1))
                .uploadedByClient(false) // System document
                .isActive(true)
                .documentStatus(ClientDocument.DocumentStatus.UPLOADED)
                .versionNumber(1)
                .accessCount(0)
                .description("Tax filing document")
                .tags("tax,filing")
                .expiryDate(LocalDateTime.now().minusDays(5)) // Expired
                .build();

        testDocument1 = documentRepository.save(testDocument1);
        testDocument2 = documentRepository.save(testDocument2);
        testDocument3 = documentRepository.save(testDocument3);
    }

    @Test
    void findByClientIdAndIsActiveTrue_WithValidClient_ReturnsActiveDocuments() {
        // Act
        List<ClientDocument> documents = documentRepository.findByClientIdAndIsActiveTrue(testClient1.getId());

        // Assert
        assertThat(documents).hasSize(2);
        assertThat(documents).extracting(ClientDocument::getDocumentName)
                .containsExactlyInAnyOrder("KYC Document.pdf", "Bank Statement.pdf");
        assertThat(documents).allMatch(ClientDocument::getIsActive);
    }

    @Test
    void findByClientId_WithPagination_ReturnsPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1);

        // Act
        Page<ClientDocument> documentsPage = documentRepository.findByClientId(testClient1.getId(), pageable);

        // Assert
        assertThat(documentsPage.getTotalElements()).isEqualTo(2);
        assertThat(documentsPage.getTotalPages()).isEqualTo(2);
        assertThat(documentsPage.getContent()).hasSize(1);
    }

    @Test
    void findByClientIdAndDocumentTypeAndIsActiveTrue_WithValidType_ReturnsFilteredDocuments() {
        // Act
        List<ClientDocument> documents = documentRepository.findByClientIdAndDocumentTypeAndIsActiveTrue(
                testClient1.getId(), ClientDocument.DocumentType.KYC_DOCUMENT);

        // Assert
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getDocumentName()).isEqualTo("KYC Document.pdf");
        assertThat(documents.get(0).getDocumentType()).isEqualTo(ClientDocument.DocumentType.KYC_DOCUMENT);
    }

    @Test
    void findByClientIdAndDocumentCategoryAndIsActiveTrue_WithValidCategory_ReturnsFilteredDocuments() {
        // Act
        List<ClientDocument> documents = documentRepository.findByClientIdAndDocumentCategoryAndIsActiveTrue(
                testClient1.getId(), ClientDocument.DocumentCategory.FINANCIAL);

        // Assert
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getDocumentName()).isEqualTo("Bank Statement.pdf");
        assertThat(documents.get(0).getDocumentCategory()).isEqualTo(ClientDocument.DocumentCategory.FINANCIAL);
    }

    @Test
    void findByClientIdAndDocumentStatusAndIsActiveTrue_WithValidStatus_ReturnsFilteredDocuments() {
        // Act
        List<ClientDocument> documents = documentRepository.findByClientIdAndDocumentStatusAndIsActiveTrue(
                testClient1.getId(), ClientDocument.DocumentStatus.APPROVED);

        // Assert
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getDocumentName()).isEqualTo("Bank Statement.pdf");
        assertThat(documents.get(0).getDocumentStatus()).isEqualTo(ClientDocument.DocumentStatus.APPROVED);
    }

    @Test
    void findByIdAndClientId_WithValidIds_ReturnsDocument() {
        // Act
        Optional<ClientDocument> document = documentRepository.findByIdAndClientId(
                testDocument1.getId(), testClient1.getId());

        // Assert
        assertThat(document).isPresent();
        assertThat(document.get().getDocumentName()).isEqualTo("KYC Document.pdf");
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
        // Act
        List<ClientDocument> documents = documentRepository.findByClientIdAndUploadedByClientTrueAndIsActiveTrue(
                testClient1.getId());

        // Assert
        assertThat(documents).hasSize(2);
        assertThat(documents).allMatch(ClientDocument::getUploadedByClient);
        assertThat(documents).extracting(ClientDocument::getDocumentName)
                .containsExactlyInAnyOrder("KYC Document.pdf", "Bank Statement.pdf");
    }

    @Test
    void findByClientIdAndUploadedByClientFalseAndIsActiveTrue_ReturnsSystemDocuments() {
        // Act
        List<ClientDocument> documents = documentRepository.findByClientIdAndUploadedByClientFalseAndIsActiveTrue(
                testClient2.getId());

        // Assert
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getDocumentName()).isEqualTo("Tax Document.pdf");
        assertThat(documents.get(0).getUploadedByClient()).isFalse();
    }

    @Test
    void searchByName_WithMatchingTerm_ReturnsMatchingDocuments() {
        // Act
        List<ClientDocument> documents = documentRepository.searchByName(testClient1.getId(), "bank");

        // Assert
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getDocumentName()).isEqualTo("Bank Statement.pdf");
    }

    @Test
    void searchDocuments_WithMatchingTermInDescription_ReturnsMatchingDocuments() {
        // Act
        List<ClientDocument> documents = documentRepository.searchDocuments(testClient1.getId(), "verification");

        // Assert
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getDocumentName()).isEqualTo("KYC Document.pdf");
    }

    @Test
    void searchDocuments_WithMatchingTermInTags_ReturnsMatchingDocuments() {
        // Act
        List<ClientDocument> documents = documentRepository.searchDocuments(testClient1.getId(), "financial");

        // Assert
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getDocumentName()).isEqualTo("Bank Statement.pdf");
    }

    @Test
    void findExpiringSoon_WithDocumentsExpiringSoon_ReturnsExpiringSoonDocuments() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(60);

        // Act
        List<ClientDocument> documents = documentRepository.findExpiringSoon(testClient1.getId(), now, futureDate);

        // Assert
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getDocumentName()).isEqualTo("Bank Statement.pdf");
    }

    @Test
    void findExpired_WithExpiredDocuments_ReturnsExpiredDocuments() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Act
        List<ClientDocument> documents = documentRepository.findExpired(testClient2.getId(), now);

        // Assert
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getDocumentName()).isEqualTo("Tax Document.pdf");
        assertThat(documents.get(0).getExpiryDate()).isBefore(now);
    }

    @Test
    void findRecentUploads_WithRecentDocuments_ReturnsRecentDocuments() {
        // Arrange
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(3);

        // Act
        List<ClientDocument> documents = documentRepository.findRecentUploads(testClient1.getId(), sinceDate);

        // Assert
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getDocumentName()).isEqualTo("Bank Statement.pdf");
        assertThat(documents.get(0).getUploadDate()).isAfter(sinceDate);
    }

    @Test
    void findMostAccessed_WithAccessCountData_ReturnsMostAccessedDocuments() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5);

        // Act
        List<ClientDocument> documents = documentRepository.findMostAccessed(testClient1.getId(), pageable);

        // Assert
        assertThat(documents).hasSize(2);
        // Should be ordered by access count DESC
        assertThat(documents.get(0).getDocumentName()).isEqualTo("KYC Document.pdf");
        assertThat(documents.get(0).getAccessCount()).isEqualTo(3);
        assertThat(documents.get(1).getDocumentName()).isEqualTo("Bank Statement.pdf");
        assertThat(documents.get(1).getAccessCount()).isEqualTo(1);
    }

    @Test
    void findWithFilters_WithMultipleFilters_ReturnsFilteredDocuments() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<ClientDocument> documentsPage = documentRepository.findWithFilters(
                testClient1.getId(),
                ClientDocument.DocumentType.KYC_DOCUMENT,
                ClientDocument.DocumentCategory.KYC,
                ClientDocument.DocumentStatus.UPLOADED,
                true,
                pageable
        );

        // Assert
        assertThat(documentsPage.getTotalElements()).isEqualTo(1);
        assertThat(documentsPage.getContent().get(0).getDocumentName()).isEqualTo("KYC Document.pdf");
    }

    @Test
    void findWithFilters_WithNullFilters_ReturnsAllActiveDocuments() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<ClientDocument> documentsPage = documentRepository.findWithFilters(
                testClient1.getId(), null, null, null, null, pageable);

        // Assert
        assertThat(documentsPage.getTotalElements()).isEqualTo(2);
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

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    void getTotalFileSizeForClient_WithDocuments_ReturnsCorrectTotalSize() {
        // Act
        Long totalSize = documentRepository.getTotalFileSizeForClient(testClient1.getId());

        // Assert
        assertThat(totalSize).isEqualTo(3072L); // 1024 + 2048
    }

    @Test
    void getTotalFileSizeForClient_WithNoDocuments_ReturnsZero() {
        // Arrange
        Client emptyClient = Client.builder()
                .firstName("Empty")
                .lastName("Client")
                .emailPrimary("empty@example.com")
                .phonePrimary("+1234567892")
                .membershipNumber("MEM003")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        emptyClient = clientRepository.save(emptyClient);

        // Act
        Long totalSize = documentRepository.getTotalFileSizeForClient(emptyClient.getId());

        // Assert
        assertThat(totalSize).isEqualTo(0L);
    }
} 