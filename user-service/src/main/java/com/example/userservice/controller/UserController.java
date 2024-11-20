package com.example.userservice.controller;

import com.example.userservice.model.User;
import com.example.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

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

    @GetMapping("/authenticate")
    public ResponseEntity<Boolean> authenticateUser(@RequestParam String accessToken) {
        boolean isAuthenticated = userService.authenticateUser(accessToken);
        return ResponseEntity.ok(isAuthenticated);
    }
}
