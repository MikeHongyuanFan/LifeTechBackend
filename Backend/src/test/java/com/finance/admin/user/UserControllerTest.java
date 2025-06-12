package com.finance.admin.user;

import com.finance.admin.config.BaseUnitTest;
import com.finance.admin.user.controller.UserController;
import com.finance.admin.user.dto.UserCreateRequest;
import com.finance.admin.user.dto.UserResponse;
import com.finance.admin.user.dto.UserUpdateRequest;
import com.finance.admin.user.entity.UserStatus;
import com.finance.admin.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the User Management API endpoints
 * Using minimal standalone MockMvc configuration
 */
public class UserControllerTest extends BaseUnitTest {

    private MockMvc mockMvc;
    private UserService userService;
    private UserController userController;
    private UserResponse testUserResponse;
    private UserCreateRequest createRequest;
    private UserUpdateRequest updateRequest;
    private UUID userId;

    @BeforeEach
    protected void setUp() {
        super.setUp(); // Call parent setup
        
        // Create mocks
        userService = mock(UserService.class);
        userController = new UserController();
        
        // Use reflection to inject the mocked service
        try {
            Field userServiceField = UserController.class.getDeclaredField("userService");
            userServiceField.setAccessible(true);
            userServiceField.set(userController, userService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock service", e);
        }
        
        // Setup MockMvc with PageableHandlerMethodArgumentResolver
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        
        userId = UUID.randomUUID();
        
        // Setup test user response
        testUserResponse = new UserResponse();
        testUserResponse.setId(userId);
        testUserResponse.setUsername("testuser");
        testUserResponse.setEmail("test@finance.com");
        testUserResponse.setFirstName("Test");
        testUserResponse.setLastName("User");
        testUserResponse.setStatus(UserStatus.ACTIVE);
        testUserResponse.setEmailVerified(true);
        testUserResponse.setPhoneVerified(false);
        testUserResponse.setCreatedAt(LocalDateTime.now());
        
        // Setup create request
        createRequest = new UserCreateRequest();
        createRequest.setUsername("newuser");
        createRequest.setEmail("new@finance.com");
        createRequest.setPassword("Password123!");
        createRequest.setFirstName("New");
        createRequest.setLastName("User");
        
        // Setup update request
        updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("User");
        updateRequest.setEmail("updated@finance.com");
    }

    @Test
    void testCreateUser() throws Exception {
        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(testUserResponse);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value(testUserResponse.getUsername()));
    }

    @Test
    void testGetUserById() throws Exception {
        when(userService.getUserById(any(UUID.class))).thenReturn(testUserResponse);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userId.toString()));
    }

    @Test
    void testGetUserByUsername() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(testUserResponse);

        mockMvc.perform(get("/users/username/{username}", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void testUpdateUser() throws Exception {
        when(userService.updateUser(any(UUID.class), any(UserUpdateRequest.class))).thenReturn(testUserResponse);

        mockMvc.perform(put("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userId.toString()));
    }

    @Test
    void testDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser(any(UUID.class));

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetAllUsers() throws Exception {
        List<UserResponse> users = new ArrayList<>();
        users.add(testUserResponse);
        Page<UserResponse> page = new PageImpl<>(users, PageRequest.of(0, 20), 1);
        
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].username").value("testuser"));
    }

    @Test
    void testGetUsersByStatus() throws Exception {
        List<UserResponse> users = new ArrayList<>();
        users.add(testUserResponse);
        Page<UserResponse> page = new PageImpl<>(users, PageRequest.of(0, 20), 1);
        
        when(userService.getUsersByStatusPage(any(UserStatus.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/users/status/{status}", UserStatus.ACTIVE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"));
    }

    @Test
    void testLockUser() throws Exception {
        when(userService.lockUser(any(UUID.class), anyString())).thenReturn(testUserResponse);

        mockMvc.perform(post("/users/{id}/lock", userId)
                .param("reason", "Security violation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testUnlockUser() throws Exception {
        when(userService.unlockUser(any(UUID.class))).thenReturn(testUserResponse);

        mockMvc.perform(post("/users/{id}/unlock", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testVerifyEmail() throws Exception {
        when(userService.verifyEmail(any(UUID.class))).thenReturn(testUserResponse);

        mockMvc.perform(post("/users/{id}/verify-email", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetUserStatistics() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", 100);
        stats.put("activeUsers", 80);
        stats.put("inactiveUsers", 20);
        
        when(userService.getUserStatistics()).thenReturn(stats);

        mockMvc.perform(get("/users/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalUsers").value(100));
    }

    @Test
    void testCheckUsernameExists() throws Exception {
        when(userService.existsByUsername(anyString())).thenReturn(true);

        mockMvc.perform(get("/users/exists/username/{username}", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }
}
