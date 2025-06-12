package com.finance.admin.client.document.controller;

import com.finance.admin.client.document.dto.*;
import com.finance.admin.client.document.model.ClientDocument;
import com.finance.admin.client.document.service.ClientDocumentService;
import com.finance.admin.client.document.util.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Legal Document Center functionality (Sprint 3.3)
 * Provides endpoints for document viewing, uploading, and management
 */
@RestController
@RequestMapping("/api/client/documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Client Document Management", description = "Legal Document Center APIs for Sprint 3.3")
@SecurityRequirement(name = "bearerAuth")
public class ClientDocumentController {

    private final ClientDocumentService documentService;
    private final JwtUtils jwtUtils;

    // ================ Sprint 3.3.1 - View Documents ================

    @GetMapping
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get all client documents", 
               description = "Retrieve all documents for the authenticated client with pagination and filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Documents retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<DocumentListResponse> getClientDocuments(
            HttpServletRequest request,
            @Parameter(description = "Document type filter") 
            @RequestParam(required = false) ClientDocument.DocumentType documentType,
            @Parameter(description = "Document category filter") 
            @RequestParam(required = false) ClientDocument.DocumentCategory documentCategory,
            @Parameter(description = "Document status filter") 
            @RequestParam(required = false) ClientDocument.DocumentStatus documentStatus,
            @Parameter(description = "Filter by client uploaded documents") 
            @RequestParam(required = false) Boolean uploadedByClient,
            @Parameter(description = "Search term for document name, description, or tags") 
            @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Sort field") 
            @RequestParam(defaultValue = "uploadDate") String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)") 
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Long clientId = jwtUtils.getClientIdFromRequest(request);
        log.info("Getting documents for client: {}", clientId);

        DocumentListResponse response = documentService.getClientDocuments(
            clientId, documentType, documentCategory, documentStatus, 
            uploadedByClient, searchTerm, page, size, sortBy, sortDirection
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{documentId}")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get document by ID", 
               description = "Retrieve a specific document by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<ClientDocumentResponse> getDocumentById(
            HttpServletRequest request,
            @Parameter(description = "Document ID") 
            @PathVariable Long documentId) {
        
        Long clientId = jwtUtils.getClientIdFromRequest(request);
        log.info("Getting document {} for client: {}", documentId, clientId);

        ClientDocumentResponse response = documentService.getDocumentById(clientId, documentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{documentId}/download")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Download document", 
               description = "Download a document file")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document downloaded successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "500", description = "Error reading file")
    })
    public ResponseEntity<byte[]> downloadDocument(
            HttpServletRequest request,
            @Parameter(description = "Document ID") 
            @PathVariable Long documentId) {
        
        Long clientId = jwtUtils.getClientIdFromRequest(request);
        log.info("Downloading document {} for client: {}", documentId, clientId);

        return documentService.downloadDocument(clientId, documentId);
    }

    @GetMapping("/categories")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get document categories", 
               description = "Retrieve available document types, categories, and statuses with counts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<Map<String, Object>> getDocumentCategories(HttpServletRequest request) {
        Long clientId = jwtUtils.getClientIdFromRequest(request);
        log.info("Getting document categories for client: {}", clientId);

        Map<String, Object> categories = documentService.getDocumentCategories(clientId);
        return ResponseEntity.ok(categories);
    }

    // ================ Sprint 3.3.2 - Upload Files ================

    @PostMapping("/upload")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Upload document", 
               description = "Upload a new document (KYC, bank info, etc.)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or request parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "413", description = "File too large"),
        @ApiResponse(responseCode = "415", description = "Unsupported file type"),
        @ApiResponse(responseCode = "500", description = "Error saving file")
    })
    public ResponseEntity<ClientDocumentResponse> uploadDocument(
            HttpServletRequest request,
            @Parameter(description = "Document file to upload") 
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "Document name") 
            @RequestPart("documentName") String documentName,
            @Parameter(description = "Document type") 
            @RequestPart("documentType") String documentType,
            @Parameter(description = "Document category") 
            @RequestPart("documentCategory") String documentCategory,
            @Parameter(description = "Document description") 
            @RequestPart(value = "description", required = false) String description,
            @Parameter(description = "Document expiry date (ISO format)") 
            @RequestPart(value = "expiryDate", required = false) String expiryDate,
            @Parameter(description = "Document tags (comma-separated)") 
            @RequestPart(value = "tags", required = false) String tags) {
        
        Long clientId = jwtUtils.getClientIdFromRequest(request);
        log.info("Uploading document for client: {}", clientId);

        DocumentUploadRequest uploadRequest = DocumentUploadRequest.builder()
            .file(file)
            .documentName(documentName)
            .documentType(ClientDocument.DocumentType.valueOf(documentType))
            .documentCategory(ClientDocument.DocumentCategory.valueOf(documentCategory))
            .description(description)
            .expiryDate(expiryDate != null ? LocalDateTime.parse(expiryDate) : null)
            .tags(tags)
            .replaceExisting(false)
            .build();

        ClientDocumentResponse response = documentService.uploadDocument(clientId, uploadRequest);
        return ResponseEntity.ok(response);
    }

    // ================ Sprint 3.3.3 - Manage Files ================

    @PutMapping("/{documentId}")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Replace document", 
               description = "Replace an existing document with a new file")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document replaced successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or request parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions or system document"),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "413", description = "File too large"),
        @ApiResponse(responseCode = "415", description = "Unsupported file type"),
        @ApiResponse(responseCode = "500", description = "Error saving file")
    })
    public ResponseEntity<ClientDocumentResponse> replaceDocument(
            HttpServletRequest request,
            @Parameter(description = "Document ID to replace") 
            @PathVariable Long documentId,
            @Parameter(description = "New document file") 
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "New document name") 
            @RequestPart("documentName") String documentName,
            @Parameter(description = "New document type") 
            @RequestPart("documentType") String documentType,
            @Parameter(description = "New document category") 
            @RequestPart("documentCategory") String documentCategory,
            @Parameter(description = "New document description") 
            @RequestPart(value = "description", required = false) String description,
            @Parameter(description = "New document expiry date (ISO format)") 
            @RequestPart(value = "expiryDate", required = false) String expiryDate,
            @Parameter(description = "New document tags (comma-separated)") 
            @RequestPart(value = "tags", required = false) String tags) {
        
        Long clientId = jwtUtils.getClientIdFromRequest(request);
        log.info("Replacing document {} for client: {}", documentId, clientId);

        DocumentUploadRequest uploadRequest = DocumentUploadRequest.builder()
            .file(file)
            .documentName(documentName)
            .documentType(ClientDocument.DocumentType.valueOf(documentType))
            .documentCategory(ClientDocument.DocumentCategory.valueOf(documentCategory))
            .description(description)
            .expiryDate(expiryDate != null ? LocalDateTime.parse(expiryDate) : null)
            .tags(tags)
            .build();

        ClientDocumentResponse response = documentService.replaceDocument(clientId, documentId, uploadRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Delete document", 
               description = "Delete a client-uploaded document (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions or system document"),
        @ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<Map<String, Object>> deleteDocument(
            HttpServletRequest request,
            @Parameter(description = "Document ID to delete") 
            @PathVariable Long documentId) {
        
        Long clientId = jwtUtils.getClientIdFromRequest(request);
        log.info("Deleting document {} for client: {}", documentId, clientId);

        Map<String, Object> response = documentService.deleteDocument(clientId, documentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get document upload history", 
               description = "Retrieve recent document upload history for the client")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document history retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<List<ClientDocumentResponse>> getDocumentHistory(
            HttpServletRequest request,
            @Parameter(description = "Number of days to look back (default: 30)") 
            @RequestParam(defaultValue = "30") Integer days) {
        
        Long clientId = jwtUtils.getClientIdFromRequest(request);
        log.info("Getting document history for client: {} for {} days", clientId, days);

        List<ClientDocumentResponse> response = documentService.getDocumentHistory(clientId, days);
        return ResponseEntity.ok(response);
    }

    // ================ Additional Utility Endpoints ================

    @GetMapping("/search")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Search documents", 
               description = "Search documents by name, description, or tags")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<DocumentListResponse> searchDocuments(
            HttpServletRequest request,
            @Parameter(description = "Search term") 
            @RequestParam String searchTerm,
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") Integer size) {
        
        Long clientId = jwtUtils.getClientIdFromRequest(request);
        log.info("Searching documents for client: {} with term: {}", clientId, searchTerm);

        DocumentListResponse response = documentService.getClientDocuments(
            clientId, null, null, null, null, searchTerm, page, size, "uploadDate", "DESC"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get document statistics", 
               description = "Get statistical information about client documents")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<Map<String, Object>> getDocumentStatistics(HttpServletRequest request) {
        Long clientId = jwtUtils.getClientIdFromRequest(request);
        log.info("Getting document statistics for client: {}", clientId);

        DocumentListResponse fullResponse = documentService.getClientDocuments(
            clientId, null, null, null, null, null, 0, 1, "uploadDate", "DESC"
        );

        Map<String, Object> statisticsOnly = Map.of(
            "statistics", fullResponse.getStatistics(),
            "categoryCount", fullResponse.getCategoryCount(),
            "typeCount", fullResponse.getTypeCount(),
            "statusCount", fullResponse.getStatusCount()
        );

        return ResponseEntity.ok(statisticsOnly);
    }
} 