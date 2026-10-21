# Codex Handoff / Continuation State

Last updated: 2026-10-21

This file is the continuation record for Codex or another developer working
on the `micarch` repository. Read this file before inspecting the project so
the implementation history does not need to be reconstructed from scratch.

## Project intent

The repository is a Java/Spring Boot microservices architecture learning
project organized into modules 01 through 09. The implementation follows
`mic_arch_implementation_plan.md`.

The requested workflow is:

1. Implement each module and its sub-modules.
2. Keep each module README updated with configuration, build, run commands,
   and Mermaid diagrams.
3. Maintain an `iprep.md` interview-preparation cheat sheet for each example.
4. Test each module and fix implementation errors.
5. Provide Docker support for the complete module set.

## Completed implementation scope

### Module 01 — Decomposition

Implemented and documented:

- `01a-domain-driven-decomposition`
- `01b-strangler-fig`
- `01c-bulkhead`

Each has README documentation, Mermaid diagrams, build/run instructions, and
an `iprep.md` file.

Docker services:

| Example | Compose service | Host port |
|---|---|---:|
| 01a | `ddd-ecommerce` | 8101 |
| 01b | `strangler-fig` | 8102 |
| 01c | `bulkhead` | 8103 |

### Modules 02–09

Application services have been added to `docker-compose.modules.yml` for all
implemented examples in modules 02–09. The overlay currently resolves 33
application services, including the circuit-breaker payment-target container.

The module groups are:

- 02 Communication
- 03 Data management
- 04 Resilience
- 05 Discovery/configuration
- 06 Observability
- 07 Security
- 08 Deployment
- 09 Legacy patterns

README files and `iprep.md` files were added or updated as implementation work
progressed.

## Docker architecture

### Compose files

- `docker-compose.yml` — shared infrastructure.
- `docker-compose.modules.yml` — application containers for modules 01–09.
- `docker/app.Dockerfile` — reusable Maven build plus Temurin JRE runtime image.
- `DOCKER.md` — Docker usage and service/port mapping documentation.

### Shared infrastructure

The root Compose file provides:

- Zookeeper and Kafka
- PostgreSQL
- MongoDB
- Redis
- Axon Server
- Zipkin
- Keycloak
- Elasticsearch, Kibana, and Logstash
- Spring Boot Admin

Kafka uses dual listeners:

- Host applications: `localhost:9092`
- Containers: `kafka:29092`

### Windows bind-mount workaround

Docker Desktop on the development machine stalled on file bind mounts. Local
configuration files are therefore embedded in small custom images:

- `infra/postgres/Dockerfile`
- `infra/keycloak/Dockerfile`
- `infra/logstash/Dockerfile`

The application image creates an empty `/config-repo` directory for the
centralized-config example instead of mounting the Windows source directory.

## Important files changed for Docker

- `docker-compose.yml`
- `docker-compose.modules.yml`
- `docker/app.Dockerfile`
- `infra/postgres/Dockerfile`
- `infra/postgres/init.sql`
- `infra/keycloak/Dockerfile`
- `infra/logstash/Dockerfile`
- `infra/config-repo/README.md`
- `.dockerignore`
- `DOCKER.md`

The root PostgreSQL initialization script includes the databases required by
modules 03 and 01a, including:

- `orders_db`
- `products_db`
- `cqrs_write_db`
- `cqrs_read_db`
- `ddd_ecommerce`

`ddd_ecommerce` was also created manually in the currently running PostgreSQL
container because the named volume had already been initialized.

## Validation already completed

- Combined Compose configuration validates successfully:

  ```bash
  docker compose -f docker-compose.yml -f docker-compose.modules.yml \
    --profile apps config --quiet
  ```

- The root infrastructure stack was started successfully.
- PostgreSQL, Kafka, Zookeeper, Redis, MongoDB, Axon Server, Zipkin,
  Elasticsearch, Kibana, Logstash, Keycloak, Kafka UI, and Spring Boot Admin
  were running during the last infrastructure validation.
- PostgreSQL was verified to contain the module databases listed above.
- The 04a circuit-breaker Maven tests passed after adding the configurable
  payment target and payment-target controller.
- The three module 01 Docker builds reached the Maven packaging phase without
  Docker configuration errors, but the focused build was stopped after a long
  period of quiet Maven execution. Do not claim that all application images
  have been built until the command below completes.

## Current known limitation / next task

The Compose definitions are complete and validated, but building every
application image is expensive because the reusable Dockerfile runs a Maven
reactor build for each selected module. The next practical task is to build
the application images in controlled groups, then start and smoke-test them.

Recommended first command:

```bash
docker compose -f docker-compose.yml -f docker-compose.modules.yml \
  --profile apps build bulkhead
```

Then continue by module group or service:

```bash
docker compose -f docker-compose.yml -f docker-compose.modules.yml \
  --profile apps build ddd-ecommerce strangler-fig
```

After image builds complete:

```bash
docker compose -f docker-compose.yml -f docker-compose.modules.yml \
  --profile apps up -d

docker compose -f docker-compose.yml -f docker-compose.modules.yml \
  --profile apps ps
```

Do not run `docker compose down -v` unless the user explicitly confirms that
all local Docker volume data may be discarded. PostgreSQL initialization only
runs automatically for a newly created named volume.

## Resume checklist

1. Read this file and `mic_arch_implementation_plan.md`.
2. Run `git status --short`; preserve the existing uncommitted work.
3. Validate Compose before changing anything:

   ```bash
   docker compose -f docker-compose.yml -f docker-compose.modules.yml \
     --profile apps config --quiet
   ```

4. Inspect module-specific README and `iprep.md` files before modifying a
   module.
5. Build/test in small groups rather than launching all Maven Docker builds
   in parallel.
6. Update this file whenever a module is tested, a Docker issue is fixed, or a
   new limitation is discovered.

## Working-tree safety

The repository contains extensive existing user/project changes, including
modified and untracked source, tests, READMEs, and interview-preparation files.
Do not use `git reset --hard`, `git checkout --`, or broad destructive cleanup.
Only modify files required for the current requested task.
