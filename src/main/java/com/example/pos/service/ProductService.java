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

import java.util.List;
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

        Page<Product> productPage = productRepository.searchProducts(
                status,
                search != null && !search.trim().isEmpty() ? search.trim() : null,
                categoryId,
                brandId,
                unitId,
                subCategoryId,
                storeId,
                warehouseId,
                productType,
                sellingType,
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
        storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new BadRequestException("Store not found"));
        warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new BadRequestException("Warehouse not found"));

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
                .productType(request.getProductType())
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
        if (request.getUnitId() != null) product.setUnitId(request.getUnitId());
        if (request.getSubCategoryId() != null) product.setSubCategoryId(request.getSubCategoryId());
        if (request.getCategoryId() != null) product.setCategoryId(request.getCategoryId());
        if (request.getBrandId() != null) product.setBrandId(request.getBrandId());
        if (request.getStoreId() != null) product.setStoreId(request.getStoreId());
        if (request.getWarehouseId() != null) product.setWarehouseId(request.getWarehouseId());
        if (request.getItemCode() != null) {
            if (product.getItemCode() == null || !product.getItemCode().equals(request.getItemCode())) {
                if (productRepository.existsByItemCode(request.getItemCode())) {
                    throw new BadRequestException("Item code already in use");
                }
            }
            product.setItemCode(request.getItemCode());
        }
        if (request.getBarcodeSymbology() != null) product.setBarcodeSymbology(request.getBarcodeSymbology());
        if (request.getBarcodeValue() != null) product.setBarcodeValue(request.getBarcodeValue());
        if (request.getSellingType() != null) product.setSellingType(request.getSellingType());
        if (request.getProductType() != null) product.setProductType(request.getProductType());
        if (request.getTaxType() != null) product.setTaxType(request.getTaxType());
        if (request.getDiscountType() != null) product.setDiscountType(request.getDiscountType());
        if (request.getDiscountValue() != null) product.setDiscountValue(request.getDiscountValue());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getManufacturer() != null) product.setManufacturer(request.getManufacturer());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
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

    @Transactional
    public void softDeleteProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setStatus("DELETED");
        productRepository.save(product);
    }

    private ProductDTO toDTO(Product p) {
        return ProductDTO.builder()
                .id(p.getId())
                .sku(p.getSku())
                .name(p.getName())
                .slug(p.getSlug())
                .price(p.getPrice())
                .quantity(p.getQuantity())
                .status(p.getStatus())
                .unitId(p.getUnitId())
                .subCategoryId(p.getSubCategoryId())
                .categoryId(p.getCategoryId())
                .brandId(p.getBrandId())
                .storeId(p.getStoreId())
                .warehouseId(p.getWarehouseId())
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