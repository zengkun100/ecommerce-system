package com.example.userservice.controller;

import java.util.*;

import com.example.common.response.ApiResponse;
import com.example.userservice.model.User;
import com.example.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"));

        verify(userService, times(1)).createUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testLogoutUser_Success() throws Exception {
        String accessToken = "validToken";
        doNothing().when(userService).logoutUser(accessToken);

        mockMvc.perform(post("/users/logout")
                        .param("accessToken", accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"));

        verify(userService, times(1)).logoutUser(accessToken);
    }

    @Test
    public void testLogoutUser_Error() throws Exception {
        String accessToken = "validToken";
        String errorMessage = "error";

        doThrow(new RuntimeException(errorMessage)).when(userService).logoutUser(accessToken);

        mockMvc.perform(post("/users/logout")
                        .param("accessToken", accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.message").value("error"));

        verify(userService, times(1)).logoutUser(accessToken);
    }

}
