package com.finance.admin.client.repository;

import com.finance.admin.client.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {

    // Find by unique identifiers
    Optional<Client> findByMembershipNumber(String membershipNumber);
    Optional<Client> findByEmailPrimary(String emailPrimary);
    Optional<Client> findByEmailPrimaryOrEmailSecondary(String emailPrimary, String emailSecondary);

    // Check for duplicates
    boolean existsByEmailPrimary(String emailPrimary);
    boolean existsByMembershipNumber(String membershipNumber);
    boolean existsByEmailPrimaryAndIdNot(String emailPrimary, Long id);
    boolean existsByMembershipNumberAndIdNot(String membershipNumber, Long id);

    // Search methods
    @Query("SELECT c FROM Client c WHERE " +
           "LOWER(CONCAT(c.firstName, ' ', COALESCE(c.middleName, ''), ' ', c.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.emailPrimary) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.membershipNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.phonePrimary) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Client> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Filter by status
    List<Client> findByStatus(Client.ClientStatus status);
    Page<Client> findByStatus(Client.ClientStatus status, Pageable pageable);

    // Filter by creation date range
    Page<Client> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Filter by location
    Page<Client> findByAddressStateAndAddressCountry(String state, String country, Pageable pageable);
    Page<Client> findByAddressCity(String city, Pageable pageable);

    // Complex search query with multiple filters
    @Query("SELECT c FROM Client c WHERE " +
           "(:firstName IS NULL OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
           "(:lastName IS NULL OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
           "(:email IS NULL OR LOWER(c.emailPrimary) LIKE LOWER(CONCAT('%', :email, '%')) OR LOWER(c.emailSecondary) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:city IS NULL OR LOWER(c.addressCity) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:state IS NULL OR LOWER(c.addressState) LIKE LOWER(CONCAT('%', :state, '%'))) AND " +
           "(:country IS NULL OR LOWER(c.addressCountry) LIKE LOWER(CONCAT('%', :country, '%'))) AND " +
           "(:riskProfile IS NULL OR c.riskProfile = :riskProfile) AND " +
           "(:hasBlockchainIdentity IS NULL OR (:hasBlockchainIdentity = true AND c.blockchainIdentityHash IS NOT NULL) OR (:hasBlockchainIdentity = false AND c.blockchainIdentityHash IS NULL))")
    Page<Client> findByMultipleFilters(
        @Param("firstName") String firstName,
        @Param("lastName") String lastName,
        @Param("email") String email,
        @Param("status") Client.ClientStatus status,
        @Param("city") String city,
        @Param("state") String state,
        @Param("country") String country,
        @Param("riskProfile") String riskProfile,
        @Param("hasBlockchainIdentity") Boolean hasBlockchainIdentity,
        Pageable pageable
    );

    // Statistics queries
    @Query("SELECT COUNT(c) FROM Client c WHERE c.status = :status")
    long countByStatus(@Param("status") Client.ClientStatus status);

    @Query("SELECT COUNT(c) FROM Client c WHERE c.createdAt >= :startDate")
    long countByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);

    // Blockchain related queries
    List<Client> findByBlockchainIdentityHashIsNull();
    List<Client> findByBlockchainIdentityHashIsNotNull();

    // Inactive clients
    @Query("SELECT c FROM Client c WHERE c.status = 'ACTIVE' AND " +
           "c.id NOT IN (SELECT DISTINCT clh.clientId FROM ClientLoginHistory clh WHERE clh.loginTimestamp >= :sinceDate)")
    List<Client> findInactiveClientsSince(@Param("sinceDate") LocalDateTime sinceDate);
} 