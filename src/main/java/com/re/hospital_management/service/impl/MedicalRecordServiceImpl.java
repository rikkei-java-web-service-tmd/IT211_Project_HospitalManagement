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
        return mapToDTO(savedRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<MedicalRecordResponseDTO> getPatientRecords(Long patientId, int page, int size) {
        if (!userRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id: " + patientId);
        }
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("uploadedAt").descending());
        return medicalRecordRepository.findByPatientId(patientId, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional
    public MedicalRecordResponseDTO updateDiagnosis(Long id, String diagnosis) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + id));
        record.setDescription(diagnosis);
        return mapToDTO(medicalRecordRepository.save(record));
    }

    private MedicalRecordResponseDTO mapToDTO(MedicalRecord record) {
        return MedicalRecordResponseDTO.builder()
                .id(record.getId())
                .patientId(record.getPatient().getId())
                .appointmentId(record.getAppointment().getId())
                .filePath(record.getFilePath())
                .description(record.getDescription())
                .uploadedAt(record.getUploadedAt())
                .build();
    }
}
