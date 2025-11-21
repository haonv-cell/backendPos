package com.example.pos.repository;

import com.example.pos.entity.Warranty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WarrantyRepository extends JpaRepository<Warranty, Integer> {

    Page<Warranty> findByStatusNot(String status, Pageable pageable);

    Page<Warranty> findByStatus(String status, Pageable pageable);

    @Query("SELECT w FROM Warranty w WHERE w.status != 'DELETED' AND " +
           "(LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(w.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Warranty> searchWarranties(@Param("search") String search, Pageable pageable);

    @Query("SELECT w FROM Warranty w WHERE w.status = :status AND " +
           "(LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(w.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Warranty> searchWarrantiesByStatus(@Param("status") String status,
                                            @Param("search") String search,
                                            Pageable pageable);
}