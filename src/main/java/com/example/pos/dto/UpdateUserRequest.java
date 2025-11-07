package com.example.pos.dto;

import com.example.pos.entity.Role;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {
    
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;
    
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;
    
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;
    
    @Size(max = 150, message = "Company name must not exceed 150 characters")
    private String companyName;
    
    private Role role;
    
    @Pattern(regexp = "^(active|inactive)$", message = "Status must be either 'active' or 'inactive'")
    private String status;
    
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;
}

