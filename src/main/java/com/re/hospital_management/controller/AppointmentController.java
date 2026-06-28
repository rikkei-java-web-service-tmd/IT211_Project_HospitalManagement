package com.re.hospital_management.controller;

import com.re.hospital_management.dto.ApiResponse;
import com.re.hospital_management.dto.AppointmentCreateDTO;
import com.re.hospital_management.dto.AppointmentResponseDTO;
import com.re.hospital_management.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> bookAppointment(@Valid @RequestBody AppointmentCreateDTO createDTO) {
        AppointmentResponseDTO responseDTO = appointmentService.bookAppointment(createDTO);
        ApiResponse<AppointmentResponseDTO> response = ApiResponse.<AppointmentResponseDTO>builder()
                .status(HttpStatus.CREATED.value())
                .message("Appointment booked successfully")
                .data(responseDTO)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @org.springframework.web.bind.annotation.GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<AppointmentResponseDTO>>> getPatientAppointments(
            @org.springframework.web.bind.annotation.PathVariable Long patientId,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size) {
        
        org.springframework.data.domain.Page<AppointmentResponseDTO> appointments = appointmentService.getPatientAppointments(patientId, page, size);
        ApiResponse<org.springframework.data.domain.Page<AppointmentResponseDTO>> response = ApiResponse.<org.springframework.data.domain.Page<AppointmentResponseDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Appointments retrieved successfully")
                .data(appointments)
                .build();
        return ResponseEntity.ok(response);
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> updateAppointmentStatus(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @Valid @RequestBody com.re.hospital_management.dto.AppointmentStatusUpdateDTO updateDTO) {
        
        AppointmentResponseDTO responseDTO = appointmentService.updateAppointmentStatus(id, updateDTO);
        ApiResponse<AppointmentResponseDTO> response = ApiResponse.<AppointmentResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Appointment status updated successfully")
                .data(responseDTO)
                .build();
        return ResponseEntity.ok(response);
    }
}
