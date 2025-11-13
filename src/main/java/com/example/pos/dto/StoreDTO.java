package com.example.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreDTO {

    private Integer id;
    private String code;
    private String name;
    private String userName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;
    private Integer warehouseId;
    private String warehouseName;
    private Integer userId;
    private Integer totalProducts;
    private Integer totalStock;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

