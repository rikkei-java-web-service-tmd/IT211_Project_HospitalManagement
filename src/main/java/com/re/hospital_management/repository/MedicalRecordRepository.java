package com.re.hospital_management.repository;

import com.re.hospital_management.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    org.springframework.data.domain.Page<MedicalRecord> findByPatientId(Long patientId, org.springframework.data.domain.Pageable pageable);
    List<MedicalRecord> findByAppointmentId(Long appointmentId);
}
