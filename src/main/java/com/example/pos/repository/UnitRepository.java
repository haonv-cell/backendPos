package com.example.pos.repository;

import com.example.pos.entity.Unit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Integer> {

    // Find all units excluding DELETED status
    Page<Unit> findByStatusNot(String status, Pageable pageable);

    // Find units by status with pagination
    Page<Unit> findByStatus(String status, Pageable pageable);

    // Search units by name or short_name with pagination
    @Query("SELECT u FROM Unit u WHERE u.status != 'DELETED' AND " +
           "(LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.shortName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Unit> searchUnits(@Param("search") String search, Pageable pageable);

    // Search units by status and search term with pagination
    @Query("SELECT u FROM Unit u WHERE u.status = :status AND " +
           "u.status != 'DELETED' AND " +
           "(LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.shortName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Unit> searchUnitsByStatus(@Param("status") String status,
                                    @Param("search") String search,
                                    Pageable pageable);
}

