package com.finance.admin.investment.service;

import com.finance.admin.client.model.Client;
import com.finance.admin.client.repository.ClientRepository;
import com.finance.admin.investment.dto.CreateInvestmentRequest;
import com.finance.admin.investment.dto.InvestmentResponse;
import com.finance.admin.investment.dto.UpdateInvestmentRequest;
import com.finance.admin.investment.model.Investment;
import com.finance.admin.investment.repository.EntityRepository;
import com.finance.admin.investment.repository.InvestmentRepository;
import com.finance.admin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final ClientRepository clientRepository;
    private final EntityRepository entityRepository;
    private final UserRepository userRepository;

    /**
     * Create a new investment
     */
    public InvestmentResponse createInvestment(CreateInvestmentRequest request) {
        log.info("Creating investment: {} for client: {}", request.getInvestmentName(), request.getClientId());

        // Validate client exists
        Client client = clientRepository.findById(request.getClientId())
            .orElseThrow(() -> new RuntimeException("Client not found with ID: " + request.getClientId()));

        // Validate entity if provided
        com.finance.admin.investment.model.Entity entity = null;
        if (request.getEntityId() != null) {
            entity = entityRepository.findById(request.getEntityId())
                .orElseThrow(() -> new RuntimeException("Entity not found with ID: " + request.getEntityId()));
            
            // Ensure entity belongs to the client
            if (!entity.getClient().getId().equals(request.getClientId())) {
                throw new RuntimeException("Entity does not belong to the specified client");
            }
        }

        // Create investment entity
        Investment investment = Investment.builder()
            .client(client)
            .entity(entity)
            .investmentName(request.getInvestmentName())
            .investmentType(request.getInvestmentType())
            .investmentCategory(request.getInvestmentCategory())
            .description(request.getDescription())
            .investmentObjective(request.getInvestmentObjective())
            .riskRating(request.getRiskRating())
            .initialAmount(request.getInitialAmount())
            .currentValue(request.getCurrentValue() != null ? request.getCurrentValue() : request.getInitialAmount())
            .purchaseDate(request.getPurchaseDate())
            .maturityDate(request.getMaturityDate())
            .expectedReturnRate(request.getExpectedReturnRate())
            .expectedReturnAmount(request.getExpectedReturnAmount())
            .unitsPurchased(request.getUnitsPurchased())
            .purchasePricePerUnit(request.getPurchasePricePerUnit())
            .transactionFees(request.getTransactionFees())
            .managementFees(request.getManagementFees())
            .performanceFees(request.getPerformanceFees())
            .status(request.getStatus() != null ? request.getStatus() : Investment.InvestmentStatus.ACTIVE)
            .build();

        Investment savedInvestment = investmentRepository.save(investment);
        log.info("Investment created successfully with ID: {}", savedInvestment.getId());

        return mapToResponse(savedInvestment);
    }

    /**
     * Get investment by ID
     */
    @Transactional(readOnly = true)
    public InvestmentResponse getInvestmentById(Long id) {
        Investment investment = investmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Investment not found with ID: " + id));
        return mapToResponse(investment);
    }

    /**
     * Update investment
     */
    public InvestmentResponse updateInvestment(Long id, UpdateInvestmentRequest request) {
        log.info("Updating investment with ID: {}", id);

        Investment investment = investmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Investment not found with ID: " + id));

        // Update fields if provided
        if (request.getEntityId() != null) {
            com.finance.admin.investment.model.Entity entity = entityRepository.findById(request.getEntityId())
                .orElseThrow(() -> new RuntimeException("Entity not found with ID: " + request.getEntityId()));
            
            // Ensure entity belongs to the same client
            if (!entity.getClient().getId().equals(investment.getClient().getId())) {
                throw new RuntimeException("Entity does not belong to the investment's client");
            }
            investment.setEntity(entity);
        }

        if (request.getInvestmentName() != null) {
            investment.setInvestmentName(request.getInvestmentName());
        }
        if (request.getInvestmentType() != null) {
            investment.setInvestmentType(request.getInvestmentType());
        }
        if (request.getInvestmentCategory() != null) {
            investment.setInvestmentCategory(request.getInvestmentCategory());
        }
        if (request.getDescription() != null) {
            investment.setDescription(request.getDescription());
        }
        if (request.getInvestmentObjective() != null) {
            investment.setInvestmentObjective(request.getInvestmentObjective());
        }
        if (request.getRiskRating() != null) {
            investment.setRiskRating(request.getRiskRating());
        }
        if (request.getInitialAmount() != null) {
            investment.setInitialAmount(request.getInitialAmount());
        }
        if (request.getCurrentValue() != null) {
            investment.setCurrentValue(request.getCurrentValue());
        }
        if (request.getPurchaseDate() != null) {
            investment.setPurchaseDate(request.getPurchaseDate());
        }
        if (request.getMaturityDate() != null) {
            investment.setMaturityDate(request.getMaturityDate());
        }
        if (request.getExpectedReturnRate() != null) {
            investment.setExpectedReturnRate(request.getExpectedReturnRate());
        }
        if (request.getExpectedReturnAmount() != null) {
            investment.setExpectedReturnAmount(request.getExpectedReturnAmount());
        }
        if (request.getActualReturnAmount() != null) {
            investment.setActualReturnAmount(request.getActualReturnAmount());
        }
        if (request.getUnitsPurchased() != null) {
            investment.setUnitsPurchased(request.getUnitsPurchased());
        }
        if (request.getPurchasePricePerUnit() != null) {
            investment.setPurchasePricePerUnit(request.getPurchasePricePerUnit());
        }
        if (request.getCurrentPricePerUnit() != null) {
            investment.setCurrentPricePerUnit(request.getCurrentPricePerUnit());
        }
        if (request.getTransactionFees() != null) {
            investment.setTransactionFees(request.getTransactionFees());
        }
        if (request.getManagementFees() != null) {
            investment.setManagementFees(request.getManagementFees());
        }
        if (request.getPerformanceFees() != null) {
            investment.setPerformanceFees(request.getPerformanceFees());
        }
        if (request.getStatus() != null) {
            investment.setStatus(request.getStatus());
        }

        Investment updatedInvestment = investmentRepository.save(investment);
        log.info("Investment updated successfully with ID: {}", updatedInvestment.getId());

        return mapToResponse(updatedInvestment);
    }

    /**
     * Delete investment
     */
    public void deleteInvestment(Long id) {
        log.info("Deleting investment with ID: {}", id);

        Investment investment = investmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Investment not found with ID: " + id));

        investmentRepository.delete(investment);
        log.info("Investment deleted successfully with ID: {}", id);
    }

    /**
     * Get all investments with pagination
     */
    @Transactional(readOnly = true)
    public Page<InvestmentResponse> getAllInvestments(Pageable pageable) {
        return investmentRepository.findAll(pageable)
            .map(this::mapToResponse);
    }

    /**
     * Get investments by client ID
     */
    @Transactional(readOnly = true)
    public Page<InvestmentResponse> getInvestmentsByClientId(Long clientId, Pageable pageable) {
        // Validate client exists
        if (!clientRepository.existsById(clientId)) {
            throw new RuntimeException("Client not found with ID: " + clientId);
        }

        return investmentRepository.findByClientId(clientId, pageable)
            .map(this::mapToResponse);
    }

    /**
     * Search investments with filters
     */
    @Transactional(readOnly = true)
    public Page<InvestmentResponse> searchInvestments(
            Long clientId, Long entityId, Investment.InvestmentType investmentType,
            Investment.RiskRating riskRating, Investment.InvestmentStatus status,
            BigDecimal minAmount, BigDecimal maxAmount,
            LocalDate startDate, LocalDate endDate, String searchTerm,
            Pageable pageable) {

        return investmentRepository.findByMultipleFilters(
            clientId, entityId, investmentType, riskRating, status,
            minAmount, maxAmount, startDate, endDate, searchTerm, pageable
        ).map(this::mapToResponse);
    }

    /**
     * Get investment summary for client
     */
    @Transactional(readOnly = true)
    public InvestmentSummary getInvestmentSummary(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new RuntimeException("Client not found with ID: " + clientId);
        }

        List<Investment> investments = investmentRepository.findByClientId(clientId);
        
        BigDecimal totalInvestment = investments.stream()
            .map(Investment::getInitialAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentValue = investments.stream()
            .map(i -> i.getCurrentValue() != null ? i.getCurrentValue() : i.getInitialAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalFees = investments.stream()
            .map(Investment::getTotalFees)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long activeCount = investments.stream()
            .mapToLong(i -> i.isActive() ? 1 : 0)
            .sum();

        long maturedCount = investments.stream()
            .mapToLong(i -> i.isMatured() ? 1 : 0)
            .sum();

        return InvestmentSummary.builder()
            .totalInvestments(investments.size())
            .activeInvestments((int) activeCount)
            .maturedInvestments((int) maturedCount)
            .totalInvestmentAmount(totalInvestment)
            .currentTotalValue(currentValue)
            .totalFees(totalFees)
            .totalReturn(currentValue.subtract(totalInvestment))
            .build();
    }

    /**
     * Map Investment entity to response DTO
     */
    private InvestmentResponse mapToResponse(Investment investment) {
        return InvestmentResponse.builder()
            .id(investment.getId())
            .clientId(investment.getClient().getId())
            .clientName(investment.getClient().getFirstName() + " " + investment.getClient().getLastName())
            .entityId(investment.getEntity() != null ? investment.getEntity().getId() : null)
            .entityName(investment.getEntity() != null ? investment.getEntity().getEntityName() : null)
            .investmentName(investment.getInvestmentName())
            .investmentType(investment.getInvestmentType())
            .investmentTypeDisplayName(investment.getInvestmentType() != null ? investment.getInvestmentType().getDisplayName() : null)
            .investmentCategory(investment.getInvestmentCategory())
            .description(investment.getDescription())
            .investmentObjective(investment.getInvestmentObjective())
            .riskRating(investment.getRiskRating())
            .riskRatingDisplayName(investment.getRiskRating() != null ? investment.getRiskRating().getDisplayName() : null)
            .initialAmount(investment.getInitialAmount())
            .currentValue(investment.getCurrentValue())
            .purchaseDate(investment.getPurchaseDate())
            .maturityDate(investment.getMaturityDate())
            .expectedReturnRate(investment.getExpectedReturnRate())
            .expectedReturnAmount(investment.getExpectedReturnAmount())
            .actualReturnAmount(investment.getActualReturnAmount())
            .unitsPurchased(investment.getUnitsPurchased())
            .purchasePricePerUnit(investment.getPurchasePricePerUnit())
            .currentPricePerUnit(investment.getCurrentPricePerUnit())
            .transactionFees(investment.getTransactionFees())
            .managementFees(investment.getManagementFees())
            .performanceFees(investment.getPerformanceFees())
            .totalFees(investment.getTotalFees())
            .status(investment.getStatus())
            .statusDisplayName(investment.getStatus() != null ? investment.getStatus().getDescription() : null)
            .currentReturn(investment.getCurrentReturn())
            .currentReturnPercentage(investment.getCurrentReturnPercentage())
            .isMatured(investment.isMatured())
            .isActive(investment.isActive())
            .createdAt(investment.getCreatedAt())
            .updatedAt(investment.getUpdatedAt())
            .createdBy(investment.getCreatedBy())
            .updatedBy(investment.getUpdatedBy())
            .build();
    }

    /**
     * Investment Summary inner class
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class InvestmentSummary {
        private Integer totalInvestments;
        private Integer activeInvestments;
        private Integer maturedInvestments;
        private BigDecimal totalInvestmentAmount;
        private BigDecimal currentTotalValue;
        private BigDecimal totalFees;
        private BigDecimal totalReturn;
    }
} 