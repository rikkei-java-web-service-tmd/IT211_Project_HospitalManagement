package com.re.hospital_management.controller;

import com.re.hospital_management.dto.ApiResponse;
import com.re.hospital_management.dto.MedicalRecordResponseDTO;
import com.re.hospital_management.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping("/upload")
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

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<MedicalRecordResponseDTO>>> getPatientRecords(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        org.springframework.data.domain.Page<MedicalRecordResponseDTO> records = medicalRecordService.getPatientRecords(patientId, page, size);
        
        ApiResponse<org.springframework.data.domain.Page<MedicalRecordResponseDTO>> response = ApiResponse.<org.springframework.data.domain.Page<MedicalRecordResponseDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched patient medical records successfully")
                .data(records)
                .build();
                
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/diagnosis")
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
