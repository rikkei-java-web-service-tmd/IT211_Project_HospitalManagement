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
}
