package com.finance.admin.investment.repository;

import com.finance.admin.investment.model.Entity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntityRepository extends JpaRepository<Entity, Long> {

    // Basic client entity queries
    List<Entity> findByClientId(Long clientId);
    Page<Entity> findByClientId(Long clientId, Pageable pageable);

    // Entity type queries
    List<Entity> findByEntityType(Entity.EntityType entityType);
    Page<Entity> findByEntityType(Entity.EntityType entityType, Pageable pageable);
    List<Entity> findByClientIdAndEntityType(Long clientId, Entity.EntityType entityType);

    // Status-based queries
    List<Entity> findByStatus(Entity.EntityStatus status);
    Page<Entity> findByStatus(Entity.EntityStatus status, Pageable pageable);
    List<Entity> findByClientIdAndStatus(Long clientId, Entity.EntityStatus status);

    // Search by entity name
    @Query("SELECT e FROM Entity e WHERE LOWER(e.entityName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Entity> findByEntityNameContainingIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Registration number queries
    Optional<Entity> findByRegistrationNumber(String registrationNumber);
    List<Entity> findByRegistrationNumberContaining(String registrationNumber);

    // ABN/ACN queries
    Optional<Entity> findByAbn(String abn);
    Optional<Entity> findByAcn(String acn);
    List<Entity> findByAbnContaining(String abn);
    List<Entity> findByAcnContaining(String acn);

    // Tax registration queries
    List<Entity> findByGstRegistered(Boolean gstRegistered);
    List<Entity> findByClientIdAndGstRegistered(Long clientId, Boolean gstRegistered);

    // Location-based queries
    List<Entity> findByRegisteredState(String state);
    List<Entity> findByRegisteredCity(String city);
    List<Entity> findByRegisteredCountry(String country);

    // Complex search query
    @Query("SELECT e FROM Entity e WHERE " +
           "(:clientId IS NULL OR e.client.id = :clientId) AND " +
           "(:entityType IS NULL OR e.entityType = :entityType) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:gstRegistered IS NULL OR e.gstRegistered = :gstRegistered) AND " +
           "(:state IS NULL OR LOWER(e.registeredState) = LOWER(:state)) AND " +
           "(:country IS NULL OR LOWER(e.registeredCountry) = LOWER(:country)) AND " +
           "(:searchTerm IS NULL OR " +
           " LOWER(e.entityName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(e.registrationNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(e.abn) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(e.acn) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Entity> findByMultipleFilters(
        @Param("clientId") Long clientId,
        @Param("entityType") Entity.EntityType entityType,
        @Param("status") Entity.EntityStatus status,
        @Param("gstRegistered") Boolean gstRegistered,
        @Param("state") String state,
        @Param("country") String country,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );

    // Count queries
    @Query("SELECT COUNT(e) FROM Entity e WHERE e.client.id = :clientId")
    Long countByClientId(@Param("clientId") Long clientId);

    @Query("SELECT COUNT(e) FROM Entity e WHERE e.entityType = :entityType")
    Long countByEntityType(@Param("entityType") Entity.EntityType entityType);

    @Query("SELECT COUNT(e) FROM Entity e WHERE e.status = :status")
    Long countByStatus(@Param("status") Entity.EntityStatus status);

    // Active entities
    @Query("SELECT e FROM Entity e WHERE e.status = 'ACTIVE'")
    List<Entity> findActiveEntities();

    @Query("SELECT e FROM Entity e WHERE e.client.id = :clientId AND e.status = 'ACTIVE'")
    List<Entity> findActiveEntitiesByClientId(@Param("clientId") Long clientId);

    // Tax compliance queries
    @Query("SELECT e FROM Entity e WHERE e.entityType IN ('COMPANY', 'TRUST', 'PARTNERSHIP', 'SMSF') AND (e.abn IS NULL OR e.abn = '')")
    List<Entity> findEntitiesRequiringAbnButMissing();

    @Query("SELECT e FROM Entity e WHERE e.entityType = 'COMPANY' AND (e.acn IS NULL OR e.acn = '')")
    List<Entity> findCompaniesRequiringAcnButMissing();

    // Check for duplicates
    @Query("SELECT COUNT(e) > 0 FROM Entity e WHERE e.abn = :abn AND e.id != :excludeId")
    boolean existsByAbnAndIdNot(@Param("abn") String abn, @Param("excludeId") Long excludeId);

    @Query("SELECT COUNT(e) > 0 FROM Entity e WHERE e.acn = :acn AND e.id != :excludeId")
    boolean existsByAcnAndIdNot(@Param("acn") String acn, @Param("excludeId") Long excludeId);

    @Query("SELECT COUNT(e) > 0 FROM Entity e WHERE e.registrationNumber = :registrationNumber AND e.id != :excludeId")
    boolean existsByRegistrationNumberAndIdNot(@Param("registrationNumber") String registrationNumber, @Param("excludeId") Long excludeId);

    // Check existence for creation
    boolean existsByAbn(String abn);
    boolean existsByAcn(String acn);
    boolean existsByRegistrationNumber(String registrationNumber);
} 