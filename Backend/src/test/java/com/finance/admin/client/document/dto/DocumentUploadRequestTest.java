package com.finance.admin.client.document.dto;

import com.finance.admin.client.document.model.ClientDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for DocumentUploadRequest DTO - Sprint 3.3 Legal Document Center
 */
class DocumentUploadRequestTest {

    private MockMultipartFile validFile;
    private DocumentUploadRequest validRequest;

    @BeforeEach
    void setUp() {
        validFile = new MockMultipartFile(
                "file", 
                "test-document.pdf", 
                "application/pdf", 
                "test content".getBytes()
        );

        validRequest = DocumentUploadRequest.builder()
                .file(validFile)
                .documentName("Test Document")
                .documentType(ClientDocument.DocumentType.KYC_DOCUMENT)
                .documentCategory(ClientDocument.DocumentCategory.KYC)
                .description("Test description")
                .tags("test,kyc")
                .build();
    }

    @Test
    void validate_WithValidRequest_DoesNotThrow() {
        // Act & Assert
        assertThatCode(() -> validRequest.validate())
                .doesNotThrowAnyException();
    }

    @Test
    void validate_WithNullFile_ThrowsException() {
        // Arrange
        validRequest.setFile(null);

        // Act & Assert
        assertThatThrownBy(() -> validRequest.validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File is required");
    }

    @Test
    void validate_WithEmptyFile_ThrowsException() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);
        validRequest.setFile(emptyFile);

        // Act & Assert
        assertThatThrownBy(() -> validRequest.validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File is required and cannot be empty");
    }

    @Test
    void validate_WithBlankDocumentName_ThrowsException() {
        // Arrange
        validRequest.setDocumentName("   ");

        // Act & Assert
        assertThatThrownBy(() -> validRequest.validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Document name is required");
    }

    @Test
    void validate_WithNullDocumentType_ThrowsException() {
        // Arrange
        validRequest.setDocumentType(null);

        // Act & Assert
        assertThatThrownBy(() -> validRequest.validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Document type is required");
    }

    @Test
    void validate_WithNullDocumentCategory_ThrowsException() {
        // Arrange
        validRequest.setDocumentCategory(null);

        // Act & Assert
        assertThatThrownBy(() -> validRequest.validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Document category is required");
    }

    @Test
    void validate_WithUnsupportedFileType_ThrowsException() {
        // Arrange
        MockMultipartFile unsupportedFile = new MockMultipartFile(
                "file", "test.exe", "application/x-executable", "test content".getBytes());
        validRequest.setFile(unsupportedFile);

        // Act & Assert
        assertThatThrownBy(() -> validRequest.validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File type not supported");
    }

    @Test
    void validate_WithFileTooLarge_ThrowsException() {
        // Arrange
        byte[] largeContent = new byte[60 * 1024 * 1024]; // 60MB > 50MB limit
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large.pdf", "application/pdf", largeContent);
        validRequest.setFile(largeFile);

        // Act & Assert
        assertThatThrownBy(() -> validRequest.validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File size cannot exceed 50MB");
    }

    @Test
    void validate_WithSupportedFileTypes_DoesNotThrow() {
        // Test PDF
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "content".getBytes());
        validRequest.setFile(pdfFile);
        assertThatCode(() -> validRequest.validate()).doesNotThrowAnyException();

        // Test JPG
        MockMultipartFile jpgFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "content".getBytes());
        validRequest.setFile(jpgFile);
        assertThatCode(() -> validRequest.validate()).doesNotThrowAnyException();

        // Test PNG
        MockMultipartFile pngFile = new MockMultipartFile(
                "file", "test.png", "image/png", "content".getBytes());
        validRequest.setFile(pngFile);
        assertThatCode(() -> validRequest.validate()).doesNotThrowAnyException();

        // Test DOC
        MockMultipartFile docFile = new MockMultipartFile(
                "file", "test.doc", "application/msword", "content".getBytes());
        validRequest.setFile(docFile);
        assertThatCode(() -> validRequest.validate()).doesNotThrowAnyException();

        // Test DOCX
        MockMultipartFile docxFile = new MockMultipartFile(
                "file", "test.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "content".getBytes());
        validRequest.setFile(docxFile);
        assertThatCode(() -> validRequest.validate()).doesNotThrowAnyException();
    }

    @Test
    void builder_WithAllFields_CreatesValidRequest() {
        // Arrange
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(365);

        // Act
        DocumentUploadRequest request = DocumentUploadRequest.builder()
                .file(validFile)
                .documentName("Complete Document")
                .documentType(ClientDocument.DocumentType.BANK_STATEMENT)
                .documentCategory(ClientDocument.DocumentCategory.FINANCIAL)
                .description("Complete description")
                .expiryDate(expiryDate)
                .tags("tag1,tag2,tag3")
                .replaceExisting(true)
                .replacingDocumentId(123L)
                .build();

        // Assert
        assertThat(request.getFile()).isEqualTo(validFile);
        assertThat(request.getDocumentName()).isEqualTo("Complete Document");
        assertThat(request.getDocumentType()).isEqualTo(ClientDocument.DocumentType.BANK_STATEMENT);
        assertThat(request.getDocumentCategory()).isEqualTo(ClientDocument.DocumentCategory.FINANCIAL);
        assertThat(request.getDescription()).isEqualTo("Complete description");
        assertThat(request.getExpiryDate()).isEqualTo(expiryDate);
        assertThat(request.getTags()).isEqualTo("tag1,tag2,tag3");
        assertThat(request.getReplaceExisting()).isTrue();
        assertThat(request.getReplacingDocumentId()).isEqualTo(123L);
    }

    @Test
    void settersAndGetters_WorkCorrectly() {
        // Arrange
        DocumentUploadRequest request = new DocumentUploadRequest();
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(30);

        // Act
        request.setFile(validFile);
        request.setDocumentName("Test Name");
        request.setDocumentType(ClientDocument.DocumentType.ADDRESS_PROOF);
        request.setDocumentCategory(ClientDocument.DocumentCategory.IDENTITY);
        request.setDescription("Test Description");
        request.setExpiryDate(expiryDate);
        request.setTags("test,tags");
        request.setReplaceExisting(false);
        request.setReplacingDocumentId(456L);

        // Assert
        assertThat(request.getFile()).isEqualTo(validFile);
        assertThat(request.getDocumentName()).isEqualTo("Test Name");
        assertThat(request.getDocumentType()).isEqualTo(ClientDocument.DocumentType.ADDRESS_PROOF);
        assertThat(request.getDocumentCategory()).isEqualTo(ClientDocument.DocumentCategory.IDENTITY);
        assertThat(request.getDescription()).isEqualTo("Test Description");
        assertThat(request.getExpiryDate()).isEqualTo(expiryDate);
        assertThat(request.getTags()).isEqualTo("test,tags");
        assertThat(request.getReplaceExisting()).isFalse();
        assertThat(request.getReplacingDocumentId()).isEqualTo(456L);
    }
} 