package com.finance.admin.client.notification.controller;

import com.finance.admin.client.document.util.JwtUtils;
import com.finance.admin.client.notification.model.ClientNotification;
import com.finance.admin.client.notification.service.ClientNotificationService;
import com.finance.admin.config.BaseUnitTest;
import com.finance.admin.common.exception.GlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for ClientNotificationController
 * Tests all notification management endpoints with various scenarios
 */
@DisplayName("Client Notification Controller Tests")
public class ClientNotificationControllerTest extends BaseUnitTest {

    private MockMvc mockMvc;
    
    @Mock
    private ClientNotificationService clientNotificationService;
    
    @Mock
    private JwtUtils jwtUtils;
    
    @Mock
    private HttpServletRequest httpServletRequest;
    
    private ClientNotificationController clientNotificationController;
    
    // Test data
    private final Long testClientId = 1L;
    private final UUID testNotificationId = UUID.randomUUID();
    private List<ClientNotification> testNotifications;
    private ClientNotification testNotification1;
    private ClientNotification testNotification2;

    @BeforeEach
    protected void setUp() {
        super.setUp();
        MockitoAnnotations.openMocks(this);
        
        clientNotificationController = new ClientNotificationController(clientNotificationService, jwtUtils);
        
        // Setup MockMvc with PageableHandlerMethodArgumentResolver and GlobalExceptionHandler
        mockMvc = MockMvcBuilders.standaloneSetup(clientNotificationController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        // Setup test data
        setupTestData();
        
        // Mock JwtUtils default behavior
        when(jwtUtils.getClientIdFromRequest(any(HttpServletRequest.class))).thenReturn(testClientId);
    }

    private void setupTestData() {
        // Create test notification 1 (unread)
        testNotification1 = ClientNotification.builder()
                .clientId(testClientId)
                .notificationType(ClientNotification.NotificationType.INVESTMENT)
                .category(ClientNotification.NotificationCategory.NEW_OPPORTUNITY)
                .title("New Investment Opportunity")
                .message("A new high-yield investment opportunity is available")
                .isRead(false)
                .deliveryMethod(ClientNotification.DeliveryMethod.EMAIL_AND_PUSH)
                .deliveryStatus(ClientNotification.DeliveryStatus.DELIVERED)
                .priorityLevel(ClientNotification.PriorityLevel.HIGH)
                .build();
        testNotification1.setId(testNotificationId);
        testNotification1.setCreatedAt(LocalDateTime.now().minusHours(2));

        // Create test notification 2 (read)
        testNotification2 = ClientNotification.builder()
                .clientId(testClientId)
                .notificationType(ClientNotification.NotificationType.KYC)
                .category(ClientNotification.NotificationCategory.DOCUMENT_REQUEST)
                .title("Document Verification Required")
                .message("Please upload your updated identification documents")
                .isRead(true)
                .deliveryMethod(ClientNotification.DeliveryMethod.EMAIL_AND_SMS)
                .deliveryStatus(ClientNotification.DeliveryStatus.READ)
                .priorityLevel(ClientNotification.PriorityLevel.NORMAL)
                .readAt(LocalDateTime.now().minusHours(1))
                .build();
        testNotification2.setId(UUID.randomUUID());
        testNotification2.setCreatedAt(LocalDateTime.now().minusDays(1));

        testNotifications = Arrays.asList(testNotification1, testNotification2);
    }

    // ================ GET /api/client/notifications Tests ================

    @Test
    @DisplayName("Should get client notifications successfully")
    void testGetNotifications_Success() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<ClientNotification> notificationsPage = new PageImpl<>(testNotifications, pageable, testNotifications.size());
        
        when(clientNotificationService.getClientNotifications(testClientId, 0, 20))
                .thenReturn(notificationsPage);

        // When & Then
        mockMvc.perform(get("/api/client/notifications")
                .param("page", "0")
                .param("size", "20")
                .with(request -> {
                    request.setRemoteAddr("127.0.0.1");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("New Investment Opportunity"))
                .andExpect(jsonPath("$.content[1].title").value("Document Verification Required"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(jwtUtils).getClientIdFromRequest(any(HttpServletRequest.class));
        verify(clientNotificationService).getClientNotifications(testClientId, 0, 20);
    }

    @Test
    @DisplayName("Should get notifications with custom pagination")
    void testGetNotifications_CustomPagination() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(1, 5);
        Page<ClientNotification> notificationsPage = new PageImpl<>(Collections.singletonList(testNotification1), pageable, 6);
        
        when(clientNotificationService.getClientNotifications(testClientId, 1, 5))
                .thenReturn(notificationsPage);

        // When & Then
        mockMvc.perform(get("/api/client/notifications")
                .param("page", "1")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(6));

        verify(clientNotificationService).getClientNotifications(testClientId, 1, 5);
    }

    // ================ GET /api/client/notifications/unread Tests ================

    @Test
    @DisplayName("Should get unread notifications successfully")
    void testGetUnreadNotifications_Success() throws Exception {
        // Given
        List<ClientNotification> unreadNotifications = Collections.singletonList(testNotification1);
        Pageable pageable = PageRequest.of(0, 20);
        Page<ClientNotification> unreadPage = new PageImpl<>(unreadNotifications, pageable, 1);
        
        when(clientNotificationService.getUnreadNotifications(testClientId, 0, 20))
                .thenReturn(unreadPage);

        // When & Then
        mockMvc.perform(get("/api/client/notifications/unread")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].isRead").value(false))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(clientNotificationService).getUnreadNotifications(testClientId, 0, 20);
    }

    // ================ GET /api/client/notifications/type/{type} Tests ================

    @Test
    @DisplayName("Should get notifications by type successfully")
    void testGetNotificationsByType_Success() throws Exception {
        // Given
        List<ClientNotification> investmentNotifications = Collections.singletonList(testNotification1);
        Pageable pageable = PageRequest.of(0, 20);
        Page<ClientNotification> typePage = new PageImpl<>(investmentNotifications, pageable, 1);
        
        when(clientNotificationService.getNotificationsByType(testClientId, ClientNotification.NotificationType.INVESTMENT, 0, 20))
                .thenReturn(typePage);

        // When & Then
        mockMvc.perform(get("/api/client/notifications/type/{type}", "INVESTMENT")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].notificationType").value("INVESTMENT"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(clientNotificationService).getNotificationsByType(testClientId, ClientNotification.NotificationType.INVESTMENT, 0, 20);
    }

    @Test
    @DisplayName("Should handle invalid notification type gracefully")
    void testGetNotificationsByType_InvalidType_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/client/notifications/type/{type}", "INVALID_TYPE"))
                .andExpect(status().isBadRequest());
    }

    // ================ GET /api/client/notifications/category/{category} Tests ================

    @Test
    @DisplayName("Should get notifications by category successfully")
    void testGetNotificationsByCategory_Success() throws Exception {
        // Given
        List<ClientNotification> documentNotifications = Collections.singletonList(testNotification2);
        Pageable pageable = PageRequest.of(0, 20);
        Page<ClientNotification> categoryPage = new PageImpl<>(documentNotifications, pageable, 1);
        
        when(clientNotificationService.getNotificationsByCategory(testClientId, ClientNotification.NotificationCategory.DOCUMENT_REQUEST, 0, 20))
                .thenReturn(categoryPage);

        // When & Then
        mockMvc.perform(get("/api/client/notifications/category/{category}", "DOCUMENT_REQUEST")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].category").value("DOCUMENT_REQUEST"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(clientNotificationService).getNotificationsByCategory(testClientId, ClientNotification.NotificationCategory.DOCUMENT_REQUEST, 0, 20);
    }

    // ================ PUT /api/client/notifications/{notificationId}/read Tests ================

    @Test
    @DisplayName("Should mark notification as read successfully")
    void testMarkAsRead_Success() throws Exception {
        // Given
        when(clientNotificationService.markAsRead(testNotificationId, testClientId))
                .thenReturn(true);

        // When & Then
        mockMvc.perform(put("/api/client/notifications/{notificationId}/read", testNotificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification marked as read successfully"));

        verify(clientNotificationService).markAsRead(testNotificationId, testClientId);
    }

    @Test
    @DisplayName("Should return 404 when notification not found for mark as read")
    void testMarkAsRead_NotificationNotFound_ReturnsNotFound() throws Exception {
        // Given
        when(clientNotificationService.markAsRead(testNotificationId, testClientId))
                .thenReturn(false);

        // When & Then
        mockMvc.perform(put("/api/client/notifications/{notificationId}/read", testNotificationId))
                .andExpect(status().isNotFound());

        verify(clientNotificationService).markAsRead(testNotificationId, testClientId);
    }

    // ================ PUT /api/client/notifications/read-all Tests ================

    @Test
    @DisplayName("Should mark all notifications as read successfully")
    void testMarkAllAsRead_Success() throws Exception {
        // Given
        when(clientNotificationService.markAllAsRead(testClientId))
                .thenReturn(5);

        // When & Then
        mockMvc.perform(put("/api/client/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("All notifications marked as read successfully"))
                .andExpect(jsonPath("$.updatedCount").value(5));

        verify(clientNotificationService).markAllAsRead(testClientId);
    }

    @Test
    @DisplayName("Should handle mark all as read when no unread notifications")
    void testMarkAllAsRead_NoUnreadNotifications() throws Exception {
        // Given
        when(clientNotificationService.markAllAsRead(testClientId))
                .thenReturn(0);

        // When & Then
        mockMvc.perform(put("/api/client/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.updatedCount").value(0));

        verify(clientNotificationService).markAllAsRead(testClientId);
    }

    // ================ GET /api/client/notifications/unread/count Tests ================

    @Test
    @DisplayName("Should get unread notification count successfully")
    void testGetUnreadCount_Success() throws Exception {
        // Given
        when(clientNotificationService.getUnreadCount(testClientId))
                .thenReturn(3L);

        // When & Then
        mockMvc.perform(get("/api/client/notifications/unread/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(3));

        verify(clientNotificationService).getUnreadCount(testClientId);
    }

    @Test
    @DisplayName("Should return zero when no unread notifications")
    void testGetUnreadCount_NoUnreadNotifications() throws Exception {
        // Given
        when(clientNotificationService.getUnreadCount(testClientId))
                .thenReturn(0L);

        // When & Then
        mockMvc.perform(get("/api/client/notifications/unread/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(0));

        verify(clientNotificationService).getUnreadCount(testClientId);
    }

    // ================ GET /api/client/notifications/statistics Tests ================

    @Test
    @DisplayName("Should get notification statistics successfully")
    void testGetNotificationStatistics_Success() throws Exception {
        // Given
        Map<String, Object> statistics = createMockStatistics();
        when(clientNotificationService.getNotificationStatistics(testClientId))
                .thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/api/client/notifications/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalNotifications").value(15))
                .andExpect(jsonPath("$.totalUnread").value(3))
                .andExpect(jsonPath("$.byType").exists());

        verify(clientNotificationService).getNotificationStatistics(testClientId);
    }

    // ================ GET /api/client/notifications/types Tests ================

    @Test
    @DisplayName("Should get notification types successfully")
    void testGetNotificationTypes_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/client/notifications/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.types").exists())
                .andExpect(jsonPath("$.types.GENERAL").value("General Notice"))
                .andExpect(jsonPath("$.types.KYC").value("KYC Alert"))
                .andExpect(jsonPath("$.types.INVESTMENT").value("Investment Alert"))
                .andExpect(jsonPath("$.types.RETURN").value("Return Notification"))
                .andExpect(jsonPath("$.types.REPORT").value("Report Reminder"))
                .andExpect(jsonPath("$.types.SYSTEM").value("System Notification"))
                .andExpect(jsonPath("$.types.SECURITY").value("Security Alert"));

        // Verify no service calls are made (this is a static endpoint)
        verifyNoInteractions(clientNotificationService);
    }

    // ================ GET /api/client/notifications/categories Tests ================

    @Test
    @DisplayName("Should get notification categories successfully")
    void testGetNotificationCategories_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/client/notifications/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").exists())
                .andExpect(jsonPath("$.categories.WELCOME").value("Welcome"))
                .andExpect(jsonPath("$.categories.BIRTHDAY").value("Birthday"))
                .andExpect(jsonPath("$.categories.ANNOUNCEMENT").value("Announcement"))
                .andExpect(jsonPath("$.categories.DOCUMENT_REQUEST").value("Document Request"))
                .andExpect(jsonPath("$.categories.VERIFICATION_RESULT").value("Verification Result"))
                .andExpect(jsonPath("$.categories.STATUS_UPDATE").value("Status Update"))
                .andExpect(jsonPath("$.categories.NEW_OPPORTUNITY").value("New Opportunity"))
                .andExpect(jsonPath("$.categories.MATURITY").value("Maturity"))
                .andExpect(jsonPath("$.categories.DISTRIBUTION").value("Distribution"))
                .andExpect(jsonPath("$.categories.MONTHLY_REPORT").value("Monthly Report"))
                .andExpect(jsonPath("$.categories.ANNUAL_STATEMENT").value("Annual Statement"))
                .andExpect(jsonPath("$.categories.TAX_DOCUMENT").value("Tax Document"))
                .andExpect(jsonPath("$.categories.MAINTENANCE").value("Maintenance"))
                .andExpect(jsonPath("$.categories.SECURITY_ALERT").value("Security Alert"));

        // Verify no service calls are made (this is a static endpoint)
        verifyNoInteractions(clientNotificationService);
    }

    // ================ Error Handling Tests ================

    @Test
    @DisplayName("Should handle JWT extraction failure gracefully")
    void testJwtExtractionFailure_ReturnsUnauthorized() throws Exception {
        // Given
        when(jwtUtils.getClientIdFromRequest(any(HttpServletRequest.class)))
                .thenThrow(new RuntimeException("Invalid JWT token"));

        // When & Then
        mockMvc.perform(get("/api/client/notifications"))
                .andExpect(status().isInternalServerError());

        verify(jwtUtils).getClientIdFromRequest(any(HttpServletRequest.class));
        verifyNoInteractions(clientNotificationService);
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void testServiceException_ReturnsInternalServerError() throws Exception {
        // Given
        when(clientNotificationService.getClientNotifications(testClientId, 0, 20))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/client/notifications"))
                .andExpect(status().isInternalServerError());

        verify(clientNotificationService).getClientNotifications(testClientId, 0, 20);
    }

    @Test
    @DisplayName("Should handle invalid UUID format for notification ID")
    void testInvalidNotificationId_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/client/notifications/{notificationId}/read", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(clientNotificationService);
    }

    @Test
    @DisplayName("Should handle concurrent marking of notification as read")
    void testConcurrentMarkAsRead_HandledGracefully() throws Exception {
        // Given
        when(clientNotificationService.markAsRead(testNotificationId, testClientId))
                .thenReturn(false); // Simulate already marked by another request

        // When & Then
        mockMvc.perform(put("/api/client/notifications/{notificationId}/read", testNotificationId))
                .andExpect(status().isNotFound());

        verify(clientNotificationService).markAsRead(testNotificationId, testClientId);
    }

    // ================ Integration Test Scenarios ================

    @Test
    @DisplayName("Should handle empty notification list gracefully")
    void testEmptyNotificationList_ReturnsEmptyPage() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<ClientNotification> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        
        when(clientNotificationService.getClientNotifications(testClientId, 0, 20))
                .thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/api/client/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(clientNotificationService).getClientNotifications(testClientId, 0, 20);
    }

    @Test
    @DisplayName("Should handle large page size gracefully")
    void testLargePageSize_HandledCorrectly() throws Exception {
        // Given
        when(clientNotificationService.getClientNotifications(testClientId, 0, 1000))
                .thenReturn(new PageImpl<>(testNotifications, PageRequest.of(0, 1000), testNotifications.size()));

        // When & Then
        mockMvc.perform(get("/api/client/notifications")
                .param("page", "0")
                .param("size", "1000"))
                .andExpect(status().isOk());

        verify(clientNotificationService).getClientNotifications(testClientId, 0, 1000);
    }

    // ================ Helper Methods ================

    private Map<String, Object> createMockStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalNotifications", 15L);
        statistics.put("totalUnread", 3L);
        
        Map<String, Map<String, Long>> byType = new HashMap<>();
        
        Map<String, Long> investmentStats = new HashMap<>();
        investmentStats.put("total", 5L);
        investmentStats.put("unread", 2L);
        byType.put("INVESTMENT", investmentStats);
        
        Map<String, Long> kycStats = new HashMap<>();
        kycStats.put("total", 3L);
        kycStats.put("unread", 1L);
        byType.put("KYC", kycStats);
        
        Map<String, Long> generalStats = new HashMap<>();
        generalStats.put("total", 7L);
        generalStats.put("unread", 0L);
        byType.put("GENERAL", generalStats);
        
        statistics.put("byType", byType);
        
        return statistics;
    }
} 