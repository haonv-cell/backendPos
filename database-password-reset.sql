-- ============================================
-- Bảng password_reset_otps
-- ============================================

CREATE TABLE IF NOT EXISTS password_reset_otps (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    otp VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_used BOOLEAN DEFAULT FALSE,
    
    CONSTRAINT fk_password_reset_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);

-- Indexes để tối ưu performance
CREATE INDEX IF NOT EXISTS idx_password_reset_user_id ON password_reset_otps(user_id);
CREATE INDEX IF NOT EXISTS idx_password_reset_expires_at ON password_reset_otps(expires_at);
CREATE INDEX IF NOT EXISTS idx_password_reset_otp ON password_reset_otps(otp);

-- Comment
COMMENT ON TABLE password_reset_otps IS 'Bảng lưu OTP để reset password';
COMMENT ON COLUMN password_reset_otps.otp IS 'Mã OTP 6 số';
COMMENT ON COLUMN password_reset_otps.expires_at IS 'Thời gian hết hạn (5 phút)';
COMMENT ON COLUMN password_reset_otps.is_used IS 'Đã sử dụng chưa (one-time use)';

