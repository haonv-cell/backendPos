# 📂 Category API Documentation

## Overview
Complete CRUD API for managing product categories (Electronics, Clothing, Food, etc.) in the POS system.

---

## 🔐 Authentication
All endpoints require **ADMIN** role authentication.

**Header:**
```
Authorization: Bearer {JWT_TOKEN}
```

---

## 📋 API Endpoints

### 1. **GET** `/api/categories` - Get All Categories

Get paginated list of categories with optional search and filtering.

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Page number (0-based) |
| `size` | int | 10 | Items per page |
| `search` | string | - | Search by name or slug |
| `status` | string | - | Filter by status (active/inactive) |
| `sortBy` | string | createdAt | Sort field (name, slug, createdAt) |
| `sortDir` | string | desc | Sort direction (asc/desc) |

**Response 200 OK:**
```json
{
  "categories": [
    {
      "id": 1,
      "name": "Electronics",
      "slug": "electronics",
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
# Get all categories (default pagination)
GET /api/categories

# Search for "Electronics"
GET /api/categories?search=Electronics

# Get active categories only
GET /api/categories?status=active

# Sort by name ascending
GET /api/categories?sortBy=name&sortDir=asc

# Page 2 with 20 items
GET /api/categories?page=1&size=20
```

---

### 2. **POST** `/api/categories` - Create New Category

Create a new category with auto-generated or custom slug.

**Request Body:**
```json
{
  "name": "Electronics",
  "slug": "electronics"
}
```

**Validation Rules:**
- `name`: Required, not blank
- `slug`: Optional, must match pattern `^[a-z0-9-]*$` (lowercase, numbers, hyphens only)
- If `slug` is not provided, it will be auto-generated from `name`

**Response 201 Created:**
```json
{
  "id": 1,
  "name": "Electronics",
  "slug": "electronics",
  "createdAt": "2024-12-24T10:30:00",
  "status": "active"
}
```

**Auto-Slug Generation Examples:**
- `"Electronics"` → `"electronics"`
- `"Food & Beverages"` → `"food-beverages"`
- `"Home, Garden & Furniture"` → `"home-garden-furniture"`

**Error Responses:**
- `400 Bad Request` - Validation failed
- `409 Conflict` - Slug already exists
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Not ADMIN role

---

### 3. **PUT** `/api/categories/{id}` - Update Category

Update an existing category.

**Path Parameter:**
- `id` (integer) - Category ID

**Request Body:**
```json
{
  "name": "Electronics & Gadgets",
  "slug": "electronics-gadgets",
  "status": "active"
}
```

**Note:** All fields are optional. Only provided fields will be updated.

**Response 200 OK:**
```json
{
  "id": 1,
  "name": "Electronics & Gadgets",
  "slug": "electronics-gadgets",
  "createdAt": "2024-12-24T10:30:00",
  "status": "active"
}
```

**Error Responses:**
- `404 Not Found` - Category not found or already deleted
- `409 Conflict` - New slug already exists
- `400 Bad Request` - Invalid data
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Not ADMIN role

---

### 4. **DELETE** `/api/categories/{id}` - Delete Category (Soft Delete)

Soft delete a category by setting status to "DELETED".

**Path Parameter:**
- `id` (integer) - Category ID

**Response 200 OK:**
```json
{
  "message": "Category deleted successfully"
}
```

**Error Responses:**
- `404 Not Found` - Category not found or already deleted
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Not ADMIN role

---

## 🔍 Search & Filter Examples

### Search by Name or Slug
```bash
GET /api/categories?search=electronics
# Returns categories with "electronics" in name or slug
```

### Filter by Status
```bash
GET /api/categories?status=active
# Returns only active categories
```

### Combined Search + Filter
```bash
GET /api/categories?search=food&status=active
# Returns active categories with "food" in name or slug
```

### Sorting
```bash
# Sort by name (A-Z)
GET /api/categories?sortBy=name&sortDir=asc

# Sort by slug (A-Z)
GET /api/categories?sortBy=slug&sortDir=asc

# Sort by creation date (newest first)
GET /api/categories?sortBy=createdAt&sortDir=desc
```

---

## 📊 Database Schema

**Table:** `categories`

| Column | Type | Description |
|--------|------|-------------|
| id | serial | Primary key |
| name | varchar(100) | Category name (e.g., "Electronics") |
| slug | varchar(100) | URL-friendly slug (unique) |
| created_at | timestamp | Auto-generated |
| status | varchar(20) | active/inactive/DELETED |

---

## 🎯 Common Use Cases

### 1. Get all active categories for dropdown
```bash
GET /api/categories?status=active&size=100&sortBy=name&sortDir=asc
```

### 2. Search categories while typing
```bash
GET /api/categories?search={userInput}
```

### 3. Create category with auto-generated slug
```bash
POST /api/categories
{
  "name": "Food & Beverages"
}
# Slug will be auto-generated as "food-beverages"
```

### 4. Create category with custom slug
```bash
POST /api/categories
{
  "name": "Clothing & Fashion",
  "slug": "fashion"
}
```

### 5. Update only category name
```bash
PUT /api/categories/1
{
  "name": "Electronics & Gadgets"
}
```

### 6. Update only category status
```bash
PUT /api/categories/1
{
  "status": "inactive"
}
```

---

## 🚨 Important Notes

1. **Soft Delete:** Categories are never physically deleted, only marked as "DELETED"
2. **Unique Slug:** Each category must have a unique slug
3. **Auto-Slug:** If slug is not provided, it's auto-generated from name
4. **Slug Pattern:** Slug must contain only lowercase letters, numbers, and hyphens
5. **ADMIN Only:** All endpoints require ADMIN role
6. **Search:** Case-insensitive search on both name and slug

---

## 🔧 Slug Generation Rules

The auto-slug generation follows these rules:
1. Convert to lowercase
2. Remove special characters (except spaces and hyphens)
3. Replace spaces with hyphens
4. Remove consecutive hyphens
5. Trim whitespace

**Examples:**
- `"Electronics"` → `"electronics"`
- `"Food & Beverages"` → `"food-beverages"`
- `"Home, Garden & Furniture"` → `"home-garden-furniture"`
- `"Books   &   Media"` → `"books-media"`

---

## 📁 Project Structure

```
src/main/java/com/example/pos/
├── entity/
│   └── Category.java                # JPA Entity
├── repository/
│   └── CategoryRepository.java      # Data access with custom queries
├── dto/
│   ├── CategoryDTO.java             # Response DTO
│   ├── CreateCategoryRequest.java   # Create request
│   ├── UpdateCategoryRequest.java   # Update request
│   └── CategoryListResponse.java    # List response with pagination
├── service/
│   └── CategoryService.java         # Business logic
└── controller/
    └── CategoryController.java      # REST endpoints
```

---

## 🎉 Implementation Complete!

All CRUD operations for Categories are now fully implemented and ready to use.

