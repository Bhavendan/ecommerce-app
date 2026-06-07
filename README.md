# Spring Boot E-Commerce Backend

## Overview

A RESTful E-Commerce Backend application built using Spring Boot. The project provides secure user authentication, product management, shopping cart functionality, and order processing.

## Features

### Authentication & Security

* JWT Authentication
* Spring Security
* Role-Based Authorization (ADMIN / USER)
* Password Encryption using BCrypt

### Product Management

* Create Product
* Update Product
* Delete Product
* Search Products
* Pagination & Sorting

### Category Management

* Create Category
* Update Category
* Delete Category
* View Categories

### Shopping Cart

* Add Products to Cart
* Remove Products from Cart
* View Cart
* Clear Cart

### Order Management

* Place Orders
* View Order History
* Cancel Orders

### Exception Handling

* Global Exception Handling
* Custom Exceptions

### API Documentation

* Swagger UI / OpenAPI

---

## Technology Stack

* Java 21
* Spring Boot 3
* Spring Security
* JWT Authentication
* Spring Data JPA
* Hibernate
* MySQL
* Maven
* Swagger OpenAPI

---

## API Endpoints

### Authentication

| Method | Endpoint             |
| ------ | -------------------- |
| POST   | /api/public/register |
| POST   | /api/public/login    |

### Products

| Method | Endpoint             |
| ------ | -------------------- |
| GET    | /api/products        |
| GET    | /api/products/{id}   |
| GET    | /api/products/search |

### Categories

| Method | Endpoint             |
| ------ | -------------------- |
| GET    | /api/categories      |
| GET    | /api/categories/{id} |

### Cart

| Method | Endpoint                      |
| ------ | ----------------------------- |
| GET    | /api/cart                     |
| POST   | /api/cart/add                 |
| DELETE | /api/cart/remove/{cartItemId} |
| DELETE | /api/cart/clear/{userId}      |

### Orders

| Method | Endpoint              |
| ------ | --------------------- |
| POST   | /api/orders/place     |
| GET    | /api/orders           |
| GET    | /api/orders/{orderId} |
| DELETE | /api/orders/{orderId} |

### Admin Product Management

| Method | Endpoint                 |
| ------ | ------------------------ |
| POST   | /api/admin/products      |
| PUT    | /api/admin/products/{id} |
| DELETE | /api/admin/products/{id} |

### Admin Category Management

| Method | Endpoint                   |
| ------ | -------------------------- |
| POST   | /api/admin/categories      |
| PUT    | /api/admin/categories/{id} |
| DELETE | /api/admin/categories/{id} |

### Admin User Management

| Method | Endpoint              |
| ------ | --------------------- |
| GET    | /api/admin/users      |
| GET    | /api/admin/users/{id} |
| PUT    | /api/admin/users/{id} |
| DELETE | /api/admin/users/{id} |

---

## Swagger Documentation

After running the application:

Swagger UI:

http://localhost:8080/swagger-ui/index.html

OpenAPI JSON:

http://localhost:8080/v3/api-docs

---

## Running the Project

1. Clone the repository

```bash
git clone https://github.com/Bhavendan/ecommerce-app.git
```

2. Configure database credentials in `application.properties`

3. Create MySQL database

4. Run the application

```bash
mvn spring-boot:run
```

---


