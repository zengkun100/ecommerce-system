package com.example.userservice.controller;

import com.example.userservice.exception.TokenExpiredException;
import com.example.userservice.model.User;
import com.example.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRegisterUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setRole("USER");

        Mockito.when(userService.createUser(any(String.class), any(String.class), any(String.class), any(String.class)))
                .thenReturn(user);

        mockMvc.perform(post("/users/register")
                        .param("username", "testuser")
                        .param("password", "password123")
                        .param("email", "testuser@example.com")
                        .param("role", "USER")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    public void testUnregister() throws Exception {
        doNothing().when(userService).unregisterUser("accessToken");
        mockMvc.perform(delete("/users/unregister")
                        .param("accessToken", "accessToken")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    public void testUnregister_error() throws Exception {
        doThrow(new RuntimeException()).when(userService).unregisterUser("accessToken");
        mockMvc.perform(delete("/users/unregister")
                        .param("accessToken", "accessToken")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(5001));
    }


    @Test
    public void testLoginUser() throws Exception {
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", "sampleAccessToken");
        tokens.put("refreshToken", "sampleRefreshToken");

        Mockito.when(userService.loginUser(any(String.class), any(String.class))).thenReturn(tokens);

        mockMvc.perform(post("/users/login")
                        .param("username", "testuser")
                        .param("password", "password123")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.accessToken").value("sampleAccessToken"))
                .andExpect(jsonPath("$.data.refreshToken").value("sampleRefreshToken"));
    }

    @Test
    public void testLoginUser_error() throws Exception {
        Mockito.when(userService.loginUser(any(String.class), any(String.class))).thenThrow(new RuntimeException());

        mockMvc.perform(post("/users/login")
                        .param("username", "testuser")
                        .param("password", "password123")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(5001));
    }


    @Test
    public void testRefreshAccessTokenError() throws Exception {
        Mockito.when(userService.refreshAccessToken(any(String.class))).thenThrow(TokenExpiredException.class);

        mockMvc.perform(post("/users/refresh-token")
                        .param("refreshToken", "sampleAccessToken")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(5001));
    }

    @Test
    public void testRefreshAccessTokenOK() throws Exception {
        Mockito.when(userService.refreshAccessToken(any(String.class))).thenReturn("sampleAccessToken");

        mockMvc.perform(post("/users/refresh-token")
                        .param("refreshToken", "sampleAccessToken")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").value("sampleAccessToken"));
    }
}
