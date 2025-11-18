# 🎨 Variant Attribute API Documentation

## 📋 Overview

API quản lý **Variant Attributes** (Thuộc tính biến thể) cho hệ thống POS. Variant Attributes được sử dụng để định nghĩa các thuộc tính có thể thay đổi của sản phẩm như màu sắc, kích thước, chất liệu, v.v.

**Base URL**: `/api/variant-attributes`

**Authentication**: Tất cả endpoints yêu cầu JWT token và role **ADMIN**

---

## 🗂️ Database Schema

```sql
create table variant_attributes
(
    id         serial primary key,
    name       varchar(100) not null,
    values     text[],
    created_at timestamp   default CURRENT_TIMESTAMP,
    status     varchar(20) default 'active'::character varying,
    image_url  varchar(500)
);
```

**Fields**:
- `id`: Primary key (auto-increment)
- `name`: Tên thuộc tính (bắt buộc) - VD: "Color", "Size", "Material"
- `values`: Mảng các giá trị có thể có - VD: ["Red", "Blue", "Green"]
- `image_url`: URL hình ảnh minh họa (tùy chọn)
- `created_at`: Thời gian tạo (tự động)
- `status`: Trạng thái - "active", "inactive", hoặc "DELETED"

---

## 🔌 API Endpoints

### 1. Get All Variant Attributes (with Pagination)

**GET** `/api/variant-attributes`

Lấy danh sách variant attributes với phân trang, tìm kiếm và lọc.

**Query Parameters**:
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Số trang (bắt đầu từ 0) |
| `size` | int | 10 | Số items mỗi trang |
| `search` | string | - | Tìm kiếm theo tên |
| `status` | string | - | Lọc theo trạng thái |
| `sortBy` | string | createdAt | Trường sắp xếp |
| `sortDir` | string | desc | Hướng sắp xếp (asc/desc) |

**Response** (200 OK):
```json
{
  "variantAttributes": [
    {
      "id": 1,
      "name": "Color",
      "values": ["Red", "Blue", "Green", "Yellow"],
      "imageUrl": "https://example.com/images/color.png",
      "createdAt": "2024-01-15T10:30:00",
      "status": "active"
    }
  ],
  "currentPage": 0,
  "totalPages": 5,
  "totalItems": 45
}
```

---

### 2. Create Variant Attribute

**POST** `/api/variant-attributes`

Tạo variant attribute mới.

**Request Body**:
```json
{
  "name": "Color",
  "values": ["Red", "Blue", "Green", "Yellow", "Black", "White"],
  "imageUrl": "https://example.com/images/color.png"
}
```

**Validation**:
- `name`: Bắt buộc, không được để trống
- `values`: Tùy chọn, có thể null hoặc mảng rỗng
- `imageUrl`: Tùy chọn

**Response** (201 Created):
```json
{
  "id": 1,
  "name": "Color",
  "values": ["Red", "Blue", "Green", "Yellow", "Black", "White"],
  "imageUrl": "https://example.com/images/color.png",
  "createdAt": "2024-01-15T10:30:00",
  "status": "active"
}
```

---

### 3. Update Variant Attribute

**PUT** `/api/variant-attributes/{id}`

Cập nhật variant attribute (partial update).

**Path Parameters**:
- `id`: ID của variant attribute

**Request Body** (tất cả fields đều optional):
```json
{
  "name": "Product Color",
  "values": ["Red", "Blue", "Green", "Yellow", "Black", "White", "Gray"],
  "imageUrl": "https://example.com/images/color-updated.png",
  "status": "active"
}
```

**Response** (200 OK):
```json
{
  "id": 1,
  "name": "Product Color",
  "values": ["Red", "Blue", "Green", "Yellow", "Black", "White", "Gray"],
  "imageUrl": "https://example.com/images/color-updated.png",
  "createdAt": "2024-01-15T10:30:00",
  "status": "active"
}
```

**Error Response** (404 Not Found):
```json
{
  "message": "VariantAttribute not found with id: 999"
}
```

---

### 4. Delete Variant Attribute (Soft Delete)

**DELETE** `/api/variant-attributes/{id}`

Xóa mềm variant attribute (set status = "DELETED").

**Path Parameters**:
- `id`: ID của variant attribute

**Response** (200 OK):
```json
{
  "message": "Variant attribute deleted successfully"
}
```

**Error Response** (404 Not Found):
```json
{
  "message": "VariantAttribute not found with id: 999"
}
```

---

## 🔍 Search & Filter Examples

### Search by name
```
GET /api/variant-attributes?search=color
```

### Filter by status
```
GET /api/variant-attributes?status=active
```

### Search + Filter combined
```
GET /api/variant-attributes?search=size&status=active
```

### Sort by name ascending
```
GET /api/variant-attributes?sortBy=name&sortDir=asc
```

### Pagination
```
GET /api/variant-attributes?page=2&size=20
```

---

## 📁 Project Structure

```
src/main/java/com/example/pos/
├── entity/
│   └── VariantAttribute.java                # JPA Entity
├── repository/
│   └── VariantAttributeRepository.java      # Data access with custom queries
├── dto/
│   ├── VariantAttributeDTO.java             # Response DTO
│   ├── CreateVariantAttributeRequest.java   # Create request
│   ├── UpdateVariantAttributeRequest.java   # Update request
│   └── VariantAttributeListResponse.java    # List response with pagination
├── service/
│   └── VariantAttributeService.java         # Business logic
└── controller/
    └── VariantAttributeController.java      # REST endpoints
```

---

## 🎉 Implementation Complete!

All CRUD operations for Variant Attributes are now fully implemented and ready to use.

