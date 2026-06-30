package com.re.hospital_management.service;

import com.re.hospital_management.dto.MedicalRecordResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface MedicalRecordService {
    MedicalRecordResponseDTO uploadRecord(Long patientId, Long appointmentId, String description, MultipartFile file);
}
