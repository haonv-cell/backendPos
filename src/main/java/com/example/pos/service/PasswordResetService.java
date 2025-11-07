package com.example.pos.service;

import com.example.pos.entity.PasswordResetOtp;
import com.example.pos.entity.User;
import com.example.pos.exception.BadRequestException;
import com.example.pos.repository.PasswordResetOtpRepository;
import com.example.pos.repository.UserRepository;
import com.example.pos.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRATION_MINUTES = 5;
    private static final SecureRandom random = new SecureRandom();

    @Transactional
    public void sendPasswordResetOtp(String email) {
        log.info("Password reset OTP requested for email: {}", email);

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return;
        }

        User user = userOptional.get();

        otpRepository.deleteByUser(user);

        String otp = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);

        PasswordResetOtp resetOtp = PasswordResetOtp.builder()
                .user(user)
                .otp(otp)
                .expiresAt(expiresAt)
                .isUsed(false)
                .build();

        otpRepository.save(resetOtp);

        emailService.sendPasswordResetOtp(user.getEmail(), user.getName(), otp);

        log.info("✅ OTP created and sent successfully for user: {}", user.getEmail());
    }

    @Transactional
    public String verifyOtp(String email, String otp) {
        log.info("Verifying OTP for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid email or OTP"));

        PasswordResetOtp resetOtp = otpRepository.findValidOtp(user, otp, LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("Invalid or expired OTP"));

        if (resetOtp.getIsUsed()) {
            throw new BadRequestException("OTP has already been used");
        }

        if (resetOtp.isExpired()) {
            throw new BadRequestException("OTP has expired");
        }

        resetOtp.setIsUsed(true);
        otpRepository.save(resetOtp);

        String resetToken = jwtTokenProvider.generatePasswordResetToken(user.getId(), user.getEmail());

        log.info("✅ OTP verified successfully for user: {}", user.getEmail());

        return resetToken;
    }

    @Transactional
    public void resetPassword(String resetToken, String newPassword, String confirmPassword) {
        log.info("Resetting password with reset token");

        if (!newPassword.equals(confirmPassword)) {
            throw new BadRequestException("Passwords do not match");
        }

        Integer userId = jwtTokenProvider.validatePasswordResetToken(resetToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("✅ Password reset successfully for user: {}", user.getEmail());
    }

    private String generateOtp() {
        int otp = random.nextInt(900000) + 100000;
        return String.valueOf(otp);
    }

    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteExpiredOtps(LocalDateTime.now());
        log.info("Cleaned up expired OTPs");
    }
}

