CREATE TABLE `roles` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(50) UNIQUE NOT NULL
);

CREATE TABLE `users` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `username` varchar(100) UNIQUE NOT NULL,
  `password` varchar(255) NOT NULL,
  `full_name` varchar(100),
  `role_id` bigint,
  `created_at` timestamp DEFAULT (CURRENT_TIMESTAMP),
  `updated_at` timestamp
);

CREATE TABLE `categories` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(100) UNIQUE NOT NULL,
  `created_at` timestamp,
  `updated_at` timestamp
);

CREATE TABLE `suppliers` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `phone` varchar(50),
  `address` varchar(255),
  `created_at` timestamp,
  `updated_at` timestamp
);

CREATE TABLE `products` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(150) NOT NULL,
  `barcode` varchar(100) UNIQUE,
  `image_url` varchar(255),
  `selling_price` decimal(10,2) NOT NULL,
  `cost_price` decimal(10,2) NOT NULL,
  `category_id` bigint,
  `supplier_id` bigint,
  `status` varchar(20) DEFAULT 'ACTIVE',
  `created_at` timestamp DEFAULT (CURRENT_TIMESTAMP),
  `updated_at` timestamp
);

CREATE TABLE `stocks` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `product_id` bigint UNIQUE NOT NULL,
  `quantity` int NOT NULL DEFAULT 0,
  `low_stock_alert` int DEFAULT 5,
  `updated_at` timestamp
);

CREATE TABLE `stock_history` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `type` varchar(20) NOT NULL,
  `quantity` int NOT NULL,
  `note` varchar(255),
  `created_at` timestamp DEFAULT (CURRENT_TIMESTAMP)
);

CREATE TABLE `discounts` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `type` varchar(20) NOT NULL,
  `value` decimal(10,2) NOT NULL,
  `active` boolean DEFAULT true
);

CREATE TABLE `orders` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint,
  `total_amount` decimal(10,2) NOT NULL,
  `discount_amount` decimal(10,2) DEFAULT 0,
  `discount_id` bigint,
  `final_amount` decimal(10,2) NOT NULL,
  `receipt_number` varchar(100) UNIQUE,
  `created_at` timestamp DEFAULT (CURRENT_TIMESTAMP)
);

CREATE TABLE `order_items` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `quantity` int NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `subtotal` decimal(10,2) NOT NULL
);

CREATE TABLE `payments` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `method` varchar(20) NOT NULL,
  `paid_amount` decimal(10,2) NOT NULL,
  `change_amount` decimal(10,2) DEFAULT 0,
  `created_at` timestamp DEFAULT (CURRENT_TIMESTAMP)
);

CREATE TABLE `settings` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `store_name` varchar(150),
  `store_phone` varchar(50),
  `store_address` varchar(255),
  `currency` varchar(20) DEFAULT 'USD',
  `tax_percentage` decimal(5,2) DEFAULT 0,
  `updated_at` timestamp
);

ALTER TABLE `users` ADD FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`);

ALTER TABLE `products` ADD FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`);

ALTER TABLE `products` ADD FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`);

ALTER TABLE `stocks` ADD FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);

ALTER TABLE `stock_history` ADD FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);

ALTER TABLE `orders` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `order_items` ADD FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`);

ALTER TABLE `order_items` ADD FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);

ALTER TABLE `payments` ADD FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`);

ALTER TABLE `orders` ADD FOREIGN KEY (`discount_id`) REFERENCES `discounts` (`id`);
