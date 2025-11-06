# POS (Point of Sale) Backend System

Backend API cho hệ thống quản lý bán hàng (POS) được xây dựng với Spring Boot 3.5.7 và Java 21.

## 🌟 Tính năng

### ✅ Authentication & Authorization
- **Local Authentication**: Đăng ký và đăng nhập với email/password
- **JWT Token**: Token-based authentication với expiration time
- **Social Login**: Đăng nhập qua Google và Facebook OAuth2
- **Role-based Access Control**: Phân quyền theo 5 roles (Admin, Biller, Supplier, Store Owner, Customer)
- **Password Encryption**: BCrypt hashing cho bảo mật

### 🏗️ Kiến trúc

Dự án được tổ chức theo **Layered Architecture** chuẩn Spring Boot:

```
📦 com.example.pos
├── 📁 entity/              # Domain models (JPA entities)
├── 📁 repository/          # Data access layer (Spring Data JPA)
├── 📁 dto/                 # Data Transfer Objects
├── 📁 service/             # Business logic layer
├── 📁 controller/          # REST API endpoints
├── 📁 security/            # Security & JWT configuration
├── 📁 config/              # Application configuration
├── 📁 exception/           # Exception handling
└── 📁 util/                # Utility classes
```

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- PostgreSQL 12+

### 1. Clone và Setup Database
```bash
# Tạo database
createdb pos

# Import schema
psql -U postgres -d pos -f database.sql
```

### 2. Build và Run
```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

Application sẽ chạy tại: **http://localhost:8080**

### 3. Test API
```bash
# Health check
curl http://localhost:8080/api/health

# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"password123","role":"CUSTOMER"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

## 📚 Documentation

- **[QUICK_START.md](QUICK_START.md)** - Hướng dẫn khởi động nhanh trong 5 phút
- **[AUTH_SETUP.md](AUTH_SETUP.md)** - Chi tiết setup OAuth2 và API documentation
- **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)** - Tổng quan implementation
- **[test-api.http](test-api.http)** - HTTP request examples

## 🔑 API Endpoints

### Public Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Đăng ký tài khoản mới |
| POST | `/api/auth/login` | Đăng nhập |
| GET | `/api/health` | Health check |
| GET | `/oauth2/authorize/google` | Google login |
| GET | `/oauth2/authorize/facebook` | Facebook login |

### Protected Endpoints
| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| GET | `/api/users/me` | Lấy thông tin user hiện tại | Any authenticated |
| GET | `/api/users/{id}` | Lấy thông tin user theo ID | ADMIN |

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.5.7
- **Language**: Java 21
- **Database**: PostgreSQL
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA (Hibernate)
- **Build Tool**: Maven
- **Authentication**: JWT + OAuth2 (Google, Facebook)

## 📦 Dependencies

```xml
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-oauth2-client
- spring-boot-starter-validation
- jjwt (JWT library)
- postgresql
- lombok
```

## 🔐 Security Features

- ✅ JWT Authentication với HS512
- ✅ BCrypt Password Hashing
- ✅ OAuth2 Integration (Google, Facebook)
- ✅ CORS Configuration
- ✅ Role-based Access Control
- ✅ Stateless Session Management
- ✅ Global Exception Handling
- ✅ Input Validation

## 👥 User Roles

| Role | Description |
|------|-------------|
| `ADMIN` | Quản trị viên hệ thống |
| `BILLER` | Nhân viên thu ngân |
| `SUPPLIER` | Nhà cung cấp |
| `STORE_OWNER` | Chủ cửa hàng |
| `CUSTOMER` | Khách hàng (default) |

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    code VARCHAR(20) UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),
    country VARCHAR(100),
    company_name VARCHAR(150),
    password_hash VARCHAR(255),
    role VARCHAR(50),
    status VARCHAR(20) DEFAULT 'active',
    provider VARCHAR(50) DEFAULT 'local',
    provider_id VARCHAR(255),
    image_url VARCHAR(500),
    email_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

Xem thêm các bảng khác trong file [database.sql](database.sql)

## ⚙️ Configuration

### Environment Variables
Tạo file `.env` từ `.env.example`:

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/pos
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=1234

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
FACEBOOK_CLIENT_ID=your-facebook-app-id
FACEBOOK_CLIENT_SECRET=your-facebook-app-secret
OAUTH2_REDIRECT_URI=http://localhost:3000/oauth2/redirect
```

## 🧪 Testing

### Using cURL
```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","email":"test@example.com","password":"password123"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Get current user
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Using test-api.http
Mở file `test-api.http` trong VS Code với REST Client extension hoặc import vào Postman.

## 📊 Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/example/pos/
│   │   │   ├── config/              # Configuration classes
│   │   │   ├── controller/          # REST controllers
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── entity/              # JPA entities
│   │   │   ├── exception/           # Custom exceptions
│   │   │   ├── repository/          # Data repositories
│   │   │   ├── security/            # Security & JWT
│   │   │   ├── service/             # Business logic
│   │   │   └── util/                # Utilities
│   │   └── resources/
│   │       └── application.yml      # Configuration
│   └── test/                        # Test classes
├── database.sql                     # Database schema
├── pom.xml                          # Maven dependencies
├── .env.example                     # Environment variables template
├── README.md                        # This file
├── QUICK_START.md                   # Quick start guide
├── AUTH_SETUP.md                    # Authentication setup
├── IMPLEMENTATION_SUMMARY.md        # Implementation details
└── test-api.http                    # API test cases
```

## 🤝 Contributing

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License.

## 📞 Support

Nếu gặp vấn đề, vui lòng:
1. Kiểm tra [QUICK_START.md](QUICK_START.md) và [AUTH_SETUP.md](AUTH_SETUP.md)
2. Xem phần Troubleshooting trong documentation
3. Tạo issue trên GitHub

## 🎯 Roadmap

- [x] Authentication & Authorization
- [x] JWT Token
- [x] Social Login (Google, Facebook)
- [x] Role-based Access Control
- [ ] Product Management
- [ ] Warehouse Management
- [ ] Sales & Purchase
- [ ] Invoice Management
- [ ] Reporting & Analytics

---

Made with ❤️ using Spring Boot

