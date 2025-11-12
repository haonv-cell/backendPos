-- Seed initial billers into users table
-- Lưu ý: password_hash là bcrypt giả định (không đăng nhập được). Dùng cho mục đích demo/listing.
-- Bạn có thể thay bằng hash thật sau này (BCryptPasswordEncoder).

INSERT INTO users (code, name, email, phone, country, company_name, password_hash, role, status, email_verified, image_url, provider, provider_id)
VALUES 
  ('BILL-0001', 'Nguyen Van A', 'biller.a@example.com', '0901000001', 'Vietnam', 'Dream POS', '$2a$10$abcdefghijklmnopqrstuvCdeFGHIJKLMNoPQRSTUVwxYZ12', 'biller', 'active', true, NULL, 'local', NULL),
  ('BILL-0002', 'Tran Thi B', 'biller.b@example.com', '0901000002', 'Vietnam', 'Dream POS', '$2a$10$abcdefghijklmnopqrstuvCdeFGHIJKLMNoPQRSTUVwxYZ12', 'biller', 'active', false, NULL, 'local', NULL),
  ('BILL-0003', 'Le Van C', 'biller.c@example.com', '0901000003', 'Vietnam', 'Dream POS', '$2a$10$abcdefghijklmnopqrstuvCdeFGHIJKLMNoPQRSTUVwxYZ12', 'biller', 'inactive', false, NULL, 'local', NULL),
  ('BILL-0004', 'Pham Thi D', 'biller.d@example.com', '0901000004', 'Vietnam', 'Dream POS', '$2a$10$abcdefghijklmnopqrstuvCdeFGHIJKLMNoPQRSTUVwxYZ12', 'biller', 'active', true, NULL, 'local', NULL),
  ('BILL-0005', 'Hoang Van E', 'biller.e@example.com', '0901000005', 'Vietnam', 'Dream POS', '$2a$10$abcdefghijklmnopqrstuvCdeFGHIJKLMNoPQRSTUVwxYZ12', 'biller', 'active', false, NULL, 'local', NULL);

-- Nếu cần reset dữ liệu seed biller:
-- DELETE FROM users WHERE role = 'biller' AND email LIKE 'biller.%@example.com';


