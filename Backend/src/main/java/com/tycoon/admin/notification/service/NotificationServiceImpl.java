package com.tycoon.admin.notification.service;

import com.tycoon.admin.user.entity.User;
import com.tycoon.admin.user.entity.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Value("${app.admin.email:admin@tycoon.com}")
    private String adminEmail;

    @Value("${app.notification.enabled:true}")
    private boolean notificationsEnabled;

    @Value("${app.notification.email.enabled:true}")
    private boolean emailNotificationsEnabled;

    @Value("${app.notification.sms.enabled:false}")
    private boolean smsNotificationsEnabled;

    @Override
    public void sendWelcomeEmail(User user) {
        if (!isEmailNotificationEnabled()) {
            return;
        }

        logger.info("Sending welcome email to user: {}", user.getEmail());
        
        try {
            // Email template data
            String subject = "Welcome to Tycoon Admin System";
            String body = buildWelcomeEmailBody(user);
            
            // In a real implementation, this would use an email service like JavaMailSender
            // For now, we'll log the email content
            logEmailNotification(user.getEmail(), subject, body);
            
            logger.info("Welcome email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", user.getEmail(), e);
        }
    }

    @Override
    public void sendEmailVerification(User user, String verificationToken) {
        if (!isEmailNotificationEnabled()) {
            return;
        }

        logger.info("Sending email verification to user: {}", user.getEmail());
        
        try {
            String subject = "Verify Your Email Address";
            String body = buildEmailVerificationBody(user, verificationToken);
            
            logEmailNotification(user.getEmail(), subject, body);
            
            logger.info("Email verification sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send email verification to: {}", user.getEmail(), e);
        }
    }

    @Override
    public void sendPasswordResetEmail(User user, String resetToken) {
        if (!isEmailNotificationEnabled()) {
            return;
        }

        logger.info("Sending password reset email to user: {}", user.getEmail());
        
        try {
            String subject = "Password Reset Request";
            String body = buildPasswordResetBody(user, resetToken);
            
            logEmailNotification(user.getEmail(), subject, body);
            
            logger.info("Password reset email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", user.getEmail(), e);
        }
    }

    @Override
    public void sendAccountStatusChangeNotification(User user, UserStatus oldStatus, UserStatus newStatus, String reason) {
        if (!isEmailNotificationEnabled()) {
            return;
        }

        logger.info("Sending account status change notification to user: {} ({}->{})", 
                   user.getEmail(), oldStatus, newStatus);
        
        try {
            String subject = "Account Status Changed";
            String body = buildAccountStatusChangeBody(user, oldStatus, newStatus, reason);
            
            logEmailNotification(user.getEmail(), subject, body);
            
            logger.info("Account status change notification sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send account status change notification to: {}", user.getEmail(), e);
        }
    }

    @Override
    public void sendAccountLockedNotification(User user, String reason) {
        if (!isEmailNotificationEnabled()) {
            return;
        }

        logger.info("Sending account locked notification to user: {}", user.getEmail());
        
        try {
            String subject = "Account Locked - Security Alert";
            String body = buildAccountLockedBody(user, reason);
            
            logEmailNotification(user.getEmail(), subject, body);
            
            logger.info("Account locked notification sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send account locked notification to: {}", user.getEmail(), e);
        }
    }

    @Override
    public void sendAccountUnlockedNotification(User user) {
        if (!isEmailNotificationEnabled()) {
            return;
        }

        logger.info("Sending account unlocked notification to user: {}", user.getEmail());
        
        try {
            String subject = "Account Unlocked";
            String body = buildAccountUnlockedBody(user);
            
            logEmailNotification(user.getEmail(), subject, body);
            
            logger.info("Account unlocked notification sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send account unlocked notification to: {}", user.getEmail(), e);
        }
    }

    @Override
    public void sendBulkOperationNotification(List<User> users, String operationType, Map<String, Object> operationDetails) {
        if (!isEmailNotificationEnabled()) {
            return;
        }

        logger.info("Sending bulk operation notification for {} users, operation: {}", users.size(), operationType);
        
        try {
            for (User user : users) {
                String subject = "Account Update Notification";
                String body = buildBulkOperationBody(user, operationType, operationDetails);
                
                logEmailNotification(user.getEmail(), subject, body);
            }
            
            // Also notify administrators
            sendAdminNotification(
                "Bulk Operation Completed", 
                "Bulk operation '" + operationType + "' completed for " + users.size() + " users",
                operationDetails
            );
            
            logger.info("Bulk operation notifications sent successfully for {} users", users.size());
        } catch (Exception e) {
            logger.error("Failed to send bulk operation notifications", e);
        }
    }

    @Override
    public void sendSystemMessage(User user, String subject, String message) {
        if (!isEmailNotificationEnabled()) {
            return;
        }

        logger.info("Sending system message to user: {}", user.getEmail());
        
        try {
            String body = buildSystemMessageBody(user, message);
            
            logEmailNotification(user.getEmail(), subject, body);
            
            logger.info("System message sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send system message to: {}", user.getEmail(), e);
        }
    }

    @Override
    public void sendSmsNotification(User user, String message) {
        if (!isSmsNotificationEnabled() || user.getPhoneNumber() == null) {
            return;
        }

        logger.info("Sending SMS notification to user: {}", user.getPhoneNumber());
        
        try {
            // In a real implementation, this would use an SMS service
            logger.info("SMS to {}: {}", user.getPhoneNumber(), message);
            
            logger.info("SMS notification sent successfully to: {}", user.getPhoneNumber());
        } catch (Exception e) {
            logger.error("Failed to send SMS notification to: {}", user.getPhoneNumber(), e);
        }
    }

    @Override
    public void sendAdminNotification(String subject, String message, Map<String, Object> details) {
        if (!isEmailNotificationEnabled()) {
            return;
        }

        logger.info("Sending admin notification: {}", subject);
        
        try {
            String body = buildAdminNotificationBody(message, details);
            
            logEmailNotification(adminEmail, subject, body);
            
            logger.info("Admin notification sent successfully");
        } catch (Exception e) {
            logger.error("Failed to send admin notification", e);
        }
    }

    // Helper methods for building email bodies
    private String buildWelcomeEmailBody(User user) {
        return String.format("""
            Dear %s,
            
            Welcome to the Tycoon Admin Management System!
            
            Your account has been successfully created with the following details:
            - Username: %s
            - Email: %s
            
            Please verify your email address to activate your account.
            
            If you have any questions, please contact our support team.
            
            Best regards,
            Tycoon Admin Team
            """, user.getFullName(), user.getUsername(), user.getEmail());
    }

    private String buildEmailVerificationBody(User user, String verificationToken) {
        return String.format("""
            Dear %s,
            
            Please verify your email address by clicking the link below:
            
            [Verification Link with token: %s]
            
            This link will expire in 24 hours.
            
            If you did not create this account, please ignore this email.
            
            Best regards,
            Tycoon Admin Team
            """, user.getFullName(), verificationToken);
    }

    private String buildPasswordResetBody(User user, String resetToken) {
        return String.format("""
            Dear %s,
            
            A password reset has been requested for your account.
            
            Reset token: %s
            
            This link will expire in 1 hour.
            
            If you did not request this reset, please contact support immediately.
            
            Best regards,
            Tycoon Admin Team
            """, user.getFullName(), resetToken);
    }

    private String buildAccountStatusChangeBody(User user, UserStatus oldStatus, UserStatus newStatus, String reason) {
        return String.format("""
            Dear %s,
            
            Your account status has been changed:
            
            Previous Status: %s
            New Status: %s
            Reason: %s
            
            If you have any questions about this change, please contact support.
            
            Best regards,
            Tycoon Admin Team
            """, user.getFullName(), oldStatus.getDisplayName(), newStatus.getDisplayName(), reason);
    }

    private String buildAccountLockedBody(User user, String reason) {
        return String.format("""
            Dear %s,
            
            Your account has been locked for security reasons.
            
            Reason: %s
            
            Please contact support to unlock your account.
            
            Best regards,
            Tycoon Admin Team
            """, user.getFullName(), reason);
    }

    private String buildAccountUnlockedBody(User user) {
        return String.format("""
            Dear %s,
            
            Your account has been unlocked and is now active.
            
            You can now log in normally.
            
            Best regards,
            Tycoon Admin Team
            """, user.getFullName());
    }

    private String buildBulkOperationBody(User user, String operationType, Map<String, Object> operationDetails) {
        return String.format("""
            Dear %s,
            
            Your account has been updated as part of a bulk operation.
            
            Operation Type: %s
            Details: %s
            
            If you have any questions about this update, please contact support.
            
            Best regards,
            Tycoon Admin Team
            """, user.getFullName(), operationType, operationDetails.toString());
    }

    private String buildSystemMessageBody(User user, String message) {
        return String.format("""
            Dear %s,
            
            %s
            
            Best regards,
            Tycoon Admin Team
            """, user.getFullName(), message);
    }

    private String buildAdminNotificationBody(String message, Map<String, Object> details) {
        return String.format("""
            Admin Notification
            
            Message: %s
            
            Details: %s
            
            Tycoon Admin System
            """, message, details != null ? details.toString() : "No additional details");
    }

    private void logEmailNotification(String to, String subject, String body) {
        logger.info("EMAIL NOTIFICATION:");
        logger.info("To: {}", to);
        logger.info("Subject: {}", subject);
        logger.info("Body: {}", body);
        logger.info("--- END EMAIL ---");
    }

    private boolean isEmailNotificationEnabled() {
        return notificationsEnabled && emailNotificationsEnabled;
    }

    private boolean isSmsNotificationEnabled() {
        return notificationsEnabled && smsNotificationsEnabled;
    }
} 