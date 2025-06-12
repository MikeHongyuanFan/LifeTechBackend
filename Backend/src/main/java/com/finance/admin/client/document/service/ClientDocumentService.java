package com.finance.admin.client.document.service;

import com.finance.admin.client.document.dto.*;
import com.finance.admin.client.document.model.ClientDocument;
import com.finance.admin.client.document.repository.ClientDocumentRepository;
import com.finance.admin.client.repository.ClientRepository;
import com.finance.admin.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for Legal Document Center functionality
 * Handles document viewing, uploading, and management operations for clients
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClientDocumentService {

    private final ClientDocumentRepository documentRepository;
    private final ClientRepository clientRepository;

    @Value("${app.document.upload.path:/tmp/documents}")
    private String uploadPath;

    @Value("${app.document.max-file-size:52428800}") // 50MB
    private long maxFileSize;

    // ================ Sprint 3.3.1 - View Documents ================

    /**
     * Get all documents for a client with pagination and filtering
     */
    public DocumentListResponse getClientDocuments(Long clientId, 
                                                 ClientDocument.DocumentType documentType,
                                                 ClientDocument.DocumentCategory documentCategory,
                                                 ClientDocument.DocumentStatus documentStatus,
                                                 Boolean uploadedByClient,
                                                 String searchTerm,
                                                 Integer page, 
                                                 Integer size,
                                                 String sortBy,
                                                 String sortDirection) {
        log.info("Getting documents for client: {} with filters", clientId);
        
        // Validate client exists
        validateClientExists(clientId);
        
        // Set defaults
        page = page != null ? page : 0;
        size = size != null ? size : 20;
        sortBy = sortBy != null ? sortBy : "uploadDate";
        sortDirection = sortDirection != null ? sortDirection : "DESC";
        
        // Create pageable with sorting
        Sort sort = Sort.by(sortDirection.equalsIgnoreCase("DESC") ? 
                           Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get filtered documents
        Page<ClientDocument> documentsPage;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            List<ClientDocument> searchResults = documentRepository.searchDocuments(clientId, searchTerm);
            documentsPage = convertListToPage(searchResults, pageable);
        } else {
            documentsPage = documentRepository.findWithFilters(clientId, documentType, 
                                                              documentCategory, documentStatus, 
                                                              uploadedByClient, pageable);
        }
        
        // Convert to response DTOs
        List<ClientDocumentResponse> documentResponses = documentsPage.getContent().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        // Get statistics
        DocumentListResponse.DocumentStatistics statistics = getDocumentStatistics(clientId);
        
        // Get counts by category/type/status
        Map<String, Integer> categoryCount = getCategoryCount(clientId);
        Map<String, Integer> typeCount = getTypeCount(clientId);
        Map<String, Integer> statusCount = getStatusCount(clientId);
        
        // Create pagination info
        DocumentListResponse.PaginationInfo paginationInfo = DocumentListResponse.PaginationInfo.builder()
            .currentPage(documentsPage.getNumber())
            .totalPages(documentsPage.getTotalPages())
            .totalElements(documentsPage.getTotalElements())
            .pageSize(documentsPage.getSize())
            .hasNext(documentsPage.hasNext())
            .hasPrevious(documentsPage.hasPrevious())
            .build();
        
        return DocumentListResponse.builder()
            .documents(documentResponses)
            .statistics(statistics)
            .categoryCount(categoryCount)
            .typeCount(typeCount)
            .statusCount(statusCount)
            .pagination(paginationInfo)
            .build();
    }

    /**
     * Get a specific document by ID
     */
    public ClientDocumentResponse getDocumentById(Long clientId, Long documentId) {
        log.info("Getting document {} for client: {}", documentId, clientId);
        
        ClientDocument document = documentRepository.findByIdAndClientId(documentId, clientId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        
        // Increment access count
        document.incrementAccessCount();
        documentRepository.save(document);
        
        return convertToResponse(document);
    }

    /**
     * Download a document
     */
    public ResponseEntity<byte[]> downloadDocument(Long clientId, Long documentId) {
        log.info("Downloading document {} for client: {}", documentId, clientId);
        
        ClientDocument document = documentRepository.findByIdAndClientId(documentId, clientId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        
        try {
            Path filePath = Paths.get(document.getFilePath());
            byte[] fileContent = Files.readAllBytes(filePath);
            
            // Increment access count
            document.incrementAccessCount();
            documentRepository.save(document);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(document.getMimeType()));
            headers.setContentDispositionFormData("attachment", document.getDocumentName());
            headers.setContentLength(fileContent.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
                
        } catch (IOException e) {
            log.error("Error reading file: {}", e.getMessage());
            throw new RuntimeException("Error downloading file", e);
        }
    }

    /**
     * Get document categories and types
     */
    public Map<String, Object> getDocumentCategories(Long clientId) {
        log.info("Getting document categories for client: {}", clientId);
        
        validateClientExists(clientId);
        
        Map<String, Object> categories = new HashMap<>();
        
        // Document types with counts
        List<Map<String, Object>> documentTypes = Arrays.stream(ClientDocument.DocumentType.values())
            .map(type -> {
                long count = documentRepository.findByClientIdAndDocumentTypeAndIsActiveTrue(clientId, type).size();
                Map<String, Object> typeInfo = new HashMap<>();
                typeInfo.put("value", type.name());
                typeInfo.put("displayName", type.getDisplayName());
                typeInfo.put("documentCount", count);
                return typeInfo;
            })
            .collect(Collectors.toList());
        
        // Document categories with counts
        List<Map<String, Object>> documentCategories = Arrays.stream(ClientDocument.DocumentCategory.values())
            .map(category -> {
                long count = documentRepository.findByClientIdAndDocumentCategoryAndIsActiveTrue(clientId, category).size();
                Map<String, Object> categoryInfo = new HashMap<>();
                categoryInfo.put("value", category.name());
                categoryInfo.put("displayName", category.getDisplayName());
                categoryInfo.put("documentCount", count);
                return categoryInfo;
            })
            .collect(Collectors.toList());
        
        // Available statuses
        List<String> availableStatuses = Arrays.stream(ClientDocument.DocumentStatus.values())
            .map(ClientDocument.DocumentStatus::name)
            .collect(Collectors.toList());
        
        categories.put("documentTypes", documentTypes);
        categories.put("documentCategories", documentCategories);
        categories.put("availableStatuses", availableStatuses);
        
        return categories;
    }

    // ================ Sprint 3.3.2 - Upload Files ================

    /**
     * Upload a new document for a client
     */
    public ClientDocumentResponse uploadDocument(Long clientId, DocumentUploadRequest request) {
        log.info("Uploading document for client: {}", clientId);
        
        // Validate client exists
        validateClientExists(clientId);
        
        // Validate upload request
        request.validate();
        
        try {
            // Save file to disk
            String savedFilePath = saveUploadedFile(request.getFile(), clientId);
            
            // Create document entity
            ClientDocument document = ClientDocument.builder()
                .client(clientRepository.findById(clientId).orElseThrow())
                .documentName(request.getDocumentName())
                .documentType(request.getDocumentType())
                .documentCategory(request.getDocumentCategory())
                .filePath(savedFilePath)
                .fileSize(request.getFile().getSize())
                .mimeType(request.getFile().getContentType())
                .uploadDate(LocalDateTime.now())
                .uploadedByClient(true)
                .description(request.getDescription())
                .expiryDate(request.getExpiryDate())
                .tags(request.getTags())
                .isActive(true)
                .documentStatus(ClientDocument.DocumentStatus.UPLOADED)
                .versionNumber(1)
                .accessCount(0)
                .build();
            
            // Handle document replacement
            if (Boolean.TRUE.equals(request.getReplaceExisting()) && request.getReplacingDocumentId() != null) {
                ClientDocument originalDocument = documentRepository.findByIdAndClientId(
                    request.getReplacingDocumentId(), clientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Original document not found"));
                
                // Mark original as replaced
                originalDocument.setIsActive(false);
                originalDocument.setDocumentStatus(ClientDocument.DocumentStatus.REPLACED);
                documentRepository.save(originalDocument);
                
                // Set new version number
                document.setVersionNumber(originalDocument.getVersionNumber() + 1);
            }
            
            // Save document
            ClientDocument savedDocument = documentRepository.save(document);
            
            log.info("Document uploaded successfully with ID: {}", savedDocument.getId());
            return convertToResponse(savedDocument);
            
        } catch (Exception e) {
            log.error("Error uploading document: {}", e.getMessage());
            throw new RuntimeException("Error uploading document", e);
        }
    }

    // ================ Sprint 3.3.3 - Manage Files ================

    /**
     * Replace an existing document
     */
    public ClientDocumentResponse replaceDocument(Long clientId, Long documentId, DocumentUploadRequest request) {
        log.info("Replacing document {} for client: {}", documentId, clientId);
        
        // Validate original document exists
        ClientDocument originalDocument = documentRepository.findByIdAndClientId(documentId, clientId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        
        // Set replacement parameters
        request.setReplaceExisting(true);
        request.setReplacingDocumentId(documentId);
        
        return uploadDocument(clientId, request);
    }

    /**
     * Delete a document
     */
    public Map<String, Object> deleteDocument(Long clientId, Long documentId) {
        log.info("Deleting document {} for client: {}", documentId, clientId);
        
        ClientDocument document = documentRepository.findByIdAndClientId(documentId, clientId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        
        // Check if document can be deleted (client-uploaded documents only)
        if (!Boolean.TRUE.equals(document.getUploadedByClient())) {
            throw new IllegalStateException("System documents cannot be deleted");
        }
        
        // Mark as inactive instead of hard delete
        document.setIsActive(false);
        document.setDocumentStatus(ClientDocument.DocumentStatus.ARCHIVED);
        documentRepository.save(document);
        
        Map<String, Object> response = new HashMap<>();
        response.put("documentId", documentId);
        response.put("documentName", document.getDocumentName());
        response.put("success", true);
        response.put("message", "Document deleted successfully");
        response.put("deletedAt", LocalDateTime.now());
        
        return response;
    }

    /**
     * Get document upload history for a client
     */
    public List<ClientDocumentResponse> getDocumentHistory(Long clientId, Integer days) {
        log.info("Getting document history for client: {} for {} days", clientId, days);
        
        validateClientExists(clientId);
        
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days != null ? days : 30);
        List<ClientDocument> recentDocuments = documentRepository.findRecentUploads(clientId, sinceDate);
        
        return recentDocuments.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    // ================ Helper Methods ================

    private void validateClientExists(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client not found with ID: " + clientId);
        }
    }

    private String saveUploadedFile(MultipartFile file, Long clientId) throws IOException {
        // Create client-specific directory
        Path uploadDir = Paths.get(uploadPath, "client_" + clientId);
        Files.createDirectories(uploadDir);
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : "";
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        Path filePath = uploadDir.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return filePath.toString();
    }

    private ClientDocumentResponse convertToResponse(ClientDocument document) {
        return ClientDocumentResponse.builder()
            .id(document.getId())
            .documentName(document.getDocumentName())
            .documentType(document.getDocumentType())
            .documentTypeDisplayName(document.getDocumentType().getDisplayName())
            .documentCategory(document.getDocumentCategory())
            .documentCategoryDisplayName(document.getDocumentCategory().getDisplayName())
            .fileSize(document.getFileSize())
            .fileSizeFormatted(ClientDocumentResponse.formatFileSize(document.getFileSize()))
            .mimeType(document.getMimeType())
            .uploadDate(document.getUploadDate())
            .uploadedByClient(document.getUploadedByClient())
            .isActive(document.getIsActive())
            .documentStatus(document.getDocumentStatus())
            .documentStatusDisplayName(document.getDocumentStatus().getDisplayName())
            .versionNumber(document.getVersionNumber())
            .description(document.getDescription())
            .expiryDate(document.getExpiryDate())
            .isExpired(document.isExpired())
            .accessCount(document.getAccessCount())
            .lastAccessedDate(document.getLastAccessedDate())
            .tags(document.getTags())
            .tagList(ClientDocumentResponse.parseTagsToList(document.getTags()))
            .createdAt(document.getCreatedAt())
            .updatedAt(document.getUpdatedAt())
            .downloadUrl("/api/client/documents/" + document.getId() + "/download")
            .canDownload(true)
            .canDelete(Boolean.TRUE.equals(document.getUploadedByClient()))
            .canReplace(Boolean.TRUE.equals(document.getUploadedByClient()))
            .securityInfo(ClientDocumentResponse.DocumentSecurityInfo.builder()
                .isEncrypted(false)
                .requiresAuthentication(true)
                .accessLevel("CLIENT")
                .isPublic(false)
                .build())
            .build();
    }

    private DocumentListResponse.DocumentStatistics getDocumentStatistics(Long clientId) {
        long totalDocuments = documentRepository.countByClientIdAndIsActiveTrue(clientId);
        Long totalFileSize = documentRepository.getTotalFileSizeForClient(clientId);
        
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<ClientDocument> recentUploads = documentRepository.findRecentUploads(clientId, thirtyDaysAgo);
        
        LocalDateTime sevenDaysFromNow = LocalDateTime.now().plusDays(7);
        List<ClientDocument> expiringSoon = documentRepository.findExpiringSoon(clientId, 
                                                                                LocalDateTime.now(), 
                                                                                sevenDaysFromNow);
        
        List<ClientDocument> expired = documentRepository.findExpired(clientId, LocalDateTime.now());
        
        List<ClientDocument> uploadedByClient = documentRepository
            .findByClientIdAndUploadedByClientTrueAndIsActiveTrue(clientId);
        
        List<ClientDocument> allDocuments = documentRepository.findByClientIdAndIsActiveTrue(clientId);
        OptionalDouble averageAccess = allDocuments.stream()
            .mapToInt(ClientDocument::getAccessCount)
            .average();
        
        LocalDateTime lastUpload = allDocuments.stream()
            .map(ClientDocument::getUploadDate)
            .filter(Objects::nonNull)
            .max(LocalDateTime::compareTo)
            .orElse(null);
        
        return DocumentListResponse.DocumentStatistics.builder()
            .totalDocuments(totalDocuments)
            .totalFileSize(totalFileSize)
            .totalFileSizeFormatted(ClientDocumentResponse.formatFileSize(totalFileSize))
            .recentUploads(recentUploads.size())
            .expiringSoon(expiringSoon.size())
            .expiredDocuments(expired.size())
            .uploadedByClient(uploadedByClient.size())
            .systemDocuments((int)(totalDocuments - uploadedByClient.size()))
            .lastUploadDate(lastUpload)
            .averageAccessCount(averageAccess.isPresent() ? (int)averageAccess.getAsDouble() : 0)
            .build();
    }

    private Map<String, Integer> getCategoryCount(Long clientId) {
        List<Object[]> results = documentRepository.countDocumentsByCategory(clientId);
        return results.stream().collect(Collectors.toMap(
            result -> ((ClientDocument.DocumentCategory) result[0]).getDisplayName(),
            result -> ((Long) result[1]).intValue()
        ));
    }

    private Map<String, Integer> getTypeCount(Long clientId) {
        List<Object[]> results = documentRepository.countDocumentsByType(clientId);
        return results.stream().collect(Collectors.toMap(
            result -> ((ClientDocument.DocumentType) result[0]).getDisplayName(),
            result -> ((Long) result[1]).intValue()
        ));
    }

    private Map<String, Integer> getStatusCount(Long clientId) {
        List<Object[]> results = documentRepository.countDocumentsByStatus(clientId);
        return results.stream().collect(Collectors.toMap(
            result -> ((ClientDocument.DocumentStatus) result[0]).getDisplayName(),
            result -> ((Long) result[1]).intValue()
        ));
    }

    @SuppressWarnings("unchecked")
    private Page<ClientDocument> convertListToPage(List<ClientDocument> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        List<ClientDocument> pageContent = start >= list.size() ? new ArrayList<>() : list.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, list.size());
    }
} 