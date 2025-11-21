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
           "(:productType IS NULL OR LOWER(p.productType) = LOWER(:productType)) AND " +
           "(:sellingType IS NULL OR LOWER(p.sellingType) = LOWER(:sellingType)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(p.itemCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> searchProducts(
            @Param("status") String status,
            @Param("search") String search,
            @Param("categoryId") Integer categoryId,
            @Param("brandId") Integer brandId,
            @Param("unitId") Integer unitId,
            @Param("subCategoryId") Integer subCategoryId,
            @Param("storeId") Integer storeId,
            @Param("warehouseId") Integer warehouseId,
            @Param("productType") String productType,
            @Param("sellingType") String sellingType,
            Pageable pageable
    );
}