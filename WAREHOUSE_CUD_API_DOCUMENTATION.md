# Warehouse CUD API Documentation

## ✅ Đã hoàn thành

Đã tạo đầy đủ CREATE, UPDATE, DELETE endpoints cho Warehouse API theo yêu cầu trong `database.sql`.

---

## 📋 Files đã tạo/cập nhật

### 1. Request DTOs
- ✅ `CreateWarehouseRequest.java` - DTO cho create với validation
- ✅ `UpdateWarehouseRequest.java` - DTO cho update

### 2. Response DTOs
- ✅ `WarehouseDTO.java` - Thêm `userId` và `managingUserName` fields

### 3. Service
- ✅ `WarehouseService.java` - Thêm `createWarehouse()`, `updateWarehouse()`, `deleteWarehouse()`

### 4. Controller
- ✅ `WarehouseController.java` - Thêm POST, PUT, DELETE endpoints

### 5. Repository
- ✅ `WarehouseRepository.java` - Thêm LEFT JOIN với users table

---

## 🎯 API Endpoints

### 1. CREATE - Thêm kho mới

**Endpoint:** `POST /api/warehouses`

**Authorization:** ADMIN only

**Request Body:**
```json
{
  "name": "New Warehouse",
  "contactPerson": "John Doe",
  "phone": "+1234567890",
  "userId": 1
}
```

**Validation:**
- ✅ `name` - Bắt buộc, max 150 ký tự
- ✅ `userId` - Bắt buộc, phải tồn tại trong bảng users
- ⚠️ `contactPerson` - Optional, max 100 ký tự
- ⚠️ `phone` - Optional, max 20 ký tự

**Response:** `201 Created`
```json
{
  "id": 11,
  "name": "New Warehouse",
  "contactPerson": "John Doe",
  "phone": "+1234567890",
  "totalProducts": 0,
  "stock": 0,
  "qty": 0,
  "createdOn": "2025-11-12",
  "status": "active",
  "userId": 1,
  "managingUserName": "Admin User"
}
```

**Error Responses:**

**400 Bad Request** - userId không tồn tại:
```json
{
  "timestamp": "2025-11-12T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "User ID 999 does not exist",
  "path": "/api/warehouses"
}
```

**400 Bad Request** - Validation error:
```json
{
  "timestamp": "2025-11-12T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Name is required",
  "path": "/api/warehouses"
}
```

---

### 2. UPDATE - Cập nhật thông tin kho

**Endpoint:** `PUT /api/warehouses/{id}`

**Authorization:** ADMIN only

**Request Body:**
```json
{
  "name": "Updated Warehouse Name",
  "contactPerson": "Jane Smith",
  "phone": "+0987654321",
  "userId": 2
}
```

**Validation:**
- ⚠️ Tất cả fields đều optional
- ✅ Nếu `userId` thay đổi, phải validate userId mới tồn tại
- ❌ **KHÔNG cho phép** update `stock`, `totalProducts`, `qty`

**Allowed Fields:**
- ✅ `name`
- ✅ `contactPerson`
- ✅ `phone`
- ✅ `userId`

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Updated Warehouse Name",
  "contactPerson": "Jane Smith",
  "phone": "+0987654321",
  "totalProducts": 10,
  "stock": 600,
  "qty": 80,
  "createdOn": "2024-12-24",
  "status": "active",
  "userId": 2,
  "managingUserName": "New Manager"
}
```

**Error Responses:**

**404 Not Found** - Warehouse không tồn tại:
```json
{
  "timestamp": "2025-11-12T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Warehouse with ID 999 not found",
  "path": "/api/warehouses/999"
}
```

**400 Bad Request** - userId mới không tồn tại:
```json
{
  "timestamp": "2025-11-12T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "User ID 999 does not exist",
  "path": "/api/warehouses/1"
}
```

---

### 3. DELETE - Xóa mềm kho

**Endpoint:** `DELETE /api/warehouses/{id}`

**Authorization:** ADMIN only

**Business Rules:**
1. ✅ **Soft Delete** - Chỉ cập nhật `status = 'DELETED'`, không xóa vật lý
2. ✅ **Stock Check** - Nếu `stock > 0`, trả về 409 Conflict
3. ✅ **Already Deleted** - Nếu đã deleted, trả về message "Warehouse already deleted"

**Response:** `200 OK`
```json
{
  "message": "Warehouse deleted successfully"
}
```

**Error Responses:**

**404 Not Found** - Warehouse không tồn tại:
```json
{
  "timestamp": "2025-11-12T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Warehouse with ID 999 not found",
  "path": "/api/warehouses/999"
}
```

**409 Conflict** - Còn tồn hàng:
```json
{
  "timestamp": "2025-11-12T10:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Không thể xóa kho vì vẫn còn tồn hàng.",
  "path": "/api/warehouses/1"
}
```

**200 OK** - Đã deleted trước đó:
```json
{
  "message": "Warehouse already deleted"
}
```

---

## 🔐 Security & Validation

### 1. Authorization
- ✅ Tất cả endpoints đều yêu cầu `ROLE_ADMIN`
- ✅ JWT token bắt buộc

### 2. Field Protection

#### ✅ CREATE - Fields được phép:
- `name` (required)
- `contactPerson` (optional)
- `phone` (optional)
- `userId` (required, must exist)

#### ✅ UPDATE - Fields được phép:
- `name` (optional)
- `contactPerson` (optional)
- `phone` (optional)
- `userId` (optional, must exist if changed)

#### ❌ Fields KHÔNG được phép update:
- `totalProducts` - Tính toán từ nghiệp vụ khác
- `stock` - Tính toán từ nghiệp vụ khác
- `qty` - Bị bỏ qua (trùng lặp với stock)
- `status` - Chỉ thay đổi qua DELETE endpoint
- `createdOn` - Auto-generated

### 3. Business Rules

#### Soft Delete
- ✅ Không xóa vật lý khỏi database
- ✅ Chỉ cập nhật `status = 'DELETED'`
- ✅ GET endpoints tự động exclude DELETED warehouses

#### Stock Check
- ✅ Không cho phép xóa kho còn tồn hàng (`stock > 0`)
- ✅ Trả về 409 Conflict với message tiếng Việt

#### User Validation
- ✅ `userId` phải tồn tại trong bảng users
- ✅ Validate cả khi CREATE và UPDATE

---

## 📊 Database Schema

```sql
create table warehouses
(
    id             serial primary key,
    name           varchar(150) not null,
    contact_person varchar(100),
    phone          varchar(20),
    total_products integer     default 0,
    stock          integer     default 0,
    qty            integer     default 0,
    created_on     date        default CURRENT_DATE,
    status         varchar(20) default 'active',
    user_id        integer references users
);
```

---

## 🧪 Test Examples

### Test CREATE

```bash
# Success case
curl -X POST "http://localhost:8080/api/warehouses" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Warehouse",
    "contactPerson": "Test Person",
    "phone": "+1234567890",
    "userId": 1
  }'

# Error: userId không tồn tại
curl -X POST "http://localhost:8080/api/warehouses" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Warehouse",
    "userId": 999
  }'

# Error: name bắt buộc
curl -X POST "http://localhost:8080/api/warehouses" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1
  }'
```

### Test UPDATE

```bash
# Success case
curl -X PUT "http://localhost:8080/api/warehouses/1" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Name",
    "contactPerson": "New Contact"
  }'

# Update userId
curl -X PUT "http://localhost:8080/api/warehouses/1" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2
  }'

# Error: warehouse không tồn tại
curl -X PUT "http://localhost:8080/api/warehouses/999" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test"
  }'
```

### Test DELETE

```bash
# Success case (stock = 0)
curl -X DELETE "http://localhost:8080/api/warehouses/1" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"

# Error: còn tồn hàng (stock > 0)
curl -X DELETE "http://localhost:8080/api/warehouses/1" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"

# Already deleted
curl -X DELETE "http://localhost:8080/api/warehouses/1" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

---

## 📝 Implementation Details

### Service Layer

<augment_code_snippet path="src/main/java/com/example/pos/service/WarehouseService.java" mode="EXCERPT">
```java
@Transactional
public WarehouseDTO createWarehouse(CreateWarehouseRequest request) {
    // Validate userId exists
    userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User ID " + request.getUserId() + " does not exist"
            ));

    // Create warehouse with defaults
    Warehouse warehouse = Warehouse.builder()
            .name(request.getName())
            .contactPerson(request.getContactPerson())
            .phone(request.getPhone())
            .userId(request.getUserId())
            .totalProducts(0)
            .stock(0)
            .qty(0)
            .status("active")
            .build();

    return convertToDTO(warehouseRepository.save(warehouse));
}
```
</augment_code_snippet>

### Controller Layer

<augment_code_snippet path="src/main/java/com/example/pos/controller/WarehouseController.java" mode="EXCERPT">
```java
@PostMapping
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<WarehouseDTO> createWarehouse(@Valid @RequestBody CreateWarehouseRequest request) {
    WarehouseDTO warehouse = warehouseService.createWarehouse(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(warehouse);
}

@PutMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<WarehouseDTO> updateWarehouse(
        @PathVariable Integer id,
        @Valid @RequestBody UpdateWarehouseRequest request) {
    WarehouseDTO warehouse = warehouseService.updateWarehouse(id, request);
    return ResponseEntity.ok(warehouse);
}

@DeleteMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<MessageResponse> deleteWarehouse(@PathVariable Integer id) {
    MessageResponse response = warehouseService.deleteWarehouse(id);
    return ResponseEntity.ok(response);
}
```
</augment_code_snippet>

---

## ✅ Build Status

```
[INFO] BUILD SUCCESS
[INFO] Total time:  8.187 s
```

---

## 📌 Summary

### ✅ Đã implement đầy đủ theo yêu cầu:

1. **CREATE**
   - ✅ Validate `name` required
   - ✅ Validate `userId` required và tồn tại
   - ✅ Gán default values: `status='active'`, `totalProducts=0`, `stock=0`
   - ✅ Response 201 Created

2. **UPDATE**
   - ✅ Chỉ update: `name`, `contactPerson`, `phone`, `userId`
   - ✅ Validate `userId` nếu thay đổi
   - ✅ KHÔNG cho phép update `stock`, `totalProducts`
   - ✅ Response 200 OK

3. **DELETE**
   - ✅ Soft delete (status = 'DELETED')
   - ✅ Check stock > 0 → 409 Conflict
   - ✅ Already deleted → 200 OK với message
   - ✅ Response 200 OK hoặc 204 No Content

4. **READ**
   - ✅ JOIN với users table để lấy `managingUserName`
   - ✅ Luôn exclude `status != 'DELETED'`
   - ✅ Pagination, search, filter, sort

### 🎯 Business Rules đã tuân thủ:

- ✅ Soft Delete - Không xóa vật lý
- ✅ Summary Fields - Không cho phép edit `totalProducts`, `stock`
- ✅ Ignored Field - Bỏ qua `qty`
- ✅ Foreign Key - Validate `userId` tồn tại

