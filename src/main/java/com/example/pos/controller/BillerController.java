package com.example.pos.controller;

import com.example.pos.dto.BillerDTO;
import com.example.pos.dto.BillerListResponse;
import com.example.pos.dto.CreateBillerRequest;
import com.example.pos.dto.MessageResponse;
import com.example.pos.dto.UpdateBillerRequest;
import com.example.pos.service.BillerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billers")
@RequiredArgsConstructor
public class BillerController {

    private final BillerService billerService;

    /**
     * Get list of billers with pagination, search, and filtering
     * Only accessible by ADMIN role
     *
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param search Search term (optional)
     * @param status Filter by status (optional)
     * @param sortBy Sort field (default: createdAt)
     * @param sortDir Sort direction: asc or desc (default: desc)
     * @return BillerListResponse with pagination info
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillerListResponse> getBillers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        BillerListResponse response = billerService.getBillers(page, size, search, status, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillerDTO> createBiller(@Valid @RequestBody CreateBillerRequest request) {
        BillerDTO billerDTO = billerService.createBiller(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(billerDTO);
    }

    /**
     * Update an existing biller
     * Only accessible by ADMIN role
     *
     * @param id Biller ID
     * @param request Update request with new data
     * @return Updated BillerDTO
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillerDTO> updateBiller(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateBillerRequest request
    ) {
        BillerDTO billerDTO = billerService.updateBiller(id, request);
        return ResponseEntity.ok(billerDTO);
    }

    /**
     * Delete a biller (soft delete)
     * Only accessible by ADMIN role
     *
     * @param id Biller ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteBiller(@PathVariable Integer id) {
        MessageResponse response = billerService.deleteBiller(id);
        return ResponseEntity.ok(response);
    }
}


