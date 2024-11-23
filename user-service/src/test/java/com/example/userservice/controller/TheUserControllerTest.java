package com.example.userservice.controller;

import java.util.*;

import com.example.userservice.model.User;
import com.example.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        user.setRole("user");

        when(userService.createUser(anyString(), anyString(), anyString(), anyString())).thenReturn(user);

        mockMvc.perform(post("/users/register")
                        .param("username", "testuser")
                        .param("password", "password")
                        .param("email", "test@example.com")
                        .param("role", "user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("user"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).createUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void loginUser_ValidCredentials_ReturnsOk() throws Exception {
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", "token123");
        tokens.put("refreshToken", "refreshToken123");

        when(userService.loginUser(anyString(), anyString())).thenReturn(tokens);

        mockMvc.perform(post("/users/login")
                        .param("username", "testuser")
                        .param("password", "password")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token123"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken123"));

        verify(userService, times(1)).loginUser(anyString(), anyString());
    }

    @Test
    void logoutUser_ValidAccessToken_ReturnsNoContent() throws Exception {
        doNothing().when(userService).logoutUser(anyString());

        mockMvc.perform(post("/users/logout")
                        .param("accessToken", "token123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).logoutUser(anyString());
    }


}
