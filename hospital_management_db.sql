CREATE DATABASE IF NOT EXISTS hospital_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hospital_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date DATETIME(6) NOT NULL,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT DEFAULT NULL,
    date DATE NOT NULL,
    time_slot VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    symptom_description TEXT,
    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS medical_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    appointment_id BIGINT NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    uploaded_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_medical_record_patient FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_medical_record_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE
);

INSERT INTO users (username, password_hash, role, is_active) VALUES 
('admin1', '$2b$10$vzx1QDSGgPDa0Vanx0N68umoFL2lFsCOA8lAcZ30j03pMdw/HlIUK', 'ADMIN', TRUE),
('doctor1', '$2b$10$vzx1QDSGgPDa0Vanx0N68umoFL2lFsCOA8lAcZ30j03pMdw/HlIUK', 'DOCTOR', TRUE),
('patient1', '$2b$10$vzx1QDSGgPDa0Vanx0N68umoFL2lFsCOA8lAcZ30j03pMdw/HlIUK', 'PATIENT', TRUE)
ON DUPLICATE KEY UPDATE username=username;
