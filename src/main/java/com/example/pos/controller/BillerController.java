package com.example.pos.controller;

import com.example.pos.dto.BillerListResponse;
import com.example.pos.service.BillerService;
import lombok.RequiredArgsConstructor;
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
}


