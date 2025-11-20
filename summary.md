# Tóm tắt Dự án Backend POS

## Giới thiệu
Dự án này là backend cho hệ thống Point of Sale (POS), được xây dựng bằng Spring Boot. Nó xử lý các chức năng như quản lý người dùng, sản phẩm, kho hàng, nhà cung cấp, và xác thực. Dự án sử dụng PostgreSQL làm cơ sở dữ liệu và tích hợp bảo mật với JWT và OAuth2.

## Cấu trúc Thư mục Chính
Dự án có cấu trúc tiêu chuẩn của Maven/Spring Boot:
- **src/main/java/com/example/pos**: Chứa mã nguồn chính.
  - **config**: Cấu hình CORS và Security.
  - **controller**: Các controller cho API (ví dụ: AuthController, BillerController).
  - **dto**: Data Transfer Objects cho request/response.
  - **entity**: Các entity JPA (ví dụ: User, Brand, Category).
  - **exception**: Xử lý ngoại lệ.
  - **repository**: Các repository JPA.
  - **security**: Bảo mật, bao gồm JWT và OAuth2.
  - **service**: Các service logic nghiệp vụ.
  - **util**: Công cụ hỗ trợ.
- **src/main/resources**: File application.yml cho cấu hình.
- **src/test/java**: Các test.
- Các file khác: pom.xml, .gitignore, database.sql, v.v.

## Các Thành phần Chính
### Controllers
Các controller xử lý API, chủ yếu dành cho admin:
- AuthController: Xử lý đăng nhập, đăng ký, quên mật khẩu.
- BillerController, BrandController, CategoryController, v.v.: Quản lý biller, brand, category, với các endpoint GET, POST, PUT, DELETE.

### Services
Logic nghiệp vụ:
- AuthService, BillerService, BrandService, v.v.: Xử lý tạo, cập nhật, xóa (soft delete) các entity.

### Entities
Các model dữ liệu:
- User, Brand, Category, SubCategory, Supplier, Unit, VariantAttribute, Warehouse, Store, PasswordResetOtp.
- Sử dụng JPA annotations cho mapping database.

### Repositories
Interface JPA cho truy vấn database: BrandRepository, CategoryRepository, v.v.

### Security
- Sử dụng Spring Security với JWT cho xác thực.
- Hỗ trợ OAuth2 cho Google/Facebook.

## Dependencies (từ pom.xml)
- Spring Boot 3.5.7.
- spring-boot-starter-data-jpa: Cho JPA.
- spring-boot-starter-web: Cho web API.
- postgresql: Driver database.
- lombok: Giảm boilerplate code.
- spring-boot-starter-security và oauth2-client: Bảo mật.
- jjwt: JWT handling.
- spring-boot-starter-validation: Validation.
- spring-boot-starter-mail: Gửi email.

## Các Tính năng Chính
- Xác thực: Đăng ký, đăng nhập, quên mật khẩu với OTP.
- Quản lý: Biller, Brand, Category, Customer, Store, Supplier, Unit, VariantAttribute, Warehouse.
- Pagination, search, filter cho các list.
- Soft delete cho hầu hết entity.
- Bảo mật: Role-based access (ADMIN), JWT token.

## Gợi ý Cải thiện
- Thêm test coverage.
- Tích hợp logging tốt hơn.
- Xem xét thêm API documentation (Swagger).

Ngày tổng kết: 2025-11-20