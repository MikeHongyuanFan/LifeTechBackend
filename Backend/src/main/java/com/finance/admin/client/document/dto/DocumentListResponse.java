package com.finance.admin.client.document.dto;

import com.finance.admin.client.document.model.ClientDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for document listing in Legal Document Center
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentListResponse {

    private List<ClientDocumentResponse> documents;
    private DocumentStatistics statistics;
    private Map<String, Integer> categoryCount;
    private Map<String, Integer> typeCount;
    private Map<String, Integer> statusCount;
    private PaginationInfo pagination;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentStatistics {
        private Long totalDocuments;
        private Long totalFileSize;
        private String totalFileSizeFormatted;
        private Integer recentUploads;
        private Integer expiringSoon;
        private Integer expiredDocuments;
        private Integer uploadedByClient;
        private Integer systemDocuments;
        private LocalDateTime lastUploadDate;
        private Integer averageAccessCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaginationInfo {
        private Integer currentPage;
        private Integer totalPages;
        private Long totalElements;
        private Integer pageSize;
        private Boolean hasNext;
        private Boolean hasPrevious;
    }
}

/**
 * Request DTO for filtering documents
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class DocumentFilterRequest {

    private ClientDocument.DocumentType documentType;
    private ClientDocument.DocumentCategory documentCategory;
    private ClientDocument.DocumentStatus documentStatus;
    private Boolean uploadedByClient;
    private String searchTerm;
    private LocalDateTime uploadDateFrom;
    private LocalDateTime uploadDateTo;
    private Boolean includeExpired;
    private String sortBy;
    private String sortDirection;
    private Integer page;
    private Integer size;
}

/**
 * Response DTO for document categories
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class DocumentCategoriesResponse {

    private List<CategoryInfo> documentTypes;
    private List<CategoryInfo> documentCategories;
    private List<String> availableStatuses;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryInfo {
        private String value;
        private String displayName;
        private String description;
        private Integer documentCount;
    }
}

/**
 * Response DTO for document upload result
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class DocumentUploadResponse {

    private Long documentId;
    private String documentName;
    private ClientDocument.DocumentType documentType;
    private ClientDocument.DocumentCategory documentCategory;
    private Long fileSize;
    private String fileSizeFormatted;
    private String mimeType;
    private LocalDateTime uploadDate;
    private ClientDocument.DocumentStatus documentStatus;
    private String message;
    private Boolean success;
    private String downloadUrl;
    private String previewUrl;
}

/**
 * Request DTO for document replacement
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class DocumentReplaceRequest {
    private DocumentUploadRequest uploadRequest;
    private Long originalDocumentId;
    private String replacementReason;
}

/**
 * Response DTO for document deletion
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class DocumentDeleteResponse {
    private Long documentId;
    private String documentName;
    private Boolean success;
    private String message;
    private LocalDateTime deletedAt;
}

/**
 * Request DTO for bulk document operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class BulkDocumentOperationRequest {
    private List<Long> documentIds;
    private String operation; // DELETE, ARCHIVE, UPDATE_STATUS
    private ClientDocument.DocumentStatus newStatus;
    private String reason;
}

/**
 * Response DTO for bulk document operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class BulkDocumentOperationResponse {
    private Integer totalRequested;
    private Integer successful;
    private Integer failed;
    private List<String> errors;
    private String message;
}

/**
 * DTO for document search results
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class DocumentSearchResponse {
    private List<ClientDocumentResponse> results;
    private Integer totalResults;
    private String searchTerm;
    private LocalDateTime searchDate;
    private SearchMetadata metadata;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchMetadata {
        private Map<String, Integer> categoryBreakdown;
        private Map<String, Integer> typeBreakdown;
        private Integer exactMatches;
        private Integer partialMatches;
    }
} 