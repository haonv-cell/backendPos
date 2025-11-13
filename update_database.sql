-- =====================================================
-- UPDATE DATABASE SCHEMA FOR POS SYSTEM
-- Bổ sung các cột còn thiếu để đáp ứng UI requirements
-- =====================================================

-- 1. UPDATE WARRANTIES TABLE
-- Thêm cột duration_unit để phân biệt Year/Months
ALTER TABLE warranties
ADD COLUMN IF NOT EXISTS duration_unit VARCHAR(20) DEFAULT 'months'
CHECK (duration_unit IN ('months', 'years'));

COMMENT ON COLUMN warranties.duration_unit IS 'Unit of warranty duration: months or years';


-- 2. UPDATE VARIANT_ATTRIBUTES TABLE
-- Thêm cột image cho variant attributes
ALTER TABLE variant_attributes
ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

COMMENT ON COLUMN variant_attributes.image_url IS 'Image URL for variant attribute';


-- 3. UPDATE UNITS TABLE
-- Thêm cột no_of_products để track số lượng sản phẩm sử dụng unit này
ALTER TABLE units
ADD COLUMN IF NOT EXISTS no_of_products INTEGER DEFAULT 0;

COMMENT ON COLUMN units.no_of_products IS 'Number of products using this unit';


-- 4. UPDATE BRANDS TABLE
-- Thêm cột image cho brands
ALTER TABLE brands
ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

COMMENT ON COLUMN brands.image_url IS 'Brand logo/image URL';


-- 5. UPDATE SUB_CATEGORIES TABLE
-- Thêm cột image cho sub categories
ALTER TABLE sub_categories
ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

COMMENT ON COLUMN sub_categories.image_url IS 'Sub category image URL';


-- 6. UPDATE PRODUCTS TABLE
-- Thêm các cột còn thiếu cho products
ALTER TABLE products
ADD COLUMN IF NOT EXISTS unit_id INTEGER REFERENCES units(id),
ADD COLUMN IF NOT EXISTS sub_category_id INTEGER REFERENCES sub_categories(id),
ADD COLUMN IF NOT EXISTS created_by INTEGER REFERENCES users(id),
ADD COLUMN IF NOT EXISTS image_url VARCHAR(500),
ADD COLUMN IF NOT EXISTS manufactured_date DATE,
ADD COLUMN IF NOT EXISTS expired_date DATE,
ADD COLUMN IF NOT EXISTS qty_alert INTEGER DEFAULT 10,
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

COMMENT ON COLUMN products.unit_id IS 'Foreign key to units table';
COMMENT ON COLUMN products.sub_category_id IS 'Foreign key to sub_categories table';
COMMENT ON COLUMN products.created_by IS 'User who created this product';
COMMENT ON COLUMN products.image_url IS 'Product image URL';
COMMENT ON COLUMN products.manufactured_date IS 'Product manufacturing date';
COMMENT ON COLUMN products.expired_date IS 'Product expiration date';
COMMENT ON COLUMN products.qty_alert IS 'Minimum quantity threshold for low stock alert';


-- 7. UPDATE MANAGE_STOCK TABLE
-- Thêm cột qty_alert cho manage_stock
ALTER TABLE manage_stock
ADD COLUMN IF NOT EXISTS qty_alert INTEGER DEFAULT 10;

COMMENT ON COLUMN manage_stock.qty_alert IS 'Low stock alert threshold for this warehouse/store';


-- 8. CREATE INDEXES FOR BETTER PERFORMANCE
-- Index cho products
CREATE INDEX IF NOT EXISTS idx_products_unit_id ON products(unit_id);
CREATE INDEX IF NOT EXISTS idx_products_sub_category_id ON products(sub_category_id);
CREATE INDEX IF NOT EXISTS idx_products_created_by ON products(created_by);
CREATE INDEX IF NOT EXISTS idx_products_expired_date ON products(expired_date);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_brand ON products(brand);
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);

-- Index cho warranties
CREATE INDEX IF NOT EXISTS idx_warranties_status ON warranties(status);

-- Index cho variant_attributes
CREATE INDEX IF NOT EXISTS idx_variant_attributes_status ON variant_attributes(status);

-- Index cho units
CREATE INDEX IF NOT EXISTS idx_units_status ON units(status);

-- Index cho brands
CREATE INDEX IF NOT EXISTS idx_brands_status ON brands(status);

-- Index cho categories
CREATE INDEX IF NOT EXISTS idx_categories_status ON categories(status);

-- Index cho sub_categories
CREATE INDEX IF NOT EXISTS idx_sub_categories_category_id ON sub_categories(category_id);
CREATE INDEX IF NOT EXISTS idx_sub_categories_status ON sub_categories(status);


-- 9. CREATE VIEW FOR LOW STOCKS
-- View để dễ dàng query low stock products
CREATE OR REPLACE VIEW v_low_stocks AS
SELECT 
    w.name AS warehouse,
    s.name AS store,
    p.name AS product_name,
    p.category,
    p.sku,
    COALESCE(ms.quantity, p.quantity) AS qty,
    COALESCE(ms.qty_alert, p.qty_alert) AS qty_alert,
    p.id AS product_id,
    ms.warehouse_id,
    ms.store_id
FROM products p
LEFT JOIN manage_stock ms ON p.id = ms.product_id
LEFT JOIN warehouses w ON ms.warehouse_id = w.id
LEFT JOIN stores s ON ms.store_id = s.id
WHERE COALESCE(ms.quantity, p.quantity) <= COALESCE(ms.qty_alert, p.qty_alert)
  AND p.status = 'active';

COMMENT ON VIEW v_low_stocks IS 'View for products with low stock levels';


-- 10. CREATE VIEW FOR EXPIRED PRODUCTS
-- View để dễ dàng query expired products
CREATE OR REPLACE VIEW v_expired_products AS
SELECT 
    p.sku,
    p.name AS product_name,
    p.manufactured_date,
    p.expired_date,
    p.id AS product_id,
    p.category,
    p.brand,
    p.quantity
FROM products p
WHERE p.expired_date IS NOT NULL 
  AND p.expired_date <= CURRENT_DATE
  AND p.status = 'active'
ORDER BY p.expired_date ASC;

COMMENT ON VIEW v_expired_products IS 'View for expired or expiring products';


-- 11. CREATE TRIGGER TO UPDATE no_of_products IN UNITS TABLE
-- Trigger để tự động cập nhật số lượng products sử dụng unit
CREATE OR REPLACE FUNCTION update_unit_product_count()
RETURNS TRIGGER AS $$
BEGIN
    -- Decrease count for old unit
    IF TG_OP = 'UPDATE' AND OLD.unit_id IS NOT NULL AND OLD.unit_id != NEW.unit_id THEN
        UPDATE units SET no_of_products = no_of_products - 1 WHERE id = OLD.unit_id;
    END IF;
    
    IF TG_OP = 'DELETE' AND OLD.unit_id IS NOT NULL THEN
        UPDATE units SET no_of_products = no_of_products - 1 WHERE id = OLD.unit_id;
        RETURN OLD;
    END IF;
    
    -- Increase count for new unit
    IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') AND NEW.unit_id IS NOT NULL THEN
        UPDATE units SET no_of_products = no_of_products + 1 WHERE id = NEW.unit_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Drop trigger if exists and create new one
DROP TRIGGER IF EXISTS trg_update_unit_product_count ON products;
CREATE TRIGGER trg_update_unit_product_count
AFTER INSERT OR UPDATE OR DELETE ON products
FOR EACH ROW
EXECUTE FUNCTION update_unit_product_count();


-- =====================================================
-- END OF UPDATE SCRIPT
-- =====================================================

