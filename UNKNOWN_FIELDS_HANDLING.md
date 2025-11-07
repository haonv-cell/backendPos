# Unknown Fields Handling - API Behavior

## 🎯 Mục tiêu

Đảm bảo API **REJECT** request nếu client gửi các field không được phép edit, thay vì im lặng ignore chúng.

---

## ⚙️ Configuration

### Spring Boot Jackson Configuration

**File:** `src/main/resources/application.yml`

```yaml
spring:
  jackson:
    deserialization:
      fail-on-unknown-properties: true
```

**Vai trò:**
- ✅ Reject request nếu JSON chứa field không có trong DTO
- ✅ Trả về error 400 Bad Request với message rõ ràng
- ✅ Bảo vệ API khỏi việc client gửi field không mong muốn

---

## 📊 Behavior Comparison

### ❌ **TRƯỚC KHI CẤU HÌNH (Default Behavior)**

**Request:**
```http
PUT /api/users/1
Content-Type: application/json

{
  "name": "New Name",
  "email": "newemail@example.com",  // ← Field không có trong UpdateUserRequest
  "provider": "GOOGLE"               // ← Field không có trong UpdateUserRequest
}
```

**Response: 200 OK** ⚠️
```json
{
  "id": 1,
  "name": "New Name",           // ✅ Updated
  "email": "old@example.com",   // ❌ Không đổi (bị ignore)
  "provider": "LOCAL"           // ❌ Không đổi (bị ignore)
}
```

**Vấn đề:**
- ❌ Client không biết `email` và `provider` bị ignore
- ❌ Không có error message
- ❌ Client có thể nghĩ update thành công
- ❌ Khó debug khi có typo trong field name

---

### ✅ **SAU KHI CẤU HÌNH (Recommended Behavior)**

**Request:**
```http
PUT /api/users/1
Content-Type: application/json

{
  "name": "New Name",
  "email": "newemail@example.com",  // ← Unknown field
  "provider": "GOOGLE"               // ← Unknown field
}
```

**Response: 400 Bad Request** ✅
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "JSON parse error: Unrecognized field \"email\" (class com.example.pos.dto.UpdateUserRequest), not marked as ignorable",
  "path": "/api/users/1"
}
```

**Lợi ích:**
- ✅ Client biết ngay field nào không hợp lệ
- ✅ Error message rõ ràng
- ✅ Dễ debug khi có typo
- ✅ Bảo mật tốt hơn (không cho phép gửi field không mong muốn)

---

## 🧪 Test Cases

### Test 1: Try to edit `email` (Protected field)

**Request:**
```http
PUT /api/users/1
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "name": "Updated Name",
  "email": "newemail@example.com"
}
```

**Expected Response: 400 Bad Request**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Unrecognized field \"email\""
}
```

---

### Test 2: Try to edit `provider` (Protected field)

**Request:**
```http
PUT /api/users/1
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "name": "Updated Name",
  "provider": "GOOGLE"
}
```

**Expected Response: 400 Bad Request**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Unrecognized field \"provider\""
}
```

---

### Test 3: Try to edit `id` (Immutable field)

**Request:**
```http
PUT /api/users/1
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "id": 999,
  "name": "Hacker"
}
```

**Expected Response: 400 Bad Request**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Unrecognized field \"id\""
}
```

---

### Test 4: Try to edit `password_hash` (Protected field)

**Request:**
```http
PUT /api/users/1
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "password_hash": "hacked_password",
  "name": "Hacker"
}
```

**Expected Response: 400 Bad Request**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Unrecognized field \"password_hash\""
}
```

---

### Test 5: Try to edit `provider_id` (Protected field)

**Request:**
```http
PUT /api/users/1
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "provider_id": "123456",
  "name": "Hacker"
}
```

**Expected Response: 400 Bad Request**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Unrecognized field \"provider_id\""
}
```

---

### Test 6: Typo in field name

**Request:**
```http
PUT /api/users/1
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "nmae": "Updated Name"  // ← Typo: "nmae" instead of "name"
}
```

**Expected Response: 400 Bad Request**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Unrecognized field \"nmae\""
}
```

**Lợi ích:** Client phát hiện ngay lỗi typo!

---

### Test 7: Valid request (Only allowed fields)

**Request:**
```http
PUT /api/users/1
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "name": "Updated Name",
  "phone": "0123456789",
  "role": "ADMIN"
}
```

**Expected Response: 200 OK** ✅
```json
{
  "id": 1,
  "name": "Updated Name",
  "phone": "0123456789",
  "role": "ADMIN",
  ...
}
```

---

## 🔒 Security Benefits

### 1. Prevent Field Injection Attacks

**Attack Scenario:**
```http
PUT /api/users/1
Content-Type: application/json

{
  "name": "John",
  "role": "ADMIN",           // ← Attacker tries to promote themselves
  "isAdmin": true,           // ← Unknown field
  "permissions": ["*"]       // ← Unknown field
}
```

**Without `fail-on-unknown-properties`:**
- ❌ `isAdmin` và `permissions` bị ignore
- ⚠️ Nhưng nếu code có bug, có thể bị exploit

**With `fail-on-unknown-properties`:**
- ✅ Request bị reject ngay
- ✅ Không có cơ hội exploit

---

### 2. Prevent Data Leakage

**Attack Scenario:**
```http
PUT /api/users/1
Content-Type: application/json

{
  "name": "John",
  "password_hash": "$2a$10$hacked..."  // ← Try to set password directly
}
```

**Without `fail-on-unknown-properties`:**
- ❌ Bị ignore, nhưng attacker có thể thử nhiều field khác nhau
- ⚠️ Có thể tìm ra field nào được accept

**With `fail-on-unknown-properties`:**
- ✅ Request bị reject ngay
- ✅ Attacker không thể brute-force field names

---

### 3. Prevent Provider Manipulation

**Attack Scenario:**
```http
PUT /api/users/1
Content-Type: application/json

{
  "name": "John",
  "provider": "LOCAL",       // ← Try to change OAuth to LOCAL
  "provider_id": null        // ← Remove provider_id
}
```

**Without `fail-on-unknown-properties`:**
- ❌ Bị ignore, nhưng nếu code có bug...
- ⚠️ Có thể tạo dữ liệu không nhất quán

**With `fail-on-unknown-properties`:**
- ✅ Request bị reject ngay
- ✅ Không thể thay đổi provider

---

## 📝 Implementation Details

### UpdateUserRequest DTO

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {
    
    // ✅ Allowed fields
    private String name;
    private String phone;
    private String country;
    private String companyName;
    private Role role;
    private String status;
    private String imageUrl;
    
    // ❌ NOT included (will be rejected if sent):
    // - id
    // - code
    // - email
    // - password_hash
    // - provider
    // - provider_id
    // - email_verified
    // - created_at
    // - updated_at
}
```

### Jackson Configuration

```yaml
spring:
  jackson:
    deserialization:
      fail-on-unknown-properties: true  # ← Key configuration
```

**Cách hoạt động:**
1. Client gửi JSON request
2. Spring Boot dùng Jackson để deserialize JSON → UpdateUserRequest
3. Jackson check: Tất cả field trong JSON có tồn tại trong UpdateUserRequest không?
4. Nếu có field không tồn tại → Throw `UnrecognizedPropertyException`
5. Spring Boot catch exception → Trả về 400 Bad Request

---

## 🎯 Best Practices

### 1. Always use DTOs for request/response

❌ **Bad:**
```java
@PutMapping("/{id}")
public User updateUser(@PathVariable Integer id, @RequestBody User user) {
    // Dangerous! Client có thể gửi bất kỳ field nào của User entity
}
```

✅ **Good:**
```java
@PutMapping("/{id}")
public UserDTO updateUser(@PathVariable Integer id, @RequestBody UpdateUserRequest request) {
    // Safe! Chỉ accept field có trong UpdateUserRequest
}
```

---

### 2. Enable `fail-on-unknown-properties` globally

```yaml
spring:
  jackson:
    deserialization:
      fail-on-unknown-properties: true  # Global setting
```

---

### 3. Document allowed fields clearly

```java
/**
 * Update user information
 * 
 * Allowed fields:
 * - name, phone, country, companyName
 * - role, status, imageUrl
 * 
 * Protected fields (will be rejected):
 * - id, code, email, password_hash
 * - provider, provider_id, email_verified
 */
@PutMapping("/{id}")
public UserDTO updateUser(...) { ... }
```

---

## ✅ Summary

| Aspect | Without Config | With Config |
|--------|---------------|-------------|
| **Unknown fields** | Silently ignored | Rejected with 400 |
| **Error message** | None | Clear error message |
| **Security** | ⚠️ Potential risk | ✅ Protected |
| **Debugging** | ❌ Hard (no feedback) | ✅ Easy (clear error) |
| **Client experience** | ❌ Confusing | ✅ Clear feedback |

**Recommendation:** ✅ **Always enable `fail-on-unknown-properties: true`**

