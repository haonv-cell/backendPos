-- ============================================
-- SQL QUERIES CHO CUSTOMER LIST API
-- ============================================

-- 1. LẤY TẤT CẢ CUSTOMERS VỚI PAGINATION
-- Tương đương: findByRole(Role.CUSTOMER, pageable)
-- API: GET /api/customers?page=0&size=10

SELECT 
    id,
    code,
    name,
    email,
    phone,
    country,
    status,
    image_url,
    created_at
FROM users
WHERE role = 'customer'
ORDER BY created_at DESC
LIMIT 10 OFFSET 0;

-- Đếm tổng số customers (để tính totalPages)
SELECT COUNT(*) 
FROM users 
WHERE role = 'customer';


-- ============================================
-- 2. LỌC CUSTOMERS THEO STATUS
-- Tương đương: findByRoleAndStatus(Role.CUSTOMER, "active", pageable)
-- API: GET /api/customers?status=active&page=0&size=10

SELECT 
    id,
    code,
    name,
    email,
    phone,
    country,
    status,
    image_url,
    created_at
FROM users
WHERE role = 'customer'
  AND status = 'active'
ORDER BY created_at DESC
LIMIT 10 OFFSET 0;

-- Đếm tổng số customers active
SELECT COUNT(*) 
FROM users 
WHERE role = 'customer' 
  AND status = 'active';


-- ============================================
-- 3. TÌM KIẾM CUSTOMERS (SEARCH)
-- Tương đương: searchCustomers(Role.CUSTOMER, "Carl", pageable)
-- API: GET /api/customers?search=Carl&page=0&size=10

SELECT
    id,
    code,
    name,
    email,
    phone,
    country,
    status,
    image_url,
    created_at
FROM users
WHERE role = 'customer'
  AND (
    LOWER(name) LIKE LOWER('%Carl%') OR
    LOWER(email) LIKE LOWER('%Carl%') OR
    LOWER(code) LIKE LOWER('%Carl%') OR
    LOWER(phone) LIKE LOWER('%Carl%') OR
    LOWER(country) LIKE LOWER('%Carl%')
  )
ORDER BY created_at DESC
LIMIT 10 OFFSET 0;

-- Đếm kết quả tìm kiếm
SELECT COUNT(*)
FROM users
WHERE role = 'customer'
  AND (
    LOWER(name) LIKE LOWER('%Carl%') OR
    LOWER(email) LIKE LOWER('%Carl%') OR
    LOWER(code) LIKE LOWER('%Carl%') OR
    LOWER(phone) LIKE LOWER('%Carl%') OR
    LOWER(country) LIKE LOWER('%Carl%')
  );


-- ============================================
-- 4. TÌM KIẾM + LỌC THEO STATUS
-- Tương đương: searchCustomersByStatus(Role.CUSTOMER, "active", "Robert", pageable)
-- API: GET /api/customers?search=Robert&status=active&page=0&size=10

SELECT
    id,
    code,
    name,
    email,
    phone,
    country,
    status,
    image_url,
    created_at
FROM users
WHERE role = 'customer'
  AND status = 'active'
  AND (
    LOWER(name) LIKE LOWER('%Robert%') OR
    LOWER(email) LIKE LOWER('%Robert%') OR
    LOWER(code) LIKE LOWER('%Robert%') OR
    LOWER(phone) LIKE LOWER('%Robert%') OR
    LOWER(country) LIKE LOWER('%Robert%')
  )
ORDER BY created_at DESC
LIMIT 10 OFFSET 0;

-- Đếm kết quả
SELECT COUNT(*)
FROM users
WHERE role = 'customer'
  AND status = 'active'
  AND (
    LOWER(name) LIKE LOWER('%Robert%') OR
    LOWER(email) LIKE LOWER('%Robert%') OR
    LOWER(code) LIKE LOWER('%Robert%') OR
    LOWER(phone) LIKE LOWER('%Robert%') OR
    LOWER(country) LIKE LOWER('%Robert%')
  );


-- ============================================
-- 5. SẮP XẾP THEO TÊN (A-Z)
-- API: GET /api/customers?sortBy=name&sortDir=asc&page=0&size=10

SELECT 
    id,
    code,
    name,
    email,
    phone,
    country,
    status,
    image_url,
    created_at
FROM users
WHERE role = 'customer'
ORDER BY name ASC
LIMIT 10 OFFSET 0;


-- ============================================
-- 6. SẮP XẾP THEO EMAIL (Z-A)
-- API: GET /api/customers?sortBy=email&sortDir=desc&page=0&size=10

SELECT 
    id,
    code,
    name,
    email,
    phone,
    country,
    status,
    image_url,
    created_at
FROM users
WHERE role = 'customer'
ORDER BY email DESC
LIMIT 10 OFFSET 0;


-- ============================================
-- 7. TRANG THỨ 2 (PAGE 1, vì page bắt đầu từ 0)
-- API: GET /api/customers?page=1&size=10

SELECT 
    id,
    code,
    name,
    email,
    phone,
    country,
    status,
    image_url,
    created_at
FROM users
WHERE role = 'customer'
ORDER BY created_at DESC
LIMIT 10 OFFSET 10;  -- OFFSET = page * size = 1 * 10 = 10


-- ============================================
-- 8. TRANG THỨ 3 VỚI 20 ITEMS MỖI TRANG
-- API: GET /api/customers?page=2&size=20

SELECT 
    id,
    code,
    name,
    email,
    phone,
    country,
    status,
    image_url,
    created_at
FROM users
WHERE role = 'customer'
ORDER BY created_at DESC
LIMIT 20 OFFSET 40;  -- OFFSET = page * size = 2 * 20 = 40


-- ============================================
-- 9. TÌM KIẾM THEO QUỐC GIA
-- API: GET /api/customers?search=Germany&page=0&size=10

SELECT
    id,
    code,
    name,
    email,
    phone,
    country,
    status,
    image_url,
    created_at
FROM users
WHERE role = 'customer'
  AND (
    LOWER(name) LIKE LOWER('%Germany%') OR
    LOWER(email) LIKE LOWER('%Germany%') OR
    LOWER(code) LIKE LOWER('%Germany%') OR
    LOWER(phone) LIKE LOWER('%Germany%') OR
    LOWER(country) LIKE LOWER('%Germany%')
  )
ORDER BY created_at DESC
LIMIT 10 OFFSET 0;


-- ============================================
-- 10. TÌM KIẾM THEO SỐ ĐIỆN THOẠI
-- API: GET /api/customers?search=+1216&page=0&size=10

SELECT
    id,
    code,
    name,
    email,
    phone,
    country,
    status,
    image_url,
    created_at
FROM users
WHERE role = 'customer'
  AND (
    LOWER(name) LIKE LOWER('%+1216%') OR
    LOWER(email) LIKE LOWER('%+1216%') OR
    LOWER(code) LIKE LOWER('%+1216%') OR
    LOWER(phone) LIKE LOWER('%+1216%') OR
    LOWER(country) LIKE LOWER('%+1216%')
  )
ORDER BY created_at DESC
LIMIT 10 OFFSET 0;


-- ============================================
-- CÔNG THỨC TÍNH PAGINATION
-- ============================================
-- LIMIT = pageSize (số items mỗi trang)
-- OFFSET = page * pageSize
-- 
-- Ví dụ:
-- - Page 0, Size 10: LIMIT 10 OFFSET 0
-- - Page 1, Size 10: LIMIT 10 OFFSET 10
-- - Page 2, Size 10: LIMIT 10 OFFSET 20
-- - Page 0, Size 20: LIMIT 20 OFFSET 0
-- - Page 3, Size 15: LIMIT 15 OFFSET 45


-- ============================================
-- THỐNG KÊ CUSTOMERS
-- ============================================

-- Tổng số customers
SELECT COUNT(*) as total_customers
FROM users
WHERE role = 'customer';

-- Số customers theo status
SELECT 
    status,
    COUNT(*) as count
FROM users
WHERE role = 'customer'
GROUP BY status;

-- Số customers theo quốc gia
SELECT 
    country,
    COUNT(*) as count
FROM users
WHERE role = 'customer'
GROUP BY country
ORDER BY count DESC;

-- Top 10 customers mới nhất
SELECT 
    code,
    name,
    email,
    country,
    created_at
FROM users
WHERE role = 'customer'
ORDER BY created_at DESC
LIMIT 10;

