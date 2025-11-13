# 📦 Mối quan hệ giữa Warehouse, Store và Stock

## 🏗️ Cấu trúc 3 bảng chính

```
┌─────────────────┐
│   WAREHOUSES    │ (Kho tổng)
│  (Kho Hà Nội)   │
└────────┬────────┘
         │
         │ warehouse_id (FK)
         │
         ├──────────────────────────────┐
         │                              │
         ▼                              ▼
┌─────────────────┐            ┌─────────────────┐
│     STORES      │            │  MANAGE_STOCK   │
│ (Cửa hàng)     │            │ (Chuyển kho)    │
│                 │◄───────────┤                 │
│ - Electro Mart  │ store_id   │ - Ghi nhận      │
│ - Quantum Shop  │   (FK)     │   chuyển hàng   │
└─────────────────┘            └─────────────────┘
```

---

## 📊 Chi tiết các bảng

### 1️⃣ **WAREHOUSES** (Kho tổng)

**Vai trò:** Kho trung tâm nhập hàng từ nhà cung cấp

**Cấu trúc:**
```sql
CREATE TABLE warehouses (
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    contact_person  VARCHAR(100),
    phone           VARCHAR(20),
    user_id         INTEGER REFERENCES users(id),
    total_products  INTEGER DEFAULT 0,
    stock           INTEGER DEFAULT 0,
    status          VARCHAR(20) DEFAULT 'active'
);
```

**Ví dụ dữ liệu:**
```sql
INSERT INTO warehouses (id, name, contact_person, phone, user_id, total_products, stock) 
VALUES (1, 'Kho Hà Nội', 'Nguyễn Văn A', '0241234567', 5, 500, 10000);
```

**Giải thích:**
- `total_products`: 500 loại sản phẩm khác nhau
- `stock`: Tổng 10,000 sản phẩm trong kho
- `user_id`: Người quản lý kho (User #5)

---

### 2️⃣ **STORES** (Cửa hàng)

**Vai trò:** Chi nhánh bán lẻ, nhận hàng từ warehouse

**Cấu trúc:**
```sql
CREATE TABLE stores (
    id              SERIAL PRIMARY KEY,
    code            VARCHAR(20) UNIQUE,
    name            VARCHAR(150) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    phone           VARCHAR(20),
    address         TEXT,
    city            VARCHAR(100),
    country         VARCHAR(100),
    warehouse_id    INTEGER REFERENCES warehouses(id),  -- ⚠️ Liên kết với kho
    user_id         INTEGER REFERENCES users(id),       -- ⚠️ Chủ cửa hàng (STORE_OWNER)
    total_products  INTEGER DEFAULT 0,
    total_stock     INTEGER DEFAULT 0,
    status          VARCHAR(20) DEFAULT 'active'
);
```

**Ví dụ dữ liệu:**
```sql
INSERT INTO stores (id, code, name, email, warehouse_id, user_id, total_products, total_stock) 
VALUES 
(1, 'ST001', 'Electro Mart - Cầu Giấy', 'caugiay@electromart.com', 1, 10, 50, 200),
(2, 'ST002', 'Quantum Gadgets - Đống Đa', 'dongda@quantum.com', 1, 11, 30, 150);
```

**Giải thích:**
- Store #1 (Electro Mart):
  - Nhận hàng từ `warehouse_id = 1` (Kho Hà Nội)
  - Quản lý bởi `user_id = 10` (User có role STORE_OWNER)
  - Có 50 loại sản phẩm, tổng 200 sản phẩm
  
- Store #2 (Quantum Gadgets):
  - Cùng nhận từ Kho Hà Nội
  - Quản lý bởi User #11
  - Có 30 loại sản phẩm, tổng 150 sản phẩm

---

### 3️⃣ **MANAGE_STOCK** (Quản lý chuyển kho)

**Vai trò:** Ghi nhận lịch sử chuyển hàng từ Warehouse → Store

**Cấu trúc:**
```sql
CREATE TABLE manage_stock (
    id              SERIAL PRIMARY KEY,
    warehouse_id    INTEGER REFERENCES warehouses(id),  -- Kho nguồn
    store_id        INTEGER REFERENCES stores(id),      -- Cửa hàng đích
    product_id      INTEGER REFERENCES products(id),    -- Sản phẩm
    quantity        INTEGER DEFAULT 0,                  -- Số lượng chuyển
    date            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    person_id       INTEGER REFERENCES users(id)        -- Người thực hiện
);
```

**Ví dụ dữ liệu:**
```sql
-- Chuyển 20 iPhone từ Kho Hà Nội → Electro Mart
INSERT INTO manage_stock (warehouse_id, store_id, product_id, quantity, person_id) 
VALUES (1, 1, 100, 20, 5);

-- Chuyển 15 iPhone từ Kho Hà Nội → Quantum Gadgets
INSERT INTO manage_stock (warehouse_id, store_id, product_id, quantity, person_id) 
VALUES (1, 2, 100, 15, 5);
```

**Giải thích:**
- Giao dịch 1: Chuyển 20 iPhone (product_id=100) từ Warehouse #1 → Store #1
- Giao dịch 2: Chuyển 15 iPhone từ Warehouse #1 → Store #2
- Người thực hiện: User #5 (quản lý kho)

---

## 🔄 Luồng nghiệp vụ thực tế

### **Bước 1: Nhập hàng vào Warehouse**

```sql
-- Nhà cung cấp giao 100 iPhone cho Kho Hà Nội
UPDATE warehouses 
SET stock = stock + 100 
WHERE id = 1;

-- Kết quả: Warehouse stock = 10,100
```

---

### **Bước 2: Chuyển hàng từ Warehouse → Store**

```sql
-- 1. Ghi nhận chuyển kho
INSERT INTO manage_stock (warehouse_id, store_id, product_id, quantity, person_id) 
VALUES (1, 1, 100, 20, 5);

-- 2. Giảm stock ở Warehouse
UPDATE warehouses 
SET stock = stock - 20 
WHERE id = 1;

-- 3. Tăng stock ở Store
UPDATE stores 
SET total_stock = total_stock + 20 
WHERE id = 1;

-- Kết quả:
-- - Warehouse stock: 10,080 (-20)
-- - Store #1 stock: 220 (+20)
-- - manage_stock: Có 1 record ghi nhận
```

---

### **Bước 3: Bán hàng tại Store**

```sql
-- Khách hàng mua 5 iPhone tại Electro Mart
UPDATE stores 
SET total_stock = total_stock - 5 
WHERE id = 1;

-- Kết quả: Store #1 stock = 215
```

---

## 📈 Truy vấn thống kê

### **1. Xem lịch sử chuyển kho của 1 Store**

```sql
SELECT 
    ms.id,
    w.name AS warehouse_name,
    s.name AS store_name,
    p.name AS product_name,
    ms.quantity,
    ms.date,
    u.name AS person_name
FROM manage_stock ms
JOIN warehouses w ON ms.warehouse_id = w.id
JOIN stores s ON ms.store_id = s.id
JOIN products p ON ms.product_id = p.id
JOIN users u ON ms.person_id = u.id
WHERE ms.store_id = 1
ORDER BY ms.date DESC;
```

**Kết quả:**
```
id | warehouse_name | store_name        | product_name | quantity | date       | person_name
---|----------------|-------------------|--------------|----------|------------|-------------
1  | Kho Hà Nội     | Electro Mart      | iPhone 15    | 20       | 2024-11-10 | Nguyễn Văn A
```

---

### **2. Tổng hợp số lượng hàng đã chuyển cho từng Store**

```sql
SELECT 
    s.name AS store_name,
    COUNT(DISTINCT ms.product_id) AS total_products_received,
    SUM(ms.quantity) AS total_quantity_received
FROM stores s
LEFT JOIN manage_stock ms ON s.id = ms.store_id
GROUP BY s.id, s.name;
```

**Kết quả:**
```
store_name              | total_products_received | total_quantity_received
------------------------|-------------------------|------------------------
Electro Mart - Cầu Giấy | 10                      | 200
Quantum Gadgets         | 8                       | 150
```

---

### **3. Kiểm tra tồn kho hiện tại**

```sql
SELECT 
    'Warehouse' AS type,
    w.name,
    w.stock AS current_stock
FROM warehouses w
WHERE w.id = 1

UNION ALL

SELECT 
    'Store' AS type,
    s.name,
    s.total_stock AS current_stock
FROM stores s
WHERE s.warehouse_id = 1;
```

**Kết quả:**
```
type      | name                    | current_stock
----------|-------------------------|---------------
Warehouse | Kho Hà Nội              | 10,080
Store     | Electro Mart - Cầu Giấy | 215
Store     | Quantum Gadgets         | 150
```

---

## ✅ Tóm tắt mối quan hệ

| Bảng | Vai trò | Liên kết |
|------|---------|----------|
| **warehouses** | Kho tổng, nhập hàng từ supplier | - |
| **stores** | Chi nhánh bán lẻ | `warehouse_id` → warehouses(id)<br>`user_id` → users(id) |
| **manage_stock** | Ghi nhận chuyển kho | `warehouse_id` → warehouses(id)<br>`store_id` → stores(id)<br>`product_id` → products(id)<br>`person_id` → users(id) |

**Công thức:**
```
Warehouse Stock = Tổng nhập - Tổng chuyển cho Stores
Store Stock = Tổng nhận từ Warehouse - Tổng bán cho Customers
```

**Luồng dữ liệu:**
```
Supplier → Warehouse → manage_stock → Store → Customer
```

