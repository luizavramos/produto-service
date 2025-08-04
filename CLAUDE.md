# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot microservice for product management (produto-service) that follows clean architecture principles with use cases, domain objects, and gateway patterns. It uses Java 21, Maven, PostgreSQL, Kafka, and includes OpenAPI documentation.

## Development Commands

### Build and Run
```bash
# Compile and package the application
./mvnw clean package

# Run the application
./mvnw spring-boot:run

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=docker
```

### Testing
```bash
# Run all tests
./mvnw test

# Run tests with coverage
./mvnw test jacoco:report

# Run specific test class
./mvnw test -Dtest=ProdutoServiceApplicationTests
```

### Database Migration
```bash
# Run Flyway migrations
./mvnw flyway:migrate

# Check migration status
./mvnw flyway:info
```

### Docker Commands
```bash
# Build and run all services
docker-compose up --build

# Run in background
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f produto-service

# Run with development tools (Kafka UI, pgAdmin)
docker-compose --profile dev up

# Build only the application image
docker build -t produto-service .

# Remove all containers and volumes
docker-compose down -v
```

## Architecture

### Layer Structure
- **Controller**: REST endpoints (`/api/produtos`)
- **Use Cases**: Business logic (CriarProdutoUsecase, BuscarProdutoUsecase, AtualizarProdutoUsecase)
- **Domain**: Core business entities (Produto)
- **Gateway**: Data access abstraction (ProdutoGateway → ProdutoJpaGateway)
- **Entity/Repository**: JPA persistence layer

### Key Patterns
- Clean Architecture with dependency inversion
- Use case driven business logic
- Domain-driven design with rich domain objects
- Gateway pattern for data access
- JSON DTOs for API contracts (ProdutoJson)

### Database
- PostgreSQL with Flyway migrations
- Table: `tb_produto` with indexes on key fields
- Supports soft delete via `ativo` boolean field

### External Integrations
- Kafka messaging (spring-kafka)
- OpenFeign for HTTP clients
- Spring Cloud LoadBalancer
- Actuator endpoints for monitoring

## Configuration

### Profiles
- Default: Local development with PostgreSQL
- Docker: Uses `application-docker.properties`

### Key Properties
- Server runs on port 8082
- Database: `produto_db` on localhost:5432
- Kafka: localhost:9092
- Actuator endpoints: `/health`, `/info`, `/metrics`

## API Endpoints

Base URL: `/api/produtos`

- `POST /` - Create product
- `GET /{id}` - Get product by ID
- `GET /sku/{sku}` - Get product by SKU
- `GET /` - List products (supports filtering by category, price range, active status)
- `PUT /{id}` - Update product
- `PATCH /{id}/preco` - Update price only
- `PATCH /{id}/ativar` - Activate product
- `PATCH /{id}/desativar` - Deactivate product
- `GET /stats` - Get product statistics

## Domain Rules

### Produto Entity
- SKU must be unique and follow pattern `^[A-Z0-9-_]{3,50}$`
- Name: 2-255 characters, required
- Price: Non-negative with max 2 decimal places
- All products are active by default
- Automatic timestamp management (createdAt, updatedAt)