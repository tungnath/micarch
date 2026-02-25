# 🏗️ Microservices Architecture Patterns — Learning Project

> A **production-grade** Spring Boot 3.3 multi-module project demonstrating **every major microservices design pattern**, including modern patterns, legacy integration approaches, and common anti-patterns — each with real executable code, Mermaid architecture diagrams, and documented pros & cons.

---

## 📋 Table of Contents
- [Project Structure](#-project-structure)
- [Technology Stack](#-technology-stack)
- [Quick Start](#-quick-start)
- [Pattern Modules](#-pattern-modules)
- [Infrastructure Services](#-infrastructure-services)
- [Learning Path](#-learning-path)

---

## 📁 Project Structure

```
micarch/
├── pom.xml                         # Parent Maven aggregator POM
├── docker-compose.yml              # All infrastructure services
├── infra/
│   ├── postgres/init.sql           # Multi-DB initialization
│   ├── logstash/logstash.conf      # ELK log pipeline
│   └── keycloak/realm-export.json  # OAuth2 realm config
│
├── 01-decomposition/               # How to split a monolith
├── 02-communication/               # Service-to-service communication
├── 03-data-management/             # Data patterns & distributed transactions
├── 04-resilience/                  # Fault tolerance patterns
├── 05-discovery-config/            # Service discovery & configuration
├── 06-observability/               # Monitoring, tracing, logging
├── 07-security/                    # Auth & authorization patterns
├── 08-deployment/                  # Deployment strategies
└── 09-legacy-patterns/             # Legacy integration & anti-patterns
```

---

## 🛠 Technology Stack

| Category | Technology |
|---|---|
| Framework | Spring Boot 3.3.x, Spring Cloud 2023.x |
| API Gateway | Spring Cloud Gateway |
| Service Discovery | Netflix Eureka |
| Configuration | Spring Cloud Config Server |
| Messaging | Apache Kafka |
| Resilience | Resilience4j, Bucket4j |
| Data | PostgreSQL, MongoDB, Redis, H2 |
| Event Sourcing | Axon Framework |
| gRPC | grpc-spring-boot-starter, Protobuf |
| Tracing | Micrometer Tracing + Zipkin |
| Logging | ELK Stack (Elasticsearch + Logstash + Kibana) |
| Security | Keycloak, Spring Security OAuth2 |
| Docs | Springdoc OpenAPI (Swagger UI) |
| Testing | JUnit 5, Testcontainers, Spring Security Test |

---

## 🚀 Quick Start

### 1. Start All Infrastructure

```bash
# From the project root
docker-compose up -d

# Wait for services to be healthy (~60s)
docker-compose ps
```

Infrastructure will be available at:
| Service | URL |
|---|---|
| Kafka UI | http://localhost:8090 |
| Spring Boot Admin | http://localhost:8091 |
| Keycloak | http://localhost:8180 |
| Zipkin | http://localhost:9411 |
| Kibana | http://localhost:5601 |
| Elasticsearch | http://localhost:9200 |

### 2. Build All Modules

```bash
mvn clean install -DskipTests
```

### 3. Run a Specific Pattern

```bash
# Example: Run the DDD Order Service
cd 01-decomposition/01a-domain-driven-decomposition
mvn spring-boot:run

# Open Swagger UI
open http://localhost:8101/swagger-ui.html
```

---

## 📚 Pattern Modules

### Group 1 — Decomposition Patterns

| Module | Port | Pattern | Description |
|---|---|---|---|
| [01a-domain-driven-decomposition](01-decomposition/01a-domain-driven-decomposition/) | 8101 | **Domain-Driven Design** | Bounded contexts, aggregates, domain events via Kafka |
| [01b-strangler-fig](01-decomposition/01b-strangler-fig/) | 8102 | **Strangler Fig** | Gateway routing progressively strangling a legacy monolith |
| [01c-bulkhead](01-decomposition/01c-bulkhead/) | 8103 | **Bulkhead** | Thread pool isolation via Resilience4j Bulkhead |

### Group 2 — Communication Patterns

| Module | Port | Pattern | Description |
|---|---|---|---|
| [02a-api-gateway](02-communication/02a-api-gateway/) | 8080 | **API Gateway** | Spring Cloud Gateway with routing, rate-limiting, CORS |
| [02b-backend-for-frontend](02-communication/02b-backend-for-frontend/) | 8201 | **BFF** | Dedicated BFF aggregating APIs for mobile clients |
| [02c-synchronous-rest](02-communication/02c-synchronous-rest/) | 8202 | **Sync REST (Feign)** | OpenFeign client with Eureka load balancing |
| [02d-async-messaging](02-communication/02d-async-messaging/) | 8203 | **Async Messaging** | Kafka producer/consumer with retry and dead-letter |
| [02e-grpc-service](02-communication/02e-grpc-service/) | 8204 | **gRPC** | Protocol Buffers + gRPC for high-performance RPC |

### Group 3 — Data Management Patterns

| Module | Port | Pattern | Description |
|---|---|---|---|
| [03a-database-per-service](03-data-management/03a-database-per-service/) | 8301 | **Database per Service** | Each service owns Postgres, MongoDB, or Redis |
| [03b-saga-choreography](03-data-management/03b-saga-choreography/) | 8302 | **Saga (Choreography)** | Kafka event-driven saga with compensation events |
| [03c-saga-orchestration](03-data-management/03c-saga-orchestration/) | 8303 | **Saga (Orchestration)** | Central saga orchestrator managing state machine |
| [03d-cqrs](03-data-management/03d-cqrs/) | 8304 | **CQRS** | Separate write (JPA) and read (projection) models |
| [03e-event-sourcing](03-data-management/03e-event-sourcing/) | 8305 | **Event Sourcing** | Axon Framework event store, projections, snapshots |
| [03f-shared-database](03-data-management/03f-shared-database/) | 8306 | **Shared Database** ⚠️ | Anti-pattern demo showing coupling pitfalls |

### Group 4 — Resilience Patterns

| Module | Port | Pattern | Description |
|---|---|---|---|
| [04a-circuit-breaker](04-resilience/04a-circuit-breaker/) | 8401 | **Circuit Breaker** | Resilience4j CB with sliding window + fallback |
| [04b-retry-timeout](04-resilience/04b-retry-timeout/) | 8402 | **Retry & Timeout** | Exponential backoff retry with time limiter |
| [04c-rate-limiter](04-resilience/04c-rate-limiter/) | 8403 | **Rate Limiter** | Resilience4j + Bucket4j token-bucket rate limiting |
| [04d-sidecar](04-resilience/04d-sidecar/) | 8404 | **Sidecar** | Helper process for TLS, observability, retries |

### Group 5 — Discovery & Configuration

| Module | Port | Pattern | Description |
|---|---|---|---|
| [05a-service-registry](05-discovery-config/05a-service-registry/) | 8761 | **Service Registry** | Netflix Eureka Server |
| [05b-centralized-config](05-discovery-config/05b-centralized-config/) | 8888 | **Centralized Config** | Spring Cloud Config Server backed by Git |
| [05c-service-mesh](05-discovery-config/05c-service-mesh/) | 8501 | **Service Mesh** | Istio concepts simulated with Spring + Resilience4j |

### Group 6 — Observability

| Module | Port | Pattern | Description |
|---|---|---|---|
| [06a-distributed-tracing](06-observability/06a-distributed-tracing/) | 8601 | **Distributed Tracing** | Micrometer + Zipkin, trace propagation via HTTP headers |
| [06b-log-aggregation](06-observability/06b-log-aggregation/) | 8602 | **Log Aggregation** | Logstash JSON appender → Elasticsearch → Kibana |
| [06c-health-check-aggregation](06-observability/06c-health-check-aggregation/) | 8091 | **Health Aggregation** | Spring Boot Admin aggregating all service health |

### Group 7 — Security

| Module | Port | Pattern | Description |
|---|---|---|---|
| [07a-oauth2-jwt](07-security/07a-oauth2-jwt/) | 8701 | **OAuth2 + JWT** | Keycloak resource server, scope-based authorization |
| [07b-api-gateway-security](07-security/07b-api-gateway-security/) | 8702 | **Gateway Security** | JWT validation filter, route-level authorization |

### Group 8 — Deployment Patterns

| Module | Port | Pattern | Description |
|---|---|---|---|
| [08a-blue-green](08-deployment/08a-blue-green/) | 8801 | **Blue-Green** | Instant traffic flip between v1.0 and v1.1 |
| [08b-canary](08-deployment/08b-canary/) | 8802 | **Canary Release** | Progressive 5→100% traffic shift with auto-rollback |
| [08c-ambassador](08-deployment/08c-ambassador/) | 8803 | **Ambassador** | REST→SOAP proxy with circuit breaker for legacy API |

### Group 9 — Legacy & Anti-Patterns

| Module | Port | Pattern | Description |
|---|---|---|---|
| [09a-monolith-starter](09-legacy-patterns/09a-monolith-starter/) | 9001 | **Monolith Baseline** | The "before" state — all domains in one app |
| [09b-anti-corruption-layer](09-legacy-patterns/09b-anti-corruption-layer/) | 9002 | **Anti-Corruption Layer** | Translates legacy SOAP XML to clean domain model |
| [09c-shared-kernel](09-legacy-patterns/09c-shared-kernel/) | 9003 | **Shared Kernel** | When sharing code helps vs. when it creates coupling |

---

## 🏗 Infrastructure Services

Started via `docker-compose up -d`:

| Service | Purpose | Port |
|---|---|---|
| Kafka | Async messaging backbone | 9092 |
| Kafka UI | Kafka topic browser | 8090 |
| PostgreSQL | Relational OLTP store | 5432 |
| MongoDB | Document store | 27017 |
| Redis | Cache + rate-limit store | 6379 |
| Axon Server | Event store for Event Sourcing | 8024 / 8124 |
| Zipkin | Distributed trace UI | 9411 |
| Keycloak | OAuth2 / OIDC identity provider | 8180 |
| Elasticsearch | Log + search backend | 9200 |
| Kibana | Log visualization UI | 5601 |
| Logstash | Log aggregation pipeline | 5044 |
| Spring Boot Admin | Service health dashboard | 8091 |

---

## 🎓 Learning Path

For the most effective learning experience, follow the modules in this order:

```
09a (Monolith)           ← Start: understand the problem
    ↓
01a (DDD)                ← Learn: how to decompose by domain
01b (Strangler Fig)      ← Learn: the migration strategy
    ↓
02a (API Gateway)        ← Learn: single entry point
02c (Sync REST)          ← Learn: service-to-service HTTP
02d (Async Messaging)    ← Learn: decoupled communication
    ↓
03a (DB per Service)     ← Learn: data ownership
03b (Saga Choreography)  ← Learn: distributed transactions
03e (Event Sourcing)     ← Learn: event-driven state
03d (CQRS)               ← Learn: read/write separation
    ↓
04a (Circuit Breaker)    ← Learn: fault tolerance
04b (Retry/Timeout)      ← Learn: transient failure handling
    ↓
05a (Eureka)             ← Learn: service discovery
05b (Config Server)      ← Learn: externalized config
    ↓
06a (Distributed Tracing)← Learn: observability
06b (Log Aggregation)    ← Learn: structured logging
    ↓
07a (OAuth2/JWT)         ← Learn: securing services
    ↓
08a (Blue-Green)         ← Learn: zero-downtime deployment
08b (Canary)             ← Learn: progressive delivery
```

---

## 🤝 Contributing

Each pattern module follows the same structure:
```
<module>/
├── pom.xml
├── README.md               ← Pattern explanation + pros/cons
└── src/
    └── main/
        ├── java/           ← Production code
        └── resources/
            └── application.yml
```

---

*Built for learning microservices architecture with Spring Boot 3.x — from monolith to cloud-native.*
