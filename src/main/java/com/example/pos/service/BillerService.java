package com.example.pos.service;

import com.example.pos.dto.BillerDTO;
import com.example.pos.dto.BillerListResponse;
import com.example.pos.dto.CreateBillerRequest;
import com.example.pos.entity.AuthProvider;
import com.example.pos.entity.Role;
import com.example.pos.entity.User;
import com.example.pos.exception.BadRequestException;
import com.example.pos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillerService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public BillerListResponse getBillers(int page, int size, String search, String status, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> billerPage;

        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        if ("DELETED".equalsIgnoreCase(cleanStatus)) {
            billerPage = Page.empty(pageable);
        } else if (search != null && !search.trim().isEmpty()) {
            if (cleanStatus != null) {
                billerPage = userRepository.searchBillersByStatus(Role.BILLER, cleanStatus, search.trim(), pageable);
            } else {
                billerPage = userRepository.searchBillers(Role.BILLER, search.trim(), pageable);
            }
        } else {
            if (cleanStatus != null) {
                billerPage = userRepository.findByRoleAndStatus(Role.BILLER, cleanStatus, pageable);
            } else {
                billerPage = userRepository.findByRoleAndStatusNot(Role.BILLER, "DELETED", pageable);
            }
        }

        List<BillerDTO> billers = billerPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return BillerListResponse.builder()
                .billers(billers)
                .totalElements(billerPage.getTotalElements())
                .totalPages(billerPage.getTotalPages())
                .currentPage(billerPage.getNumber())
                .pageSize(billerPage.getSize())
                .build();
    }

    @Transactional
    public BillerDTO createBiller(CreateBillerRequest request) {
        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email address already in use.");
        }

        // Generate unique code
        String code = generateUniqueCode();

        // Generate default password (can be changed later)
        String defaultPassword = generateDefaultPassword();
        
        User user = User.builder()
                .code(code)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .country(request.getCountry())
                .companyName(request.getCompanyName())
                .passwordHash(passwordEncoder.encode(defaultPassword))
                .role(Role.BILLER)
                .status("active")
                .provider(AuthProvider.LOCAL)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = "USR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (userRepository.existsByCode(code));
        return code;
    }

    private String generateDefaultPassword() {
        // Generate a random default password
        return UUID.randomUUID().toString().substring(0, 12);
    }

    private BillerDTO convertToDTO(User user) {
        return BillerDTO.builder()
                .id(user.getId())
                .code(user.getCode())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .country(user.getCountry())
                .status(user.getStatus())
                .imageUrl(user.getImageUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}


