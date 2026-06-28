package com.re.hospital_management.service;

import com.re.hospital_management.dto.PageResponseDTO;
import com.re.hospital_management.dto.UserRegisterDTO;
import com.re.hospital_management.dto.UserResponseDTO;
import com.re.hospital_management.dto.UserUpdateDTO;

public interface UserService {
    UserResponseDTO registerPatient(UserRegisterDTO registerDTO);
    UserResponseDTO createUser(UserRegisterDTO createDTO);
    UserResponseDTO getUserById(Long id);
    PageResponseDTO<UserResponseDTO> getAllUsers(int page, int size);
    UserResponseDTO updateUser(Long id, UserUpdateDTO updateDTO);
    void deleteUser(Long id);
    void changePassword(Long userId, com.re.hospital_management.dto.PasswordChangeDTO changeDTO);
}
