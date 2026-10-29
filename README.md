# 🛒 Enterprise E-Commerce Management System

A full-stack, enterprise-grade E-Commerce Management System engineered with **Java**, **Spring Boot 3**, **MySQL**, **Spring Data JPA & Hibernate**, **Spring Security**, **OpenAPI 3 / Swagger**, and **Modern JavaScript (ES6+)**.

Designed specifically to demonstrate **production-grade business logic**, **database relationships**, **concurrency control**, and **inventory audit management** for real-world software engineering portfolios and resumes.

---

## 🌟 Key Features & Business Logic (Resume Highlights)

### 1. Database Relationships & Entity Design
- **1-to-1**: `User` ⇄ `Cart` (Each registered customer owns a persistent cart with automated lifecycle).
- **1-to-Many**: `User` ⇄ `Address` (Multiple shipping/billing addresses per customer with default flags).
- **1-to-Many**: `User` ⇄ `Order` (Customer purchase history and fulfillment tracking).
- **1-to-Many**: `Cart` ⇄ `CartItem` (Orphan removal and automatic subtotal calculation).
- **1-to-Many**: `Order` ⇄ `OrderItem` (Price snapshotting at checkout to isolate historic orders from catalog price fluctuations).
- **Many-to-1**: `Product` ⇄ `Category` (Categorized catalog taxonomy with slug indexing).
- **1-to-Many**: `Product` ⇄ `InventoryAudit` (Full immutable audit ledger tracking every stock delta).

### 2. Transactional Inventory & Concurrency Protection
- **Atomic Stock Deductions (`@Transactional`)**: During checkout, every line item is verified against real-time database inventory. If requested quantity exceeds available stock, checkout safely aborts with an `InsufficientStockException`.
- **Automatic Stock Replenishment**: When an order is cancelled (either by customer while pending or by administrator), ordered quantities are automatically restored to product inventory with an `ORDER_CANCEL_REFUND` audit log.
- **Optimistic Locking (`@Version`)**: Guarantees zero lost-updates during high-concurrency checkouts.
- **Strict Ledger Audit (`MovementType`)**: Tracks movements (`INITIAL_SEED`, `ORDER_DEDUCTION`, `ORDER_CANCEL_REFUND`, `RESTOCK`, `MANUAL_ADJUSTMENT`), previous quantity, new quantity, operator, and timestamp.

### 3. Shopping Cart & Dynamic Pricing Engine
- Real-time cart calculations: item subtotals, configurable tax rate (e.g., 8%), and free shipping threshold (e.g. Free shipping above $50.00).
- Inventory ceiling validation: customers cannot add more units than currently in stock.

### 4. Admin Management Dashboard & Business Intelligence
- Executive KPI summary cards: Total Net Revenue ($), Total Orders, Active Catalog Products, and Low Stock Alerts (&le; 10 units).
- Product Catalog CRUD: Add new products, edit attributes, archive products.
- Inventory Control: Quick 1-click restock (+10, +50 units), custom stock adjustments, and live movement audit trail.
- Order Fulfillment: Advance order states (`PENDING` → `CONFIRMED` → `SHIPPED` → `DELIVERED` or `CANCELLED`).

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Backend Framework** | Java 17+, Spring Boot 3.3.4 |
| **Persistence & ORM** | Spring Data JPA, Hibernate ORM |
| **Databases** | MySQL 8.0+ (Primary Production), H2 In-Memory (Dev/Testing Profile) |
| **Security & Auth** | Spring Security 6 (BCrypt Password Hashing, RBAC) |
| **API Documentation** | Springdoc OpenAPI 3.0 / Swagger UI |
| **Frontend** | Modern Vanilla JavaScript (ES6+), HTML5, CSS3 Responsive Grid |
| **Build & Tooling** | Apache Maven 3.9+, Maven Wrapper (`mvnw`) |

---

## 📁 Project Architecture

```
ecommerce-system/
├── pom.xml                               # Maven build configuration
├── mvnw / mvnw.cmd                       # Maven wrapper scripts
├── README.md                             # Documentation & Setup Guide
├── src/
│   ├── main/
│   │   ├── java/com/ecommerce/
│   │   │   ├── EcommerceApplication.java # Spring Boot main entrypoint
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java   # BCrypt, CORS, security chain
│   │   │   │   ├── WebMvcConfig.java     # Static asset handlers
│   │   │   │   ├── OpenApiConfig.java    # Swagger 3.0 configuration
│   │   │   │   └── DataInitializer.java  # Auto-seeds 12 products, users & demo data
│   │   │   ├── controller/               # REST API Controllers
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── ProductController.java
│   │   │   │   ├── CategoryController.java
│   │   │   │   ├── CartController.java
│   │   │   │   ├── OrderController.java
│   │   │   │   ├── InventoryController.java
│   │   │   │   └── DashboardController.java
│   │   │   ├── dto/                      # Request & Response Data Transfer Objects
│   │   │   ├── entity/                   # JPA Database Entities & Enums
│   │   │   ├── exception/                # Global exception handlers & custom errors
│   │   │   ├── repository/               # Spring Data JPA Repositories
│   │   │   └── service/                  # Business logic interfaces & implementations
│   │   └── resources/
│   │       ├── application.properties    # MySQL configuration
│   │       ├── application-h2.properties # In-memory H2 profile
│   │       ├── db/migration/
│   │       │   ├── schema.sql            # Clean MySQL DDL script
│   │       │   └── data.sql              # Rich sample seed dataset
│   │       └── static/                   # Production-grade SPA frontend
│   │           ├── index.html            # Storefront, Orders & Admin Dashboard UI
│   │           ├── css/styles.css        # Responsive styling system
│   │           └── js/
│   │               ├── api.js            # REST client
│   │               ├── state.js          # Reactive state store
│   │               └── app.js            # DOM controllers & business events
│   └── test/                             # Automated JUnit 5 & Mockito test suite
```

---

## 🚀 Getting Started & Execution

### Prerequisites
- **Java JDK 17, 21, or 26** installed.
- **MySQL Server 8.0+** running locally (or use the built-in H2 profile).

### Option 1: Running with MySQL (Default)
1. Verify MySQL is running on `localhost:3306`.
2. Ensure database credentials in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=root
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
   *(Or open in IntelliJ IDEA / Eclipse and run `EcommerceApplication.java`)*

### Option 2: Running with In-Memory H2 (Instant Zero-Config Mode)
To run instantly without needing a local MySQL server installed:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```
Or execute with java jar:
```bash
java -jar -Dspring.profiles.active=h2 target/ecommerce-management-system-1.0.0.jar
```

---

## 🌐 Application Endpoints & Ports

Once started, open your web browser:
- **Interactive Web Application**: [http://localhost:8080](http://localhost:8080)
- **Interactive Swagger REST API Docs**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI 3 JSON Specification**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **H2 Database Console** *(when running in H2 profile)*: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:ecommerce_db`, User: `sa`, Password: empty)

---

## 🔑 Pre-Seeded Demo Accounts

The application automatically seeds test accounts on startup:

| Role | Username / Email | Password | Access Level |
|---|---|---|---|
| **Administrator** | `admin` / `admin@eshop.com` | `admin123` | Full Admin Dashboard, Catalog CRUD, Inventory Restock, Order Fulfillment |
| **Customer** | `john_doe` / `john@eshop.com` | `customer123` | Browse Catalog, Manage Cart, Place Orders, Cancel Orders |

*(The web interface includes a 1-click **"Switch to Admin" / "Switch to Customer"** button in the top navbar for testing both perspectives without needing to log in repeatedly).*

---

## 📡 REST API Reference

### 🔐 Authentication (`/api/auth`)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register new user account (`ROLE_CUSTOMER` or `ROLE_ADMIN`) |
| `POST` | `/api/auth/login` | Authenticate credentials and receive auth token |
| `GET` | `/api/auth/me?identifier=...` | Retrieve user details by username or email |

### 📦 Products Catalog (`/api/products`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/products` | Filter & search products by category, status, and keyword |
| `GET` | `/api/products/{id}` | Get product details by ID |
| `GET` | `/api/products/sku/{sku}` | Get product by SKU |
| `POST` | `/api/products` | Create new catalog product *(Admin)* |
| `PUT` | `/api/products/{id}` | Update existing product details *(Admin)* |
| `DELETE` | `/api/products/{id}` | Archive / Soft-delete product *(Admin)* |

### 🏷️ Categories (`/api/categories`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/categories` | List all product taxonomy categories |
| `POST` | `/api/categories` | Create new category *(Admin)* |
| `PUT` | `/api/categories/{id}` | Update category *(Admin)* |
| `DELETE` | `/api/categories/{id}` | Delete category *(Admin)* |

### 🛒 Shopping Cart (`/api/cart`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/cart` | Get current customer's cart with subtotals, taxes, and shipping |
| `POST` | `/api/cart/items` | Add item to cart *(Validates against current inventory stock)* |
| `PUT` | `/api/cart/items/{itemId}` | Adjust cart item quantity *(Enforces available stock ceiling)* |
| `DELETE` | `/api/cart/items/{itemId}` | Remove item from cart |
| `DELETE` | `/api/cart` | Clear entire shopping cart |

### 📑 Orders & Checkout (`/api/orders`)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/orders` | Place order from cart *(Atomic stock deduction & price snapshotting)* |
| `GET` | `/api/orders` | Get order history for current customer |
| `GET` | `/api/orders/all` | Get all orders across the platform *(Admin)* |
| `GET` | `/api/orders/{id}` | Get detailed order summary |
| `PUT` | `/api/orders/{id}/status` | Update fulfillment status *(Admin - restores stock if cancelled)* |
| `POST` | `/api/orders/{id}/cancel` | Cancel order *(Customer - automatically restores product stock)* |

### 📊 Inventory Management (`/api/inventory`)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/inventory/adjust/{productId}`| Restock or adjust inventory with reason & audit logging *(Admin)* |
| `GET` | `/api/inventory/low-stock` | Retrieve products below warning threshold (&le; 10 units) |
| `GET` | `/api/inventory/audits` | View top 50 recent stock movement audit records *(Admin)* |
| `GET` | `/api/inventory/audits/product/{productId}` | View full audit history for a specific product |

### 📈 Admin Dashboard (`/api/dashboard`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/dashboard/metrics` | Retrieve total revenue, total orders, top products, and status counts |

---

## 🧪 Unit & Integration Tests

The project includes JUnit 5 and Mockito test suites:
- `OrderServiceTest`: Tests atomic order creation, stock deduction, cart clearing, and verifies that `InsufficientStockException` is thrown when stock is deficient.
- `InventoryServiceTest`: Tests supplier restocking, stock delta calculations, and verifies that reductions causing negative stock are strictly rejected.
- `EcommerceApplicationTests`: Spring Boot context load verification.

Run all tests via:
```bash
mvn test
```

---

## 📝 Resume & Portfolio Bullet Points

Here is how you can describe this project on your resume:
- *“Architected and built a full-stack **E-Commerce Management System** utilizing **Java 17**, **Spring Boot 3**, **MySQL**, **Spring Data JPA**, and **JavaScript**, implementing robust domain modeling across 8 interconnected relational entities.”*
- *“Engineered transactional order processing logic (`@Transactional`) featuring **atomic inventory reservation**, concurrency control (`@Version`), and automated inventory replenishment on order cancellations.”*
- *“Implemented an immutable **Inventory Audit Trail** to track all stock movements (restocks, order deductions, returns, manual adjustments), providing transparency across thousands of SKU line items.”*
- *“Developed an interactive, responsive Single Page Application (SPA) with real-time stock validation, cart drawer, checkout modal, customer order tracking, and an executive Admin Dashboard featuring business KPI analytics.”*
