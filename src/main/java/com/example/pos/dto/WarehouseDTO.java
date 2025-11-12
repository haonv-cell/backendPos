package com.example.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseDTO {

    private Integer id;
    private String name;
    private String contactPerson;
    private String phone;
    private Integer totalProducts;
    private Integer stock;
    private Integer qty;
    private LocalDate createdOn;
    private String status;
    private Integer userId;
    private String managingUserName;
}

