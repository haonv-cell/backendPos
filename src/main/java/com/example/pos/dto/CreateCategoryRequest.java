package com.example.pos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCategoryRequest {
    
    @NotBlank(message = "Category name is required")
    private String name;
    
    @Pattern(regexp = "^[a-z0-9-]*$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    private String slug;

    private String status;
}

