package com.example.pos.dto.subcategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubCategoryDTO {
    
    private Integer id;
    private String name;
    private String code;
    private String description;
    private String status;
    private String imageUrl;
    private LocalDateTime createdAt;
    
    // Thông tin từ Category cha
    private Integer categoryId;
    private String categoryName;
}