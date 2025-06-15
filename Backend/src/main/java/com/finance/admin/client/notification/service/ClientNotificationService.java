package com.finance.admin.client.notification.service;

import com.finance.admin.client.notification.model.ClientNotification;
import com.finance.admin.client.notification.repository.ClientNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClientNotificationService {

    private final ClientNotificationRepository notificationRepository;

    /**
     * Create and send a notification
     */
    public ClientNotification createNotification(Long clientId, 
                                                ClientNotification.NotificationType type,
                                                ClientNotification.NotificationCategory category,
                                                String title, 
                                                String message,
                                                ClientNotification.DeliveryMethod deliveryMethod,
                                                ClientNotification.PriorityLevel priority,
                                                LocalDateTime scheduledAt,
                                                LocalDateTime expiresAt,
                                                Map<String, Object> metadata) {
        
        log.debug("Creating notification for client ID: {}, type: {}, category: {}", clientId, type, category);

        ClientNotification notification = ClientNotification.builder()
                .clientId(clientId)
                .notificationType(type)
                .category(category)
                .title(title)
                .message(message)
                .deliveryMethod(deliveryMethod)
                .priorityLevel(priority)
                .scheduledAt(scheduledAt)
                .expiresAt(expiresAt)
                .metadata(metadata)
                .build();

        notification = notificationRepository.save(notification);

        // If not scheduled, send immediately
        if (scheduledAt == null || scheduledAt.isBefore(LocalDateTime.now().plusMinutes(1))) {
            sendNotification(notification);
        }

        log.info("Notification created with ID: {} for client: {}", notification.getId(), clientId);
        return notification;
    }

    /**
     * Create a simple notification with defaults
     */
    public ClientNotification createSimpleNotification(Long clientId, 
                                                      ClientNotification.NotificationType type,
                                                      String title, 
                                                      String message) {
        return createNotification(
                clientId, 
                type, 
                getDefaultCategory(type),
                title, 
                message,
                ClientNotification.DeliveryMethod.IN_APP,
                ClientNotification.PriorityLevel.NORMAL,
                null, // Send immediately
                LocalDateTime.now().plusDays(30), // Expire in 30 days
                null
        );
    }

    /**
     * Get notifications for a client with pagination
     */
    @Transactional(readOnly = true)
    public Page<ClientNotification> getClientNotifications(Long clientId, int page, int size) {
        log.debug("Getting notifications for client ID: {}, page: {}, size: {}", clientId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByClientIdOrderByCreatedAtDesc(clientId, pageable);
    }

    /**
     * Get unread notifications for a client
     */
    @Transactional(readOnly = true)
    public Page<ClientNotification> getUnreadNotifications(Long clientId, int page, int size) {
        log.debug("Getting unread notifications for client ID: {}", clientId);
        
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByClientIdAndIsReadFalseOrderByCreatedAtDesc(clientId, pageable);
    }

    /**
     * Get notifications by type
     */
    @Transactional(readOnly = true)
    public Page<ClientNotification> getNotificationsByType(Long clientId, 
                                                          ClientNotification.NotificationType type,
                                                          int page, int size) {
        log.debug("Getting notifications by type {} for client ID: {}", type, clientId);
        
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByClientIdAndNotificationTypeOrderByCreatedAtDesc(clientId, type, pageable);
    }

    /**
     * Get notifications by category
     */
    @Transactional(readOnly = true)
    public Page<ClientNotification> getNotificationsByCategory(Long clientId, 
                                                              ClientNotification.NotificationCategory category,
                                                              int page, int size) {
        log.debug("Getting notifications by category {} for client ID: {}", category, clientId);
        
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByClientIdAndCategoryOrderByCreatedAtDesc(clientId, category, pageable);
    }

    /**
     * Mark notification as read
     */
    public boolean markAsRead(UUID notificationId, Long clientId) {
        log.debug("Marking notification {} as read for client {}", notificationId, clientId);
        
        int updated = notificationRepository.markAsRead(notificationId, clientId, LocalDateTime.now());
        
        if (updated > 0) {
            log.info("Notification {} marked as read for client {}", notificationId, clientId);
            return true;
        } else {
            log.warn("Failed to mark notification {} as read for client {}", notificationId, clientId);
            return false;
        }
    }

    /**
     * Mark all notifications as read for a client
     */
    public int markAllAsRead(Long clientId) {
        log.debug("Marking all notifications as read for client {}", clientId);
        
        int updated = notificationRepository.markAllAsRead(clientId, LocalDateTime.now());
        
        log.info("Marked {} notifications as read for client {}", updated, clientId);
        return updated;
    }

    /**
     * Get unread notification count
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long clientId) {
        return notificationRepository.countByClientIdAndIsReadFalse(clientId);
    }

    /**
     * Get notification statistics for a client
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getNotificationStatistics(Long clientId) {
        log.debug("Getting notification statistics for client {}", clientId);
        
        List<Object[]> stats = notificationRepository.getNotificationStatistics(clientId);
        Map<String, Object> result = new HashMap<>();
        
        long totalNotifications = 0;
        long totalUnread = 0;
        Map<String, Map<String, Long>> typeStats = new HashMap<>();
        
        for (Object[] stat : stats) {
            String type = stat[0].toString();
            Long count = (Long) stat[1];
            Long unreadCount = (Long) stat[2];
            
            totalNotifications += count;
            totalUnread += unreadCount;
            
            Map<String, Long> typeStat = new HashMap<>();
            typeStat.put("total", count);
            typeStat.put("unread", unreadCount);
            typeStats.put(type, typeStat);
        }
        
        result.put("totalNotifications", totalNotifications);
        result.put("totalUnread", totalUnread);
        result.put("byType", typeStats);
        
        return result;
    }

    /**
     * Send notification (placeholder for actual delivery implementation)
     */
    @Async
    public void sendNotification(ClientNotification notification) {
        log.debug("Sending notification {} via {}", notification.getId(), notification.getDeliveryMethod());
        
        try {
            // Simulate delivery based on method
            switch (notification.getDeliveryMethod()) {
                case IN_APP -> {
                    // In-app notifications are already stored in database
                    notification.markAsDelivered();
                    log.debug("In-app notification delivered: {}", notification.getId());
                }
                case EMAIL -> {
                    // TODO: Integrate with email service
                    simulateEmailDelivery(notification);
                    notification.markAsDelivered();
                    log.debug("Email notification delivered: {}", notification.getId());
                }
                case SMS -> {
                    // TODO: Integrate with SMS service
                    simulateSmsDelivery(notification);
                    notification.markAsDelivered();
                    log.debug("SMS notification delivered: {}", notification.getId());
                }
                case PUSH -> {
                    // TODO: Integrate with push notification service
                    simulatePushDelivery(notification);
                    notification.markAsDelivered();
                    log.debug("Push notification delivered: {}", notification.getId());
                }
                default -> {
                    log.warn("Unsupported delivery method: {}", notification.getDeliveryMethod());
                    notification.setDeliveryStatus(ClientNotification.DeliveryStatus.FAILED);
                }
            }
            
            notificationRepository.save(notification);
            
        } catch (Exception e) {
            log.error("Failed to send notification {}: {}", notification.getId(), e.getMessage());
            notification.setDeliveryStatus(ClientNotification.DeliveryStatus.FAILED);
            notificationRepository.save(notification);
        }
    }

    /**
     * Process scheduled notifications
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void processScheduledNotifications() {
        log.debug("Processing scheduled notifications");
        
        List<ClientNotification> scheduledNotifications = 
                notificationRepository.findScheduledNotifications(LocalDateTime.now());
        
        for (ClientNotification notification : scheduledNotifications) {
            sendNotification(notification);
        }
        
        if (!scheduledNotifications.isEmpty()) {
            log.info("Processed {} scheduled notifications", scheduledNotifications.size());
        }
    }

    /**
     * Process expired notifications
     */
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void processExpiredNotifications() {
        log.debug("Processing expired notifications");
        
        int expiredCount = notificationRepository.markExpiredNotifications(LocalDateTime.now());
        
        if (expiredCount > 0) {
            log.info("Marked {} notifications as expired", expiredCount);
        }
    }

    /**
     * Cleanup old read notifications
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void cleanupOldNotifications() {
        log.debug("Cleaning up old notifications");
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90); // Keep for 90 days
        int deletedCount = notificationRepository.deleteOldReadNotifications(cutoffDate);
        
        if (deletedCount > 0) {
            log.info("Deleted {} old read notifications", deletedCount);
        }
    }

    // Helper methods for delivery simulation
    private void simulateEmailDelivery(ClientNotification notification) {
        // TODO: Implement actual email delivery
        log.debug("Simulating email delivery for notification: {}", notification.getId());
        // Add delay to simulate email sending
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void simulateSmsDelivery(ClientNotification notification) {
        // TODO: Implement actual SMS delivery
        log.debug("Simulating SMS delivery for notification: {}", notification.getId());
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void simulatePushDelivery(ClientNotification notification) {
        // TODO: Implement actual push notification delivery
        log.debug("Simulating push delivery for notification: {}", notification.getId());
        try {
            Thread.sleep(25);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private ClientNotification.NotificationCategory getDefaultCategory(ClientNotification.NotificationType type) {
        return switch (type) {
            case GENERAL -> ClientNotification.NotificationCategory.ANNOUNCEMENT;
            case KYC -> ClientNotification.NotificationCategory.VERIFICATION_RESULT;
            case INVESTMENT -> ClientNotification.NotificationCategory.NEW_OPPORTUNITY;
            case RETURN -> ClientNotification.NotificationCategory.DISTRIBUTION;
            case REPORT -> ClientNotification.NotificationCategory.MONTHLY_REPORT;
            case SYSTEM -> ClientNotification.NotificationCategory.MAINTENANCE;
            case SECURITY -> ClientNotification.NotificationCategory.SECURITY_ALERT;
        };
    }
} 