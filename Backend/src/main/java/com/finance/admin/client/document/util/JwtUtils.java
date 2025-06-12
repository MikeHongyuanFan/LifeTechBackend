package com.finance.admin.client.document.util;

import com.finance.admin.client.service.JwtTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * JWT utility class for extracting client information from HTTP requests
 * Used specifically for client document access authentication
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtils {

    private final JwtTokenService jwtTokenService;

    /**
     * Extract client ID from the JWT token in the request
     */
    public Long getClientIdFromRequest(HttpServletRequest request) {
        String token = getJwtFromRequest(request);
        if (token == null) {
            throw new RuntimeException("No JWT token found in request");
        }

        try {
            return jwtTokenService.getClientIdFromToken(token);
        } catch (Exception e) {
            log.error("Failed to extract client ID from token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token");
        }
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Validate JWT token from request
     */
    public boolean validateTokenFromRequest(HttpServletRequest request) {
        String token = getJwtFromRequest(request);
        if (token == null) {
            return false;
        }

        try {
            return jwtTokenService.validateToken(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
} 