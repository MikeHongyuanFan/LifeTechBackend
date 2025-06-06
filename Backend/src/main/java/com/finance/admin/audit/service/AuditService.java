package com.finance.admin.audit.service;

import com.finance.admin.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AuditService {
    AuditLog saveAuditLog(AuditLog auditLog);
    
    Page<AuditLog> findAuditLogs(
        UUID userId,
        String userType,
        String actionType,
        String entityType,
        UUID entityId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String status,
        Pageable pageable
    );
    
    List<AuditLog> exportAuditLogs(
        UUID userId,
        String userType,
        String actionType,
        String entityType,
        LocalDateTime startDate,
        LocalDateTime endDate
    );
    
    void cleanupOldAuditLogs();
    
    List<AuditLog> findUserActionHistory(UUID userId, int limit);
    
    List<AuditLog> findEntityHistory(String entityType, UUID entityId);
} 
