# Interview Preparation — Backend for Frontend

## 60-second explanation

A Backend for Frontend is a client-specific backend that aggregates and shapes
data for one experience, such as mobile or web. It prevents clients from making
many chatty calls and avoids forcing one generic API to satisfy incompatible
payload, latency, and security needs. This module exposes mobile and web
dashboard contracts and isolates downstream access behind `BffDataGateway`.

## Core concepts

- A BFF owns client representation and orchestration, not core business ownership.
- Different clients can choose different payload sizes, pagination, and freshness.
- Aggregation should be parallel where dependencies are independent.
- Downstream calls need deadlines, partial-failure policy, and observability.
- A BFF can be deployed per client team or client type; one process here is a teaching simplification.

## Likely interview questions

### BFF versus API gateway?

An API gateway provides shared edge concerns such as authentication, routing, and
rate limiting. A BFF provides client-specific aggregation and response shaping.
They can coexist, with the gateway forwarding to the appropriate BFF.

### Why not expose one generic API to every client?

Clients have different bandwidth, interaction, caching, and presentation needs.
A generic API often causes over-fetching for mobile or chatty request patterns for
web. A BFF lets each contract evolve without contaminating another client.

### Should a BFF own business logic?

It may coordinate a client use case, but business invariants and source-of-truth
state belong in domain services. Duplicated pricing, inventory, or payment rules
in a BFF will drift and create inconsistent behavior.

### How should downstream calls be made?

Use asynchronous or parallel calls for independent reads, propagate deadlines and
correlation IDs, handle partial responses explicitly, and apply bounded retries
only to safe operations.

## Tricky questions

### What if one of three downstream calls fails?

Choose by business priority. Return a partial response with a documented degraded
section, use a cached value, or fail the whole dashboard if the missing data makes
the response unsafe. Do not silently fabricate critical data.

### Does a BFF solve the N+1 problem?

Not automatically. It can consolidate client calls, but its own downstream
aggregation can create N+1 traffic. Batch endpoints, bulk queries, caching, and
request-specific limits are still required.

### Should mobile and web share the same BFF codebase?

It depends on ownership and coupling. Sharing libraries for stable cross-cutting
concerns can help, but sharing one deployment for unrelated client contracts can
recreate the coupling the pattern is intended to remove.

### How do you version BFF contracts?

Version by URL, media type, or compatible evolution. Treat each client contract
as an API with consumer-driven contract tests and an explicit deprecation policy.

### How do you prevent a slow downstream from slowing every client?

Use per-dependency timeouts, parallel calls, bulkheads, circuit breakers, cache
fallbacks, and client-specific SLAs. A BFF should not wait indefinitely for the
slowest dependency.

## Design exercise: mobile dashboard at scale

An interview-quality answer should include:

1. A mobile-specific compact contract with pagination and only required fields.
2. Parallel downstream reads with an overall deadline shorter than the mobile client's timeout.
3. Cache policy for low-volatility data and explicit freshness metadata.
4. Partial response semantics for non-critical inventory or recommendation data.
5. Per-client metrics for payload size, downstream latency, fan-out, errors, and cache hits.
6. Authorization checks at the BFF and downstream services, with tenant/user scope propagation.

## Failure matrix

| Failure | Possible response |
|---|---|
| Orders unavailable | Return degraded dashboard without orders if the contract permits |
| Payment service timeout | Return pending-payment data as unavailable, never as zero |
| Inventory stale | Return cached data with freshness timestamp |
| All dependencies unavailable | Fail fast with a traceable 503 response |
| One dependency is slow | Cancel its request when the overall deadline expires |

## Rapid-fire questions

### Is a BFF just an aggregation controller?

No. It includes the client contract, aggregation policy, downstream adapters,
security propagation, caching, deadlines, and observability.

### Where should transformation happen?

Client-specific transformation belongs in the BFF; domain semantic transformation
between bounded contexts belongs in a domain service or anti-corruption layer.

### Can a BFF call a database directly?

Usually no. It should call owned service APIs or read models. Direct database reads
couple it to storage ownership and bypass service authorization and invariants.

### How do you test a BFF?

Unit-test shaping and failure policy, contract-test downstream adapters, and run
integration tests with stubbed dependencies to verify fan-out, deadlines, and
partial responses.

## Code areas to know

- `BffController.java` — mobile/web public contracts.
- `BffDataGateway.java` — downstream abstraction and shared data records.
- `DemoBffDataGateway.java` — deterministic local adapter.
- `HttpBffDataGateway.java` — production transport seam and URL configuration.
- `BffControllerTest.java` — client-specific response-shape tests.
