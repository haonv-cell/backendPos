# ✅ Hướng dẫn kiểm tra dữ liệu trong Database

## 🎉 KẾT QUẢ: Dữ liệu ĐÃ được lưu thành công!

Khi test API register, dữ liệu **ĐÃ được lưu vào database**. Hiện tại có **2 users** trong database:

```
User #1:
- Code: USREF5AA31B
- Name: John Doe
- Email: john@example.com
- Role: CUSTOMER
- Provider: LOCAL
- Created: 2025-11-05T09:00:50

User #2:
- Code: USR7EAC3B77
- Name: John Doe
- Email: johnn@example.com
- Role: CUSTOMER
- Provider: LOCAL
- Created: 2025-11-05T09:08:51
```

---

## 📋 3 Cách kiểm tra dữ liệu trong Database

### **Cách 1: Sử dụng API (KHUYẾN NGHỊ - Nhanh nhất)**

```powershell
# Chạy script PowerShell
powershell -File test-check-users.ps1
```

Hoặc test trực tiếp với curl/Postman:

```bash
# Đếm số lượng users
GET http://localhost:8080/api/users/count

# Lấy tất cả users
GET http://localhost:8080/api/users/all
```

---

### **Cách 2: Sử dụng pgAdmin (GUI)**

1. Mở **pgAdmin**
2. Kết nối đến server PostgreSQL
3. Chọn database **pos**
4. Mở **Schemas → public → Tables → users**
5. Click chuột phải → **View/Edit Data → All Rows**

---

### **Cách 3: Sử dụng psql (Command Line)**

```bash
# Kết nối đến database
psql -U postgres -d pos

# Xem tất cả users
SELECT id, code, name, email, role, provider, status, created_at 
FROM users 
ORDER BY id DESC;

# Đếm số lượng users
SELECT COUNT(*) FROM users;

# Xem user mới nhất
SELECT * FROM users ORDER BY created_at DESC LIMIT 1;

# Thoát
\q
```

Hoặc chạy file SQL:

```bash
psql -U postgres -d pos -f check-data.sql
```

---

## 🔍 Lý do tại sao bạn không thấy dữ liệu trước đó

Có thể do một trong các nguyên nhân sau:

### 1. **Kiểm tra sai database**
- Bạn có thể đang kết nối đến database khác (không phải `pos`)
- Kiểm tra connection string trong pgAdmin/psql

### 2. **Kiểm tra sai schema**
- PostgreSQL có thể có nhiều schema
- Đảm bảo bạn đang xem schema `public`

### 3. **Kiểm tra trước khi dữ liệu được insert**
- Dữ liệu chỉ được lưu SAU KHI API register được gọi thành công
- Thời gian insert: 09:00:50 và 09:08:51

### 4. **Cache của pgAdmin**
- pgAdmin có thể cache dữ liệu cũ
- Giải pháp: Click **Refresh** (F5) trong pgAdmin

---

## ✅ Xác nhận Entity khớp với Database

### Database Schema (database.sql)

```sql
create table users (
    id            integer PRIMARY KEY,
    code          varchar(20) UNIQUE,
    name          varchar(100) NOT NULL,
    email         varchar(150) NOT NULL UNIQUE,
    phone         varchar(20),
    country       varchar(100),
    company_name  varchar(150),
    password_hash varchar(255),
    role          varchar(50),
    status        varchar(20) DEFAULT 'active',
    provider      varchar(50) DEFAULT 'local',
    provider_id   varchar(255),
    image_url     varchar(500),
    email_verified boolean DEFAULT false,
    created_at    timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_at    timestamp DEFAULT CURRENT_TIMESTAMP
);
```

### Entity Mapping (User.java)

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String code;
    private String name;
    private String email;
    private String phone;
    private String country;
    
    @Column(name = "company_name")
    private String companyName;
    
    @Column(name = "password_hash")
    private String passwordHash;
    
    @Convert(converter = RoleConverter.class)
    private Role role;
    
    private String status;
    
    @Convert(converter = AuthProviderConverter.class)
    private AuthProvider provider;
    
    @Column(name = "provider_id")
    private String providerId;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "email_verified")
    private Boolean emailVerified;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### ✅ Mapping hoàn toàn khớp!

| Database Column | Entity Field | @Column Annotation |
|----------------|--------------|-------------------|
| `id` | `id` | ✅ Auto |
| `code` | `code` | ✅ Auto |
| `name` | `name` | ✅ Auto |
| `email` | `email` | ✅ Auto |
| `phone` | `phone` | ✅ Auto |
| `country` | `country` | ✅ Auto |
| `company_name` | `companyName` | ✅ `@Column(name = "company_name")` |
| `password_hash` | `passwordHash` | ✅ `@Column(name = "password_hash")` |
| `role` | `role` | ✅ Auto + Converter |
| `status` | `status` | ✅ Auto |
| `provider` | `provider` | ✅ Auto + Converter |
| `provider_id` | `providerId` | ✅ `@Column(name = "provider_id")` |
| `image_url` | `imageUrl` | ✅ `@Column(name = "image_url")` |
| `email_verified` | `emailVerified` | ✅ `@Column(name = "email_verified")` |
| `created_at` | `createdAt` | ✅ `@Column(name = "created_at")` + `@CreationTimestamp` |
| `updated_at` | `updatedAt` | ✅ `@Column(name = "updated_at")` + `@UpdateTimestamp` |

---

## 🎯 Test Register API

### Request

```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123",
    "role": "CUSTOMER"
}
```

### Response (Success)

```json
{
    "success": true,
    "message": "User registered successfully",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "user": {
            "id": 3,
            "code": "USR123ABC",
            "name": "Test User",
            "email": "test@example.com",
            "role": "CUSTOMER",
            "provider": "LOCAL",
            "status": "active"
        }
    }
}
```

### Kiểm tra trong Database

```bash
# Cách 1: API
curl http://localhost:8080/api/users/count
# Response: 3

# Cách 2: SQL
psql -U postgres -d pos -c "SELECT COUNT(*) FROM users;"
# Response: 3
```

---

## 🚀 Kết luận

✅ **Entity mapping hoàn toàn khớp với database schema**  
✅ **Dữ liệu ĐÃ được lưu thành công vào database**  
✅ **API register hoạt động chính xác**  
✅ **Có thể kiểm tra dữ liệu qua API hoặc trực tiếp database**

**Hệ thống hoạt động hoàn hảo!** 🎉

