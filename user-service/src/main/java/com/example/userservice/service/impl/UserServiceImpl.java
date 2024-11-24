package com.example.userservice.service.impl;

import com.example.userservice.exception.TokenExpiredException;
import com.example.userservice.exception.TokenNotFoundException;
import com.example.userservice.model.AccessToken;
import com.example.userservice.model.RefreshToken;
import com.example.userservice.model.User;
import com.example.userservice.repository.AccessTokenRepository;
import com.example.userservice.repository.RefreshTokenRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenExpiration;

    @Autowired
    public UserServiceImpl(
            UserRepository userRepository,
            AccessTokenRepository accessTokenRepository,
            RefreshTokenRepository refreshTokenRepository,
            RedisTemplate<String, String> redisTemplate) {
        this.userRepository = userRepository;
        this.accessTokenRepository = accessTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.redisTemplate = redisTemplate;
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
        try {
            // 解析 JWT token 获取过期时间
            var claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(accessTokenValue)
                    .getBody();
            
            Date expiration = claims.getExpiration();
            Long userId = Long.parseLong(claims.getSubject());
            
            // 计算剩余过期时间（毫秒）
            long remainingTime = expiration.getTime() - System.currentTimeMillis();
            if (remainingTime > 0) {
                // 将 token 加入黑名单，key 使用 "jwt_blacklist:" 前缀
                String blacklistKey = "jwt_blacklist:" + accessTokenValue;
                redisTemplate.opsForValue().set(
                    blacklistKey,
                    "blacklisted",
                    remainingTime,
                    TimeUnit.MILLISECONDS
                );
            }

            // 删除数据库中的 token 记录
            accessTokenRepository.deleteAllByUserId(userId);
            refreshTokenRepository.deleteAllByUserId(userId);
            
        } catch (JwtException e) {
            throw new RuntimeException("Invalid access token");
        }
    }

    public boolean authenticateUser(String accessToken) {
        try {
            // 首先检查 token 是否在黑名单中
            String blacklistKey = "jwt_blacklist:" + accessToken;
            Boolean isBlacklisted = redisTemplate.hasKey(blacklistKey);
            if (Boolean.TRUE.equals(isBlacklisted)) {
                return false;
            }

            // 验证 token 的有效性
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(accessToken);
            return true;
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Access token has expired");
        } catch (JwtException e) {
            return false;
        }
    }

    public String getUserIdFromToken(String token){
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
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

    @Transactional
    public void unregisterUser(String accessToken) {
        try {
            // 解析token获取用户信息
            var claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(accessToken)
                    .getBody();

            Long userId = Long.parseLong(claims.getSubject());

            // 删除用户相关的所有token
            accessTokenRepository.deleteAllByUserId(userId);
            refreshTokenRepository.deleteAllByUserId(userId);

            // 删除用户
            userRepository.deleteById(userId);

        } catch (JwtException e) {
            throw new RuntimeException("Invalid access token");
        }
    }
}
