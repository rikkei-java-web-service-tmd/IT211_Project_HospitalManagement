package com.re.hospital_management.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRefreshRequestDTO {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
