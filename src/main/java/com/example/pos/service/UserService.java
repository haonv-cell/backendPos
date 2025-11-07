package com.example.pos.service;

import com.example.pos.dto.UpdateUserRequest;
import com.example.pos.dto.UserDTO;
import com.example.pos.entity.AuthProvider;
import com.example.pos.entity.User;
import com.example.pos.exception.BadRequestException;
import com.example.pos.exception.ResourceNotFoundException;
import com.example.pos.repository.UserRepository;
import com.example.pos.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        return convertToDTO(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Integer id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        return convertToDTO(user);
    }

    @Transactional
    public UserDTO updateUser(Integer id, UpdateUserRequest request) {
        // Find existing user
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Validate data integrity before update
        validateUserDataIntegrity(user);

        // Update only allowed fields
        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getCountry() != null) {
            user.setCountry(request.getCountry());
        }

        if (request.getCompanyName() != null) {
            user.setCompanyName(request.getCompanyName());
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (StringUtils.hasText(request.getStatus())) {
            user.setStatus(request.getStatus());
        }

        if (request.getImageUrl() != null) {
            user.setImageUrl(request.getImageUrl());
        }

        // Save and return
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    /**
     * Validate user data integrity to prevent invalid states
     */
    private void validateUserDataIntegrity(User user) {
        // Check if OAuth account has provider_id
        if (user.getProvider() != AuthProvider.LOCAL && user.getProviderId() == null) {
            throw new BadRequestException(
                "Invalid account state: OAuth account (provider=" + user.getProvider() +
                ") is missing provider_id. Please contact support."
            );
        }

        // Check if LOCAL account has password
        if (user.getProvider() == AuthProvider.LOCAL && user.getPasswordHash() == null) {
            throw new BadRequestException(
                "Invalid account state: Local account is missing password. " +
                "Please contact support or reset your password."
            );
        }
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
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
            .build();
    }
}

