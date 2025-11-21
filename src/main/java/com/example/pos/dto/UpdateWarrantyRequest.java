package com.example.pos.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateWarrantyRequest {
    @Size(max = 100)
    private String name;

    private String description;

    @Min(1)
    private Integer duration;

    @Size(max = 20)
    private String durationUnit;

    @Size(max = 20)
    private String status;
}