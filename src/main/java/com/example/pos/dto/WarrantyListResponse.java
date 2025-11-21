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
public class WarrantyListResponse {
    private List<WarrantyDTO> warranties;
    private int currentPage;
    private int totalPages;
    private long totalItems;
}