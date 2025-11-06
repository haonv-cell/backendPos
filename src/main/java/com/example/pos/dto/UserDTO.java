package com.example.pos.dto;

import com.example.pos.entity.AuthProvider;
import com.example.pos.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    
    private Integer id;
    private String code;
    private String name;
    private String email;
    private String phone;
    private String country;
    private String companyName;
    private Role role;
    private String status;
    private AuthProvider provider;
    private String imageUrl;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

