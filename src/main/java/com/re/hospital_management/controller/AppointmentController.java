package com.re.hospital_management.controller;

import com.re.hospital_management.dto.ApiResponse;
import com.re.hospital_management.dto.AppointmentCreateDTO;
import com.re.hospital_management.dto.AppointmentResponseDTO;
import com.re.hospital_management.dto.AppointmentStatusUpdateDTO;
import com.re.hospital_management.security.CustomUserDetails;
import com.re.hospital_management.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // Only PATIENT (or ADMIN for admin-created) can book
    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> bookAppointment(
            @Valid @RequestBody AppointmentCreateDTO createDTO) {
        AppointmentResponseDTO responseDTO = appointmentService.bookAppointment(createDTO);
        ApiResponse<AppointmentResponseDTO> response = ApiResponse.<AppointmentResponseDTO>builder()
                .status(HttpStatus.CREATED.value())
                .message("Appointment booked successfully")
                .data(responseDTO)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // PATIENT can see their own; DOCTOR and ADMIN can see any patient's
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR') or (hasRole('PATIENT') and #patientId == authentication.principal.id)")
    public ResponseEntity<ApiResponse<Page<AppointmentResponseDTO>>> getPatientAppointments(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<AppointmentResponseDTO> appointments = appointmentService.getPatientAppointments(patientId, page, size);
        ApiResponse<Page<AppointmentResponseDTO>> response = ApiResponse.<Page<AppointmentResponseDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Appointments retrieved successfully")
                .data(appointments)
                .build();
        return ResponseEntity.ok(response);
    }

    // Only ADMIN or DOCTOR can see all appointments
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<Page<AppointmentResponseDTO>>> getAllAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<AppointmentResponseDTO> appointments = appointmentService.getAllAppointments(page, size);
        ApiResponse<Page<AppointmentResponseDTO>> response = ApiResponse.<Page<AppointmentResponseDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("All appointments retrieved successfully")
                .data(appointments)
                .build();
        return ResponseEntity.ok(response);
    }

    // Only ADMIN or DOCTOR can update appointment status (approve/cancel/complete)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> updateAppointmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusUpdateDTO updateDTO,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Long actorUserId = currentUser != null ? currentUser.getId() : null;
        AppointmentResponseDTO responseDTO = appointmentService.updateAppointmentStatus(id, updateDTO, actorUserId);
        ApiResponse<AppointmentResponseDTO> response = ApiResponse.<AppointmentResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Appointment status updated successfully")
                .data(responseDTO)
                .build();
        return ResponseEntity.ok(response);
    }
}
