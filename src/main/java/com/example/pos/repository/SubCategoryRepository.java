package com.example.pos.repository;



import com.example.pos.entity.SubCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Integer> {

    Optional<SubCategory> findByCode(String code);

    // Lấy tất cả (trừ DELETED)
    Page<SubCategory> findByStatusNot(String status, Pageable pageable);
    
    // Lọc theo Status
    Page<SubCategory> findByStatus(String status, Pageable pageable);
    
    // Lọc theo Category
    Page<SubCategory> findByCategoryIdAndStatusNot(Integer categoryId, String status, Pageable pageable);
    
    // Lọc theo Category và Status
    Page<SubCategory> findByCategoryIdAndStatus(Integer categoryId, String status, Pageable pageable);

    // Tìm kiếm (theo name, code)
    @Query("SELECT sc FROM SubCategory sc WHERE sc.status != 'DELETED' AND " +
           "(LOWER(sc.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(sc.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<SubCategory> searchSubCategories(@Param("search") String search, Pageable pageable);

    // Tìm kiếm + Lọc Status
    @Query("SELECT sc FROM SubCategory sc WHERE sc.status = :status AND " +
           "(LOWER(sc.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(sc.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<SubCategory> searchSubCategoriesByStatus(@Param("status") String status,
                                                   @Param("search") String search,
                                                   Pageable pageable);
                                                   
    // ... (Bạn có thể thêm các kết hợp truy vấn khác cho Lọc Category + Tìm kiếm nếu cần)
    // Hoặc đơn giản là dùng logic trong Service để xử lý
    // 7. Tìm kiếm + Lọc Category
    @Query("SELECT sc FROM SubCategory sc WHERE sc.category.id = :categoryId AND sc.status != 'DELETED' AND " +
           "(LOWER(sc.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(sc.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<SubCategory> searchSubCategoriesByCategory(@Param("categoryId") Integer categoryId,
                                                    @Param("search") String search,
                                                    Pageable pageable);

    // 8. Tìm kiếm + Lọc Status + Lọc Category
    @Query("SELECT sc FROM SubCategory sc WHERE sc.status = :status AND sc.category.id = :categoryId AND " +
           "(LOWER(sc.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(sc.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<SubCategory> searchSubCategoriesByStatusAndCategory(@Param("status") String status,
                                                              @Param("categoryId") Integer categoryId,
                                                              @Param("search") String search,
                                                              Pageable pageable);
}