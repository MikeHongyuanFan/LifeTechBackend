package com.finance.admin.client.document.dto;

import com.finance.admin.client.document.model.ClientDocument;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ClientDocumentResponse DTO - Sprint 3.3 Legal Document Center
 */
class ClientDocumentResponseTest {

    @Test
    void formatFileSize_WithBytesOnly_ReturnsFormattedString() {
        // Act & Assert
        assertThat(ClientDocumentResponse.formatFileSize(0L)).isEqualTo("0 B");
        assertThat(ClientDocumentResponse.formatFileSize(512L)).isEqualTo("512 B");
        assertThat(ClientDocumentResponse.formatFileSize(1023L)).isEqualTo("1023 B");
    }

    @Test
    void formatFileSize_WithKilobytes_ReturnsFormattedString() {
        // Act & Assert
        assertThat(ClientDocumentResponse.formatFileSize(1024L)).isEqualTo("1.0 KB");
        assertThat(ClientDocumentResponse.formatFileSize(1536L)).isEqualTo("1.5 KB");
        assertThat(ClientDocumentResponse.formatFileSize(2048L)).isEqualTo("2.0 KB");
    }

    @Test
    void formatFileSize_WithMegabytes_ReturnsFormattedString() {
        // Act & Assert
        assertThat(ClientDocumentResponse.formatFileSize(1048576L)).isEqualTo("1.0 MB");
        assertThat(ClientDocumentResponse.formatFileSize(1572864L)).isEqualTo("1.5 MB");
        assertThat(ClientDocumentResponse.formatFileSize(5242880L)).isEqualTo("5.0 MB");
    }

    @Test
    void formatFileSize_WithGigabytes_ReturnsFormattedString() {
        // Act & Assert
        assertThat(ClientDocumentResponse.formatFileSize(1073741824L)).isEqualTo("1.0 GB");
        assertThat(ClientDocumentResponse.formatFileSize(2147483648L)).isEqualTo("2.0 GB");
    }

    @Test
    void formatFileSize_WithNullSize_ReturnsUnknown() {
        // Act & Assert
        assertThat(ClientDocumentResponse.formatFileSize(null)).isEqualTo("Unknown");
    }

    @Test
    void parseTagsToList_WithValidTags_ReturnsTagList() {
        // Act & Assert
        List<String> tags = ClientDocumentResponse.parseTagsToList("tag1,tag2,tag3");
        assertThat(tags).containsExactly("tag1", "tag2", "tag3");
    }

    @Test
    void parseTagsToList_WithSpacesInTags_ReturnsTrimmeddTags() {
        // Act & Assert
        List<String> tags = ClientDocumentResponse.parseTagsToList(" tag1 , tag2 , tag3 ");
        assertThat(tags).containsExactly("tag1", "tag2", "tag3");
    }

    @Test
    void parseTagsToList_WithEmptyTags_ReturnsEmptyList() {
        // Act & Assert
        assertThat(ClientDocumentResponse.parseTagsToList("")).isEmpty();
        assertThat(ClientDocumentResponse.parseTagsToList("   ")).isEmpty();
    }

    @Test
    void parseTagsToList_WithNullTags_ReturnsEmptyList() {
        // Act & Assert
        assertThat(ClientDocumentResponse.parseTagsToList(null)).isEmpty();
    }

    @Test
    void parseTagsToList_WithSingleTag_ReturnsSingletonList() {
        // Act & Assert
        List<String> tags = ClientDocumentResponse.parseTagsToList("single-tag");
        assertThat(tags).containsExactly("single-tag");
    }

    @Test
    void parseTagsToList_WithEmptyTagsInString_FiltersEmptyTags() {
        // Act & Assert
        List<String> tags = ClientDocumentResponse.parseTagsToList("tag1,,tag3,");
        assertThat(tags).containsExactly("tag1", "tag3");
    }

    @Test
    void builder_CreatesCompleteResponse() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        ClientDocumentResponse.DocumentSecurityInfo securityInfo = 
                ClientDocumentResponse.DocumentSecurityInfo.builder()
                        .isEncrypted(true)
                        .requiresAuthentication(true)
                        .accessLevel("CLIENT")
                        .isPublic(false)
                        .build();

        // Act
        ClientDocumentResponse response = ClientDocumentResponse.builder()
                .id(1L)
                .documentName("Test Document.pdf")
                .documentType(ClientDocument.DocumentType.KYC_DOCUMENT)
                .documentTypeDisplayName("KYC Document")
                .documentCategory(ClientDocument.DocumentCategory.KYC)
                .documentCategoryDisplayName("Know Your Customer")
                .fileSize(1024L)
                .fileSizeFormatted("1.0 KB")
                .mimeType("application/pdf")
                .uploadDate(now)
                .uploadedByClient(true)
                .isActive(true)
                .documentStatus(ClientDocument.DocumentStatus.UPLOADED)
                .documentStatusDisplayName("Uploaded")
                .versionNumber(1)
                .description("Test description")
                .expiryDate(now.plusDays(365))
                .isExpired(false)
                .accessCount(5)
                .lastAccessedDate(now.minusHours(1))
                .tags("test,kyc,document")
                .tagList(Arrays.asList("test", "kyc", "document"))
                .createdAt(now.minusDays(1))
                .updatedAt(now)
                .downloadUrl("/api/client/documents/1/download")
                .previewUrl("/api/client/documents/1/preview")
                .canDownload(true)
                .canDelete(true)
                .canReplace(true)
                .securityInfo(securityInfo)
                .build();

        // Assert
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getDocumentName()).isEqualTo("Test Document.pdf");
        assertThat(response.getDocumentType()).isEqualTo(ClientDocument.DocumentType.KYC_DOCUMENT);
        assertThat(response.getDocumentTypeDisplayName()).isEqualTo("KYC Document");
        assertThat(response.getDocumentCategory()).isEqualTo(ClientDocument.DocumentCategory.KYC);
        assertThat(response.getDocumentCategoryDisplayName()).isEqualTo("Know Your Customer");
        assertThat(response.getFileSize()).isEqualTo(1024L);
        assertThat(response.getFileSizeFormatted()).isEqualTo("1.0 KB");
        assertThat(response.getMimeType()).isEqualTo("application/pdf");
        assertThat(response.getUploadDate()).isEqualTo(now);
        assertThat(response.getUploadedByClient()).isTrue();
        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getDocumentStatus()).isEqualTo(ClientDocument.DocumentStatus.UPLOADED);
        assertThat(response.getDocumentStatusDisplayName()).isEqualTo("Uploaded");
        assertThat(response.getVersionNumber()).isEqualTo(1);
        assertThat(response.getDescription()).isEqualTo("Test description");
        assertThat(response.getExpiryDate()).isEqualTo(now.plusDays(365));
        assertThat(response.getIsExpired()).isFalse();
        assertThat(response.getAccessCount()).isEqualTo(5);
        assertThat(response.getLastAccessedDate()).isEqualTo(now.minusHours(1));
        assertThat(response.getTags()).isEqualTo("test,kyc,document");
        assertThat(response.getTagList()).containsExactly("test", "kyc", "document");
        assertThat(response.getCreatedAt()).isEqualTo(now.minusDays(1));
        assertThat(response.getUpdatedAt()).isEqualTo(now);
        assertThat(response.getDownloadUrl()).isEqualTo("/api/client/documents/1/download");
        assertThat(response.getPreviewUrl()).isEqualTo("/api/client/documents/1/preview");
        assertThat(response.getCanDownload()).isTrue();
        assertThat(response.getCanDelete()).isTrue();
        assertThat(response.getCanReplace()).isTrue();
        assertThat(response.getSecurityInfo()).isEqualTo(securityInfo);
    }

    @Test
    void documentSecurityInfo_Builder_CreatesCompleteSecurityInfo() {
        // Act
        ClientDocumentResponse.DocumentSecurityInfo securityInfo = 
                ClientDocumentResponse.DocumentSecurityInfo.builder()
                        .isEncrypted(true)
                        .requiresAuthentication(true)
                        .accessLevel("ADMIN")
                        .isPublic(false)
                        .build();

        // Assert
        assertThat(securityInfo.getIsEncrypted()).isTrue();
        assertThat(securityInfo.getRequiresAuthentication()).isTrue();
        assertThat(securityInfo.getAccessLevel()).isEqualTo("ADMIN");
        assertThat(securityInfo.getIsPublic()).isFalse();
    }

    @Test
    void documentSecurityInfo_DefaultConstructor_CreatesEmptySecurityInfo() {
        // Act
        ClientDocumentResponse.DocumentSecurityInfo securityInfo = 
                new ClientDocumentResponse.DocumentSecurityInfo();

        // Assert
        assertThat(securityInfo.getIsEncrypted()).isNull();
        assertThat(securityInfo.getRequiresAuthentication()).isNull();
        assertThat(securityInfo.getAccessLevel()).isNull();
        assertThat(securityInfo.getIsPublic()).isNull();
    }

    @Test
    void documentSecurityInfo_SettersAndGetters_WorkCorrectly() {
        // Arrange
        ClientDocumentResponse.DocumentSecurityInfo securityInfo = 
                new ClientDocumentResponse.DocumentSecurityInfo();

        // Act
        securityInfo.setIsEncrypted(false);
        securityInfo.setRequiresAuthentication(true);
        securityInfo.setAccessLevel("CLIENT");
        securityInfo.setIsPublic(true);

        // Assert
        assertThat(securityInfo.getIsEncrypted()).isFalse();
        assertThat(securityInfo.getRequiresAuthentication()).isTrue();
        assertThat(securityInfo.getAccessLevel()).isEqualTo("CLIENT");
        assertThat(securityInfo.getIsPublic()).isTrue();
    }

    @Test
    void settersAndGetters_WorkCorrectly() {
        // Arrange
        ClientDocumentResponse response = new ClientDocumentResponse();
        LocalDateTime now = LocalDateTime.now();

        // Act
        response.setId(100L);
        response.setDocumentName("Setter Test.pdf");
        response.setDocumentType(ClientDocument.DocumentType.BANK_STATEMENT);
        response.setFileSize(2048L);
        response.setUploadDate(now);
        response.setAccessCount(10);

        // Assert
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getDocumentName()).isEqualTo("Setter Test.pdf");
        assertThat(response.getDocumentType()).isEqualTo(ClientDocument.DocumentType.BANK_STATEMENT);
        assertThat(response.getFileSize()).isEqualTo(2048L);
        assertThat(response.getUploadDate()).isEqualTo(now);
        assertThat(response.getAccessCount()).isEqualTo(10);
    }
} 