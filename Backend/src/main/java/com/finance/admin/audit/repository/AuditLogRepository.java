package com.finance.admin.audit.repository;

import com.finance.admin.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {
    
    @Query(value = "SELECT a FROM AuditLog a WHERE a.userId = :userId ORDER BY a.startTime DESC LIMIT :limit")
    List<AuditLog> findByUserIdOrderByStartTimeDesc(@Param("userId") UUID userId, @Param("limit") int limit);
    
    List<AuditLog> findByEntityTypeAndEntityIdOrderByStartTimeDesc(String entityType, UUID entityId);
    
    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.createdAt < :cutoffDate")
    int deleteByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    Page<AuditLog> findByUserIdAndActionType(UUID userId, String actionType, Pageable pageable);
    
    Page<AuditLog> findByEntityTypeAndActionType(String entityType, String actionType, Pageable pageable);
    
    @Query("SELECT DISTINCT a.actionType FROM AuditLog a WHERE a.entityType = :entityType")
    List<String> findDistinctActionTypesByEntityType(@Param("entityType") String entityType);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userId = :userId AND a.startTime >= :since")
    long countUserActionsInPeriod(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
} 
