# Fast Food Delivery Service

The repository is a Maven multi-module build containing a shared `common` module and two independently runnable Spring Boot services:

- `product-service` owns the product catalog and stock, listens on port `8082`, and uses the `products_db` schema.
- `order-service` owns orders, listens on port `8081`, and uses the `orders_db` schema. Orders and stock reservations are coordinated asynchronously through RabbitMQ; there are no direct HTTP calls between these services.

Both services have their own entry point, configuration, tests, and Dockerfile. The `common` module contains classes shared by the services. The root POM aggregates all modules and contains shared build settings and test dependencies.

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

The Compose file starts only the infrastructure: MySQL, RabbitMQ, and Keycloak. Start it from the repository root:

```powershell
docker compose up -d
```

On first startup, the MySQL initialization script creates `orders_db` and `products_db` and grants the configured `user` account access. The Compose file does not declare a named MySQL volume: the image uses an anonymous volume. `docker compose down` leaves that volume behind, but a later `up` may create a new one instead of reusing it. To reset and remove the current volume, run `docker compose down -v`; **this permanently deletes its data**.

Keycloak imports the realm configuration from `./keycloak` and is available at `http://localhost:8080`. Its default admin credentials are `admin` / `admin`. The application services are not part of Compose: run `product-service` and `order-service` from the IDE. Their local defaults connect to MySQL at `localhost:3307`, RabbitMQ at `localhost:5672`, and Keycloak at `localhost:8080`. Start the infrastructure before launching the applications.

Check the infrastructure status and logs. RabbitMQ's management UI is available at `http://localhost:15672` (credentials: `fastfood` / `fastfood` by default):

```powershell
docker compose ps
docker compose logs -f mysql rabbitmq keycloak
```

After starting the applications, open the order-service UI at `http://localhost:8081/store.html` and the product API at `http://localhost:8082/api/products`. MySQL is exposed on host port `3307`. Use `Ctrl+C` to stop following logs; containers keep running. To stop the infrastructure while preserving MySQL data, run `docker compose down` (without `-v`).

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

The Dockerfiles build standalone application images; Compose does not currently run those images. When running the services from an IDE, the broker default is `localhost:5672`. The applications accept `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, and `RABBITMQ_PASSWORD` environment variables if you need to override the defaults. Set `RABBITMQ_PASSWORD` to a non-demo password outside local development.

## Order event flow

`POST /api/orders` stores an order with status `PENDING` and an outbox message in the same database transaction. The order service periodically publishes committed outbox records to RabbitMQ. The product service atomically checks and reserves all requested quantities, then writes a result event to its own outbox. The order service consumes that result and stores product names, price snapshots, the total, and the final `CONFIRMED` status. If a product is missing or stock is insufficient, no stock is reserved and the order becomes `DECLINED`.

The order response is therefore initially pending; query `GET /api/orders/{id}` or `GET /api/orders/mine` to see the eventual status and calculated total. The order service also publishes cancellation and status-change commands; product-service releases active reservations on cancellation and keeps an order-status projection. Both services use durable queues, publisher confirms, transactional outboxes, and persistent processed-message records so broker outages and redelivery do not lose or apply an order twice. `RABBITMQ_LISTENER_ENABLED=false` can be used to stop a consumer temporarily while its durable queue continues holding messages.

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
