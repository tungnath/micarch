# Interview Preparation — Strangler Fig Pattern

## 60-second explanation

The Strangler Fig pattern places a gateway in front of a legacy application. New capabilities are routed to replacement services while all unmigrated routes continue to use the monolith. This enables incremental migration, independent rollback, and verification of one capability at a time without a big-bang rewrite.

This module routes `/api/orders/**` to the new service and the remaining `/api/**` traffic to the legacy system. The `demo` profile supplies in-process downstream controllers so routing can be tested without external applications.

## Architecture points

- The gateway preserves the public client contract while downstream paths are rewritten.
- Route order matters: the specific migrated order route must win over `/api/**`.
- Default configuration uses external URIs; the demo profile uses `forward:` routes.
- The gateway is a migration boundary, not a place for business-domain orchestration.
- Each migrated route needs ownership, observability, rollback, and contract validation.

## Likely interview questions

### Why use a strangler migration instead of rewriting the monolith?

It reduces delivery and rollback risk. The organization can migrate one capability, compare behavior, and learn incrementally while the existing system continues serving the remaining functionality.

### How does the gateway decide where a request goes?

It evaluates route predicates. `/api/orders/**` matches the new service first, while the broader `/api/**` route is the fallback for legacy traffic. The gateway rewrites the public path to an internal downstream path.

### Why is the demo route using `forward:` instead of the normal rewrite route?

`forward:` dispatches inside the same application. It is useful for a self-contained test profile, but it does not represent a network hop. The default profile uses external HTTP URIs and `RewritePath` filters.

### Why should a gateway remain thin?

Business logic in the gateway creates a second monolith, makes independent service evolution harder, and couples every client flow to gateway releases. Cross-cutting concerns belong there; domain ownership belongs in services.

### How do you migrate a route safely?

Define the contract, add observability, deploy the replacement behind the gateway, route a controlled scope, compare functional and performance behavior, maintain rollback, and remove the legacy path only after data and traffic ownership are settled.

## Tricky questions

### What happens if `/api/orders/**` is declared after `/api/**`?

The broad route may capture the request first, sending migrated traffic to the monolith. Route specificity and explicit ordering must be tested, not assumed.

### How do you handle data during migration?

Choose an explicit ownership strategy: legacy remains the source of truth temporarily, the new service owns new data, or controlled replication is introduced. Dual writes require reconciliation and idempotency; they are not automatically safe.

### How do you roll back after new orders have been created?

Traffic rollback is easy; data rollback is not. The migration plan must define compatibility, replay or synchronization, and which system owns orders created during the migration window.

### Is a gateway enough to migrate a monolith?

No. Routing only changes traffic location. Successful migration also requires domain ownership, data ownership, contract compatibility, operations, security, observability, and a plan to retire legacy code.

### What is the difference between route fallback and failure fallback?

Route fallback sends an intentionally unmigrated path to the legacy system. Failure fallback handles an error from the new service. Automatically sending all new-service failures to the monolith can duplicate writes or hide defects, so it requires careful policy and idempotency.

## Production improvements

- Add authentication, authorization, rate limiting, request correlation, and audit logging.
- Add route metrics by destination, status, latency, and migration cohort.
- Use feature flags or weighted routing for gradual traffic shifting.
- Add consumer-driven contract tests and backward-compatible API versioning.
- Define data synchronization, reconciliation, and rollback procedures before cutover.
- Remove legacy routes and code deliberately after usage reaches zero.

## Migration decision matrix

| Concern | Safe interview answer |
|---|---|
| Traffic cutover | Start with an internal cohort, tenant, header, percentage, or single route. Keep rollback reversible. |
| Data ownership | Name one source of truth at every phase. Avoid uncoordinated dual writes. |
| API compatibility | Preserve the public contract or introduce an explicit version; translate at the edge only when necessary. |
| Failure handling | Distinguish new-service errors from intentionally unmigrated routes. Do not blindly retry writes against the monolith. |
| Completion | Remove the legacy route only after traffic, data, operational, and compliance ownership have moved. |

## Implementation walkthrough

For a request to `/api/orders/123`:

1. Spring Cloud Gateway evaluates the route predicates.
2. The specific `/api/orders/**` route matches before the broad `/api/**` fallback.
3. In the default profile, the gateway sends an HTTP request to the configured new-service URI and rewrites the path to `/__strangler/new/orders/123`.
4. In the `demo` profile, a `forward:` route dispatches to the in-process replacement controller; this demonstrates routing but not a network hop.
5. An unmigrated path such as `/api/customers/123` follows the legacy route and is rewritten to `/__strangler/legacy/customers/123`.

## Migration phases and risks

| Phase | Primary risk | Required control |
|---|---|---|
| Discovery | Unknown dependencies and hidden callers | Traffic inventory, contract capture, dependency mapping |
| Shadow or compare | Side effects from duplicate execution | Read-only shadowing or idempotent writes |
| Partial cutover | Behavior and latency differences | Cohort routing, metrics, feature flag, rollback |
| Data transition | Divergent records | Single owner, replication, reconciliation |
| Legacy retirement | Orphaned clients and jobs | Usage proof, deprecation window, dependency review |

## Design exercise: migrating orders safely

An architect should propose:

1. Capture the existing order API contract and identify all synchronous and asynchronous consumers.
2. Define the new order service's data ownership and an anti-corruption translation layer for legacy fields.
3. Route only a controlled cohort, with request ID, tenant, route, destination, status, and latency metrics.
4. Make order creation idempotent before allowing retries or rollback between systems.
5. Compare business outcomes, not only HTTP status codes: totals, inventory effects, payment effects, and notifications.
6. Keep a tested rollback plan and reconcile records created during any split-brain window.

## Rapid-fire questions

### Why not put the strangler logic inside the monolith?

That keeps the old application as the migration decision-maker and increases coupling. An independently deployable gateway provides a cleaner traffic boundary and rollback point.

### Does path rewriting solve contract incompatibility?

No. It changes the URL presented downstream. Payload translation, authentication changes, versioning, and semantic differences need explicit adapters or service changes.

### How do you avoid the gateway becoming a bottleneck?

Keep it stateless and thin, scale horizontally, use bounded connection pools, apply timeouts, monitor route-level latency, and avoid domain orchestration.

### What is the difference between blue-green deployment and strangler migration?

Blue-green swaps versions of one capability or application. Strangler migration moves capabilities from a legacy system to new ownership over time; the gateway may use blue-green techniques during each cutover.

### What evidence allows you to remove the monolith route?

No production traffic or scheduled jobs depend on it, data ownership is transferred, reconciliation is clean, rollback is understood, and the route has passed its deprecation period.

## Code areas to know

- `src/main/resources/application.yml` — external routes, rewrite filters, and demo profile.
- `StranglerFigApplication.java` — gateway bootstrap.
- `DemoDownstreamController.java` — in-process replacement and legacy behavior.
- `StranglerFigRoutingTest.java` — route-level verification through `WebTestClient`.
