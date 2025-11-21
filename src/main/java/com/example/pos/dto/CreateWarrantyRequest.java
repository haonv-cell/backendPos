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
public class CreateWarrantyRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    private String description;

    @NotNull
    @Min(1)
    private Integer duration;

    @NotBlank
    @Size(max = 20)
    private String durationUnit;
}