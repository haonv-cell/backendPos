package com.example.pos.controller;

import com.example.pos.dto.CreateVariantAttributeRequest;
import com.example.pos.dto.MessageResponse;
import com.example.pos.dto.UpdateVariantAttributeRequest;
import com.example.pos.dto.VariantAttributeDTO;
import com.example.pos.dto.VariantAttributeListResponse;
import com.example.pos.service.VariantAttributeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/variant-attributes")
@RequiredArgsConstructor
public class VariantAttributeController {

    private final VariantAttributeService variantAttributeService;

    /**
     * Get list of variant attributes with pagination, search, and filtering
     * Only accessible by ADMIN role
     *
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param search Search term for variant attribute name (optional)
     * @param status Filter by status (optional)
     * @param sortBy Sort field (default: createdAt)
     * @param sortDir Sort direction: asc or desc (default: desc)
     * @return VariantAttributeListResponse with pagination info
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VariantAttributeListResponse> getVariantAttributes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        VariantAttributeListResponse response = variantAttributeService.getVariantAttributes(page, size, search, status, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new variant attribute
     * Only accessible by ADMIN role
     *
     * @param request CreateVariantAttributeRequest with name, values, and imageUrl
     * @return VariantAttributeDTO of created variant attribute with 201 Created status
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VariantAttributeDTO> createVariantAttribute(@Valid @RequestBody CreateVariantAttributeRequest request) {
        VariantAttributeDTO variantAttribute = variantAttributeService.createVariantAttribute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(variantAttribute);
    }

    /**
     * Update an existing variant attribute
     * Only accessible by ADMIN role
     *
     * @param id Variant attribute ID
     * @param request UpdateVariantAttributeRequest with name, values, imageUrl, status
     * @return VariantAttributeDTO of updated variant attribute
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VariantAttributeDTO> updateVariantAttribute(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateVariantAttributeRequest request) {
        VariantAttributeDTO variantAttribute = variantAttributeService.updateVariantAttribute(id, request);
        return ResponseEntity.ok(variantAttribute);
    }

    /**
     * Soft delete a variant attribute
     * Only accessible by ADMIN role
     *
     * @param id Variant attribute ID
     * @return MessageResponse with 200 OK
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteVariantAttribute(@PathVariable Integer id) {
        MessageResponse response = variantAttributeService.deleteVariantAttribute(id);
        return ResponseEntity.ok(response);
    }
}

