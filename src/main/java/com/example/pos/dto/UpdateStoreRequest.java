package com.example.pos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStoreRequest {

    @Size(max = 20, message = "Code must not exceed 20 characters")
    private String code;

    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    @Email(message = "Email should be valid")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    private Integer warehouseId;

    private Integer userId;
}

