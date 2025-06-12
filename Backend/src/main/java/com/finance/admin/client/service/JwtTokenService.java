package com.finance.admin.client.service;

import com.finance.admin.client.model.Client;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
public class JwtTokenService {

    @Value("${app.jwt.secret:defaultSecretKeyForJWTTokenGenerationMustBe256BitsLong}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration-seconds:3600}")
    private int accessTokenExpirationSeconds;

    @Value("${app.jwt.refresh-token-expiration-seconds:86400}")
    private int refreshTokenExpirationSeconds;

    @Value("${app.jwt.reset-token-expiration-seconds:1800}")
    private int resetTokenExpirationSeconds;

    private final Set<String> blacklistedTokens = new HashSet<>();

    /**
     * Generate JWT access token for client
     */
    public String generateAccessToken(Client client) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationSeconds * 1000L);

        return Jwts.builder()
                .setSubject(client.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("clientId", client.getId())
                .claim("membershipNumber", client.getMembershipNumber())
                .claim("email", client.getEmailPrimary())
                .claim("fullName", client.getFullName())
                .claim("status", client.getStatus().toString())
                .claim("type", "ACCESS_TOKEN")
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate JWT refresh token for client
     */
    public String generateRefreshToken(Client client) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationSeconds * 1000L);

        return Jwts.builder()
                .setSubject(client.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("clientId", client.getId())
                .claim("type", "REFRESH_TOKEN")
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate password reset token
     */
    public String generatePasswordResetToken(Client client) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + resetTokenExpirationSeconds * 1000L);

        return Jwts.builder()
                .setSubject(client.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("clientId", client.getId())
                .claim("email", client.getEmailPrimary())
                .claim("type", "RESET_TOKEN")
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            if (blacklistedTokens.contains(token)) {
                log.warn("Token is blacklisted");
                return false;
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Check if token is expired
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                log.warn("Token is expired");
                return false;
            }

            return true;

        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract client ID from token
     */
    public Long getClientIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("clientId", Long.class);

        } catch (JwtException | IllegalArgumentException e) {
            log.error("Failed to extract client ID from token: {}", e.getMessage());
            throw new RuntimeException("Invalid token");
        }
    }

    /**
     * Extract all claims from token
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (JwtException | IllegalArgumentException e) {
            log.error("Failed to extract claims from token: {}", e.getMessage());
            throw new RuntimeException("Invalid token");
        }
    }

    /**
     * Validate refresh token and return client ID
     */
    public Long validateRefreshToken(String refreshToken) {
        try {
            if (blacklistedTokens.contains(refreshToken)) {
                throw new RuntimeException("Token is blacklisted");
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();

            // Check token type
            String tokenType = claims.get("type", String.class);
            if (!"REFRESH_TOKEN".equals(tokenType)) {
                throw new RuntimeException("Invalid token type");
            }

            // Check if token is expired
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                throw new RuntimeException("Refresh token is expired");
            }

            return claims.get("clientId", Long.class);

        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid refresh token: {}", e.getMessage());
            throw new RuntimeException("Invalid refresh token");
        }
    }

    /**
     * Validate password reset token and return client ID
     */
    public Long validatePasswordResetToken(String resetToken) {
        try {
            if (blacklistedTokens.contains(resetToken)) {
                throw new RuntimeException("Token is blacklisted");
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(resetToken)
                    .getBody();

            // Check token type
            String tokenType = claims.get("type", String.class);
            if (!"RESET_TOKEN".equals(tokenType)) {
                throw new RuntimeException("Invalid token type");
            }

            // Check if token is expired
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                throw new RuntimeException("Reset token is expired");
            }

            return claims.get("clientId", Long.class);

        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid reset token: {}", e.getMessage());
            throw new RuntimeException("Invalid or expired reset token");
        }
    }

    /**
     * Invalidate token by adding to blacklist
     */
    public void invalidateToken(String token) {
        try {
            blacklistedTokens.add(token);
            log.info("Token has been invalidated");
        } catch (Exception e) {
            log.error("Failed to invalidate token: {}", e.getMessage());
        }
    }

    /**
     * Check if token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    /**
     * Get token expiration date
     */
    public Date getTokenExpiration(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration();
        } catch (Exception e) {
            log.error("Failed to get token expiration: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get remaining time until token expires (in seconds)
     */
    public long getRemainingTokenTime(String token) {
        try {
            Date expiration = getTokenExpiration(token);
            if (expiration == null) {
                return 0;
            }
            
            long currentTime = System.currentTimeMillis();
            long expirationTime = expiration.getTime();
            
            return Math.max(0, (expirationTime - currentTime) / 1000);
        } catch (Exception e) {
            log.error("Failed to get remaining token time: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Extract user details from token for authentication
     */
    public Map<String, Object> extractUserDetails(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("clientId", claims.get("clientId"));
            userDetails.put("membershipNumber", claims.get("membershipNumber"));
            userDetails.put("email", claims.get("email"));
            userDetails.put("fullName", claims.get("fullName"));
            userDetails.put("status", claims.get("status"));
            userDetails.put("issuedAt", claims.getIssuedAt());
            userDetails.put("expiration", claims.getExpiration());
            
            return userDetails;
        } catch (Exception e) {
            log.error("Failed to extract user details: {}", e.getMessage());
            throw new RuntimeException("Invalid token");
        }
    }

    /**
     * Get access token expiration time in seconds
     */
    public int getAccessTokenExpirationSeconds() {
        return accessTokenExpirationSeconds;
    }

    /**
     * Get refresh token expiration time in seconds
     */
    public int getRefreshTokenExpirationSeconds() {
        return refreshTokenExpirationSeconds;
    }

    /**
     * Cleanup expired blacklisted tokens
     */
    public void cleanupExpiredTokens() {
        Iterator<String> iterator = blacklistedTokens.iterator();
        while (iterator.hasNext()) {
            String token = iterator.next();
            try {
                Date expiration = getTokenExpiration(token);
                if (expiration != null && expiration.before(new Date())) {
                    iterator.remove();
                }
            } catch (Exception e) {
                // If we can't parse the token, it's probably expired or invalid, so remove it
                iterator.remove();
            }
        }
        log.debug("Cleaned up {} expired blacklisted tokens", blacklistedTokens.size());
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
} 