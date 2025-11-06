# 🔧 FIX LỖI NGAY BÂY GIỜ

## Lỗi hiện tại:
```
ERROR: new row for relation "users" violates check constraint "users_provider_check"
```

## Nguyên nhân:
Database constraint yêu cầu giá trị lowercase nhưng code Java lưu UPPERCASE.

## ✅ GIẢI PHÁP - Chọn 1 trong 3 cách:

---

### CÁCH 1: Sử dụng pgAdmin (KHUYẾN NGHỊ)

1. **Mở pgAdmin**
2. **Kết nối đến PostgreSQL server**
3. **Chọn database `pos`**
4. **Click chuột phải vào `pos` → Query Tool**
5. **Copy và paste đoạn SQL sau vào Query Tool:**

```sql
-- Drop existing constraints
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_provider_check;

-- Add new constraints that accept both cases
ALTER TABLE users ADD CONSTRAINT users_role_check 
    CHECK (LOWER(role) IN ('admin', 'biller', 'supplier', 'store_owner', 'customer'));

ALTER TABLE users ADD CONSTRAINT users_provider_check 
    CHECK (LOWER(provider) IN ('local', 'google', 'facebook'));
```

6. **Click Execute (F5) hoặc nút ▶️**
7. **Kiểm tra kết quả - phải thấy "Query returned successfully"**

---

### CÁCH 2: Sử dụng DBeaver / DataGrip

1. **Mở DBeaver hoặc DataGrip**
2. **Kết nối đến database `pos`**
3. **Mở SQL Editor (Ctrl+Enter hoặc New SQL Script)**
4. **Copy và paste SQL từ file `fix-constraints.sql`**
5. **Execute (Ctrl+Enter)**

---

### CÁCH 3: Sử dụng psql command line

Nếu bạn biết đường dẫn đến psql.exe:

```powershell
# Thay đổi đường dẫn phù hợp với version PostgreSQL của bạn
& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -h localhost -U postgres -d pos -f fix-constraints.sql
```

Hoặc nếu psql đã có trong PATH:
```bash
psql -h localhost -U postgres -d pos -f fix-constraints.sql
```

---

## SAU KHI CHẠY SQL THÀNH CÔNG:

### 1. Restart ứng dụng Spring Boot

Nếu đang chạy, kill process:
```powershell
# Tìm process Java
Get-Process -Name java | Where-Object {$_.Path -like "*pos*"} | Stop-Process -Force
```

Hoặc đơn giản: **Ctrl+C** trong terminal đang chạy Spring Boot

### 2. Chạy lại ứng dụng:
```bash
./mvnw spring-boot:run
```

### 3. Test lại với Postman:

**POST** `http://localhost:8080/api/auth/register`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123",
    "phone": "0123456789",
    "country": "Vietnam",
    "companyName": "ABC Company",
    "role": "CUSTOMER"
}
```

### 4. Kết quả mong đợi:

✅ **Success Response:**
```json
{
    "success": true,
    "message": "User registered successfully",
    "data": {
        "user": {
            "id": 1,
            "code": "USR...",
            "name": "John Doe",
            "email": "john@example.com",
            "role": "CUSTOMER",
            "status": "ACTIVE"
        },
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
        "tokenType": "Bearer"
    }
}
```

---

## XÁC NHẬN FIX THÀNH CÔNG:

Chạy query này trong pgAdmin để kiểm tra constraints đã được update:

```sql
SELECT conname, pg_get_constraintdef(oid) 
FROM pg_constraint 
WHERE conrelid = 'users'::regclass 
  AND conname IN ('users_role_check', 'users_provider_check');
```

Kết quả phải hiển thị:
- `users_role_check` → `CHECK (lower(...) IN (...))`
- `users_provider_check` → `CHECK (lower(...) IN (...))`

---

## NẾU VẪN GẶP LỖI:

1. **Kiểm tra PostgreSQL đang chạy:**
   ```powershell
   Get-Service -Name postgresql*
   ```

2. **Kiểm tra kết nối database trong `application.yml`:**
   - URL: `jdbc:postgresql://localhost:5432/pos`
   - Username: `postgres`
   - Password: (kiểm tra đúng password)

3. **Xem log chi tiết trong terminal Spring Boot**

4. **Xóa test data cũ:**
   ```sql
   DELETE FROM users WHERE email IN ('test@example.com', 'john@example.com');
   ```

---

## TÓM TẮT:

1. ✅ Chạy SQL trong `fix-constraints.sql` bằng pgAdmin
2. ✅ Restart Spring Boot application
3. ✅ Test lại với Postman
4. ✅ Thành công! 🎉

