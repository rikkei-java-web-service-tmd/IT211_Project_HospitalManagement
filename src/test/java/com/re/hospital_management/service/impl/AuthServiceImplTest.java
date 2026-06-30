package com.re.hospital_management.service.impl;

import com.re.hospital_management.dto.JwtAuthResponseDTO;
import com.re.hospital_management.dto.LoginRequestDTO;
import com.re.hospital_management.entity.RefreshToken;
import com.re.hospital_management.entity.User;
import com.re.hospital_management.repository.RefreshTokenRepository;
import com.re.hospital_management.repository.UserRepository;
import com.re.hospital_management.security.JwtTokenProvider;
import com.re.hospital_management.service.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenDurationMs", 86400000L);
        ReflectionTestUtils.setField(authService, "jwtExpirationMs", 3600000L);
    }

    @Test
    void testLogin_Success() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("testuser");
        request.setPassword("password");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken("testuser")).thenReturn("mockAccessToken");

        User user = new User();
        user.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        JwtAuthResponseDTO response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("mockAccessToken", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(refreshTokenRepository).deleteByUser(user);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void testLogout_Success() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Act
        authService.logout("testuser", "mockAccessToken");

        // Assert
        verify(refreshTokenRepository).deleteByUser(user);
        verify(tokenBlacklistService).blacklistToken(eq("mockAccessToken"), anyLong());
    }
}
