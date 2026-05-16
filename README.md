# Java Interview Prep Platform

Production-style Spring Boot 3 backend for a YouTube educational series.

## Tech Stack
Java 17, Spring Boot 3, Security, JPA, MySQL, Redis, JWT, OpenAPI, Maven, Lombok, Testcontainers.

## Modules
- auth
- user
- product
- order
- payment
- audit

## Run
```bash
mvn spring-boot:run
```

## API Docs
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Mermaid Architecture
```mermaid
graph TD
  Client --> API[Spring Boot API]
  API --> Security[SecurityFilterChain + JWT Filter]
  API --> Auth[Auth Module]
  API --> User[User Module]
  API --> Product[Product Module]
  API --> Order[Order Module]
  API --> Payment[Payment Module]
  API --> Audit[Audit Module]
  Auth --> MySQL[(MySQL)]
  Order --> MySQL
  API --> Redis[(Redis)]
```

## Database Entities
users, roles, user_roles, products, orders, order_items, payments, audit_logs, refresh_tokens.

## Future Expansion
- @Transactional demos
- Async processing
- Caching
- Kafka integration
- Concurrency scenarios
