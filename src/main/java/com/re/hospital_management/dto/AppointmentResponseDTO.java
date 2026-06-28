package com.re.hospital_management.dto;

import com.re.hospital_management.enums.StatusEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AppointmentResponseDTO {
    private Long id;
    private LocalDate date;
    private String timeSlot;
    private StatusEnum status;
    private String symptomDescription;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
}
