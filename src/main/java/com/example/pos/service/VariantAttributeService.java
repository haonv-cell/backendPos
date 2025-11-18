package com.example.pos.service;

import com.example.pos.dto.CreateVariantAttributeRequest;
import com.example.pos.dto.MessageResponse;
import com.example.pos.dto.UpdateVariantAttributeRequest;
import com.example.pos.dto.VariantAttributeDTO;
import com.example.pos.dto.VariantAttributeListResponse;
import com.example.pos.entity.VariantAttribute;
import com.example.pos.exception.ResourceNotFoundException;
import com.example.pos.repository.VariantAttributeRepository;
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
public class VariantAttributeService {

    private final VariantAttributeRepository variantAttributeRepository;

    @Transactional(readOnly = true)
    public VariantAttributeListResponse getVariantAttributes(int page, int size, String search, String status, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<VariantAttribute> variantAttributePage;

        // Clean status
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        // Security: Prevent searching for DELETED variant attributes
        if ("DELETED".equalsIgnoreCase(cleanStatus)) {
            variantAttributePage = Page.empty(pageable);
        }
        // Has search term
        else if (search != null && !search.trim().isEmpty()) {
            if (cleanStatus != null) {
                // Case 1: Search + Status filter
                variantAttributePage = variantAttributeRepository.searchVariantAttributesByStatus(cleanStatus, search.trim(), pageable);
            } else {
                // Case 2: Search only
                variantAttributePage = variantAttributeRepository.searchVariantAttributes(search.trim(), pageable);
            }
        }
        // No search term
        else {
            if (cleanStatus != null) {
                // Case 3: Status filter only
                variantAttributePage = variantAttributeRepository.findByStatus(cleanStatus, pageable);
            } else {
                // Case 4: No filter (exclude DELETED)
                variantAttributePage = variantAttributeRepository.findByStatusNot("DELETED", pageable);
            }
        }

        // Convert to DTOs
        List<VariantAttributeDTO> variantAttributes = variantAttributePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Build response
        return VariantAttributeListResponse.builder()
                .variantAttributes(variantAttributes)
                .currentPage(variantAttributePage.getNumber())
                .totalPages(variantAttributePage.getTotalPages())
                .totalItems(variantAttributePage.getTotalElements())
                .build();
    }

    @Transactional
    public VariantAttributeDTO createVariantAttribute(CreateVariantAttributeRequest request) {
        // Create variant attribute entity
        VariantAttribute variantAttribute = VariantAttribute.builder()
                .name(request.getName())
                .values(request.getValues())
                .imageUrl(request.getImageUrl())
                .status(request.getStatus() != null ? request.getStatus() : "active")
                .build();

        VariantAttribute savedVariantAttribute = variantAttributeRepository.save(variantAttribute);

        return convertToDTO(savedVariantAttribute);
    }

    @Transactional
    public VariantAttributeDTO updateVariantAttribute(Integer id, UpdateVariantAttributeRequest request) {
        // Find variant attribute
        VariantAttribute variantAttribute = variantAttributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VariantAttribute", "id", id));

        // Check if already deleted
        if ("DELETED".equalsIgnoreCase(variantAttribute.getStatus())) {
            throw new ResourceNotFoundException("VariantAttribute", "id", id);
        }

        // Update allowed fields only
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            variantAttribute.setName(request.getName());
        }
        if (request.getValues() != null) {
            variantAttribute.setValues(request.getValues());
        }
        if (request.getImageUrl() != null) {
            variantAttribute.setImageUrl(request.getImageUrl());
        }
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            variantAttribute.setStatus(request.getStatus());
        }

        VariantAttribute updatedVariantAttribute = variantAttributeRepository.save(variantAttribute);

        return convertToDTO(updatedVariantAttribute);
    }

    @Transactional
    public MessageResponse deleteVariantAttribute(Integer id) {
        // Find variant attribute
        VariantAttribute variantAttribute = variantAttributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VariantAttribute", "id", id));

        // Check if already deleted
        if ("DELETED".equalsIgnoreCase(variantAttribute.getStatus())) {
            throw new ResourceNotFoundException("VariantAttribute", "id", id);
        }

        // Soft delete
        variantAttribute.setStatus("DELETED");
        variantAttributeRepository.save(variantAttribute);

        return MessageResponse.of("Variant attribute deleted successfully");
    }

    private VariantAttributeDTO convertToDTO(VariantAttribute variantAttribute) {
        return VariantAttributeDTO.builder()
                .id(variantAttribute.getId())
                .name(variantAttribute.getName())
                .values(variantAttribute.getValues())
                .imageUrl(variantAttribute.getImageUrl())
                .createdAt(variantAttribute.getCreatedAt())
                .status(variantAttribute.getStatus())
                .build();
    }
}

