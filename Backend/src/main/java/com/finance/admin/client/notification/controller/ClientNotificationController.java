package com.finance.admin.client.notification.controller;

import com.finance.admin.client.notification.model.ClientNotification;
import com.finance.admin.client.notification.service.ClientNotificationService;
import com.finance.admin.client.document.util.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/client/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Client Notifications", description = "Client notification management APIs")
public class ClientNotificationController {

    private final ClientNotificationService notificationService;
    private final JwtUtils jwtUtils;

    @Operation(summary = "Get client notifications", 
               description = "Retrieve paginated list of notifications for the authenticated client")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Page<ClientNotification>> getNotifications(
            HttpServletRequest request,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Get notifications request received - page: {}, size: {}", page, size);

        Long clientId = jwtUtils.getClientIdFromRequest(request);
        Page<ClientNotification> notifications = notificationService.getClientNotifications(clientId, page, size);

        log.info("Retrieved {} notifications for client ID: {}", notifications.getTotalElements(), clientId);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get unread notifications", 
               description = "Retrieve paginated list of unread notifications for the authenticated client")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Unread notifications retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/unread")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Page<ClientNotification>> getUnreadNotifications(
            HttpServletRequest request,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Get unread notifications request received - page: {}, size: {}", page, size);

        Long clientId = jwtUtils.getClientIdFromRequest(request);
        Page<ClientNotification> notifications = notificationService.getUnreadNotifications(clientId, page, size);

        log.info("Retrieved {} unread notifications for client ID: {}", notifications.getTotalElements(), clientId);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get notifications by type", 
               description = "Retrieve paginated list of notifications filtered by type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid notification type"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Page<ClientNotification>> getNotificationsByType(
            HttpServletRequest request,
            @Parameter(description = "Notification type") @PathVariable ClientNotification.NotificationType type,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Get notifications by type request received - type: {}, page: {}, size: {}", type, page, size);

        Long clientId = jwtUtils.getClientIdFromRequest(request);
        Page<ClientNotification> notifications = notificationService.getNotificationsByType(clientId, type, page, size);

        log.info("Retrieved {} notifications of type {} for client ID: {}", notifications.getTotalElements(), type, clientId);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get notifications by category", 
               description = "Retrieve paginated list of notifications filtered by category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid notification category"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Page<ClientNotification>> getNotificationsByCategory(
            HttpServletRequest request,
            @Parameter(description = "Notification category") @PathVariable ClientNotification.NotificationCategory category,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Get notifications by category request received - category: {}, page: {}, size: {}", category, page, size);

        Long clientId = jwtUtils.getClientIdFromRequest(request);
        Page<ClientNotification> notifications = notificationService.getNotificationsByCategory(clientId, category, page, size);

        log.info("Retrieved {} notifications of category {} for client ID: {}", notifications.getTotalElements(), category, clientId);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Mark notification as read", 
               description = "Mark a specific notification as read")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification marked as read successfully"),
        @ApiResponse(responseCode = "404", description = "Notification not found or doesn't belong to client"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Map<String, Object>> markAsRead(
            HttpServletRequest request,
            @Parameter(description = "Notification ID") @PathVariable UUID notificationId) {
        
        log.info("Mark notification as read request received - notificationId: {}", notificationId);

        Long clientId = jwtUtils.getClientIdFromRequest(request);
        boolean success = notificationService.markAsRead(notificationId, clientId);

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "Notification marked as read successfully");
            log.info("Notification {} marked as read for client ID: {}", notificationId, clientId);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Notification not found or already read");
            log.warn("Failed to mark notification {} as read for client ID: {}", notificationId, clientId);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Mark all notifications as read", 
               description = "Mark all notifications as read for the authenticated client")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "All notifications marked as read successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PutMapping("/read-all")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Map<String, Object>> markAllAsRead(HttpServletRequest request) {
        
        log.info("Mark all notifications as read request received");

        Long clientId = jwtUtils.getClientIdFromRequest(request);
        int updatedCount = notificationService.markAllAsRead(clientId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "All notifications marked as read successfully");
        response.put("updatedCount", updatedCount);

        log.info("Marked {} notifications as read for client ID: {}", updatedCount, clientId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get unread notification count", 
               description = "Get the count of unread notifications for the authenticated client")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Unread count retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/unread/count")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Map<String, Object>> getUnreadCount(HttpServletRequest request) {
        
        log.info("Get unread notification count request received");

        Long clientId = jwtUtils.getClientIdFromRequest(request);
        long unreadCount = notificationService.getUnreadCount(clientId);

        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", unreadCount);

        log.info("Unread notification count for client ID {}: {}", clientId, unreadCount);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get notification statistics", 
               description = "Get detailed notification statistics for the authenticated client")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Map<String, Object>> getNotificationStatistics(HttpServletRequest request) {
        
        log.info("Get notification statistics request received");

        Long clientId = jwtUtils.getClientIdFromRequest(request);
        Map<String, Object> statistics = notificationService.getNotificationStatistics(clientId);

        log.info("Retrieved notification statistics for client ID: {}", clientId);
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Get notification types", 
               description = "Get all available notification types")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification types retrieved successfully")
    })
    @GetMapping("/types")
    public ResponseEntity<Map<String, Object>> getNotificationTypes() {
        
        log.info("Get notification types request received");

        Map<String, Object> response = new HashMap<>();
        Map<String, String> types = new HashMap<>();
        
        for (ClientNotification.NotificationType type : ClientNotification.NotificationType.values()) {
            types.put(type.name(), type.getDisplayName());
        }
        
        response.put("types", types);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get notification categories", 
               description = "Get all available notification categories")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification categories retrieved successfully")
    })
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getNotificationCategories() {
        
        log.info("Get notification categories request received");

        Map<String, Object> response = new HashMap<>();
        Map<String, String> categories = new HashMap<>();
        
        for (ClientNotification.NotificationCategory category : ClientNotification.NotificationCategory.values()) {
            categories.put(category.name(), category.getDisplayName());
        }
        
        response.put("categories", categories);
        
        return ResponseEntity.ok(response);
    }
} 