-- ================================================
-- POS System - Initial Data Seed
-- Run AFTER init.sql
-- ================================================

-- ==========================
-- Roles
-- ==========================
INSERT INTO roles (name)
VALUES
('ADMIN'),
('CASHIER')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ==========================
-- Users
-- ==========================

-- Default admin (password: admin123)
INSERT INTO users (
    username,
    password,
    full_name,
    role_id,
    created_at,
    updated_at
)
SELECT
    'admin',
    '$2a$12$WIYI3Te.kAyg2jmTU87dXe.RPCggC36yA7TA8p.bMHtbBdUy1wcHe',
    'Administrator',
    (SELECT id FROM roles WHERE name = 'ADMIN'),
    NOW(),
    NOW()
ON DUPLICATE KEY UPDATE
updated_at = NOW();

-- Default cashier (password: cashier123)
INSERT INTO users (
    username,
    password,
    full_name,
    role_id,
    created_at,
    updated_at
)
SELECT
    'cashier',
    '$2a$12$27txhzNsjmn3bi9hpuF68uzeFXlPrbww6/bhMo5/3KbJ14/OrFx72',
    'Default Cashier',
    (SELECT id FROM roles WHERE name = 'CASHIER'),
    NOW(),
    NOW()
ON DUPLICATE KEY UPDATE
updated_at = NOW();

-- ==========================
-- Categories
-- ==========================
INSERT INTO categories (
    name,
    created_at,
    updated_at
)
VALUES
('Beverages', NOW(), NOW()),
('Snacks', NOW(), NOW()),
('Dairy', NOW(), NOW()),
('Electronics', NOW(), NOW()),
('Stationery', NOW(), NOW())
ON DUPLICATE KEY UPDATE
updated_at = NOW();

-- ==========================
-- Suppliers
-- ==========================
INSERT INTO suppliers (
    name,
    phone,
    address,
    created_at,
    updated_at
)
VALUES
('ABC Distributors', '012-345-6789', 'Phnom Penh, Cambodia', NOW(), NOW()),
('XYZ Trading Co.', '098-765-4321', 'Siem Reap, Cambodia', NOW(), NOW());

-- ==========================
-- Products
-- ==========================
INSERT INTO products (
    name,
    barcode,
    image_url,
    selling_price,
    cost_price,
    category_id,
    supplier_id,
    status,
    created_at,
    updated_at
)
VALUES
(
    'Coca-Cola 350ml',
    'BAR001',
    'images/coca-cola.png',
    1.00,
    0.60,
    (SELECT id FROM categories WHERE name='Beverages'),
    (SELECT id FROM suppliers WHERE name='ABC Distributors'),
    'ACTIVE',
    NOW(),
    NOW()
),
(
    'Pepsi 350ml',
    'BAR002',
    'images/pepsi.png',
    1.00,
    0.60,
    (SELECT id FROM categories WHERE name='Beverages'),
    (SELECT id FROM suppliers WHERE name='ABC Distributors'),
    'ACTIVE',
    NOW(),
    NOW()
),
(
    'Lay''s Chips',
    'BAR003',
    'images/lays.png',
    1.50,
    0.90,
    (SELECT id FROM categories WHERE name='Snacks'),
    (SELECT id FROM suppliers WHERE name='XYZ Trading Co.'),
    'ACTIVE',
    NOW(),
    NOW()
),
(
    'Oreo Cookies',
    'BAR004',
    'images/oreo.png',
    2.00,
    1.20,
    (SELECT id FROM categories WHERE name='Snacks'),
    (SELECT id FROM suppliers WHERE name='XYZ Trading Co.'),
    'ACTIVE',
    NOW(),
    NOW()
),
(
    'Milk 1L',
    'BAR005',
    'images/milk.png',
    2.50,
    1.80,
    (SELECT id FROM categories WHERE name='Dairy'),
    (SELECT id FROM suppliers WHERE name='ABC Distributors'),
    'ACTIVE',
    NOW(),
    NOW()
),
(
    'Notebook A4',
    'BAR006',
    'images/notebook.png',
    1.20,
    0.70,
    (SELECT id FROM categories WHERE name='Stationery'),
    (SELECT id FROM suppliers WHERE name='XYZ Trading Co.'),
    'ACTIVE',
    NOW(),
    NOW()
),
(
    'Ballpoint Pen',
    'BAR007',
    'images/pen.png',
    0.50,
    0.25,
    (SELECT id FROM categories WHERE name='Stationery'),
    (SELECT id FROM suppliers WHERE name='XYZ Trading Co.'),
    'ACTIVE',
    NOW(),
    NOW()
),
(
    'Mineral Water 1L',
    'BAR008',
    'images/water.png',
    0.75,
    0.40,
    (SELECT id FROM categories WHERE name='Beverages'),
    (SELECT id FROM suppliers WHERE name='ABC Distributors'),
    'ACTIVE',
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE
updated_at = NOW();

-- ==========================
-- Stocks
-- ==========================
INSERT INTO stocks (
    product_id,
    quantity,
    low_stock_alert,
    updated_at
)
SELECT
    id,
    100,
    10,
    NOW()
FROM products
ON DUPLICATE KEY UPDATE
quantity = VALUES(quantity),
updated_at = NOW();

-- ==========================
-- Stock History
-- ==========================
INSERT INTO stock_history (
    product_id,
    type,
    quantity,
    note,
    created_at
)
SELECT
    id,
    'IN',
    100,
    'Initial stock seed',
    NOW()
FROM products;

-- ==========================
-- Discounts
-- ==========================
INSERT INTO discounts (
    name,
    type,
    value,
    active
)
VALUES
('Member Discount', 'PERCENTAGE', 10.00, TRUE),
('Happy Hour', 'PERCENTAGE', 5.00, TRUE),
('$1 Off', 'FIXED', 1.00, TRUE);

-- ==========================
-- Settings
-- ==========================
INSERT INTO settings (
    store_name,
    store_phone,
    store_address,
    currency,
    tax_percentage,
    updated_at
)
VALUES (
    'My POS Store',
    '012-000-0000',
    'Phnom Penh, Cambodia',
    'USD',
    0.00,
    NOW()
);

-- ==========================
-- Sample Order
-- ==========================
INSERT INTO orders (
    user_id,
    total_amount,
    discount_amount,
    discount_id,
    final_amount,
    receipt_number,
    created_at
)
VALUES (
    (SELECT id FROM users WHERE username='admin'),
    3.50,
    0.00,
    NULL,
    3.50,
    'RCPT-0001',
    NOW()
);

-- ==========================
-- Sample Order Items
-- ==========================
INSERT INTO order_items (
    order_id,
    product_id,
    quantity,
    price,
    subtotal
)
VALUES
(
    (SELECT id FROM orders WHERE receipt_number='RCPT-0001'),
    (SELECT id FROM products WHERE barcode='BAR001'),
    2,
    1.00,
    2.00
),
(
    (SELECT id FROM orders WHERE receipt_number='RCPT-0001'),
    (SELECT id FROM products WHERE barcode='BAR003'),
    1,
    1.50,
    1.50
);

-- ==========================
-- Sample Payment
-- ==========================
INSERT INTO payments (
    order_id,
    method,
    paid_amount,
    change_amount,
    created_at
)
VALUES (
    (SELECT id FROM orders WHERE receipt_number='RCPT-0001'),
    'CASH',
    5.00,
    1.50,
    NOW()
);

SELECT 'Seed data inserted successfully!' AS status;
