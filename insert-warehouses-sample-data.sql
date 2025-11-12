-- ============================================
-- INSERT SAMPLE DATA FOR WAREHOUSES TABLE
-- ============================================
-- Dữ liệu mẫu cho 10 warehouses từ màn hình

-- 1. Lavish Warehouse
INSERT INTO warehouses (name, contact_person, phone, total_products, stock, qty, created_on, status, user_id)
VALUES ('Lavish Warehouse', 'Chad Taylor', '+12498345785', 10, 600, 80, '2024-12-24', 'active', NULL);

-- 2. Quaint Warehouse
INSERT INTO warehouses (name, contact_person, phone, total_products, stock, qty, created_on, status, user_id)
VALUES ('Quaint Warehouse', 'Jenny Ellis', '+13178964582', 15, 300, 85, '2024-12-10', 'active', NULL);

-- 3. Traditional Warehouse
INSERT INTO warehouses (name, contact_person, phone, total_products, stock, qty, created_on, status, user_id)
VALUES ('Traditional Warehouse', 'Leon Baxter', '+12796183487', 12, 400, 70, '2024-11-27', 'active', NULL);

-- 4. Cool Warehouse
INSERT INTO warehouses (name, contact_person, phone, total_products, stock, qty, created_on, status, user_id)
VALUES ('Cool Warehouse', 'Karen Flores', '+17538647943', 20, 320, 65, '2024-11-18', 'active', NULL);

-- 5. Overflow Warehouse
INSERT INTO warehouses (name, contact_person, phone, total_products, stock, qty, created_on, status, user_id)
VALUES ('Overflow Warehouse', 'Michael Dawson', '+13798132475', 8, 170, 80, '2024-11-06', 'active', NULL);

-- 6. Nova Storage Hub
INSERT INTO warehouses (name, contact_person, phone, total_products, stock, qty, created_on, status, user_id)
VALUES ('Nova Storage Hub', 'Karen Galvan', '+17596341894', 13, 220, 75, '2024-10-25', 'active', NULL);

-- 7. Retail Supply Hub
INSERT INTO warehouses (name, contact_person, phone, total_products, stock, qty, created_on, status, user_id)
VALUES ('Retail Supply Hub', 'Thomas Ward', '+12973548678', 17, 310, 60, '2024-10-14', 'active', NULL);

-- 8. EdgeWare Solutions
INSERT INTO warehouses (name, contact_person, phone, total_products, stock, qty, created_on, status, user_id)
VALUES ('EdgeWare Solutions', 'Aliza Duncan', '+13147858357', 22, 450, 50, '2024-10-03', 'active', NULL);

-- 9. North Zone Warehouse
INSERT INTO warehouses (name, contact_person, phone, total_products, stock, qty, created_on, status, user_id)
VALUES ('North Zone Warehouse', 'James Higham', '+11978348626', 24, 270, 70, '2024-09-20', 'active', NULL);

-- 10. Fulfillment Hub
INSERT INTO warehouses (name, contact_person, phone, total_products, stock, qty, created_on, status, user_id)
VALUES ('Fulfillment Hub', 'Jada Robinson', '+12678934561', 14, 300, 45, '2024-09-10', 'active', NULL);


-- ============================================
-- VERIFY DATA
-- ============================================

-- Check inserted data
SELECT 
    id,
    name,
    contact_person,
    phone,
    total_products,
    stock,
    qty,
    created_on,
    status
FROM warehouses
ORDER BY created_on DESC;

-- Count total warehouses
SELECT COUNT(*) as total_warehouses FROM warehouses;

-- Count by status
SELECT status, COUNT(*) as count
FROM warehouses
GROUP BY status;

