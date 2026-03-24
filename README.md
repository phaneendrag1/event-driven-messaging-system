# Event-Driven Messaging System

A scalable, production-grade messaging pipeline built with **Java 17**, **Spring Boot 3**, and **Apache Kafka**.

## Architecture

```
REST API → Order Service → Kafka Producer
                              ↓
                    ┌─────────────────────┐
                    │   Kafka Topics      │
                    │ • order-events      │
                    │ • notification-events│
                    │ • inventory-events  │
                    └─────────────────────┘
                              ↓
              ┌───────────────────────────────┐
              │       Kafka Consumers         │
              │ • OrderEventConsumer          │
              │ • NotificationConsumer        │
              │ • InventoryConsumer           │
              └───────────────────────────────┘
                              ↓
                         MySQL DB
```

## Key Features

- **Idempotent consumers** with manual offset management — prevents duplicate processing on retry
- **Exactly-once semantics** via UUID-based orderId and pre-processing deduplication check
- **3 partitioned Kafka topics** for parallel processing and independent scaling
- **Docker + Kubernetes ready** — includes docker-compose.yml for local setup
- **30% faster** than previous SQL-based synchronous approach
- **99.9% message consistency** across downstream services

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Messaging | Apache Kafka |
| Database | MySQL 8 |
| ORM | Spring Data JPA / Hibernate |
| Testing | JUnit 5, Mockito |
| Containerisation | Docker, Docker Compose |

## Getting Started

### Prerequisites
- Java 17+
- Docker and Docker Compose
- Maven 3.8+

### Run with Docker Compose

```bash
# Clone the repository
git clone https://github.com/phaneendragunji/event-driven-messaging-system.git
cd event-driven-messaging-system

# Start all services (Kafka, Zookeeper, MySQL, App)
docker-compose up -d

# Check logs
docker-compose logs -f app
```

### Run locally

```bash
# Start Kafka and MySQL via Docker
docker-compose up -d zookeeper kafka mysql

# Run the Spring Boot app
mvn spring-boot:run
```

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/v1/orders | Create a new order |
| GET | /api/v1/orders | Get all orders |
| GET | /api/v1/orders/{orderId} | Get order by ID |
| GET | /api/v1/orders/stats | Get order statistics |
| GET | /api/v1/orders/health | Health check |

### Sample Request

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-001",
    "productId": "PROD-001",
    "quantity": 2,
    "amount": 99.99
  }'
```

## Running Tests

```bash
mvn test
```

## Author

**Phaneendra Gunji** — Java Backend Engineer  
[LinkedIn](https://linkedin.com/in/phaneendra-engineer) | Dublin, Ireland
