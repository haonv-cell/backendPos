package com.example.pos.repository;

import com.example.pos.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {

    // Find all warehouses excluding DELETED status
    Page<Warehouse> findByStatusNot(String status, Pageable pageable);

    // Find warehouses by status with pagination
    Page<Warehouse> findByStatus(String status, Pageable pageable);

    // Search warehouses by name, contact_person, or phone with pagination
    @Query("SELECT w FROM Warehouse w LEFT JOIN w.user u WHERE w.status != 'DELETED' AND " +
           "(LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(w.contactPerson) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(w.phone) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Warehouse> searchWarehouses(@Param("search") String search, Pageable pageable);

    // Search warehouses by status and search term with pagination
    @Query("SELECT w FROM Warehouse w LEFT JOIN w.user u WHERE w.status = :status AND " +
           "w.status != 'DELETED' AND " +
           "(LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(w.contactPerson) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(w.phone) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Warehouse> searchWarehousesByStatus(@Param("status") String status,
                                              @Param("search") String search,
                                              Pageable pageable);
}

