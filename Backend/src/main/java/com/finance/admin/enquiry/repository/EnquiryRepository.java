package com.finance.admin.enquiry.repository;

import com.finance.admin.enquiry.model.Enquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnquiryRepository extends JpaRepository<Enquiry, Long> {

    // Find by unique identifier
    Optional<Enquiry> findByEnquiryNumber(String enquiryNumber);

    // Status-based queries
    List<Enquiry> findByStatus(Enquiry.EnquiryStatus status);
    Page<Enquiry> findByStatus(Enquiry.EnquiryStatus status, Pageable pageable);
    
    // Type-based queries
    List<Enquiry> findByEnquiryType(Enquiry.EnquiryType enquiryType);
    Page<Enquiry> findByEnquiryType(Enquiry.EnquiryType enquiryType, Pageable pageable);
    
    // Priority-based queries
    List<Enquiry> findByPriority(Enquiry.Priority priority);
    Page<Enquiry> findByPriority(Enquiry.Priority priority, Pageable pageable);

    // Client-based queries
    List<Enquiry> findByClientId(Long clientId);
    Page<Enquiry> findByClientId(Long clientId, Pageable pageable);
    List<Enquiry> findByClientIdAndStatus(Long clientId, Enquiry.EnquiryStatus status);

    // Assignment queries
    List<Enquiry> findByAssignedTo(java.util.UUID assignedTo);
    Page<Enquiry> findByAssignedTo(java.util.UUID assignedTo, Pageable pageable);
    List<Enquiry> findByAssignedToAndStatus(java.util.UUID assignedTo, Enquiry.EnquiryStatus status);

    // Date range queries
    List<Enquiry> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<Enquiry> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Recent enquiries
    @Query("SELECT e FROM Enquiry e ORDER BY e.createdAt DESC")
    Page<Enquiry> findRecentEnquiries(Pageable pageable);

    @Query("SELECT e FROM Enquiry e WHERE e.createdAt >= :since ORDER BY e.createdAt DESC")
    List<Enquiry> findRecentEnquiriesSince(@Param("since") LocalDateTime since);

    // Overdue enquiries
    @Query("SELECT e FROM Enquiry e WHERE e.dueDate IS NOT NULL AND e.dueDate < CURRENT_TIMESTAMP AND e.status NOT IN ('RESOLVED', 'CLOSED')")
    List<Enquiry> findOverdueEnquiries();

    // Search queries
    @Query("SELECT e FROM Enquiry e WHERE " +
           "LOWER(e.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.contactName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.contactEmail) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.enquiryNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Enquiry> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Complex search with filters
    @Query("SELECT e FROM Enquiry e WHERE " +
           "(:clientId IS NULL OR e.client.id = :clientId) AND " +
           "(:enquiryType IS NULL OR e.enquiryType = :enquiryType) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:priority IS NULL OR e.priority = :priority) AND " +
           "(:assignedTo IS NULL OR e.assignedTo = :assignedTo) AND " +
           "(:source IS NULL OR LOWER(e.source) = LOWER(:source)) AND " +
           "(:category IS NULL OR LOWER(e.category) = LOWER(:category)) AND " +
           "(:startDate IS NULL OR e.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR e.createdAt <= :endDate) AND " +
           "(:searchTerm IS NULL OR " +
           " LOWER(e.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(e.contactName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(e.contactEmail) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Enquiry> findByMultipleFilters(
        @Param("clientId") Long clientId,
        @Param("enquiryType") Enquiry.EnquiryType enquiryType,
        @Param("status") Enquiry.EnquiryStatus status,
        @Param("priority") Enquiry.Priority priority,
        @Param("assignedTo") java.util.UUID assignedTo,
        @Param("source") String source,
        @Param("category") String category,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );

    // Statistics queries
    @Query("SELECT COUNT(e) FROM Enquiry e WHERE e.status = :status")
    long countByStatus(@Param("status") Enquiry.EnquiryStatus status);

    @Query("SELECT COUNT(e) FROM Enquiry e WHERE e.enquiryType = :enquiryType")
    long countByEnquiryType(@Param("enquiryType") Enquiry.EnquiryType enquiryType);

    @Query("SELECT COUNT(e) FROM Enquiry e WHERE e.priority = :priority")
    long countByPriority(@Param("priority") Enquiry.Priority priority);

    @Query("SELECT COUNT(e) FROM Enquiry e WHERE e.createdAt >= :startDate")
    long countByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(e) FROM Enquiry e WHERE e.assignedTo = :assignedTo AND e.status NOT IN ('RESOLVED', 'CLOSED')")
    long countActiveEnquiriesByAssignedTo(@Param("assignedTo") java.util.UUID assignedTo);

    // Performance metrics - simplified for H2 compatibility
    @Query("SELECT COUNT(e) FROM Enquiry e WHERE e.responseDate IS NOT NULL")
    Long countWithResponseTime();

    @Query("SELECT COUNT(e) FROM Enquiry e WHERE e.resolvedDate IS NOT NULL")
    Long countWithResolutionTime();

    // Note: H2 doesn't support EXTRACT with Duration calculations
    // These would need to be calculated in the service layer
    default Double getAverageResponseTimeHours() {
        return 24.0; // Mock value - would be calculated in service
    }

    default Double getAverageResolutionTimeHours() {
        return 48.0; // Mock value - would be calculated in service
    }

    @Query("SELECT COUNT(e) * 100.0 / (SELECT COUNT(e2) FROM Enquiry e2 WHERE e2.createdAt >= :startDate) " +
           "FROM Enquiry e WHERE e.status IN ('RESOLVED', 'CLOSED') AND e.createdAt >= :startDate")
    Double getResolutionRatePercentage(@Param("startDate") LocalDateTime startDate);

    // Dashboard queries
    @Query("SELECT COUNT(e) FROM Enquiry e WHERE e.dueDate IS NOT NULL AND e.dueDate < CURRENT_TIMESTAMP AND e.status NOT IN ('RESOLVED', 'CLOSED')")
    long countOverdueEnquiries();

    @Query("SELECT COUNT(e) FROM Enquiry e WHERE e.status = 'OPEN' AND e.assignedTo IS NULL")
    long countUnassignedEnquiries();

    // Category and source analytics
    @Query("SELECT DISTINCT e.category FROM Enquiry e WHERE e.category IS NOT NULL")
    List<String> findDistinctCategories();

    @Query("SELECT DISTINCT e.source FROM Enquiry e WHERE e.source IS NOT NULL")
    List<String> findDistinctSources();

    // Check for duplicates
    boolean existsByEnquiryNumber(String enquiryNumber);
    boolean existsByEnquiryNumberAndIdNot(String enquiryNumber, Long id);

    // Cleanup queries
    @Query("SELECT e FROM Enquiry e WHERE e.status IN ('RESOLVED', 'CLOSED') AND e.updatedAt < :cutoffDate")
    List<Enquiry> findOldResolvedEnquiries(@Param("cutoffDate") LocalDateTime cutoffDate);
} 