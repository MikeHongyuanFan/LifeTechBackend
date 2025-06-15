package com.finance.admin.client.notification.model;

import com.finance.admin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "client_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientNotification extends BaseEntity {

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private NotificationCategory category;

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method")
    @Builder.Default
    private DeliveryMethod deliveryMethod = DeliveryMethod.IN_APP;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status")
    @Builder.Default
    private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority_level")
    @Builder.Default
    private PriorityLevel priorityLevel = PriorityLevel.NORMAL;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "metadata", columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private Map<String, Object> metadata;

    /**
     * Notification types for different categories
     */
    public enum NotificationType {
        GENERAL("General Notice"),
        KYC("KYC Alert"),
        INVESTMENT("Investment Alert"),
        RETURN("Return Notification"),
        REPORT("Report Reminder"),
        SYSTEM("System Notification"),
        SECURITY("Security Alert");

        private final String displayName;

        NotificationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Notification categories for organization
     */
    public enum NotificationCategory {
        WELCOME("Welcome"),
        BIRTHDAY("Birthday"),
        ANNOUNCEMENT("Announcement"),
        DOCUMENT_REQUEST("Document Request"),
        VERIFICATION_RESULT("Verification Result"),
        STATUS_UPDATE("Status Update"),
        NEW_OPPORTUNITY("New Opportunity"),
        MATURITY("Maturity"),
        DISTRIBUTION("Distribution"),
        MONTHLY_REPORT("Monthly Report"),
        ANNUAL_STATEMENT("Annual Statement"),
        TAX_DOCUMENT("Tax Document"),
        MAINTENANCE("Maintenance"),
        SECURITY_ALERT("Security Alert");

        private final String displayName;

        NotificationCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Delivery methods for notifications
     */
    public enum DeliveryMethod {
        IN_APP("In-App"),
        EMAIL("Email"),
        SMS("SMS"),
        PUSH("Push Notification"),
        EMAIL_AND_PUSH("Email & Push"),
        EMAIL_AND_SMS("Email & SMS"),
        PUSH_AND_EMAIL("Push & Email"),
        ALL("All Methods");

        private final String displayName;

        DeliveryMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Delivery status tracking
     */
    public enum DeliveryStatus {
        PENDING("Pending"),
        SENT("Sent"),
        DELIVERED("Delivered"),
        READ("Read"),
        FAILED("Failed"),
        EXPIRED("Expired"),
        CANCELLED("Cancelled");

        private final String displayName;

        DeliveryStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Priority levels for notifications
     */
    public enum PriorityLevel {
        LOW("Low"),
        NORMAL("Normal"),
        HIGH("High"),
        URGENT("Urgent"),
        CRITICAL("Critical");

        private final String displayName;

        PriorityLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Mark notification as read
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
        if (this.deliveryStatus == DeliveryStatus.DELIVERED) {
            this.deliveryStatus = DeliveryStatus.READ;
        }
    }

    /**
     * Mark notification as delivered
     */
    public void markAsDelivered() {
        this.deliveredAt = LocalDateTime.now();
        this.deliveryStatus = DeliveryStatus.DELIVERED;
    }

    /**
     * Check if notification is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if notification is active (not expired and not cancelled)
     */
    public boolean isActive() {
        return !isExpired() && deliveryStatus != DeliveryStatus.CANCELLED && deliveryStatus != DeliveryStatus.EXPIRED;
    }
} 