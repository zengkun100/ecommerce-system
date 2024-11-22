package com.example.userservice.controller;

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
    public ResponseEntity<User> registerUser(@RequestParam String username, @RequestParam String password, @RequestParam String email, @RequestParam String role) {
        User user = userService.createUser(username, password, email, role);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestParam String username, @RequestParam String password) {
        Map<String, String> tokens = userService.loginUser(username, password);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser(@RequestParam String accessToken) {
        userService.logoutUser(accessToken);
        return ResponseEntity.noContent().build();
    }

    // TODO: 需要增加权限校验，只有管理员能删除用户
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // @GetMapping("/authenticate")
    // public ResponseEntity<String> authenticateUser(@RequestParam String accessToken) {
    //     try {
    //         boolean isAuthenticated = userService.authenticateUser(accessToken);
    //         if (isAuthenticated) {
    //             return ResponseEntity.ok("User authenticated successfully.");
    //         } else {
    //             return ResponseEntity.status(401).body("Invalid access token.");
    //         }
    //     } catch (TokenExpiredException e) {
    //         return ResponseEntity.status(401).body(e.getMessage());
    //     }
    // }

    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshAccessToken(@RequestParam String refreshToken) {
        try {
            String newAccessToken = userService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(newAccessToken);
        } catch (TokenExpiredException | TokenNotFoundException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
