package com.example.pos.service;

import com.example.pos.dto.BillerDTO;
import com.example.pos.dto.BillerListResponse;
import com.example.pos.entity.Role;
import com.example.pos.entity.User;
import com.example.pos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillerService {

    private final UserRepository userRepository;

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


