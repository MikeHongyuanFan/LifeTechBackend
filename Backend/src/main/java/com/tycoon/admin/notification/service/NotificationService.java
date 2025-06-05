package com.tycoon.admin.notification.service;

import com.tycoon.admin.user.entity.User;
import com.tycoon.admin.user.entity.UserStatus;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    /**
     * Send welcome email to new user
     */
    void sendWelcomeEmail(User user);

    /**
     * Send email verification
     */
    void sendEmailVerification(User user, String verificationToken);

    /**
     * Send password reset email
     */
    void sendPasswordResetEmail(User user, String resetToken);

    /**
     * Send account status change notification
     */
    void sendAccountStatusChangeNotification(User user, UserStatus oldStatus, UserStatus newStatus, String reason);

    /**
     * Send account locked notification
     */
    void sendAccountLockedNotification(User user, String reason);

    /**
     * Send account unlocked notification
     */
    void sendAccountUnlockedNotification(User user);

    /**
     * Send bulk operation notification
     */
    void sendBulkOperationNotification(List<User> users, String operationType, Map<String, Object> operationDetails);

    /**
     * Send system message to user
     */
    void sendSystemMessage(User user, String subject, String message);

    /**
     * Send SMS notification (if phone number is available)
     */
    void sendSmsNotification(User user, String message);

    /**
     * Send admin notification about user actions
     */
    void sendAdminNotification(String subject, String message, Map<String, Object> details);
} 