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
public class SupplierListResponse {
    
    private List<SupplierDTO> suppliers;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}


