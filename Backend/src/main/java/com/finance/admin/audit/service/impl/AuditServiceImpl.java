package com.finance.admin.audit.service.impl;

import com.finance.admin.audit.entity.AuditLog;
import com.finance.admin.audit.repository.AuditLogRepository;
import com.finance.admin.audit.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AuditServiceImpl implements AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Value("${app.audit.retention-days}")
    private int retentionDays;

    @Override
    public AuditLog saveAuditLog(AuditLog auditLog) {
        logger.debug("Saving audit log: {}", auditLog);
        return auditLogRepository.save(auditLog);
    }

    @Override
    public Page<AuditLog> findAuditLogs(
            UUID userId,
            String userType,
            String actionType,
            String entityType,
            UUID entityId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String status,
            Pageable pageable) {
        
        Specification<AuditLog> spec = Specification.where(null);

        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }
        if (userType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userType"), userType));
        }
        if (actionType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("actionType"), actionType));
        }
        if (entityType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("entityType"), entityType));
        }
        if (entityId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("entityId"), entityId));
        }
        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startTime"), startDate));
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("endTime"), endDate));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        return auditLogRepository.findAll(spec, pageable);
    }

    @Override
    public List<AuditLog> exportAuditLogs(
            UUID userId,
            String userType,
            String actionType,
            String entityType,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        Specification<AuditLog> spec = Specification.where(null);

        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }
        if (userType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userType"), userType));
        }
        if (actionType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("actionType"), actionType));
        }
        if (entityType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("entityType"), entityType));
        }
        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startTime"), startDate));
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("endTime"), endDate));
        }

        return auditLogRepository.findAll(spec);
    }

    @Override
    @Scheduled(cron = "${app.audit.cleanup-schedule}")
    public void cleanupOldAuditLogs() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        logger.info("Cleaning up audit logs older than: {}", cutoffDate);
        
        try {
            int deletedCount = auditLogRepository.deleteByCreatedAtBefore(cutoffDate);
            logger.info("Deleted {} old audit logs", deletedCount);
        } catch (Exception e) {
            logger.error("Failed to cleanup old audit logs", e);
        }
    }

    @Override
    public List<AuditLog> findUserActionHistory(UUID userId, int limit) {
        return auditLogRepository.findByUserIdOrderByStartTimeDesc(userId, limit);
    }

    @Override
    public List<AuditLog> findEntityHistory(String entityType, UUID entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByStartTimeDesc(entityType, entityId);
    }
} 
