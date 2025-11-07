package com.example.pos.service;

import com.example.pos.dto.AuthResponse;
import com.example.pos.dto.LoginRequest;
import com.example.pos.dto.RegisterRequest;
import com.example.pos.dto.UserDTO;
import com.example.pos.entity.AuthProvider;
import com.example.pos.entity.Role;
import com.example.pos.entity.User;
import com.example.pos.exception.BadRequestException;
import com.example.pos.repository.UserRepository;
import com.example.pos.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        // Find user first to validate provider
        User user = userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        // Validate provider: Only LOCAL users can login with email/password
        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new BadRequestException(
                "This account is registered with " + user.getProvider() +
                ". Please use '" + user.getProvider() + "' to login."
            );
        }

        // Validate data integrity: LOCAL account must have password
        if (user.getPasswordHash() == null) {
            throw new BadRequestException(
                "Invalid account state: Local account is missing password. " +
                "Please contact support or reset your password."
            );
        }

        // Authenticate
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
            .accessToken(token)
            .user(convertToDTO(user))
            .build();
    }
    
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email address already in use.");
        }
        
        // Generate unique code
        String code = generateUniqueCode();
        
        User user = User.builder()
            .code(code)
            .name(registerRequest.getName())
            .email(registerRequest.getEmail())
            .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
            .phone(registerRequest.getPhone())
            .country(registerRequest.getCountry())
            .companyName(registerRequest.getCompanyName())
            .role(registerRequest.getRole() != null ? registerRequest.getRole() : Role.CUSTOMER)
            .status("active")
            .provider(AuthProvider.LOCAL)
            .emailVerified(false)
            .build();
        
        User savedUser = userRepository.save(user);
        
        // Auto login after registration
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                registerRequest.getEmail(),
                registerRequest.getPassword()
            )
        );
        
        String token = tokenProvider.generateToken(authentication);
        
        return AuthResponse.builder()
            .accessToken(token)
            .user(convertToDTO(savedUser))
            .build();
    }
    
    private String generateUniqueCode() {
        String code;
        do {
            code = "USR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (userRepository.existsByCode(code));
        return code;
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

