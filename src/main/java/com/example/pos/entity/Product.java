package com.example.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 20, unique = true)
    private String sku;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 150, unique = true)
    private String slug;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Builder.Default
    @Column
    private Integer quantity = 0;

    @Builder.Default
    @Column(length = 20)
    private String status = "active";

    @Column(name = "unit_id")
    private Integer unitId;

    @Column(name = "sub_category_id")
    private Integer subCategoryId;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "brand_id")
    private Integer brandId;

    @Column(name = "store_id")
    private Integer storeId;

    @Column(name = "warehouse_id")
    private Integer warehouseId;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "item_code", length = 50)
    private String itemCode;

    @Column(name = "barcode_symbology", length = 20)
    private String barcodeSymbology;

    @Column(name = "barcode_value", length = 128)
    private String barcodeValue;

    @Column(name = "selling_type", length = 20)
    private String sellingType;

    @Column(name = "product_type", length = 20)
    private String productType;

    @Column(name = "tax_type", length = 20)
    private String taxType;

    @Column(name = "discount_type", length = 20)
    private String discountType;

    @Column(name = "discount_value", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountValue = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 150)
    private String manufacturer;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "manufactured_date")
    private LocalDate manufacturedDate;

    @Column(name = "expired_date")
    private LocalDate expiredDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships (read-only for convenience)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", insertable = false, updatable = false)
    private Unit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id", insertable = false, updatable = false)
    private SubCategory subCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", insertable = false, updatable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", insertable = false, updatable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", insertable = false, updatable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User createdByUser;
}