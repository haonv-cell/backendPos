package com.example.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Integer id;
    private String sku;
    private String name;
    private String slug;
    private BigDecimal price;
    private Integer quantity;
    private String status;
    private Integer unitId;
    private Integer subCategoryId;
    private Integer categoryId;
    private Integer brandId;
    private Integer storeId;
    private Integer warehouseId;
    private String itemCode;
    private String barcodeSymbology;
    private String barcodeValue;
    private String sellingType;
    private String productType;
    private String taxType;
    private String discountType;
    private BigDecimal discountValue;
    private String description;
    private String manufacturer;
    private String imageUrl;
    private LocalDate manufacturedDate;
    private LocalDate expiredDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}