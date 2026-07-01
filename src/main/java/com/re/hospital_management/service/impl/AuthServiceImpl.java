package com.re.hospital_management.service.impl;

import com.re.hospital_management.dto.JwtAuthResponseDTO;
import com.re.hospital_management.dto.LoginRequestDTO;
import com.re.hospital_management.dto.TokenRefreshRequestDTO;
import com.re.hospital_management.entity.RefreshToken;
import com.re.hospital_management.entity.User;
import com.re.hospital_management.repository.RefreshTokenRepository;
import com.re.hospital_management.repository.UserRepository;
import com.re.hospital_management.security.JwtTokenProvider;
import com.re.hospital_management.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final com.re.hospital_management.service.TokenBlacklistService tokenBlacklistService;

    @Value("${app.jwt.refresh-expiration-ms:86400000}")
    private Long refreshTokenDurationMs;

    @Value("${app.jwt.expiration-ms:3600000}")
    private Long jwtExpirationMs;

    @Override
    @Transactional
    public JwtAuthResponseDTO login(LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String jwt = tokenProvider.generateToken(loginRequest.getUsername(), user.getId(), user.getRole().name());

        RefreshToken refreshToken = createRefreshToken(user);

        return JwtAuthResponseDTO.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Override
    @Transactional
    public JwtAuthResponseDTO refreshToken(TokenRefreshRequestDTO refreshRequest) {
        String requestRefreshToken = refreshRequest.getRefreshToken();

        return refreshTokenRepository.findByToken(requestRefreshToken)
                .map(this::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = tokenProvider.generateToken(user.getUsername(), user.getId(), user.getRole().name());
                    return JwtAuthResponseDTO.builder()
                            .accessToken(token)
                            .refreshToken(requestRefreshToken)
                            .build();
                })
                .orElseThrow(() -> new IllegalArgumentException("Refresh token is invalid or expired"));
    }

    @Override
    @Transactional
    public void logout(String username, String accessToken) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        refreshTokenRepository.deleteByUser(user);
        if (accessToken != null) {
            tokenBlacklistService.blacklistToken(accessToken, jwtExpirationMs);
        }
    }

    private RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    private RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new IllegalArgumentException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }
}
