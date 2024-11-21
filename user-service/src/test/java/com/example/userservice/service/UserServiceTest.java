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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.userservice.model.AccessToken;
import com.example.userservice.model.RefreshToken;
import com.example.userservice.model.User;
import com.example.userservice.repository.AccessTokenRepository;
import com.example.userservice.repository.RefreshTokenRepository;
import com.example.userservice.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccessTokenRepository accessTokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @InjectMocks
    private UserService userService;

    private User testUser;

    private PasswordEncoder encoder = new BCryptPasswordEncoder();

    private AccessToken validToken;
    private AccessToken expiredToken;

    @BeforeEach
    public void setUp() {
        String encryptedPassword = encoder.encode("password");

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(encryptedPassword);
        testUser.setEmail("test@example.com");
        testUser.setRole("user");

        validToken = new AccessToken();
        validToken.setToken("validToken");
        validToken.setExpiration(LocalDateTime.now().plusMinutes(30));

        expiredToken = new AccessToken();
        expiredToken.setToken("expiredToken");
        expiredToken.setExpiration(LocalDateTime.now().minusMinutes(30));
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
    public void test_DeleteUserOk() {
        doNothing().when(userRepository).deleteById(anyLong());
        doNothing().when(accessTokenRepository).deleteAllByUserId(anyLong());
        doNothing().when(refreshTokenRepository).deleteAllByUserId(anyLong());

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
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
        assertEquals(36, tokens.get("accessToken").length());
        assertEquals(36, tokens.get("refreshToken").length());
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

    @Test
    public void test_LogoutUserInvalidToken() {
        String accessTokenValue = UUID.randomUUID().toString();
        when(accessTokenRepository.findByToken(accessTokenValue)).thenReturn(Optional.empty());

        userService.logoutUser(accessTokenValue);

        verify(accessTokenRepository, times(0)).deleteAllByUserId(anyLong());
        verify(refreshTokenRepository, times(0)).deleteAllByUserId(anyLong());
    }


    @Test
    void testAuthenticateUser_ValidToken() {
        when(accessTokenRepository.findByToken("validToken")).thenReturn(Optional.of(validToken));

        boolean isAuthenticated = userService.authenticateUser("validToken");

        assertTrue(isAuthenticated, "Valid token should authenticate successfully.");
        verify(accessTokenRepository, times(1)).findByToken("validToken");
    }

    @Test
    void testAuthenticateUser_ExpiredToken() {
        when(accessTokenRepository.findByToken("expiredToken")).thenReturn(Optional.of(expiredToken));

        boolean isAuthenticated = userService.authenticateUser("expiredToken");

        assertFalse(isAuthenticated, "Expired token should not authenticate.");
        verify(accessTokenRepository, times(1)).findByToken("expiredToken");
    }

    @Test
    void testAuthenticateUser_TokenNotFound() {
        when(accessTokenRepository.findByToken("nonexistentToken")).thenReturn(Optional.empty());

        boolean isAuthenticated = userService.authenticateUser("nonexistentToken");

        assertFalse(isAuthenticated, "Nonexistent token should not authenticate.");
        verify(accessTokenRepository, times(1)).findByToken("nonexistentToken");
    }
}
