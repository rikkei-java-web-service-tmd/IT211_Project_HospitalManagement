package com.re.hospital_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HospitalManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitalManagementApplication.class, args);
    }
    @org.springframework.context.annotation.Bean
    public org.springframework.boot.CommandLineRunner initData(com.re.hospital_management.repository.UserRepository userRepository, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        return args -> {
            String fixedHash = passwordEncoder.encode("password123");
            java.util.List.of("admin1", "doctor1", "patient1").forEach(username -> {
                userRepository.findByUsername(username).ifPresent(user -> {
                    user.setPasswordHash(fixedHash);
                    userRepository.save(user);
                });
            });
        };
    }

}
