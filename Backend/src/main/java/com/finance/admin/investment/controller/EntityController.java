package com.finance.admin.investment.controller;

import com.finance.admin.investment.dto.CreateEntityRequest;
import com.finance.admin.investment.dto.EntityResponse;
import com.finance.admin.investment.model.Entity;
import com.finance.admin.investment.service.EntityService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/entities")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Entity Management", description = "APIs for managing corporate entities and structures")
@PreAuthorize("hasRole('ADMIN')")
public class EntityController {

    private final EntityService entityService;

    @PostMapping
    @Operation(summary = "Create a new entity", description = "Create a new corporate entity for a client")
    public ResponseEntity<EntityResponse> createEntity(
            @Valid @RequestBody CreateEntityRequest request) {
        log.info("Creating entity: {} for client: {}", request.getEntityName(), request.getClientId());
        
        EntityResponse response = entityService.createEntity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get entity by ID", description = "Retrieve a specific entity by its ID")
    public ResponseEntity<EntityResponse> getEntity(
            @Parameter(description = "Entity ID") @PathVariable Long id) {
        log.info("Retrieving entity with ID: {}", id);
        
        EntityResponse response = entityService.getEntityById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update entity", description = "Update an existing entity record")
    public ResponseEntity<EntityResponse> updateEntity(
            @Parameter(description = "Entity ID") @PathVariable Long id,
            @Valid @RequestBody CreateEntityRequest request) {
        log.info("Updating entity with ID: {}", id);
        
        EntityResponse response = entityService.updateEntity(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete entity", description = "Delete an entity record")
    public ResponseEntity<Void> deleteEntity(
            @Parameter(description = "Entity ID") @PathVariable Long id) {
        log.info("Deleting entity with ID: {}", id);
        
        entityService.deleteEntity(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get all entities", description = "Retrieve all entities with pagination and sorting")
    public ResponseEntity<Page<EntityResponse>> getAllEntities(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<EntityResponse> entities = entityService.getAllEntities(pageable);
        return ResponseEntity.ok(entities);
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get entities by client", description = "Retrieve all entities for a specific client")
    public ResponseEntity<Page<EntityResponse>> getEntitiesByClient(
            @Parameter(description = "Client ID") @PathVariable Long clientId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<EntityResponse> entities = entityService.getEntitiesByClientId(clientId, pageable);
        return ResponseEntity.ok(entities);
    }

    @GetMapping("/client/{clientId}/active")
    @Operation(summary = "Get active entities by client", description = "Retrieve all active entities for a specific client")
    public ResponseEntity<List<EntityResponse>> getActiveEntitiesByClient(
            @Parameter(description = "Client ID") @PathVariable Long clientId) {
        
        List<EntityResponse> entities = entityService.getActiveEntitiesByClientId(clientId);
        return ResponseEntity.ok(entities);
    }

    @GetMapping("/search")
    @Operation(summary = "Search entities", description = "Search entities with multiple filters")
    public ResponseEntity<Page<EntityResponse>> searchEntities(
            @Parameter(description = "Client ID filter") @RequestParam(required = false) Long clientId,
            @Parameter(description = "Entity type filter") @RequestParam(required = false) Entity.EntityType entityType,
            @Parameter(description = "Status filter") @RequestParam(required = false) Entity.EntityStatus status,
            @Parameter(description = "GST registered filter") @RequestParam(required = false) Boolean gstRegistered,
            @Parameter(description = "State filter") @RequestParam(required = false) String state,
            @Parameter(description = "Country filter") @RequestParam(required = false) String country,
            @Parameter(description = "Search term for name/number") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<EntityResponse> entities = entityService.searchEntities(
            clientId, entityType, status, gstRegistered, state, country, searchTerm, pageable
        );
        
        return ResponseEntity.ok(entities);
    }

    @GetMapping("/compliance/non-compliant")
    @Operation(summary = "Get non-compliant entities", description = "Get entities that require compliance action (missing ABN/ACN)")
    public ResponseEntity<List<EntityResponse>> getNonCompliantEntities() {
        
        List<EntityResponse> entities = entityService.getEntitiesRequiringCompliance();
        return ResponseEntity.ok(entities);
    }

    @GetMapping("/types")
    @Operation(summary = "Get entity types", description = "Get all available entity types")
    public ResponseEntity<Entity.EntityType[]> getEntityTypes() {
        return ResponseEntity.ok(Entity.EntityType.values());
    }

    @GetMapping("/statuses")
    @Operation(summary = "Get entity statuses", description = "Get all available entity statuses")
    public ResponseEntity<Entity.EntityStatus[]> getEntityStatuses() {
        return ResponseEntity.ok(Entity.EntityStatus.values());
    }

    // Bulk operations
    @PutMapping("/{id}/status")
    @Operation(summary = "Update entity status", description = "Update the status of an entity")
    public ResponseEntity<EntityResponse> updateEntityStatus(
            @Parameter(description = "Entity ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam Entity.EntityStatus status) {
        
        CreateEntityRequest request = CreateEntityRequest.builder()
            .status(status)
            .build();
            
        EntityResponse response = entityService.updateEntity(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/gst-status")
    @Operation(summary = "Update GST registration status", description = "Update the GST registration status of an entity")
    public ResponseEntity<EntityResponse> updateGstStatus(
            @Parameter(description = "Entity ID") @PathVariable Long id,
            @Parameter(description = "GST registered status") @RequestParam Boolean gstRegistered) {
        
        CreateEntityRequest request = CreateEntityRequest.builder()
            .gstRegistered(gstRegistered)
            .build();
            
        EntityResponse response = entityService.updateEntity(id, request);
        return ResponseEntity.ok(response);
    }

    // Validation endpoints
    @GetMapping("/validate/abn/{abn}")
    @Operation(summary = "Check ABN availability", description = "Check if an ABN is already in use")
    public ResponseEntity<Boolean> checkAbnAvailability(
            @Parameter(description = "ABN to check") @PathVariable String abn,
            @Parameter(description = "Entity ID to exclude") @RequestParam(required = false) Long excludeId) {
        
        // This would call a validation service
        // For now, return simple response
        return ResponseEntity.ok(true);
    }

    @GetMapping("/validate/acn/{acn}")
    @Operation(summary = "Check ACN availability", description = "Check if an ACN is already in use")
    public ResponseEntity<Boolean> checkAcnAvailability(
            @Parameter(description = "ACN to check") @PathVariable String acn,
            @Parameter(description = "Entity ID to exclude") @RequestParam(required = false) Long excludeId) {
        
        // This would call a validation service
        // For now, return simple response
        return ResponseEntity.ok(true);
    }
} 