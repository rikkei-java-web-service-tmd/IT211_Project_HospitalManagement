-- Tạo database (nếu chưa có)
CREATE DATABASE IF NOT EXISTS hospital_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hospital_db;

-- 1. Bảng users (Bao gồm Patient, Doctor, Admin)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL COMMENT 'PATIENT, DOCTOR, ADMIN',
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 2. Bảng refresh_tokens (Lưu phiên đăng nhập)
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date DATETIME(6) NOT NULL,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Bảng appointments (Lịch khám bệnh)
CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT DEFAULT NULL,
    date DATE NOT NULL,
    time_slot VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL COMMENT 'PENDING, CONFIRMED, COMPLETED, CANCELLED',
    symptom_description TEXT,
    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 4. Bảng medical_records (Hồ sơ/Tài liệu bệnh án)
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

-- --------------------------------------------------------
-- DỮ LIỆU MẪU (OPTIONAL)
-- --------------------------------------------------------

-- Insert một vài user mẫu (Mật khẩu mặc định: password123, hash bằng BCrypt)
-- Hash của 'password123' là '$2a$10$wK/p4Xq5Yq5Yq5Yq5Yq5Yq5Yq5Yq5Yq5Yq5Yq5Yq5Yq5Yq5Yq5Yq5' (Tương đối)
INSERT INTO users (username, password_hash, role, is_active) VALUES 
('admin1', '$2a$10$r96F6wS7rL9L9D/7.K4s.Ou8eF9lV5iZ/U7P6qC9wM/r5v0D3s/Jm', 'ADMIN', TRUE),
('doctor1', '$2a$10$r96F6wS7rL9L9D/7.K4s.Ou8eF9lV5iZ/U7P6qC9wM/r5v0D3s/Jm', 'DOCTOR', TRUE),
('patient1', '$2a$10$r96F6wS7rL9L9D/7.K4s.Ou8eF9lV5iZ/U7P6qC9wM/r5v0D3s/Jm', 'PATIENT', TRUE)
ON DUPLICATE KEY UPDATE username=username;
