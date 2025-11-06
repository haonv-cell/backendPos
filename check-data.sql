-- Kiểm tra dữ liệu trong database

-- 1. Kiểm tra tất cả users
SELECT id, code, name, email, role, provider, status, created_at 
FROM users 
ORDER BY id DESC;

-- 2. Đếm số lượng users
SELECT COUNT(*) as total_users FROM users;

-- 3. Kiểm tra user mới nhất
SELECT * FROM users ORDER BY created_at DESC LIMIT 1;

-- 4. Kiểm tra schema của bảng users
SELECT column_name, data_type, character_maximum_length, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'users'
ORDER BY ordinal_position;

