package com.example.pos.controller;

import com.example.pos.dto.*;
import com.example.pos.service.AuthService;
import com.example.pos.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse response = authService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.sendPasswordResetOtp(request.getEmail());
        return ResponseEntity.ok(MessageResponse.of(
            "If your email exists in our system, you will receive an OTP code shortly."
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        String resetToken = passwordResetService.verifyOtp(
            request.getEmail(),
            request.getOtp()
        );
        return ResponseEntity.ok(VerifyOtpResponse.of(
            "OTP verified successfully. Use the reset token to change your password.",
            resetToken
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(
            request.getResetToken(),
            request.getNewPassword(),
            request.getConfirmPassword()
        );
        return ResponseEntity.ok(MessageResponse.of("Password has been reset successfully."));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<MessageResponse> resendOtp(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.sendPasswordResetOtp(request.getEmail());
        return ResponseEntity.ok(MessageResponse.of(
            "If your email exists in our system, you will receive a new OTP code shortly."
        ));
    }
}

