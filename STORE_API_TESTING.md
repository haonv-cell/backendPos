# Store API Testing Documentation

Base URL: `http://localhost:8080/api/stores`

**Authentication Required**: All endpoints require ADMIN role
**Authorization Header**: `Authorization: Bearer <your_jwt_token>`

---

## 1. Get List of Stores (GET)

**Endpoint**: `GET /api/stores`

**Query Parameters**:
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 10) - Page size
- `search` (optional) - Search term for name, email, phone, or user name
- `status` (optional) - Filter by status
- `sortBy` (optional, default: createdAt) - Sort field
- `sortDir` (optional, default: desc) - Sort direction (asc/desc)

**Example Requests**:

```bash
# Get all stores (first page)
curl -X GET "http://localhost:8080/api/stores" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Get stores with pagination
curl -X GET "http://localhost:8080/api/stores?page=0&size=20" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Search stores
curl -X GET "http://localhost:8080/api/stores?search=store1" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Filter by status
curl -X GET "http://localhost:8080/api/stores?status=ACTIVE" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Sort by name ascending
curl -X GET "http://localhost:8080/api/stores?sortBy=name&sortDir=asc" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Combined filters
curl -X GET "http://localhost:8080/api/stores?page=0&size=10&search=hanoi&status=ACTIVE&sortBy=name&sortDir=asc" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response Example** (200 OK):
```json
{
  "stores": [
    {
      "id": 1,
      "code": "ST001",
      "name": "Store Hanoi",
      "userName": "John Doe",
      "email": "hanoi@store.com",
      "phone": "0123456789",
      "address": "123 Main St",
      "city": "Hanoi",
      "country": "Vietnam",
      "warehouseId": 1,
      "warehouseName": "Main Warehouse",
      "userId": 5,
      "totalProducts": 150,
      "totalStock": 5000,
      "status": "ACTIVE",
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 10
}
```

---

## 2. Create Store (POST)

**Endpoint**: `POST /api/stores`

**Request Body**:
```json
{
  "code": "ST001",
  "name": "Store Hanoi",
  "email": "hanoi@store.com",
  "phone": "0123456789",
  "address": "123 Main Street, District 1",
  "city": "Hanoi",
  "country": "Vietnam",
  "warehouseId": 1,
  "userId": 5
}
```

**Field Validations**:
- `code` (optional): Max 20 characters
- `name` (required): Max 150 characters
- `email` (required): Valid email format, max 150 characters
- `phone` (optional): Max 20 characters
- `address` (optional): Text
- `city` (optional): Max 100 characters
- `country` (optional): Max 100 characters
- `warehouseId` (required): Integer
- `userId` (required): Integer

**Example Request**:
```bash
curl -X POST "http://localhost:8080/api/stores" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "ST001",
    "name": "Store Hanoi",
    "email": "hanoi@store.com",
    "phone": "0123456789",
    "address": "123 Main Street, District 1",
    "city": "Hanoi",
    "country": "Vietnam",
    "warehouseId": 1,
    "userId": 5
  }'
```

**Response Example** (201 Created):
```json
{
  "id": 1,
  "code": "ST001",
  "name": "Store Hanoi",
  "userName": "John Doe",
  "email": "hanoi@store.com",
  "phone": "0123456789",
  "address": "123 Main Street, District 1",
  "city": "Hanoi",
  "country": "Vietnam",
  "warehouseId": 1,
  "warehouseName": "Main Warehouse",
  "userId": 5,
  "totalProducts": 0,
  "totalStock": 0,
  "status": "ACTIVE",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

---

## 3. Update Store (PUT)

**Endpoint**: `PUT /api/stores/{id}`

**Path Parameter**:
- `id` (required): Store ID

**Request Body** (all fields optional):
```json
{
  "code": "ST001-UPDATED",
  "name": "Store Hanoi Updated",
  "email": "hanoi.updated@store.com",
  "phone": "0987654321",
  "address": "456 New Street, District 2",
  "city": "Hanoi",
  "country": "Vietnam",
  "warehouseId": 2,
  "userId": 6
}
```

**Field Validations**:
- `code` (optional): Max 20 characters
- `name` (optional): Max 150 characters
- `email` (optional): Valid email format, max 150 characters
- `phone` (optional): Max 20 characters
- `address` (optional): Text
- `city` (optional): Max 100 characters
- `country` (optional): Max 100 characters
- `warehouseId` (optional): Integer
- `userId` (optional): Integer

**Example Request**:
```bash
curl -X PUT "http://localhost:8080/api/stores/1" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Store Hanoi Updated",
    "email": "hanoi.updated@store.com",
    "phone": "0987654321"
  }'
```

**Response Example** (200 OK):
```json
{
  "id": 1,
  "code": "ST001",
  "name": "Store Hanoi Updated",
  "userName": "Jane Smith",
  "email": "hanoi.updated@store.com",
  "phone": "0987654321",
  "address": "123 Main Street, District 1",
  "city": "Hanoi",
  "country": "Vietnam",
  "warehouseId": 1,
  "warehouseName": "Main Warehouse",
  "userId": 5,
  "totalProducts": 150,
  "totalStock": 5000,
  "status": "ACTIVE",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T14:20:00"
}
```

---

## 4. Delete Store (DELETE)

**Endpoint**: `DELETE /api/stores/{id}`

**Path Parameter**:
- `id` (required): Store ID

**Example Request**:
```bash
curl -X DELETE "http://localhost:8080/api/stores/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response Example** (200 OK):
```json
{
  "message": "Store deleted successfully"
}
```

---

## Testing with Postman

### 1. Setup Environment Variables
Create a new environment in Postman with:
- `base_url`: `http://localhost:8080`
- `token`: Your JWT token after login

### 2. Import Collection
Create a new collection "Store API" with the following requests:

#### Request 1: Get All Stores
- Method: GET
- URL: `{{base_url}}/api/stores`
- Headers: `Authorization: Bearer {{token}}`

#### Request 2: Get Stores with Filters
- Method: GET
- URL: `{{base_url}}/api/stores?page=0&size=10&search=hanoi&status=ACTIVE`
- Headers: `Authorization: Bearer {{token}}`

#### Request 3: Create Store
- Method: POST
- URL: `{{base_url}}/api/stores`
- Headers:
  - `Authorization: Bearer {{token}}`
  - `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "code": "ST001",
  "name": "Store Hanoi",
  "email": "hanoi@store.com",
  "phone": "0123456789",
  "address": "123 Main Street",
  "city": "Hanoi",
  "country": "Vietnam",
  "warehouseId": 1,
  "userId": 5
}
```

#### Request 4: Update Store
- Method: PUT
- URL: `{{base_url}}/api/stores/1`
- Headers:
  - `Authorization: Bearer {{token}}`
  - `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "name": "Store Hanoi Updated",
  "phone": "0987654321"
}
```

#### Request 5: Delete Store
- Method: DELETE
- URL: `{{base_url}}/api/stores/1`
- Headers: `Authorization: Bearer {{token}}`

---

## Common Error Responses

### 400 Bad Request - Validation Error
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": {
    "name": "Name is required",
    "email": "Email should be valid"
  }
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

### 404 Not Found
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Store not found with id: 1"
}
```

---

## Test Scenarios

### Scenario 1: Complete CRUD Flow
1. **Login** to get JWT token (ADMIN role required)
2. **Create** a new store
3. **Get** list of stores to verify creation
4. **Update** the store information
5. **Get** the store again to verify update
6. **Delete** the store
7. **Get** list to verify deletion (soft delete)

### Scenario 2: Validation Testing
1. Try creating store without required fields (name, email, warehouseId, userId)
2. Try creating store with invalid email format
3. Try creating store with fields exceeding max length
4. Verify appropriate error messages

### Scenario 3: Search and Filter Testing
1. Create multiple stores with different attributes
2. Test search by name
3. Test search by email
4. Test filter by status
5. Test pagination with different page sizes
6. Test sorting by different fields

### Scenario 4: Authorization Testing
1. Try accessing endpoints without token (should get 401)
2. Try accessing with non-ADMIN role token (should get 403)
3. Access with valid ADMIN token (should succeed)

---

## Notes
- All endpoints require ADMIN role authentication
- Store deletion is soft delete (status changed, not physically removed)
- Search works across name, email, phone, and user name fields
- Default sorting is by createdAt in descending order (newest first)

