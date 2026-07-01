package com.re.hospital_management.controller;

import com.re.hospital_management.dto.ApiResponse;
import com.re.hospital_management.dto.MedicalRecordResponseDTO;
import com.re.hospital_management.security.CustomUserDetails;
import com.re.hospital_management.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    // Only DOCTOR or ADMIN can upload medical records
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<MedicalRecordResponseDTO>> uploadMedicalRecord(
            @RequestParam("patientId") Long patientId,
            @RequestParam("appointmentId") Long appointmentId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file) {

        MedicalRecordResponseDTO responseDTO = medicalRecordService.uploadRecord(patientId, appointmentId, description, file);

        ApiResponse<MedicalRecordResponseDTO> response = ApiResponse.<MedicalRecordResponseDTO>builder()
                .status(HttpStatus.CREATED.value())
                .message("File uploaded successfully")
                .data(responseDTO)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ADMIN/DOCTOR can view any patient's records; PATIENT can only view their own
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<MedicalRecordResponseDTO>>> getPatientRecords(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        boolean isPatient = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));
        if (isPatient && !currentUser.getId().equals(patientId)) {
            throw new AccessDeniedException("Patients can only view their own medical records");
        }

        Page<MedicalRecordResponseDTO> records = medicalRecordService.getPatientRecords(patientId, page, size);

        ApiResponse<Page<MedicalRecordResponseDTO>> response = ApiResponse.<Page<MedicalRecordResponseDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched patient medical records successfully")
                .data(records)
                .build();

        return ResponseEntity.ok(response);
    }

    // Only DOCTOR or ADMIN can update diagnosis
    @PutMapping("/{id}/diagnosis")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<MedicalRecordResponseDTO>> updateDiagnosis(
            @PathVariable Long id,
            @RequestParam("description") String description) {

        MedicalRecordResponseDTO updatedRecord = medicalRecordService.updateDiagnosis(id, description);

        ApiResponse<MedicalRecordResponseDTO> response = ApiResponse.<MedicalRecordResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Diagnosis updated successfully")
                .data(updatedRecord)
                .build();

        return ResponseEntity.ok(response);
    }
}
