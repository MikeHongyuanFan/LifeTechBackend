package com.finance.admin.client.notification.model;

import com.finance.admin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "client_notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientNotificationPreference extends BaseEntity {

    @Column(name = "client_id", nullable = false, unique = true)
    private Long clientId;

    // General notifications
    @Column(name = "general_in_app")
    @Builder.Default
    private Boolean generalInApp = true;

    @Column(name = "general_email")
    @Builder.Default
    private Boolean generalEmail = true;

    @Column(name = "general_push")
    @Builder.Default
    private Boolean generalPush = true;

    @Column(name = "general_sms")
    @Builder.Default
    private Boolean generalSms = false;

    // KYC notifications
    @Column(name = "kyc_in_app")
    @Builder.Default
    private Boolean kycInApp = true;

    @Column(name = "kyc_email")
    @Builder.Default
    private Boolean kycEmail = true;

    @Column(name = "kyc_push")
    @Builder.Default
    private Boolean kycPush = true;

    @Column(name = "kyc_sms")
    @Builder.Default
    private Boolean kycSms = true;

    // Investment notifications
    @Column(name = "investment_in_app")
    @Builder.Default
    private Boolean investmentInApp = true;

    @Column(name = "investment_email")
    @Builder.Default
    private Boolean investmentEmail = true;

    @Column(name = "investment_push")
    @Builder.Default
    private Boolean investmentPush = true;

    @Column(name = "investment_sms")
    @Builder.Default
    private Boolean investmentSms = false;

    // Return notifications
    @Column(name = "return_in_app")
    @Builder.Default
    private Boolean returnInApp = true;

    @Column(name = "return_email")
    @Builder.Default
    private Boolean returnEmail = true;

    @Column(name = "return_push")
    @Builder.Default
    private Boolean returnPush = true;

    @Column(name = "return_sms")
    @Builder.Default
    private Boolean returnSms = false;

    // Report notifications
    @Column(name = "report_in_app")
    @Builder.Default
    private Boolean reportInApp = true;

    @Column(name = "report_email")
    @Builder.Default
    private Boolean reportEmail = true;

    @Column(name = "report_push")
    @Builder.Default
    private Boolean reportPush = false;

    @Column(name = "report_sms")
    @Builder.Default
    private Boolean reportSms = false;

    // System notifications
    @Column(name = "system_in_app")
    @Builder.Default
    private Boolean systemInApp = true;

    @Column(name = "system_email")
    @Builder.Default
    private Boolean systemEmail = true;

    @Column(name = "system_push")
    @Builder.Default
    private Boolean systemPush = true;

    @Column(name = "system_sms")
    @Builder.Default
    private Boolean systemSms = false;

    // Security notifications
    @Column(name = "security_in_app")
    @Builder.Default
    private Boolean securityInApp = true;

    @Column(name = "security_email")
    @Builder.Default
    private Boolean securityEmail = true;

    @Column(name = "security_push")
    @Builder.Default
    private Boolean securityPush = true;

    @Column(name = "security_sms")
    @Builder.Default
    private Boolean securitySms = true;

    // Global settings
    @Column(name = "do_not_disturb_enabled")
    @Builder.Default
    private Boolean doNotDisturbEnabled = false;

    @Column(name = "do_not_disturb_start_time")
    private String doNotDisturbStartTime; // Format: "22:00"

    @Column(name = "do_not_disturb_end_time")
    private String doNotDisturbEndTime; // Format: "08:00"

    @Column(name = "timezone")
    @Builder.Default
    private String timezone = "Australia/Sydney";

    @Column(name = "language")
    @Builder.Default
    private String language = "en";

    /**
     * Check if a specific notification type and delivery method is enabled
     */
    public boolean isEnabled(ClientNotification.NotificationType type, ClientNotification.DeliveryMethod method) {
        return switch (type) {
            case GENERAL -> switch (method) {
                case IN_APP -> generalInApp;
                case EMAIL -> generalEmail;
                case PUSH -> generalPush;
                case SMS -> generalSms;
                default -> false;
            };
            case KYC -> switch (method) {
                case IN_APP -> kycInApp;
                case EMAIL -> kycEmail;
                case PUSH -> kycPush;
                case SMS -> kycSms;
                default -> false;
            };
            case INVESTMENT -> switch (method) {
                case IN_APP -> investmentInApp;
                case EMAIL -> investmentEmail;
                case PUSH -> investmentPush;
                case SMS -> investmentSms;
                default -> false;
            };
            case RETURN -> switch (method) {
                case IN_APP -> returnInApp;
                case EMAIL -> returnEmail;
                case PUSH -> returnPush;
                case SMS -> returnSms;
                default -> false;
            };
            case REPORT -> switch (method) {
                case IN_APP -> reportInApp;
                case EMAIL -> reportEmail;
                case PUSH -> reportPush;
                case SMS -> reportSms;
                default -> false;
            };
            case SYSTEM -> switch (method) {
                case IN_APP -> systemInApp;
                case EMAIL -> systemEmail;
                case PUSH -> systemPush;
                case SMS -> systemSms;
                default -> false;
            };
            case SECURITY -> switch (method) {
                case IN_APP -> securityInApp;
                case EMAIL -> securityEmail;
                case PUSH -> securityPush;
                case SMS -> securitySms;
                default -> false;
            };
        };
    }

    /**
     * Create default preferences for a new client
     */
    public static ClientNotificationPreference createDefault(Long clientId) {
        return ClientNotificationPreference.builder()
                .clientId(clientId)
                .build(); // All defaults are set via @Builder.Default
    }
} 