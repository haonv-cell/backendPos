package com.example.pos.controller;

import com.example.pos.dto.CreateCustomerRequest;
import com.example.pos.dto.CustomerDTO;
import com.example.pos.dto.CustomerListResponse;
import com.example.pos.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    
    private final CustomerService customerService;
    
    /**
     * Get list of customers with pagination, search, and filtering
     * Only accessible by ADMIN role
     * 
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param search Search term for name, email, code, phone, or country (optional)
     * @param status Filter by status (optional)
     * @param sortBy Sort field (default: createdAt)
     * @param sortDir Sort direction: asc or desc (default: desc)
     * @return CustomerListResponse with pagination info
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerListResponse> getCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        CustomerListResponse response = customerService.getCustomers(page, size, search, status, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerDTO customerDTO = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(customerDTO);
    }
}

