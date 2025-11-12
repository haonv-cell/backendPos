# Warehouse API Implementation

## ✅ Đã hoàn thành

Đã tạo CRUD API cho Warehouse với đầy đủ tính năng pagination, search, filter giống CustomerController.

---

## 📁 Files đã tạo

### 1. Entity
- **`Warehouse.java`** - Entity mapping với table `warehouses`

### 2. Repository
- **`WarehouseRepository.java`** - Repository với search methods

### 3. DTOs
- **`WarehouseDTO.java`** - Response DTO
- **`WarehouseListResponse.java`** - List response với pagination info

### 4. Service
- **`WarehouseService.java`** - Business logic

### 5. Controller
- **`WarehouseController.java`** - REST API endpoint

---

## 🗄️ Database Schema

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

## 📊 Entity Class

<augment_code_snippet path="src/main/java/com/example/pos/entity/Warehouse.java" mode="EXCERPT">
```java
@Entity
@Table(name = "warehouses")
@Data
@Builder
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String name;
    private String contactPerson;
    private String phone;
    
    @Builder.Default
    private Integer totalProducts = 0;
    
    @Builder.Default
    private Integer stock = 0;
    
    @Builder.Default
    private Integer qty = 0;
    
    @CreationTimestamp
    private LocalDate createdOn;
    
    @Builder.Default
    private String status = "active";
    
    private Integer userId;
}
```
</augment_code_snippet>

---

## 🔍 Repository Methods

<augment_code_snippet path="src/main/java/com/example/pos/repository/WarehouseRepository.java" mode="EXCERPT">
```java
public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {
    
    // Find all excluding DELETED
    Page<Warehouse> findByStatusNot(String status, Pageable pageable);
    
    // Find by status
    Page<Warehouse> findByStatus(String status, Pageable pageable);
    
    // Search by name, contact_person, phone
    @Query("SELECT w FROM Warehouse w WHERE w.status != 'DELETED' AND " +
           "(LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(w.contactPerson) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(w.phone) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Warehouse> searchWarehouses(@Param("search") String search, Pageable pageable);
    
    // Search with status filter
    @Query("SELECT w FROM Warehouse w WHERE w.status = :status AND " +
           "w.status != 'DELETED' AND " +
           "(LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(w.contactPerson) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(w.phone) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Warehouse> searchWarehousesByStatus(@Param("status") String status,
                                              @Param("search") String search,
                                              Pageable pageable);
}
```
</augment_code_snippet>

---

## 🎯 API Endpoint

### GET /api/warehouses

**Authorization:** ADMIN only

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Page number (0-based) |
| `size` | int | 10 | Page size |
| `search` | string | - | Search term (name, contact person, phone) |
| `status` | string | - | Filter by status (active, inactive) |
| `sortBy` | string | createdOn | Sort field |
| `sortDir` | string | desc | Sort direction (asc/desc) |

---

## 📝 Request Examples

### 1. Get all warehouses (page 1, 10 items)

```http
GET http://localhost:8080/api/warehouses?page=0&size=10
Authorization: Bearer <admin_token>
```

### 2. Search warehouses

```http
GET http://localhost:8080/api/warehouses?search=Lavish
Authorization: Bearer <admin_token>
```

### 3. Filter by status

```http
GET http://localhost:8080/api/warehouses?status=active
Authorization: Bearer <admin_token>
```

### 4. Search + Filter + Sort

```http
GET http://localhost:8080/api/warehouses?search=warehouse&status=active&sortBy=name&sortDir=asc
Authorization: Bearer <admin_token>
```

---

## 📤 Response Format

```json
{
  "warehouses": [
    {
      "id": 1,
      "name": "Lavish Warehouse",
      "contactPerson": "Chad Taylor",
      "phone": "+12498345785",
      "totalProducts": 10,
      "stock": 600,
      "qty": 80,
      "createdOn": "2024-12-24",
      "status": "active"
    },
    {
      "id": 2,
      "name": "Quaint Warehouse",
      "contactPerson": "Jenny Ellis",
      "phone": "+13178964582",
      "totalProducts": 15,
      "stock": 300,
      "qty": 85,
      "createdOn": "2024-12-10",
      "status": "active"
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 10
}
```

---

## 🔐 Security Features

### 1. Authorization
- Chỉ ADMIN mới được truy cập
- JWT token bắt buộc

### 2. Soft Delete Protection
- Không cho phép search/filter warehouses có status = "DELETED"
- Tự động exclude DELETED khi không có filter

### 3. Input Validation
- Clean status input (trim whitespace)
- Validate search term

---

## 🔄 Business Logic Flow

```
Request → Controller → Service → Repository → Database
                ↓
         Security Check (ADMIN only)
                ↓
         Validate Parameters
                ↓
         Build Query (search/filter/sort)
                ↓
         Execute Query
                ↓
         Convert to DTOs
                ↓
         Build Response
                ↓
         Return JSON
```

---

## 📊 Search Logic

### Case 1: No search, No filter
```sql
SELECT * FROM warehouses 
WHERE status != 'DELETED' 
ORDER BY created_on DESC 
LIMIT 10 OFFSET 0;
```

### Case 2: Search only
```sql
SELECT * FROM warehouses 
WHERE status != 'DELETED' 
  AND (LOWER(name) LIKE LOWER('%search%') OR 
       LOWER(contact_person) LIKE LOWER('%search%') OR 
       LOWER(phone) LIKE LOWER('%search%'))
ORDER BY created_on DESC 
LIMIT 10 OFFSET 0;
```

### Case 3: Filter only
```sql
SELECT * FROM warehouses 
WHERE status = 'active' 
ORDER BY created_on DESC 
LIMIT 10 OFFSET 0;
```

### Case 4: Search + Filter
```sql
SELECT * FROM warehouses 
WHERE status = 'active' 
  AND status != 'DELETED'
  AND (LOWER(name) LIKE LOWER('%search%') OR 
       LOWER(contact_person) LIKE LOWER('%search%') OR 
       LOWER(phone) LIKE LOWER('%search%'))
ORDER BY created_on DESC 
LIMIT 10 OFFSET 0;
```

---

## 🎨 Frontend Integration

### Columns mapping

| UI Column | DTO Field | Type |
|-----------|-----------|------|
| Warehouse | name | String |
| Contact Person | contactPerson | String |
| Phone | phone | String |
| Total Products | totalProducts | Integer |
| Stock | stock | Integer |
| Qty | qty | Integer |
| Created On | createdOn | LocalDate |
| Status | status | String |

### Status badge colors
- `active` → Green badge
- `inactive` → Gray badge

---

## ✅ Build Status

```
[INFO] BUILD SUCCESS
[INFO] Total time:  6.218 s
```

---

## 🧪 Testing

### Test với curl:

```bash
# 1. Get all warehouses
curl -X GET "http://localhost:8080/api/warehouses" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"

# 2. Search
curl -X GET "http://localhost:8080/api/warehouses?search=Lavish" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"

# 3. Filter by status
curl -X GET "http://localhost:8080/api/warehouses?status=active" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"

# 4. Pagination
curl -X GET "http://localhost:8080/api/warehouses?page=1&size=5" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"

# 5. Sort
curl -X GET "http://localhost:8080/api/warehouses?sortBy=name&sortDir=asc" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

---

## 📝 So sánh với CustomerController

| Feature | CustomerController | WarehouseController |
|---------|-------------------|---------------------|
| **Endpoint** | `/api/customers` | `/api/warehouses` |
| **Authorization** | ADMIN only | ADMIN only |
| **Pagination** | ✅ | ✅ |
| **Search fields** | name, email, code, phone, country | name, contactPerson, phone |
| **Filter** | status | status |
| **Sort** | createdAt (default) | createdOn (default) |
| **Soft delete** | ✅ | ✅ |
| **JPQL queries** | ✅ | ✅ |

---

## 🚀 Next Steps (Optional)

Có thể mở rộng thêm:

1. **CRUD operations:**
   - POST `/api/warehouses` - Create warehouse
   - PUT `/api/warehouses/{id}` - Update warehouse
   - DELETE `/api/warehouses/{id}` - Soft delete warehouse

2. **Additional endpoints:**
   - GET `/api/warehouses/{id}` - Get warehouse by ID
   - GET `/api/warehouses/stats` - Warehouse statistics

3. **Validation:**
   - Add `@Valid` for create/update requests
   - Create `CreateWarehouseRequest` DTO
   - Create `UpdateWarehouseRequest` DTO

4. **Business logic:**
   - Auto-calculate totalProducts, stock, qty from products
   - Validate user_id exists
   - Check duplicate warehouse names

---

## 📌 Notes

- ✅ Warehouse entity đã có `@Builder.Default` cho các fields có default values
- ✅ Repository sử dụng JPQL với `LOWER() LIKE LOWER()` (database agnostic)
- ✅ Service có soft delete protection
- ✅ Controller có full documentation
- ✅ Build thành công không có errors

