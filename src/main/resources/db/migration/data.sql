-- =====================================================================
-- E-Commerce Management System - Seed Data
-- =====================================================================

USE ecommerce_db;

-- 1. Insert Categories
INSERT INTO categories (id, name, slug, description, image_url) VALUES
(1, 'Electronics', 'electronics', 'Cutting-edge gadgets, laptops, audio equipment, and computer peripherals.', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600'),
(2, 'Footwear & Apparel', 'apparel', 'Premium activewear, designer shoes, and comfortable everyday clothing.', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600'),
(3, 'Home & Kitchen', 'home-kitchen', 'Modern kitchen appliances, cookware, and cozy home accessories.', 'https://images.unsplash.com/photo-1584269600464-37b1b58a9fe7?w=600'),
(4, 'Books & Media', 'books', 'Bestselling technical books, software engineering manuals, and literature.', 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. Insert Products
INSERT INTO products (id, sku, name, description, price, stock_quantity, status, image_url, category_id, version) VALUES
(1, 'ELEC-SNY-001', 'Sony WH-1000XM5 Wireless Headphones', 'Industry-leading noise cancelation with two processors and 8 microphones. Up to 30 hours battery life with quick charging.', 399.99, 25, 'ACTIVE', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600', 1, 0),
(2, 'ELEC-APL-002', 'Apple MacBook Air M3 15-inch', 'Strikingly thin design with the lightning-fast Apple M3 chip, 16GB Unified Memory, and 512GB SSD storage.', 1299.00, 8, 'ACTIVE', 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600', 1, 0),
(3, 'ELEC-LOG-003', 'Logitech MX Master 3S Wireless Mouse', 'Ergonomic performance mouse with quiet clicks, 8K DPI track-on-glass sensor, and ultrafast MagSpeed scrolling.', 99.99, 45, 'ACTIVE', 'https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=600', 1, 0),
(4, 'ELEC-DEL-004', 'Dell UltraSharp 27 4K Monitor', 'Brilliant 4K UHD resolution with IPS Black technology, 98% DCI-P3 color gamut, and integrated USB-C hub.', 549.99, 4, 'ACTIVE', 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=600', 1, 0),
(5, 'APPL-NIK-005', 'Nike Air Zoom Pegasus 40 Running Shoes', 'Engineered mesh upper with dual Zoom Air units and responsive React foam for everyday running performance.', 130.00, 32, 'ACTIVE', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600', 2, 0),
(6, 'APPL-PAT-006', 'Patagonia Torrentshell 3L Rain Jacket', 'Simple and unpretentious 3-layer H2No Performance Standard shell for exceptional waterproof/breathable performance.', 179.00, 16, 'ACTIVE', 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=600', 2, 0),
(7, 'APPL-LEV-007', 'Levi\'s 511 Slim Fit Stretch Jeans', 'A modern slim with room to move. Added stretch for all-day comfort and mobility in classic dark wash.', 69.50, 50, 'ACTIVE', 'https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=600', 2, 0),
(8, 'HOME-DLG-008', 'De\'Longhi Magnifica S Espresso Machine', 'Compact bean-to-cup machine with manual milk frother, 15 bar pressure, and personalized coffee length.', 649.95, 5, 'ACTIVE', 'https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?w=600', 3, 0),
(9, 'HOME-NNJ-009', 'Ninja Air Fryer Pro 4-in-1', 'Crisp, roast, reheat, and dehydrate with 5-quart nonstick ceramic basket and rapid air circulation.', 119.99, 28, 'ACTIVE', 'https://images.unsplash.com/photo-1584269600464-37b1b58a9fe7?w=600', 3, 0),
(10, 'HOME-HDF-010', 'Hydro Flask 32 oz Wide Mouth Bottle', 'TempShield double-wall vacuum insulation keeps drinks cold up to 24 hours or hot up to 12 hours.', 44.95, 65, 'ACTIVE', 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=600', 3, 0),
(11, 'BOOK-DDA-011', 'Designing Data-Intensive Applications', 'The definitive guide to the architecture, data models, storage engines, and consensus algorithms of distributed systems.', 42.50, 38, 'ACTIVE', 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600', 4, 0),
(12, 'BOOK-CLA-012', 'Clean Architecture: A Craftsman\'s Guide', 'Robert C. Martin presents the universal rules of software architecture for high-quality, maintainable systems.', 34.99, 24, 'ACTIVE', 'https://images.unsplash.com/photo-1532012164546-f432f2e3777a?w=600', 4, 0)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 3. Initial Inventory Audit
INSERT INTO inventory_audits (product_id, movement_type, quantity_changed, previous_quantity, new_quantity, reason, performed_by) VALUES
(1, 'INITIAL_SEED', 25, 0, 25, 'Initial catalog setup', 'SYSTEM'),
(2, 'INITIAL_SEED', 8, 0, 8, 'Initial catalog setup', 'SYSTEM'),
(3, 'INITIAL_SEED', 45, 0, 45, 'Initial catalog setup', 'SYSTEM'),
(4, 'INITIAL_SEED', 4, 0, 4, 'Initial catalog setup - Low stock', 'SYSTEM'),
(5, 'INITIAL_SEED', 32, 0, 32, 'Initial catalog setup', 'SYSTEM'),
(6, 'INITIAL_SEED', 16, 0, 16, 'Initial catalog setup', 'SYSTEM'),
(7, 'INITIAL_SEED', 50, 0, 50, 'Initial catalog setup', 'SYSTEM'),
(8, 'INITIAL_SEED', 5, 0, 5, 'Initial catalog setup - Low stock', 'SYSTEM'),
(9, 'INITIAL_SEED', 28, 0, 28, 'Initial catalog setup', 'SYSTEM'),
(10, 'INITIAL_SEED', 65, 0, 65, 'Initial catalog setup', 'SYSTEM'),
(11, 'INITIAL_SEED', 38, 0, 38, 'Initial catalog setup', 'SYSTEM'),
(12, 'INITIAL_SEED', 24, 0, 24, 'Initial catalog setup', 'SYSTEM');
