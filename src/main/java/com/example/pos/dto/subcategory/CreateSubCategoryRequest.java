package com.example.pos.dto.subcategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSubCategoryRequest {
    
    @NotBlank(message = "SubCategory name is required")
    private String name;
    
    @NotBlank(message = "SubCategory code is required")
    private String code;
    
    @NotNull(message = "Category ID is required")
    private Integer categoryId;
    
    private String description;
    private String imageUrl;

    private String status;
}