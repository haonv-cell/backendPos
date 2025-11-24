package com.example.pos.repository;

import com.example.pos.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    Boolean existsBySku(String sku);
    Boolean existsByItemCode(String itemCode);
    Boolean existsBySlug(String slug);

    @Query("SELECT p FROM Product p WHERE " +
           "((:status IS NULL AND p.status != 'DELETED') OR (p.status = :status)) AND " +
           "(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
           "(:brandId IS NULL OR p.brandId = :brandId) AND " +
           "(:unitId IS NULL OR p.unitId = :unitId) AND " +
           "(:subCategoryId IS NULL OR p.subCategoryId = :subCategoryId) AND " +
           "(:storeId IS NULL OR p.storeId = :storeId) AND " +
           "(:warehouseId IS NULL OR p.warehouseId = :warehouseId) AND " +
           "(:productTypeLower IS NULL OR LOWER(p.productType) = :productTypeLower) AND " +
           "(:sellingTypeLower IS NULL OR LOWER(p.sellingType) = :sellingTypeLower) AND " +
           "(:searchLower IS NULL OR :searchLower = '' OR " +
           " LOWER(p.name) LIKE CONCAT('%', :searchLower, '%') OR " +
           " LOWER(p.sku) LIKE CONCAT('%', :searchLower, '%') OR " +
           " LOWER(p.itemCode) LIKE CONCAT('%', :searchLower, '%'))")
    Page<Product> searchProducts(
            @Param("status") String status,
            @Param("searchLower") String searchLower,
            @Param("categoryId") Integer categoryId,
            @Param("brandId") Integer brandId,
            @Param("unitId") Integer unitId,
            @Param("subCategoryId") Integer subCategoryId,
            @Param("storeId") Integer storeId,
            @Param("warehouseId") Integer warehouseId,
            @Param("productTypeLower") String productTypeLower,
            @Param("sellingTypeLower") String sellingTypeLower,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p WHERE p.status != 'DELETED' AND " +
           "(p.qtyAlert IS NOT NULL AND p.quantity <= p.qtyAlert OR (:threshold IS NOT NULL AND p.quantity <= :threshold))")
    Page<Product> findLowStocks(@Param("threshold") Integer threshold, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status != 'DELETED' AND p.expiredDate IS NOT NULL AND p.expiredDate <= :today")
    Page<Product> findExpiredProducts(@Param("today") java.time.LocalDate today, Pageable pageable);
}