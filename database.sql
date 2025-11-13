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

create index idx_categories_status
    on categories (status);

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
    status      varchar(20) default 'active'::character varying,
    image_url   varchar(500)
);

comment on column sub_categories.image_url is 'Sub category image URL';

alter table sub_categories
    owner to postgres;

create index idx_sub_categories_category_id
    on sub_categories (category_id);

create index idx_sub_categories_status
    on sub_categories (status);

create table brands
(
    id         serial
        primary key,
    name       varchar(100) not null,
    created_at timestamp   default CURRENT_TIMESTAMP,
    status     varchar(20) default 'active'::character varying,
    image_url  varchar(500)
);

comment on column brands.image_url is 'Brand logo/image URL';

alter table brands
    owner to postgres;

create index idx_brands_status
    on brands (status);

create table units
(
    id             serial
        primary key,
    name           varchar(50) not null,
    short_name     varchar(10),
    created_at     timestamp   default CURRENT_TIMESTAMP,
    status         varchar(20) default 'active'::character varying,
    no_of_products integer     default 0
);

comment on column units.no_of_products is 'Number of products using this unit';

alter table units
    owner to postgres;

create table products
(
    id                serial
        primary key,
    sku               varchar(20)
        unique,
    name              varchar(150)   not null,
    category          varchar(100),
    brand             varchar(100),
    price             numeric(10, 2) not null,
    quantity          integer        default 0,
    total_ordered     integer        default 0,
    revenue           numeric(10, 2) default 0,
    status            varchar(20)    default 'active'::character varying,
    unit_id           integer
        references units,
    sub_category_id   integer
        references sub_categories,
    created_by        integer
        references users,
    image_url         varchar(500),
    manufactured_date date,
    expired_date      date,
    qty_alert         integer        default 10,
    created_at        timestamp      default CURRENT_TIMESTAMP,
    updated_at        timestamp      default CURRENT_TIMESTAMP
);

comment on column products.unit_id is 'Foreign key to units table';

comment on column products.sub_category_id is 'Foreign key to sub_categories table';

comment on column products.created_by is 'User who created this product';

comment on column products.image_url is 'Product image URL';

comment on column products.manufactured_date is 'Product manufacturing date';

comment on column products.expired_date is 'Product expiration date';

comment on column products.qty_alert is 'Minimum quantity threshold for low stock alert';

alter table products
    owner to postgres;

create index idx_products_unit_id
    on products (unit_id);

create index idx_products_sub_category_id
    on products (sub_category_id);

create index idx_products_created_by
    on products (created_by);

create index idx_products_expired_date
    on products (expired_date);

create index idx_products_category
    on products (category);

create index idx_products_brand
    on products (brand);

create index idx_products_status
    on products (status);

create trigger trg_update_unit_product_count
    after insert or update or delete
    on products
    for each row
execute procedure update_unit_product_count();

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

create index idx_units_status
    on units (status);

create table variant_attributes
(
    id         serial
        primary key,
    name       varchar(100) not null,
    values     text[],
    created_at timestamp   default CURRENT_TIMESTAMP,
    status     varchar(20) default 'active'::character varying,
    image_url  varchar(500)
);

comment on column variant_attributes.image_url is 'Image URL for variant attribute';

alter table variant_attributes
    owner to postgres;

create index idx_variant_attributes_status
    on variant_attributes (status);

create table warranties
(
    id            serial
        primary key,
    name          varchar(100) not null,
    description   text,
    duration      integer,
    status        varchar(20) default 'active'::character varying,
    duration_unit varchar(20) default 'months'::character varying
        constraint warranties_duration_unit_check
            check ((duration_unit)::text = ANY
                   ((ARRAY ['months'::character varying, 'years'::character varying])::text[]))
);

comment on column warranties.duration_unit is 'Unit of warranty duration: months or years';

alter table warranties
    owner to postgres;

create index idx_warranties_status
    on warranties (status);

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

create table stores
(
    id             serial
        primary key,
    code           varchar(20)
        unique,
    name           varchar(150) not null,
    user_name      varchar(100),
    email          varchar(150) not null
        unique,
    phone          varchar(20),
    address        text,
    city           varchar(100),
    country        varchar(100),
    warehouse_id   integer
        references warehouses,
    user_id        integer
        references users,
    total_products integer     default 0,
    total_stock    integer     default 0,
    status         varchar(20) default 'active'::character varying
        constraint stores_status_check
            check ((status)::text = ANY
                   ((ARRAY ['active'::character varying, 'inactive'::character varying, 'DELETED'::character varying])::text[])),
    created_at     timestamp   default CURRENT_TIMESTAMP,
    updated_at     timestamp   default CURRENT_TIMESTAMP
);

alter table stores
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
        references users,
    store_id     integer
        references stores,
    qty_alert    integer   default 10
);

comment on column manage_stock.qty_alert is 'Low stock alert threshold for this warehouse/store';

alter table manage_stock
    owner to postgres;

create index idx_manage_stock_store_id
    on manage_stock (store_id);

create index idx_stores_status
    on stores (status);

create index idx_stores_user_id
    on stores (user_id);

create index idx_stores_warehouse_id
    on stores (warehouse_id);

