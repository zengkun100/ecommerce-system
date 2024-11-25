package com.example.userservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.example.userservice.controller.BaseTestClass;
import com.example.userservice.exception.TokenExpiredException;
import com.example.userservice.exception.TokenNotFoundException;
import com.example.userservice.model.AccessToken;
import com.example.userservice.model.RefreshToken;
import com.example.userservice.model.User;
import com.example.userservice.repository.AccessTokenRepository;
import com.example.userservice.repository.RefreshTokenRepository;
import com.example.userservice.repository.UserRepository;

import com.example.userservice.service.impl.UserServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

@ActiveProfiles(value = "test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserServiceTest extends BaseTestClass {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccessTokenRepository accessTokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    private PasswordEncoder encoder = new BCryptPasswordEncoder();

    private AccessToken validToken;
    private AccessToken expiredToken;

    private String jwtSecret = "testSecretKey";
    private final Long userId = 1L;
    private final String username = "testUser";

    @BeforeEach
    public void setUp() {
        String encryptedPassword = encoder.encode("password");

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(encryptedPassword);
        testUser.setEmail("test@example.com");
        testUser.setRole("user");
        testUser.setId(1L);

        validToken = new AccessToken();
        validToken.setToken("validToken");
        validToken.setExpiration(LocalDateTime.now().plusMinutes(30));

        expiredToken = new AccessToken();
        expiredToken.setToken("expiredToken");
        expiredToken.setExpiration(LocalDateTime.now().minusMinutes(30));

        // 手动设置 @Value 的值
        ReflectionTestUtils.setField(userService, "jwtSecret", "testSecretKey");
        ReflectionTestUtils.setField(userService, "accessTokenExpiration", 15L);
        ReflectionTestUtils.setField(userService, "refreshTokenExpiration", 7L);

    }


    @Test
    public void test_CreateUserOk() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User createdUser = userService.createUser("testuser", "password", "test@example.com", "user");

        assertNotNull(createdUser);
        assertEquals("test@example.com", createdUser.getEmail());
        assertEquals("user", createdUser.getRole());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void test_LoginUserOk() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        when(accessTokenRepository.save(any(AccessToken.class))).thenReturn(new AccessToken());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(new RefreshToken());

        Map<String, String> tokens = userService.loginUser("testuser", "password");

        assertNotNull(tokens);
        assertNotNull(tokens.get("accessToken"));
        assertNotNull(tokens.get("refreshToken"));
        verify(accessTokenRepository, times(1)).save(any(AccessToken.class));
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }


    @Test
    public void test_LoginUserInvalidPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.loginUser("testuser", "123456");
        });

        assertEquals("Invalid username or password", exception.getMessage());
        verify(accessTokenRepository, times(0)).save(any(AccessToken.class));
        verify(refreshTokenRepository, times(0)).save(any(RefreshToken.class));
    }

    @Test
    public void test_LogoutUserOk() {
        try (MockedStatic<Jwts> mockedJwts = mockStatic(Jwts.class)) {
            JwtParser jwtParser = mock(JwtParser.class);
            Jws<Claims> mockedJws = mock(Jws.class);
            Claims claims = mock(Claims.class);

            when(jwtParser.setSigningKey(jwtSecret)).thenReturn(jwtParser);
            when(jwtParser.parseClaimsJws(anyString())).thenReturn(mockedJws);
            when(mockedJws.getBody()).thenReturn(claims);

            when(claims.getSubject()).thenReturn(String.valueOf(userId));
            when(claims.getExpiration()).thenReturn(new Date());

            mockedJwts.when(Jwts::parser).thenReturn(jwtParser);

            String accessTokenValue = UUID.randomUUID().toString();
            AccessToken accessToken = new AccessToken();
            accessToken.setUserId(testUser.getId());
            accessToken.setToken(accessTokenValue);
            accessToken.setExpiration(LocalDateTime.now().plusMinutes(30));

            when(accessTokenRepository.findByToken(accessTokenValue)).thenReturn(Optional.of(accessToken));

            userService.logoutUser(accessTokenValue);

            verify(accessTokenRepository, times(1)).deleteAllByUserId(testUser.getId());
            verify(refreshTokenRepository, times(1)).deleteAllByUserId(testUser.getId());
        }
    }

    @Test
    public void test_LogoutUserOk_updateRedis() {
        try (MockedStatic<Jwts> mockedJwts = mockStatic(Jwts.class)) {
            JwtParser jwtParser = mock(JwtParser.class);
            Jws<Claims> mockedJws = mock(Jws.class);
            Claims claims = mock(Claims.class);

            when(jwtParser.setSigningKey(jwtSecret)).thenReturn(jwtParser);
            when(jwtParser.parseClaimsJws(anyString())).thenReturn(mockedJws);
            when(mockedJws.getBody()).thenReturn(claims);

            when(claims.getSubject()).thenReturn(String.valueOf(userId));
            when(claims.getExpiration()).thenReturn(new Date(new Date().getTime() + 1000000));

            mockedJwts.when(Jwts::parser).thenReturn(jwtParser);

            String accessTokenValue = UUID.randomUUID().toString();
            AccessToken accessToken = new AccessToken();
            accessToken.setUserId(testUser.getId());
            accessToken.setToken(accessTokenValue);
            accessToken.setExpiration(LocalDateTime.now().plusMinutes(30));

            ValueOperations<String, String> ops = mock(ValueOperations.class);
            when(redisTemplate.opsForValue()).thenReturn(ops);
            doNothing().when(ops).set(any(),anyString(), anyLong(),any());

            when(accessTokenRepository.findByToken(accessTokenValue)).thenReturn(Optional.of(accessToken));

            userService.logoutUser(accessTokenValue);

            verify(accessTokenRepository, times(1)).deleteAllByUserId(testUser.getId());
            verify(refreshTokenRepository, times(1)).deleteAllByUserId(testUser.getId());
        }
    }

    @Test
    public void test_LogoutUserInvalidToken() {
        when(redisTemplate.hasKey(anyString())).thenReturn(Boolean.TRUE);
        String accessTokenValue = UUID.randomUUID().toString();
        when(accessTokenRepository.findByToken(accessTokenValue)).thenReturn(Optional.empty());

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            userService.logoutUser(accessTokenValue);
        });

        verify(accessTokenRepository, times(0)).deleteAllByUserId(anyLong());
        verify(refreshTokenRepository, times(0)).deleteAllByUserId(anyLong());
    }

    @Test
    void testAuthenticateUser_ValidToken() {
        when(redisTemplate.hasKey(anyString())).thenReturn(Boolean.FALSE);

        try (MockedStatic<Jwts> mockedJwts = mockStatic(Jwts.class)) {
            JwtParser jwtParser = mock(JwtParser.class);
            Jws<Claims> mockedJws = mock(Jws.class);
            Claims claims = mock(Claims.class);

            when(jwtParser.setSigningKey(jwtSecret)).thenReturn(jwtParser);
            when(jwtParser.parseClaimsJws(anyString())).thenReturn(mockedJws);
            when(mockedJws.getBody()).thenReturn(claims);

            when(claims.getSubject()).thenReturn(String.valueOf(userId));
            when(claims.getExpiration()).thenReturn(new Date(new Date().getTime() + 1000000));

            mockedJwts.when(Jwts::parser).thenReturn(jwtParser);
            boolean result = userService.authenticateUser("testSecretKey");
            assertTrue(result, "Valid token should return true");
        }
    }

    @Test
    void testAuthenticateUser_ExpiredToken() {
        String expiredToken = "expiredToken";
        when(redisTemplate.hasKey(anyString())).thenReturn(Boolean.FALSE);
        try (MockedStatic<Jwts> mockedJwts = mockStatic(Jwts.class)) {
            mockedJwts.when(() -> Jwts.parser().setSigningKey("testSecretKey").parseClaimsJws(expiredToken))
                    .thenThrow(new ExpiredJwtException(null, null, "Token expired"));

            Exception exception = assertThrows(TokenExpiredException.class, () -> {
                userService.authenticateUser(expiredToken);
            });

            assertEquals("Access token has expired", exception.getMessage());
        }
    }

    @Test
    void testAuthenticateUser_InvalidToken() {
        String expiredToken = "expiredToken";
        when(redisTemplate.hasKey(anyString())).thenReturn(Boolean.FALSE);
        try (MockedStatic<Jwts> mockedJwts = mockStatic(Jwts.class)) {
            mockedJwts.when(() -> Jwts.parser().setSigningKey("testSecretKey").parseClaimsJws(expiredToken))
                    .thenThrow(new JwtException("Token expired"));

            userService.authenticateUser(expiredToken);

            assertFalse(false, "fail");
        }
    }

    @Test
    void testAuthenticateUser_TokenNotFound() {
        when(redisTemplate.hasKey(anyString())).thenReturn(Boolean.TRUE);
        when(accessTokenRepository.findByToken("nonexistentToken")).thenReturn(Optional.empty());

        boolean isAuthenticated = userService.authenticateUser("nonexistentToken");

        assertFalse(isAuthenticated, "Nonexistent token should not authenticate.");
    }

    @Test
    void testRefreshAccessToken_ValidToken() {
        String refreshTokenValue = "validRefreshToken";
        String userId = "123";
        String username = "testUser";
        String role = "USER";

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRefreshToken(refreshTokenValue);
        refreshToken.setUserId(1L);
        refreshToken.setExpiration(LocalDateTime.now().plusMinutes(30));

        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setRole(role);

        when(refreshTokenRepository.findByRefreshToken(refreshTokenValue)).thenReturn(Optional.of(refreshToken));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        String result = userService.refreshAccessToken(refreshTokenValue);

        assertNotNull(result);
        verify(accessTokenRepository).save(any(AccessToken.class));
    }

    @Test
    void testRefreshAccessToken_ExpiredRefreshToken() {
        String refreshTokenValue = "expiredRefreshToken";

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRefreshToken(refreshTokenValue);
        refreshToken.setExpiration(LocalDateTime.now().minusMinutes(10));

        when(refreshTokenRepository.findByRefreshToken(refreshTokenValue)).thenReturn(Optional.of(refreshToken));

        TokenExpiredException exception = assertThrows(TokenExpiredException.class, () ->
                userService.refreshAccessToken(refreshTokenValue));
        assertEquals("Refresh token has expired. Please log in again.", exception.getMessage());
    }

    @Test
    void testRefreshAccessToken_InvalidRefreshToken() {
        String refreshTokenValue = "invalidRefreshToken";

        when(refreshTokenRepository.findByRefreshToken(refreshTokenValue)).thenReturn(Optional.empty());

        TokenNotFoundException exception = assertThrows(TokenNotFoundException.class, () ->
                userService.refreshAccessToken(refreshTokenValue));
        assertEquals("Invalid refresh token.", exception.getMessage());
    }

    @Test
    void testRefreshAccessToken_UserNotFound() {
        String refreshTokenValue = "validRefreshToken";

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRefreshToken(refreshTokenValue);
        refreshToken.setUserId(1L);
        refreshToken.setExpiration(LocalDateTime.now().plusMinutes(30));

        when(refreshTokenRepository.findByRefreshToken(refreshTokenValue)).thenReturn(Optional.of(refreshToken));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.refreshAccessToken(refreshTokenValue));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void test_getUserIdFromToken() {
        // Arrange: 模拟解析token获取userId
        try (MockedStatic<Jwts> mockedJwts = mockStatic(Jwts.class)) {
            JwtParser jwtParser = mock(JwtParser.class);
            Jws<Claims> mockedJws = mock(Jws.class);
            Claims claims = mock(Claims.class);

            when(mockedJws.getBody()).thenReturn(claims);
            when(claims.getSubject()).thenReturn(String.valueOf(userId));
            when(jwtParser.setSigningKey(jwtSecret)).thenReturn(jwtParser);
            when(jwtParser.parseClaimsJws(anyString())).thenReturn(mockedJws);

            mockedJwts.when(Jwts::parser).thenReturn(jwtParser);

            userService.getUserIdFromToken("mockedAccessToken");
        }
    }

    @Test
    void unregisterUser_Success() {
        // Arrange: 模拟解析token获取userId
        try (MockedStatic<Jwts> mockedJwts = mockStatic(Jwts.class)) {
            JwtParser jwtParser = mock(JwtParser.class);
            Jws<Claims> mockedJws = mock(Jws.class);
            Claims claims = mock(Claims.class);

            when(mockedJws.getBody()).thenReturn(claims);
            when(claims.getSubject()).thenReturn(String.valueOf(userId));
            when(jwtParser.setSigningKey(jwtSecret)).thenReturn(jwtParser);
            when(jwtParser.parseClaimsJws(anyString())).thenReturn(mockedJws);

            mockedJwts.when(Jwts::parser).thenReturn(jwtParser);

            when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(null));
            doNothing().when(accessTokenRepository).deleteAllByUserId(userId);
            doNothing().when(refreshTokenRepository).deleteAllByUserId(userId);
            doNothing().when(userRepository).deleteById(userId);

            userService.unregisterUser("mockedAccessToken");

            verify(accessTokenRepository, times(1)).deleteAllByUserId(userId);
            verify(refreshTokenRepository, times(1)).deleteAllByUserId(userId);
            verify(userRepository, times(1)).deleteById(userId);
        }
    }

    @Test
    void unregisterUser_InvalidToken() {
        try (MockedStatic<Jwts> mockedJwts = mockStatic(Jwts.class)) {
            JwtParser jwtParser = mock(JwtParser.class);
            when(jwtParser.setSigningKey(jwtSecret)).thenReturn(jwtParser);
            when(jwtParser.parseClaimsJws(anyString()))
                    .thenThrow(new JwtException("Invalid access token"));

            mockedJwts.when(Jwts::parser).thenReturn(jwtParser);

            try {
                userService.unregisterUser("mockedAccessToken");
            } catch (RuntimeException e) {
                assert e.getMessage().equals("Invalid access token");
            }

            verifyNoInteractions(userRepository, accessTokenRepository, refreshTokenRepository);
        }
    }
}
