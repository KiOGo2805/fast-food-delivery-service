# Fast Food Delivery Service

A robust Spring Boot application for managing a fast-food delivery service, featuring product management, order processing, and integration with Keycloak for security.

## 🚀 Features

- **Product Management**: CRUD operations for products with validation.
- **Order Processing**: Place orders, track status, and manage order items.
- **Security**: OAuth2 and JWT-based security using Keycloak.
- **AOP Support**: Custom annotations for execution time logging, result logging, and store status checks.
- **Async Operations**: Asynchronous processing for order details.
- **Database Migrations**: Liquibase for managed database schema changes.
- **Containerization**: Docker Compose integration for local development (MySQL, Keycloak).
- **Quality Assurance**: Integrated JaCoCo for code coverage and WireMock for integration testing.

## 🛠 Tech Stack

- **Java**: 21
- **Framework**: Spring Boot 3.5.16
- **Database**: MySQL 8.0
- **Migration**: Liquibase
- **Security**: Spring Security (OAuth2 / JWT / Keycloak)
- **Mapping**: MapStruct
- **Utilities**: Lombok
- **Testing**: JUnit 5, Mockito, WireMock, Jacoco
- **Infrastructure**: Docker Compose

## 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.9+**
- **Docker & Docker Compose**

## 🏁 Getting Started

### 1. Start Infrastructure
Use Docker Compose to launch MySQL and Keycloak:
```bash
docker-compose up -d
```
- **MySQL**: Accessible on port `3307`.
- **Keycloak**: Accessible on port `8080`. The `course-realm.json` is automatically imported.

### 2. Configure Environment
The application is pre-configured to connect to the Docker containers via `src/main/resources/application.yml`.

### 3. Build and Run
```bash
mvn clean install
mvn spring-boot:run
```
The server will start on `http://localhost:8081`.

## 🔌 API Endpoints

### Products
- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create new product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product (Admin only)
- `GET /api/products/info` - Get client info (custom annotation demo)

### Orders
- `POST /api/orders` - Place a new order
- `GET /api/orders/mine` - View current user's orders
- `GET /api/orders/{id}` - Get order details
- `PATCH /api/orders/{id}/status` - Update order status (Admin only)
- `GET /api/orders` - List all orders (Admin only)
- `GET /api/orders/{id}/async-details` - Get order details asynchronously

## 🧩 Custom Annotations & AOP

The project includes several custom annotations to simplify cross-cutting concerns:

- `@ExecutionTime`: Logs the execution time of a method.
- `@LogResult`: Logs the return value of a method.
- `@RequireStoreOpen`: Ensures the store is "open" before proceeding (demonstrates AOP logic).
- `@ClientInfo`: Custom argument resolver to inject client information into controllers.
- `@PostCreated`: Custom status mapping for creation endpoints.

## 🧪 Testing & Quality

- **Run Tests**: `mvn test`
- **Coverage**: JaCoCo reports are generated in `target/site/jacoco/index.html` after running `mvn verify`.
- **WireMock**: Used for simulating external services in integration tests.

## 📜 Documentation
Additional configuration examples (e.g., Jacoco) can be found in the `docs/` folder.
