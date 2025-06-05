package com.tycoon.admin.audit.aspect;

import com.tycoon.admin.audit.annotation.Audited;
import com.tycoon.admin.audit.entity.AuditLog;
import com.tycoon.admin.audit.service.AuditService;
import com.tycoon.admin.auth.security.AdminUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@Aspect
@Component
public class AuditLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditLoggingAspect.class);

    @Autowired
    private AuditService auditService;

    @Around("@annotation(audited)")
    public Object logAuditEvent(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        LocalDateTime startTime = LocalDateTime.now();
        Object result = null;
        Exception exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            try {
                recordAudit(joinPoint, audited, startTime, result, exception);
            } catch (Exception e) {
                logger.error("Failed to record audit log", e);
            }
        }
    }

    private void recordAudit(ProceedingJoinPoint joinPoint, Audited audited, 
                           LocalDateTime startTime, Object result, Exception exception) {
        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AdminUserPrincipal userPrincipal = authentication != null && authentication.getPrincipal() instanceof AdminUserPrincipal ?
                    (AdminUserPrincipal) authentication.getPrincipal() : null;

            // Get request details
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String sessionId = request.getSession(false) != null ? request.getSession(false).getId() : null;

            // Create audit log
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userPrincipal != null ? userPrincipal.getId() : null);
            auditLog.setUserType("admin");
            auditLog.setActionType(audited.action());
            auditLog.setEntityType(audited.entity());
            auditLog.setEntityId(extractEntityId(result));
            auditLog.setOldValues(null); // To be implemented with entity change tracking
            auditLog.setNewValues(null); // To be implemented with entity change tracking
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            auditLog.setSessionId(sessionId);
            auditLog.setMethodName(joinPoint.getSignature().getName());
            auditLog.setParameters(Arrays.toString(joinPoint.getArgs()));
            auditLog.setStartTime(startTime);
            auditLog.setEndTime(LocalDateTime.now());
            auditLog.setStatus(exception == null ? "SUCCESS" : "FAILURE");
            auditLog.setErrorMessage(exception != null ? exception.getMessage() : null);

            auditService.saveAuditLog(auditLog);
        } catch (Exception e) {
            logger.error("Failed to record audit log", e);
        }
    }

    private UUID extractEntityId(Object result) {
        if (result == null) {
            return null;
        }
        try {
            // Add logic to extract entity ID based on your entity structure
            return null;
        } catch (Exception e) {
            logger.warn("Failed to extract entity ID from result", e);
            return null;
        }
    }
} 