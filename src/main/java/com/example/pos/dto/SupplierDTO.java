package com.example.pos.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDTO {
    private Integer id;
    private String name;
    private String contactName;
    private String email;
    private String phone;
    private String country;
    private String status;
    private LocalDateTime createdAt;
}


