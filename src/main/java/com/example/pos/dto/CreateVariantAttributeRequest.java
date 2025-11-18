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
public class CreateVariantAttributeRequest {
    
    @NotBlank(message = "Variant attribute name is required")
    private String name;
    
    private String[] values;
    
    private String imageUrl;

    private String status;
}

