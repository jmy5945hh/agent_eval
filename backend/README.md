# Agent Evaluation Backend

A Spring Boot 2.7 REST API service using DDD (Domain-Driven Design) architecture.

## Tech Stack

- Java 11
- Spring Boot 2.7.18
- MySQL 8
- Redis
- JPA/Hibernate
- Lombok
- MapStruct

## Project Structure

```
com.example.agenteval
├── adaptor                    # Interface Adapters Layer
│   ├── rest                  # REST Controllers
│   └── dto                   # Data Transfer Objects
├── application               # Application Layer
│   ├── command               # CQRS Commands (Write)
│   ├── query                 # CQRS Queries (Read)
│   └── dto                   # Application DTOs
├── domain                    # Domain Layer
│   ├── model                 # Domain Entities
│   ├── repository            # Repository Interfaces
│   └── service               # Domain Services
└── infrastructure            # Infrastructure Layer
    ├── config                # Configuration Classes
    ├── repository            # Repository Implementations
    └── cache                 # Cache Implementations
```

## Getting Started

### Prerequisites

- Java 11+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### Database Setup

1. Create MySQL database:
```sql
CREATE DATABASE agent_eval CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Execute schema.sql to create tables:
```bash
mysql -u root -p agent_eval < src/main/resources/schema.sql
```

### Configuration

Update `src/main/resources/application.yml` with your database and Redis credentials:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/agent_eval
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379
```

### Run Application

```bash
mvn spring-boot:run
```

The application will start on port 8080.

## API Endpoints

### User API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/users | Create a new user |
| GET | /api/users/{id} | Get user by ID |

## Example Request

```bash
# Create user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "phone": "1234567890"
  }'

# Get user
curl http://localhost:8080/api/users/1
```
