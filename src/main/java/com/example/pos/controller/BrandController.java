package com.example.pos.controller;

import com.example.pos.dto.BrandDTO;
import com.example.pos.dto.BrandListResponse;
import com.example.pos.dto.CreateBrandRequest;
import com.example.pos.dto.MessageResponse;
import com.example.pos.dto.UpdateBrandRequest;
import com.example.pos.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    /**
     * Get list of brands with pagination, search, and filtering
     * Only accessible by ADMIN role
     *
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param search Search term for brand name (optional)
     * @param status Filter by status (optional)
     * @param sortBy Sort field (default: createdAt)
     * @param sortDir Sort direction: asc or desc (default: desc)
     * @return BrandListResponse with pagination info
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandListResponse> getBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        BrandListResponse response = brandService.getBrands(page, size, search, status, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new brand
     * Only accessible by ADMIN role
     *
     * @param request CreateBrandRequest with name and imageUrl
     * @return BrandDTO of created brand with 201 Created status
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandDTO> createBrand(@Valid @RequestBody CreateBrandRequest request) {
        BrandDTO brand = brandService.createBrand(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(brand);
    }

    /**
     * Update an existing brand
     * Only accessible by ADMIN role
     *
     * @param id Brand ID
     * @param request UpdateBrandRequest with name, imageUrl, status
     * @return BrandDTO of updated brand
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandDTO> updateBrand(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateBrandRequest request) {
        BrandDTO brand = brandService.updateBrand(id, request);
        return ResponseEntity.ok(brand);
    }

    /**
     * Soft delete a brand
     * Only accessible by ADMIN role
     *
     * @param id Brand ID
     * @return MessageResponse with 200 OK
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteBrand(@PathVariable Integer id) {
        MessageResponse response = brandService.deleteBrand(id);
        return ResponseEntity.ok(response);
    }
}

