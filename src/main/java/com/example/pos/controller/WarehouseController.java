package com.example.pos.controller;

import com.example.pos.dto.CreateWarehouseRequest;
import com.example.pos.dto.MessageResponse;
import com.example.pos.dto.UpdateWarehouseRequest;
import com.example.pos.dto.WarehouseDTO;
import com.example.pos.dto.WarehouseListResponse;
import com.example.pos.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    /**
     * Get list of warehouses with pagination, search, and filtering
     * Only accessible by ADMIN role
     *
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param search Search term for name, contact person, or phone (optional)
     * @param status Filter by status (optional)
     * @param sortBy Sort field (default: createdOn)
     * @param sortDir Sort direction: asc or desc (default: desc)
     * @return WarehouseListResponse with pagination info
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarehouseListResponse> getWarehouses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdOn") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        WarehouseListResponse response = warehouseService.getWarehouses(page, size, search, status, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new warehouse
     * Only accessible by ADMIN role
     *
     * @param request CreateWarehouseRequest with name, contactPerson, phone, userId
     * @return WarehouseDTO of created warehouse with 201 Created status
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarehouseDTO> createWarehouse(@Valid @RequestBody CreateWarehouseRequest request) {
        WarehouseDTO warehouse = warehouseService.createWarehouse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouse);
    }

    /**
     * Update an existing warehouse
     * Only accessible by ADMIN role
     *
     * @param id Warehouse ID
     * @param request UpdateWarehouseRequest with name, contactPerson, phone, userId
     * @return WarehouseDTO of updated warehouse
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarehouseDTO> updateWarehouse(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateWarehouseRequest request) {
        WarehouseDTO warehouse = warehouseService.updateWarehouse(id, request);
        return ResponseEntity.ok(warehouse);
    }

    /**
     * Soft delete a warehouse
     * Only accessible by ADMIN role
     *
     * @param id Warehouse ID
     * @return MessageResponse with 200 OK or 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteWarehouse(@PathVariable Integer id) {
        MessageResponse response = warehouseService.deleteWarehouse(id);
        return ResponseEntity.ok(response);
    }
}

