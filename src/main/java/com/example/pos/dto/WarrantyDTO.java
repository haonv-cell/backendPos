package com.example.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyDTO {
    private Integer id;
    private String name;
    private String description;
    private Integer duration;
    private String durationUnit;
    private String status;
}