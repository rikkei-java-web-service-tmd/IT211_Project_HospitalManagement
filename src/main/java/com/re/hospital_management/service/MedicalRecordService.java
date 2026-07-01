package com.re.hospital_management.service;

import com.re.hospital_management.dto.MedicalRecordResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface MedicalRecordService {
    MedicalRecordResponseDTO uploadRecord(Long patientId, Long appointmentId, String description, MultipartFile file);
    org.springframework.data.domain.Page<MedicalRecordResponseDTO> getPatientRecords(Long patientId, int page, int size);
    MedicalRecordResponseDTO updateDiagnosis(Long id, String diagnosis);
}
