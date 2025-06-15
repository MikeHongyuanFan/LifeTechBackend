package com.finance.admin.client.notification.repository;

import com.finance.admin.client.notification.model.ClientNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ClientNotificationRepository extends JpaRepository<ClientNotification, UUID> {

    /**
     * Find all notifications for a specific client
     */
    Page<ClientNotification> findByClientIdOrderByCreatedAtDesc(Long clientId, Pageable pageable);

    /**
     * Find unread notifications for a client
     */
    Page<ClientNotification> findByClientIdAndIsReadFalseOrderByCreatedAtDesc(Long clientId, Pageable pageable);

    /**
     * Find notifications by type for a client
     */
    Page<ClientNotification> findByClientIdAndNotificationTypeOrderByCreatedAtDesc(
            Long clientId, ClientNotification.NotificationType notificationType, Pageable pageable);

    /**
     * Find notifications by category for a client
     */
    Page<ClientNotification> findByClientIdAndCategoryOrderByCreatedAtDesc(
            Long clientId, ClientNotification.NotificationCategory category, Pageable pageable);

    /**
     * Find notifications by priority level
     */
    List<ClientNotification> findByClientIdAndPriorityLevelOrderByCreatedAtDesc(
            Long clientId, ClientNotification.PriorityLevel priorityLevel);

    /**
     * Find notifications scheduled for delivery
     */
    @Query("SELECT n FROM ClientNotification n WHERE n.scheduledAt <= :now AND n.deliveryStatus = 'PENDING'")
    List<ClientNotification> findScheduledNotifications(@Param("now") LocalDateTime now);

    /**
     * Find expired notifications
     */
    @Query("SELECT n FROM ClientNotification n WHERE n.expiresAt <= :now AND n.deliveryStatus != 'EXPIRED'")
    List<ClientNotification> findExpiredNotifications(@Param("now") LocalDateTime now);

    /**
     * Count unread notifications for a client
     */
    long countByClientIdAndIsReadFalse(Long clientId);

    /**
     * Count notifications by type for a client
     */
    long countByClientIdAndNotificationType(Long clientId, ClientNotification.NotificationType notificationType);

    /**
     * Mark notification as read
     */
    @Modifying
    @Query("UPDATE ClientNotification n SET n.isRead = true, n.readAt = :readAt, n.deliveryStatus = 'READ' WHERE n.id = :notificationId AND n.clientId = :clientId")
    int markAsRead(@Param("notificationId") UUID notificationId, @Param("clientId") Long clientId, @Param("readAt") LocalDateTime readAt);

    /**
     * Mark all notifications as read for a client
     */
    @Modifying
    @Query("UPDATE ClientNotification n SET n.isRead = true, n.readAt = :readAt WHERE n.clientId = :clientId AND n.isRead = false")
    int markAllAsRead(@Param("clientId") Long clientId, @Param("readAt") LocalDateTime readAt);

    /**
     * Mark notifications as delivered
     */
    @Modifying
    @Query("UPDATE ClientNotification n SET n.deliveryStatus = 'DELIVERED', n.deliveredAt = :deliveredAt WHERE n.id IN :notificationIds")
    int markAsDelivered(@Param("notificationIds") List<UUID> notificationIds, @Param("deliveredAt") LocalDateTime deliveredAt);

    /**
     * Mark expired notifications
     */
    @Modifying
    @Query("UPDATE ClientNotification n SET n.deliveryStatus = 'EXPIRED' WHERE n.expiresAt <= :now AND n.deliveryStatus != 'EXPIRED'")
    int markExpiredNotifications(@Param("now") LocalDateTime now);

    /**
     * Delete old notifications (cleanup)
     */
    @Modifying
    @Query("DELETE FROM ClientNotification n WHERE n.createdAt < :cutoffDate AND n.isRead = true")
    int deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find notifications by delivery status
     */
    List<ClientNotification> findByDeliveryStatusAndCreatedAtAfter(
            ClientNotification.DeliveryStatus deliveryStatus, LocalDateTime after);

    /**
     * Find high priority undelivered notifications
     */
    @Query("SELECT n FROM ClientNotification n WHERE n.priorityLevel IN ('HIGH', 'URGENT', 'CRITICAL') AND n.deliveryStatus = 'PENDING' ORDER BY n.priorityLevel DESC, n.createdAt ASC")
    List<ClientNotification> findHighPriorityPendingNotifications();

    /**
     * Find notifications for bulk operations
     */
    @Query("SELECT n FROM ClientNotification n WHERE n.clientId IN :clientIds AND n.notificationType = :type AND n.createdAt >= :fromDate")
    List<ClientNotification> findForBulkOperation(
            @Param("clientIds") List<Long> clientIds, 
            @Param("type") ClientNotification.NotificationType type,
            @Param("fromDate") LocalDateTime fromDate);

    /**
     * Get notification statistics for a client
     */
    @Query("SELECT n.notificationType, COUNT(n), SUM(CASE WHEN n.isRead = false THEN 1 ELSE 0 END) " +
           "FROM ClientNotification n WHERE n.clientId = :clientId GROUP BY n.notificationType")
    List<Object[]> getNotificationStatistics(@Param("clientId") Long clientId);
} 