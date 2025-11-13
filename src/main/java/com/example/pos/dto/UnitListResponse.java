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
public class UnitListResponse {
    
    private List<UnitDTO> units;
    private int currentPage;
    private int totalPages;
    private long totalItems;
}

