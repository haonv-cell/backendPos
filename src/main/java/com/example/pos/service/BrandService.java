package com.example.pos.service;

import com.example.pos.dto.BrandDTO;
import com.example.pos.dto.BrandListResponse;
import com.example.pos.dto.CreateBrandRequest;
import com.example.pos.dto.MessageResponse;
import com.example.pos.dto.UpdateBrandRequest;
import com.example.pos.entity.Brand;
import com.example.pos.exception.ResourceNotFoundException;
import com.example.pos.repository.BrandRepository;
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
public class BrandService {

    private final BrandRepository brandRepository;

    @Transactional(readOnly = true)
    public BrandListResponse getBrands(int page, int size, String search, String status, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Brand> brandPage;

        // Clean status
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        // Security: Prevent searching for DELETED brands
        if ("DELETED".equalsIgnoreCase(cleanStatus)) {
            brandPage = Page.empty(pageable);
        }
        // Has search term
        else if (search != null && !search.trim().isEmpty()) {
            if (cleanStatus != null) {
                // Case 1: Search + Status filter
                brandPage = brandRepository.searchBrandsByStatus(cleanStatus, search.trim(), pageable);
            } else {
                // Case 2: Search only
                brandPage = brandRepository.searchBrands(search.trim(), pageable);
            }
        }
        // No search term
        else {
            if (cleanStatus != null) {
                // Case 3: Status filter only
                brandPage = brandRepository.findByStatus(cleanStatus, pageable);
            } else {
                // Case 4: No filter (exclude DELETED)
                brandPage = brandRepository.findByStatusNot("DELETED", pageable);
            }
        }

        // Convert to DTOs
        List<BrandDTO> brands = brandPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Build response
        return BrandListResponse.builder()
                .brands(brands)
                .currentPage(brandPage.getNumber())
                .totalPages(brandPage.getTotalPages())
                .totalItems(brandPage.getTotalElements())
                .build();
    }

    @Transactional
    public BrandDTO createBrand(CreateBrandRequest request) {
        // Create brand entity
        Brand brand = Brand.builder()
                .name(request.getName())
                .imageUrl(request.getImageUrl())
                .status("active")
                .build();

        Brand savedBrand = brandRepository.save(brand);

        return convertToDTO(savedBrand);
    }

    @Transactional
    public BrandDTO updateBrand(Integer id, UpdateBrandRequest request) {
        // Find brand
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));

        // Check if already deleted
        if ("DELETED".equalsIgnoreCase(brand.getStatus())) {
            throw new ResourceNotFoundException("Brand", "id", id);
        }

        // Update allowed fields only
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            brand.setName(request.getName());
        }
        if (request.getImageUrl() != null) {
            brand.setImageUrl(request.getImageUrl());
        }
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            brand.setStatus(request.getStatus());
        }

        Brand updatedBrand = brandRepository.save(brand);

        return convertToDTO(updatedBrand);
    }

    @Transactional
    public MessageResponse deleteBrand(Integer id) {
        // Find brand
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));

        // Check if already deleted
        if ("DELETED".equalsIgnoreCase(brand.getStatus())) {
            throw new ResourceNotFoundException("Brand", "id", id);
        }

        // Soft delete
        brand.setStatus("DELETED");
        brandRepository.save(brand);

        return MessageResponse.of("Brand deleted successfully");
    }

    private BrandDTO convertToDTO(Brand brand) {
        return BrandDTO.builder()
                .id(brand.getId())
                .name(brand.getName())
                .imageUrl(brand.getImageUrl())
                .createdAt(brand.getCreatedAt())
                .status(brand.getStatus())
                .build();
    }
}

