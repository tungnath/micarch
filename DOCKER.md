# Docker environments

The repository has two Compose layers:

| File | Purpose |
|---|---|
| `docker-compose.yml` | Shared infrastructure: Kafka, PostgreSQL, MongoDB, Redis, Axon Server, Zipkin, Keycloak, ELK, and Spring Boot Admin |
| `docker-compose.modules.yml` | Application containers for every example in modules 01–09 |

## Start infrastructure only

```bash
docker compose up -d
docker compose ps
```

## Build and start modules 01–09

Run this from the repository root. The first run builds each selected module
with Maven inside its Docker build stage.

```bash
docker compose -f docker-compose.yml -f docker-compose.modules.yml \
  --profile apps up -d --build
docker compose -f docker-compose.yml -f docker-compose.modules.yml \
  --profile apps ps
```

To start only one example and its required infrastructure, use its Compose
service name, for example:

```bash
docker compose -f docker-compose.yml -f docker-compose.modules.yml \
  --profile apps up -d --build cqrs postgres mongodb kafka zookeeper
```

To stop the application layer while retaining infrastructure:

```bash
docker compose -f docker-compose.yml -f docker-compose.modules.yml \
  --profile apps stop
```

To remove containers and the application network (named data volumes are kept):

```bash
docker compose -f docker-compose.yml -f docker-compose.modules.yml \
  --profile apps down
```

## Container mapping

The module Compose service names correspond to the module names used in the
project README files. Host ports are intentionally aligned with the examples,
with these collision-safe mappings:

| Module | Compose service | Host port |
|---|---|---:|
| 01a–01c | `ddd-ecommerce`, `strangler-fig`, `bulkhead` | 8101–8103 |
| 02a–02e | `api-gateway`, `bff`, `sync-rest`, `async-messaging`, `grpc-service` | 8080, 8201–8204 |
| 03a–03f | `database-per-service`, `saga-choreography`, `saga-orchestration`, `cqrs`, `event-sourcing`, `shared-database` | 8311, 8301, 8303–8306 |
| 04a–04d | `circuit-breaker`, `retry-timeout`, `rate-limiter`, `sidecar` | 8401, 8422, 8403–8404 |
| 05a–05c | `service-registry`, `centralized-config`, `service-mesh` | 8761, 8888, 8501 |
| 06a–06c | `distributed-tracing`, `log-aggregation`, `health-check-aggregation` | 8601–8602, 8091 |
| 07a–07b | `oauth2-jwt`, `gateway-security` | 8701–8702 |
| 08a–08c | `blue-green`, `canary`, `ambassador` | 8801–8803 |
| 09a–09c | `monolith`, `anti-corruption-layer`, `shared-kernel` | 9001–9003 |

The 03a service uses host port 8311 because 03b already owns 8301. The 04b
service uses host port 8422 because the circuit-breaker demo reserves 8402 for
its payment-target container. The application ports inside the containers are
unchanged.

## Container networking notes

The Kafka broker advertises `localhost:9092` to host-run applications and
`kafka:29092` to containers. The module overlay supplies the internal address
through `SPRING_KAFKA_BOOTSTRAP_SERVERS`. Database, Redis, Eureka, Zipkin,
Axon Server, and Keycloak endpoints are similarly overridden with Compose
service names at runtime.

The PostgreSQL, Keycloak, and Logstash initialization files are embedded in
small local images. The centralized-config example receives an empty
`/config-repo` directory in its application image; add configuration files to
the source repository and rebuild that image when demonstrating a Git-backed
configuration repository.

PostgreSQL database initialization runs only when the named volume is created.
If the database volume predates a newly added database, recreate it explicitly
after confirming that its data can be discarded:

```bash
docker compose down -v
docker compose up -d postgres
```
