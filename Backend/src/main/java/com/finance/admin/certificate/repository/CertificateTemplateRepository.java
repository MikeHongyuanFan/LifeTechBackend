package com.finance.admin.certificate.repository;

import com.finance.admin.certificate.model.CertificateTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateTemplateRepository extends JpaRepository<CertificateTemplate, Long> {

    // Find by name
    Optional<CertificateTemplate> findByTemplateName(String templateName);
    boolean existsByTemplateName(String templateName);

    // Find by type
    List<CertificateTemplate> findByTemplateType(CertificateTemplate.TemplateType templateType);
    Page<CertificateTemplate> findByTemplateType(CertificateTemplate.TemplateType templateType, Pageable pageable);

    // Find active templates
    List<CertificateTemplate> findByIsActiveTrue();
    Page<CertificateTemplate> findByIsActiveTrue(Pageable pageable);

    // Find default templates
    List<CertificateTemplate> findByIsDefaultTrue();
    Optional<CertificateTemplate> findByTemplateTypeAndIsDefaultTrue(CertificateTemplate.TemplateType templateType);

    // Find by type and active status
    List<CertificateTemplate> findByTemplateTypeAndIsActiveTrue(CertificateTemplate.TemplateType templateType);

    // Search templates
    @Query("SELECT t FROM CertificateTemplate t WHERE " +
           "LOWER(t.templateName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.templateContent) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<CertificateTemplate> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Complex search with filters
    @Query("SELECT t FROM CertificateTemplate t WHERE " +
           "(:templateType IS NULL OR t.templateType = :templateType) AND " +
           "(:isActive IS NULL OR t.isActive = :isActive) AND " +
           "(:isDefault IS NULL OR t.isDefault = :isDefault) AND " +
           "(:searchTerm IS NULL OR " +
           " LOWER(t.templateName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(t.templateContent) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<CertificateTemplate> findByMultipleFilters(
        @Param("templateType") CertificateTemplate.TemplateType templateType,
        @Param("isActive") Boolean isActive,
        @Param("isDefault") Boolean isDefault,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );

    // Statistics queries
    @Query("SELECT t.templateType, COUNT(t) FROM CertificateTemplate t GROUP BY t.templateType")
    List<Object[]> getTemplateCountByType();

    @Query("SELECT COUNT(t) FROM CertificateTemplate t WHERE t.isActive = true")
    long countActiveTemplates();

    @Query("SELECT COUNT(t) FROM CertificateTemplate t WHERE t.isDefault = true")
    long countDefaultTemplates();

    // Recent templates
    @Query("SELECT t FROM CertificateTemplate t ORDER BY t.createdAt DESC")
    Page<CertificateTemplate> findRecentTemplates(Pageable pageable);

    // Templates by version
    @Query("SELECT t FROM CertificateTemplate t WHERE t.templateName = :templateName ORDER BY t.version DESC")
    List<CertificateTemplate> findVersionsByTemplateName(@Param("templateName") String templateName);

    @Query("SELECT t FROM CertificateTemplate t WHERE t.templateName = :templateName AND t.version = " +
           "(SELECT MAX(t2.version) FROM CertificateTemplate t2 WHERE t2.templateName = :templateName)")
    Optional<CertificateTemplate> findLatestVersionByTemplateName(@Param("templateName") String templateName);
} 