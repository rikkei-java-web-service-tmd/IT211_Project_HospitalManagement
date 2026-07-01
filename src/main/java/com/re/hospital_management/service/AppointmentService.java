package com.re.hospital_management.service;

import com.re.hospital_management.dto.AppointmentCreateDTO;
import com.re.hospital_management.dto.AppointmentResponseDTO;
import com.re.hospital_management.dto.AppointmentStatusUpdateDTO;

import org.springframework.data.domain.Page;

public interface AppointmentService {
    AppointmentResponseDTO bookAppointment(AppointmentCreateDTO createDTO);
    Page<AppointmentResponseDTO> getPatientAppointments(Long patientId, int page, int size);
    Page<AppointmentResponseDTO> getAllAppointments(int page, int size);
    AppointmentResponseDTO updateAppointmentStatus(Long id, AppointmentStatusUpdateDTO updateDTO, Long actorUserId);
}
