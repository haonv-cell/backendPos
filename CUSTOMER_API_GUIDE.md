# Customer List API - Hướng dẫn sử dụng

## 📋 Tổng quan

API để quản lý danh sách khách hàng (customers) với các tính năng:
- ✅ Chỉ **ADMIN** mới có quyền truy cập
- ✅ Phân trang (pagination)
- ✅ Tìm kiếm (search)
- ✅ Lọc theo trạng thái (status filter)
- ✅ Sắp xếp (sorting)

## 🔐 Yêu cầu

- Phải đăng nhập với tài khoản có **role = ADMIN**
- Cần có JWT token trong header `Authorization: Bearer <token>`

## 📡 API Endpoint

### GET `/api/customers`

Lấy danh sách khách hàng với pagination và filtering.

#### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | integer | No | 0 | Số trang (bắt đầu từ 0) |
| `size` | integer | No | 10 | Số lượng items mỗi trang |
| `search` | string | No | - | Tìm kiếm theo name, email, code, phone, country |
| `status` | string | No | - | Lọc theo trạng thái (active, inactive, etc.) |
| `sortBy` | string | No | createdAt | Trường để sắp xếp (name, email, createdAt, etc.) |
| `sortDir` | string | No | desc | Hướng sắp xếp: `asc` hoặc `desc` |

#### Response Format

```json
{
  "customers": [
    {
      "id": 1,
      "code": "CU001",
      "name": "Carl Evans",
      "email": "carlevans@example.com",
      "phone": "+12163547758",
      "country": "Germany",
      "status": "active",
      "imageUrl": "https://example.com/avatar.jpg",
      "createdAt": "2025-11-06T10:30:00"
    }
  ],
  "totalElements": 100,
  "totalPages": 10,
  "currentPage": 0,
  "pageSize": 10
}
```

## 📝 Ví dụ sử dụng

### 1. Lấy trang đầu tiên (10 customers)

```bash
GET http://localhost:8080/api/customers?page=0&size=10
Authorization: Bearer <your-admin-jwt-token>
```

### 2. Tìm kiếm khách hàng theo tên

```bash
GET http://localhost:8080/api/customers?search=Carl
Authorization: Bearer <your-admin-jwt-token>
```

### 3. Lọc khách hàng theo trạng thái

```bash
GET http://localhost:8080/api/customers?status=active
Authorization: Bearer <your-admin-jwt-token>
```

### 4. Tìm kiếm + Lọc theo trạng thái

```bash
GET http://localhost:8080/api/customers?search=Robert&status=active
Authorization: Bearer <your-admin-jwt-token>
```

### 5. Sắp xếp theo tên (A-Z)

```bash
GET http://localhost:8080/api/customers?sortBy=name&sortDir=asc
Authorization: Bearer <your-admin-jwt-token>
```

### 6. Lấy trang thứ 2 với 10 items

```bash
GET http://localhost:8080/api/customers?page=1&size=10
Authorization: Bearer <your-admin-jwt-token>
```

## 🔒 Bảo mật

### Chỉ ADMIN có quyền truy cập

API này được bảo vệ bởi `@PreAuthorize("hasRole('ADMIN')")`. Nếu user không phải ADMIN sẽ nhận lỗi:

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

### Cần JWT Token

Nếu không có token hoặc token không hợp lệ:

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required"
}
```

## 🧪 Testing

Sử dụng file `test-customer-api.http` để test API:

1. **Đăng ký tài khoản Admin**
2. **Login để lấy JWT token**
3. **Đăng ký một số customer để test**
4. **Gọi API với các tham số khác nhau**

## 📊 Database Query

API này query từ bảng `users` với điều kiện `role = 'customer'`:

```sql
SELECT * FROM users 
WHERE role = 'customer' 
  AND (name ILIKE '%search%' OR email ILIKE '%search%' OR ...)
  AND status = 'active'
ORDER BY created_at DESC
LIMIT 10 OFFSET 0;
```

## 🎯 Tính năng tìm kiếm

Search term sẽ tìm trong các trường:
- ✅ `name` - Tên khách hàng
- ✅ `email` - Email
- ✅ `code` - Mã khách hàng (CU001, CU002, ...)
- ✅ `phone` - Số điện thoại
- ✅ `country` - Quốc gia

Tìm kiếm **không phân biệt chữ hoa/thường** (case-insensitive).

## 📦 Files đã tạo

1. **DTOs**:
   - `CustomerDTO.java` - DTO cho thông tin customer
   - `CustomerListResponse.java` - Response wrapper với pagination info

2. **Service**:
   - `CustomerService.java` - Business logic cho customer operations

3. **Controller**:
   - `CustomerController.java` - REST endpoint với ADMIN authorization

4. **Repository**:
   - `UserRepository.java` - Đã thêm custom query methods

5. **Test**:
   - `test-customer-api.http` - HTTP requests để test API

## 🚀 Cách chạy

1. Start ứng dụng:
```bash
mvn spring-boot:run
```

2. Đăng ký tài khoản Admin (nếu chưa có)

3. Login để lấy JWT token

4. Sử dụng token để gọi API `/api/customers`

## ✅ Checklist

- [x] Chỉ ADMIN có quyền truy cập
- [x] Pagination (page, size)
- [x] Search (name, email, code, phone, country)
- [x] Filter by status
- [x] Sorting (sortBy, sortDir)
- [x] Response với pagination metadata
- [x] Case-insensitive search
- [x] Chỉ lấy users có role = CUSTOMER

