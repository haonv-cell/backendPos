# Quick Start Guide - POS Authentication System

## 🚀 Khởi động nhanh trong 5 phút

### Bước 1: Chuẩn bị Database (2 phút)

```bash
# Tạo database
createdb pos

# Import schema
psql -U postgres -d pos -f database.sql
```

### Bước 2: Build Project (1 phút)

```bash
cd backend
./mvnw clean install
```

### Bước 3: Chạy Application (1 phút)

```bash
./mvnw spring-boot:run
```

Ứng dụng sẽ chạy tại: **http://localhost:8080**

### Bước 4: Test API (1 phút)

#### 1. Health Check
```bash
curl http://localhost:8080/api/health
```

#### 2. Đăng ký tài khoản
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123",
    "role": "CUSTOMER"
  }'
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "code": "USR12345678",
    "name": "Test User",
    "email": "test@example.com",
    "role": "CUSTOMER"
  }
}
```

#### 3. Đăng nhập
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

#### 4. Lấy thông tin user hiện tại
```bash
# Thay YOUR_TOKEN bằng accessToken từ response trên
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 🎯 Các tính năng chính

### ✅ Local Authentication
- ✅ Đăng ký tài khoản mới
- ✅ Đăng nhập với email/password
- ✅ JWT token authentication
- ✅ Password encryption (BCrypt)

### ✅ Social Login (Cần cấu hình OAuth2)
- ✅ Google Login
- ✅ Facebook Login

### ✅ Security
- ✅ JWT-based authentication
- ✅ Role-based access control
- ✅ CORS configuration
- ✅ Exception handling

## 📁 Cấu trúc Project

```
backend/
├── src/main/java/com/example/pos/
│   ├── entity/              # Domain models
│   ├── repository/          # Data access
│   ├── dto/                 # Request/Response objects
│   ├── service/             # Business logic
│   ├── controller/          # REST endpoints
│   ├── security/            # Security & JWT
│   ├── config/              # Configuration
│   └── exception/           # Exception handling
│
├── src/main/resources/
│   └── application.yml      # Configuration file
│
├── database.sql             # Database schema
├── AUTH_SETUP.md           # Chi tiết setup OAuth2
├── IMPLEMENTATION_SUMMARY.md # Tổng quan implementation
├── test-api.http           # Test cases
└── .env.example            # Environment variables template
```

## 🔑 API Endpoints

### Public (Không cần authentication)
```
POST   /api/auth/register      - Đăng ký
POST   /api/auth/login         - Đăng nhập
GET    /api/health             - Health check
```

### Protected (Cần JWT token)
```
GET    /api/users/me           - Thông tin user hiện tại
GET    /api/users/{id}         - Thông tin user theo ID (Admin only)
```

### OAuth2 (Mở trong browser)
```
GET    /oauth2/authorize/google    - Google login
GET    /oauth2/authorize/facebook  - Facebook login
```

## 🔧 Cấu hình OAuth2 (Tùy chọn)

Nếu muốn sử dụng Social Login, xem chi tiết tại: [AUTH_SETUP.md](AUTH_SETUP.md)

### Quick Setup:
1. Lấy credentials từ Google/Facebook
2. Tạo file `.env` từ `.env.example`
3. Điền thông tin OAuth2
4. Restart application

## 📝 Test với Postman

Import file `test-api.http` vào Postman hoặc sử dụng VS Code REST Client extension.

## 🎭 Roles

- `ADMIN` - Quản trị viên
- `BILLER` - Nhân viên thu ngân
- `SUPPLIER` - Nhà cung cấp
- `STORE_OWNER` - Chủ cửa hàng
- `CUSTOMER` - Khách hàng (mặc định)

## ⚠️ Troubleshooting

### Lỗi kết nối database
```
Error: Connection refused
```
**Giải pháp:** Kiểm tra PostgreSQL đang chạy và thông tin kết nối trong `application.yml`

### Lỗi port đã được sử dụng
```
Error: Port 8080 is already in use
```
**Giải pháp:** Đổi port trong `application.yml` hoặc kill process đang dùng port 8080

### Build failed
```
Error: Cannot find symbol
```
**Giải pháp:** 
```bash
./mvnw clean install -U
```

## 📚 Tài liệu chi tiết

- [AUTH_SETUP.md](AUTH_SETUP.md) - Hướng dẫn setup OAuth2 chi tiết
- [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) - Tổng quan implementation
- [test-api.http](test-api.http) - Test cases đầy đủ

## 🎉 Hoàn thành!

Bây giờ bạn đã có một hệ thống authentication hoàn chỉnh với:
- ✅ Login/Register qua Database
- ✅ JWT Authentication
- ✅ Social Login (Google/Facebook)
- ✅ Role-based Access Control
- ✅ Security best practices

Sẵn sàng để phát triển các tính năng tiếp theo! 🚀

