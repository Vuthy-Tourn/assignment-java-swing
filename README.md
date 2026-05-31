# POS System — Java Swing

A full-featured Point of Sale system built with Java Swing following OOP principles.

## Architecture

```
com.pos/
├── Main.java                  ← Entry point
├── model/                     ← POJOs (User, Product, Order, etc.)
├── dao/                       ← JDBC data access (ProductDAO, OrderDAO, etc.)
├── service/                   ← Business logic (OrderService, StockService, etc.)
├── ui/
│   ├── MainFrame.java         ← Main window with sidebar navigation
│   ├── panels/                ← SalesPanel, ProductPanel, StockPanel, OrderPanel, etc.
│   ├── dialogs/               ← LoginDialog, PaymentDialog
│   └── components/            ← RoundedButton, StyledTable, UIConstants
└── util/                      ← DatabaseConnection, PasswordUtils, AppContext
```

## Features

| Panel | Features |
|-------|---------|
| **Sales** | Barcode scanning, product search, cart management, discount application, cash/card/QR payment |
| **Products** | CRUD with category & supplier, barcode, cost/selling price, status |
| **Stock** | Stock-in, adjustments, low-stock alerts (highlighted red), history log |
| **Orders** | Date-range filter, order detail view, total revenue summary |
| **Users** | Admin-only: create/edit/delete users, change passwords, role assignment |
| **Settings** | Store name, phone, address, currency, tax % |

## Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.0+

## Setup

### 1. Create database

```bash
cd pos_db

docker compose up
```

### 2. Run schema

```bash
mysql -u admin -p mydb < init.sql
```

### 3. Seed initial data

```bash
mysql -u admin -p mydb < src/main/resources/seed.sql
```

### 4. Configure database connection

Edit `src/main/resources/application.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=root
db.password=your_password
```

### 5. Build and run

```bash
mvn clean package -q
java -jar target/pos-system-1.0.0-jar-with-dependencies.jar
```

Or run directly in your IDE by running `com.pos.Main`.

## Default Login Credentials

| Role    | Username  | Password    |
|---------|-----------|-------------|
| Admin   | admin     | admin123    |
| Cashier | cashier   | cashier123  |

> **Change passwords after first login!**

## OOP Design Highlights

- **Layered architecture**: UI → Service → DAO → Model — no layer skips
- **Single responsibility**: each class has one job (e.g. `OrderDAO` only handles DB, `OrderService` handles business rules)
- **Encapsulation**: all model fields are private with getters/setters
- **Connection pooling**: HikariCP manages DB connections efficiently
- **Transaction safety**: `OrderDAO.save()` uses a single transaction for order + items + payment + stock deduction
- **Enum-like constants**: `RoundedButton.Style`, `Payment.Method`, `StockHistory.Type`
- **Renderer pattern**: `ProductCellRenderer` in SalesPanel shows rich list items

## Dependencies

| Library | Purpose |
|---------|---------|
| `mysql-connector-j` | MySQL JDBC driver |
| `HikariCP` | High-performance connection pool |
| `FlatLaf` | Modern flat look-and-feel for Swing |
| `jbcrypt` | Secure password hashing |
| `slf4j-simple` | Logging |
