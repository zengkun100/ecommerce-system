package com.example.userservice.service;

import com.example.userservice.exception.TokenExpiredException;
import com.example.userservice.exception.TokenNotFoundException;
import com.example.userservice.model.AccessToken;
import com.example.userservice.model.RefreshToken;
import com.example.userservice.model.User;
import com.example.userservice.repository.AccessTokenRepository;
import com.example.userservice.repository.RefreshTokenRepository;
import com.example.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccessTokenRepository accessTokenRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User createUser(String username, String rawPassword, String email, String role) {
        String hashedPassword = passwordEncoder.encode(rawPassword);
        User user = new User();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setEmail(email);
        user.setRole(role);
        return userRepository.save(user);
    }

    @Transactional
    public Map<String, String> loginUser(String username, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && passwordEncoder.matches(rawPassword, userOpt.get().getPassword())) {
            User user = userOpt.get();

            // Delete existing tokens for the user
            accessTokenRepository.deleteAllByUserId(user.getId());
            refreshTokenRepository.deleteAllByUserId(user.getId());

            // Generate Access Token
            String accessTokenValue = UUID.randomUUID().toString();
            LocalDateTime accessTokenExpiry = LocalDateTime.now().plusMinutes(30);
            AccessToken accessToken = new AccessToken();
            accessToken.setUserId(user.getId());
            accessToken.setToken(accessTokenValue);
            accessToken.setExpiration(accessTokenExpiry);
            accessTokenRepository.save(accessToken);

            // Generate Refresh Token
            String refreshTokenValue = UUID.randomUUID().toString();
            LocalDateTime refreshTokenExpiry = LocalDateTime.now().plusDays(7);
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setUserId(user.getId());
            refreshToken.setRefreshToken(refreshTokenValue);
            refreshToken.setExpiration(refreshTokenExpiry);
            refreshTokenRepository.save(refreshToken);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessTokenValue);
            tokens.put("refreshToken", refreshTokenValue);
            return tokens;
        } else {
            throw new RuntimeException("Invalid username or password");
        }
    }

    @Transactional
    public void logoutUser(String accessTokenValue) {
        Optional<AccessToken> accessTokenOpt = accessTokenRepository.findByToken(accessTokenValue);
        accessTokenOpt.ifPresent(accessToken -> {
            accessTokenRepository.deleteAllByUserId(accessToken.getUserId());
            refreshTokenRepository.deleteAllByUserId(accessToken.getUserId());
        });
    }

    @Transactional
    public void deleteUser(Long userId) {
        // Delete all tokens related to the user
        accessTokenRepository.deleteAllByUserId(userId);
        refreshTokenRepository.deleteAllByUserId(userId);
        // Delete the user
        userRepository.deleteById(userId);
    }

    public boolean authenticateUser(String accessTokenValue) {
        Optional<AccessToken> accessTokenOpt = accessTokenRepository.findByToken(accessTokenValue);
        if (accessTokenOpt.isPresent()) {
            AccessToken accessToken = accessTokenOpt.get();
            if (accessToken.getExpiration().isAfter(LocalDateTime.now())) {
                return true;
            } else {
                throw new TokenExpiredException("Access token has expired. Please refresh the token.");
            }
        }
        return false;
    }

    public String refreshAccessToken(String refreshTokenValue) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByRefreshToken(refreshTokenValue);
        if (refreshTokenOpt.isPresent()) {
            RefreshToken refreshToken = refreshTokenOpt.get();
            if (refreshToken.getExpiration().isAfter(LocalDateTime.now())) {
                // 生成新的 Access Token
                AccessToken newAccessToken = new AccessToken();
                newAccessToken.setToken(UUID.randomUUID().toString());
                newAccessToken.setUserId(refreshToken.getUserId());
                newAccessToken.setExpiration(LocalDateTime.now().plusMinutes(30)); // 设置过期时间为 30 分钟
                accessTokenRepository.save(newAccessToken);

                return newAccessToken.getToken();
            } else {
                throw new TokenExpiredException("Refresh token has expired. Please log in again.");
            }
        } else {
            throw new TokenNotFoundException("Invalid refresh token.");
        }
    }
}
