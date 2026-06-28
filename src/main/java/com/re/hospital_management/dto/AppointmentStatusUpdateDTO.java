package com.re.hospital_management.dto;

import com.re.hospital_management.enums.StatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentStatusUpdateDTO {
    @NotNull(message = "Status is required")
    private StatusEnum status;
}
