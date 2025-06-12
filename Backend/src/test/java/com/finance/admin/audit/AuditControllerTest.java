package com.finance.admin.audit;

import com.finance.admin.audit.controller.AuditController;
import com.finance.admin.audit.entity.AuditLog;
import com.finance.admin.audit.service.AuditService;
import com.finance.admin.config.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the Audit Management API endpoints
 * Using minimal standalone MockMvc configuration
 */
public class AuditControllerTest extends BaseUnitTest {

    private MockMvc mockMvc;
    private AuditService auditService;
    private AuditController auditController;
    private List<AuditLog> auditLogs;
    private UUID userId;
    private UUID entityId;

    @BeforeEach
    protected void setUp() {
        super.setUp(); // Call parent setup
        
        // Create mocks
        auditService = mock(AuditService.class);
        auditController = new AuditController();
        
        // Use reflection to inject the mocked service
        try {
            Field auditServiceField = AuditController.class.getDeclaredField("auditService");
            auditServiceField.setAccessible(true);
            auditServiceField.set(auditController, auditService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock service", e);
        }
        
        // Setup MockMvc with PageableHandlerMethodArgumentResolver
        mockMvc = MockMvcBuilders.standaloneSetup(auditController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        
        userId = UUID.randomUUID();
        entityId = UUID.randomUUID();
        
        // Create sample audit logs
        AuditLog log1 = new AuditLog();
        log1.setId(UUID.randomUUID());
        log1.setUserId(userId);
        log1.setUserType("ADMIN");
        log1.setActionType("CREATE");
        log1.setEntityType("USER");
        log1.setEntityId(entityId);
        log1.setStartTime(LocalDateTime.now());
        log1.setEndTime(LocalDateTime.now().plusSeconds(1));
        log1.setIpAddress("127.0.0.1");
        log1.setStatus("SUCCESS");
        
        AuditLog log2 = new AuditLog();
        log2.setId(UUID.randomUUID());
        log2.setUserId(userId);
        log2.setUserType("ADMIN");
        log2.setActionType("UPDATE");
        log2.setEntityType("USER");
        log2.setEntityId(entityId);
        log2.setStartTime(LocalDateTime.now().minusHours(1));
        log2.setEndTime(LocalDateTime.now().minusHours(1).plusSeconds(2));
        log2.setIpAddress("127.0.0.1");
        log2.setStatus("SUCCESS");
        
        auditLogs = Arrays.asList(log1, log2);
    }

    @Test
    void testGetAuditLogs() throws Exception {
        Page<AuditLog> page = new PageImpl<>(auditLogs, PageRequest.of(0, 10), 2);
        
        when(auditService.findAuditLogs(
                isNull(), isNull(), isNull(), isNull(), 
                isNull(), isNull(), isNull(), 
                isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/audit/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    void testGetUserActionHistory() throws Exception {
        when(auditService.findUserActionHistory(eq(userId), eq(100)))
                .thenReturn(auditLogs);

        mockMvc.perform(get("/audit/user/{userId}/history", userId)
                .param("limit", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testGetEntityHistory() throws Exception {
        when(auditService.findEntityHistory(eq("USER"), eq(entityId)))
                .thenReturn(auditLogs);
        
        mockMvc.perform(get("/audit/entity/{entityType}/{entityId}", "USER", entityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testTriggerCleanup() throws Exception {
        doNothing().when(auditService).cleanupOldAuditLogs();

        mockMvc.perform(post("/audit/cleanup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetAuditLogsWithFilters() throws Exception {
        Page<AuditLog> page = new PageImpl<>(auditLogs, PageRequest.of(0, 10), 2);
        
        when(auditService.findAuditLogs(
                eq(userId), eq("ADMIN"), eq("CREATE"), eq("USER"), 
                eq(entityId), any(), any(), 
                eq("SUCCESS"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/audit/logs")
                .param("userId", userId.toString())
                .param("userType", "ADMIN")
                .param("actionType", "CREATE")
                .param("entityType", "USER")
                .param("entityId", entityId.toString())
                .param("status", "SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    void testGetAuditLogsWithPagination() throws Exception {
        Page<AuditLog> page = new PageImpl<>(auditLogs, PageRequest.of(1, 5), 2);
        
        when(auditService.findAuditLogs(
                isNull(), isNull(), isNull(), isNull(), 
                isNull(), isNull(), isNull(), 
                isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/audit/logs")
                .param("page", "1")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }
}
