# Spring Boot E-Commerce & Inventory Management Backend

A production-ready, highly concurrent RESTful E-Commerce and Dedicated Inventory Management backend built with **Spring Boot 3.5** and **Java 21**. 

This system features stateless **JWT authentication**, **role-based access control (RBAC)**, an **isolated inventory subsystem** with dynamic available stock calculations, **deadlock-free pessimistic locking**, an **immutable inventory movement audit ledger**, and **atomic order lifecycle state machines**.

---

## Table of Contents

- [Key Architecture & Design Highlights](#key-architecture--design-highlights)
- [System Architecture & Concurrency Model](#system-architecture--concurrency-model)
- [Features](#features)
  - [Authentication & Security](#1-authentication--security)
  - [Product & Category Catalog](#2-product--category-catalog)
  - [Shopping Cart Management](#3-shopping-cart-management)
  - [Dedicated Inventory Subsystem](#4-dedicated-inventory-subsystem)
  - [Inventory Movement Audit Ledger](#5-inventory-movement-audit-ledger)
  - [Order Management & Lifecycle State Machine](#6-order-management--lifecycle-state-machine)
- [Technology Stack](#technology-stack)
- [Database & Historical Integrity](#database--historical-integrity)
- [API Endpoints Reference](#api-endpoints-reference)
- [Getting Started & Local Setup](#getting-started--local-setup)
- [API Documentation (Swagger / OpenAPI)](#api-documentation-swagger--openapi)

---

## Key Architecture & Design Highlights

1. **Decoupled Inventory Architecture**: Product catalog entities do not store mutable physical stock directly. Stock is managed by an independent `Inventory` entity enforcing:
   $$\text{Available Quantity} = \text{Quantity On Hand} - \text{Reserved Quantity}$$
2. **Deadlock-Free Checkout Concurrency**: During checkout, order cancellation, and shipment, all affected product IDs are sorted in ascending order before acquiring pessimistic write locks (`PESSIMISTIC_WRITE`), eliminating circular wait conditions across multi-item transactions.
3. **Cart Concurrency Guard**: Pessimistic write locking on the user's cart prevents double-submit and race-condition duplicate orders.
4. **Historical Immutability & Snapshots**: 
   - `OrderItem` preserves purchase-time snapshots of product names, IDs, unit prices, and subtotals.
   - `Order` preserves complete immutable shipping address snapshots at checkout.
   - Product deletion is protected by comprehensive integrity guards (cannot delete products linked to past orders, active cart items, reserved stock, or movement ledgers).
5. **Full Audit Traceability**:
   - `InventoryMovement` records every physical stock adjustment, checkout reservation, cancellation release, and fulfillment shipment.
   - `OrderStatusHistory` tracks every status transition, who changed it, and timestamp.

---

## System Architecture & Concurrency Model

### 1. Layered Architecture
```
Client (HTTP/JSON + Bearer JWT)
       │
       ▼
[SecurityFilterChain & JwtAuthenticationFilter]
       │
       ▼
Controllers (@RestController)
       │
       ▼
Service Layer (Interfaces & Transactional ServiceImpls)
       │
       ▼
Spring Data JPA Repositories (Pessimistic Locks & Custom Queries)
       │
       ▼
MySQL 8.0 Database (InnoDB Engine)
```

### 2. Order Checkout & Inventory Reservation Flow

```
User Checkout Request (POST /api/orders/place)
  │
  ├─► 1. Lock Cart via `findByUserEmailWithLock` (Pessimistic Write Lock)
  │      └─ Verify cart is not empty
  │
  ├─► 2. Sort Cart Items by Product ID Ascending (Deadlock Prevention)
  │
  ├─► 3. For each Item:
  │      ├─ Acquire `Inventory` lock via `findByProductIdWithLock`
  │      ├─ Check: `availableQuantity >= item.quantity`
  │      ├─ Call `inventory.reserve(quantity)` (Increments reservedQuantity)
  │      └─ Create OrderItem snapshot (product details & purchase price)
  │
  ├─► 4. Persist Order (Status: PENDING)
  │      └─ Record OrderStatusHistory (PENDING, email)
  │
  ├─► 5. Record `InventoryMovement` entries (Type: RESERVATION, referenceId: order.id)
  │
  ├─► 6. Clear Cart & Reset Total Amount
  │
  └─► 7. Commit Transaction & Return OrderDto (HTTP 201 Created)
```

### 3. Order Lifecycle State Machine

```
              ┌───────────────────────────┐
              │          PENDING          │
              └─────────────┬─────────────┘
                            │
               ┌────────────┴────────────┐
               │                         │
     [Admin Confirms]            [User/Admin Cancels]
               │                         │
               ▼                         ▼
  ┌─────────────────────────┐   ┌─────────────────────────┐
  │        CONFIRMED        │   │        CANCELLED        │◄── (Stock Released)
  └────────────┬────────────┘   └─────────────────────────┘
               │                         ▲
        [Admin Ships]            [Admin Cancels]
               │                         │
               ▼                         │
  ┌─────────────────────────┐            │
  │         SHIPPED         ├────────────┘
  │ (Stock DeductedOnHand)  │
  └────────────┬────────────┘
               │
        [Admin Delivers]
               │
               ▼
  ┌─────────────────────────┐
  │        DELIVERED        │ (Terminal State)
  └─────────────────────────┘
```

---

## Features

### 1. Authentication & Security
- **Stateless JWT Authentication**: Tokens issued upon login with 1-hour expiration.
- **Spring Security 6 Integration**: Configured with stateless session management and BCrypt password encryption (strength 10).
- **Role-Based Authorization**: Distinct access controls for `ROLE_USER` and `ROLE_ADMIN`.
- **IDOR Protection**: Customer order retrieval and cancellation strictly validated against the authenticated user's email.

### 2. Product & Category Catalog
- Category CRUD operations with association constraints (cannot delete categories containing active products).
- Product CRUD with real-time stock retrieval mapped to inventory `availableQuantity`.
- Search with case-insensitive keyword queries.
- Paginated and sorted catalog browsing (`page`, `size`, `sortBy`, `direction`).
- Safe deletion validation protecting historical order, movement, and cart data.

### 3. Shopping Cart Management
- User-bound carts with automated creation on first item addition.
- Quantity management and subtotal calculation.
- Pre-reservation stock verification (checks available stock prior to cart addition without locking inventory prematurely).
- Thread-safe cart operations with pessimistic cart locking.

### 4. Dedicated Inventory Subsystem
- **Independent Inventory Entity**: 1:1 relationship with `Product`.
- **Dynamic Formula**: `availableQuantity = quantityOnHand - reservedQuantity`.
- **Explicit Lifecycle Methods**:
  - `reserve(qty)`: `quantityOnHand` unchanged, `reservedQuantity += qty`.
  - `releaseReservation(qty)`: `quantityOnHand` unchanged, `reservedQuantity -= qty`.
  - `shipReserved(qty)`: `quantityOnHand -= qty`, `reservedQuantity -= qty`.
  - `adjustAvailableQuantity(newAvailable)`: `quantityOnHand = newAvailable + reservedQuantity`.
- **Concurrency & Versioning**: Row-level pessimistic locking (`PESSIMISTIC_WRITE`) combined with `@Version` optimistic locking protection.

### 5. Inventory Movement Audit Ledger
- Immutable ledger tracking every change to inventory quantities.
- Movement Types:
  - `STOCK_ADJUSTMENT`: Direct admin adjustments or initial product creation stock.
  - `RESERVATION`: Placed orders reserving inventory.
  - `RELEASE`: Cancelled orders releasing reservations back to available stock.
  - `SHIPMENT`: Confirmed orders transitioning to shipped, consuming physical on-hand stock.
- Tracks `previousQuantityOnHand`, `newQuantityOnHand`, `previousReservedQuantity`, `newReservedQuantity`, `quantity`, `referenceId` (Order ID), `changedBy`, and timestamps.
- Paginated movement history API by product.

### 6. Order Management & Lifecycle State Machine
- **Atomic Checkout**: Complete cart-to-order transaction processing.
- **Shipping Address Snapshots**: Full recipient and address capture stored on `Order`.
- **Purchase-Time Product Snapshots**: Name, ID, unit price, and subtotal stored in `OrderItem`.
- **Order Cancellation**: Restores reserved stock, appends `RELEASE` audit record, and updates status to `CANCELLED`.
- **Order Fulfillment / Shipment**: Deducts physical and reserved inventory, appends `SHIPMENT` audit record, and updates status to `SHIPPED`.
- **Status History Audit**: Dedicated `OrderStatusHistory` recording every state transition.

---

## Technology Stack

| Layer / Concern | Technology |
|---|---|
| **Language & Platform** | Java 21 (LTS) |
| **Framework** | Spring Boot 3.5.0 |
| **Security** | Spring Security 6, JJWT (io.jsonwebtoken 0.12.6) |
| **Persistence** | Spring Data JPA, Hibernate 6.6 (JPA 3.1) |
| **Database** | MySQL 8.0+ (InnoDB Engine) |
| **Validation** | Jakarta Bean Validation (Hibernate Validator) |
| **Documentation** | Springdoc OpenAPI 2.8.10 (Swagger UI 5) |
| **Build & Tooling** | Apache Maven 3.9+, Project Lombok |

---

## Database & Historical Integrity

The application enforces database integrity across interrelated entities:

```
[categories] 1 ──── N [products] 1 ──── 1 [inventory] 1 ──── N [inventory_movements]
                          │
             ┌────────────┴────────────┐
             ▼                         ▼
       [cart_items]              [order_items] N ──── 1 [orders] 1 ──── N [order_status_history]
             ▲                                              │
             │                                              │
             └───────────── N [cart] 1 ──── 1 [users] 1 ────┘
```

- **Unique Constraints**:
  - `inventory.uk_inventory_product` on `(product_id)` (strictly enforces 1:1 product-to-inventory).
  - `users.email` (unique customer identity).
  - `cart.user_id` (one active cart per user).
- **Audit Preservation**: Deleting a product is blocked whenever related `order_items`, `cart_items`, or `inventory_movements` exist, preserving business records and reporting integrity.

---

## API Endpoints Reference

### Public & Authentication

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/public/register` | Register a new user account | Public |
| `POST` | `/api/public/login` | Authenticate and obtain JWT token | Public |

### Product Catalog (Public / Customer)

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/products` | Paginated product list (`page`, `size`, `sortBy`, `direction`) | Public |
| `GET` | `/api/products/{id}` | Get product details by ID (includes available stock) | Public |
| `GET` | `/api/products/search` | Search products by keyword (`keyword`, `page`, `size`) | Public |

### Categories (Public / Customer)

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/categories` | List all product categories | Public |
| `GET` | `/api/categories/{id}` | Get category details by ID | Public |

### Shopping Cart (Customer)

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/cart` | Get authenticated user's cart and item totals | Authenticated |
| `POST` | `/api/cart/add` | Add product to cart (`productId`, `quantity`) | Authenticated |
| `DELETE` | `/api/cart/remove/{cartItemId}` | Remove specific cart item | Authenticated |
| `DELETE` | `/api/cart/clear` | Clear all items from user's cart | Authenticated |

### Order Management (Customer)

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/orders/place` | Place order from cart with shipping address | Authenticated |
| `GET` | `/api/orders` | View authenticated user's order history | Authenticated |
| `GET` | `/api/orders/{orderId}` | Get order details by ID (IDOR protected) | Authenticated |
| `DELETE` | `/api/orders/{orderId}` | Cancel order (releases reserved stock) | Authenticated |

### Admin Product Management

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/admin/products` | Create product and initialize inventory | `ADMIN` |
| `PUT` | `/api/admin/products/{id}` | Update product details and adjust inventory | `ADMIN` |
| `DELETE` | `/api/admin/products/{id}` | Safe-delete product (enforces integrity rules) | `ADMIN` |

### Admin Category Management

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/admin/categories` | Create product category | `ADMIN` |
| `PUT` | `/api/admin/categories/{id}` | Update category name | `ADMIN` |
| `DELETE` | `/api/admin/categories/{id}` | Delete category (blocked if products exist) | `ADMIN` |

### Admin Inventory & Audit Management

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/admin/inventory/{productId}` | Get stock levels (on-hand, reserved, available) | `ADMIN` |
| `PUT` | `/api/admin/inventory/{productId}/available-quantity` | Adjust available quantity (audited) | `ADMIN` |
| `GET` | `/api/admin/inventory/{productId}/movements` | Paginated audit movement history | `ADMIN` |

### Admin Order Management

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/admin/orders` | View all customer orders | `ADMIN` |
| `GET` | `/api/admin/orders/{orderId}` | View any order details by ID | `ADMIN` |
| `PUT` | `/api/admin/orders/{orderId}/status/{status}` | Update status (`CONFIRMED`, `SHIPPED`, `CANCELLED`, etc.) | `ADMIN` |

### Admin User Management

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/admin/users` | List all users (paginated and sorted) | `ADMIN` |
| `GET` | `/api/admin/users/{id}` | Get user account by ID | `ADMIN` |
| `PUT` | `/api/admin/users/{id}` | Update user profile | `ADMIN` |
| `DELETE` | `/api/admin/users/{id}` | Delete user account | `ADMIN` |

---

## Getting Started & Local Setup

### Prerequisites

- **Java JDK 21** or later installed
- **MySQL Server 8.0+** running locally or in Docker
- **Maven 3.9+** (or use included `mvnw`)

### 1. Clone Repository

```bash
git clone https://github.com/Bhavendan/ecommerce-app.git
cd ecommerce-app
```

### 2. Configure Environment & Database

Create the MySQL database:
```sql
CREATE DATABASE ecommerce_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Configure `src/main/resources/application.properties` or set environment variables:

```properties
spring.application.name=ecommerce-app
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:your_password}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT Signing Secret (256-bit HMAC key)
jwt.secret=${JWT_SECRET:mysupersecretkeyforecommerceapp-mysupersecretkeyforecommerceapp}
```

### 3. Build & Run

Using the Maven wrapper:

```bash
# Compile and package
./mvnw clean package -DskipTests

# Run the Spring Boot application
./mvnw spring-boot:run
```

The application starts on port `8080`.

---

## API Documentation (Swagger / OpenAPI)

Once the application is running, interact with and test all endpoints via the interactive Swagger UI:

- **Swagger UI Interface**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI 3.0 Specification**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

To test authenticated endpoints in Swagger UI:
1. Register or login via `/api/public/login`.
2. Copy the returned JWT token.
3. Click the green **Authorize** button at the top of Swagger UI.
4. Enter `Bearer <your_token>` and submit.
