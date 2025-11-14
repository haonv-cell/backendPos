package com.example.pos.dto.subcategory;



import lombok.Data;

@Data
public class UpdateSubCategoryRequest {
    
    private String name;
    private String code;
    private Integer categoryId;
    private String description;
    private String status;
    private String imageUrl;
}