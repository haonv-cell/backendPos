package com.example.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListResponse {
    private List<ProductDTO> products;
    private int currentPage;
    private int totalPages;
    private long totalItems;
}