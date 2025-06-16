package com.finance.admin.client.controller;

import com.finance.admin.client.dto.*;
import com.finance.admin.client.model.Client;
import com.finance.admin.client.service.ClientService;
import com.finance.admin.config.BaseUnitTest;
import com.finance.admin.common.exception.ResourceNotFoundException;
import com.finance.admin.common.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the ClientController endpoints
 * Tests all CRUD operations and search functionality for client management
 * Using minimal standalone MockMvc configuration
 */
@DisplayName("Client Controller Tests")
public class ClientControllerTest extends BaseUnitTest {

    private MockMvc mockMvc;
    private ClientService clientService;
    private ClientController clientController;
    
    // Test data
    private ClientResponse testClientResponse;
    private CreateClientRequest createClientRequest;
    private UpdateClientRequest updateClientRequest;
    private final Long testClientId = 1L;

    @BeforeEach
    protected void setUp() {
        super.setUp(); // Call parent setup
        
        // Create mocks
        clientService = mock(ClientService.class);
        clientController = new ClientController(clientService);
        
        // Setup MockMvc with PageableHandlerMethodArgumentResolver
        mockMvc = MockMvcBuilders.standaloneSetup(clientController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        
        // Initialize test data
        setupTestData();
    }

    private void setupTestData() {
        // Setup test client response
        testClientResponse = ClientResponse.builder()
                .id(testClientId)
                .membershipNumber("MEM001")
                .firstName("John")
                .middleName("Alexander")
                .lastName("Doe")
                .fullName("John Alexander Doe")
                .dateOfBirth(LocalDate.of(1985, 5, 15))
                .emailPrimary("john.doe@example.com")
                .emailSecondary("john.doe.alt@example.com")
                .phonePrimary("+61412345678")
                .phoneSecondary("+61498765432")
                .addressStreet("123 Test Street")
                .addressCity("Sydney")
                .addressState("NSW")
                .addressPostalCode("2000")
                .addressCountry("Australia")
                .mailingAddressSame(true)
                .taxResidencyStatus("Australian Resident")
                .bankBsb("123456")
                .bankAccountName("John Alexander Doe")
                .investmentTarget(new BigDecimal("100000.00"))
                .riskProfile("MODERATE")
                .blockchainIdentityHash("0x1234567890abcdef")
                .status(Client.ClientStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(30))
                .updatedAt(LocalDateTime.now())
                .build();

        // Setup create client request
        createClientRequest = CreateClientRequest.builder()
                .firstName("Jane")
                .middleName("Elizabeth")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1990, 8, 20))
                .emailPrimary("jane.smith@example.com")
                .emailSecondary("jane.smith.alt@example.com")
                .phonePrimary("+61423456789")
                .phoneSecondary("+61487654321")
                .addressStreet("456 Example Avenue")
                .addressCity("Melbourne")
                .addressState("VIC")
                .addressPostalCode("3000")
                .addressCountry("Australia")
                .mailingAddressSame(true)
                .taxResidencyStatus("Australian Resident")
                .bankBsb("654321")
                .bankAccountName("Jane Elizabeth Smith")
                .investmentTarget(new BigDecimal("150000.00"))
                .riskProfile("AGGRESSIVE")
                .build();

        // Setup update client request
        updateClientRequest = UpdateClientRequest.builder()
                .firstName("John")
                .middleName("Alexander")
                .lastName("Doe")
                .emailPrimary("john.doe.updated@example.com")
                .phonePrimary("+61412345679")
                .addressStreet("123 Updated Street")
                .addressCity("Brisbane")
                .addressState("QLD")
                .addressPostalCode("4000")
                .investmentTarget(new BigDecimal("200000.00"))
                .riskProfile("CONSERVATIVE")
                .build();
    }

    // ================ GET /api/admin/clients - Get All Clients Tests ================

    @Test
    @DisplayName("Should get all clients with default pagination")
    void testGetAllClients_DefaultPagination_Success() throws Exception {
        // Given
        List<ClientResponse> clients = List.of(testClientResponse);
        Page<ClientResponse> page = new PageImpl<>(clients, PageRequest.of(0, 20), 1);
        
        when(clientService.getAllClients(0, 20, "createdAt", "DESC")).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/admin/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(testClientId))
                .andExpect(jsonPath("$.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.content[0].lastName").value("Doe"))
                .andExpect(jsonPath("$.content[0].emailPrimary").value("john.doe@example.com"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.number").value(0));

        verify(clientService).getAllClients(0, 20, "createdAt", "DESC");
    }

    @Test
    @DisplayName("Should get all clients with custom pagination parameters")
    void testGetAllClients_CustomPagination_Success() throws Exception {
        // Given
        List<ClientResponse> clients = List.of(testClientResponse);
        Page<ClientResponse> page = new PageImpl<>(clients, PageRequest.of(1, 10), 25);
        
        when(clientService.getAllClients(1, 10, "firstName", "ASC")).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/admin/clients")
                .param("page", "1")
                .param("size", "10")
                .param("sortBy", "firstName")
                .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(testClientId))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(1));

        verify(clientService).getAllClients(1, 10, "firstName", "ASC");
    }

    @Test
    @DisplayName("Should handle empty client list")
    void testGetAllClients_EmptyList_Success() throws Exception {
        // Given
        Page<ClientResponse> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 20), 0);
        
        when(clientService.getAllClients(0, 20, "createdAt", "DESC")).thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/api/admin/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        verify(clientService).getAllClients(0, 20, "createdAt", "DESC");
    }

    // ================ GET /api/admin/clients/{id} - Get Client By ID Tests ================

    @Test
    @DisplayName("Should get client by ID successfully")
    void testGetClientById_ValidId_Success() throws Exception {
        // Given
        when(clientService.getClientById(testClientId)).thenReturn(testClientResponse);

        // When & Then
        mockMvc.perform(get("/api/admin/clients/{id}", testClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testClientId))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.fullName").value("John Alexander Doe"))
                .andExpect(jsonPath("$.emailPrimary").value("john.doe@example.com"))
                .andExpect(jsonPath("$.membershipNumber").value("MEM001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.riskProfile").value("MODERATE"));

        verify(clientService).getClientById(testClientId);
    }

    @Test
    @DisplayName("Should return 404 when client not found by ID")
    void testGetClientById_ClientNotFound_ReturnsNotFound() throws Exception {
        // Given
        Long nonExistentId = 999L;
        when(clientService.getClientById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Client not found with ID: " + nonExistentId));

        // When & Then
        mockMvc.perform(get("/api/admin/clients/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(clientService).getClientById(nonExistentId);
    }

    // ================ PUT /api/admin/clients/{id} - Update Client Tests ================

    @Test
    @DisplayName("Should update client successfully")
    void testUpdateClient_ValidRequest_Success() throws Exception {
        // Given
        ClientResponse updatedResponse = ClientResponse.builder()
                .id(testClientId)
                .firstName("John")
                .lastName("Doe")
                .emailPrimary("john.doe.updated@example.com")
                .addressCity("Brisbane")
                .addressState("QLD")
                .status(Client.ClientStatus.ACTIVE)
                .build();

        when(clientService.updateClient(eq(testClientId), any(UpdateClientRequest.class)))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/admin/clients/{id}", testClientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(updateClientRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testClientId))
                .andExpect(jsonPath("$.emailPrimary").value("john.doe.updated@example.com"))
                .andExpect(jsonPath("$.addressCity").value("Brisbane"))
                .andExpect(jsonPath("$.addressState").value("QLD"));

        verify(clientService).updateClient(eq(testClientId), any(UpdateClientRequest.class));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent client")
    void testUpdateClient_ClientNotFound_ReturnsNotFound() throws Exception {
        // Given
        Long nonExistentId = 999L;
        when(clientService.updateClient(eq(nonExistentId), any(UpdateClientRequest.class)))
                .thenThrow(new ResourceNotFoundException("Client not found with ID: " + nonExistentId));

        // When & Then
        mockMvc.perform(put("/api/admin/clients/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(updateClientRequest)))
                .andExpect(status().isNotFound());

        verify(clientService).updateClient(eq(nonExistentId), any(UpdateClientRequest.class));
    }



    @Test
    @DisplayName("Should return 400 for invalid update request data")
    void testUpdateClient_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given - Create invalid request with invalid email format
        UpdateClientRequest invalidRequest = UpdateClientRequest.builder()
                .emailPrimary("invalid-email-format")
                .build();

        // When & Then
        mockMvc.perform(put("/api/admin/clients/{id}", testClientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Should not call service for invalid request
        verify(clientService, never()).updateClient(any(), any());
    }

    // ================ DELETE /api/admin/clients/{id} - Deactivate Client Tests ================

    @Test
    @DisplayName("Should deactivate client successfully")
    void testDeactivateClient_ValidId_Success() throws Exception {
        // Given
        doNothing().when(clientService).deactivateClient(testClientId);

        // When & Then
        mockMvc.perform(delete("/api/admin/clients/{id}", testClientId))
                .andExpect(status().isNoContent());

        verify(clientService).deactivateClient(testClientId);
    }

    @Test
    @DisplayName("Should return 404 when deactivating non-existent client")
    void testDeactivateClient_ClientNotFound_ReturnsNotFound() throws Exception {
        // Given
        Long nonExistentId = 999L;
        doThrow(new ResourceNotFoundException("Client not found with ID: " + nonExistentId))
                .when(clientService).deactivateClient(nonExistentId);

        // When & Then
        mockMvc.perform(delete("/api/admin/clients/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(clientService).deactivateClient(nonExistentId);
    }

    // ================ GET /api/admin/clients/search - Search Clients Tests ================

    @Test
    @DisplayName("Should search clients with search term")
    void testSearchClients_WithSearchTerm_Success() throws Exception {
        // Given
        List<ClientResponse> searchResults = List.of(testClientResponse);
        Page<ClientResponse> searchPage = new PageImpl<>(searchResults, PageRequest.of(0, 20), 1);
        
        when(clientService.searchClients(any(ClientSearchRequest.class))).thenReturn(searchPage);

        // When & Then
        mockMvc.perform(get("/api/admin/clients/search")
                .param("searchTerm", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(testClientId))
                .andExpect(jsonPath("$.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(clientService).searchClients(any(ClientSearchRequest.class));
    }

    @Test
    @DisplayName("Should search clients with multiple filters")
    void testSearchClients_WithMultipleFilters_Success() throws Exception {
        // Given
        List<ClientResponse> searchResults = List.of(testClientResponse);
        Page<ClientResponse> searchPage = new PageImpl<>(searchResults, PageRequest.of(0, 10), 1);
        
        when(clientService.searchClients(any(ClientSearchRequest.class))).thenReturn(searchPage);

        // When & Then
        mockMvc.perform(get("/api/admin/clients/search")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("email", "john.doe@example.com")
                .param("status", "ACTIVE")
                .param("city", "Sydney")
                .param("state", "NSW")
                .param("country", "Australia")
                .param("riskProfile", "MODERATE")
                .param("hasBlockchainIdentity", "true")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "firstName")
                .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(testClientId))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(clientService).searchClients(any(ClientSearchRequest.class));
    }

    @Test
    @DisplayName("Should search clients with no results")
    void testSearchClients_NoResults_Success() throws Exception {
        // Given
        Page<ClientResponse> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 20), 0);
        
        when(clientService.searchClients(any(ClientSearchRequest.class))).thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/api/admin/clients/search")
                .param("searchTerm", "NonExistentClient"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(clientService).searchClients(any(ClientSearchRequest.class));
    }

    @Test
    @DisplayName("Should search clients with default parameters when no params provided")
    void testSearchClients_DefaultParameters_Success() throws Exception {
        // Given
        List<ClientResponse> searchResults = List.of(testClientResponse);
        Page<ClientResponse> searchPage = new PageImpl<>(searchResults, PageRequest.of(0, 20), 1);
        
        when(clientService.searchClients(any(ClientSearchRequest.class))).thenReturn(searchPage);

        // When & Then
        mockMvc.perform(get("/api/admin/clients/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(clientService).searchClients(any(ClientSearchRequest.class));
    }

    // ================ Edge Cases and Error Handling Tests ================

    @Test
    @DisplayName("Should handle invalid pagination parameters gracefully")
    void testGetAllClients_InvalidPaginationParams_UsesDefaults() throws Exception {
        // Given
        List<ClientResponse> clients = List.of(testClientResponse);
        Page<ClientResponse> page = new PageImpl<>(clients, PageRequest.of(0, 20), 1);
        
        // Service should be called with corrected/default values
        when(clientService.getAllClients(anyInt(), anyInt(), anyString(), anyString())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/admin/clients")
                .param("page", "-1")  // Invalid negative page
                .param("size", "0"))  // Invalid zero size
                .andExpect(status().isOk());

        // Verify service was called (Spring will handle param validation)
        verify(clientService).getAllClients(anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should handle malformed JSON in update request")
    void testUpdateClient_MalformedJson_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/admin/clients/{id}", testClientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest());

        // Should not call service for malformed JSON
        verify(clientService, never()).updateClient(any(), any());
    }

    @Test
    @DisplayName("Should handle missing Content-Type header in update request")
    void testUpdateClient_MissingContentType_ReturnsUnsupportedMediaType() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/admin/clients/{id}", testClientId)
                .content(getObjectMapper().writeValueAsString(updateClientRequest)))
                .andExpect(status().isUnsupportedMediaType());

        // Should not call service without proper content type
        verify(clientService, never()).updateClient(any(), any());
    }

    @Test
    @DisplayName("Should handle invalid client ID format")
    void testGetClientById_InvalidIdFormat_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/clients/{id}", "invalid-id"))
                .andExpect(status().isBadRequest());

        // Should not call service for invalid ID format
        verify(clientService, never()).getClientById(any());
    }
} 