package com.re.hospital_management.dto;

import com.re.hospital_management.enums.RoleEnum;
import lombok.Data;

@Data
public class UserUpdateDTO {
    private RoleEnum role;
    private Boolean isActive;
}
