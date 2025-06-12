package com.finance.admin.investment.controller;

import com.finance.admin.investment.dto.CreateInvestmentRequest;
import com.finance.admin.investment.dto.InvestmentResponse;
import com.finance.admin.investment.dto.UpdateInvestmentRequest;
import com.finance.admin.investment.model.Investment;
import com.finance.admin.investment.service.InvestmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/investments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Investment Management", description = "APIs for managing client investments")
@PreAuthorize("hasRole('ADMIN')")
public class InvestmentController {

    private final InvestmentService investmentService;

    @PostMapping
    @Operation(summary = "Create a new investment", description = "Create a new investment record for a client")
    public ResponseEntity<InvestmentResponse> createInvestment(
            @Valid @RequestBody CreateInvestmentRequest request) {
        log.info("Creating investment: {} for client: {}", request.getInvestmentName(), request.getClientId());
        
        InvestmentResponse response = investmentService.createInvestment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get investment by ID", description = "Retrieve a specific investment by its ID")
    public ResponseEntity<InvestmentResponse> getInvestment(
            @Parameter(description = "Investment ID") @PathVariable Long id) {
        log.info("Retrieving investment with ID: {}", id);
        
        InvestmentResponse response = investmentService.getInvestmentById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update investment", description = "Update an existing investment record")
    public ResponseEntity<InvestmentResponse> updateInvestment(
            @Parameter(description = "Investment ID") @PathVariable Long id,
            @Valid @RequestBody UpdateInvestmentRequest request) {
        log.info("Updating investment with ID: {}", id);
        
        InvestmentResponse response = investmentService.updateInvestment(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete investment", description = "Delete an investment record")
    public ResponseEntity<Void> deleteInvestment(
            @Parameter(description = "Investment ID") @PathVariable Long id) {
        log.info("Deleting investment with ID: {}", id);
        
        investmentService.deleteInvestment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get all investments", description = "Retrieve all investments with pagination and sorting")
    public ResponseEntity<Page<InvestmentResponse>> getAllInvestments(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<InvestmentResponse> investments = investmentService.getAllInvestments(pageable);
        return ResponseEntity.ok(investments);
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get investments by client", description = "Retrieve all investments for a specific client")
    public ResponseEntity<Page<InvestmentResponse>> getInvestmentsByClient(
            @Parameter(description = "Client ID") @PathVariable Long clientId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<InvestmentResponse> investments = investmentService.getInvestmentsByClientId(clientId, pageable);
        return ResponseEntity.ok(investments);
    }

    @GetMapping("/client/{clientId}/summary")
    @Operation(summary = "Get investment summary", description = "Get investment summary statistics for a client")
    public ResponseEntity<InvestmentService.InvestmentSummary> getInvestmentSummary(
            @Parameter(description = "Client ID") @PathVariable Long clientId) {
        
        InvestmentService.InvestmentSummary summary = investmentService.getInvestmentSummary(clientId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/search")
    @Operation(summary = "Search investments", description = "Search investments with multiple filters")
    public ResponseEntity<Page<InvestmentResponse>> searchInvestments(
            @Parameter(description = "Client ID filter") @RequestParam(required = false) Long clientId,
            @Parameter(description = "Entity ID filter") @RequestParam(required = false) Long entityId,
            @Parameter(description = "Investment type filter") @RequestParam(required = false) Investment.InvestmentType investmentType,
            @Parameter(description = "Risk rating filter") @RequestParam(required = false) Investment.RiskRating riskRating,
            @Parameter(description = "Status filter") @RequestParam(required = false) Investment.InvestmentStatus status,
            @Parameter(description = "Minimum investment amount") @RequestParam(required = false) BigDecimal minAmount,
            @Parameter(description = "Maximum investment amount") @RequestParam(required = false) BigDecimal maxAmount,
            @Parameter(description = "Start date for purchase date range") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for purchase date range") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Search term for name/description") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<InvestmentResponse> investments = investmentService.searchInvestments(
            clientId, entityId, investmentType, riskRating, status,
            minAmount, maxAmount, startDate, endDate, searchTerm, pageable
        );
        
        return ResponseEntity.ok(investments);
    }

    @GetMapping("/types")
    @Operation(summary = "Get investment types", description = "Get all available investment types")
    public ResponseEntity<Investment.InvestmentType[]> getInvestmentTypes() {
        return ResponseEntity.ok(Investment.InvestmentType.values());
    }

    @GetMapping("/risk-ratings")
    @Operation(summary = "Get risk ratings", description = "Get all available risk ratings")
    public ResponseEntity<Investment.RiskRating[]> getRiskRatings() {
        return ResponseEntity.ok(Investment.RiskRating.values());
    }

    @GetMapping("/statuses")
    @Operation(summary = "Get investment statuses", description = "Get all available investment statuses")
    public ResponseEntity<Investment.InvestmentStatus[]> getInvestmentStatuses() {
        return ResponseEntity.ok(Investment.InvestmentStatus.values());
    }

    // Bulk operations
    @PutMapping("/{id}/status")
    @Operation(summary = "Update investment status", description = "Update the status of an investment")
    public ResponseEntity<InvestmentResponse> updateInvestmentStatus(
            @Parameter(description = "Investment ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam Investment.InvestmentStatus status) {
        
        UpdateInvestmentRequest request = UpdateInvestmentRequest.builder()
            .status(status)
            .build();
            
        InvestmentResponse response = investmentService.updateInvestment(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/current-value")
    @Operation(summary = "Update current value", description = "Update the current market value of an investment")
    public ResponseEntity<InvestmentResponse> updateCurrentValue(
            @Parameter(description = "Investment ID") @PathVariable Long id,
            @Parameter(description = "Current value") @RequestParam BigDecimal currentValue) {
        
        UpdateInvestmentRequest request = UpdateInvestmentRequest.builder()
            .currentValue(currentValue)
            .build();
            
        InvestmentResponse response = investmentService.updateInvestment(id, request);
        return ResponseEntity.ok(response);
    }
} 