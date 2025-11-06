# Vai trò của application.yml và pom.xml

## 📄 1. application.yml - File cấu hình ứng dụng

**Vị trí:** `src/main/resources/application.yml`

**Vai trò:** Cấu hình runtime của Spring Boot application (database, security, server, etc.)

---

### 🔧 Cấu trúc chi tiết:

#### A. Spring Application Configuration

```yaml
spring:
  application:
    name: pos
```

**Vai trò:**
- Đặt tên cho ứng dụng
- Dùng trong logging, monitoring, service discovery
- Hiển thị trong Spring Boot Admin dashboard

---

#### B. Database Configuration (PostgreSQL)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/pos
    username: postgres
    password: 1234
    driver-class-name: org.postgresql.Driver
```

**Vai trò:**
- `url`: Địa chỉ kết nối database
  - `localhost:5432` - PostgreSQL server
  - `pos` - Tên database
- `username/password`: Thông tin đăng nhập
- `driver-class-name`: Driver JDBC cho PostgreSQL

**Khi nào dùng:**
- Mỗi khi application khởi động
- Mỗi khi thực hiện query database
- Spring Data JPA tự động tạo connection pool

---

#### C. JPA/Hibernate Configuration

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

**Vai trò:**

| Property | Giá trị | Ý nghĩa |
|----------|---------|---------|
| `ddl-auto: update` | update | Tự động cập nhật schema database khi Entity thay đổi |
| `show-sql: true` | true | In SQL queries ra console (để debug) |
| `format_sql: true` | true | Format SQL cho dễ đọc |
| `dialect` | PostgreSQLDialect | Tối ưu SQL cho PostgreSQL |

**Ví dụ:**
```java
// Khi bạn tạo Entity mới:
@Entity
public class Product {
    @Id
    private Integer id;
    private String name;
}

// Hibernate tự động tạo table:
// CREATE TABLE product (id INTEGER, name VARCHAR(255));
```

**Các giá trị `ddl-auto`:**
- `none` - Không làm gì
- `validate` - Chỉ validate schema
- `update` - Cập nhật schema (thêm column, table mới)
- `create` - Xóa và tạo lại schema mỗi lần chạy
- `create-drop` - Tạo khi start, xóa khi stop

---

#### D. Flyway Configuration

```yaml
spring:
  flyway:
    enabled: false
```

**Vai trò:**
- Flyway = Database migration tool
- `enabled: false` = Không dùng Flyway
- Nếu `true`, Flyway sẽ chạy SQL scripts trong `db/migration/`

**Khi nào enable:**
- Production environment
- Cần version control cho database schema
- Nhiều developers cùng làm việc

---

#### E. OAuth2 Configuration (Google & Facebook Login)

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID:your-google-client-id}
            client-secret: ${GOOGLE_CLIENT_SECRET:your-google-client-secret}
            scope:
              - email
              - profile
          facebook:
            client-id: ${FACEBOOK_CLIENT_ID:your-facebook-client-id}
            client-secret: ${FACEBOOK_CLIENT_SECRET:your-facebook-client-secret}
            scope:
              - email
              - public_profile
```

**Vai trò:**
- Cấu hình Social Login (Google, Facebook)
- `${GOOGLE_CLIENT_ID:default-value}` = Đọc từ environment variable hoặc dùng default

**Flow:**
1. User click "Login with Google"
2. Redirect đến Google OAuth
3. Google trả về authorization code
4. Spring Security đổi code lấy access token
5. Lấy user info từ Google
6. Tạo/update user trong database

**Lấy credentials:**
- Google: https://console.cloud.google.com/
- Facebook: https://developers.facebook.com/

---

#### F. JWT Configuration

```yaml
app:
  jwt:
    secret: ${JWT_SECRET:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}
    expiration: 86400000 # 1 day in milliseconds
```

**Vai trò:**
- `secret`: Key để sign/verify JWT token
- `expiration`: Token hết hạn sau 1 ngày (86400000ms = 24h)

**Sử dụng trong code:**
```java
@Value("${app.jwt.secret}")
private String jwtSecret;

@Value("${app.jwt.expiration}")
private long jwtExpiration;

// Generate token
String token = Jwts.builder()
    .setSubject(user.getEmail())
    .setExpiration(new Date(now + jwtExpiration))
    .signWith(SignatureAlgorithm.HS512, jwtSecret)
    .compact();
```

---

#### G. OAuth2 Redirect URI

```yaml
app:
  oauth2:
    redirectUri: ${OAUTH2_REDIRECT_URI:http://localhost:3000/oauth2/redirect}
```

**Vai trò:**
- URL để redirect sau khi OAuth2 login thành công
- Frontend (React/Vue) nhận token tại URL này

**Flow:**
1. User login với Google thành công
2. Backend tạo JWT token
3. Redirect về `http://localhost:3000/oauth2/redirect?token=xxx`
4. Frontend lưu token vào localStorage

---

#### H. Server Configuration

```yaml
server:
  port: 8080
```

**Vai trò:**
- Application chạy trên port 8080
- API endpoint: `http://localhost:8080/api/...`

**Thay đổi port:**
```yaml
server:
  port: 9090  # Chạy trên port 9090
```

---

## 📦 2. pom.xml - Maven Project Configuration

**Vị trí:** `pom.xml` (root directory)

**Vai trò:** Quản lý dependencies, build configuration, plugins

---

### 🔧 Cấu trúc chi tiết:

#### A. Project Information

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.7</version>
</parent>
<groupId>com.example</groupId>
<artifactId>pos</artifactId>
<version>0.0.1-SNAPSHOT</version>
<name>pos</name>
```

**Vai trò:**
- `parent`: Kế thừa Spring Boot configuration
- `groupId`: Tên organization (com.example)
- `artifactId`: Tên project (pos)
- `version`: Phiên bản (0.0.1-SNAPSHOT)

---

#### B. Java Version

```xml
<properties>
    <java.version>21</java.version>
</properties>
```

**Vai trò:**
- Compile code với Java 21
- Sử dụng features của Java 21 (Virtual Threads, Pattern Matching, etc.)

---

#### C. Dependencies (Thư viện)

##### 1. Spring Boot Data JPA

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

**Vai trò:**
- JPA/Hibernate ORM
- Spring Data repositories
- Transaction management

**Cung cấp:**
- `@Entity`, `@Table`, `@Column`
- `JpaRepository`, `@Query`
- `@Transactional`

---

##### 2. Spring Boot Web

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

**Vai trò:**
- REST API
- Embedded Tomcat server
- JSON serialization/deserialization

**Cung cấp:**
- `@RestController`, `@GetMapping`, `@PostMapping`
- `@RequestBody`, `@ResponseBody`
- Jackson JSON library

---

##### 3. Spring Boot DevTools

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

**Vai trò:**
- Auto-restart khi code thay đổi
- LiveReload browser
- Disable cache trong development

**Chỉ hoạt động trong development, không có trong production**

---

##### 4. PostgreSQL Driver

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Vai trò:**
- JDBC driver để kết nối PostgreSQL
- Được load khi application chạy (`scope=runtime`)

---

##### 5. Lombok

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

**Vai trò:**
- Generate boilerplate code tự động
- `@Data` → getter/setter/toString/equals/hashCode
- `@Builder` → Builder pattern
- `@RequiredArgsConstructor` → Constructor injection

**Ví dụ:**
```java
@Data
@Builder
public class User {
    private String name;
    private String email;
}

// Lombok tự động generate:
// - getName(), setName()
// - getEmail(), setEmail()
// - toString(), equals(), hashCode()
// - User.builder().name("John").email("john@example.com").build()
```

---

##### 6. Spring Boot Security

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**Vai trò:**
- Authentication & Authorization
- Password encryption (BCrypt)
- CSRF protection
- Session management

**Cung cấp:**
- `@PreAuthorize`, `@Secured`
- `SecurityFilterChain`
- `UserDetailsService`

---

##### 7. OAuth2 Client

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

**Vai trò:**
- Social Login (Google, Facebook, GitHub)
- OAuth2 flow handling
- Token management

---

##### 8. JWT (JSON Web Token)

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

**Vai trò:**
- Tạo và verify JWT tokens
- Stateless authentication

**Sử dụng:**
```java
String token = Jwts.builder()
    .setSubject(user.getEmail())
    .signWith(SignatureAlgorithm.HS512, secret)
    .compact();
```

---

##### 9. Validation

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

**Vai trò:**
- Validate request data
- Bean Validation (JSR-303)

**Cung cấp:**
- `@NotNull`, `@NotBlank`, `@Email`
- `@Size`, `@Min`, `@Max`
- `@Valid`

**Ví dụ:**
```java
public class RegisterRequest {
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    @Size(min = 8)
    private String password;
}
```

---

#### D. Build Plugins

##### 1. Maven Compiler Plugin

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

**Vai trò:**
- Compile Java code
- Process Lombok annotations

---

##### 2. Spring Boot Maven Plugin

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```

**Vai trò:**
- Package application thành executable JAR
- Chạy application: `mvn spring-boot:run`
- Build: `mvn clean package`

---

## 🔄 Mối quan hệ giữa 2 files

```
pom.xml (Build time)          application.yml (Runtime)
       ↓                              ↓
  Dependencies                  Configuration
       ↓                              ↓
  mvn compile                   Application starts
       ↓                              ↓
  .class files                  Read configs
       ↓                              ↓
  mvn package                   Connect database
       ↓                              ↓
  pos-0.0.1-SNAPSHOT.jar        Start server on port 8080
       ↓                              ↓
  java -jar pos.jar             Application running
```

---

## 📝 Tóm tắt

| File | Vai trò | Khi nào dùng |
|------|---------|--------------|
| **pom.xml** | Quản lý dependencies, build | Khi thêm thư viện mới, build project |
| **application.yml** | Cấu hình runtime | Khi thay đổi database, port, JWT secret |

**pom.xml** = "Cần gì để build?"  
**application.yml** = "Chạy như thế nào?"

