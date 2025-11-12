create table users
(
    id             serial
        primary key,
    code           varchar(20)
        unique,
    name           varchar(100) not null,
    email          varchar(150) not null
        unique,
    phone          varchar(20),
    country        varchar(100),
    company_name   varchar(150),
    password_hash  varchar(255) not null,
    role           varchar(50)
        constraint users_role_check
            check (lower((role)::text) = ANY
                   (ARRAY ['admin'::text, 'biller'::text, 'supplier'::text, 'store_owner'::text, 'customer'::text])),
    status         varchar(20) default 'active'::character varying,
    created_at     timestamp   default CURRENT_TIMESTAMP,
    updated_at     timestamp   default CURRENT_TIMESTAMP,
    email_verified boolean,
    image_url      varchar(500),
    provider       varchar(50)
        constraint users_provider_check
            check (lower((provider)::text) = ANY (ARRAY ['local'::text, 'google'::text, 'facebook'::text])),
    provider_id    varchar(255)
);

alter table users
    owner to postgres;

create table warehouses
(
    id             serial
        primary key,
    name           varchar(150) not null,
    contact_person varchar(100),
    phone          varchar(20),
    total_products integer     default 0,
    stock          integer     default 0,
    qty            integer     default 0,
    created_on     date        default CURRENT_DATE,
    status         varchar(20) default 'active'::character varying,
    user_id        integer
        references users
);

alter table warehouses
    owner to postgres;

create table products
(
    id            serial
        primary key,
    sku           varchar(20)
        unique,
    name          varchar(150)   not null,
    category      varchar(100),
    brand         varchar(100),
    price         numeric(10, 2) not null,
    quantity      integer        default 0,
    total_ordered integer        default 0,
    revenue       numeric(10, 2) default 0,
    status        varchar(20)    default 'active'::character varying
);

alter table products
    owner to postgres;

create table sales
(
    id           serial
        primary key,
    product_id   integer
        references products,
    warehouse_id integer
        references warehouses,
    sold_qty     integer        default 0,
    sold_amount  numeric(10, 2) default 0,
    date         timestamp      default CURRENT_TIMESTAMP
);

alter table sales
    owner to postgres;

create table purchases
(
    id              serial
        primary key,
    product_id      integer
        references products,
    warehouse_id    integer
        references warehouses,
    purchase_qty    integer        default 0,
    purchase_amount numeric(10, 2) default 0,
    purchase_date   timestamp      default CURRENT_TIMESTAMP
);

alter table purchases
    owner to postgres;

create table invoices
(
    id             serial
        primary key,
    invoice_number varchar(50)    not null
        unique,
    customer_id    integer
        references users,
    total_amount   numeric(10, 2) not null,
    paid_amount    numeric(10, 2) default 0,
    amount_due     numeric(10, 2) default 0,
    due_date       timestamp,
    status         varchar(20)    default 'pending'::character varying,
    created_at     timestamp      default CURRENT_TIMESTAMP
);

alter table invoices
    owner to postgres;

create table suppliers
(
    id            serial
        primary key,
    name          varchar(150) not null,
    contact_name  varchar(100),
    contact_email varchar(150),
    contact_phone varchar(20),
    address       text,
    created_at    timestamp   default CURRENT_TIMESTAMP,
    updated_at    timestamp   default CURRENT_TIMESTAMP,
    status        varchar(20) default 'active'::character varying
);

alter table suppliers
    owner to postgres;

create table supplier_reports
(
    id             serial
        primary key,
    reference      varchar(50)
        unique,
    supplier_id    integer
        references suppliers,
    total_items    integer,
    amount         numeric(10, 2),
    payment_method varchar(50),
    status         varchar(20) default 'pending'::character varying,
    report_date    timestamp   default CURRENT_TIMESTAMP
);

alter table supplier_reports
    owner to postgres;

create table manage_stock
(
    id           serial
        primary key,
    warehouse_id integer
        references warehouses,
    store_name   varchar(150),
    product_id   integer
        references products,
    quantity     integer   default 0,
    date         timestamp default CURRENT_TIMESTAMP,
    person_id    integer
        references users
);

alter table manage_stock
    owner to postgres;

create table stock_adjustment
(
    id              serial
        primary key,
    warehouse_id    integer
        references warehouses,
    product_id      integer
        references products,
    adjustment_qty  integer,
    adjustment_type varchar(20)
        constraint stock_adjustment_adjustment_type_check
            check ((adjustment_type)::text = ANY
        (ARRAY [('increase'::character varying)::text, ('decrease'::character varying)::text])),
    reason          text,
    date            timestamp default CURRENT_TIMESTAMP,
    adjusted_by     integer
        references users
);

alter table stock_adjustment
    owner to postgres;

create table stock_transfer
(
    id                   serial
        primary key,
    from_warehouse_id    integer
        references warehouses,
    to_warehouse_id      integer
        references warehouses,
    product_id           integer
        references products,
    quantity_transferred integer,
    reference_number     varchar(50) not null
        unique,
    transfer_date        timestamp default CURRENT_TIMESTAMP,
    transferred_by       integer
        references users
);

alter table stock_transfer
    owner to postgres;

create table categories
(
    id         serial
        primary key,
    name       varchar(100) not null,
    slug       varchar(100)
        unique,
    created_at timestamp   default CURRENT_TIMESTAMP,
    status     varchar(20) default 'active'::character varying
);

alter table categories
    owner to postgres;

create table sub_categories
(
    id          serial
        primary key,
    name        varchar(100) not null,
    category_id integer
        references categories,
    code        varchar(50)  not null
        unique,
    description text,
    created_at  timestamp   default CURRENT_TIMESTAMP,
    status      varchar(20) default 'active'::character varying
);

alter table sub_categories
    owner to postgres;

create table brands
(
    id         serial
        primary key,
    name       varchar(100) not null,
    created_at timestamp   default CURRENT_TIMESTAMP,
    status     varchar(20) default 'active'::character varying
);

alter table brands
    owner to postgres;

create table units
(
    id         serial
        primary key,
    name       varchar(50) not null,
    short_name varchar(10),
    created_at timestamp   default CURRENT_TIMESTAMP,
    status     varchar(20) default 'active'::character varying
);

alter table units
    owner to postgres;

create table variant_attributes
(
    id         serial
        primary key,
    name       varchar(100) not null,
    values     text[],
    created_at timestamp   default CURRENT_TIMESTAMP,
    status     varchar(20) default 'active'::character varying
);

alter table variant_attributes
    owner to postgres;

create table warranties
(
    id          serial
        primary key,
    name        varchar(100) not null,
    description text,
    duration    integer,
    status      varchar(20) default 'active'::character varying
);

alter table warranties
    owner to postgres;

create table product_warranties
(
    id          serial
        primary key,
    product_id  integer
        references products,
    warranty_id integer
        references warranties,
    created_at  timestamp default CURRENT_TIMESTAMP
);

alter table product_warranties
    owner to postgres;

create table invoice_items
(
    id         serial
        primary key,
    invoice_id integer
        references invoices,
    product_id integer
        references products,
    quantity   integer        not null,
    price      numeric(10, 2) not null,
    total      numeric(10, 2) not null,
    created_at timestamp default CURRENT_TIMESTAMP
);

alter table invoice_items
    owner to postgres;

create table password_reset_otps
(
    id         integer generated by default as identity
        primary key,
    created_at timestamp(6),
    expires_at timestamp(6) not null,
    is_used    boolean,
    otp        varchar(6)   not null,
    user_id    integer      not null
        constraint fk9c75odu4o05pvbvhn2n9ia0tj
            references users
);

alter table password_reset_otps
    owner to postgres;

yêu cầu:Chi tiết 4 Endpoints
1. CREATE (Thêm kho mới)
Endpoint: POST /api/warehouses

Input: WarehouseRequest

Validation:

name không được rỗng.

userId không được rỗng và phải tồn tại trong bảng users. Nếu không, trả về 400 Bad Request.

Logic:

Tạo entity Warehouse mới từ DTO.

Gán giá trị mặc định: status = 'active', totalProducts = 0, stock = 0.

Lưu vào database.

Response: 201 Created - Trả về WarehouseResponse của kho vừa tạo.

2. READ (Lấy danh sách kho - cho màn hình chính)
Endpoint: GET /api/warehouses

Parameters:

page (int): Số trang (cho phân trang).

size (int): Kích thước trang (cho phân trang).
search: name,Sting contactPerson, , stock (đọc từ DB)
sortBy (String): Tên cột để sắp xếp (ví dụ: stock, name, createdOn).

sortDir (String): asc hoặc desc.

status (String, optional): Lọc theo status (ví dụ: active, inactive).

Logic:

Xây dựng query (ví dụ: Specification hoặc JPQL).

Bắt buộc: Luôn JOIN với bảng users (u) qua warehouses.user_id = u.id để lấy u.name và gán vào managingUserName.

Bắt buộc: Luôn có điều kiện WHERE status != 'DELETE'.

Áp dụng các tham số lọc, sắp xếp, và phân trang.

Response: 200 OK - Trả về đối tượng Page<WarehouseResponse> (bao gồm danh sách kho và thông tin phân trang).

3. UPDATE (Cập nhật thông tin kho)
Endpoint: PUT /api/warehouses/{id}

Input: WarehouseRequest

Logic:

Tìm Warehouse theo {id}. Nếu không tìm thấy, trả về 404 Not Found.

Xác thực userId từ WarehouseRequest: nếu userId thay đổi, userId mới phải tồn tại trong bảng users. Nếu không, trả về 400 Bad Request.

Chỉ cập nhật các trường: name, contactPerson, phone, userId.

Cảnh báo: Không được phép cập nhật stock hoặc totalProducts từ API này.

Response: 200 OK - Trả về WarehouseResponse của kho vừa cập nhật.

4. DELETE (Xóa mềm kho)
Endpoint: DELETE /api/warehouses/{id}

Logic:

Tìm Warehouse theo {id}. Nếu không tìm thấy, trả về 404 Not Found.

Nếu status đã là 'DELETE', trả về 204 No Content.

Kiểm tra nghiệp vụ:

Kiểm tra warehouse.stock.

Nếu stock > 0, trả về 409 Conflict với message "Không thể xóa kho vì vẫn còn tồn hàng."

Nếu stock == 0 (an toàn để xóa):

Cập nhật status = 'DELETE'.

Response: 204 No Content.


Quy tắc nghiệp vụ cốt lõi (Bắt buộc):

Soft Delete (Xóa Mềm): Bảng warehouses có nhiều khóa ngoại tham chiếu đến. Do đó, DELETE API không được xóa vật lý. Thay vào đó, phải cập nhật status = 'DELETE'.

Trường Tổng hợp (Summary Fields): Các trường total_products và stock là dữ liệu tổng hợp, được tính toán từ các nghiệp vụ khác (như manage_stock, sales, purchases). API Warehouse (cả CREATE và UPDATE) không được phép cho người dùng tự ý điền hay thay đổi giá trị của các trường này.

Trường Bị Bỏ qua: Trường qty không rõ ràng và bị trùng lặp với stock. Bỏ qua trường này trong mọi logic (không đọc, không ghi).

Khóa ngoại user_id: Luôn phải xác thực user_id có tồn tại trong bảng users hay không.