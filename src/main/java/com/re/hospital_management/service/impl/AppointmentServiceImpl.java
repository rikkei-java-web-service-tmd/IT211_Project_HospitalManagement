package com.re.hospital_management.service.impl;

import com.re.hospital_management.dto.AppointmentCreateDTO;
import com.re.hospital_management.dto.AppointmentResponseDTO;
import com.re.hospital_management.entity.Appointment;
import com.re.hospital_management.entity.User;
import com.re.hospital_management.enums.StatusEnum;
import com.re.hospital_management.exception.ResourceNotFoundException;
import com.re.hospital_management.repository.AppointmentRepository;
import com.re.hospital_management.repository.UserRepository;
import com.re.hospital_management.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Override
    public AppointmentResponseDTO bookAppointment(AppointmentCreateDTO createDTO) {
        User patient = userRepository.findById(createDTO.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + createDTO.getPatientId()));

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .date(createDTO.getDate())
                .timeSlot(createDTO.getTimeSlot())
                .status(StatusEnum.PENDING)
                .symptomDescription(createDTO.getSymptomDescription())
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return mapToDTO(savedAppointment);
    }

    @Override
    public org.springframework.data.domain.Page<AppointmentResponseDTO> getPatientAppointments(Long patientId, int page, int size) {
        if (!userRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id: " + patientId);
        }
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("date").descending());
        org.springframework.data.domain.Page<Appointment> appointments = appointmentRepository.findByPatientId(patientId, pageable);
        return appointments.map(this::mapToDTO);
    }

    @Override
    public org.springframework.data.domain.Page<AppointmentResponseDTO> getAllAppointments(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("date").descending());
        org.springframework.data.domain.Page<Appointment> appointments = appointmentRepository.findAll(pageable);
        return appointments.map(this::mapToDTO);
    }

    @Override
    public AppointmentResponseDTO updateAppointmentStatus(Long id, com.re.hospital_management.dto.AppointmentStatusUpdateDTO updateDTO) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        
        appointment.setStatus(updateDTO.getStatus());
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return mapToDTO(savedAppointment);
    }

    private AppointmentResponseDTO mapToDTO(Appointment appointment) {
        return AppointmentResponseDTO.builder()
                .id(appointment.getId())
                .date(appointment.getDate())
                .timeSlot(appointment.getTimeSlot())
                .status(appointment.getStatus())
                .symptomDescription(appointment.getSymptomDescription())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getUsername())
                .doctorId(appointment.getDoctor() != null ? appointment.getDoctor().getId() : null)
                .doctorName(appointment.getDoctor() != null ? appointment.getDoctor().getUsername() : null)
                .build();
    }
}
