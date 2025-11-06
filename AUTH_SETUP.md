# Authentication Setup Guide

## Cấu trúc dự án Spring Boot

Dự án được tổ chức theo kiến trúc layered chuẩn Spring Boot:

```
src/main/java/com/example/pos/
├── entity/              # Entity classes (JPA entities)
│   ├── User.java
│   ├── Role.java
│   └── AuthProvider.java
├── repository/          # Data access layer (Spring Data JPA)
│   └── UserRepository.java
├── dto/                 # Data Transfer Objects
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── AuthResponse.java
│   ├── UserDTO.java
│   └── ApiResponse.java
├── service/             # Business logic layer
│   ├── AuthService.java
│   └── UserService.java
├── controller/          # REST API endpoints
│   ├── AuthController.java
│   └── UserController.java
├── security/            # Security configuration & JWT
│   ├── UserPrincipal.java
│   ├── CustomUserDetailsService.java
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   ├── CustomOAuth2UserService.java
│   ├── OAuth2AuthenticationSuccessHandler.java
│   ├── OAuth2AuthenticationFailureHandler.java
│   └── oauth2/
│       ├── OAuth2UserInfo.java
│       ├── GoogleOAuth2UserInfo.java
│       ├── FacebookOAuth2UserInfo.java
│       └── OAuth2UserInfoFactory.java
├── config/              # Application configuration
│   ├── SecurityConfig.java
│   └── CorsConfig.java
├── exception/           # Exception handling
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   └── GlobalExceptionHandler.java
└── util/                # Utility classes
```

## Cấu hình OAuth2

### 1. Google OAuth2

1. Truy cập [Google Cloud Console](https://console.cloud.google.com/)
2. Tạo project mới hoặc chọn project có sẵn
3. Vào **APIs & Services** > **Credentials**
4. Click **Create Credentials** > **OAuth 2.0 Client ID**
5. Chọn **Web application**
6. Thêm **Authorized redirect URIs**:
   - `http://localhost:8080/oauth2/callback/google`
   - `http://localhost:8080/login/oauth2/code/google`
7. Copy **Client ID** và **Client Secret**

### 2. Facebook OAuth2

1. Truy cập [Facebook Developers](https://developers.facebook.com/)
2. Tạo app mới hoặc chọn app có sẵn
3. Vào **Settings** > **Basic**
4. Copy **App ID** và **App Secret**
5. Vào **Facebook Login** > **Settings**
6. Thêm **Valid OAuth Redirect URIs**:
   - `http://localhost:8080/oauth2/callback/facebook`
   - `http://localhost:8080/login/oauth2/code/facebook`

### 3. Cấu hình Environment Variables

Tạo file `.env` hoặc cấu hình environment variables:

```bash
# JWT Configuration
JWT_SECRET=your-secret-key-at-least-256-bits-long
JWT_EXPIRATION=86400000

# Google OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Facebook OAuth2
FACEBOOK_CLIENT_ID=your-facebook-app-id
FACEBOOK_CLIENT_SECRET=your-facebook-app-secret

# OAuth2 Redirect URI (Frontend URL)
OAUTH2_REDIRECT_URI=http://localhost:3000/oauth2/redirect
```

## API Endpoints

### Authentication Endpoints

#### 1. Register (Local)
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "0123456789",
  "country": "Vietnam",
  "companyName": "ABC Company",
  "role": "CUSTOMER"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "code": "USR12345678",
    "name": "John Doe",
    "email": "john@example.com",
    "role": "CUSTOMER",
    "provider": "LOCAL",
    "emailVerified": false
  }
}
```

#### 2. Login (Local)
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "code": "USR12345678",
    "name": "John Doe",
    "email": "john@example.com",
    "role": "CUSTOMER",
    "provider": "LOCAL"
  }
}
```

#### 3. Social Login (Google/Facebook)

**Google Login:**
```
GET /oauth2/authorize/google
```

**Facebook Login:**
```
GET /oauth2/authorize/facebook
```

Sau khi đăng nhập thành công, user sẽ được redirect về:
```
http://localhost:3000/oauth2/redirect?token=eyJhbGciOiJIUzUxMiJ9...
```

### User Endpoints

#### 1. Get Current User
```http
GET /api/users/me
Authorization: Bearer {token}
```

**Response:**
```json
{
  "id": 1,
  "code": "USR12345678",
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "0123456789",
  "role": "CUSTOMER",
  "status": "active",
  "provider": "LOCAL",
  "emailVerified": false,
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

#### 2. Get User By ID (Admin only)
```http
GET /api/users/{id}
Authorization: Bearer {token}
```

## Database Schema

Bảng `users` đã được cập nhật để hỗ trợ OAuth2:

```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    code VARCHAR(20) UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),
    country VARCHAR(100),
    company_name VARCHAR(150),
    password_hash VARCHAR(255),  -- Nullable for OAuth2 users
    role VARCHAR(50) CHECK (role IN ('admin', 'biller', 'supplier', 'store_owner', 'customer')),
    status VARCHAR(20) DEFAULT 'active',
    provider VARCHAR(50) DEFAULT 'local' CHECK (provider IN ('local', 'google', 'facebook')),
    provider_id VARCHAR(255),
    image_url VARCHAR(500),
    email_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Chạy ứng dụng

1. **Cài đặt dependencies:**
```bash
./mvnw clean install
```

2. **Chạy database migration:**
```bash
# Chạy file database.sql trong PostgreSQL
psql -U postgres -d pos -f database.sql
```

3. **Chạy ứng dụng:**
```bash
./mvnw spring-boot:run
```

Ứng dụng sẽ chạy tại: `http://localhost:8080`

## Testing với Postman/cURL

### Register
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

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

### Get Current User
```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Security Features

1. **JWT Authentication**: Token-based authentication với expiration time
2. **Password Encryption**: BCrypt password hashing
3. **OAuth2 Integration**: Google và Facebook login
4. **CORS Configuration**: Hỗ trợ cross-origin requests
5. **Role-based Access Control**: Phân quyền theo role
6. **Exception Handling**: Global exception handler
7. **Input Validation**: Bean validation cho request DTOs

## Roles

- `ADMIN`: Quản trị viên hệ thống
- `BILLER`: Nhân viên thu ngân
- `SUPPLIER`: Nhà cung cấp
- `STORE_OWNER`: Chủ cửa hàng
- `CUSTOMER`: Khách hàng

## Notes

- JWT secret key nên được generate random và lưu trong environment variables
- Trong production, nên sử dụng HTTPS
- Cấu hình CORS cần được điều chỉnh theo domain thực tế
- OAuth2 redirect URI cần match với frontend URL

