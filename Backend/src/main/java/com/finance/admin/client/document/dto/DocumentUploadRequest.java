package com.finance.admin.client.document.dto;

import com.finance.admin.client.document.model.ClientDocument;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * Request DTO for uploading client documents
 * Used in Legal Document Center for file upload operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUploadRequest {

    @NotNull(message = "File is required")
    private MultipartFile file;

    @NotBlank(message = "Document name is required")
    @Size(max = 255, message = "Document name must not exceed 255 characters")
    private String documentName;

    @NotNull(message = "Document type is required")
    private ClientDocument.DocumentType documentType;

    @NotNull(message = "Document category is required")
    private ClientDocument.DocumentCategory documentCategory;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private LocalDateTime expiryDate;

    @Size(max = 500, message = "Tags must not exceed 500 characters")
    private String tags;

    // Optional metadata
    private Boolean replaceExisting;
    private Long replacingDocumentId;
    
    /**
     * Validate upload request
     */
    public void validate() {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required and cannot be empty");
        }
        
        if (documentName == null || documentName.trim().isEmpty()) {
            throw new IllegalArgumentException("Document name is required");
        }
        
        if (documentType == null) {
            throw new IllegalArgumentException("Document type is required");
        }
        
        if (documentCategory == null) {
            throw new IllegalArgumentException("Document category is required");
        }
        
        // Validate file size (max 50MB)
        long maxFileSize = 50 * 1024 * 1024; // 50MB
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size cannot exceed 50MB");
        }
        
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedFileType(contentType)) {
            throw new IllegalArgumentException("File type not supported. Allowed types: PDF, JPG, PNG, DOC, DOCX");
        }
    }
    
    /**
     * Check if file type is allowed
     */
    private boolean isAllowedFileType(String contentType) {
        return contentType.equals("application/pdf") ||
               contentType.equals("image/jpeg") ||
               contentType.equals("image/jpg") ||
               contentType.equals("image/png") ||
               contentType.equals("application/msword") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }
} 