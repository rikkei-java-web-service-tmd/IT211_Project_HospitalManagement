package com.re.hospital_management.service.impl;

import com.re.hospital_management.dto.MedicalRecordResponseDTO;
import com.re.hospital_management.entity.Appointment;
import com.re.hospital_management.entity.MedicalRecord;
import com.re.hospital_management.entity.User;
import com.re.hospital_management.exception.ResourceNotFoundException;
import com.re.hospital_management.repository.AppointmentRepository;
import com.re.hospital_management.repository.MedicalRecordRepository;
import com.re.hospital_management.repository.UserRepository;
import com.re.hospital_management.service.FileStorageService;
import com.re.hospital_management.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public MedicalRecordResponseDTO uploadRecord(Long patientId, Long appointmentId, String description, MultipartFile file) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        String fileName = fileStorageService.storeFile(file);

        MedicalRecord record = MedicalRecord.builder()
                .patient(patient)
                .appointment(appointment)
                .filePath(fileName)
                .description(description)
                .build();

        MedicalRecord savedRecord = medicalRecordRepository.save(record);

        return MedicalRecordResponseDTO.builder()
                .id(savedRecord.getId())
                .patientId(patient.getId())
                .appointmentId(appointment.getId())
                .filePath(savedRecord.getFilePath())
                .description(savedRecord.getDescription())
                .uploadedAt(savedRecord.getUploadedAt())
                .build();
    }
}
