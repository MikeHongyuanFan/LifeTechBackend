package com.finance.admin.client.document.dto;

import com.finance.admin.client.document.model.ClientDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for client document information
 * Used in Legal Document Center for document viewing and management
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDocumentResponse {

    private Long id;
    private String documentName;
    private ClientDocument.DocumentType documentType;
    private String documentTypeDisplayName;
    private ClientDocument.DocumentCategory documentCategory;
    private String documentCategoryDisplayName;
    private Long fileSize;
    private String fileSizeFormatted;
    private String mimeType;
    private LocalDateTime uploadDate;
    private Boolean uploadedByClient;
    private Boolean isActive;
    private ClientDocument.DocumentStatus documentStatus;
    private String documentStatusDisplayName;
    private Integer versionNumber;
    private String description;
    private LocalDateTime expiryDate;
    private Boolean isExpired;
    private Integer accessCount;
    private LocalDateTime lastAccessedDate;
    private String tags;
    private List<String> tagList;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional metadata
    private String downloadUrl;
    private String previewUrl;
    private Boolean canDownload;
    private Boolean canDelete;
    private Boolean canReplace;
    private DocumentSecurityInfo securityInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentSecurityInfo {
        private Boolean isEncrypted;
        private Boolean requiresAuthentication;
        private String accessLevel;
        private Boolean isPublic;
    }

    /**
     * Helper method to format file size in human-readable format
     */
    public static String formatFileSize(Long bytes) {
        if (bytes == null) return "Unknown";
        if (bytes == 0) return "0 B";
        
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        final long[] thresholds = {1L, 1024L, 1048576L, 1073741824L, 1099511627776L};
        
        for (int i = units.length - 1; i >= 0; i--) {
            if (bytes >= thresholds[i]) {
                if (i == 0) {
                    return bytes + " B";
                } else {
                    double value = (double) bytes / thresholds[i];
                    return String.format("%.1f %s", value, units[i]);
                }
            }
        }
        
        return bytes + " B";
    }

    /**
     * Convert tags string to list
     */
    public static List<String> parseTagsToList(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return List.of();
        }
        return List.of(tags.split(",")).stream()
            .map(String::trim)
            .filter(tag -> !tag.isEmpty())
            .toList();
    }
} 