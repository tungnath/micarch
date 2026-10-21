# Interview preparation — Database per Service

Each bounded context owns its schema, lifecycle, migrations, credentials, and read/write model. Other services consume APIs or events, never another service's tables.

- Why does database-per-service improve autonomy? It removes schema coupling and lets each team choose storage and release cadence.
- How do you join data across services? Compose API responses, publish projections, or use a workflow; never create distributed SQL joins.
- What replaces a distributed transaction? A saga with idempotent steps and compensating actions.
- Is polyglot persistence free? No: operations, backups, observability, skills, and consistency models multiply.
- How do you migrate a schema? Versioned migrations, expand/contract changes, backward-compatible rollout, then cleanup.

Trick: a cache is not authoritative, and a reporting replica can silently recreate coupling if services depend on undocumented table shape.
