package com.example.pos.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequest {
    @Size(max = 150)
    private String name;

    @Size(max = 20)
    private String sku;

    @Size(max = 150)
    private String slug;

    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal price;

    @Min(0)
    private Integer quantity;

    private Integer unitId;
    private Integer subCategoryId;
    private Integer categoryId;
    private Integer brandId;
    private Integer storeId;
    private Integer warehouseId;

    @Size(max = 50)
    private String itemCode;

    @Size(max = 20)
    private String barcodeSymbology;

    @Size(max = 128)
    private String barcodeValue;

    @Size(max = 20)
    private String sellingType;

    @Size(max = 20)
    private String productType;

    @Size(max = 20)
    private String taxType;

    @Size(max = 20)
    private String discountType;

    @DecimalMin(value = "0.0")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal discountValue;

    private String description;

    @Size(max = 150)
    private String manufacturer;
    private String imageUrl;
    private LocalDate manufacturedDate;
    private LocalDate expiredDate;

    @Size(max = 20)
    private String status; // active|inactive
}