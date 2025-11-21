ALTER TABLE products ADD COLUMN IF NOT EXISTS slug VARCHAR(150);
CREATE UNIQUE INDEX IF NOT EXISTS uk_products_slug ON products(slug);

ALTER TABLE products ADD COLUMN IF NOT EXISTS category_id INTEGER REFERENCES categories(id);
ALTER TABLE products ADD COLUMN IF NOT EXISTS brand_id INTEGER REFERENCES brands(id);
UPDATE products p SET category_id = c.id FROM categories c WHERE p.category_id IS NULL AND p.category IS NOT NULL AND LOWER(c.name) = LOWER(p.category);
UPDATE products p SET brand_id = b.id FROM brands b WHERE p.brand_id IS NULL AND p.brand IS NOT NULL AND LOWER(b.name) = LOWER(p.brand);

ALTER TABLE products ADD COLUMN IF NOT EXISTS store_id INTEGER REFERENCES stores(id);
ALTER TABLE products ADD COLUMN IF NOT EXISTS warehouse_id INTEGER REFERENCES warehouses(id);

ALTER TABLE products ADD COLUMN IF NOT EXISTS item_code VARCHAR(50);
CREATE UNIQUE INDEX IF NOT EXISTS uk_products_item_code ON products(item_code);

ALTER TABLE products ADD COLUMN IF NOT EXISTS barcode_symbology VARCHAR(20);
ALTER TABLE products ADD COLUMN IF NOT EXISTS barcode_value VARCHAR(128);
DO $$ BEGIN
IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'products_barcode_symbology_check') THEN
ALTER TABLE products ADD CONSTRAINT products_barcode_symbology_check CHECK (barcode_symbology IS NULL OR LOWER(barcode_symbology) = ANY (ARRAY['ean13','upc_a','code128','code39','qr']));
END IF;
END $$;

ALTER TABLE products ADD COLUMN IF NOT EXISTS selling_type VARCHAR(20);
DO $$ BEGIN
IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'products_selling_type_check') THEN
ALTER TABLE products ADD CONSTRAINT products_selling_type_check CHECK (selling_type IS NULL OR LOWER(selling_type) = ANY (ARRAY['unit','weight','service']));
END IF;
END $$;

ALTER TABLE products ADD COLUMN IF NOT EXISTS product_type VARCHAR(20) DEFAULT 'single';
DO $$ BEGIN
IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'products_product_type_check') THEN
ALTER TABLE products ADD CONSTRAINT products_product_type_check CHECK (LOWER(product_type) = ANY (ARRAY['single','variable']));
END IF;
END $$;

ALTER TABLE products ADD COLUMN IF NOT EXISTS tax_type VARCHAR(20);
DO $$ BEGIN
IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'products_tax_type_check') THEN
ALTER TABLE products ADD CONSTRAINT products_tax_type_check CHECK (tax_type IS NULL OR LOWER(tax_type) = ANY (ARRAY['inclusive','exclusive','none']));
END IF;
END $$;

ALTER TABLE products ADD COLUMN IF NOT EXISTS discount_type VARCHAR(20);
ALTER TABLE products ADD COLUMN IF NOT EXISTS discount_value NUMERIC(10,2) DEFAULT 0;
DO $$ BEGIN
IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'products_discount_value_check') THEN
ALTER TABLE products ADD CONSTRAINT products_discount_value_check CHECK (discount_value >= 0);
END IF;
END $$;

ALTER TABLE products ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE products ADD COLUMN IF NOT EXISTS manufacturer VARCHAR(150);

DO $$ BEGIN
IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'products_status_check') THEN
ALTER TABLE products ADD CONSTRAINT products_status_check CHECK (LOWER(status) = ANY (ARRAY['active','inactive','deleted']));
END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_brand_id ON products(brand_id);
CREATE INDEX IF NOT EXISTS idx_products_store_id ON products(store_id);
CREATE INDEX IF NOT EXISTS idx_products_warehouse_id ON products(warehouse_id);