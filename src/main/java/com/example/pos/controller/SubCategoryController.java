package com.example.pos.controller;

import com.example.pos.dto.subcategory.*;

import com.example.pos.dto.MessageResponse;
import com.example.pos.service.SubCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subcategories")
@RequiredArgsConstructor
public class SubCategoryController {

    private final SubCategoryService subCategoryService;

    /**
     * Get list of subcategories with pagination, search, and filtering
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubCategoryListResponse> getSubCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer categoryId, // Tham số mới
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        SubCategoryListResponse response = subCategoryService.getSubCategories(
                page, size, search, status, categoryId, sortBy, sortDir
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new subcategory
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubCategoryDTO> createSubCategory(
            @Valid @RequestBody CreateSubCategoryRequest request
    ) {
        SubCategoryDTO subCategory = subCategoryService.createSubCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(subCategory);
    }

    /**
     * Update an existing subcategory
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubCategoryDTO> updateSubCategory(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateSubCategoryRequest request
    ) {
        SubCategoryDTO subCategory = subCategoryService.updateSubCategory(id, request);
        return ResponseEntity.ok(subCategory);
    }

    /**
    * Soft delete a subcategory
    */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteSubCategory(@PathVariable Integer id) {
        MessageResponse response = subCategoryService.deleteSubCategory(id);
        return ResponseEntity.ok(response);
    }
}