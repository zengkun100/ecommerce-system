package com.example.userservice.service;

import com.example.userservice.exception.TokenExpiredException;
import com.example.userservice.exception.TokenNotFoundException;
import com.example.userservice.model.AccessToken;
import com.example.userservice.model.RefreshToken;
import com.example.userservice.model.User;
import com.example.userservice.repository.AccessTokenRepository;
import com.example.userservice.repository.RefreshTokenRepository;
import com.example.userservice.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.time.ZoneId;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenExpiration;

    @Autowired
    public UserService(
            UserRepository userRepository,
            AccessTokenRepository accessTokenRepository,
            RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.accessTokenRepository = accessTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

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

            // Generate JWT access token
            LocalDateTime accessTokenExpiry = LocalDateTime.now().plusMinutes(accessTokenExpiration);
            String accessTokenValue = Jwts.builder()
                    .setSubject(user.getId().toString())
                    .claim("username", user.getUsername())
                    .claim("role", user.getRole())
                    .setIssuedAt(new Date())
                    .setExpiration(Date.from(accessTokenExpiry.atZone(ZoneId.systemDefault()).toInstant()))
                    .signWith(SignatureAlgorithm.HS512, jwtSecret)
                    .compact();

            // Generate JWT refresh token
            LocalDateTime refreshTokenExpiry = LocalDateTime.now().plusDays(refreshTokenExpiration);
            String refreshTokenValue = Jwts.builder()
                    .setSubject(user.getId().toString())
                    .setIssuedAt(new Date())
                    .setExpiration(Date.from(refreshTokenExpiry.atZone(ZoneId.systemDefault()).toInstant()))
                    .signWith(SignatureAlgorithm.HS512, jwtSecret)
                    .compact();

            // Save tokens to database (optional, for token revocation)
            AccessToken accessToken = new AccessToken();
            accessToken.setUserId(user.getId());
            accessToken.setToken(accessTokenValue);
            accessToken.setExpiration(accessTokenExpiry);
            accessTokenRepository.save(accessToken);

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

    public boolean authenticateUser(String accessToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(accessToken);
            return true;
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Access token has expired");
        } catch (JwtException e) {
            return false;
        }
    }

    public String refreshAccessToken(String refreshTokenValue) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByRefreshToken(refreshTokenValue);
        if (refreshTokenOpt.isPresent()) {
            RefreshToken refreshToken = refreshTokenOpt.get();
            if (refreshToken.getExpiration().isAfter(LocalDateTime.now())) {
                // 获取用户信息
                User user = userRepository.findById(refreshToken.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                // 生成新的 JWT Access Token
                LocalDateTime accessTokenExpiry = LocalDateTime.now().plusMinutes(accessTokenExpiration);
                String accessTokenValue = Jwts.builder()
                        .setSubject(user.getId().toString())
                        .claim("username", user.getUsername())
                        .claim("role", user.getRole())
                        .setIssuedAt(new Date())
                        .setExpiration(Date.from(accessTokenExpiry.atZone(ZoneId.systemDefault()).toInstant()))
                        .signWith(SignatureAlgorithm.HS512, jwtSecret)
                        .compact();

                // 保存新的 Access Token 到数据库
                AccessToken newAccessToken = new AccessToken();
                newAccessToken.setToken(accessTokenValue);
                newAccessToken.setUserId(refreshToken.getUserId());
                newAccessToken.setExpiration(accessTokenExpiry);
                accessTokenRepository.save(newAccessToken);

                return accessTokenValue;
            } else {
                throw new TokenExpiredException("Refresh token has expired. Please log in again.");
            }
        } else {
            throw new TokenNotFoundException("Invalid refresh token.");
        }
    }
}
