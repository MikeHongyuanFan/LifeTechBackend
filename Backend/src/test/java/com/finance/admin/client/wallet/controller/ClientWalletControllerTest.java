package com.finance.admin.client.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.admin.client.wallet.dto.WalletIntegrationRequest;
import com.finance.admin.client.wallet.model.ClientWalletIntegration;
import com.finance.admin.client.wallet.service.ClientWalletService;
import com.finance.admin.client.wallet.service.ClientDigitalCertificateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientWalletController.class)
class ClientWalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientWalletService walletService;

    @MockBean
    private ClientDigitalCertificateService certificateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getWalletOverview_WithValidClientId_ReturnsOk() throws Exception {
        Long clientId = 1L;

        mockMvc.perform(get("/api/client/{clientId}/wallet/overview", clientId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPlatformBalances_WithValidClientId_ReturnsOk() throws Exception {
        Long clientId = 1L;

        mockMvc.perform(get("/api/client/{clientId}/wallet/balances", clientId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getWalletIntegrations_WithValidClientId_ReturnsOk() throws Exception {
        Long clientId = 1L;

        mockMvc.perform(get("/api/client/{clientId}/wallet/integrations", clientId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createWalletIntegration_WithValidRequest_ReturnsOk() throws Exception {
        Long clientId = 1L;
        WalletIntegrationRequest request = WalletIntegrationRequest.builder()
                .platformName("CommSec")
                .platformType(ClientWalletIntegration.PlatformType.BROKERAGE)
                .accountIdentifier("12345678")
                .apiKey("test-key")
                .apiSecret("test-secret")
                .syncFrequency(ClientWalletIntegration.SyncFrequency.DAILY)
                .build();

        mockMvc.perform(post("/api/client/{clientId}/wallet/integrate", clientId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeWalletIntegration_WithValidIds_ReturnsOk() throws Exception {
        Long clientId = 1L;
        Long integrationId = 1L;

        mockMvc.perform(delete("/api/client/{clientId}/wallet/integrations/{integrationId}", 
                        clientId, integrationId)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDigitalCertificates_WithValidClientId_ReturnsOk() throws Exception {
        Long clientId = 1L;

        mockMvc.perform(get("/api/client/{clientId}/wallet/certificates", clientId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDigitalCertificate_WithValidIds_ReturnsOk() throws Exception {
        Long clientId = 1L;
        Long certificateId = 1L;

        mockMvc.perform(get("/api/client/{clientId}/wallet/certificates/{certificateId}", 
                        clientId, certificateId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void verifyCertificate_WithValidNumber_ReturnsOk() throws Exception {
        Long clientId = 1L;
        String certificateNumber = "CERT-001";

        mockMvc.perform(get("/api/client/{clientId}/wallet/certificates/verify/{certificateNumber}", 
                        clientId, certificateNumber))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createMockCertificate_WithValidClientId_ReturnsOk() throws Exception {
        Long clientId = 1L;

        mockMvc.perform(post("/api/client/{clientId}/wallet/certificates/create-mock", clientId)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void getWalletOverview_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        Long clientId = 1L;

        mockMvc.perform(get("/api/client/{clientId}/wallet/overview", clientId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createWalletIntegration_WithUserRole_ReturnsOk() throws Exception {
        Long clientId = 1L;
        WalletIntegrationRequest request = WalletIntegrationRequest.builder()
                .platformName("CommSec")
                .platformType(ClientWalletIntegration.PlatformType.BROKERAGE)
                .build();

        mockMvc.perform(post("/api/client/{clientId}/wallet/integrate", clientId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
} 