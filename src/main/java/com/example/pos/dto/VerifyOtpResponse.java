package com.example.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpResponse {
    
    private String message;
    private String resetToken;
    
    public static VerifyOtpResponse of(String message, String resetToken) {
        return new VerifyOtpResponse(message, resetToken);
    }
}

