package com.finance.admin.common.controller;

import com.finance.admin.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Tag(name = "Test", description = "Test endpoints for debugging")
public class TestController {

    @GetMapping("/health")
    @Operation(summary = "Test endpoint", description = "Simple test endpoint to verify API is working")
    public ResponseEntity<ApiResponse<String>> testHealth() {
        return ResponseEntity.ok(ApiResponse.success("Test endpoint is working", "OK"));
    }
} 
