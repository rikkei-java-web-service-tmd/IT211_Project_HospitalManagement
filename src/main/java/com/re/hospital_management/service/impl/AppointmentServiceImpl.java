package com.re.hospital_management.service.impl;

import com.re.hospital_management.dto.AppointmentCreateDTO;
import com.re.hospital_management.dto.AppointmentResponseDTO;
import com.re.hospital_management.dto.AppointmentStatusUpdateDTO;
import com.re.hospital_management.entity.Appointment;
import com.re.hospital_management.entity.User;
import com.re.hospital_management.enums.RoleEnum;
import com.re.hospital_management.enums.StatusEnum;
import com.re.hospital_management.exception.ResourceNotFoundException;
import com.re.hospital_management.repository.AppointmentRepository;
import com.re.hospital_management.repository.UserRepository;
import com.re.hospital_management.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
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
    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getPatientAppointments(Long patientId, int page, int size) {
        if (!userRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id: " + patientId);
        }
        Page<Appointment> appointments = appointmentRepository.findByPatientId(patientId,
                PageRequest.of(page, size, Sort.by("date").descending()));
        return appointments.map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getAllAppointments(int page, int size) {
        Page<Appointment> appointments = appointmentRepository.findAll(
                PageRequest.of(page, size, Sort.by("date").descending()));
        return appointments.map(this::mapToDTO);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO updateAppointmentStatus(Long id, AppointmentStatusUpdateDTO updateDTO, Long actorUserId) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        StatusEnum currentStatus = appointment.getStatus();
        StatusEnum newStatus = updateDTO.getStatus();

        // Validate state machine transitions
        validateTransition(currentStatus, newStatus);

        // Phase 3: Auto-assign doctor when DOCTOR approves
        if (newStatus == StatusEnum.APPROVED && actorUserId != null) {
            User actor = userRepository.findById(actorUserId).orElse(null);
            if (actor != null && actor.getRole() == RoleEnum.DOCTOR) {
                appointment.setDoctor(actor);
            }
        }

        appointment.setStatus(newStatus);
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return mapToDTO(savedAppointment);
    }

    /**
     * Phase 2: Enforce valid state transitions based on the State Transition Diagram:
     * PENDING -> APPROVED (Doctor/Admin confirms)
     * PENDING -> CANCELLED (Patient/Doctor/Admin cancels)
     * APPROVED -> COMPLETED (Doctor marks done)
     * APPROVED -> CANCELLED (Patient no-show or cancels)
     * COMPLETED / CANCELLED -> terminal states, no further transitions
     */
    private void validateTransition(StatusEnum current, StatusEnum next) {
        boolean valid = switch (current) {
            case PENDING -> next == StatusEnum.APPROVED || next == StatusEnum.CANCELLED;
            case APPROVED -> next == StatusEnum.COMPLETED || next == StatusEnum.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };

        if (!valid) {
            throw new AccessDeniedException(
                    "Invalid status transition from " + current + " to " + next);
        }
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
