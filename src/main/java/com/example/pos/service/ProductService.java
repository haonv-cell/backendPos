package com.example.pos.service;

import com.example.pos.dto.CreateProductRequest;
import com.example.pos.dto.ProductDTO;
import com.example.pos.dto.ProductListResponse;
import com.example.pos.dto.UpdateProductRequest;
import com.example.pos.entity.Product;
import com.example.pos.entity.User;
import com.example.pos.exception.BadRequestException;
import com.example.pos.exception.ResourceNotFoundException;
import com.example.pos.repository.*;
import com.example.pos.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UnitRepository unitRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final StoreRepository storeRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ProductListResponse getProducts(
            int page,
            int size,
            String search,
            String status,
            Integer categoryId,
            Integer brandId,
            Integer unitId,
            Integer subCategoryId,
            Integer storeId,
            Integer warehouseId,
            String productType,
            String sellingType,
            String sortBy,
            String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        String searchLower = (search != null && !search.trim().isEmpty()) ? search.trim().toLowerCase() : null;
        String productTypeLower = (productType != null && !productType.trim().isEmpty()) ? productType.trim().toLowerCase() : null;
        String sellingTypeLower = (sellingType != null && !sellingType.trim().isEmpty()) ? sellingType.trim().toLowerCase() : null;

        Page<Product> productPage = productRepository.searchProducts(
                status,
                searchLower,
                categoryId,
                brandId,
                unitId,
                subCategoryId,
                storeId,
                warehouseId,
                productTypeLower,
                sellingTypeLower,
                pageable
        );

        List<ProductDTO> products = productPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ProductListResponse.builder()
                .products(products)
                .currentPage(productPage.getNumber())
                .totalPages(productPage.getTotalPages())
                .totalItems(productPage.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public ProductListResponse getLowStocks(int page, int size, Integer threshold, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findLowStocks(threshold, pageable);
        List<ProductDTO> products = productPage.getContent().stream().map(this::toDTO).collect(Collectors.toList());
        return ProductListResponse.builder()
                .products(products)
                .currentPage(productPage.getNumber())
                .totalPages(productPage.getTotalPages())
                .totalItems(productPage.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public ProductListResponse getExpiredProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findExpiredProducts(java.time.LocalDate.now(), pageable);
        List<ProductDTO> products = productPage.getContent().stream().map(this::toDTO).collect(Collectors.toList());
        return ProductListResponse.builder()
                .products(products)
                .currentPage(productPage.getNumber())
                .totalPages(productPage.getTotalPages())
                .totalItems(productPage.getTotalElements())
                .build();
    }

    @Transactional
    public ProductDTO duplicateProduct(Integer id) {
        Product original = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        if ("DELETED".equalsIgnoreCase(original.getStatus())) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        Product copy = Product.builder()
                .name(original.getName())
                .sku(generateUniqueSku(original.getSku()))
                .slug(generateUniqueSlug(original.getSlug()))
                .price(original.getPrice())
                .quantity(original.getQuantity())
                .qtyAlert(original.getQtyAlert())
                .status(original.getStatus())
                .unitId(original.getUnitId())
                .subCategoryId(original.getSubCategoryId())
                .categoryId(original.getCategoryId())
                .brandId(original.getBrandId())
                .storeId(original.getStoreId())
                .warehouseId(original.getWarehouseId())
                .itemCode(original.getItemCode() != null ? generateUniqueItemCode(original.getItemCode()) : null)
                .barcodeSymbology(original.getBarcodeSymbology())
                .barcodeValue(original.getBarcodeValue())
                .sellingType(original.getSellingType())
                .productType(original.getProductType())
                .taxType(original.getTaxType())
                .discountType(original.getDiscountType())
                .discountValue(original.getDiscountValue())
                .description(original.getDescription())
                .manufacturer(original.getManufacturer())
                .imageUrl(original.getImageUrl())
                .manufacturedDate(original.getManufacturedDate())
                .expiredDate(original.getExpiredDate())
                .createdBy(original.getCreatedBy())
                .build();
        Product saved = productRepository.save(copy);
        return toDTO(saved);
    }

    @Transactional
    public ProductDTO updateStatus(Integer id, String status) {
        if (status == null || !(status.equalsIgnoreCase("active") || status.equalsIgnoreCase("inactive"))) {
            throw new BadRequestException("Invalid status: must be 'active' or 'inactive'");
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        if ("DELETED".equalsIgnoreCase(product.getStatus())) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        product.setStatus(status);
        Product updated = productRepository.save(product);
        return toDTO(updated);
    }

    private String generateUniqueSku(String baseSku) {
        String candidate;
        String suffix = randomSuffix();
        candidate = baseSku + "-" + suffix;
        while (productRepository.existsBySku(candidate)) {
            candidate = baseSku + "-" + randomSuffix();
        }
        return candidate;
    }

    private String generateUniqueSlug(String baseSlug) {
        String candidate = baseSlug + "-copy";
        if (!productRepository.existsBySlug(candidate)) return candidate;
        candidate = baseSlug + "-copy-" + randomSuffix();
        while (productRepository.existsBySlug(candidate)) {
            candidate = baseSlug + "-copy-" + randomSuffix();
        }
        return candidate;
    }

    private String generateUniqueItemCode(String baseCode) {
        String candidate = baseCode + "-" + randomSuffix();
        while (productRepository.existsByItemCode(candidate)) {
            candidate = baseCode + "-" + randomSuffix();
        }
        return candidate;
    }

    private String randomSuffix() {
        return java.util.UUID.randomUUID().toString().substring(0, 5).toUpperCase();
    }

    @Transactional
    public com.example.pos.dto.ProductImportReport importProductsFromCsv(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            throw new BadRequestException("CSV content is empty");
        }

        String[] lines = csv.split("\r?\n");
        int total = Math.max(lines.length - 1, 0);
        int imported = 0;
        java.util.List<String> errors = new java.util.ArrayList<>();

        if (lines.length < 2) {
            return com.example.pos.dto.ProductImportReport.builder()
                    .totalRows(0)
                    .imported(0)
                    .failed(0)
                    .errors(java.util.Collections.singletonList("No data rows in CSV"))
                    .build();
        }

        String header = lines[0];
        String[] cols = header.split(",");
        java.util.Map<String, Integer> idx = new java.util.HashMap<>();
        for (int i = 0; i < cols.length; i++) {
            idx.put(cols[i].trim().toLowerCase(), i);
        }

        // Required minimal columns
        String[] required = new String[]{"name","sku","slug","price","quantity","unitId","categoryId","brandId","subCategoryId","storeId","warehouseId"};
        for (String r : required) {
            if (!idx.containsKey(r.toLowerCase())) {
                throw new BadRequestException("Missing required column: " + r);
            }
        }

        for (int r = 1; r < lines.length; r++) {
            String row = lines[r];
            if (row.trim().isEmpty()) continue;
            String[] vals = row.split(",");
            try {
                CreateProductRequest req = new CreateProductRequest();
                req.setName(getStr(vals, idx, "name"));
                req.setSku(getStr(vals, idx, "sku"));
                req.setSlug(getStr(vals, idx, "slug"));
                req.setPrice(new java.math.BigDecimal(getStr(vals, idx, "price")));
                req.setQuantity(Integer.valueOf(getStr(vals, idx, "quantity")));
                req.setUnitId(Integer.valueOf(getStr(vals, idx, "unitId")));
                req.setCategoryId(Integer.valueOf(getStr(vals, idx, "categoryId")));
                req.setBrandId(Integer.valueOf(getStr(vals, idx, "brandId")));
                req.setSubCategoryId(Integer.valueOf(getStr(vals, idx, "subCategoryId")));
                req.setStoreId(Integer.valueOf(getStr(vals, idx, "storeId")));
                req.setWarehouseId(Integer.valueOf(getStr(vals, idx, "warehouseId")));

                if (idx.containsKey("itemCode")) req.setItemCode(getStr(vals, idx, "itemCode"));
                if (idx.containsKey("barcodeSymbology")) req.setBarcodeSymbology(getStr(vals, idx, "barcodeSymbology"));
                if (idx.containsKey("barcodeValue")) req.setBarcodeValue(getStr(vals, idx, "barcodeValue"));
                if (idx.containsKey("sellingType")) req.setSellingType(getStr(vals, idx, "sellingType"));
                if (idx.containsKey("productType")) req.setProductType(getStr(vals, idx, "productType"));
                if (idx.containsKey("taxType")) req.setTaxType(getStr(vals, idx, "taxType"));
                if (idx.containsKey("discountType")) req.setDiscountType(getStr(vals, idx, "discountType"));
                if (idx.containsKey("discountValue")) req.setDiscountValue(new java.math.BigDecimal(getStr(vals, idx, "discountValue")));
                if (idx.containsKey("description")) req.setDescription(getStr(vals, idx, "description"));
                if (idx.containsKey("manufacturer")) req.setManufacturer(getStr(vals, idx, "manufacturer"));
                if (idx.containsKey("imageUrl")) req.setImageUrl(getStr(vals, idx, "imageUrl"));
                if (idx.containsKey("manufacturedDate")) req.setManufacturedDate(java.time.LocalDate.parse(getStr(vals, idx, "manufacturedDate")));
                if (idx.containsKey("expiredDate")) req.setExpiredDate(java.time.LocalDate.parse(getStr(vals, idx, "expiredDate")));

                ProductDTO dto = createProduct(req, null);
                if (dto != null) imported++;
            } catch (Exception e) {
                errors.add("Row " + r + ": " + e.getMessage());
            }
        }

        return com.example.pos.dto.ProductImportReport.builder()
                .totalRows(total)
                .imported(imported)
                .failed(total - imported)
                .errors(errors)
                .build();
    }

    private String getStr(String[] vals, java.util.Map<String,Integer> idx, String key) {
        Integer i = idx.get(key.toLowerCase());
        if (i == null || i >= vals.length) return null;
        return vals[i].trim();
    }
    @Transactional
    public ProductDTO createProduct(CreateProductRequest request, Authentication authentication) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("SKU already in use");
        }
        if (request.getItemCode() != null && productRepository.existsByItemCode(request.getItemCode())) {
            throw new BadRequestException("Item code already in use");
        }
        if (productRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Slug already in use");
        }

        unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new BadRequestException("Unit not found"));
        subCategoryRepository.findById(request.getSubCategoryId())
                .orElseThrow(() -> new BadRequestException("Sub category not found"));
        categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Category not found"));
        brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new BadRequestException("Brand not found"));
        var store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new BadRequestException("Store not found"));
        var warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new BadRequestException("Warehouse not found"));

        if (store.getWarehouseId() != null && !store.getWarehouseId().equals(request.getWarehouseId())) {
            throw new BadRequestException("Store and warehouse mismatch");
        }

        if (request.getBarcodeSymbology() != null) {
            validateEnum(request.getBarcodeSymbology(), Set.of("ean13", "upc_a", "code128", "code39", "qr"), "barcodeSymbology");
        }
        if (request.getSellingType() != null) {
            validateEnum(request.getSellingType(), Set.of("unit", "weight", "service"), "sellingType");
        }
        String productType = request.getProductType() != null ? request.getProductType() : "single";
        validateEnum(productType, Set.of("single", "variable"), "productType");
        if (request.getTaxType() != null) {
            validateEnum(request.getTaxType(), Set.of("inclusive", "exclusive", "none"), "taxType");
        }
        if (request.getDiscountType() != null) {
            validateEnum(request.getDiscountType(), Set.of("percent", "amount"), "discountType");
        }
        if (request.getManufacturedDate() != null && request.getExpiredDate() != null) {
            if (request.getExpiredDate().isBefore(request.getManufacturedDate())) {
                throw new BadRequestException("expiredDate must be after manufacturedDate");
            }
        }

        Integer createdBy = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            createdBy = userPrincipal.getId();
        }

        Product product = Product.builder()
                .name(request.getName())
                .sku(request.getSku())
                .slug(request.getSlug())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .qtyAlert(request.getQtyAlert() != null ? request.getQtyAlert() : 10)
                .unitId(request.getUnitId())
                .subCategoryId(request.getSubCategoryId())
                .categoryId(request.getCategoryId())
                .brandId(request.getBrandId())
                .storeId(request.getStoreId())
                .warehouseId(request.getWarehouseId())
                .itemCode(request.getItemCode())
                .barcodeSymbology(request.getBarcodeSymbology())
                .barcodeValue(request.getBarcodeValue())
                .sellingType(request.getSellingType())
                .productType(productType)
                .taxType(request.getTaxType())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .description(request.getDescription())
                .manufacturer(request.getManufacturer())
                .imageUrl(request.getImageUrl())
                .manufacturedDate(request.getManufacturedDate())
                .expiredDate(request.getExpiredDate())
                .createdBy(createdBy)
                .build();

        Product saved = productRepository.save(product);
        return toDTO(saved);
    }

    @Transactional
    public ProductDTO updateProduct(Integer id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if ("DELETED".equalsIgnoreCase(product.getStatus())) {
            throw new ResourceNotFoundException("Product", "id", id);
        }

        if (request.getName() != null) product.setName(request.getName());
        if (request.getSku() != null) {
            if (!request.getSku().equals(product.getSku()) && productRepository.existsBySku(request.getSku())) {
                throw new BadRequestException("SKU already in use");
            }
            product.setSku(request.getSku());
        }
        if (request.getSlug() != null) {
            if (!request.getSlug().equals(product.getSlug()) && productRepository.existsBySlug(request.getSlug())) {
                throw new BadRequestException("Slug already in use");
            }
            product.setSlug(request.getSlug());
        }
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getQuantity() != null) product.setQuantity(request.getQuantity());
        if (request.getQtyAlert() != null) product.setQtyAlert(request.getQtyAlert());
        if (request.getUnitId() != null) product.setUnitId(request.getUnitId());
        if (request.getSubCategoryId() != null) product.setSubCategoryId(request.getSubCategoryId());
        if (request.getCategoryId() != null) product.setCategoryId(request.getCategoryId());
        if (request.getBrandId() != null) product.setBrandId(request.getBrandId());
        if (request.getStoreId() != null) {
            var store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new BadRequestException("Store not found"));
            Integer targetWarehouseId = request.getWarehouseId() != null ? request.getWarehouseId() : product.getWarehouseId();
            if (store.getWarehouseId() != null && targetWarehouseId != null && !store.getWarehouseId().equals(targetWarehouseId)) {
                throw new BadRequestException("Store and warehouse mismatch");
            }
            product.setStoreId(request.getStoreId());
        }
        if (request.getWarehouseId() != null) {
            warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new BadRequestException("Warehouse not found"));
            if (product.getStoreId() != null) {
                var store = storeRepository.findById(product.getStoreId())
                        .orElseThrow(() -> new BadRequestException("Store not found"));
                if (store.getWarehouseId() != null && !store.getWarehouseId().equals(request.getWarehouseId())) {
                    throw new BadRequestException("Store and warehouse mismatch");
                }
            }
            product.setWarehouseId(request.getWarehouseId());
        }
        if (request.getItemCode() != null) {
            if (product.getItemCode() == null || !product.getItemCode().equals(request.getItemCode())) {
                if (productRepository.existsByItemCode(request.getItemCode())) {
                    throw new BadRequestException("Item code already in use");
                }
            }
            product.setItemCode(request.getItemCode());
        }
        if (request.getBarcodeSymbology() != null) {
            validateEnum(request.getBarcodeSymbology(), Set.of("ean13", "upc_a", "code128", "code39", "qr"), "barcodeSymbology");
            product.setBarcodeSymbology(request.getBarcodeSymbology());
        }
        if (request.getBarcodeValue() != null) product.setBarcodeValue(request.getBarcodeValue());
        if (request.getSellingType() != null) {
            validateEnum(request.getSellingType(), Set.of("unit", "weight", "service"), "sellingType");
            product.setSellingType(request.getSellingType());
        }
        if (request.getProductType() != null) {
            validateEnum(request.getProductType(), Set.of("single", "variable"), "productType");
            product.setProductType(request.getProductType());
        }
        if (request.getTaxType() != null) {
            validateEnum(request.getTaxType(), Set.of("inclusive", "exclusive", "none"), "taxType");
            product.setTaxType(request.getTaxType());
        }
        if (request.getDiscountType() != null) {
            validateEnum(request.getDiscountType(), Set.of("percent", "amount"), "discountType");
            product.setDiscountType(request.getDiscountType());
        }
        if (request.getDiscountValue() != null) product.setDiscountValue(request.getDiscountValue());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getManufacturer() != null) product.setManufacturer(request.getManufacturer());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
        LocalDate manufacturedDate = request.getManufacturedDate() != null ? request.getManufacturedDate() : product.getManufacturedDate();
        LocalDate expiredDate = request.getExpiredDate() != null ? request.getExpiredDate() : product.getExpiredDate();
        if (manufacturedDate != null && expiredDate != null && expiredDate.isBefore(manufacturedDate)) {
            throw new BadRequestException("expiredDate must be after manufacturedDate");
        }
        if (request.getManufacturedDate() != null) product.setManufacturedDate(request.getManufacturedDate());
        if (request.getExpiredDate() != null) product.setExpiredDate(request.getExpiredDate());
        if (request.getStatus() != null) product.setStatus(request.getStatus());

        Product updated = productRepository.save(product);
        return toDTO(updated);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        if ("DELETED".equalsIgnoreCase(product.getStatus())) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        return toDTO(product);
    }

    private void validateEnum(String value, Set<String> allowed, String field) {
        String v = value.toLowerCase();
        if (!allowed.contains(v)) {
            throw new BadRequestException("Invalid " + field + ": must be one of " + String.join(", ", allowed));
        }
    }

    @Transactional(readOnly = true)
    public com.example.pos.dto.BarcodeDataResponse getBarcodeData(Integer id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        if (p.getBarcodeSymbology() == null || p.getBarcodeValue() == null) {
            throw new BadRequestException("No barcode data for this product");
        }
        return com.example.pos.dto.BarcodeDataResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .sku(p.getSku())
                .symbology(p.getBarcodeSymbology())
                .value(p.getBarcodeValue())
                .build();
    }

    @Transactional
    public void softDeleteProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setStatus("DELETED");
        productRepository.save(product);
    }

    private ProductDTO toDTO(Product p) {
        String unitShortName = null;
        String categoryName = null;
        String brandName = null;
        String createdByName = null;
        String storeName = null;
        String warehouseName = null;

        if (p.getUnit() != null) unitShortName = p.getUnit().getShortName();
        if (p.getCategory() != null) categoryName = p.getCategory().getName();
        if (p.getBrand() != null) brandName = p.getBrand().getName();
        if (p.getCreatedByUser() != null) createdByName = p.getCreatedByUser().getName();
        if (p.getStore() != null) storeName = p.getStore().getName();
        if (p.getWarehouse() != null) warehouseName = p.getWarehouse().getName();

        return ProductDTO.builder()
                .id(p.getId())
                .sku(p.getSku())
                .name(p.getName())
                .slug(p.getSlug())
                .price(p.getPrice())
                .quantity(p.getQuantity())
                .qtyAlert(p.getQtyAlert())
                .status(p.getStatus())
                .unitId(p.getUnitId())
                .subCategoryId(p.getSubCategoryId())
                .categoryId(p.getCategoryId())
                .brandId(p.getBrandId())
                .storeId(p.getStoreId())
                .warehouseId(p.getWarehouseId())
                .storeName(storeName)
                .warehouseName(warehouseName)
                .unitShortName(unitShortName)
                .categoryName(categoryName)
                .brandName(brandName)
                .createdByName(createdByName)
                .itemCode(p.getItemCode())
                .barcodeSymbology(p.getBarcodeSymbology())
                .barcodeValue(p.getBarcodeValue())
                .sellingType(p.getSellingType())
                .productType(p.getProductType())
                .taxType(p.getTaxType())
                .discountType(p.getDiscountType())
                .discountValue(p.getDiscountValue())
                .description(p.getDescription())
                .manufacturer(p.getManufacturer())
                .imageUrl(p.getImageUrl())
                .manufacturedDate(p.getManufacturedDate())
                .expiredDate(p.getExpiredDate())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}