package com.tycoon.admin.user.dto;

import com.tycoon.admin.user.entity.UserStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

public class UserSearchCriteria {

    private String searchTerm; // General search in username, email, first name, last name
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserStatus status;
    private List<UserStatus> statuses;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Boolean accountLocked;

    // Date range filters
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdDateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdDateTo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime lastLoginFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime lastLoginTo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime statusChangedFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime statusChangedTo;

    // Analytics filters
    private Long minLoginCount;
    private Long maxLoginCount;
    private Integer maxFailedAttempts;

    // Geographic filters
    private String timezone;
    private String locale;

    // Constructors
    public UserSearchCriteria() {}

    // Getters and Setters
    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public List<UserStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<UserStatus> statuses) {
        this.statuses = statuses;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Boolean getPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(Boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public Boolean getAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public LocalDateTime getCreatedDateFrom() {
        return createdDateFrom;
    }

    public void setCreatedDateFrom(LocalDateTime createdDateFrom) {
        this.createdDateFrom = createdDateFrom;
    }

    public LocalDateTime getCreatedDateTo() {
        return createdDateTo;
    }

    public void setCreatedDateTo(LocalDateTime createdDateTo) {
        this.createdDateTo = createdDateTo;
    }

    public LocalDateTime getLastLoginFrom() {
        return lastLoginFrom;
    }

    public void setLastLoginFrom(LocalDateTime lastLoginFrom) {
        this.lastLoginFrom = lastLoginFrom;
    }

    public LocalDateTime getLastLoginTo() {
        return lastLoginTo;
    }

    public void setLastLoginTo(LocalDateTime lastLoginTo) {
        this.lastLoginTo = lastLoginTo;
    }

    public LocalDateTime getStatusChangedFrom() {
        return statusChangedFrom;
    }

    public void setStatusChangedFrom(LocalDateTime statusChangedFrom) {
        this.statusChangedFrom = statusChangedFrom;
    }

    public LocalDateTime getStatusChangedTo() {
        return statusChangedTo;
    }

    public void setStatusChangedTo(LocalDateTime statusChangedTo) {
        this.statusChangedTo = statusChangedTo;
    }

    public Long getMinLoginCount() {
        return minLoginCount;
    }

    public void setMinLoginCount(Long minLoginCount) {
        this.minLoginCount = minLoginCount;
    }

    public Long getMaxLoginCount() {
        return maxLoginCount;
    }

    public void setMaxLoginCount(Long maxLoginCount) {
        this.maxLoginCount = maxLoginCount;
    }

    public Integer getMaxFailedAttempts() {
        return maxFailedAttempts;
    }

    public void setMaxFailedAttempts(Integer maxFailedAttempts) {
        this.maxFailedAttempts = maxFailedAttempts;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
} 