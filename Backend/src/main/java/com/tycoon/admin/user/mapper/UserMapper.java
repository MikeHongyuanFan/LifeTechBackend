package com.tycoon.admin.user.mapper;

import com.tycoon.admin.user.dto.UserCreateRequest;
import com.tycoon.admin.user.dto.UserResponse;
import com.tycoon.admin.user.dto.UserUpdateRequest;
import com.tycoon.admin.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public User toEntity(UserCreateRequest request) {
        if (request == null) {
            return null;
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // Note: This should be encoded before calling this method
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setStatus(request.getStatus());
        user.setProfileImageUrl(request.getProfileImageUrl());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setTimezone(request.getTimezone());
        user.setLocale(request.getLocale());

        return user;
    }

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setStatus(user.getStatus());
        response.setEmailVerified(user.getEmailVerified());
        response.setPhoneVerified(user.getPhoneVerified());
        response.setProfileImageUrl(user.getProfileImageUrl());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setTimezone(user.getTimezone());
        response.setLocale(user.getLocale());

        // Analytics fields
        response.setLoginCount(user.getLoginCount());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setLastLoginIp(user.getLastLoginIp());
        response.setFailedLoginAttempts(user.getFailedLoginAttempts());
        response.setLastFailedLoginAt(user.getLastFailedLoginAt());
        response.setAccountLockedAt(user.getAccountLockedAt());
        response.setPasswordChangedAt(user.getPasswordChangedAt());
        response.setStatusChangedAt(user.getStatusChangedAt());
        response.setStatusChangedBy(user.getStatusChangedBy());
        response.setStatusChangeReason(user.getStatusChangeReason());

        // Base entity fields
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setCreatedBy(user.getCreatedBy());
        response.setUpdatedBy(user.getUpdatedBy());

        // Computed fields
        response.setFullName(user.getFullName());
        response.setAccountLocked(user.isAccountLocked());

        return response;
    }

    public List<UserResponse> toResponseList(List<User> users) {
        if (users == null) {
            return null;
        }

        return users.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void updateEntity(User user, UserUpdateRequest request) {
        if (user == null || request == null) {
            return;
        }

        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getTimezone() != null) {
            user.setTimezone(request.getTimezone());
        }
        if (request.getLocale() != null) {
            user.setLocale(request.getLocale());
        }
        if (request.getEmailVerified() != null) {
            user.setEmailVerified(request.getEmailVerified());
        }
        if (request.getPhoneVerified() != null) {
            user.setPhoneVerified(request.getPhoneVerified());
        }
    }
} 