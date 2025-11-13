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
public class UnitDTO {
    
    private Integer id;
    private String name;
    private String shortName;
    private Integer noOfProducts;
    private LocalDateTime createdAt;
    private String status;
}

