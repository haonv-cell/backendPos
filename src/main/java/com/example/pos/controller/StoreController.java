package com.example.pos.controller;

import com.example.pos.dto.CreateStoreRequest;
import com.example.pos.dto.MessageResponse;
import com.example.pos.dto.StoreDTO;
import com.example.pos.dto.StoreListResponse;
import com.example.pos.dto.UpdateStoreRequest;
import com.example.pos.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    /**
     * Get list of stores with pagination, search, and filtering
     * Only accessible by ADMIN role
     *
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param search Search term for name, email, phone, or user name (optional)
     * @param status Filter by status (optional)
     * @param sortBy Sort field (default: createdAt)
     * @param sortDir Sort direction: asc or desc (default: desc)
     * @return StoreListResponse with pagination info
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoreListResponse> getStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        StoreListResponse response = storeService.getStores(page, size, search, status, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new store
     * Only accessible by ADMIN role
     *
     * @param request CreateStoreRequest with name, email, phone, address, warehouseId, userId
     * @return StoreDTO of created store with 201 Created status
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoreDTO> createStore(@Valid @RequestBody CreateStoreRequest request) {
        StoreDTO store = storeService.createStore(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(store);
    }

    /**
     * Update an existing store
     * Only accessible by ADMIN role
     *
     * @param id Store ID
     * @param request UpdateStoreRequest with name, email, phone, address, warehouseId, userId
     * @return StoreDTO of updated store
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoreDTO> updateStore(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateStoreRequest request) {
        StoreDTO store = storeService.updateStore(id, request);
        return ResponseEntity.ok(store);
    }

    /**
     * Soft delete a store
     * Only accessible by ADMIN role
     *
     * @param id Store ID
     * @return MessageResponse with 200 OK or 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteStore(@PathVariable Integer id) {
        MessageResponse response = storeService.deleteStore(id);
        return ResponseEntity.ok(response);
    }
}

