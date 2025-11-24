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

    // Alias endpoint cho màn Manage Stock
    @GetMapping("/stocks")
    @PreAuthorize("hasAnyRole('ADMIN','BILLER','STORE_OWNER')")
    public ResponseEntity<ProductListResponse> getManageStock(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer storeId,
            @RequestParam(required = false) Integer warehouseId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) Integer unitId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        ProductListResponse response = productService.getProducts(
                page, size, search, null, categoryId, brandId, unitId, null,
                storeId, warehouseId, null, null, sortBy, sortDir
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id:\\d+}")
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

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Integer id,
                                                    @Valid @RequestBody UpdateProductRequest request) {
        ProductDTO updated = productService.updateProduct(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteProduct(@PathVariable Integer id) {
        productService.softDeleteProduct(id);
        return ResponseEntity.ok(MessageResponse.of("Product deleted"));
    }

    @PostMapping("/{id:\\d+}/duplicate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> duplicateProduct(@PathVariable Integer id) {
        ProductDTO copy = productService.duplicateProduct(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(copy);
    }

    @PutMapping("/{id:\\d+}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProductStatus(@PathVariable Integer id,
                                                          @RequestParam String status) {
        ProductDTO updated = productService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/low-stocks")
    @PreAuthorize("hasAnyRole('ADMIN','BILLER','STORE_OWNER')")
    public ResponseEntity<ProductListResponse> getLowStocks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer threshold,
            @RequestParam(defaultValue = "quantity") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        ProductListResponse response = productService.getLowStocks(page, size, threshold, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expired")
    @PreAuthorize("hasAnyRole('ADMIN','BILLER','STORE_OWNER')")
    public ResponseEntity<ProductListResponse> getExpiredProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "expiredDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        ProductListResponse response = productService.getExpiredProducts(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id:\\d+}/barcode-data")
    @PreAuthorize("hasAnyRole('ADMIN','BILLER','STORE_OWNER')")
    public ResponseEntity<com.example.pos.dto.BarcodeDataResponse> getBarcodeData(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getBarcodeData(id));
    }

    @PostMapping(value = "/import", consumes = "text/csv")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.example.pos.dto.ProductImportReport> importCsv(@RequestBody String csv) {
        return ResponseEntity.ok(productService.importProductsFromCsv(csv));
    }

    @PostMapping(value = "/import-file", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.example.pos.dto.ProductImportReport> importCsvFile(@RequestPart("file") org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        String csv = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok(productService.importProductsFromCsv(csv));
    }
}