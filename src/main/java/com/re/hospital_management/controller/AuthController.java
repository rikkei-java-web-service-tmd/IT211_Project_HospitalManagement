package com.re.hospital_management.controller;

import com.re.hospital_management.dto.ApiResponse;
import com.re.hospital_management.dto.JwtAuthResponseDTO;
import com.re.hospital_management.dto.LoginRequestDTO;
import com.re.hospital_management.dto.TokenRefreshRequestDTO;
import com.re.hospital_management.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthResponseDTO>> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        JwtAuthResponseDTO responseDTO = authService.login(loginRequest);
        ApiResponse<JwtAuthResponseDTO> response = ApiResponse.<JwtAuthResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("User logged in successfully")
                .data(responseDTO)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtAuthResponseDTO>> refreshToken(@Valid @RequestBody TokenRefreshRequestDTO refreshRequest) {
        JwtAuthResponseDTO responseDTO = authService.refreshToken(refreshRequest);
        ApiResponse<JwtAuthResponseDTO> response = ApiResponse.<JwtAuthResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Token refreshed successfully")
                .data(responseDTO)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logoutUser(Authentication authentication, jakarta.servlet.http.HttpServletRequest request) {
        if (authentication != null && authentication.getName() != null) {
            String bearerToken = request.getHeader("Authorization");
            String accessToken = null;
            if (org.springframework.util.StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                accessToken = bearerToken.substring(7);
            }
            authService.logout(authentication.getName(), accessToken);
        }
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("User logged out successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
