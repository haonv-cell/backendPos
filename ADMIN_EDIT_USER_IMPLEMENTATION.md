# Admin Edit User - Implementation Summary

## 🎯 Mục tiêu

Triển khai API cho phép Admin edit thông tin user với kiểm soát nghiêm ngặt các field được phép thay đổi, đảm bảo tính toàn vẹn dữ liệu và bảo mật.

---

## 📦 Files đã tạo/cập nhật

### 1. DTO Layer
- ✅ **`UpdateUserRequest.java`** - Request DTO với validation

### 2. Service Layer
- ✅ **`UserService.java`** - Thêm method `updateUser()` với validation
- ✅ **`AuthService.java`** - Thêm validation khi login LOCAL
- ✅ **`CustomOAuth2UserService.java`** - Thêm validation khi login OAuth

### 3. Controller Layer
- ✅ **`UserController.java`** - Thêm endpoint `PUT /api/users/{id}`

### 4. Documentation & Testing
- ✅ **`ADMIN_UPDATE_USER_API.md`** - API documentation đầy đủ
- ✅ **`test-update-user.http`** - Test cases cho API
- ✅ **`add-provider-constraints.sql`** - SQL script để thêm constraints

---

## 🔐 Security Features

### 1. Authorization
```java
@PutMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<UserDTO> updateUser(...)
```
- Chỉ user có `ROLE_ADMIN` mới được phép gọi API
- JWT token bắt buộc

### 2. Field Protection

#### ✅ Fields được phép edit:
- `name` - Tên user
- `phone` - Số điện thoại
- `country` - Quốc gia
- `companyName` - Tên công ty
- `role` - Vai trò (ADMIN, BILLER, SUPPLIER, STORE_OWNER, CUSTOMER)
- `status` - Trạng thái (active, inactive)
- `imageUrl` - URL ảnh đại diện

#### ❌ Fields KHÔNG được phép edit:
- `id` - Primary key
- `code` - Unique identifier
- `email` - Cần API riêng với verification
- `password_hash` - Cần API riêng (change password)
- `provider` - Authentication provider (LOCAL, GOOGLE, FACEBOOK)
- `provider_id` - OAuth provider ID
- `email_verified` - Cần verification process
- `created_at` - Timestamp tạo
- `updated_at` - Auto-updated

### 3. Data Integrity Validation

```java
private void validateUserDataIntegrity(User user) {
    // Check OAuth account has provider_id
    if (user.getProvider() != AuthProvider.LOCAL && user.getProviderId() == null) {
        throw new BadRequestException("Invalid account state: OAuth account missing provider_id");
    }
    
    // Check LOCAL account has password
    if (user.getProvider() == AuthProvider.LOCAL && user.getPasswordHash() == null) {
        throw new BadRequestException("Invalid account state: Local account missing password");
    }
}
```

---

## 🛡️ Login Validation Enhancements

### 1. LOCAL Login Validation

**Trước khi update:**
```java
public AuthResponse login(LoginRequest loginRequest) {
    // Chỉ authenticate, không check provider
    Authentication authentication = authenticationManager.authenticate(...);
    ...
}
```

**Sau khi update:**
```java
public AuthResponse login(LoginRequest loginRequest) {
    // 1. Find user first
    User user = userRepository.findByEmail(loginRequest.getEmail())...
    
    // 2. Check provider = LOCAL
    if (user.getProvider() != AuthProvider.LOCAL) {
        throw new BadRequestException("Use " + user.getProvider() + " to login");
    }
    
    // 3. Check has password
    if (user.getPasswordHash() == null) {
        throw new BadRequestException("Invalid account state");
    }
    
    // 4. Then authenticate
    Authentication authentication = authenticationManager.authenticate(...);
    ...
}
```

### 2. OAuth Login Validation

**Trước khi update:**
```java
if (userOptional.isPresent()) {
    user = userOptional.get();
    if (!user.getProvider().equals(expectedProvider)) {
        throw new BadRequestException("Use your " + user.getProvider() + " account");
    }
    ...
}
```

**Sau khi update:**
```java
if (userOptional.isPresent()) {
    user = userOptional.get();
    
    // 1. Check provider matches
    if (!user.getProvider().equals(expectedProvider)) {
        throw new BadRequestException("Use your " + user.getProvider() + " account");
    }
    
    // 2. Check has provider_id
    if (user.getProviderId() == null) {
        throw new BadRequestException("Invalid account state: missing provider_id");
    }
    
    // 3. Check provider_id matches (prevent hijacking)
    if (!user.getProviderId().equals(oAuth2UserInfo.getId())) {
        throw new BadRequestException("Provider ID mismatch");
    }
    ...
}
```

---

## 📊 API Specification

### Endpoint
```
PUT /api/users/{id}
```

### Request
```json
{
  "name": "string (optional, max 100)",
  "phone": "string (optional, max 20)",
  "country": "string (optional, max 100)",
  "companyName": "string (optional, max 150)",
  "role": "ADMIN|BILLER|SUPPLIER|STORE_OWNER|CUSTOMER",
  "status": "active|inactive",
  "imageUrl": "string (optional, max 500)"
}
```

### Response (200 OK)
```json
{
  "id": 1,
  "code": "USR12345678",
  "name": "Updated Name",
  "email": "user@example.com",
  "phone": "0123456789",
  "country": "Vietnam",
  "companyName": "ABC Company",
  "role": "STORE_OWNER",
  "status": "active",
  "provider": "LOCAL",
  "imageUrl": "https://example.com/avatar.jpg",
  "emailVerified": true,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

---

## 🗄️ Database Constraints

### SQL Script: `add-provider-constraints.sql`

```sql
-- Constraint 1: OAuth accounts must have provider_id
ALTER TABLE users 
ADD CONSTRAINT check_oauth_provider_id 
CHECK (
    (LOWER(provider) = 'local' AND provider_id IS NULL) 
    OR 
    (LOWER(provider) IN ('google', 'facebook') AND provider_id IS NOT NULL)
);
```

**Lợi ích:**
- ✅ Database tự động reject dữ liệu không hợp lệ
- ✅ Bảo vệ tầng cuối cùng
- ✅ Đảm bảo tính toàn vẹn dữ liệu

---

## 🧪 Testing

### Test File: `test-update-user.http`

**Test Cases:**
1. ✅ Full update (all fields)
2. ✅ Partial update (only some fields)
3. ✅ Change role
4. ✅ Change status
5. ✅ Clear optional fields (set to null)
6. ❌ Invalid status (validation error)
7. ❌ Name too long (validation error)
8. ❌ Phone too long (validation error)
9. ❌ Unauthorized (no token)
10. ❌ Forbidden (non-admin token)
11. ❌ Not found (invalid user ID)
12. ✅ All valid roles

---

## 🚀 Deployment Steps

### 1. Build & Compile
```bash
mvn clean compile
```

### 2. Run Database Migration
```bash
psql -U postgres -d your_database -f add-provider-constraints.sql
```

### 3. Start Application
```bash
mvn spring-boot:run
```

### 4. Test API
- Sử dụng file `test-update-user.http`
- Hoặc Postman/Insomnia

---

## 📝 Usage Examples

### Example 1: Update Name and Phone
```http
PUT http://localhost:8080/api/users/1
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "name": "John Doe",
  "phone": "0123456789"
}
```

### Example 2: Promote User to Admin
```http
PUT http://localhost:8080/api/users/2
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "role": "ADMIN"
}
```

### Example 3: Deactivate User
```http
PUT http://localhost:8080/api/users/3
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "status": "inactive"
}
```

---

## ⚠️ Important Notes

### 1. Tại sao KHÔNG cho phép edit Provider?

**Vấn đề nếu cho phép:**

**Case 1: LOCAL → GOOGLE**
```
Before: provider=LOCAL, password_hash=xxx
After:  provider=GOOGLE, password_hash=xxx, provider_id=NULL

Problem:
- User vẫn login được bằng email/password (vì code không check provider)
- Nhưng dữ liệu không nhất quán (GOOGLE account không có provider_id)
```

**Case 2: GOOGLE → LOCAL**
```
Before: provider=GOOGLE, provider_id=123, password_hash=NULL
After:  provider=LOCAL, provider_id=123, password_hash=NULL

Problem:
- User KHÔNG login được bằng email/password (vì không có password)
- User mất quyền truy cập!
```

**Giải pháp:**
- ❌ KHÔNG cho phép edit `provider` và `provider_id`
- ✅ Validation khi login để reject trường hợp dữ liệu sai
- ✅ Database constraint để ngăn chặn dữ liệu không hợp lệ

### 2. Partial Update Support

API hỗ trợ partial update - chỉ cần gửi field muốn thay đổi:
```json
{
  "phone": "0123456789"
}
```
Các field khác giữ nguyên.

### 3. Null Values

Có thể set optional fields về `null`:
```json
{
  "phone": null,
  "country": null
}
```

---

## 🔗 Related Documentation

- **API Documentation**: `ADMIN_UPDATE_USER_API.md`
- **Test Cases**: `test-update-user.http`
- **Database Migration**: `add-provider-constraints.sql`

---

## ✅ Checklist

- [x] Tạo UpdateUserRequest DTO với validation
- [x] Implement updateUser() method trong UserService
- [x] Thêm endpoint PUT /api/users/{id} trong UserController
- [x] Thêm validation trong AuthService.login()
- [x] Thêm validation trong CustomOAuth2UserService
- [x] Tạo API documentation
- [x] Tạo test cases
- [x] Tạo SQL migration script
- [x] Kiểm tra không có lỗi compile

---

## 🎉 Summary

API Admin Edit User đã được triển khai với:
- ✅ Kiểm soát nghiêm ngặt các field được phép edit
- ✅ Validation đầy đủ cho data integrity
- ✅ Bảo vệ các field nhạy cảm (provider, password, email)
- ✅ Authorization chỉ cho ADMIN
- ✅ Database constraints để đảm bảo tính toàn vẹn
- ✅ Documentation và test cases đầy đủ

