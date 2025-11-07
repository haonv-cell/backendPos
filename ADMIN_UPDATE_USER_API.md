# 📘 Hướng dẫn Test API Admin Edit User

## 📋 Tổng quan

API cho phép Admin cập nhật thông tin user với kiểm soát nghiêm ngặt các field được phép edit.

**Endpoint:** `PUT /api/users/{id}`
**Authorization:** Chỉ `ROLE_ADMIN`
**URL Format:** ID của user trên URL (RESTful)

---

## � Bước 1: Chuẩn bị

### 1.1. Khởi động Backend

```bash
cd g:\OneDrive\Desktop\backendPos
mvn spring-boot:run
```

**Kiểm tra server đã chạy:**
```bash
curl http://localhost:8080/api/health
```

### 1.2. Tạo Admin Account (nếu chưa có)

**Option 1: Đăng ký qua API**
```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Admin User",
  "email": "admin@example.com",
  "password": "admin123",
  "phone": "0123456789",
  "role": "ADMIN"
}
```

**Option 2: Insert trực tiếp vào database**
```sql
INSERT INTO users (code, name, email, password_hash, role, status, provider, email_verified)
VALUES (
  'USRADMIN001',
  'Admin User',
  'admin@example.com',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: admin123
  'admin',
  'active',
  'local',
  true
);
```

---

## � Bước 2: Lấy Admin Token

### 2.1. Login để lấy JWT Token

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "admin123"
}
```

### 2.2. Response

```json
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "code": "USRADMIN001",
    "name": "Admin User",
    "email": "admin@example.com",
    "role": "ADMIN",
    "status": "active"
  }
}
```

**📝 Lưu lại `accessToken` để dùng cho các request tiếp theo!**

---

## 📊 Bước 3: Xem danh sách Users

### 3.1. Get All Users

```http
GET http://localhost:8080/api/users/all
```

### 3.2. Response

```json
[
  {
    "id": 1,
    "code": "USRADMIN001",
    "name": "Admin User",
    "email": "admin@example.com",
    "role": "ADMIN",
    "status": "active",
    "provider": "LOCAL"
  },
  {
    "id": 2,
    "code": "USR12345678",
    "name": "John Doe",
    "email": "john@example.com",
    "role": "CUSTOMER",
    "status": "active",
    "provider": "LOCAL"
  }
]
```

**📝 Chọn một user ID để test (ví dụ: ID = 2)**

---

## ✅ Bước 4: Test Cases - Successful Updates

### Test 4.1: Update Name và Phone

**Request:**
```http
PUT http://localhost:8080/api/users/2
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "name": "John Doe Updated",
  "phone": "0987654321"
}
```

**Expected Response: 200 OK**
```json
{
  "id": 2,
  "code": "USR12345678",
  "name": "John Doe Updated",
  "email": "john@example.com",
  "phone": "0987654321",
  "role": "CUSTOMER",
  "status": "active",
  "provider": "LOCAL",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**✅ Kiểm tra:**
- `name` đã đổi thành "John Doe Updated"
- `phone` đã đổi thành "0987654321"
- `updatedAt` đã thay đổi

---

### Test 4.2: Promote User to Admin

**Request:**
```http
PUT http://localhost:8080/api/users/2
Authorization: Bearer <your_admin_token>
Content-Type: application/json

{
  "role": "ADMIN"
}
```

**Expected Response: 200 OK**
```json
{
  "id": 2,
  "role": "ADMIN",
  ...
}
```

**✅ Kiểm tra:** `role` đã đổi thành "ADMIN"

---

### Test 4.3: Deactivate User

**Request:**
```http
PUT http://localhost:8080/api/users/2
Authorization: Bearer <your_admin_token>
Content-Type: application/json

{
  "status": "inactive"
}
```

**Expected Response: 200 OK**
```json
{
  "id": 2,
  "status": "inactive",
  ...
}
```

**✅ Kiểm tra:** `status` đã đổi thành "inactive"

---

### Test 4.4: Update All Allowed Fields

**Request:**
```http
PUT http://localhost:8080/api/users/2
Authorization: Bearer <your_admin_token>
Content-Type: application/json

{
  "name": "Jane Smith",
  "phone": "0111222333",
  "country": "Vietnam",
  "companyName": "ABC Corporation",
  "role": "STORE_OWNER",
  "status": "active",
  "imageUrl": "https://example.com/avatar.jpg"
}
```

**Expected Response: 200 OK**
```json
{
  "id": 2,
  "name": "Jane Smith",
  "phone": "0111222333",
  "country": "Vietnam",
  "companyName": "ABC Corporation",
  "role": "STORE_OWNER",
  "status": "active",
  "imageUrl": "https://example.com/avatar.jpg",
  ...
}
```

**✅ Kiểm tra:** Tất cả fields đã được update

---

### Test 4.5: Clear Optional Fields (Set to null)

**Request:**
```http
PUT http://localhost:8080/api/users/2
Authorization: Bearer <your_admin_token>
Content-Type: application/json

{
  "phone": null,
  "country": null,
  "companyName": null,
  "imageUrl": null
}
```

**Expected Response: 200 OK**
```json
{
  "id": 2,
  "phone": null,
  "country": null,
  "companyName": null,
  "imageUrl": null,
  ...
}
```

**✅ Kiểm tra:** Optional fields đã bị xóa (set về null)

---

## ❌ Bước 5: Test Cases - Validation Errors

### Test 5.1: Invalid Status Value

**Request:**
```http
PUT http://localhost:8080/api/users/2
Authorization: Bearer <your_admin_token>
Content-Type: application/json

{
  "status": "invalid_status"
}
```

**Expected Response: 400 Bad Request**
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "status": "Status must be either 'active' or 'inactive'"
  }
}
```

**✅ Kiểm tra:** Validation error cho status

---

### Test 5.2: Name Too Long

**Request:**
```http
PUT http://localhost:8080/api/users/2
Authorization: Bearer <your_admin_token>
Content-Type: application/json

{
  "name": "This is a very long name that exceeds the maximum allowed length of 100 characters and should fail validation test"
}
```

**Expected Response: 400 Bad Request**
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "name": "Name must not exceed 100 characters"
  }
}
```

**✅ Kiểm tra:** Validation error cho name length

---

### Test 5.3: Phone Too Long

**Request:**
```http
PUT http://localhost:8080/api/users/2
Authorization: Bearer <your_admin_token>
Content-Type: application/json

{
  "phone": "012345678901234567890"
}
```

**Expected Response: 400 Bad Request**
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "phone": "Phone must not exceed 20 characters"
  }
}
```

**✅ Kiểm tra:** Validation error cho phone length

---

## 🚫 Bước 6: Test Cases - Protected Fields

### Test 6.1: Try to Edit Email (Protected)

**Request:**
```http
PUT http://localhost:8080/api/users/2
Authorization: Bearer <your_admin_token>
Content-Type: application/json

{
  "name": "John Doe",
  "email": "newemail@example.com"
}
```

**Expected Response: 400 Bad Request**
```json
{
  "success": false,
  "message": "Field 'email' is not allowed to be updated",
  "field": "email",
  "hint": "Only these fields can be updated: name, phone, country, companyName, role, status, imageUrl"
}
```

**✅ Kiểm tra:** Email không được phép edit

---

### Test 6.2: Try to Edit Provider (Protected)

**Request:**
```http
PUT http://localhost:8080/api/users/2
Authorization: Bearer <your_admin_token>
Content-Type: application/json

{
  "provider": "GOOGLE"
}
```

**Expected Response: 400 Bad Request**
```json
{
  "success": false,
  "message": "Field 'provider' is not allowed to be updated",
  "field": "provider",
  "hint": "Only these fields can be updated: name, phone, country, companyName, role, status, imageUrl"
}
```

**✅ Kiểm tra:** Provider không được phép edit

---

### Test 6.3: Try to Edit ID (Protected)

**Request:**
```http
PUT http://localhost:8080/api/users/2
Authorization: Bearer <your_admin_token>
Content-Type: application/json

{
  "id": 999,
  "name": "Hacker"
}
```

**Expected Response: 400 Bad Request**
```json
{
  "success": false,
  "message": "Field 'id' is not allowed to be updated",
  "field": "id",
  "hint": "Only these fields can be updated: name, phone, country, companyName, role, status, imageUrl"
}
```

**✅ Kiểm tra:** ID không được phép edit

---

### Test 6.4: Try to Edit Password Hash (Protected)

**Request:**
```http
PUT http://localhost:8080/api/users/2
Authorization: Bearer <your_admin_token>
Content-Type: application/json

{
  "password_hash": "hacked_password"
}
```

**Expected Response: 400 Bad Request**
```json
{
  "success": false,
  "message": "Field 'password_hash' is not allowed to be updated",
  "field": "password_hash",
  "hint": "Only these fields can be updated: name, phone, country, companyName, role, status, imageUrl"
}
```

**✅ Kiểm tra:** Password hash không được phép edit

---

## 🔒 Bước 7: Test Cases - Authorization

### Test 7.1: Update Without Token (Unauthorized)

**Request:**
```http
PUT http://localhost:8080/api/users/2
Content-Type: application/json

{
  "name": "Unauthorized Update"
}
```

**Expected Response: 401 Unauthorized**
```json
{
  "success": false,
  "message": "Full authentication is required to access this resource"
}
```

**✅ Kiểm tra:** Request bị reject vì không có token

---

### Test 7.2: Update with Non-Admin Token (Forbidden)

**Bước 1: Login as regular user**
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "user123"
}
```

**Bước 2: Try to update with user token**
```http
PUT http://localhost:8080/api/users/2
Authorization: Bearer <user_token_not_admin>
Content-Type: application/json

{
  "name": "Forbidden Update"
}
```

**Expected Response: 403 Forbidden**
```json
{
  "success": false,
  "message": "Access Denied"
}
```

**✅ Kiểm tra:** Chỉ ADMIN mới được phép update

---

## 🔍 Bước 8: Test Cases - Not Found

### Test 8.1: Update Non-Existent User

**Request:**
```http
PUT http://localhost:8080/api/users/99999
Authorization: Bearer <your_admin_token>
Content-Type: application/json

{
  "name": "Non Existent User"
}
```

**Expected Response: 404 Not Found**
```json
{
  "success": false,
  "message": "User not found with id: 99999"
}
```

**✅ Kiểm tra:** User không tồn tại

---

## 🧪 Bước 9: Test Cases - Data Integrity

### Test 9.1: Update User with Invalid Account State

**Scenario:** User có `provider=GOOGLE` nhưng `provider_id=NULL` (dữ liệu không hợp lệ)

**Setup (Insert invalid data):**
```sql
INSERT INTO users (code, name, email, provider, provider_id, role, status, email_verified)
VALUES (
  'USRINVALID01',
  'Invalid User',
  'invalid@example.com',
  'google',
  NULL,  -- Invalid: OAuth account without provider_id
  'customer',
  'active',
  true
);
```

**Request:**
```http
PUT http://localhost:8080/api/users/3
Authorization: Bearer <your_admin_token>
Content-Type: application/json

{
  "name": "Updated Name"
}
```

**Expected Response: 400 Bad Request**
```json
{
  "success": false,
  "message": "Invalid account state: OAuth account (provider=GOOGLE) is missing provider_id. Please contact support."
}
```

**✅ Kiểm tra:** API reject update nếu dữ liệu không hợp lệ

---

## 📊 Bước 10: Verify Updates

### 10.1. Get User by ID

```http
GET http://localhost:8080/api/users/2
Authorization: Bearer <your_admin_token>
```

**Response:**
```json
{
  "id": 2,
  "code": "USR12345678",
  "name": "Jane Smith",
  "email": "john@example.com",
  "phone": "0111222333",
  "country": "Vietnam",
  "companyName": "ABC Corporation",
  "role": "STORE_OWNER",
  "status": "active",
  "provider": "LOCAL",
  "imageUrl": "https://example.com/avatar.jpg",
  "emailVerified": false,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**✅ Kiểm tra:** Tất cả thay đổi đã được lưu

---

### 10.2. Get All Users

```http
GET http://localhost:8080/api/users/all
```

**✅ Kiểm tra:** User đã được update trong danh sách

---

## 📝 Bước 11: Test All Valid Roles

### Test 11.1: Role = CUSTOMER

```http
PUT http://localhost:8080/api/users/2
Authorization: Bearer <your_admin_token>
Content-Type: application/json

{
  "role": "CUSTOMER"
}
```

**Expected:** ✅ 200 OK

---

### Test 11.2: Role = BILLER

```http
PUT http://localhost:8080/api/users/2
Authorization: Bearer <your_admin_token>
Content-Type: application/json

{
  "role": "BILLER"
}
```

**Expected:** ✅ 200 OK

---

### Test 11.3: Role = SUPPLIER

```http
PUT http://localhost:8080/api/users/2
Authorization: Bearer <your_admin_token>
Content-Type: application/json

{
  "role": "SUPPLIER"
}
```

**Expected:** ✅ 200 OK

---

### Test 11.4: Role = STORE_OWNER

```http
PUT http://localhost:8080/api/users/2
Authorization: Bearer <your_admin_token>
Content-Type: application/json

{
  "role": "STORE_OWNER"
}
```

**Expected:** ✅ 200 OK

---

### Test 11.5: Role = ADMIN

```http
PUT http://localhost:8080/api/users/2
Authorization: Bearer <your_admin_token>
Content-Type: application/json

{
  "role": "ADMIN"
}
```

**Expected:** ✅ 200 OK

---

### Test 11.6: Invalid Role

```http
PUT http://localhost:8080/api/users/2
Authorization: Bearer <your_admin_token>
Content-Type: application/json

{
  "role": "SUPER_ADMIN"
}
```

**Expected Response: 400 Bad Request**
```json
{
  "success": false,
  "message": "Invalid request format: Cannot deserialize value of type `com.example.pos.entity.Role` from String \"SUPER_ADMIN\""
}
```

**✅ Kiểm tra:** Invalid role bị reject

---

## 📋 Summary - Test Checklist

### ✅ Successful Updates
- [x] Test 4.1: Update name và phone
- [x] Test 4.2: Promote user to admin
- [x] Test 4.3: Deactivate user
- [x] Test 4.4: Update all allowed fields
- [x] Test 4.5: Clear optional fields (set to null)

### ❌ Validation Errors
- [x] Test 5.1: Invalid status value
- [x] Test 5.2: Name too long
- [x] Test 5.3: Phone too long

### 🚫 Protected Fields
- [x] Test 6.1: Try to edit email
- [x] Test 6.2: Try to edit provider
- [x] Test 6.3: Try to edit id
- [x] Test 6.4: Try to edit password_hash

### 🔒 Authorization
- [x] Test 7.1: Update without token (401)
- [x] Test 7.2: Update with non-admin token (403)

### 🔍 Not Found
- [x] Test 8.1: Update non-existent user (404)

### 🧪 Data Integrity
- [x] Test 9.1: Update user with invalid account state (400)

### 📝 All Valid Roles
- [x] Test 11.1-11.5: All valid roles (CUSTOMER, BILLER, SUPPLIER, STORE_OWNER, ADMIN)
- [x] Test 11.6: Invalid role

---

## 🎯 Expected Results Summary

| Test Category | Total Tests | Expected Pass | Expected Fail |
|---------------|-------------|---------------|---------------|
| Successful Updates | 5 | 5 | 0 |
| Validation Errors | 3 | 0 | 3 (400) |
| Protected Fields | 4 | 0 | 4 (400) |
| Authorization | 2 | 0 | 2 (401, 403) |
| Not Found | 1 | 0 | 1 (404) |
| Data Integrity | 1 | 0 | 1 (400) |
| Valid Roles | 6 | 5 | 1 (400) |
| **TOTAL** | **22** | **10** | **12** |

---

## 🛠️ Tools for Testing

### Option 1: VS Code REST Client Extension

1. Install extension: **REST Client** by Huachao Mao
2. Open file: `test-update-user.http`
3. Click "Send Request" above each request

### Option 2: Postman

1. Import collection từ file `test-update-user.http`
2. Set environment variable `admin_token`
3. Run collection

### Option 3: cURL

```bash
# Get admin token
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"admin123"}' \
  | jq -r '.accessToken')

# Update user
curl -X PUT http://localhost:8080/api/users/2 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated Name","phone":"0123456789"}'
```

---

## 🐛 Troubleshooting

### Issue 1: 401 Unauthorized

**Cause:** Token expired hoặc invalid

**Solution:**
```bash
# Login lại để lấy token mới
POST http://localhost:8080/api/auth/login
```

---

### Issue 2: 403 Forbidden

**Cause:** User không có role ADMIN

**Solution:**
```sql
-- Update user role to ADMIN
UPDATE users SET role = 'admin' WHERE email = 'your@email.com';
```

---

### Issue 3: 500 Internal Server Error

**Cause:** Database connection issue hoặc bug trong code

**Solution:**
1. Check database đã chạy chưa
2. Check logs: `mvn spring-boot:run`
3. Check database connection trong `application.yml`

---

## 📚 Related Documentation

- **API Specification**: `ADMIN_UPDATE_USER_API.md`
- **Implementation Summary**: `ADMIN_EDIT_USER_IMPLEMENTATION.md`
- **Unknown Fields Handling**: `UNKNOWN_FIELDS_HANDLING.md`
- **Database Constraints**: `add-provider-constraints.sql`

---

## ✅ Success Criteria

API được coi là hoạt động đúng nếu:

1. ✅ **Successful updates**: Tất cả 5 test cases pass
2. ✅ **Validation**: Reject invalid data với 400 Bad Request
3. ✅ **Protected fields**: Reject với message rõ ràng
4. ✅ **Authorization**: Chỉ ADMIN được phép update
5. ✅ **Data integrity**: Validate trước khi update
6. ✅ **Error messages**: Rõ ràng, dễ hiểu

---

**🎉 Chúc bạn test thành công!**



