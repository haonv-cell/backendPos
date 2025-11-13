# 📦 Unit API Documentation

## Overview
Complete CRUD API for managing product units (Kilograms, Liters, Pieces, etc.) in the POS system.

---

## 🔐 Authentication
All endpoints require **ADMIN** role authentication.

**Header:**
```
Authorization: Bearer {JWT_TOKEN}
```

---

## 📋 API Endpoints

### 1. **GET** `/api/units` - Get All Units

Get paginated list of units with optional search and filtering.

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Page number (0-based) |
| `size` | int | 10 | Items per page |
| `search` | string | - | Search by name or short name |
| `status` | string | - | Filter by status (active/inactive) |
| `sortBy` | string | createdAt | Sort field (name, createdAt, noOfProducts) |
| `sortDir` | string | desc | Sort direction (asc/desc) |

**Response 200 OK:**
```json
{
  "units": [
    {
      "id": 1,
      "name": "Kilograms",
      "shortName": "kg",
      "noOfProducts": 25,
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
# Get all units (default pagination)
GET /api/units

# Search for "kg"
GET /api/units?search=kg

# Get active units only
GET /api/units?status=active

# Sort by name ascending
GET /api/units?sortBy=name&sortDir=asc

# Page 2 with 20 items
GET /api/units?page=1&size=20
```

---

### 2. **POST** `/api/units` - Create New Unit

Create a new unit.

**Request Body:**
```json
{
  "name": "Kilograms",
  "shortName": "kg"
}
```

**Validation Rules:**
- `name`: Required, not blank
- `shortName`: Required, not blank

**Response 201 Created:**
```json
{
  "id": 1,
  "name": "Kilograms",
  "shortName": "kg",
  "noOfProducts": 0,
  "createdAt": "2024-12-24T10:30:00",
  "status": "active"
}
```

**Error Responses:**
- `400 Bad Request` - Validation failed
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Not ADMIN role

---

### 3. **PUT** `/api/units/{id}` - Update Unit

Update an existing unit.

**Path Parameter:**
- `id` (integer) - Unit ID

**Request Body:**
```json
{
  "name": "Kilograms Updated",
  "shortName": "kg",
  "status": "active"
}
```

**Note:** All fields are optional. Only provided fields will be updated.

**Response 200 OK:**
```json
{
  "id": 1,
  "name": "Kilograms Updated",
  "shortName": "kg",
  "noOfProducts": 25,
  "createdAt": "2024-12-24T10:30:00",
  "status": "active"
}
```

**Error Responses:**
- `404 Not Found` - Unit not found or already deleted
- `400 Bad Request` - Invalid data
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Not ADMIN role

---

### 4. **DELETE** `/api/units/{id}` - Delete Unit (Soft Delete)

Soft delete a unit by setting status to "DELETED".

**Path Parameter:**
- `id` (integer) - Unit ID

**Response 200 OK:**
```json
{
  "message": "Unit deleted successfully"
}
```

**Error Responses:**
- `404 Not Found` - Unit not found or already deleted
- `409 Conflict` - Unit is being used by products
  ```json
  {
    "message": "Cannot delete unit because it is being used by 25 product(s)"
  }
  ```
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Not ADMIN role

---

## 🔍 Search & Filter Examples

### Search by Name or Short Name
```bash
GET /api/units?search=kg
# Returns units with "kg" in name or shortName
```

### Filter by Status
```bash
GET /api/units?status=active
# Returns only active units
```

### Combined Search + Filter
```bash
GET /api/units?search=kg&status=active
# Returns active units with "kg" in name or shortName
```

### Sorting
```bash
# Sort by name (A-Z)
GET /api/units?sortBy=name&sortDir=asc

# Sort by number of products (high to low)
GET /api/units?sortBy=noOfProducts&sortDir=desc

# Sort by creation date (newest first)
GET /api/units?sortBy=createdAt&sortDir=desc
```

---

## 📊 Database Schema

**Table:** `units`

| Column | Type | Description |
|--------|------|-------------|
| id | serial | Primary key |
| name | varchar(50) | Unit name (e.g., "Kilograms") |
| short_name | varchar(10) | Short name (e.g., "kg") |
| no_of_products | integer | Auto-updated by trigger |
| created_at | timestamp | Auto-generated |
| status | varchar(20) | active/inactive/DELETED |

---

## 🔄 Auto-Update Trigger

The `no_of_products` field is **automatically updated** by a database trigger when:
- A product is created with this unit
- A product's unit is changed
- A product is deleted

**You don't need to manually update this field.**

---

## ✅ Testing

Use the provided `test-unit-api.http` file to test all endpoints.

**Steps:**
1. Login to get JWT token
2. Replace `{{token}}` in the file with your actual token
3. Run the requests in order

---

## 🎯 Common Use Cases

### 1. Get all active units for dropdown
```bash
GET /api/units?status=active&size=100
```

### 2. Search units while typing
```bash
GET /api/units?search={userInput}
```

### 3. Check if unit can be deleted
```bash
GET /api/units/{id}
# Check noOfProducts field
# If > 0, cannot delete
```

### 4. Create standard units
```bash
POST /api/units
{
  "name": "Kilograms",
  "shortName": "kg"
}
```

---

## 🚨 Important Notes

1. **Soft Delete:** Units are never physically deleted, only marked as "DELETED"
2. **Protection:** Cannot delete units that are being used by products
3. **Auto-Count:** `noOfProducts` is automatically maintained by database trigger
4. **ADMIN Only:** All endpoints require ADMIN role
5. **Search:** Case-insensitive search on both name and shortName

---

## 📁 Project Structure

```
src/main/java/com/example/pos/
├── entity/
│   └── Unit.java                    # JPA Entity
├── repository/
│   └── UnitRepository.java          # Data access with custom queries
├── dto/
│   ├── UnitDTO.java                 # Response DTO
│   ├── CreateUnitRequest.java       # Create request
│   ├── UpdateUnitRequest.java       # Update request
│   └── UnitListResponse.java        # List response with pagination
├── service/
│   └── UnitService.java             # Business logic
└── controller/
    └── UnitController.java          # REST endpoints
```

---

## 🎉 Implementation Complete!

All CRUD operations for Units are now fully implemented and ready to use.

