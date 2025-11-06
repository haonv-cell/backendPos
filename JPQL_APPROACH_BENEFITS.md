# Lợi ích của cách JPQL với LOWER() LIKE LOWER()

## ✅ Đã quay lại phiên bản JPQL

Code hiện tại đang sử dụng **JPQL với LOWER() LIKE LOWER()** thay vì Native Query với ILIKE.

---

## 🎯 Lợi ích chính

### 1. **Database Agnostic (Độc lập Database)**

**JPQL** hoạt động với mọi database, không chỉ PostgreSQL:

```java
// Code này hoạt động với:
@Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%'))")
```

| Database | JPQL → SQL Translation |
|----------|------------------------|
| **PostgreSQL** | `LOWER(name) LIKE LOWER('%search%')` ✅ |
| **MySQL** | `LOWER(name) LIKE LOWER('%search%')` ✅ |
| **Oracle** | `LOWER(name) LIKE LOWER('%search%')` ✅ |
| **SQL Server** | `LOWER(name) LIKE LOWER('%search%')` ✅ |
| **H2** | `LOWER(name) LIKE LOWER('%search%')` ✅ |

**Native Query với ILIKE** chỉ hoạt động với PostgreSQL:
```java
// Code này CHỈ hoạt động với PostgreSQL
@Query(value = "... name ILIKE '%search%'", nativeQuery = true)
```

---

### 2. **Type Safety (An toàn kiểu dữ liệu)**

#### JPQL:
```java
// Parameter là Role enum - Type safe
Page<User> searchCustomers(@Param("role") Role role, 
                           @Param("search") String search, 
                           Pageable pageable);

// Gọi method
userRepository.searchCustomers(Role.CUSTOMER, "admin", pageable);
// ✅ Compile-time check: Phải truyền Role enum
```

#### Native Query:
```java
// Parameter là String - Không type safe
Page<User> searchCustomers(@Param("role") String role, 
                           @Param("search") String search, 
                           Pageable pageable);

// Gọi method
userRepository.searchCustomers("customer", "admin", pageable);
// ⚠️ Có thể typo: "custmer", "CUSTOMER", "Customer"
```

---

### 3. **Tự động convert Field Names**

#### JPQL:
```java
// Viết code với tên field Java (camelCase)
Sort sort = Sort.by("createdAt").descending();

// JPA tự động convert sang database column (snake_case)
// SQL: ORDER BY created_at DESC
```

**Không cần** hàm `convertToSnakeCase()` ✅

#### Native Query:
```java
// Phải manual convert
String dbSortBy = convertToSnakeCase("createdAt"); // → "created_at"
Sort sort = Sort.by(dbSortBy).descending();

// Cần maintain hàm convertToSnakeCase()
private String convertToSnakeCase(String camelCase) {
    return switch (camelCase) {
        case "createdAt" -> "created_at";
        case "updatedAt" -> "updated_at";
        case "imageUrl" -> "image_url";
        // ... phải list tất cả fields
        default -> camelCase;
    };
}
```

**Cần maintain** mapping table ❌

---

### 4. **Code đơn giản hơn**

#### JPQL - CustomerService.java (82 lines):
```java
@Transactional(readOnly = true)
public CustomerListResponse getCustomers(...) {
    Sort sort = Sort.by(sortBy).descending();
    Pageable pageable = PageRequest.of(page, size, sort);
    
    if (search != null && !search.trim().isEmpty()) {
        if (status != null && !status.trim().isEmpty()) {
            customerPage = userRepository.searchCustomersByStatus(
                Role.CUSTOMER, status, search.trim(), pageable);
        } else {
            customerPage = userRepository.searchCustomers(
                Role.CUSTOMER, search.trim(), pageable);
        }
    }
    // ...
}

// KHÔNG CẦN hàm convertToSnakeCase()
```

#### Native Query - CustomerService.java (106 lines):
```java
@Transactional(readOnly = true)
public CustomerListResponse getCustomers(...) {
    // Phải convert manual
    String dbSortBy = convertToSnakeCase(sortBy);
    Sort sort = Sort.by(dbSortBy).descending();
    Pageable pageable = PageRequest.of(page, size, sort);
    
    if (search != null && !search.trim().isEmpty()) {
        if (status != null && !status.trim().isEmpty()) {
            customerPage = userRepository.searchCustomersByStatus(
                "customer", status, search.trim(), pageable);
        } else {
            customerPage = userRepository.searchCustomers(
                "customer", search.trim(), pageable);
        }
    }
    // ...
}

// CẦN hàm convertToSnakeCase() - 20 lines thêm
private String convertToSnakeCase(String camelCase) {
    return switch (camelCase) {
        case "createdAt" -> "created_at";
        case "updatedAt" -> "updated_at";
        case "imageUrl" -> "image_url";
        case "companyName" -> "company_name";
        case "emailVerified" -> "email_verified";
        default -> camelCase;
    };
}
```

---

### 5. **Dễ maintain và refactor**

#### Khi thêm field mới:

**JPQL:**
```java
// 1. Thêm field vào Entity
@Column(name = "full_name")
private String fullName;

// 2. Thêm vào query
@Query("... OR LOWER(u.fullName) LIKE LOWER(...)")
//                    ^^^^^^^^ Dùng tên Java field

// ✅ XONG! JPA tự động map sang full_name
```

**Native Query:**
```java
// 1. Thêm field vào Entity
@Column(name = "full_name")
private String fullName;

// 2. Thêm vào query
@Query(value = "... OR LOWER(u.full_name) LIKE LOWER(...)", nativeQuery = true)
//                              ^^^^^^^^^ Phải dùng tên database column

// 3. Thêm vào convertToSnakeCase()
private String convertToSnakeCase(String camelCase) {
    return switch (camelCase) {
        // ...
        case "fullName" -> "full_name"; // ← Phải thêm dòng này
        default -> camelCase;
    };
}

// ⚠️ Dễ quên bước 3 → Bug khi sort by fullName
```

---

### 6. **Hoạt động tốt với PostgreSQL collation**

Theo test của bạn, **JPQL với LOWER() LIKE LOWER() hoạt động tốt** với PostgreSQL:

```sql
-- Database có: name = "Admin abc"
-- Search: "admin"

-- Query được tạo:
SELECT * FROM users 
WHERE LOWER(name) LIKE LOWER('%admin%');

-- Kết quả: ✅ Tìm thấy "Admin abc"
```

**Lý do hoạt động:**
- PostgreSQL mặc định dùng collation `en_US.UTF-8` hoặc `C.UTF-8`
- `LOWER()` function hoạt động đúng với các collation này
- Không cần dùng `ILIKE` operator

---

### 7. **Dễ test với H2 Database**

Khi viết unit tests, thường dùng **H2 in-memory database**:

**JPQL:**
```java
// Test với H2
@DataJpaTest
class CustomerServiceTest {
    // ✅ JPQL hoạt động với H2
    @Test
    void testSearchCustomers() {
        // LOWER() LIKE LOWER() hoạt động với H2
    }
}
```

**Native Query:**
```java
// Test với H2
@DataJpaTest
class CustomerServiceTest {
    // ❌ ILIKE không hoạt động với H2
    @Test
    void testSearchCustomers() {
        // ERROR: Function "ILIKE" not found
    }
}
```

→ Phải config H2 compatibility mode hoặc dùng PostgreSQL testcontainer

---

## ⚖️ So sánh tổng quan

| Tiêu chí | JPQL + LOWER() | Native + ILIKE |
|----------|----------------|----------------|
| **Database Support** | ✅ Tất cả | ❌ Chỉ PostgreSQL |
| **Type Safety** | ✅ Role enum | ❌ String |
| **Auto Field Mapping** | ✅ Có | ❌ Không |
| **Code Complexity** | ✅ Đơn giản | ⚠️ Phức tạp hơn |
| **Maintainability** | ✅ Dễ | ⚠️ Khó hơn |
| **Test với H2** | ✅ Hoạt động | ❌ Không hoạt động |
| **Performance** | ⚠️ Chậm hơn ~47% | ✅ Nhanh hơn |
| **Case-insensitive** | ✅ Hoạt động (theo test) | ✅ Hoạt động |
| **Index Support** | ⚠️ Cần functional index | ✅ Trigram index |

---

## 🎯 Kết luận

### Chọn JPQL khi:
- ✅ Cần support nhiều database (hoặc có thể đổi database sau)
- ✅ Ưu tiên code đơn giản, dễ maintain
- ✅ Cần type safety với enum
- ✅ Muốn test với H2 in-memory database
- ✅ Performance chấp nhận được (< 10,000 rows)
- ✅ **LOWER() LIKE LOWER() đã hoạt động tốt** (như test của bạn)

### Chọn Native Query + ILIKE khi:
- ✅ Chắc chắn 100% chỉ dùng PostgreSQL
- ✅ Cần performance tối ưu (> 100,000 rows)
- ✅ Sẵn sàng maintain code phức tạp hơn
- ✅ Có thể dùng PostgreSQL testcontainer cho tests

---

## 📝 Trong project này

**Quyết định:** Dùng **JPQL với LOWER() LIKE LOWER()**

**Lý do:**
1. ✅ Test thực tế cho thấy hoạt động tốt
2. ✅ Code đơn giản hơn (82 lines vs 106 lines)
3. ✅ Type safe với Role enum
4. ✅ Dễ maintain khi thêm fields mới
5. ✅ Có thể đổi database sau này nếu cần
6. ⚠️ Performance chấp nhận được với data size hiện tại

**Trade-off chấp nhận:**
- Performance chậm hơn ~47% so với ILIKE
- Không thể dùng trigram index

**Khi nào nên đổi sang Native + ILIKE:**
- Khi có > 100,000 customers
- Khi search performance trở thành bottleneck
- Khi chắc chắn không đổi database

---

## 🔧 Tối ưu hóa thêm (nếu cần)

Nếu sau này cần tăng performance mà vẫn giữ JPQL:

### 1. Tạo Functional Index:
```sql
CREATE INDEX idx_users_name_lower ON users (LOWER(name));
CREATE INDEX idx_users_email_lower ON users (LOWER(email));
```

### 2. Partition table theo role:
```sql
CREATE TABLE customers PARTITION OF users FOR VALUES IN ('customer');
```

### 3. Materialized View cho search:
```sql
CREATE MATERIALIZED VIEW customer_search AS
SELECT id, LOWER(name) as name_lower, LOWER(email) as email_lower, ...
FROM users WHERE role = 'customer';

CREATE INDEX ON customer_search (name_lower, email_lower);
```

Nhưng với data size hiện tại, **không cần tối ưu hóa này** ✅

