package com.example.pos.repository;

import com.example.pos.entity.VariantAttribute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VariantAttributeRepository extends JpaRepository<VariantAttribute, Integer> {

    // Find all variant attributes excluding DELETED status
    Page<VariantAttribute> findByStatusNot(String status, Pageable pageable);

    // Find variant attributes by status with pagination
    Page<VariantAttribute> findByStatus(String status, Pageable pageable);

    // Search variant attributes by name with pagination
    @Query("SELECT v FROM VariantAttribute v WHERE v.status != 'DELETED' AND " +
           "LOWER(v.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<VariantAttribute> searchVariantAttributes(@Param("search") String search, Pageable pageable);

    // Search variant attributes by status and search term with pagination
    @Query("SELECT v FROM VariantAttribute v WHERE v.status = :status AND " +
           "v.status != 'DELETED' AND " +
           "LOWER(v.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<VariantAttribute> searchVariantAttributesByStatus(@Param("status") String status,
                                                            @Param("search") String search,
                                                            Pageable pageable);
}

