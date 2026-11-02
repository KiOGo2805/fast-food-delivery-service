# Fast Food Delivery Service

The repository is a Maven multi-module build containing two independently runnable Spring Boot services:

- `product-service` owns the product catalog and stock, listens on port `8082`, and uses the `products_db` schema.
- `order-service` owns orders, listens on port `8081`, and uses the `orders_db` schema. It reserves stock through the product service's internal HTTP API and releases completed reservations if order creation fails; it has no Maven dependency on `product-service`.

Both modules have their own entry point, configuration, tests, and Dockerfile. The root POM aggregates the modules and contains shared build settings and test dependencies.

## Requirements

- Java 21+
- Maven 3.9+
- Docker Compose

## Build and test

Run the complete reactor from the repository root:

```bash
mvn verify
```

Inspect either service's dependency tree independently:

```bash
mvn -pl order-service dependency:tree
mvn -pl product-service dependency:tree
```

## Local development

From the repository root, remove the old containers and MySQL volume if you want to reset the local databases. **`down -v` permanently deletes the data stored in the Compose volumes.**

```powershell
docker compose down -v
$env:PRODUCT_SERVICE_API_KEY = "local-dev-change-this-key"
docker compose up -d --build
```

This starts MySQL, Keycloak, `product-service`, and `order-service`. On a fresh MySQL volume, the initialization script creates `orders_db` and `products_db` and grants the configured `user` account access. The services wait for MySQL's health check before starting.

Keycloak is configured with `http://localhost:8080` as its canonical issuer. This keeps the token issuer consistent when the browser authenticates through `localhost` and the services contact Keycloak over Docker's internal network.

Check startup and follow the service logs:

```powershell
docker compose ps
docker compose logs -f mysql product-service order-service
```

Open the order-service UI at `http://localhost:8081/store.html`, the product API at `http://localhost:8082/api/products`, and Keycloak at `http://localhost:8080`. MySQL is available on host port `3307`. Use `Ctrl+C` to stop following logs; containers keep running. To stop containers but preserve the databases, run `docker compose down` (without `-v`).

For an existing volume where the databases were not initialized, either reset it with the commands above (this erases all database data), or create both databases and grant permissions without deleting the volume:

```powershell
docker compose exec -T mysql mysql -uroot -proot -e "CREATE DATABASE IF NOT EXISTS orders_db; CREATE DATABASE IF NOT EXISTS products_db; GRANT ALL PRIVILEGES ON orders_db.* TO 'user'@'%'; GRANT ALL PRIVILEGES ON products_db.* TO 'user'@'%';"
```

## Docker images

Build each image from the repository root:

```bash
docker build -f product-service/Dockerfile -t fastfood-product-service .
docker build -f order-service/Dockerfile -t fastfood-order-service .
```

The Compose configuration sets the internal service URLs and ports. For deployment, provide a unique `PRODUCT_SERVICE_API_KEY`, configure external database credentials, and adjust the browser-facing URLs and allowed origin.

When running the services separately outside Compose, both use `12345` as the development fallback for `PRODUCT_SERVICE_API_KEY`. Set the same key explicitly in both environments for any non-local deployment.

## API

### Products (`http://localhost:8082`)

- `GET /api/products` — list products
- `GET /api/products/{id}` — get a product
- `POST /api/products` — create a product (admin)
- `PUT /api/products/{id}` — update a product (admin)
- `DELETE /api/products/{id}` — delete a product (admin)
- `GET /api/products/info` — client information demo

### Orders (`http://localhost:8081`)

- `POST /api/orders` — place an order
- `GET /api/orders/mine` — view current user's orders
- `GET /api/orders/{id}` — get order details
- `PATCH /api/orders/{id}/status` — update order status (admin)
- `GET /api/orders` — list all orders (admin)
- `GET /api/orders/{id}/async-details` — get order details asynchronously

## Testing and quality

Run tests for all services with `mvn test`; `mvn verify` also packages both runnable applications. JaCoCo reports are available under each module's `target/site/jacoco/`.
