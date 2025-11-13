package com.example.pos.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBillerRequest {
    
    private String companyName;
    
    private String name;
    
    @Email(message = "Email should be valid")
    private String email;
    
    private String phone;
    
    private String country;
    
    private String status;
}

