package com.example.userservice.controller;

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
                .andExpect(jsonPath("$.email").value("testuser@example.com"));
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
                .andExpect(jsonPath("$.accessToken").value("sampleAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("sampleRefreshToken"));
    }

    @Test
    public void testLogoutUser() throws Exception {
        Mockito.doNothing().when(userService).logoutUser(any(String.class));

        mockMvc.perform(post("/users/logout")
                        .param("accessToken", "sampleAccessToken")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteUser() throws Exception {
        Mockito.doNothing().when(userService).deleteUser(any(Long.class));

        mockMvc.perform(delete("/users/{userId}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testAuthenticateUser() throws Exception {
        Mockito.when(userService.authenticateUser(any(String.class))).thenReturn(true);

        mockMvc.perform(get("/users/authenticate")
                        .param("accessToken", "sampleAccessToken")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
