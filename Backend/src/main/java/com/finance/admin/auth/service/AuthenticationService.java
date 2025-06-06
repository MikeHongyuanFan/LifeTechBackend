package com.finance.admin.auth.service;

import com.finance.admin.auth.dto.LoginRequest;
import com.finance.admin.auth.dto.LoginResponse;
import com.finance.admin.auth.dto.MfaVerificationRequest;
import com.finance.admin.auth.entity.AdminUser;
import com.finance.admin.auth.repository.AdminUserRepository;
import com.finance.admin.auth.security.AdminUserPrincipal;
import com.finance.admin.auth.security.JwtTokenProvider;
import com.finance.admin.common.exception.AuthenticationException;
import com.finance.admin.common.exception.AccountLockedException;
import com.finance.admin.common.util.IpAddressUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

@Service
@Transactional
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private static final String MFA_TOKEN_PREFIX = "mfa_token:";

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private MfaService mfaService;

    @Value("${app.security.max-failed-attempts}")
    private int maxFailedAttempts;

    @Value("${app.security.lockout-duration}")
    private long lockoutDurationMs;

    public LoginResponse authenticateUser(LoginRequest loginRequest, HttpServletRequest request) {
        logger.info("Authentication attempt for user: {}", loginRequest.getUsername());
        
        // Find user
        AdminUser user = adminUserRepository.findByUsernameOrEmail(
                loginRequest.getUsername(), loginRequest.getUsername())
                .orElse(null);

        if (user == null) {
            logger.warn("User not found: {}", loginRequest.getUsername());
            throw new AuthenticationException("Invalid username or password");
        }

        // Check if account is locked
        if (!user.isAccountNonLocked()) {
            logger.warn("Account locked for user: {}", user.getUsername());
            throw new AccountLockedException("Account is temporarily locked");
        }

        try {
            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            AdminUserPrincipal userPrincipal = (AdminUserPrincipal) authentication.getPrincipal();

            // Reset failed attempts on successful authentication
            user.resetFailedAttempts();
            adminUserRepository.save(user);

            // Check if MFA is enabled
            if (user.getMfaEnabled()) {
                logger.info("MFA required for user: {}", user.getUsername());
                String mfaToken = generateMfaToken(user.getId());
                return LoginResponse.mfaRequired(mfaToken);
            }

            // Complete login
            return completeLogin(user, userPrincipal);

        } catch (org.springframework.security.core.AuthenticationException ex) {
            logger.warn("Authentication failed for user: {}", loginRequest.getUsername());
            handleFailedLogin(user);
            throw new AuthenticationException("Invalid username or password");
        }
    }

    public LoginResponse verifyMfa(MfaVerificationRequest request, HttpServletRequest httpRequest) {
        logger.info("MFA verification attempt");
        
        // Validate MFA token
        if (!isMfaTokenValid(request.getMfaToken())) {
            logger.warn("Invalid MFA token");
            throw new AuthenticationException("Invalid or expired MFA token");
        }

        // Get user from MFA token
        AdminUser user = getUserFromMfaToken(request.getMfaToken());
        
        if (user == null) {
            logger.warn("User not found for MFA token");
            throw new AuthenticationException("Invalid MFA token");
        }

        // Verify MFA code
        if (!mfaService.verifyCode(user.getMfaSecret(), request.getMfaCode())) {
            logger.warn("Invalid MFA code for user: {}", user.getUsername());
            handleFailedLogin(user);
            throw new AuthenticationException("Invalid MFA code");
        }

        // Complete login
        AdminUserPrincipal userPrincipal = AdminUserPrincipal.create(user);
        LoginResponse response = completeLogin(user, userPrincipal);
        
        // Remove MFA token
        removeMfaToken(request.getMfaToken());
        
        return response;
    }

    public void logout(String token) {
        logger.info("User logout");
        
        if (tokenProvider.validateToken(token)) {
            // Add token to blacklist (Redis)
            UUID userId = tokenProvider.getUserIdFromToken(token);
            LocalDateTime expiration = tokenProvider.getExpirationFromToken(token);
            long ttl = java.time.Duration.between(LocalDateTime.now(), expiration).toMillis();
            
            if (ttl > 0) {
                redisTemplate.opsForValue().set("blacklisted_token:" + token, userId.toString(), 
                                              ttl, TimeUnit.MILLISECONDS);
            }
        }
    }

    private LoginResponse completeLogin(AdminUser user, AdminUserPrincipal userPrincipal) {
        // Generate JWT token
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());
        String jwt = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        adminUserRepository.save(user);

        logger.info("User successfully authenticated: {}", user.getUsername());

        return LoginResponse.success(jwt, refreshToken, userPrincipal);
    }

    private void handleFailedLogin(AdminUser user) {
        user.incrementFailedAttempts();
        
        if (user.getFailedLoginAttempts() >= maxFailedAttempts) {
            LocalDateTime lockUntil = LocalDateTime.now().plusNanos(lockoutDurationMs * 1_000_000);
            user.lockAccount(lockUntil);
            logger.warn("Account locked for user: {} until {}", user.getUsername(), lockUntil);
        }
        
        adminUserRepository.save(user);
    }

    private String generateMfaToken(UUID userId) {
        String mfaToken = java.util.UUID.randomUUID().toString();
        String key = MFA_TOKEN_PREFIX + mfaToken;
        redisTemplate.opsForValue().set(key, userId.toString(), 5, TimeUnit.MINUTES);
        return mfaToken;
    }

    private boolean isMfaTokenValid(String mfaToken) {
        String key = MFA_TOKEN_PREFIX + mfaToken;
        return redisTemplate.hasKey(key);
    }

    private AdminUser getUserFromMfaToken(String mfaToken) {
        String key = MFA_TOKEN_PREFIX + mfaToken;
        String userIdStr = (String) redisTemplate.opsForValue().get(key);
        
        if (userIdStr == null) {
            return null;
        }
        
        UUID userId = UUID.fromString(userIdStr);
        return adminUserRepository.findById(userId).orElse(null);
    }

    private void removeMfaToken(String mfaToken) {
        String key = MFA_TOKEN_PREFIX + mfaToken;
        redisTemplate.delete(key);
    }

    public boolean isTokenBlacklisted(String token) {
        try {
            String key = "blacklisted_token:" + token;
            logger.debug("Checking if token is blacklisted: {}", key);
            Boolean hasKey = redisTemplate.hasKey(key);
            
            if (hasKey == null) {
                logger.warn("Redis connection issue: hasKey returned null for {}", key);
                // If Redis is not responding properly, allow the request to proceed
                return false;
            }
            
            return hasKey;
        } catch (Exception e) {
            logger.error("Error checking blacklisted token: {}", e.getMessage(), e);
            // If Redis is not available, assume token is not blacklisted
            return false;
        }
    }
}
