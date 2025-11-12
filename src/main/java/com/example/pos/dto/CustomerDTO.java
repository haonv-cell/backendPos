package com.example.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDTO {
    
    private Integer id;
    private String code;
    private String name;
    private String companyName;
    private String email;
    private String phone;
    private String country;
    private String status;
    private String imageUrl;
    private LocalDateTime createdAt;
}

