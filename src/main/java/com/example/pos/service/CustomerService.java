package com.example.pos.service;

import com.example.pos.dto.CreateCustomerRequest;
import com.example.pos.dto.CustomerDTO;
import com.example.pos.dto.CustomerListResponse;
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
public class CustomerService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public CustomerListResponse getCustomers(int page, int size, String search, String status, String sortBy,
            String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> customerPage;

        // Biến lưu trữ status sạch
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        // --- FIX LỖ HỔNG BẢO MẬT ---
        // Nếu ai đó cố tình tìm user đã bị xoá, ta trả về trang rỗng
        if ("DELETED".equalsIgnoreCase(cleanStatus)) {
            customerPage = Page.empty(pageable); // Trả về trang rỗng
        }
        // --- HẾT PHẦN FIX ---

        else if (search != null && !search.trim().isEmpty()) {
            // Có search
            if (cleanStatus != null) {
                // Case 1: Có search VÀ có status (status này đã được đảm bảo KHÁC "DELETED")
                // (Giả sử searchCustomersByStatus cũng đã được fix @Query trong Repo)
                customerPage = userRepository.searchCustomersByStatus(Role.CUSTOMER, cleanStatus, search.trim(),
                        pageable);
            } else {
                // Case 2: Có search, KHÔNG có status
                // (Phải đảm bảo searchCustomers trong Repo đã fix @Query)
                customerPage = userRepository.searchCustomers(Role.CUSTOMER, search.trim(), pageable);
            }
        } else {
            // Không search
            if (cleanStatus != null) {
                // Case 3: KHÔNG search, CÓ status (status này đã được đảm bảo KHÁC "DELETED")
                customerPage = userRepository.findByRoleAndStatus(Role.CUSTOMER, cleanStatus, pageable);
            } else {
                // Case 4: KHÔNG search, KHÔNG status (Đã fix chính xác)
                customerPage = userRepository.findByRoleAndStatusNot(Role.CUSTOMER, "DELETED", pageable);
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

    @Transactional
    public CustomerDTO createCustomer(CreateCustomerRequest request) {
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
                .passwordHash(passwordEncoder.encode(defaultPassword))
                .role(Role.CUSTOMER)
                .status(request.getStatus() != null ? request.getStatus() : "active")
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

    private CustomerDTO convertToDTO(User user) {
        return CustomerDTO.builder()
                .id(user.getId())
                .code(user.getCode())
                .name(user.getName())
                .companyName(user.getCompanyName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .country(user.getCountry())
                .status(user.getStatus())
                .imageUrl(user.getImageUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
