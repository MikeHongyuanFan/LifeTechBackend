package com.finance.admin.client.document.repository;

import com.finance.admin.client.document.model.ClientDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ClientDocument entity
 * Provides data access methods for Legal Document Center functionality
 */
@Repository
public interface ClientDocumentRepository extends JpaRepository<ClientDocument, Long> {

    /**
     * Find all active documents for a specific client
     */
    List<ClientDocument> findByClientIdAndIsActiveTrue(Long clientId);

    /**
     * Find all documents for a specific client with pagination
     */
    Page<ClientDocument> findByClientId(Long clientId, Pageable pageable);

    /**
     * Find active documents for a specific client with pagination
     */
    Page<ClientDocument> findByClientIdAndIsActiveTrue(Long clientId, Pageable pageable);

    /**
     * Find documents by client and document type
     */
    List<ClientDocument> findByClientIdAndDocumentTypeAndIsActiveTrue(
            Long clientId, 
            ClientDocument.DocumentType documentType
    );

    /**
     * Find documents by client and document category
     */
    List<ClientDocument> findByClientIdAndDocumentCategoryAndIsActiveTrue(
            Long clientId, 
            ClientDocument.DocumentCategory documentCategory
    );

    /**
     * Find documents by client and document status
     */
    List<ClientDocument> findByClientIdAndDocumentStatusAndIsActiveTrue(
            Long clientId, 
            ClientDocument.DocumentStatus documentStatus
    );

    /**
     * Find a specific document for a client by ID
     */
    Optional<ClientDocument> findByIdAndClientId(Long documentId, Long clientId);

    /**
     * Find documents uploaded by client
     */
    List<ClientDocument> findByClientIdAndUploadedByClientTrueAndIsActiveTrue(Long clientId);

    /**
     * Find system documents (not uploaded by client)
     */
    List<ClientDocument> findByClientIdAndUploadedByClientFalseAndIsActiveTrue(Long clientId);

    /**
     * Search documents by name pattern
     */
    @Query("SELECT d FROM ClientDocument d WHERE d.client.id = :clientId " +
           "AND d.isActive = true " +
           "AND LOWER(d.documentName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<ClientDocument> searchByName(@Param("clientId") Long clientId, 
                                     @Param("searchTerm") String searchTerm);

    /**
     * Search documents by name, description, or tags
     */
    @Query("SELECT d FROM ClientDocument d WHERE d.client.id = :clientId " +
           "AND d.isActive = true " +
           "AND (LOWER(d.documentName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(d.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(d.tags) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<ClientDocument> searchDocuments(@Param("clientId") Long clientId, 
                                        @Param("searchTerm") String searchTerm);

    /**
     * Find documents expiring soon
     */
    @Query("SELECT d FROM ClientDocument d WHERE d.client.id = :clientId " +
           "AND d.isActive = true " +
           "AND d.expiryDate IS NOT NULL " +
           "AND d.expiryDate BETWEEN :now AND :futureDate")
    List<ClientDocument> findExpiringSoon(@Param("clientId") Long clientId,
                                         @Param("now") LocalDateTime now,
                                         @Param("futureDate") LocalDateTime futureDate);

    /**
     * Find expired documents
     */
    @Query("SELECT d FROM ClientDocument d WHERE d.client.id = :clientId " +
           "AND d.isActive = true " +
           "AND d.expiryDate IS NOT NULL " +
           "AND d.expiryDate < :now")
    List<ClientDocument> findExpired(@Param("clientId") Long clientId,
                                    @Param("now") LocalDateTime now);

    /**
     * Get document count by category for a client
     */
    @Query("SELECT d.documentCategory, COUNT(d) FROM ClientDocument d " +
           "WHERE d.client.id = :clientId AND d.isActive = true " +
           "GROUP BY d.documentCategory")
    List<Object[]> countDocumentsByCategory(@Param("clientId") Long clientId);

    /**
     * Get document count by type for a client
     */
    @Query("SELECT d.documentType, COUNT(d) FROM ClientDocument d " +
           "WHERE d.client.id = :clientId AND d.isActive = true " +
           "GROUP BY d.documentType")
    List<Object[]> countDocumentsByType(@Param("clientId") Long clientId);

    /**
     * Get document count by status for a client
     */
    @Query("SELECT d.documentStatus, COUNT(d) FROM ClientDocument d " +
           "WHERE d.client.id = :clientId AND d.isActive = true " +
           "GROUP BY d.documentStatus")
    List<Object[]> countDocumentsByStatus(@Param("clientId") Long clientId);

    /**
     * Find recently uploaded documents
     */
    @Query("SELECT d FROM ClientDocument d WHERE d.client.id = :clientId " +
           "AND d.isActive = true " +
           "AND d.uploadDate >= :sinceDate " +
           "ORDER BY d.uploadDate DESC")
    List<ClientDocument> findRecentUploads(@Param("clientId") Long clientId,
                                          @Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Find most accessed documents
     */
    @Query("SELECT d FROM ClientDocument d WHERE d.client.id = :clientId " +
           "AND d.isActive = true " +
           "ORDER BY d.accessCount DESC")
    List<ClientDocument> findMostAccessed(@Param("clientId") Long clientId, Pageable pageable);

    /**
     * Find documents by multiple criteria with dynamic query
     */
    @Query("SELECT d FROM ClientDocument d WHERE d.client.id = :clientId " +
           "AND d.isActive = true " +
           "AND (:documentType IS NULL OR d.documentType = :documentType) " +
           "AND (:documentCategory IS NULL OR d.documentCategory = :documentCategory) " +
           "AND (:documentStatus IS NULL OR d.documentStatus = :documentStatus) " +
           "AND (:uploadedByClient IS NULL OR d.uploadedByClient = :uploadedByClient)")
    Page<ClientDocument> findWithFilters(@Param("clientId") Long clientId,
                                        @Param("documentType") ClientDocument.DocumentType documentType,
                                        @Param("documentCategory") ClientDocument.DocumentCategory documentCategory,
                                        @Param("documentStatus") ClientDocument.DocumentStatus documentStatus,
                                        @Param("uploadedByClient") Boolean uploadedByClient,
                                        Pageable pageable);

    /**
     * Check if client has specific document type
     */
    boolean existsByClientIdAndDocumentTypeAndIsActiveTrue(Long clientId, 
                                                          ClientDocument.DocumentType documentType);

    /**
     * Count total active documents for client
     */
    long countByClientIdAndIsActiveTrue(Long clientId);

    /**
     * Get total file size for client documents
     */
    @Query("SELECT COALESCE(SUM(d.fileSize), 0) FROM ClientDocument d " +
           "WHERE d.client.id = :clientId AND d.isActive = true")
    Long getTotalFileSizeForClient(@Param("clientId") Long clientId);
} 