# Test Case-Insensitive Search

## Vấn đề đã fix

**Trước đây**: Sử dụng `LOWER(name) LIKE LOWER('%search%')` - có thể không hoạt động đúng với một số collation

**Bây giờ**: Sử dụng `ILIKE` (PostgreSQL) - đảm bảo tìm kiếm không phân biệt hoa/thường

## Ví dụ

### Database có:
```
name = "Admin abc"
name = "ADMIN User"  
name = "admin test"
name = "Carl Evans"
```

### Tìm kiếm "admin" sẽ tìm thấy:
- ✅ "Admin abc"
- ✅ "ADMIN User"
- ✅ "admin test"

### Tìm kiếm "CARL" sẽ tìm thấy:
- ✅ "Carl Evans"

### Tìm kiếm "carl" sẽ tìm thấy:
- ✅ "Carl Evans"

## SQL Query đã cập nhật

```sql
-- Trước (có thể không hoạt động đúng)
SELECT * FROM users 
WHERE LOWER(name) LIKE LOWER('%admin%');

-- Sau (đảm bảo hoạt động đúng với PostgreSQL)
SELECT * FROM users 
WHERE name ILIKE '%admin%';
```

## Code Java đã cập nhật

### UserRepository.java

```java
// Sử dụng native query với ILIKE
@Query(value = "SELECT * FROM users u WHERE u.role = :role AND " +
       "(u.name ILIKE CONCAT('%', :search, '%') OR " +
       "u.email ILIKE CONCAT('%', :search, '%') OR " +
       "u.code ILIKE CONCAT('%', :search, '%') OR " +
       "u.phone ILIKE CONCAT('%', :search, '%') OR " +
       "u.country ILIKE CONCAT('%', :search, '%'))",
       nativeQuery = true)
Page<User> searchCustomers(@Param("role") String role, 
                           @Param("search") String search, 
                           Pageable pageable);
```

## Test Cases

### 1. Test tìm kiếm không phân biệt hoa/thường

```bash
# Tìm "admin" - phải tìm thấy "Admin abc"
GET /api/customers?search=admin
Authorization: Bearer <admin-token>

# Tìm "ADMIN" - phải tìm thấy "Admin abc"  
GET /api/customers?search=ADMIN
Authorization: Bearer <admin-token>

# Tìm "AdMiN" - phải tìm thấy "Admin abc"
GET /api/customers?search=AdMiN
Authorization: Bearer <admin-token>
```

### 2. Test tìm kiếm với các trường khác

```bash
# Tìm theo email không phân biệt hoa/thường
GET /api/customers?search=CARL@EXAMPLE.COM
# Phải tìm thấy: carl@example.com

# Tìm theo country
GET /api/customers?search=germany
# Phải tìm thấy: Germany, GERMANY, germany
```

### 3. Test tìm kiếm kết hợp với filter

```bash
# Tìm "admin" với status active
GET /api/customers?search=admin&status=active
Authorization: Bearer <admin-token>
```

## Lợi ích của ILIKE

1. **Case-insensitive**: Không phân biệt hoa/thường
2. **PostgreSQL native**: Tối ưu hóa cho PostgreSQL
3. **Đơn giản hơn**: Không cần LOWER() wrapper
4. **Hiệu suất tốt hơn**: PostgreSQL tối ưu hóa ILIKE

## Lưu ý

- `ILIKE` là tính năng của **PostgreSQL**
- Nếu dùng MySQL, cần dùng `LIKE` (MySQL mặc định case-insensitive)
- Nếu dùng Oracle, cần dùng `UPPER()` hoặc `LOWER()`

## Files đã cập nhật

1. ✅ `UserRepository.java` - Đổi sang native query với ILIKE
2. ✅ `CustomerService.java` - Truyền role dưới dạng String "customer"
3. ✅ `customer-queries.sql` - Cập nhật tất cả queries dùng ILIKE

