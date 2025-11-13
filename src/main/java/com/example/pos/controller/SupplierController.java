package com.example.pos.controller;

import com.example.pos.dto.CreateSupplierRequest;
import com.example.pos.dto.MessageResponse;
import com.example.pos.dto.SupplierDTO;
import com.example.pos.dto.SupplierListResponse;
import com.example.pos.dto.UpdateSupplierRequest;
import com.example.pos.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {
    
    private final SupplierService supplierService;
    
    /**
     * Get list of suppliers with pagination, search, and filtering
     * Only accessible by ADMIN role
     * 
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param search Search term (optional)
     * @param status Filter by status (optional)
     * @param sortBy Sort field (default: createdAt)
     * @param sortDir Sort direction: asc or desc (default: desc)
     * @return SupplierListResponse with pagination info
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplierListResponse> getSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        SupplierListResponse response = supplierService.getSuppliers(page, size, search, status, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplierDTO> createSupplier(@Valid @RequestBody CreateSupplierRequest request) {
        SupplierDTO supplierDTO = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierDTO);
    }

    /**
     * Update an existing supplier
     * Only accessible by ADMIN role
     *
     * @param id Supplier ID
     * @param request Update request with new data
     * @return Updated SupplierDTO
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplierDTO> updateSupplier(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateSupplierRequest request
    ) {
        SupplierDTO supplierDTO = supplierService.updateSupplier(id, request);
        return ResponseEntity.ok(supplierDTO);
    }

    /**
     * Delete a supplier (soft delete)
     * Only accessible by ADMIN role
     *
     * @param id Supplier ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteSupplier(@PathVariable Integer id) {
        MessageResponse response = supplierService.deleteSupplier(id);
        return ResponseEntity.ok(response);
    }
}
