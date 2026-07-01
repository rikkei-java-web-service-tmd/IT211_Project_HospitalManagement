package com.re.hospital_management.controller;

import com.re.hospital_management.dto.ApiResponse;
import com.re.hospital_management.dto.PageResponseDTO;
import com.re.hospital_management.dto.PasswordChangeDTO;
import com.re.hospital_management.dto.UserRegisterDTO;
import com.re.hospital_management.dto.UserResponseDTO;
import com.re.hospital_management.dto.UserUpdateDTO;
import com.re.hospital_management.security.CustomUserDetails;
import com.re.hospital_management.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Public endpoint - anyone can register as patient
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDTO>> registerPatient(@Valid @RequestBody UserRegisterDTO registerDTO) {
        UserResponseDTO responseDTO = userService.registerPatient(registerDTO);
        ApiResponse<UserResponseDTO> response = ApiResponse.<UserResponseDTO>builder()
                .status(HttpStatus.CREATED.value())
                .message("Patient registered successfully")
                .data(responseDTO)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Only ADMIN can create users via this endpoint
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(@Valid @RequestBody UserRegisterDTO createDTO) {
        UserResponseDTO responseDTO = userService.createUser(createDTO);
        ApiResponse<UserResponseDTO> response = ApiResponse.<UserResponseDTO>builder()
                .status(HttpStatus.CREATED.value())
                .message("User created successfully")
                .data(responseDTO)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ADMIN and DOCTOR can see the full list; PATIENT cannot access this
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<PageResponseDTO<UserResponseDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponseDTO<UserResponseDTO> users = userService.getAllUsers(page, size);
        ApiResponse<PageResponseDTO<UserResponseDTO>> response = ApiResponse.<PageResponseDTO<UserResponseDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Users fetched successfully")
                .data(users)
                .build();
        return ResponseEntity.ok(response);
    }

    // ADMIN/DOCTOR can get any user; PATIENT can only get themselves
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // If PATIENT, enforce they can only view their own profile
        boolean isPatient = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));
        if (isPatient && !currentUser.getId().equals(id)) {
            throw new AccessDeniedException("Patients can only view their own profile");
        }

        UserResponseDTO user = userService.getUserById(id);
        ApiResponse<UserResponseDTO> response = ApiResponse.<UserResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("User fetched successfully")
                .data(user)
                .build();
        return ResponseEntity.ok(response);
    }

    // Only ADMIN can update user roles and active status
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateDTO updateDTO) {
        UserResponseDTO updatedUser = userService.updateUser(id, updateDTO);
        ApiResponse<UserResponseDTO> response = ApiResponse.<UserResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("User updated successfully")
                .data(updatedUser)
                .build();
        return ResponseEntity.ok(response);
    }

    // Only ADMIN can delete users
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.NO_CONTENT.value())
                .message("User deleted successfully")
                .build();
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    // User can change their own password; ADMIN can change anyone's
    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordChangeDTO changeDTO) {
        userService.changePassword(id, changeDTO);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Password changed successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
