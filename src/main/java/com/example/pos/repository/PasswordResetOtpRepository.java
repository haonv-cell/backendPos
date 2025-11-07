package com.example.pos.repository;

import com.example.pos.entity.PasswordResetOtp;
import com.example.pos.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Integer> {

    Optional<PasswordResetOtp> findByUserAndOtp(User user, String otp);

    @Query("SELECT o FROM PasswordResetOtp o WHERE o.user = ?1 AND o.otp = ?2 AND o.isUsed = false AND o.expiresAt > ?3")
    Optional<PasswordResetOtp> findValidOtp(User user, String otp, LocalDateTime now);

    @Modifying
    @Query("DELETE FROM PasswordResetOtp o WHERE o.user = ?1")
    void deleteByUser(User user);

    @Modifying
    @Query("DELETE FROM PasswordResetOtp o WHERE o.expiresAt < ?1")
    void deleteExpiredOtps(LocalDateTime now);
}

