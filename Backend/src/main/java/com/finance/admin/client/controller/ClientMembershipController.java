package com.finance.admin.client.controller;

import com.finance.admin.client.dto.MembershipResponse;
import com.finance.admin.client.service.ClientMembershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/client/membership")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Client Membership", description = "Client membership management endpoints")
public class ClientMembershipController {

    private final ClientMembershipService membershipService;

    @Operation(summary = "Get client membership", description = "Retrieve membership details for a client")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Membership details retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Membership not found")
    })
    @GetMapping("/{clientId}")
    public ResponseEntity<MembershipResponse> getMembership(
            @Parameter(description = "Client ID") @PathVariable Long clientId) {
        log.info("Getting membership details for client: {}", clientId);
        MembershipResponse response = membershipService.getMembershipByClientId(clientId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create client membership", description = "Create a new membership for a client")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Membership created successfully"),
        @ApiResponse(responseCode = "400", description = "Client already has a membership"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @PostMapping("/{clientId}")
    public ResponseEntity<MembershipResponse> createMembership(
            @Parameter(description = "Client ID") @PathVariable Long clientId) {
        log.info("Creating membership for client: {}", clientId);
        MembershipResponse response = membershipService.createMembership(clientId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update membership tier", description = "Update the membership tier for a client")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Membership tier updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid tier"),
        @ApiResponse(responseCode = "404", description = "Membership not found")
    })
    @PutMapping("/{clientId}/tier")
    public ResponseEntity<MembershipResponse> updateMembershipTier(
            @Parameter(description = "Client ID") @PathVariable Long clientId,
            @Parameter(description = "New tier") @RequestBody Map<String, String> request) {
        log.info("Updating membership tier for client: {}", clientId);
        MembershipResponse response = membershipService.updateMembershipTier(clientId, request.get("tier"));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update points balance", description = "Update the points balance for a client's membership")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Points balance updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid points delta"),
        @ApiResponse(responseCode = "404", description = "Membership not found")
    })
    @PutMapping("/{clientId}/points")
    public ResponseEntity<MembershipResponse> updatePointsBalance(
            @Parameter(description = "Client ID") @PathVariable Long clientId,
            @Parameter(description = "Points delta") @RequestBody Map<String, Integer> request) {
        log.info("Updating points balance for client: {}", clientId);
        MembershipResponse response = membershipService.updatePointsBalance(clientId, request.get("pointsDelta"));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Generate digital card", description = "Generate or retrieve digital membership card")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Digital card generated/retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Membership not found")
    })
    @PostMapping("/{clientId}/digital-card")
    public ResponseEntity<MembershipResponse> generateDigitalCard(
            @Parameter(description = "Client ID") @PathVariable Long clientId) {
        log.info("Generating digital card for client: {}", clientId);
        MembershipResponse response = membershipService.generateDigitalCard(clientId);
        return ResponseEntity.ok(response);
    }
} 