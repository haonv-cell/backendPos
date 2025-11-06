package com.example.pos.repository;

import com.example.pos.entity.Role;
import com.example.pos.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByCode(String code);

    Boolean existsByEmail(String email);

    Boolean existsByCode(String code);

    // Find customers with pagination
    Page<User> findByRole(Role role, Pageable pageable);

    // Find customers by status with pagination
    Page<User> findByRoleAndStatus(Role role, String status, Pageable pageable);

    // Search customers by name, email, code, phone, or country with pagination
    // Using native query with ILIKE for case-insensitive search in PostgreSQL
    @Query(value = "SELECT * FROM users u WHERE u.role = :role AND " +
           "(u.name ILIKE CONCAT('%', :search, '%') OR " +
           "u.email ILIKE CONCAT('%', :search, '%') OR " +
           "u.code ILIKE CONCAT('%', :search, '%') OR " +
           "u.phone ILIKE CONCAT('%', :search, '%') OR " +
           "u.country ILIKE CONCAT('%', :search, '%'))",
           countQuery = "SELECT COUNT(*) FROM users u WHERE u.role = :role AND " +
           "(u.name ILIKE CONCAT('%', :search, '%') OR " +
           "u.email ILIKE CONCAT('%', :search, '%') OR " +
           "u.code ILIKE CONCAT('%', :search, '%') OR " +
           "u.phone ILIKE CONCAT('%', :search, '%') OR " +
           "u.country ILIKE CONCAT('%', :search, '%'))",
           nativeQuery = true)
    Page<User> searchCustomers(@Param("role") String role,
                               @Param("search") String search,
                               Pageable pageable);

    // Search customers by status and search term with pagination
    @Query(value = "SELECT * FROM users u WHERE u.role = :role AND u.status = :status AND " +
           "(u.name ILIKE CONCAT('%', :search, '%') OR " +
           "u.email ILIKE CONCAT('%', :search, '%') OR " +
           "u.code ILIKE CONCAT('%', :search, '%') OR " +
           "u.phone ILIKE CONCAT('%', :search, '%') OR " +
           "u.country ILIKE CONCAT('%', :search, '%'))",
           countQuery = "SELECT COUNT(*) FROM users u WHERE u.role = :role AND u.status = :status AND " +
           "(u.name ILIKE CONCAT('%', :search, '%') OR " +
           "u.email ILIKE CONCAT('%', :search, '%') OR " +
           "u.code ILIKE CONCAT('%', :search, '%') OR " +
           "u.phone ILIKE CONCAT('%', :search, '%') OR " +
           "u.country ILIKE CONCAT('%', :search, '%'))",
           nativeQuery = true)
    Page<User> searchCustomersByStatus(@Param("role") String role,
                                        @Param("status") String status,
                                        @Param("search") String search,
                                        Pageable pageable);
}

