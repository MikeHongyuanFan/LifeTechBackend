package com.finance.admin.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Upcoming Birthdays Response DTO
 * Contains birthday information for automated congratulations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingBirthdaysResponse {

    private Long totalUpcomingBirthdays;
    private Long birthdaysNext7Days;
    private Long birthdaysNext30Days;
    private Long greetingsSentToday;
    private Long pendingGreetings;

    private List<BirthdayClient> upcomingBirthdays;
    private List<BirthdayGreeting> recentGreetings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BirthdayClient {
        private Long clientId;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String phone;
        private LocalDate dateOfBirth;
        private LocalDate nextBirthday;
        private Integer daysUntilBirthday;
        private Integer age;
        private Boolean greetingSent;
        private LocalDateTime greetingSentAt;
        private String clientStatus;
        private String preferredContactMethod;
        private Boolean emailOptIn;
        private Boolean smsOptIn;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BirthdayGreeting {
        private Long greetingId;
        private Long clientId;
        private String clientName;
        private String email;
        private LocalDate birthdayDate;
        private LocalDateTime sentAt;
        private String deliveryStatus; // SENT, DELIVERED, FAILED, PENDING
        private String deliveryMethod; // EMAIL, SMS, BOTH
        private String templateUsed;
        private String errorMessage;
        private Integer retryCount;
    }
} 