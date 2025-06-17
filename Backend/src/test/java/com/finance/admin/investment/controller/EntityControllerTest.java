package com.finance.admin.investment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.admin.investment.dto.CreateEntityRequest;
import com.finance.admin.investment.dto.EntityResponse;
import com.finance.admin.investment.model.Entity;
import com.finance.admin.investment.service.EntityService;
import com.finance.admin.config.BaseUnitTest;
import com.finance.admin.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for EntityController
 * Tests all entity management endpoints with various scenarios
 */
@DisplayName("Entity Controller Tests")
public class EntityControllerTest extends BaseUnitTest {

    private MockMvc mockMvc;
    
    @Mock
    private EntityService entityService;
    
    private EntityController entityController;
    
    // Test data
    private CreateEntityRequest mockCreateRequest;
    private EntityResponse mockEntityResponse;
    private final Long testEntityId = 1L;
    private final Long testClientId = 100L;

    @BeforeEach
    protected void setUp() {
        super.setUp();
        MockitoAnnotations.openMocks(this);
        
        entityController = new EntityController(entityService);
        
        // Setup MockMvc with proper configuration
        mockMvc = MockMvcBuilders.standaloneSetup(entityController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        // Setup test data
        setupTestData();
    }

    private void setupTestData() {
        // Setup mock create request
        mockCreateRequest = CreateEntityRequest.builder()
                .clientId(testClientId)
                .entityName("Smith Family Trust")
                .entityType(Entity.EntityType.FAMILY_TRUST)
                .registrationNumber("FT123456789")
                .abn("12345678901")
                .tfn("123456789")
                .registrationDate(LocalDate.now().minusYears(2))
                .registeredStreet("123 Collins Street")
                .registeredCity("Melbourne")
                .registeredState("VIC")
                .registeredPostalCode("3000")
                .registeredCountry("Australia")
                .contactPerson("John Smith")
                .contactPhone("+61423456789")
                .contactEmail("john@smithfamily.com.au")
                .taxResidencyStatus("Australian Resident")
                .gstRegistered(true)
                .status(Entity.EntityStatus.ACTIVE)
                .build();

        // Setup mock entity response
        mockEntityResponse = EntityResponse.builder()
                .id(testEntityId)
                .clientId(testClientId)
                .clientName("John Michael Smith")
                .entityName("Smith Family Trust")
                .entityType(Entity.EntityType.FAMILY_TRUST)
                .entityTypeDisplayName("Family Trust")
                .registrationNumber("FT123456789")
                .abn("12345678901")
                .registrationDate(LocalDate.now().minusYears(2))
                .registeredStreet("123 Collins Street")
                .registeredCity("Melbourne")
                .registeredState("VIC")
                .registeredPostalCode("3000")
                .registeredCountry("Australia")
                .fullRegisteredAddress("123 Collins Street, Melbourne, VIC 3000, Australia")
                .contactPerson("John Smith")
                .contactPhone("+61423456789")
                .contactEmail("john@smithfamily.com.au")
                .taxResidencyStatus("Australian Resident")
                .gstRegistered(true)
                .status(Entity.EntityStatus.ACTIVE)
                .statusDisplayName("Active")
                .requiresAbn(true)
                .requiresAcn(false)
                .isActive(true)
                .createdAt(LocalDateTime.now().minusYears(2))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // =========================== CREATE ENTITY TESTS ===========================
    
    @Test
    @DisplayName("Should create entity successfully")
    void testCreateEntity_Success() throws Exception {
        when(entityService.createEntity(any(CreateEntityRequest.class)))
                .thenReturn(mockEntityResponse);

        mockMvc.perform(post("/api/admin/entities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testEntityId))
                .andExpect(jsonPath("$.entityName").value("Smith Family Trust"))
                .andExpect(jsonPath("$.entityType").value("FAMILY_TRUST"))
                .andExpect(jsonPath("$.clientId").value(testClientId))
                .andExpect(jsonPath("$.abn").value("12345678901"))
                .andExpect(jsonPath("$.gstRegistered").value(true))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(entityService).createEntity(any(CreateEntityRequest.class));
    }

    @Test
    @DisplayName("Should handle validation errors for create entity")
    void testCreateEntity_ValidationError() throws Exception {
        CreateEntityRequest invalidRequest = CreateEntityRequest.builder()
                .entityType(Entity.EntityType.COMPANY)
                // Missing required fields: clientId, entityName
                .build();

        mockMvc.perform(post("/api/admin/entities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle invalid ABN format")
    void testCreateEntity_InvalidAbnFormat() throws Exception {
        CreateEntityRequest invalidRequest = CreateEntityRequest.builder()
                .clientId(testClientId)
                .entityName("Test Company")
                .entityType(Entity.EntityType.COMPANY)
                .abn("123456789") // Invalid ABN format (should be 11 digits)
                .build();

        mockMvc.perform(post("/api/admin/entities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle invalid ACN format")
    void testCreateEntity_InvalidAcnFormat() throws Exception {
        CreateEntityRequest invalidRequest = CreateEntityRequest.builder()
                .clientId(testClientId)
                .entityName("Test Company")
                .entityType(Entity.EntityType.COMPANY)
                .acn("12345678") // Invalid ACN format (should be 9 digits)
                .build();

        mockMvc.perform(post("/api/admin/entities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle service exception during entity creation")
    void testCreateEntity_ServiceException() throws Exception {
        when(entityService.createEntity(any(CreateEntityRequest.class)))
                .thenThrow(new RuntimeException("Client not found with ID: " + testClientId));

        mockMvc.perform(post("/api/admin/entities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockCreateRequest)))
                .andExpect(status().isInternalServerError());

        verify(entityService).createEntity(any(CreateEntityRequest.class));
    }

    @Test
    @DisplayName("Should handle duplicate ABN error")
    void testCreateEntity_DuplicateAbn() throws Exception {
        when(entityService.createEntity(any(CreateEntityRequest.class)))
                .thenThrow(new RuntimeException("ABN already exists: 12345678901"));

        mockMvc.perform(post("/api/admin/entities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockCreateRequest)))
                .andExpect(status().isInternalServerError());

        verify(entityService).createEntity(any(CreateEntityRequest.class));
    }

    @Test
    @DisplayName("Should handle invalid JSON request")
    void testCreateEntity_InvalidJson() throws Exception {
        mockMvc.perform(post("/api/admin/entities")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    // =========================== GET ENTITY TESTS ===========================
    
    @Test
    @DisplayName("Should get entity by ID successfully")
    void testGetEntity_Success() throws Exception {
        when(entityService.getEntityById(testEntityId))
                .thenReturn(mockEntityResponse);

        mockMvc.perform(get("/api/admin/entities/{id}", testEntityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEntityId))
                .andExpect(jsonPath("$.entityName").value("Smith Family Trust"))
                .andExpect(jsonPath("$.entityType").value("FAMILY_TRUST"))
                .andExpect(jsonPath("$.clientId").value(testClientId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(entityService).getEntityById(testEntityId);
    }

    @Test
    @DisplayName("Should handle entity not found by ID")
    void testGetEntity_NotFound() throws Exception {
        when(entityService.getEntityById(testEntityId))
                .thenThrow(new RuntimeException("Entity not found with ID: " + testEntityId));

        mockMvc.perform(get("/api/admin/entities/{id}", testEntityId))
                .andExpect(status().isInternalServerError());

        verify(entityService).getEntityById(testEntityId);
    }

    @Test
    @DisplayName("Should handle invalid entity ID format")
    void testGetEntity_InvalidIdFormat() throws Exception {
        mockMvc.perform(get("/api/admin/entities/{id}", "invalid"))
                .andExpect(status().isBadRequest());
    }

    // =========================== UPDATE ENTITY TESTS ===========================
    
    @Test
    @DisplayName("Should update entity successfully")
    void testUpdateEntity_Success() throws Exception {
        CreateEntityRequest updateRequest = CreateEntityRequest.builder()
                .clientId(testClientId)
                .entityName("Updated Smith Family Trust")
                .entityType(Entity.EntityType.FAMILY_TRUST)
                .registeredCity("Sydney")
                .registeredState("NSW")
                .contactPhone("+61423456790")
                .gstRegistered(false)
                .build();

        EntityResponse updatedResponse = EntityResponse.builder()
                .id(testEntityId)
                .clientId(testClientId)
                .clientName("John Michael Smith")
                .entityName("Updated Smith Family Trust")
                .entityType(Entity.EntityType.FAMILY_TRUST)
                .entityTypeDisplayName("Family Trust")
                .registrationNumber("FT123456789")
                .abn("12345678901")
                .registrationDate(LocalDate.now().minusYears(2))
                .registeredStreet("123 Collins Street")
                .registeredCity("Sydney")
                .registeredState("NSW")
                .registeredPostalCode("3000")
                .registeredCountry("Australia")
                .fullRegisteredAddress("123 Collins Street, Sydney, NSW 3000, Australia")
                .contactPerson("John Smith")
                .contactPhone("+61423456790")
                .contactEmail("john@smithfamily.com.au")
                .taxResidencyStatus("Australian Resident")
                .gstRegistered(false)
                .status(Entity.EntityStatus.ACTIVE)
                .statusDisplayName("Active")
                .requiresAbn(true)
                .requiresAcn(false)
                .isActive(true)
                .createdAt(LocalDateTime.now().minusYears(2))
                .updatedAt(LocalDateTime.now())
                .build();

        when(entityService.updateEntity(eq(testEntityId), any(CreateEntityRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/admin/entities/{id}", testEntityId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEntityId))
                .andExpect(jsonPath("$.entityName").value("Updated Smith Family Trust"))
                .andExpect(jsonPath("$.registeredCity").value("Sydney"))
                .andExpect(jsonPath("$.registeredState").value("NSW"))
                .andExpect(jsonPath("$.contactPhone").value("+61423456790"))
                .andExpect(jsonPath("$.gstRegistered").value(false));

        verify(entityService).updateEntity(eq(testEntityId), any(CreateEntityRequest.class));
    }

    @Test
    @DisplayName("Should handle update of non-existent entity")
    void testUpdateEntity_NotFound() throws Exception {
        when(entityService.updateEntity(eq(testEntityId), any(CreateEntityRequest.class)))
                .thenThrow(new RuntimeException("Entity not found with ID: " + testEntityId));

        mockMvc.perform(put("/api/admin/entities/{id}", testEntityId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockCreateRequest)))
                .andExpect(status().isInternalServerError());

        verify(entityService).updateEntity(eq(testEntityId), any(CreateEntityRequest.class));
    }

    // =========================== DELETE ENTITY TESTS ===========================
    
    @Test
    @DisplayName("Should delete entity successfully")
    void testDeleteEntity_Success() throws Exception {
        doNothing().when(entityService).deleteEntity(testEntityId);

        mockMvc.perform(delete("/api/admin/entities/{id}", testEntityId))
                .andExpect(status().isNoContent());

        verify(entityService).deleteEntity(testEntityId);
    }

    @Test
    @DisplayName("Should handle delete of non-existent entity")
    void testDeleteEntity_NotFound() throws Exception {
        doThrow(new RuntimeException("Entity not found with ID: " + testEntityId))
                .when(entityService).deleteEntity(testEntityId);

        mockMvc.perform(delete("/api/admin/entities/{id}", testEntityId))
                .andExpect(status().isInternalServerError());

        verify(entityService).deleteEntity(testEntityId);
    }

    @Test
    @DisplayName("Should handle delete of entity with investments")
    void testDeleteEntity_HasInvestments() throws Exception {
        doThrow(new RuntimeException("Cannot delete entity: it is referenced by existing investments"))
                .when(entityService).deleteEntity(testEntityId);

        mockMvc.perform(delete("/api/admin/entities/{id}", testEntityId))
                .andExpect(status().isInternalServerError());

        verify(entityService).deleteEntity(testEntityId);
    }

    // =========================== GET ALL ENTITIES TESTS ===========================
    
    @Test
    @DisplayName("Should get all entities with pagination")
    void testGetAllEntities_Success() throws Exception {
        List<EntityResponse> entities = Arrays.asList(mockEntityResponse);
        Page<EntityResponse> entitiesPage = new PageImpl<>(entities, PageRequest.of(0, 20), 1);

        when(entityService.getAllEntities(any(Pageable.class)))
                .thenReturn(entitiesPage);

        mockMvc.perform(get("/api/admin/entities")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "createdAt")
                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(testEntityId))
                .andExpect(jsonPath("$.content[0].entityName").value("Smith Family Trust"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.number").value(0));

        verify(entityService).getAllEntities(any(Pageable.class));
    }

    @Test
    @DisplayName("Should get all entities with default pagination")
    void testGetAllEntities_DefaultPagination() throws Exception {
        List<EntityResponse> entities = Arrays.asList(mockEntityResponse);
        Page<EntityResponse> entitiesPage = new PageImpl<>(entities, PageRequest.of(0, 20), 1);

        when(entityService.getAllEntities(any(Pageable.class)))
                .thenReturn(entitiesPage);

        mockMvc.perform(get("/api/admin/entities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.number").value(0));

        verify(entityService).getAllEntities(any(Pageable.class));
    }

    @Test
    @DisplayName("Should handle empty entity list")
    void testGetAllEntities_Empty() throws Exception {
        Page<EntityResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);

        when(entityService.getAllEntities(any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/admin/entities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        verify(entityService).getAllEntities(any(Pageable.class));
    }

    // =========================== GET ENTITIES BY CLIENT TESTS ===========================
    
    @Test
    @DisplayName("Should get entities by client ID successfully")
    void testGetEntitiesByClient_Success() throws Exception {
        List<EntityResponse> entities = Arrays.asList(mockEntityResponse);
        Page<EntityResponse> entitiesPage = new PageImpl<>(entities, PageRequest.of(0, 20), 1);

        when(entityService.getEntitiesByClientId(eq(testClientId), any(Pageable.class)))
                .thenReturn(entitiesPage);

        mockMvc.perform(get("/api/admin/entities/client/{clientId}", testClientId)
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].clientId").value(testClientId))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(entityService).getEntitiesByClientId(eq(testClientId), any(Pageable.class));
    }

    @Test
    @DisplayName("Should handle client not found for entities")
    void testGetEntitiesByClient_ClientNotFound() throws Exception {
        when(entityService.getEntitiesByClientId(eq(testClientId), any(Pageable.class)))
                .thenThrow(new RuntimeException("Client not found with ID: " + testClientId));

        mockMvc.perform(get("/api/admin/entities/client/{clientId}", testClientId))
                .andExpect(status().isInternalServerError());

        verify(entityService).getEntitiesByClientId(eq(testClientId), any(Pageable.class));
    }

    // =========================== GET ACTIVE ENTITIES BY CLIENT TESTS ===========================
    
    @Test
    @DisplayName("Should get active entities by client ID successfully")
    void testGetActiveEntitiesByClient_Success() throws Exception {
        List<EntityResponse> activeEntities = Arrays.asList(mockEntityResponse);

        when(entityService.getActiveEntitiesByClientId(testClientId))
                .thenReturn(activeEntities);

        mockMvc.perform(get("/api/admin/entities/client/{clientId}/active", testClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].clientId").value(testClientId))
                .andExpect(jsonPath("$[0].isActive").value(true))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(entityService).getActiveEntitiesByClientId(testClientId);
    }

    // =========================== SEARCH ENTITIES TESTS ===========================
    
    @Test
    @DisplayName("Should search entities with all filters")
    void testSearchEntities_WithAllFilters() throws Exception {
        List<EntityResponse> entities = Arrays.asList(mockEntityResponse);
        Page<EntityResponse> entitiesPage = new PageImpl<>(entities, PageRequest.of(0, 20), 1);

        when(entityService.searchEntities(
                eq(testClientId), 
                eq(Entity.EntityType.FAMILY_TRUST), 
                eq(Entity.EntityStatus.ACTIVE),
                eq(true), 
                eq("VIC"), 
                eq("Australia"), 
                eq("Smith"), 
                any(Pageable.class)
        )).thenReturn(entitiesPage);

        mockMvc.perform(get("/api/admin/entities/search")
                .param("clientId", testClientId.toString())
                .param("entityType", "FAMILY_TRUST")
                .param("status", "ACTIVE")
                .param("gstRegistered", "true")
                .param("state", "VIC")
                .param("country", "Australia")
                .param("searchTerm", "Smith")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].entityName").value("Smith Family Trust"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(entityService).searchEntities(
                eq(testClientId), 
                eq(Entity.EntityType.FAMILY_TRUST), 
                eq(Entity.EntityStatus.ACTIVE),
                eq(true), 
                eq("VIC"), 
                eq("Australia"), 
                eq("Smith"), 
                any(Pageable.class)
        );
    }

    @Test
    @DisplayName("Should search entities with minimal filters")
    void testSearchEntities_MinimalFilters() throws Exception {
        List<EntityResponse> entities = Arrays.asList(mockEntityResponse);
        Page<EntityResponse> entitiesPage = new PageImpl<>(entities, PageRequest.of(0, 20), 1);

        when(entityService.searchEntities(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), 
                eq("Smith"), any(Pageable.class)
        )).thenReturn(entitiesPage);

        mockMvc.perform(get("/api/admin/entities/search")
                .param("searchTerm", "Smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].entityName").value("Smith Family Trust"));

        verify(entityService).searchEntities(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), 
                eq("Smith"), any(Pageable.class)
        );
    }

    @Test
    @DisplayName("Should search entities with no results")
    void testSearchEntities_NoResults() throws Exception {
        Page<EntityResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);

        when(entityService.searchEntities(
                any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)
        )).thenReturn(emptyPage);

        mockMvc.perform(get("/api/admin/entities/search")
                .param("searchTerm", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(entityService).searchEntities(
                any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)
        );
    }

    // =========================== NON-COMPLIANT ENTITIES TESTS ===========================
    
    @Test
    @DisplayName("Should get non-compliant entities successfully")
    void testGetNonCompliantEntities_Success() throws Exception {
        EntityResponse nonCompliantEntity = EntityResponse.builder()
                .id(testEntityId)
                .clientId(testClientId)
                .clientName("John Michael Smith")
                .entityName("Smith Family Trust")
                .entityType(Entity.EntityType.FAMILY_TRUST)
                .entityTypeDisplayName("Family Trust")
                .registrationNumber("FT123456789")
                .abn(null)
                .registrationDate(LocalDate.now().minusYears(2))
                .registeredStreet("123 Collins Street")
                .registeredCity("Melbourne")
                .registeredState("VIC")
                .registeredPostalCode("3000")
                .registeredCountry("Australia")
                .fullRegisteredAddress("123 Collins Street, Melbourne, VIC 3000, Australia")
                .contactPerson("John Smith")
                .contactPhone("+61423456789")
                .contactEmail("john@smithfamily.com.au")
                .taxResidencyStatus("Australian Resident")
                .gstRegistered(true)
                .status(Entity.EntityStatus.ACTIVE)
                .statusDisplayName("Active")
                .requiresAbn(true)
                .requiresAcn(false)
                .isActive(true)
                .createdAt(LocalDateTime.now().minusYears(2))
                .updatedAt(LocalDateTime.now())
                .build();
        
        List<EntityResponse> nonCompliantEntities = Arrays.asList(nonCompliantEntity);

        when(entityService.getEntitiesRequiringCompliance())
                .thenReturn(nonCompliantEntities);

        mockMvc.perform(get("/api/admin/entities/compliance/non-compliant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].requiresAbn").value(true));

        verify(entityService).getEntitiesRequiringCompliance();
    }

    @Test
    @DisplayName("Should return empty list when all entities are compliant")
    void testGetNonCompliantEntities_AllCompliant() throws Exception {
        when(entityService.getEntitiesRequiringCompliance())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/entities/compliance/non-compliant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(entityService).getEntitiesRequiringCompliance();
    }

    // =========================== UTILITY ENDPOINTS TESTS ===========================
    
    @Test
    @DisplayName("Should get entity types successfully")
    void testGetEntityTypes_Success() throws Exception {
        mockMvc.perform(get("/api/admin/entities/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("INDIVIDUAL"))
                .andExpect(jsonPath("$[2]").value("COMPANY"))
                .andExpect(jsonPath("$[3]").value("FAMILY_TRUST"));
    }

    @Test
    @DisplayName("Should get entity statuses successfully")
    void testGetEntityStatuses_Success() throws Exception {
        mockMvc.perform(get("/api/admin/entities/statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("ACTIVE"))
                .andExpect(jsonPath("$[1]").value("INACTIVE"))
                .andExpect(jsonPath("$[2]").value("PENDING"))
                .andExpect(jsonPath("$[3]").value("SUSPENDED"));
    }

    // =========================== UPDATE STATUS TESTS ===========================
    
    @Test
    @DisplayName("Should update entity status successfully")
    void testUpdateEntityStatus_Success() throws Exception {
        EntityResponse updatedResponse = EntityResponse.builder()
                .id(testEntityId)
                .clientId(testClientId)
                .clientName("John Michael Smith")
                .entityName("Smith Family Trust")
                .entityType(Entity.EntityType.FAMILY_TRUST)
                .entityTypeDisplayName("Family Trust")
                .registrationNumber("FT123456789")
                .abn("12345678901")
                .registrationDate(LocalDate.now().minusYears(2))
                .registeredStreet("123 Collins Street")
                .registeredCity("Melbourne")
                .registeredState("VIC")
                .registeredPostalCode("3000")
                .registeredCountry("Australia")
                .fullRegisteredAddress("123 Collins Street, Melbourne, VIC 3000, Australia")
                .contactPerson("John Smith")
                .contactPhone("+61423456789")
                .contactEmail("john@smithfamily.com.au")
                .taxResidencyStatus("Australian Resident")
                .gstRegistered(true)
                .status(Entity.EntityStatus.INACTIVE)
                .statusDisplayName("Inactive")
                .requiresAbn(true)
                .requiresAcn(false)
                .isActive(false)
                .createdAt(LocalDateTime.now().minusYears(2))
                .updatedAt(LocalDateTime.now())
                .build();

        when(entityService.updateEntity(eq(testEntityId), any(CreateEntityRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/admin/entities/{id}/status", testEntityId)
                .param("status", "INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEntityId))
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.statusDisplayName").value("Inactive"))
                .andExpect(jsonPath("$.isActive").value(false));

        verify(entityService).updateEntity(eq(testEntityId), any(CreateEntityRequest.class));
    }

    @Test
    @DisplayName("Should handle invalid status value")
    void testUpdateEntityStatus_InvalidStatus() throws Exception {
        mockMvc.perform(put("/api/admin/entities/{id}/status", testEntityId)
                .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());
    }

    // =========================== UPDATE GST STATUS TESTS ===========================
    
    @Test
    @DisplayName("Should update GST status successfully")
    void testUpdateGstStatus_Success() throws Exception {
        EntityResponse updatedResponse = EntityResponse.builder()
                .id(testEntityId)
                .clientId(testClientId)
                .clientName("John Michael Smith")
                .entityName("Smith Family Trust")
                .entityType(Entity.EntityType.FAMILY_TRUST)
                .entityTypeDisplayName("Family Trust")
                .registrationNumber("FT123456789")
                .abn("12345678901")
                .registrationDate(LocalDate.now().minusYears(2))
                .registeredStreet("123 Collins Street")
                .registeredCity("Melbourne")
                .registeredState("VIC")
                .registeredPostalCode("3000")
                .registeredCountry("Australia")
                .fullRegisteredAddress("123 Collins Street, Melbourne, VIC 3000, Australia")
                .contactPerson("John Smith")
                .contactPhone("+61423456789")
                .contactEmail("john@smithfamily.com.au")
                .taxResidencyStatus("Australian Resident")
                .gstRegistered(false)
                .status(Entity.EntityStatus.ACTIVE)
                .statusDisplayName("Active")
                .requiresAbn(true)
                .requiresAcn(false)
                .isActive(true)
                .createdAt(LocalDateTime.now().minusYears(2))
                .updatedAt(LocalDateTime.now())
                .build();

        when(entityService.updateEntity(eq(testEntityId), any(CreateEntityRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/admin/entities/{id}/gst-status", testEntityId)
                .param("gstRegistered", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEntityId))
                .andExpect(jsonPath("$.gstRegistered").value(false));

        verify(entityService).updateEntity(eq(testEntityId), any(CreateEntityRequest.class));
    }

    @Test
    @DisplayName("Should handle missing GST status parameter")
    void testUpdateGstStatus_MissingParameter() throws Exception {
        // Missing required parameter should return 500 (Internal Server Error) as controller doesn't handle gracefully
        mockMvc.perform(put("/api/admin/entities/{id}/gst-status", testEntityId))
                .andExpect(status().isInternalServerError());
    }

    // =========================== VALIDATION ENDPOINTS TESTS ===========================
    
    @Test
    @DisplayName("Should check ABN availability successfully")
    void testCheckAbnAvailability_Success() throws Exception {
        mockMvc.perform(get("/api/admin/entities/validate/abn/{abn}", "12345678901"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("Should check ABN availability with exclude ID")
    void testCheckAbnAvailability_WithExcludeId() throws Exception {
        mockMvc.perform(get("/api/admin/entities/validate/abn/{abn}", "12345678901")
                .param("excludeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("Should check ACN availability successfully")
    void testCheckAcnAvailability_Success() throws Exception {
        mockMvc.perform(get("/api/admin/entities/validate/acn/{acn}", "123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("Should check ACN availability with exclude ID")
    void testCheckAcnAvailability_WithExcludeId() throws Exception {
        mockMvc.perform(get("/api/admin/entities/validate/acn/{acn}", "123456789")
                .param("excludeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }
} 