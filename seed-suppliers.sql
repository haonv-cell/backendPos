-- Seed initial suppliers into suppliers table
-- Các bản ghi mẫu để test listing/search/filter

INSERT INTO suppliers (name, contact_name, contact_email, contact_phone, address, status)
VALUES
  ('Cty TNHH Minh Long', 'Nguyen Minh Long', 'minhlong@supplier.example.com', '0281234567', '12 Nguyen Trai, Q1, HCM', 'active'),
  ('Cong ty Phu An', 'Tran Phu An', 'phuan@supplier.example.com', '0282233445', '45 Le Loi, Q1, HCM', 'active'),
  ('Nha Phan Phoi Sao Mai', 'Le Sao Mai', 'saomai@supplier.example.com', '0243344556', '89 Kim Ma, Ba Dinh, HN', 'inactive'),
  ('Viet Goods Co.', 'Pham Viet', 'vietgoods@supplier.example.com', '0938123456', 'KCN Tan Binh, HCM', 'active'),
  ('An Khang Supply', 'Do An Khang', 'ankhang@supplier.example.com', '0909123123', 'Da Nang City', 'active');

-- Nếu cần rollback dữ liệu seed supplier:
-- DELETE FROM suppliers WHERE contact_email LIKE '%@supplier.example.com';


