package com.example.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Column(name = "warehouse_id")
    private Integer warehouseId;

    @Column(name = "user_id")
    private Integer userId;

    @Builder.Default
    @Column(name = "total_products")
    private Integer totalProducts = 0;

    @Builder.Default
    @Column(name = "total_stock")
    private Integer totalStock = 0;

    @Builder.Default
    @Column(length = 20)
    private String status = "active";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", insertable = false, updatable = false)
    private Warehouse warehouse;
}

