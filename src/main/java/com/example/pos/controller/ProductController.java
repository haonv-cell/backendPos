package com.example.pos.controller;

import com.example.pos.dto.CreateProductRequest;
import com.example.pos.dto.ProductDTO;
import com.example.pos.dto.ProductListResponse;
import com.example.pos.dto.UpdateProductRequest;
import com.example.pos.dto.MessageResponse;
import com.example.pos.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','BILLER','STORE_OWNER')")
    public ResponseEntity<ProductListResponse> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) Integer unitId,
            @RequestParam(required = false) Integer subCategoryId,
            @RequestParam(required = false) Integer storeId,
            @RequestParam(required = false) Integer warehouseId,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) String sellingType,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        ProductListResponse response = productService.getProducts(
                page, size, search, status, categoryId, brandId, unitId, subCategoryId,
                storeId, warehouseId, productType, sellingType, sortBy, sortDir
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BILLER','STORE_OWNER')")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Integer id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductRequest request,
                                                    Authentication authentication) {
        ProductDTO product = productService.createProduct(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Integer id,
                                                    @Valid @RequestBody UpdateProductRequest request) {
        ProductDTO updated = productService.updateProduct(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteProduct(@PathVariable Integer id) {
        productService.softDeleteProduct(id);
        return ResponseEntity.ok(MessageResponse.of("Product deleted"));
    }
}