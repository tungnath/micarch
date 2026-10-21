# Claude Code Handoff / Continuation State

Last updated: 2026-10-21

This file is the continuation record for Claude Code or another developer
working on the `micarch` repository. Read it before inspecting the project so
the implementation history does not need to be reconstructed from scratch.

## Project intent

This is a Java/Spring Boot microservices architecture learning project
organized into modules 01 through 09. The implementation follows
`mic_arch_implementation_plan.md`.

The project workflow is to implement each module, keep its README and Mermaid
diagrams current, maintain an `iprep.md` interview cheat sheet, test the code,
and provide Docker support for the complete module set.

## Current implementation state

Modules 01 through 09 have implementation, README, interview-preparation,
and Docker work in progress or completed across the repository.

Module 01 contains:

- `01a-domain-driven-decomposition`
- `01b-strangler-fig`
- `01c-bulkhead`

The shared Docker overlay contains these module 01 services:

| Example | Compose service | Host port |
|---|---|---:|
| 01a | `ddd-ecommerce` | 8101 |
| 01b | `strangler-fig` | 8102 |
| 01c | `bulkhead` | 8103 |

`docker-compose.modules.yml` also contains application services for modules
02 through 09. The resolved model currently contains 33 application services,
including the circuit-breaker payment-target container.

## Docker files

- `docker-compose.yml` — shared infrastructure.
- `docker-compose.modules.yml` — application containers for modules 01–09.
- `docker/app.Dockerfile` — reusable Maven build and Temurin JRE runtime image.
- `DOCKER.md` — Docker commands and service/port mappings.

Shared infrastructure includes Kafka, PostgreSQL, MongoDB, Redis, Axon Server,
Zipkin, Keycloak, Elasticsearch, Kibana, Logstash, and Spring Boot Admin.

Kafka addresses are:

- Host applications: `localhost:9092`
- Containers: `kafka:29092`

PostgreSQL initialization includes `orders_db`, `products_db`,
`cqrs_write_db`, `cqrs_read_db`, and `ddd_ecommerce`.

Because Docker Desktop on the original Windows machine stalled on file bind
mounts, PostgreSQL, Keycloak, and Logstash local configuration files are
embedded in custom images under `infra/`. The application image creates an
empty `/config-repo` directory for centralized-config.

## Validation state

The combined Compose model has been validated with:

```bash
docker compose -f docker-compose.yml -f docker-compose.modules.yml \
  --profile apps config --quiet
```

The shared infrastructure stack was started successfully, and the required
PostgreSQL databases were verified. The 04a circuit-breaker Maven tests passed
after adding its configurable payment target.

The module 01 Docker builds reached Maven packaging without Docker
configuration errors, but the focused build was stopped after a long period
of quiet Maven execution. Do not claim that all application images have been
built until the build command completes.

## Recommended continuation

Read `codex.md` for the fuller Codex handoff record, then inspect the relevant
module README and `iprep.md` before making changes.

Validate Compose first:

```bash
docker compose -f docker-compose.yml -f docker-compose.modules.yml \
  --profile apps config --quiet
```

Build application images in small groups rather than launching all Maven
Docker builds in parallel:

```bash
docker compose -f docker-compose.yml -f docker-compose.modules.yml \
  --profile apps build bulkhead
```

Then start the application layer:

```bash
docker compose -f docker-compose.yml -f docker-compose.modules.yml \
  --profile apps up -d

docker compose -f docker-compose.yml -f docker-compose.modules.yml \
  --profile apps ps
```

Do not run `docker compose down -v` unless the user explicitly confirms that
all local Docker volume data can be discarded.

## Working-tree safety

The repository contains extensive existing user/project changes, including
modified and untracked source, tests, READMEs, and interview-preparation files.
Preserve those changes. Do not use `git reset --hard`, `git checkout --`, or
broad destructive cleanup.

Update both `CLAUDE.md` and `codex.md` when a meaningful implementation,
Docker, testing, or continuation-state change is completed.
