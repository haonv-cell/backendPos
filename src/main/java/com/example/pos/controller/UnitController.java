package com.example.pos.controller;

import com.example.pos.dto.CreateUnitRequest;
import com.example.pos.dto.MessageResponse;
import com.example.pos.dto.UpdateUnitRequest;
import com.example.pos.dto.UnitDTO;
import com.example.pos.dto.UnitListResponse;
import com.example.pos.service.UnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    /**
     * Get list of units with pagination, search, and filtering
     * Only accessible by ADMIN role
     *
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param search Search term for name or short name (optional)
     * @param status Filter by status (optional)
     * @param sortBy Sort field (default: createdAt)
     * @param sortDir Sort direction: asc or desc (default: desc)
     * @return UnitListResponse with pagination info
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UnitListResponse> getUnits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        UnitListResponse response = unitService.getUnits(page, size, search, status, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new unit
     * Only accessible by ADMIN role
     *
     * @param request CreateUnitRequest with name and shortName
     * @return UnitDTO of created unit with 201 Created status
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UnitDTO> createUnit(@Valid @RequestBody CreateUnitRequest request) {
        UnitDTO unit = unitService.createUnit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(unit);
    }

    /**
     * Update an existing unit
     * Only accessible by ADMIN role
     *
     * @param id Unit ID
     * @param request UpdateUnitRequest with name, shortName, status
     * @return UnitDTO of updated unit
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UnitDTO> updateUnit(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUnitRequest request) {
        UnitDTO unit = unitService.updateUnit(id, request);
        return ResponseEntity.ok(unit);
    }

    /**
     * Soft delete a unit
     * Only accessible by ADMIN role
     *
     * @param id Unit ID
     * @return MessageResponse with 200 OK
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteUnit(@PathVariable Integer id) {
        MessageResponse response = unitService.deleteUnit(id);
        return ResponseEntity.ok(response);
    }
}

