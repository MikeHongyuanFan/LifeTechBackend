package com.finance.admin.client.controller;

import com.finance.admin.client.dto.*;
import com.finance.admin.client.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/clients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Client Management", description = "APIs for managing client profiles")
@PreAuthorize("hasRole('ADMIN')")
public class ClientController {

    private final ClientService clientService;

    @Operation(summary = "Create new client", description = "Create a new client profile with comprehensive information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Client created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Client already exists")
    })
    @PostMapping
    public ResponseEntity<ClientResponse> createClient(
            @Valid @RequestBody CreateClientRequest request) {
        log.info("Creating new client with email: {}", request.getEmailPrimary());
        ClientResponse response = clientService.createClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all clients", description = "Retrieve all clients with pagination and sorting")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Clients retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    @GetMapping
    public ResponseEntity<Page<ClientResponse>> getAllClients(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        log.info("Getting all clients - page: {}, size: {}, sortBy: {}, sortDirection: {}", 
                page, size, sortBy, sortDirection);
        
        Page<ClientResponse> clients = clientService.getAllClients(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(clients);
    }

    @Operation(summary = "Get client by ID", description = "Retrieve a specific client by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Client found"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getClientById(
            @Parameter(description = "Client ID") @PathVariable Long id) {
        log.info("Getting client by ID: {}", id);
        ClientResponse client = clientService.getClientById(id);
        return ResponseEntity.ok(client);
    }

    @Operation(summary = "Update client", description = "Update client information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Client updated successfully"),
        @ApiResponse(responseCode = "404", description = "Client not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Duplicate data conflict")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> updateClient(
            @Parameter(description = "Client ID") @PathVariable Long id,
            @Valid @RequestBody UpdateClientRequest request) {
        log.info("Updating client with ID: {}", id);
        ClientResponse response = clientService.updateClient(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Deactivate client", description = "Deactivate a client (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Client deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateClient(
            @Parameter(description = "Client ID") @PathVariable Long id) {
        log.info("Deactivating client with ID: {}", id);
        clientService.deactivateClient(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search clients", description = "Search and filter clients with advanced criteria")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<ClientResponse>> searchClients(
            @Parameter(description = "Search term") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "First name filter") @RequestParam(required = false) String firstName,
            @Parameter(description = "Last name filter") @RequestParam(required = false) String lastName,
            @Parameter(description = "Email filter") @RequestParam(required = false) String email,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @Parameter(description = "City filter") @RequestParam(required = false) String city,
            @Parameter(description = "State filter") @RequestParam(required = false) String state,
            @Parameter(description = "Country filter") @RequestParam(required = false) String country,
            @Parameter(description = "Risk profile filter") @RequestParam(required = false) String riskProfile,
            @Parameter(description = "Has blockchain identity") @RequestParam(required = false) Boolean hasBlockchainIdentity,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.info("Searching clients with term: {}", searchTerm);
        
        ClientSearchRequest searchRequest = ClientSearchRequest.builder()
                .searchTerm(searchTerm)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .status(status != null ? com.finance.admin.client.model.Client.ClientStatus.valueOf(status) : null)
                .city(city)
                .state(state)
                .country(country)
                .riskProfile(riskProfile)
                .hasBlockchainIdentity(hasBlockchainIdentity)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        Page<ClientResponse> results = clientService.searchClients(searchRequest);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Bulk update clients", description = "Update multiple clients at once")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bulk update completed"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping("/bulk-update")
    public ResponseEntity<Map<String, Object>> bulkUpdateClients(
            @Valid @RequestBody BulkUpdateRequest request) {
        log.info("Bulk updating {} clients", request.getClientIds().size());
        // Implementation would go here
        Map<String, Object> result = Map.of(
            "message", "Bulk update feature will be implemented in future version",
            "affectedClients", 0
        );
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Anchor client identity on blockchain", description = "Store client identity hash on blockchain for verification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Identity anchored successfully"),
        @ApiResponse(responseCode = "404", description = "Client not found"),
        @ApiResponse(responseCode = "500", description = "Blockchain operation failed")
    })
    @PostMapping("/{id}/anchor-identity")
    public ResponseEntity<Map<String, Object>> anchorClientIdentity(
            @Parameter(description = "Client ID") @PathVariable Long id) {
        log.info("Anchoring identity for client: {}", id);
        
        try {
            clientService.anchorClientIdentity(id);
            Map<String, Object> response = Map.of(
                "message", "Client identity successfully anchored on blockchain",
                "clientId", id,
                "timestamp", java.time.LocalDateTime.now()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to anchor identity for client {}: {}", id, e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to anchor identity on blockchain",
                "message", e.getMessage(),
                "clientId", id
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Get client activity summary", description = "Retrieve dashboard statistics for client activity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Activity summary retrieved successfully")
    })
    @GetMapping("/activity/summary")
    public ResponseEntity<Map<String, Object>> getActivitySummary() {
        log.info("Getting client activity summary");
        Map<String, Object> summary = clientService.getClientActivitySummary();
        return ResponseEntity.ok(summary);
    }
}

// Supporting DTO class for bulk operations
class BulkUpdateRequest {
    private java.util.List<Long> clientIds;
    private UpdateClientRequest updateData;
    
    public java.util.List<Long> getClientIds() { return clientIds; }
    public void setClientIds(java.util.List<Long> clientIds) { this.clientIds = clientIds; }
    public UpdateClientRequest getUpdateData() { return updateData; }
    public void setUpdateData(UpdateClientRequest updateData) { this.updateData = updateData; }
} 