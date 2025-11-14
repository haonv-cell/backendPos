package com.example.pos.dto.subcategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor // <-- THÊM VÀO
@AllArgsConstructor // <-- THÊM VÀO
@Builder
public class SubCategoryListResponse {
    
    private List<SubCategoryDTO> subCategories;
    private int currentPage;
    private int totalPages;
    private long totalItems;
}