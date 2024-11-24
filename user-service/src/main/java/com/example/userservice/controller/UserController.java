package com.example.userservice.controller;

import com.example.common.response.ApiCode;
import com.example.common.response.ApiResponse;
//import com.example.userservice.dto.UserRegistrationRequest;
import com.example.userservice.dto.request.UserRegistrationRequest;
import com.example.userservice.dto.request.LoginRequest;
import com.example.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ApiResponse<Map<String, String>>> registerUser(@RequestBody UserRegistrationRequest request) {
        userService.createUser(
            request.getUsername(), 
            request.getPassword(), 
            request.getEmail(), 
            request.getRole()
        );
        Map<String, String> tokens = userService.loginUser(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success(tokens));
    }

    @DeleteMapping("/unregister")
    public ResponseEntity<ApiResponse<Void>> unregisterUser(@RequestHeader("Authorization") String token) {
        if (!token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(ApiCode.TOKEN_INVALID, "无效的认证token格式", null));
        }

        String accessToken = token.substring(7);
        userService.unregisterUser(accessToken);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> loginUser(@RequestBody LoginRequest request) {
        Map<String, String> tokens = userService.loginUser(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success(tokens));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logoutUser(@RequestHeader("Authorization") String token) {
        if (!token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(ApiCode.TOKEN_INVALID, "无效的认证token格式", null));
        }

        String jwtToken = token.substring(7);
        userService.logoutUser(jwtToken);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<String>> refreshAccessToken(@RequestHeader("Authorization") String token) {
        if (!token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(ApiCode.TOKEN_INVALID, "无效的认证token格式", null));
        }
        
        String refreshToken = token.substring(7);
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
