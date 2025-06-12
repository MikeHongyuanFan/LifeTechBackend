package com.finance.admin.investment.service;

import com.finance.admin.client.model.Client;
import com.finance.admin.client.repository.ClientRepository;
import com.finance.admin.investment.dto.CreateEntityRequest;
import com.finance.admin.investment.dto.EntityResponse;
import com.finance.admin.investment.model.Entity;
import com.finance.admin.investment.repository.EntityRepository;
import com.finance.admin.investment.repository.InvestmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EntityService {

    private final EntityRepository entityRepository;
    private final ClientRepository clientRepository;
    private final InvestmentRepository investmentRepository;

    // Simple encryption key (in production, use proper key management)
    private static final String ENCRYPTION_KEY = "MySecretKey12345"; // 16 chars for AES-128

    /**
     * Create a new entity
     */
    public EntityResponse createEntity(CreateEntityRequest request) {
        log.info("Creating entity: {} for client: {}", request.getEntityName(), request.getClientId());

        // Validate client exists
        Client client = clientRepository.findById(request.getClientId())
            .orElseThrow(() -> new RuntimeException("Client not found with ID: " + request.getClientId()));

        // Validate business rules
        validateBusinessRules(request, null);

        // Create entity
        Entity entity = Entity.builder()
            .client(client)
            .entityName(request.getEntityName())
            .entityType(request.getEntityType())
            .registrationNumber(request.getRegistrationNumber())
            .abn(request.getAbn())
            .acn(request.getAcn())
            .tfnEncrypted(encryptTfn(request.getTfn()))
            .registrationDate(request.getRegistrationDate())
            .registeredStreet(request.getRegisteredStreet())
            .registeredCity(request.getRegisteredCity())
            .registeredState(request.getRegisteredState())
            .registeredPostalCode(request.getRegisteredPostalCode())
            .registeredCountry(request.getRegisteredCountry())
            .contactPerson(request.getContactPerson())
            .contactPhone(request.getContactPhone())
            .contactEmail(request.getContactEmail())
            .taxResidencyStatus(request.getTaxResidencyStatus())
            .gstRegistered(request.getGstRegistered() != null ? request.getGstRegistered() : false)
            .status(request.getStatus() != null ? request.getStatus() : Entity.EntityStatus.ACTIVE)
            .build();

        Entity savedEntity = entityRepository.save(entity);
        log.info("Entity created successfully with ID: {}", savedEntity.getId());

        return mapToResponse(savedEntity);
    }

    /**
     * Get entity by ID
     */
    @Transactional(readOnly = true)
    public EntityResponse getEntityById(Long id) {
        Entity entity = entityRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Entity not found with ID: " + id));
        return mapToResponse(entity);
    }

    /**
     * Update entity
     */
    public EntityResponse updateEntity(Long id, CreateEntityRequest request) {
        log.info("Updating entity with ID: {}", id);

        Entity entity = entityRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Entity not found with ID: " + id));

        // Validate business rules (excluding current entity from uniqueness checks)
        validateBusinessRules(request, id);

        // Update fields
        if (StringUtils.hasText(request.getEntityName())) {
            entity.setEntityName(request.getEntityName());
        }
        if (request.getEntityType() != null) {
            entity.setEntityType(request.getEntityType());
        }
        if (request.getRegistrationNumber() != null) {
            entity.setRegistrationNumber(request.getRegistrationNumber());
        }
        if (request.getAbn() != null) {
            entity.setAbn(request.getAbn());
        }
        if (request.getAcn() != null) {
            entity.setAcn(request.getAcn());
        }
        if (request.getTfn() != null) {
            entity.setTfnEncrypted(encryptTfn(request.getTfn()));
        }
        if (request.getRegistrationDate() != null) {
            entity.setRegistrationDate(request.getRegistrationDate());
        }
        if (request.getRegisteredStreet() != null) {
            entity.setRegisteredStreet(request.getRegisteredStreet());
        }
        if (request.getRegisteredCity() != null) {
            entity.setRegisteredCity(request.getRegisteredCity());
        }
        if (request.getRegisteredState() != null) {
            entity.setRegisteredState(request.getRegisteredState());
        }
        if (request.getRegisteredPostalCode() != null) {
            entity.setRegisteredPostalCode(request.getRegisteredPostalCode());
        }
        if (request.getRegisteredCountry() != null) {
            entity.setRegisteredCountry(request.getRegisteredCountry());
        }
        if (request.getContactPerson() != null) {
            entity.setContactPerson(request.getContactPerson());
        }
        if (request.getContactPhone() != null) {
            entity.setContactPhone(request.getContactPhone());
        }
        if (request.getContactEmail() != null) {
            entity.setContactEmail(request.getContactEmail());
        }
        if (request.getTaxResidencyStatus() != null) {
            entity.setTaxResidencyStatus(request.getTaxResidencyStatus());
        }
        if (request.getGstRegistered() != null) {
            entity.setGstRegistered(request.getGstRegistered());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }

        Entity updatedEntity = entityRepository.save(entity);
        log.info("Entity updated successfully with ID: {}", updatedEntity.getId());

        return mapToResponse(updatedEntity);
    }

    /**
     * Delete entity
     */
    public void deleteEntity(Long id) {
        log.info("Deleting entity with ID: {}", id);

        Entity entity = entityRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Entity not found with ID: " + id));

        // Check if entity is used in investments
        if (investmentRepository.existsByEntityId(id)) {
            throw new RuntimeException("Cannot delete entity: it is referenced by existing investments");
        }

        entityRepository.delete(entity);
        log.info("Entity deleted successfully with ID: {}", id);
    }

    /**
     * Get all entities with pagination
     */
    @Transactional(readOnly = true)
    public Page<EntityResponse> getAllEntities(Pageable pageable) {
        return entityRepository.findAll(pageable)
            .map(this::mapToResponse);
    }

    /**
     * Get entities by client ID
     */
    @Transactional(readOnly = true)
    public Page<EntityResponse> getEntitiesByClientId(Long clientId, Pageable pageable) {
        // Validate client exists
        if (!clientRepository.existsById(clientId)) {
            throw new RuntimeException("Client not found with ID: " + clientId);
        }

        return entityRepository.findByClientId(clientId, pageable)
            .map(this::mapToResponse);
    }

    /**
     * Get active entities by client ID
     */
    @Transactional(readOnly = true)
    public List<EntityResponse> getActiveEntitiesByClientId(Long clientId) {
        // Validate client exists
        if (!clientRepository.existsById(clientId)) {
            throw new RuntimeException("Client not found with ID: " + clientId);
        }

        return entityRepository.findActiveEntitiesByClientId(clientId)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    /**
     * Search entities with filters
     */
    @Transactional(readOnly = true)
    public Page<EntityResponse> searchEntities(
            Long clientId, Entity.EntityType entityType, Entity.EntityStatus status,
            Boolean gstRegistered, String state, String country, String searchTerm,
            Pageable pageable) {

        return entityRepository.findByMultipleFilters(
            clientId, entityType, status, gstRegistered, state, country, searchTerm, pageable
        ).map(this::mapToResponse);
    }

    /**
     * Get entities requiring compliance action
     */
    @Transactional(readOnly = true)
    public List<EntityResponse> getEntitiesRequiringCompliance() {
        List<Entity> entitiesRequiringAbn = entityRepository.findEntitiesRequiringAbnButMissing();
        List<Entity> companiesRequiringAcn = entityRepository.findCompaniesRequiringAcnButMissing();

        List<Entity> allNonCompliant = new java.util.ArrayList<>(entitiesRequiringAbn);
        allNonCompliant.addAll(companiesRequiringAcn);

        return allNonCompliant.stream()
            .distinct()
            .map(this::mapToResponse)
            .toList();
    }

    /**
     * Validate business rules
     */
    private void validateBusinessRules(CreateEntityRequest request, Long excludeId) {
        // Check ABN uniqueness
        if (StringUtils.hasText(request.getAbn())) {
            boolean abnExists = excludeId != null ? 
                entityRepository.existsByAbnAndIdNot(request.getAbn(), excludeId) :
                entityRepository.existsByAbn(request.getAbn());
            
            if (abnExists) {
                throw new RuntimeException("ABN already exists: " + request.getAbn());
            }
        }

        // Check ACN uniqueness
        if (StringUtils.hasText(request.getAcn())) {
            boolean acnExists = excludeId != null ? 
                entityRepository.existsByAcnAndIdNot(request.getAcn(), excludeId) :
                entityRepository.existsByAcn(request.getAcn());
            
            if (acnExists) {
                throw new RuntimeException("ACN already exists: " + request.getAcn());
            }
        }

        // Check registration number uniqueness
        if (StringUtils.hasText(request.getRegistrationNumber())) {
            boolean regNumExists = excludeId != null ? 
                entityRepository.existsByRegistrationNumberAndIdNot(request.getRegistrationNumber(), excludeId) :
                entityRepository.existsByRegistrationNumber(request.getRegistrationNumber());
            
            if (regNumExists) {
                throw new RuntimeException("Registration number already exists: " + request.getRegistrationNumber());
            }
        }

        // Validate required fields based on entity type
        if (request.getEntityType() != null) {
            switch (request.getEntityType()) {
                case COMPANY:
                    if (!StringUtils.hasText(request.getAcn())) {
                        throw new RuntimeException("ACN is required for Company entities");
                    }
                    if (!StringUtils.hasText(request.getAbn())) {
                        throw new RuntimeException("ABN is required for Company entities");
                    }
                    break;
                case FAMILY_TRUST:
                case UNIT_TRUST:
                case DISCRETIONARY_TRUST:
                case SMSF:
                case PARTNERSHIP:
                    if (!StringUtils.hasText(request.getAbn())) {
                        throw new RuntimeException("ABN is required for " + request.getEntityType().getDisplayName() + " entities");
                    }
                    break;
            }
        }
    }

    /**
     * Simple TFN encryption (in production, use proper encryption service)
     */
    private String encryptTfn(String tfn) {
        if (!StringUtils.hasText(tfn)) {
            return null;
        }

        try {
            SecretKeySpec secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(tfn.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Error encrypting TFN", e);
            throw new RuntimeException("Failed to encrypt TFN");
        }
    }

    /**
     * Map Entity to response DTO
     */
    private EntityResponse mapToResponse(Entity entity) {
        return EntityResponse.builder()
            .id(entity.getId())
            .clientId(entity.getClient().getId())
            .clientName(entity.getClient().getFirstName() + " " + entity.getClient().getLastName())
            .entityName(entity.getEntityName())
            .entityType(entity.getEntityType())
            .entityTypeDisplayName(entity.getEntityType() != null ? entity.getEntityType().getDisplayName() : null)
            .registrationNumber(entity.getRegistrationNumber())
            .abn(entity.getAbn())
            .acn(entity.getAcn())
            .registrationDate(entity.getRegistrationDate())
            .registeredStreet(entity.getRegisteredStreet())
            .registeredCity(entity.getRegisteredCity())
            .registeredState(entity.getRegisteredState())
            .registeredPostalCode(entity.getRegisteredPostalCode())
            .registeredCountry(entity.getRegisteredCountry())
            .fullRegisteredAddress(entity.getFullRegisteredAddress())
            .contactPerson(entity.getContactPerson())
            .contactPhone(entity.getContactPhone())
            .contactEmail(entity.getContactEmail())
            .taxResidencyStatus(entity.getTaxResidencyStatus())
            .gstRegistered(entity.getGstRegistered())
            .status(entity.getStatus())
            .statusDisplayName(entity.getStatus() != null ? entity.getStatus().getDisplayName() : null)
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedBy(entity.getUpdatedBy())
            .requiresAbn(entity.requiresAbn())
            .requiresAcn(entity.requiresAcn())
            .isActive(entity.isActive())
            .build();
    }
} 