package com.re.hospital_management.service.impl;

import com.re.hospital_management.dto.AppointmentCreateDTO;
import com.re.hospital_management.dto.AppointmentResponseDTO;
import com.re.hospital_management.entity.Appointment;
import com.re.hospital_management.entity.User;
import com.re.hospital_management.enums.StatusEnum;
import com.re.hospital_management.repository.AppointmentRepository;
import com.re.hospital_management.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    @Test
    void testBookAppointment_Success() {
        // Arrange
        AppointmentCreateDTO createDTO = new AppointmentCreateDTO();
        createDTO.setPatientId(1L);
        createDTO.setDate(LocalDate.now().plusDays(1));
        createDTO.setTimeSlot("10:00-11:00");
        createDTO.setSymptomDescription("Headache");

        User patient = new User();
        patient.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));

        Appointment savedAppointment = new Appointment();
        savedAppointment.setId(100L);
        savedAppointment.setPatient(patient);
        savedAppointment.setDate(createDTO.getDate());
        savedAppointment.setTimeSlot(createDTO.getTimeSlot());
        savedAppointment.setStatus(StatusEnum.PENDING);
        savedAppointment.setSymptomDescription("Headache");

        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        // Act
        AppointmentResponseDTO response = appointmentService.bookAppointment(createDTO);

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(StatusEnum.PENDING, response.getStatus());
        assertEquals("Headache", response.getSymptomDescription());
    }
}
