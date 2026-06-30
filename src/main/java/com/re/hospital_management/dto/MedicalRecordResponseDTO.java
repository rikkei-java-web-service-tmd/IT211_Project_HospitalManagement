package com.re.hospital_management.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MedicalRecordResponseDTO {
    private Long id;
    private Long patientId;
    private Long appointmentId;
    private String filePath;
    private String description;
    private LocalDateTime uploadedAt;
}
