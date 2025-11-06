package com.example.pos.service;

import com.example.pos.dto.CustomerDTO;
import com.example.pos.dto.CustomerListResponse;
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
public class CustomerService {
    
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public CustomerListResponse getCustomers(int page, int size, String search, String status, String sortBy, String sortDir) {
        // Convert camelCase to snake_case for database column names
        String dbSortBy = convertToSnakeCase(sortBy);

        // Create sort object
        Sort sort = sortDir.equalsIgnoreCase("desc")
            ? Sort.by(dbSortBy).descending()
            : Sort.by(dbSortBy).ascending();

        // Create pageable object
        Pageable pageable = PageRequest.of(page, size, sort);

        // Fetch customers based on filters
        Page<User> customerPage;

        if (search != null && !search.trim().isEmpty()) {
            // Search with or without status filter
            if (status != null && !status.trim().isEmpty()) {
                customerPage = userRepository.searchCustomersByStatus("customer", status, search.trim(), pageable);
            } else {
                customerPage = userRepository.searchCustomers("customer", search.trim(), pageable);
            }
        } else {
            // No search, just filter by status or get all
            if (status != null && !status.trim().isEmpty()) {
                customerPage = userRepository.findByRoleAndStatus(Role.CUSTOMER, status, pageable);
            } else {
                customerPage = userRepository.findByRole(Role.CUSTOMER, pageable);
            }
        }
        
        // Convert to DTOs
        List<CustomerDTO> customers = customerPage.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        // Build response
        return CustomerListResponse.builder()
            .customers(customers)
            .totalElements(customerPage.getTotalElements())
            .totalPages(customerPage.getTotalPages())
            .currentPage(customerPage.getNumber())
            .pageSize(customerPage.getSize())
            .build();
    }
    
    private CustomerDTO convertToDTO(User user) {
        return CustomerDTO.builder()
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

    /**
     * Convert camelCase to snake_case for database column names
     */
    private String convertToSnakeCase(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return "created_at"; // default
        }

        // Map common field names
        return switch (camelCase) {
            case "createdAt" -> "created_at";
            case "updatedAt" -> "updated_at";
            case "imageUrl" -> "image_url";
            case "companyName" -> "company_name";
            case "emailVerified" -> "email_verified";
            default -> camelCase; // name, email, phone, country, status, code, id
        };
    }
}

