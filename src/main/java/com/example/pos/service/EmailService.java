package com.example.pos.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    public void sendPasswordResetOtp(String toEmail, String userName, String otp) {
        log.info("================================================================================");
        log.info("📧 SENDING PASSWORD RESET OTP EMAIL");
        log.info("================================================================================");
        log.info("To: {}", toEmail);
        log.info("Subject: Password Reset OTP");
        log.info("OTP: {}", otp);
        log.info("================================================================================");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset OTP - DreamsPOS");

            String htmlContent = buildEmailTemplate(userName, otp);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("✅ Email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("❌ Failed to send email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildEmailTemplate(String userName, String otp) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        background-color: #f5f5f5;
                    }
                    .container {
                        max-width: 600px;
                        margin: 40px auto;
                        background: white;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #FF9066 0%%, #FF6B35 100%%);
                        color: white;
                        padding: 30px 20px;
                        text-align: center;
                    }
                    .logo {
                        font-size: 28px;
                        font-weight: bold;
                        margin-bottom: 10px;
                    }
                    .logo-icon {
                        display: inline-block;
                        width: 40px;
                        height: 40px;
                        background: white;
                        border-radius: 50%%;
                        margin-right: 10px;
                        vertical-align: middle;
                    }
                    .content {
                        padding: 40px 30px;
                    }
                    .greeting {
                        font-size: 18px;
                        margin-bottom: 20px;
                        color: #333;
                    }
                    .message {
                        font-size: 14px;
                        color: #666;
                        margin-bottom: 30px;
                    }
                    .otp-section {
                        background: #FFF5F0;
                        border: 2px dashed #FF9066;
                        border-radius: 8px;
                        padding: 30px;
                        text-align: center;
                        margin: 30px 0;
                    }
                    .otp-label {
                        font-size: 14px;
                        color: #666;
                        margin-bottom: 15px;
                        text-transform: uppercase;
                        letter-spacing: 1px;
                    }
                    .otp-code {
                        font-size: 36px;
                        font-weight: bold;
                        color: #FF6B35;
                        letter-spacing: 8px;
                        font-family: 'Courier New', monospace;
                    }
                    .warning {
                        background: #FFF3CD;
                        border-left: 4px solid #FFC107;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .warning-text {
                        font-size: 13px;
                        color: #856404;
                        margin: 0;
                    }
                    .timer {
                        color: #FF6B35;
                        font-weight: bold;
                    }
                    .footer {
                        background: #f9f9f9;
                        text-align: center;
                        color: #999;
                        font-size: 12px;
                        padding: 20px;
                        border-top: 1px solid #eee;
                    }
                    .footer p {
                        margin: 5px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">
                            <span class="logo-icon">🎯</span>
                            DreamsPOS
                        </div>
                        <p style="margin: 0; font-size: 16px;">Password Reset Request</p>
                    </div>
                    <div class="content">
                        <div class="greeting">Hi <strong>%s</strong>,</div>
                        <div class="message">
                            You requested to reset your password. Please use the OTP code below to proceed with resetting your password.
                        </div>

                        <div class="otp-section">
                            <div class="otp-label">Your OTP Code</div>
                            <div class="otp-code">%s</div>
                        </div>

                        <div class="warning">
                            <p class="warning-text">
                                ⏰ This OTP will expire in <span class="timer">5 minutes</span>.
                            </p>
                        </div>

                        <div class="message">
                            If you didn't request this password reset, please ignore this email and your password will remain unchanged.
                        </div>
                    </div>
                    <div class="footer">
                        <p>This is an automated email. Please do not reply.</p>
                        <p>Copyrights © 2025 - DreamsPOS</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, otp);
    }
}

