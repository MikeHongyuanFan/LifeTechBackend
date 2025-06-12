package com.finance.admin.certificate.repository;

import com.finance.admin.certificate.model.Certificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    // Find by unique identifier
    Optional<Certificate> findByCertificateNumber(String certificateNumber);
    boolean existsByCertificateNumber(String certificateNumber);

    // Client-based queries
    List<Certificate> findByClientId(Long clientId);
    Page<Certificate> findByClientId(Long clientId, Pageable pageable);
    List<Certificate> findByClientIdAndStatus(Long clientId, Certificate.CertificateStatus status);

    // Investment-based queries
    List<Certificate> findByInvestmentId(Long investmentId);
    Optional<Certificate> findByInvestmentIdAndStatus(Long investmentId, Certificate.CertificateStatus status);

    // Status-based queries
    List<Certificate> findByStatus(Certificate.CertificateStatus status);
    Page<Certificate> findByStatus(Certificate.CertificateStatus status, Pageable pageable);
    long countByStatus(Certificate.CertificateStatus status);

    // Type-based queries
    List<Certificate> findByCertificateType(Certificate.CertificateType certificateType);
    Page<Certificate> findByCertificateType(Certificate.CertificateType certificateType, Pageable pageable);

    // Date range queries
    List<Certificate> findByIssueDateBetween(LocalDate startDate, LocalDate endDate);
    Page<Certificate> findByIssueDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<Certificate> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Expiry queries
    @Query("SELECT c FROM Certificate c WHERE c.expiryDate IS NOT NULL AND c.expiryDate <= :date AND c.status = 'ACTIVE'")
    List<Certificate> findExpiringCertificates(@Param("date") LocalDate date);

    @Query("SELECT c FROM Certificate c WHERE c.expiryDate IS NOT NULL AND c.expiryDate BETWEEN :startDate AND :endDate AND c.status = 'ACTIVE'")
    List<Certificate> findCertificatesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Template-based queries
    List<Certificate> findByTemplateId(Long templateId);
    long countByTemplateId(Long templateId);

    // Search queries
    @Query("SELECT c FROM Certificate c WHERE " +
           "LOWER(c.certificateNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.client.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.client.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.investment.investmentName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Certificate> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Complex search with filters
    @Query("SELECT c FROM Certificate c WHERE " +
           "(:clientId IS NULL OR c.client.id = :clientId) AND " +
           "(:investmentId IS NULL OR c.investment.id = :investmentId) AND " +
           "(:certificateType IS NULL OR c.certificateType = :certificateType) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:startDate IS NULL OR c.issueDate >= :startDate) AND " +
           "(:endDate IS NULL OR c.issueDate <= :endDate) AND " +
           "(:searchTerm IS NULL OR " +
           " LOWER(c.certificateNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(c.client.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(c.client.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Certificate> findByMultipleFilters(
        @Param("clientId") Long clientId,
        @Param("investmentId") Long investmentId,
        @Param("certificateType") Certificate.CertificateType certificateType,
        @Param("status") Certificate.CertificateStatus status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );

    // Statistics queries
    @Query("SELECT COUNT(c) FROM Certificate c WHERE c.createdAt >= :since")
    long countCertificatesCreatedSince(@Param("since") LocalDateTime since);

    @Query("SELECT c.certificateType, COUNT(c) FROM Certificate c GROUP BY c.certificateType")
    List<Object[]> getCertificateCountByType();

    @Query("SELECT c.status, COUNT(c) FROM Certificate c GROUP BY c.status")
    List<Object[]> getCertificateCountByStatus();

    // Recent certificates
    @Query("SELECT c FROM Certificate c ORDER BY c.createdAt DESC")
    Page<Certificate> findRecentCertificates(Pageable pageable);

    // Active certificates for client
    @Query("SELECT c FROM Certificate c WHERE c.client.id = :clientId AND c.status = 'ACTIVE' ORDER BY c.issueDate DESC")
    List<Certificate> findActiveCertificatesByClientId(@Param("clientId") Long clientId);

    // Certificates without files (for batch processing)
    @Query("SELECT c FROM Certificate c WHERE c.filePath IS NULL OR c.filePath = ''")
    List<Certificate> findCertificatesWithoutFiles();

    // Certificates by investment type
    @Query("SELECT c FROM Certificate c WHERE c.investment.investmentType = :investmentType")
    List<Certificate> findByInvestmentType(@Param("investmentType") String investmentType);

    // Additional expiry monitoring queries
    List<Certificate> findByExpiryDateAndStatus(LocalDate expiryDate, Certificate.CertificateStatus status);
    List<Certificate> findByExpiryDateBeforeAndStatus(LocalDate expiryDate, Certificate.CertificateStatus status);
    long countByExpiryDateBeforeAndStatus(LocalDate expiryDate, Certificate.CertificateStatus status);
    long countByExpiryDateBetweenAndStatus(LocalDate startDate, LocalDate endDate, Certificate.CertificateStatus status);
} 