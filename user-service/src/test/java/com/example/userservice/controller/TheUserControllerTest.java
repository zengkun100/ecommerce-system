package com.example.userservice.controller;

import java.time.LocalDateTime;
import java.util.*;

import com.example.common.response.ApiResponse;
import com.example.userservice.dto.request.UserRegistrationRequest;
import com.example.userservice.model.User;
import com.example.userservice.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TheUserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void registerUser_ValidUserDetails_ReturnsOk() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testUser");
        request.setPassword("password123");
        request.setEmail("testuser@example.com");
        request.setRole("USER");

        Map<String, String> mockTokens = new HashMap<>();
        mockTokens.put("accessToken", "mockAccessToken");
        mockTokens.put("refreshToken", "mockRefreshToken");

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername(request.getUsername());
        mockUser.setPassword(request.getPassword());
        mockUser.setEmail(request.getEmail());
        mockUser.setRole(request.getRole());
        mockUser.setCreateTime(LocalDateTime.now());

        Mockito.when(userService.createUser(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getRole()
        )).thenReturn(mockUser);

        Mockito.when(userService.loginUser(request.getUsername(), request.getPassword())).thenReturn(mockTokens);

        // 调用 Controller 方法
        ResponseEntity<ApiResponse<Map<String, String>>> response = userController.registerUser(request);

        // 验证方法调用
        Mockito.verify(userService).createUser(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getRole()
        );
        Mockito.verify(userService).loginUser(request.getUsername(), request.getPassword());

        // 验证返回结果
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(response.getBody().isSuccess());
        Assertions.assertEquals(mockTokens, response.getBody().getData());
    }

    @Test
    public void testLogoutUser_Success() throws Exception {
        String accessToken = "Bearer validToken";
        doNothing().when(userService).logoutUser("validToken");

        mockMvc.perform(post("/users/logout")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"));

        verify(userService, times(1)).logoutUser("validToken");
    }

    @Test
    public void testLogoutUser_Valid() throws Exception {
        String accessToken = "validToken";
        mockMvc.perform(post("/users/logout")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
