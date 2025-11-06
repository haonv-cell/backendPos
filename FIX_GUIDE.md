# Hướng dẫn Fix Lỗi

## Lỗi đã gặp và cách giải quyết

### 1. Lỗi Flyway Migration
**Lỗi:**
```
FlywayException: Found non-empty schema(s) "public" but no schema history table
```

**Nguyên nhân:** Flyway cố gắng migrate database nhưng phát hiện schema đã có dữ liệu.

**Giải pháp:**
- Đã disable Flyway trong `application.yml`:
```yaml
spring:
  flyway:
    enabled: false
```
- Đã xóa Flyway dependencies khỏi `pom.xml`

### 2. Lỗi Database Constraint (role và provider)
**Lỗi:**
```
ERROR: new row for relation "users" violates check constraint "users_role_check"
ERROR: new row for relation "users" violates check constraint "users_provider_check"
```

**Nguyên nhân:** 
- Database constraint yêu cầu giá trị lowercase ('admin', 'customer', 'local', etc.)
- Code Java sử dụng Enum với giá trị UPPERCASE (ADMIN, CUSTOMER, LOCAL, etc.)
- JPA AttributeConverter không hoạt động đúng

**Giải pháp:**
Chạy script SQL để sửa constraints:

```bash
# Nếu có psql trong PATH:
psql -U postgres -d pos -f fix-constraints.sql

# Hoặc kết nối vào PostgreSQL và chạy:
```

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

### 3. Cách chạy SQL script trên Windows

**Cách 1: Sử dụng pgAdmin**
1. Mở pgAdmin
2. Kết nối đến database `pos`
3. Mở Query Tool
4. Copy nội dung file `fix-constraints.sql` và paste vào
5. Click Execute (F5)

**Cách 2: Sử dụng psql (nếu có trong PATH)**
```bash
psql -U postgres -d pos -f fix-constraints.sql
```

**Cách 3: Sử dụng DBeaver/DataGrip**
1. Kết nối đến database `pos`
2. Mở SQL Editor
3. Copy nội dung file `fix-constraints.sql` và paste vào
4. Execute

## Sau khi fix

1. Restart ứng dụng:
```bash
# Kill process hiện tại (Ctrl+C)
# Hoặc trong PowerShell tìm và kill process:
Get-Process -Name java | Where-Object {$_.Path -like "*pos*"} | Stop-Process

# Chạy lại:
./mvnw spring-boot:run
```

2. Test register API:
```bash
powershell -File test-register2.ps1
```

## Kiểm tra database

```sql
-- Xem constraints hiện tại
SELECT conname, pg_get_constraintdef(oid) 
FROM pg_constraint 
WHERE conrelid = 'users'::regclass;

-- Xem dữ liệu users
SELECT id, code, name, email, role, provider, status 
FROM users;

-- Xóa test data
DELETE FROM users WHERE email IN ('test@example.com', 'john@example.com');
```

## Troubleshooting

### Port 8080 đã được sử dụng
```bash
# Tìm process đang dùng port 8080
netstat -ano | findstr :8080

# Kill process (thay PID bằng số từ lệnh trên)
taskkill /PID <PID> /F
```

### Database connection refused
- Kiểm tra PostgreSQL service đang chạy
- Kiểm tra thông tin kết nối trong `application.yml`
- Kiểm tra firewall

### Build failed
```bash
# Clean và rebuild
./mvnw clean install -U
```

## Files đã tạo để fix

1. `RoleConverter.java` - Convert Role enum sang lowercase
2. `AuthProviderConverter.java` - Convert AuthProvider enum sang lowercase  
3. `fix-constraints.sql` - Sửa database constraints
4. `clean-db.sql` - Xóa test data
5. `test-register2.ps1` - Test script mới

## Next Steps

Sau khi fix xong constraints, hệ thống sẽ hoạt động bình thường với:
- ✅ Register API
- ✅ Login API
- ✅ JWT Authentication
- ✅ Social Login (cần cấu hình OAuth2 credentials)

