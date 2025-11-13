package com.example.pos.repository;

import com.example.pos.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

    Page<Supplier> findByStatus(String status, Pageable pageable);

    Page<Supplier> findByStatusNot(String status, Pageable pageable);

    Optional<Supplier> findByContactEmail(String contactEmail);

    @Query("SELECT s FROM Supplier s WHERE s.status != 'DELETED' AND (" +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.contactName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.contactEmail) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.contactPhone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.address) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Supplier> searchSuppliers(@Param("search") String search, Pageable pageable);

    @Query("SELECT s FROM Supplier s WHERE s.status = :status AND (" +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.contactName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.contactEmail) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.contactPhone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.address) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Supplier> searchSuppliersByStatus(@Param("status") String status, @Param("search") String search, Pageable pageable);
}


