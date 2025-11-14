package com.example.pos.repository;

import com.example.pos.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // Find all categories excluding DELETED status
    Page<Category> findByStatusNot(String status, Pageable pageable);

    // Find categories by status with pagination
    Page<Category> findByStatus(String status, Pageable pageable);

    // Search categories by name or slug with pagination
    @Query("SELECT c FROM Category c WHERE c.status != 'DELETED' AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.slug) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Category> searchCategories(@Param("search") String search, Pageable pageable);

    // Search categories by status and search term with pagination
    @Query("SELECT c FROM Category c WHERE c.status = :status AND " +
           "c.status != 'DELETED' AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.slug) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Category> searchCategoriesByStatus(@Param("status") String status,
                                             @Param("search") String search,
                                             Pageable pageable);

    // Find by slug (for uniqueness check)
    Optional<Category> findBySlug(String slug);
}

