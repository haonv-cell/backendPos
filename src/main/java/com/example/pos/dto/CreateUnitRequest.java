package com.example.pos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUnitRequest {
    
    @NotBlank(message = "Unit name is required")
    private String name;
    
    @NotBlank(message = "Short name is required")
    private String shortName;
}

