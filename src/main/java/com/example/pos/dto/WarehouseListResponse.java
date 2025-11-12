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
public class WarehouseListResponse {

    private List<WarehouseDTO> warehouses;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}

