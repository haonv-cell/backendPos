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
public class BillerDTO {
      private Integer id;
    private String code;
    private String name;
    private String email;
    private String phone;
    private String country;
    private String status;
    private String imageUrl;
    private LocalDateTime createdAt;
}
