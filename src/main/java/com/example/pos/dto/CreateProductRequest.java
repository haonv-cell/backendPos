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
public class CreateProductRequest {
    @NotBlank
    @Size(max = 150)
    private String name;

    @NotBlank
    @Size(max = 20)
    private String sku;

    @NotBlank
    @Size(max = 150)
    private String slug;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal price;

    @NotNull
    @Min(0)
    private Integer quantity;

    @NotNull
    private Integer unitId;

    @NotNull
    private Integer subCategoryId;

    @NotNull
    private Integer categoryId;

    @NotNull
    private Integer brandId;

    @NotNull
    private Integer storeId;

    @NotNull
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
    private String productType; // single|variable

    @Size(max = 20)
    private String taxType; // inclusive|exclusive|none

    @Size(max = 20)
    private String discountType; // percent|amount

    @DecimalMin(value = "0.0")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal discountValue;

    private String description;

    @Size(max = 150)
    private String manufacturer;

    private String imageUrl;
    private LocalDate manufacturedDate;
    private LocalDate expiredDate;
}