package com.example.pos.controller;

import com.example.pos.dto.CreateWarrantyRequest;
import com.example.pos.dto.UpdateWarrantyRequest;
import com.example.pos.dto.WarrantyDTO;
import com.example.pos.dto.WarrantyListResponse;
import com.example.pos.dto.MessageResponse;
import com.example.pos.service.WarrantyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warranties")
@RequiredArgsConstructor
public class WarrantyController {

    private final WarrantyService warrantyService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','BILLER','STORE_OWNER')")
    public ResponseEntity<WarrantyListResponse> getWarranties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        WarrantyListResponse response = warrantyService.getWarranties(page, size, search, status, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarrantyDTO> createWarranty(@Valid @RequestBody CreateWarrantyRequest request) {
        WarrantyDTO warranty = warrantyService.createWarranty(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(warranty);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarrantyDTO> updateWarranty(@PathVariable Integer id,
                                                      @Valid @RequestBody UpdateWarrantyRequest request) {
        WarrantyDTO warranty = warrantyService.updateWarranty(id, request);
        return ResponseEntity.ok(warranty);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteWarranty(@PathVariable Integer id) {
        warrantyService.deleteWarranty(id);
        return ResponseEntity.ok(MessageResponse.of("Warranty deleted"));
    }
}