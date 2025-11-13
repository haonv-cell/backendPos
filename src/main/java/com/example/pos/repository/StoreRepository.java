package com.example.pos.repository;

import com.example.pos.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Integer> {

    // Find all stores excluding DELETED status with JOIN
    @Query("SELECT s FROM Store s LEFT JOIN FETCH s.user LEFT JOIN FETCH s.warehouse WHERE s.status != :status")
    Page<Store> findByStatusNot(@Param("status") String status, Pageable pageable);

    // Find stores by status with pagination with JOIN
    @Query("SELECT s FROM Store s LEFT JOIN FETCH s.user LEFT JOIN FETCH s.warehouse WHERE s.status = :status")
    Page<Store> findByStatus(@Param("status") String status, Pageable pageable);

    // Find by ID with relationships loaded
    @Query("SELECT s FROM Store s LEFT JOIN FETCH s.user LEFT JOIN FETCH s.warehouse WHERE s.id = :id")
    Optional<Store> findByIdWithRelations(@Param("id") Integer id);

    // Find by email (for unique check)
    Optional<Store> findByEmail(String email);

    // Find by code (for unique check)
    Optional<Store> findByCode(String code);

    // Search stores by name, email, phone, or user name with pagination
    @Query("SELECT s FROM Store s LEFT JOIN FETCH s.user u LEFT JOIN FETCH s.warehouse WHERE s.status != 'DELETED' AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Store> searchStores(@Param("search") String search, Pageable pageable);

    // Search stores by status and search term with pagination
    @Query("SELECT s FROM Store s LEFT JOIN FETCH s.user u LEFT JOIN FETCH s.warehouse WHERE s.status = :status AND " +
           "s.status != 'DELETED' AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Store> searchStoresByStatus(@Param("status") String status,
                                      @Param("search") String search,
                                      Pageable pageable);
}

