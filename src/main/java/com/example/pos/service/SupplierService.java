package com.example.pos.service;

import com.example.pos.dto.CreateSupplierRequest;
import com.example.pos.dto.MessageResponse;
import com.example.pos.dto.SupplierDTO;
import com.example.pos.dto.SupplierListResponse;
import com.example.pos.dto.UpdateSupplierRequest;
import com.example.pos.entity.Supplier;
import com.example.pos.exception.ResourceNotFoundException;
import com.example.pos.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Transactional(readOnly = true)
    public SupplierListResponse getSuppliers(int page, int size, String search, String status, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Supplier> supplierPage;

        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        if ("DELETED".equalsIgnoreCase(cleanStatus)) {
            supplierPage = Page.empty(pageable);
        } else if (search != null && !search.trim().isEmpty()) {
            if (cleanStatus != null) {
                supplierPage = supplierRepository.searchSuppliersByStatus(cleanStatus, search.trim(), pageable);
            } else {
                supplierPage = supplierRepository.searchSuppliers(search.trim(), pageable);
            }
        } else {
            if (cleanStatus != null) {
                supplierPage = supplierRepository.findByStatus(cleanStatus, pageable);
            } else {
                supplierPage = supplierRepository.findByStatusNot("DELETED", pageable);
            }
        }

        List<SupplierDTO> suppliers = supplierPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return SupplierListResponse.builder()
                .suppliers(suppliers)
                .totalElements(supplierPage.getTotalElements())
                .totalPages(supplierPage.getTotalPages())
                .currentPage(supplierPage.getNumber())
                .pageSize(supplierPage.getSize())
                .build();
    }

    @Transactional
    public SupplierDTO createSupplier(CreateSupplierRequest request) {
        Supplier supplier = Supplier.builder()
                .name(request.getName())
                .contactEmail(request.getEmail())
                .contactPhone(request.getPhone())
                .address(request.getCountry() != null ? request.getCountry() : null)
                .status("active")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Supplier savedSupplier = supplierRepository.save(supplier);
        return convertToDTO(savedSupplier);
    }

    @Transactional
    public SupplierDTO updateSupplier(Integer id, UpdateSupplierRequest request) {
        // Find supplier
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));

        // Check if deleted
        if ("DELETED".equalsIgnoreCase(supplier.getStatus())) {
            throw new ResourceNotFoundException("Supplier", "id", id);
        }

        // Validate email uniqueness (if changed)
        if (request.getEmail() != null && !request.getEmail().equals(supplier.getContactEmail())) {
            Optional<Supplier> existingEmail = supplierRepository.findByContactEmail(request.getEmail());
            if (existingEmail.isPresent() && !existingEmail.get().getId().equals(id)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Email already exists"
                );
            }
            supplier.setContactEmail(request.getEmail());
        }

        // Update fields if provided
        if (request.getName() != null) {
            supplier.setName(request.getName());
        }
        if (request.getPhone() != null) {
            supplier.setContactPhone(request.getPhone());
        }
        if (request.getCountry() != null) {
            supplier.setAddress(request.getCountry());
        }
        if (request.getStatus() != null) {
            supplier.setStatus(request.getStatus());
        }

        supplier.setUpdatedAt(LocalDateTime.now());
        Supplier updatedSupplier = supplierRepository.save(supplier);
        return convertToDTO(updatedSupplier);
    }

    @Transactional
    public MessageResponse deleteSupplier(Integer id) {
        // Find supplier
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));

        // Check if already deleted
        if ("DELETED".equalsIgnoreCase(supplier.getStatus())) {
            throw new ResourceNotFoundException("Supplier", "id", id);
        }

        // Soft delete
        supplier.setStatus("DELETED");
        supplier.setUpdatedAt(LocalDateTime.now());
        supplierRepository.save(supplier);

        return MessageResponse.of("Supplier deleted successfully");
    }

    private SupplierDTO convertToDTO(Supplier supplier) {
        return SupplierDTO.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .contactName(supplier.getContactName())
                .email(supplier.getContactEmail())
                .phone(supplier.getContactPhone())
                .country(supplier.getAddress())
                .status(supplier.getStatus())
                .createdAt(supplier.getCreatedAt())
                .build();
    }
}
