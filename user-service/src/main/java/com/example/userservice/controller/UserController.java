package com.example.userservice.controller;

import com.example.common.response.ApiCode;
import com.example.common.response.ApiResponse;
import com.example.userservice.exception.TokenExpiredException;
import com.example.userservice.exception.TokenNotFoundException;
import com.example.userservice.model.User;
import com.example.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Long>> registerUser(@RequestParam String username, @RequestParam String password, @RequestParam String email, @RequestParam String role) {
        User user = userService.createUser(username, password, email, role);
        return ResponseEntity.ok(ApiResponse.success(user.getId()));
    }

    @DeleteMapping("/unregister")
    public ResponseEntity<ApiResponse<Void>> unregisterUser(@RequestParam String accessToken) {
        try {
            userService.unregisterUser(accessToken);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(ApiCode.SYS_ERROR, e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> loginUser(@RequestParam String username, @RequestParam String password) {
        try {
            Map<String, String> tokens = userService.loginUser(username, password);
            return ResponseEntity.ok(ApiResponse.success(tokens));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(ApiCode.SYS_ERROR, e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logoutUser(@RequestParam String accessToken) {
        try {
            userService.logoutUser(accessToken);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(ApiCode.SYS_ERROR, e.getMessage()));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<String>> refreshAccessToken(@RequestParam String refreshToken) {
        try {
            String newAccessToken = userService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(ApiResponse.success(newAccessToken));
        } catch (TokenExpiredException | TokenNotFoundException e) {
            return ResponseEntity.ok(ApiResponse.error(ApiCode.SYS_ERROR, e.getMessage()));
        }
    }
}
