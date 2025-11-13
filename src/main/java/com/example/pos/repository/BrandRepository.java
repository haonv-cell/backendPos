package com.example.pos.repository;

import com.example.pos.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer> {

    // Find all brands excluding DELETED status
    Page<Brand> findByStatusNot(String status, Pageable pageable);

    // Find brands by status with pagination
    Page<Brand> findByStatus(String status, Pageable pageable);

    // Search brands by name with pagination
    @Query("SELECT b FROM Brand b WHERE b.status != 'DELETED' AND " +
           "LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Brand> searchBrands(@Param("search") String search, Pageable pageable);

    // Search brands by status and search term with pagination
    @Query("SELECT b FROM Brand b WHERE b.status = :status AND " +
           "b.status != 'DELETED' AND " +
           "LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Brand> searchBrandsByStatus(@Param("status") String status,
                                      @Param("search") String search,
                                      Pageable pageable);
}

