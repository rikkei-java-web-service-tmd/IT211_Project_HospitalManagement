package com.re.hospital_management.service;

import com.re.hospital_management.dto.JwtAuthResponseDTO;
import com.re.hospital_management.dto.LoginRequestDTO;
import com.re.hospital_management.dto.TokenRefreshRequestDTO;

public interface AuthService {
    JwtAuthResponseDTO login(LoginRequestDTO loginRequest);
    JwtAuthResponseDTO refreshToken(TokenRefreshRequestDTO refreshRequest);
    void logout(String username, String accessToken);
}
