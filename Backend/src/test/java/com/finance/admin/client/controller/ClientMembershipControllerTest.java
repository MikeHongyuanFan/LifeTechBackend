package com.finance.admin.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.admin.client.dto.MembershipResponse;
import com.finance.admin.client.service.ClientMembershipService;
import com.finance.admin.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ClientMembershipController
 * Tests all REST endpoints for membership management functionality
 */
@WebMvcTest(ClientMembershipController.class)
@EnableMethodSecurity
class ClientMembershipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientMembershipService membershipService;

    @Autowired
    private ObjectMapper objectMapper;

    private MembershipResponse testMembershipResponse;
    private Map<String, Object> testBenefits;

    @BeforeEach
    void setUp() {
        // Create test benefits
        testBenefits = new HashMap<>();
        testBenefits.put("basicAccess", true);
        testBenefits.put("monthlyNewsletter", true);
        testBenefits.put("investmentAlerts", true);

        // Create test membership response
        testMembershipResponse = MembershipResponse.builder()
                .id(1L)
                .clientId(1L)
                .membershipNumber("FM-202406-ABC12345")
                .membershipTier("BASIC")
                .membershipStatus("ACTIVE")
                .joinedDate(LocalDate.of(2024, 6, 1))
                .tierUpgradeDate(null)
                .pointsBalance(100)
                .benefitsUnlocked(testBenefits)
                .digitalCardIssued(false)
                .qrCode("iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAYAAACtWK6eAAAA...")
                .build();
    }

    // ================ GET /{clientId} - Get Membership Tests ================

    @Test
    @WithMockUser(roles = "CLIENT")
    void getMembership_WithValidClientId_ReturnsMembershipDetails() throws Exception {
        // Arrange
        Long clientId = 1L;
        when(membershipService.getMembershipByClientId(clientId)).thenReturn(testMembershipResponse);

        // Act & Assert
        mockMvc.perform(get("/api/client/membership/{clientId}", clientId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clientId").value(1))
                .andExpect(jsonPath("$.membershipNumber").value("FM-202406-ABC12345"))
                .andExpect(jsonPath("$.membershipTier").value("BASIC"))
                .andExpect(jsonPath("$.membershipStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.pointsBalance").value(100))
                .andExpect(jsonPath("$.digitalCardIssued").value(false))
                .andExpect(jsonPath("$.benefitsUnlocked.basicAccess").value(true))
                .andExpect(jsonPath("$.benefitsUnlocked.monthlyNewsletter").value(true))
                .andExpect(jsonPath("$.qrCode").exists());

        verify(membershipService).getMembershipByClientId(clientId);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void getMembership_WithNonExistentClientId_ReturnsNotFound() throws Exception {
        // Arrange
        Long clientId = 999L;
        when(membershipService.getMembershipByClientId(clientId))
                .thenThrow(new ResourceNotFoundException("Membership not found for client: " + clientId));

        // Act & Assert
        mockMvc.perform(get("/api/client/membership/{clientId}", clientId)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(membershipService).getMembershipByClientId(clientId);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void getMembership_WithInvalidClientId_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/client/membership/{clientId}", "invalid")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(membershipService, never()).getMembershipByClientId(any());
    }

    // ================ POST /{clientId} - Create Membership Tests ================

    @Test
    @WithMockUser(roles = "CLIENT")
    void createMembership_WithValidClientId_ReturnsCreatedMembership() throws Exception {
        // Arrange
        Long clientId = 1L;
        when(membershipService.createMembership(clientId)).thenReturn(testMembershipResponse);

        // Act & Assert
        mockMvc.perform(post("/api/client/membership/{clientId}", clientId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clientId").value(1))
                .andExpect(jsonPath("$.membershipNumber").value("FM-202406-ABC12345"))
                .andExpect(jsonPath("$.membershipTier").value("BASIC"))
                .andExpect(jsonPath("$.membershipStatus").value("ACTIVE"));

        verify(membershipService).createMembership(clientId);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void createMembership_WithNonExistentClient_ReturnsNotFound() throws Exception {
        // Arrange
        Long clientId = 999L;
        when(membershipService.createMembership(clientId))
                .thenThrow(new ResourceNotFoundException("Client not found with ID: " + clientId));

        // Act & Assert
        mockMvc.perform(post("/api/client/membership/{clientId}", clientId)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(membershipService).createMembership(clientId);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void createMembership_WithExistingMembership_ReturnsBadRequest() throws Exception {
        // Arrange
        Long clientId = 1L;
        when(membershipService.createMembership(clientId))
                .thenThrow(new IllegalStateException("Membership already exists for client: " + clientId));

        // Act & Assert
        mockMvc.perform(post("/api/client/membership/{clientId}", clientId)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(membershipService).createMembership(clientId);
    }

    // ================ PUT /{clientId}/tier - Update Membership Tier Tests ================

    @Test
    @WithMockUser(roles = "CLIENT")
    void updateMembershipTier_WithValidTier_ReturnsUpdatedMembership() throws Exception {
        // Arrange
        Long clientId = 1L;
        String newTier = "PREMIUM";
        Map<String, String> request = Map.of("tier", newTier);
        
        MembershipResponse updatedResponse = MembershipResponse.builder()
                .id(1L)
                .clientId(1L)
                .membershipNumber("FM-202406-ABC12345")
                .membershipTier("PREMIUM")
                .membershipStatus("ACTIVE")
                .joinedDate(LocalDate.of(2024, 6, 1))
                .tierUpgradeDate(LocalDate.now())
                .pointsBalance(100)
                .benefitsUnlocked(testBenefits)
                .digitalCardIssued(false)
                .qrCode("iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAYAAACtWK6eAAAA...")
                .build();

        when(membershipService.updateMembershipTier(clientId, newTier)).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/client/membership/{clientId}/tier", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.membershipTier").value("PREMIUM"))
                .andExpect(jsonPath("$.tierUpgradeDate").exists());

        verify(membershipService).updateMembershipTier(clientId, newTier);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void updateMembershipTier_WithInvalidTier_ReturnsBadRequest() throws Exception {
        // Arrange
        Long clientId = 1L;
        String invalidTier = "INVALID_TIER";
        Map<String, String> request = Map.of("tier", invalidTier);

        when(membershipService.updateMembershipTier(clientId, invalidTier))
                .thenThrow(new IllegalArgumentException("Invalid membership tier: " + invalidTier));

        // Act & Assert
        mockMvc.perform(put("/api/client/membership/{clientId}/tier", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(membershipService).updateMembershipTier(clientId, invalidTier);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void updateMembershipTier_WithNonExistentMembership_ReturnsNotFound() throws Exception {
        // Arrange
        Long clientId = 999L;
        String tier = "PREMIUM";
        Map<String, String> request = Map.of("tier", tier);

        when(membershipService.updateMembershipTier(clientId, tier))
                .thenThrow(new ResourceNotFoundException("Membership not found for client: " + clientId));

        // Act & Assert
        mockMvc.perform(put("/api/client/membership/{clientId}/tier", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(membershipService).updateMembershipTier(clientId, tier);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void updateMembershipTier_WithMissingTierField_ReturnsBadRequest() throws Exception {
        // Arrange
        Long clientId = 1L;
        Map<String, String> request = Map.of("wrongField", "PREMIUM");

        // Act & Assert
        mockMvc.perform(put("/api/client/membership/{clientId}/tier", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        // No need to verify service call since validation happens before service invocation
    }

    // ================ PUT /{clientId}/points - Update Points Balance Tests ================

    @Test
    @WithMockUser(roles = "CLIENT")
    void updatePointsBalance_WithPositiveDelta_ReturnsUpdatedMembership() throws Exception {
        // Arrange
        Long clientId = 1L;
        Integer pointsDelta = 50;
        Map<String, Integer> request = Map.of("pointsDelta", pointsDelta);
        
        MembershipResponse updatedResponse = MembershipResponse.builder()
                .id(1L)
                .clientId(1L)
                .membershipNumber("FM-202406-ABC12345")
                .membershipTier("BASIC")
                .membershipStatus("ACTIVE")
                .joinedDate(LocalDate.of(2024, 6, 1))
                .pointsBalance(150) // 100 + 50
                .benefitsUnlocked(testBenefits)
                .digitalCardIssued(false)
                .qrCode("iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAYAAACtWK6eAAAA...")
                .build();

        when(membershipService.updatePointsBalance(clientId, pointsDelta)).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/client/membership/{clientId}/points", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pointsBalance").value(150));

        verify(membershipService).updatePointsBalance(clientId, pointsDelta);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void updatePointsBalance_WithNegativeDelta_ReturnsUpdatedMembership() throws Exception {
        // Arrange
        Long clientId = 1L;
        Integer pointsDelta = -30;
        Map<String, Integer> request = Map.of("pointsDelta", pointsDelta);
        
        MembershipResponse updatedResponse = MembershipResponse.builder()
                .id(1L)
                .clientId(1L)
                .membershipNumber("FM-202406-ABC12345")
                .membershipTier("BASIC")
                .membershipStatus("ACTIVE")
                .joinedDate(LocalDate.of(2024, 6, 1))
                .pointsBalance(70) // 100 - 30
                .benefitsUnlocked(testBenefits)
                .digitalCardIssued(false)
                .qrCode("iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAYAAACtWK6eAAAA...")
                .build();

        when(membershipService.updatePointsBalance(clientId, pointsDelta)).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/client/membership/{clientId}/points", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pointsBalance").value(70));

        verify(membershipService).updatePointsBalance(clientId, pointsDelta);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void updatePointsBalance_WithNegativeBalanceResult_ReturnsBadRequest() throws Exception {
        // Arrange
        Long clientId = 1L;
        Integer pointsDelta = -200; // Would result in negative balance
        Map<String, Integer> request = Map.of("pointsDelta", pointsDelta);

        when(membershipService.updatePointsBalance(clientId, pointsDelta))
                .thenThrow(new IllegalArgumentException("Points balance cannot be negative"));

        // Act & Assert
        mockMvc.perform(put("/api/client/membership/{clientId}/points", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(membershipService).updatePointsBalance(clientId, pointsDelta);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void updatePointsBalance_WithMissingPointsDeltaField_ReturnsBadRequest() throws Exception {
        // Arrange
        Long clientId = 1L;
        Map<String, Integer> request = Map.of("wrongField", 50);

        // Act & Assert
        mockMvc.perform(put("/api/client/membership/{clientId}/points", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        // No need to verify service call since validation happens before service invocation
    }

    // ================ POST /{clientId}/digital-card - Generate Digital Card Tests ================

    @Test
    @WithMockUser(roles = "CLIENT")
    void generateDigitalCard_WithValidClientId_ReturnsUpdatedMembership() throws Exception {
        // Arrange
        Long clientId = 1L;
        
        MembershipResponse updatedResponse = MembershipResponse.builder()
                .id(1L)
                .clientId(1L)
                .membershipNumber("FM-202406-ABC12345")
                .membershipTier("BASIC")
                .membershipStatus("ACTIVE")
                .joinedDate(LocalDate.of(2024, 6, 1))
                .pointsBalance(100)
                .benefitsUnlocked(testBenefits)
                .digitalCardIssued(true) // Now true
                .qrCode("iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAYAAACtWK6eAAAA...")
                .build();

        when(membershipService.generateDigitalCard(clientId)).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/client/membership/{clientId}/digital-card", clientId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.digitalCardIssued").value(true))
                .andExpect(jsonPath("$.qrCode").exists());

        verify(membershipService).generateDigitalCard(clientId);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void generateDigitalCard_WithNonExistentMembership_ReturnsNotFound() throws Exception {
        // Arrange
        Long clientId = 999L;
        when(membershipService.generateDigitalCard(clientId))
                .thenThrow(new ResourceNotFoundException("Membership not found for client: " + clientId));

        // Act & Assert
        mockMvc.perform(post("/api/client/membership/{clientId}/digital-card", clientId)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ================ Security and Authorization Tests ================

    @Test
    void getMembership_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/client/membership/{clientId}", 1L))
                .andExpect(status().isUnauthorized());

        verify(membershipService, never()).getMembershipByClientId(any());
    }

    @Test
    @WithMockUser(roles = "USER") // Wrong role
    void getMembership_WithWrongRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/client/membership/{clientId}", 1L)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(membershipService, never()).getMembershipByClientId(any());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void createMembership_WithoutCsrf_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/client/membership/{clientId}", 1L))
                .andExpect(status().isForbidden());

        verify(membershipService, never()).createMembership(any());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void updateMembershipTier_WithoutCsrf_ReturnsForbidden() throws Exception {
        // Arrange
        Map<String, String> request = Map.of("tier", "PREMIUM");

        // Act & Assert
        mockMvc.perform(put("/api/client/membership/{clientId}/tier", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(membershipService, never()).updateMembershipTier(any(), any());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void updatePointsBalance_WithoutCsrf_ReturnsForbidden() throws Exception {
        // Arrange
        Map<String, Integer> request = Map.of("pointsDelta", 50);

        // Act & Assert
        mockMvc.perform(put("/api/client/membership/{clientId}/points", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(membershipService, never()).updatePointsBalance(anyLong(), anyInt());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void generateDigitalCard_WithoutCsrf_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/client/membership/{clientId}/digital-card", 1L))
                .andExpect(status().isForbidden());

        verify(membershipService, never()).generateDigitalCard(any());
    }

    // ================ Input Validation Tests ================

    @Test
    @WithMockUser(roles = "CLIENT")
    void updateMembershipTier_WithEmptyRequestBody_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/client/membership/{clientId}/tier", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(membershipService, never()).updateMembershipTier(any(), any());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void updatePointsBalance_WithEmptyRequestBody_ReturnsBadRequest() throws Exception {
        // Arrange
        Long clientId = 1L;

        // Act & Assert
        mockMvc.perform(put("/api/client/membership/{clientId}/points", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        // No need to verify service call since validation happens before service invocation
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void updateMembershipTier_WithInvalidJson_ReturnsBadRequest() throws Exception {
        // Arrange
        Long clientId = 1L;

        // Act & Assert - sending invalid JSON
        mockMvc.perform(put("/api/client/membership/{clientId}/tier", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        // No need to verify service call since JSON parsing fails before service invocation
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void updatePointsBalance_WithInvalidJson_ReturnsBadRequest() throws Exception {
        // Arrange
        Long clientId = 1L;

        // Act & Assert - sending invalid JSON
        mockMvc.perform(put("/api/client/membership/{clientId}/points", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        // No need to verify service call since JSON parsing fails before service invocation
    }
} 