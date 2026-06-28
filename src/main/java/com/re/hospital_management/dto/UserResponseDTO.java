package com.re.hospital_management.dto;

import com.re.hospital_management.enums.RoleEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDTO {
    private Long id;
    private String username;
    private RoleEnum role;
    private Boolean isActive;
}
