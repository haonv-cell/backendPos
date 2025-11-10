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

       // Thành hàm này:
       Page<User> findByRoleAndStatusNot(Role role, String status, Pageable pageable);

       // Find customers by status with pagination
       Page<User> findByRoleAndStatus(Role role, String status, Pageable pageable);

       // Search customers by name, email, code, phone, or country with pagination
       @Query("SELECT u FROM User u WHERE u.role = :role AND " +
                     "u.status != 'DELETED' AND " +
                     "(LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(u.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(u.country) LIKE LOWER(CONCAT('%', :search, '%')))")
       Page<User> searchCustomers(@Param("role") Role role, @Param("search") String search, Pageable pageable);

       // Sửa hàm này:
       @Query("SELECT u FROM User u WHERE u.role = :role AND " +
                     "u.status != 'DELETED' AND " + // (Dòng này bạn thêm vào là đúng rồi)
                     "(LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     // BỎ DẤU ( THỪA Ở ĐẦU DÒNG TIẾP THEO
                     "LOWER(u.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(u.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(u.country) LIKE LOWER(CONCAT('%', :search, '%')))")
       Page<User> searchBillers(@Param("role") Role role, @Param("search") String search, Pageable pageable);

       // Search customers by status and search term with pagination
       @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = :status AND " +
                     "u.status != 'DELETED' AND " +
                     "(LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(u.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(u.country) LIKE LOWER(CONCAT('%', :search, '%')))")
       Page<User> searchCustomersByStatus(@Param("role") Role role, @Param("status") String status,
                     @Param("search") String search, Pageable pageable);

       // Và sửa cả hàm này:
       @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = :status AND " +
                     "(LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     // BỎ DẤU ( THỪA Ở ĐẦU DÒNG TIẾP THEO
                     "LOWER(u.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(u.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(u.country) LIKE LOWER(CONCAT('%', :search, '%')))")
       Page<User> searchBillersByStatus(@Param("role") Role role, @Param("status") String status,
                     @Param("search") String search, Pageable pageable);
}
