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
    public ResponseEntity<ApiResponse<Map<String, String>>> registerUser(@RequestParam String username, @RequestParam String password, @RequestParam String email, @RequestParam String role) {
        userService.createUser(username, password, email, role);
        Map<String, String> tokens = userService.loginUser(username, password);
        return ResponseEntity.ok(ApiResponse.success(tokens));
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
        Map<String, String> tokens = userService.loginUser(username, password);
        return ResponseEntity.ok(ApiResponse.success(tokens));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logoutUser(@RequestParam String accessToken) {
        userService.logoutUser(accessToken);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<String>> refreshAccessToken(@RequestParam String refreshToken) {
        String newAccessToken = userService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(newAccessToken));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateToken(@RequestHeader("Authorization") String token) {
        String jwtToken = token.substring(7);
        userService.authenticateUser(jwtToken);
        String userId = userService.getUserIdFromToken(jwtToken);
        return ResponseEntity.ok(ApiResponse.success(userId));
    }
}
