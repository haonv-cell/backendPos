package com.example.pos.service;

import com.example.pos.dto.UserDTO;
import com.example.pos.entity.User;
import com.example.pos.exception.ResourceNotFoundException;
import com.example.pos.repository.UserRepository;
import com.example.pos.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

