package com.example.pos.controller;

import com.example.pos.dto.MessageResponse;
import com.example.pos.dto.UpdateUserRequest;
import com.example.pos.dto.UserDTO;
import com.example.pos.entity.User;
import com.example.pos.repository.UserRepository;
import com.example.pos.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        UserDTO user = userService.getCurrentUser(authentication);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDTO updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Integer id) {

        // 1. Gọi service, giờ đây nó sẽ trả về MessageResponse
        MessageResponse response = userService.deleteUser(id);

        // 2. Trả về HTTP 200 OK cùng với message body
        return ResponseEntity.ok(response);
    }
    // Endpoint để kiểm tra tất cả users trong database (public - để debug)
    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOs = users.stream()
                .map(user -> UserDTO.builder()
                        .id(user.getId())
                        .code(user.getCode())
                        .name(user.getName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .country(user.getCountry())
                        .companyName(user.getCompanyName())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .provider(user.getProvider())
                        .imageUrl(user.getImageUrl())
                        .emailVerified(user.getEmailVerified())
                        .createdAt(user.getCreatedAt())
                        .updatedAt(user.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    // Endpoint để đếm số lượng users (public - để debug)
    @GetMapping("/count")
    public ResponseEntity<Long> countUsers() {
        long count = userRepository.count();
        return ResponseEntity.ok(count);
    }
    
}

