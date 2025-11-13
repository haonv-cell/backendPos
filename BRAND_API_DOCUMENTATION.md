# 🏷️ Brand API Documentation

## Overview
Complete CRUD API for managing product brands (Nike, Samsung, Apple, etc.) in the POS system.

---

## 🔐 Authentication
All endpoints require **ADMIN** role authentication.

**Header:**
```
Authorization: Bearer {JWT_TOKEN}
```

---

## 📋 API Endpoints

### 1. **GET** `/api/brands` - Get All Brands

Get paginated list of brands with optional search and filtering.

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Page number (0-based) |
| `size` | int | 10 | Items per page |
| `search` | string | - | Search by brand name |
| `status` | string | - | Filter by status (active/inactive) |
| `sortBy` | string | createdAt | Sort field (name, createdAt) |
| `sortDir` | string | desc | Sort direction (asc/desc) |

**Response 200 OK:**
```json
{
  "brands": [
    {
      "id": 1,
      "name": "Nike",
      "imageUrl": "https://example.com/images/nike-logo.png",
      "createdAt": "2024-12-24T10:30:00",
      "status": "active"
    }
  ],
  "currentPage": 0,
  "totalPages": 3,
  "totalItems": 30
}
```

**Examples:**
```bash
# Get all brands (default pagination)
GET /api/brands

# Search for "Nike"
GET /api/brands?search=Nike

# Get active brands only
GET /api/brands?status=active

# Sort by name ascending
GET /api/brands?sortBy=name&sortDir=asc

# Page 2 with 20 items
GET /api/brands?page=1&size=20
```

---

### 2. **POST** `/api/brands` - Create New Brand

Create a new brand.

**Request Body:**
```json
{
  "name": "Nike",
  "imageUrl": "https://example.com/images/nike-logo.png"
}
```

**Validation Rules:**
- `name`: Required, not blank
- `imageUrl`: Optional

**Response 201 Created:**
```json
{
  "id": 1,
  "name": "Nike",
  "imageUrl": "https://example.com/images/nike-logo.png",
  "createdAt": "2024-12-24T10:30:00",
  "status": "active"
}
```

**Error Responses:**
- `400 Bad Request` - Validation failed
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Not ADMIN role

---

### 3. **PUT** `/api/brands/{id}` - Update Brand

Update an existing brand.

**Path Parameter:**
- `id` (integer) - Brand ID

**Request Body:**
```json
{
  "name": "Nike Updated",
  "imageUrl": "https://example.com/images/nike-new-logo.png",
  "status": "active"
}
```

**Note:** All fields are optional. Only provided fields will be updated.

**Response 200 OK:**
```json
{
  "id": 1,
  "name": "Nike Updated",
  "imageUrl": "https://example.com/images/nike-new-logo.png",
  "createdAt": "2024-12-24T10:30:00",
  "status": "active"
}
```

**Error Responses:**
- `404 Not Found` - Brand not found or already deleted
- `400 Bad Request` - Invalid data
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Not ADMIN role

---

### 4. **DELETE** `/api/brands/{id}` - Delete Brand (Soft Delete)

Soft delete a brand by setting status to "DELETED".

**Path Parameter:**
- `id` (integer) - Brand ID

**Response 200 OK:**
```json
{
  "message": "Brand deleted successfully"
}
```

**Error Responses:**
- `404 Not Found` - Brand not found or already deleted
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Not ADMIN role

---

## 🔍 Search & Filter Examples

### Search by Name
```bash
GET /api/brands?search=Nike
# Returns brands with "Nike" in name (case-insensitive)
```

### Filter by Status
```bash
GET /api/brands?status=active
# Returns only active brands
```

### Combined Search + Filter
```bash
GET /api/brands?search=Sam&status=active
# Returns active brands with "Sam" in name (e.g., Samsung)
```

### Sorting
```bash
# Sort by name (A-Z)
GET /api/brands?sortBy=name&sortDir=asc

# Sort by creation date (newest first)
GET /api/brands?sortBy=createdAt&sortDir=desc
```

---

## 📊 Database Schema

**Table:** `brands`

| Column | Type | Description |
|--------|------|-------------|
| id | serial | Primary key |
| name | varchar(100) | Brand name (e.g., "Nike") |
| image_url | varchar(500) | Brand logo URL (optional) |
| created_at | timestamp | Auto-generated |
| status | varchar(20) | active/inactive/DELETED |

---

## 🎯 Common Use Cases

### 1. Get all active brands for dropdown
```bash
GET /api/brands?status=active&size=100&sortBy=name&sortDir=asc
```

### 2. Search brands while typing
```bash
GET /api/brands?search={userInput}
```

### 3. Create brand with logo
```bash
POST /api/brands
{
  "name": "Nike",
  "imageUrl": "https://cdn.example.com/nike-logo.png"
}
```

### 4. Create brand without logo
```bash
POST /api/brands
{
  "name": "Generic Brand"
}
```

### 5. Update only brand logo
```bash
PUT /api/brands/1
{
  "imageUrl": "https://cdn.example.com/new-logo.png"
}
```

---

## 🚨 Important Notes

1. **Soft Delete:** Brands are never physically deleted, only marked as "DELETED"
2. **Image URL:** Optional field - can create brands without images
3. **ADMIN Only:** All endpoints require ADMIN role
4. **Search:** Case-insensitive search on brand name
5. **No Product Check:** Unlike Units, brands can be deleted even if used by products (soft delete only)

---

## 📁 Project Structure

```
src/main/java/com/example/pos/
├── entity/
│   └── Brand.java                   # JPA Entity
├── repository/
│   └── BrandRepository.java         # Data access with custom queries
├── dto/
│   ├── BrandDTO.java                # Response DTO
│   ├── CreateBrandRequest.java      # Create request
│   ├── UpdateBrandRequest.java      # Update request
│   └── BrandListResponse.java       # List response with pagination
├── service/
│   └── BrandService.java            # Business logic
└── controller/
    └── BrandController.java         # REST endpoints
```

---

## 🎉 Implementation Complete!

All CRUD operations for Brands are now fully implemented and ready to use.

