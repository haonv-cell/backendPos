# Fix: Column Name Issue in Native Query

## Vấn đề

Khi sử dụng native query với Spring Data JPA, Spring tự động thêm ORDER BY từ Pageable vào query.
Nhưng nó sử dụng tên field Java (camelCase) thay vì tên column database (snake_case).

### Lỗi gặp phải:
```
ERROR: column u.createdat does not exist
Hint: Perhaps you meant to reference the column "u.created_at".
```

## Nguyên nhân

```java
// Code gốc
Sort sort = Sort.by("createdAt").descending();
Pageable pageable = PageRequest.of(0, 10, sort);
```

Spring Data JPA tạo query:
```sql
SELECT * FROM users u WHERE ... ORDER BY u.createdAt DESC
                                              ^^^^^^^^^ 
                                              Sai! Phải là created_at
```

## Giải pháp

### 1. Tạo hàm convert camelCase → snake_case

```java
private String convertToSnakeCase(String camelCase) {
    return switch (camelCase) {
        case "createdAt" -> "created_at";
        case "updatedAt" -> "updated_at";
        case "imageUrl" -> "image_url";
        case "companyName" -> "company_name";
        case "emailVerified" -> "email_verified";
        default -> camelCase; // name, email, phone, etc.
    };
}
```

### 2. Sử dụng tên column đúng khi tạo Sort

```java
// Convert trước khi tạo Sort
String dbSortBy = convertToSnakeCase(sortBy);

// Tạo Sort với tên column database
Sort sort = sortDir.equalsIgnoreCase("desc") 
    ? Sort.by(dbSortBy).descending() 
    : Sort.by(dbSortBy).ascending();

Pageable pageable = PageRequest.of(page, size, sort);
```

### 3. Query được tạo ra (đúng)

```sql
SELECT * FROM users u 
WHERE u.role = 'customer' 
  AND u.name ILIKE '%search%'
ORDER BY u.created_at DESC
             ^^^^^^^^^^^ Đúng!
LIMIT 10 OFFSET 0
```

## Mapping giữa Java field và Database column

| Java Field (camelCase) | Database Column (snake_case) |
|------------------------|------------------------------|
| `id` | `id` |
| `code` | `code` |
| `name` | `name` |
| `email` | `email` |
| `phone` | `phone` |
| `country` | `country` |
| `status` | `status` |
| `createdAt` | `created_at` ⚠️ |
| `updatedAt` | `updated_at` ⚠️ |
| `imageUrl` | `image_url` ⚠️ |
| `companyName` | `company_name` ⚠️ |
| `emailVerified` | `email_verified` ⚠️ |

⚠️ = Cần convert

## Test Cases

### 1. Sort by createdAt (default)
```bash
GET /api/customers?sortBy=createdAt&sortDir=desc
```
SQL: `ORDER BY u.created_at DESC` ✅

### 2. Sort by name
```bash
GET /api/customers?sortBy=name&sortDir=asc
```
SQL: `ORDER BY u.name ASC` ✅

### 3. Sort by email
```bash
GET /api/customers?sortBy=email&sortDir=desc
```
SQL: `ORDER BY u.email DESC` ✅

### 4. Sort by updatedAt
```bash
GET /api/customers?sortBy=updatedAt&sortDir=asc
```
SQL: `ORDER BY u.updated_at ASC` ✅

## Files đã sửa

1. ✅ `CustomerService.java`
   - Thêm method `convertToSnakeCase()`
   - Convert sortBy trước khi tạo Sort object

2. ✅ `UserRepository.java`
   - Giữ nguyên native query với ILIKE
   - Spring Data tự động thêm ORDER BY từ Pageable

## Lưu ý quan trọng

### Khi dùng Native Query:
- ❌ KHÔNG dùng tên field Java trong ORDER BY
- ✅ PHẢI dùng tên column database

### Khi dùng JPQL:
- ✅ Dùng tên field Java (Spring tự convert)
- Ví dụ: `ORDER BY u.createdAt` → `ORDER BY u.created_at`

### Trong project này:
- Native query → Cần convert manual
- JPQL query → Spring tự convert

## Kết quả

✅ Build thành công  
✅ Không còn lỗi "column does not exist"  
✅ Sorting hoạt động đúng với tất cả fields  
✅ Search case-insensitive với ILIKE  

