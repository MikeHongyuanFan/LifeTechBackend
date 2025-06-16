package com.finance.admin.client.wallet.controller;

import com.finance.admin.client.wallet.dto.*;
import com.finance.admin.client.wallet.service.ClientWalletService;
import com.finance.admin.client.wallet.service.ClientDigitalCertificateService;
import com.finance.admin.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/client/{clientId}/wallet")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Client Wallet", description = "Digital wealth wallet management for clients")
public class ClientWalletController {

    private final ClientWalletService walletService;
    private final ClientDigitalCertificateService certificateService;

    @Operation(summary = "Get wallet overview", description = "Get comprehensive wallet overview including balances, performance, and recent transactions")
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<WalletOverviewResponse>> getWalletOverview(
            @Parameter(description = "Client ID") @PathVariable Long clientId) {
        
        log.info("Getting wallet overview for client: {}", clientId);
        
        WalletOverviewResponse overview = walletService.getWalletOverview(clientId);
        
        return ResponseEntity.ok(ApiResponse.success("Wallet overview retrieved successfully", overview));
    }

    @Operation(summary = "Get platform balances", description = "Get balances from all connected platforms")
    @GetMapping("/balances")
    public ResponseEntity<ApiResponse<List<WalletOverviewResponse.PlatformBalance>>> getPlatformBalances(
            @Parameter(description = "Client ID") @PathVariable Long clientId) {
        
        log.info("Getting platform balances for client: {}", clientId);
        
        List<WalletOverviewResponse.PlatformBalance> balances = walletService.getPlatformBalances(clientId);
        
        return ResponseEntity.ok(ApiResponse.success("Platform balances retrieved successfully", balances));
    }

    @Operation(summary = "Get wallet integrations", description = "Get all wallet platform integrations for the client")
    @GetMapping("/integrations")
    public ResponseEntity<ApiResponse<List<WalletIntegrationResponse>>> getWalletIntegrations(
            @Parameter(description = "Client ID") @PathVariable Long clientId) {
        
        log.info("Getting wallet integrations for client: {}", clientId);
        
        List<WalletIntegrationResponse> integrations = walletService.getWalletIntegrations(clientId);
        
        return ResponseEntity.ok(ApiResponse.success("Wallet integrations retrieved successfully", integrations));
    }

    @Operation(summary = "Create wallet integration", description = "Connect a new platform to the client's wallet")
    @PostMapping("/integrate")
    public ResponseEntity<ApiResponse<WalletIntegrationResponse>> createWalletIntegration(
            @Parameter(description = "Client ID") @PathVariable Long clientId,
            @Valid @RequestBody WalletIntegrationRequest request) {
        
        log.info("Creating wallet integration for client: {} with platform: {}", clientId, request.getPlatformName());
        
        WalletIntegrationResponse integration = walletService.createWalletIntegration(clientId, request);
        
        return ResponseEntity.ok(ApiResponse.success("Wallet integration created successfully", integration));
    }

    @Operation(summary = "Remove wallet integration", description = "Disconnect a platform from the client's wallet")
    @DeleteMapping("/integrations/{integrationId}")
    public ResponseEntity<ApiResponse<Void>> removeWalletIntegration(
            @Parameter(description = "Client ID") @PathVariable Long clientId,
            @Parameter(description = "Integration ID") @PathVariable Long integrationId) {
        
        log.info("Removing wallet integration {} for client: {}", integrationId, clientId);
        
        walletService.removeWalletIntegration(clientId, integrationId);
        
        return ResponseEntity.ok(ApiResponse.success("Wallet integration removed successfully"));
    }

    @Operation(summary = "Get digital certificates", description = "Get all e-share certificates in the client's wallet")
    @GetMapping("/certificates")
    public ResponseEntity<ApiResponse<Page<DigitalCertificateResponse>>> getDigitalCertificates(
            @Parameter(description = "Client ID") @PathVariable Long clientId,
            @Parameter(description = "Company name filter") @RequestParam(required = false) String companyName,
            @Parameter(description = "Certificate type filter") @RequestParam(required = false) String certificateType,
            @Parameter(description = "Valid certificates only") @RequestParam(defaultValue = "true") Boolean validOnly,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Getting digital certificates for client: {}", clientId);
        
        Page<DigitalCertificateResponse> certificates;
        if (companyName != null || certificateType != null) {
            // Parse certificate type if provided
            var type = certificateType != null ? 
                    parseeCertificateType(certificateType) : null;
            
            certificates = certificateService.searchCertificates(clientId, companyName, type, validOnly, pageable);
        } else {
            certificates = certificateService.getClientCertificates(clientId, pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success("Digital certificates retrieved successfully", certificates));
    }

    @Operation(summary = "Get specific certificate", description = "Get details of a specific e-share certificate")
    @GetMapping("/certificates/{certificateId}")
    public ResponseEntity<ApiResponse<DigitalCertificateResponse>> getCertificate(
            @Parameter(description = "Client ID") @PathVariable Long clientId,
            @Parameter(description = "Certificate ID") @PathVariable Long certificateId) {
        
        log.info("Getting certificate {} for client: {}", certificateId, clientId);
        
        DigitalCertificateResponse certificate = certificateService.getCertificate(clientId, certificateId);
        
        return ResponseEntity.ok(ApiResponse.success("Certificate retrieved successfully", certificate));
    }

    @Operation(summary = "Get certificate by number", description = "Get certificate details by certificate number")
    @GetMapping("/certificates/number/{certificateNumber}")
    public ResponseEntity<ApiResponse<DigitalCertificateResponse>> getCertificateByNumber(
            @Parameter(description = "Certificate number") @PathVariable String certificateNumber) {
        
        log.info("Getting certificate with number: {}", certificateNumber);
        
        DigitalCertificateResponse certificate = certificateService.getCertificateByNumber(certificateNumber);
        
        return ResponseEntity.ok(ApiResponse.success("Certificate retrieved successfully", certificate));
    }

    @Operation(summary = "Verify certificate authenticity", description = "Verify the authenticity of a digital certificate")
    @GetMapping("/certificates/verify/{certificateNumber}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyCertificate(
            @Parameter(description = "Certificate number") @PathVariable String certificateNumber) {
        
        log.info("Verifying certificate: {}", certificateNumber);
        
        boolean isValid = certificateService.verifyCertificateAuthenticity(certificateNumber);
        
        Map<String, Object> result = Map.of(
                "certificateNumber", certificateNumber,
                "isValid", isValid,
                "verificationStatus", isValid ? "VERIFIED" : "INVALID",
                "verifiedAt", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(ApiResponse.success("Certificate verification completed", result));
    }

    @Operation(summary = "Get total certificate value", description = "Get total market value of all certificates")
    @GetMapping("/certificates/total-value")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTotalCertificateValue(
            @Parameter(description = "Client ID") @PathVariable Long clientId) {
        
        log.info("Getting total certificate value for client: {}", clientId);
        
        BigDecimal totalValue = certificateService.getTotalCertificateValue(clientId);
        
        Map<String, Object> result = Map.of(
                "totalValue", totalValue,
                "currency", "AUD",
                "formattedValue", String.format("$%,.2f AUD", totalValue)
        );
        
        return ResponseEntity.ok(ApiResponse.success("Total certificate value calculated successfully", result));
    }

    @Operation(summary = "Create mock certificates", description = "Create mock certificates for testing (development only)")
    @PostMapping("/certificates/create-mock")
    public ResponseEntity<ApiResponse<List<DigitalCertificateResponse>>> createMockCertificates(
            @Parameter(description = "Client ID") @PathVariable Long clientId) {
        
        log.info("Creating mock certificates for client: {}", clientId);
        
        List<DigitalCertificateResponse> certificates = certificateService.createMockCertificatesForClient(clientId);
        
        return ResponseEntity.ok(ApiResponse.success("Mock certificates created successfully", certificates));
    }

    private com.finance.admin.client.wallet.model.ClientDigitalCertificate.CertificateType parseeCertificateType(String type) {
        try {
            return com.finance.admin.client.wallet.model.ClientDigitalCertificate.CertificateType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid certificate type: {}", type);
            return null;
        }
    }
} 