package com.finance.admin.client.wallet.repository;

import com.finance.admin.client.wallet.model.ClientDigitalCertificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientDigitalCertificateRepository extends JpaRepository<ClientDigitalCertificate, Long> {

    /**
     * Find all certificates for a specific client
     */
    Page<ClientDigitalCertificate> findByClientIdAndIsValidTrueOrderByIssueDateDesc(Long clientId, Pageable pageable);

    /**
     * Find all certificates for a client including invalid ones
     */
    Page<ClientDigitalCertificate> findByClientIdOrderByIssueDateDesc(Long clientId, Pageable pageable);

    /**
     * Find certificate by certificate number
     */
    Optional<ClientDigitalCertificate> findByCertificateNumberAndIsValidTrue(String certificateNumber);

    /**
     * Find certificates by company
     */
    List<ClientDigitalCertificate> findByClientIdAndCompanyNameContainingIgnoreCaseAndIsValidTrue(
            Long clientId, String companyName);

    /**
     * Find certificates by type
     */
    List<ClientDigitalCertificate> findByClientIdAndCertificateTypeAndIsValidTrue(
            Long clientId, ClientDigitalCertificate.CertificateType certificateType);

    /**
     * Find certificates by date range
     */
    @Query("SELECT cdc FROM ClientDigitalCertificate cdc WHERE cdc.client.id = :clientId " +
           "AND cdc.issueDate BETWEEN :startDate AND :endDate " +
           "AND cdc.isValid = true ORDER BY cdc.issueDate DESC")
    List<ClientDigitalCertificate> findByClientIdAndIssueDateBetween(
            @Param("clientId") Long clientId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Calculate total market value of certificates for a client
     */
    @Query("SELECT COALESCE(SUM(cdc.currentMarketValue), 0) FROM ClientDigitalCertificate cdc " +
           "WHERE cdc.client.id = :clientId AND cdc.isValid = true AND cdc.currentMarketValue IS NOT NULL")
    java.math.BigDecimal calculateTotalMarketValueByClientId(@Param("clientId") Long clientId);

    /**
     * Count certificates by client
     */
    long countByClientIdAndIsValidTrue(Long clientId);

    /**
     * Find certificates with blockchain hash
     */
    List<ClientDigitalCertificate> findByClientIdAndBlockchainHashIsNotNullAndIsValidTrue(Long clientId);

    /**
     * Find certificates that can be transferred (no restrictions)
     */
    @Query("SELECT cdc FROM ClientDigitalCertificate cdc WHERE cdc.client.id = :clientId " +
           "AND cdc.isValid = true " +
           "AND (cdc.transferRestrictions IS NULL OR cdc.transferRestrictions = '')")
    List<ClientDigitalCertificate> findTransferableCertificatesByClientId(@Param("clientId") Long clientId);

    /**
     * Search certificates with multiple criteria
     */
    @Query("SELECT cdc FROM ClientDigitalCertificate cdc WHERE cdc.client.id = :clientId " +
           "AND (:companyName IS NULL OR LOWER(cdc.companyName) LIKE LOWER(CONCAT('%', :companyName, '%'))) " +
           "AND (:certificateType IS NULL OR cdc.certificateType = :certificateType) " +
           "AND (:isValid IS NULL OR cdc.isValid = :isValid) " +
           "ORDER BY cdc.issueDate DESC")
    Page<ClientDigitalCertificate> searchCertificates(
            @Param("clientId") Long clientId,
            @Param("companyName") String companyName,
            @Param("certificateType") ClientDigitalCertificate.CertificateType certificateType,
            @Param("isValid") Boolean isValid,
            Pageable pageable);

    /**
     * Verify blockchain hash exists
     */
    boolean existsByBlockchainHashAndIsValidTrue(String blockchainHash);
} 