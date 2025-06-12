package com.finance.admin.investment.repository;

import com.finance.admin.investment.model.Investment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment, Long> {

    // Basic client investment queries
    List<Investment> findByClientId(Long clientId);
    Page<Investment> findByClientId(Long clientId, Pageable pageable);
    
    // Entity-based queries
    List<Investment> findByEntityId(Long entityId);
    Page<Investment> findByEntityId(Long entityId, Pageable pageable);

    // Status-based queries
    List<Investment> findByStatus(Investment.InvestmentStatus status);
    Page<Investment> findByStatus(Investment.InvestmentStatus status, Pageable pageable);
    List<Investment> findByClientIdAndStatus(Long clientId, Investment.InvestmentStatus status);

    // Investment type queries
    List<Investment> findByInvestmentType(Investment.InvestmentType investmentType);
    Page<Investment> findByInvestmentType(Investment.InvestmentType investmentType, Pageable pageable);
    List<Investment> findByClientIdAndInvestmentType(Long clientId, Investment.InvestmentType investmentType);

    // Risk rating queries
    List<Investment> findByRiskRating(Investment.RiskRating riskRating);
    List<Investment> findByClientIdAndRiskRating(Long clientId, Investment.RiskRating riskRating);

    // Date range queries
    List<Investment> findByPurchaseDateBetween(LocalDate startDate, LocalDate endDate);
    List<Investment> findByClientIdAndPurchaseDateBetween(Long clientId, LocalDate startDate, LocalDate endDate);
    
    // Maturity queries
    List<Investment> findByMaturityDateBefore(LocalDate date);
    List<Investment> findByMaturityDateBetween(LocalDate startDate, LocalDate endDate);

    // Investment amount queries
    List<Investment> findByInitialAmountGreaterThan(BigDecimal amount);
    List<Investment> findByInitialAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
    List<Investment> findByClientIdAndInitialAmountBetween(Long clientId, BigDecimal minAmount, BigDecimal maxAmount);

    // Complex search query
    @Query("SELECT i FROM Investment i WHERE " +
           "(:clientId IS NULL OR i.client.id = :clientId) AND " +
           "(:entityId IS NULL OR i.entity.id = :entityId) AND " +
           "(:investmentType IS NULL OR i.investmentType = :investmentType) AND " +
           "(:riskRating IS NULL OR i.riskRating = :riskRating) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:minAmount IS NULL OR i.initialAmount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR i.initialAmount <= :maxAmount) AND " +
           "(:startDate IS NULL OR i.purchaseDate >= :startDate) AND " +
           "(:endDate IS NULL OR i.purchaseDate <= :endDate) AND " +
           "(:searchTerm IS NULL OR " +
           " LOWER(i.investmentName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(i.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(i.investmentCategory) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Investment> findByMultipleFilters(
        @Param("clientId") Long clientId,
        @Param("entityId") Long entityId,
        @Param("investmentType") Investment.InvestmentType investmentType,
        @Param("riskRating") Investment.RiskRating riskRating,
        @Param("status") Investment.InvestmentStatus status,
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );

    // Search by investment name
    @Query("SELECT i FROM Investment i WHERE LOWER(i.investmentName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Investment> findByInvestmentNameContainingIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Aggregate queries
    @Query("SELECT SUM(i.initialAmount) FROM Investment i WHERE i.client.id = :clientId AND i.status = :status")
    Optional<BigDecimal> getTotalInvestmentByClientAndStatus(@Param("clientId") Long clientId, @Param("status") Investment.InvestmentStatus status);

    @Query("SELECT COUNT(i) FROM Investment i WHERE i.client.id = :clientId")
    Long countByClientId(@Param("clientId") Long clientId);

    @Query("SELECT COUNT(i) FROM Investment i WHERE i.status = :status")
    Long countByStatus(@Param("status") Investment.InvestmentStatus status);

    // Performance queries
    @Query("SELECT i FROM Investment i WHERE i.currentValue IS NOT NULL AND i.initialAmount IS NOT NULL AND " +
           "(i.currentValue - i.initialAmount) / i.initialAmount * 100 >= :minReturnPercentage")
    List<Investment> findByMinReturnPercentage(@Param("minReturnPercentage") BigDecimal minReturnPercentage);

    // Matured investments
    @Query("SELECT i FROM Investment i WHERE i.maturityDate IS NOT NULL AND i.maturityDate <= CURRENT_DATE AND i.status = 'ACTIVE'")
    List<Investment> findMaturedActiveInvestments();

    // Recent investments
    @Query("SELECT i FROM Investment i WHERE i.purchaseDate >= :fromDate ORDER BY i.purchaseDate DESC")
    List<Investment> findRecentInvestments(@Param("fromDate") LocalDate fromDate);

    // Check if client has investments
    boolean existsByClientId(Long clientId);
    
    // Check if entity has investments
    boolean existsByEntityId(Long entityId);

    // Dashboard aggregation queries
    @Query("SELECT SUM(i.initialAmount) FROM Investment i")
    Optional<BigDecimal> sumInvestmentAmount();

    @Query("SELECT COUNT(DISTINCT i.client.id) FROM Investment i")
    Long countDistinctClients();

    @Query("SELECT SUM(i.initialAmount) FROM Investment i WHERE i.purchaseDate BETWEEN :startDate AND :endDate")
    Optional<BigDecimal> sumInvestmentAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(i.currentValue) FROM Investment i WHERE i.currentValue IS NOT NULL")
    Optional<BigDecimal> sumCurrentValue();

    @Query("SELECT COUNT(DISTINCT i.client.id) FROM Investment i WHERE i.purchaseDate BETWEEN :startDate AND :endDate")
    Long countDistinctClientsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
} 