# Interview Preparation — Synchronous REST with OpenFeign

## 60-second explanation

Synchronous REST communication gives the caller an immediate response by making
request/response calls between services. OpenFeign provides a declarative client
interface, while service discovery or configured URLs resolve the destination.
This module demonstrates an order service calling inventory and payment in a
blocking chain and highlights the resulting temporal coupling.

## Core concepts

- The caller is blocked until downstream calls complete or fail.
- Feign removes HTTP boilerplate but does not remove network failure modes.
- Connect timeout, read timeout, overall deadline, retries, and circuit breaking are separate policies.
- A synchronous chain's latency is approximately the sum of dependent call latency plus local processing.
- A response contract must distinguish business rejection from transport failure.

## Likely interview questions

### Why use OpenFeign?

It expresses a remote HTTP contract as a Java interface, integrates with Spring
Cloud discovery/load balancing, and reduces repetitive request construction. It
does not make the call local or transactional.

### Feign versus WebClient?

Feign is declarative and usually blocking in this style. WebClient is reactive
and better suited to non-blocking pipelines and streaming. Choose based on the
application execution model and dependency behavior, not preference alone.

### What is temporal coupling?

The caller and callee must both be available at the same time. A downstream
outage or slow response directly affects the caller unless isolation and fallback
policies exist.

### How do you handle a partial failure after reserving stock?

Emit or execute a compensating release if payment fails. Make both reservation
and release idempotent, record the workflow state, and reconcile ambiguous cases.
For complex workflows, use a saga rather than hiding compensation in a controller.

## Tricky questions

### Why is a read timeout not enough?

A read timeout limits one network read, but the total workflow can still exceed
the caller's deadline across multiple calls and retries. Propagate an overall
deadline and budget each downstream call from it.

### Should every Feign call be retried?

No. Retry only transient failures and operations that are safe or idempotent.
Retries on a payment capture or reservation can duplicate side effects unless an
idempotency key and server-side deduplication exist.

### Does HTTP 200 mean the business operation succeeded?

Not necessarily. The response body and contract must express business status.
Transport success only means the server returned a response.

### What happens if the payment response is lost after capture?

The caller sees failure or timeout even though payment may have succeeded. Use an
idempotency key, query/reconciliation endpoint, durable payment state, and a
retry that returns the existing capture result.

### When is synchronous REST the wrong choice?

When workflows are long-running, fan out to many slow services, tolerate eventual
consistency, or must continue while consumers are unavailable. Asynchronous events
or a saga may be a better fit.

## Production hardening checklist

- Set connect, read, and overall request deadlines.
- Add circuit breakers and bounded retries with exponential backoff and jitter.
- Propagate correlation IDs, trace context, auth context, and idempotency keys.
- Use bulkheads to prevent one dependency from consuming all request capacity.
- Map transport errors to stable API errors without leaking internal details.
- Keep Feign DTOs versioned and compatible; do not share persistence entities.
- Monitor dependency latency, error rate, timeout rate, retry count, and saturation.
- Test dependency outage, slow response, malformed response, and duplicate request behavior.

## Design exercise: checkout with synchronous dependencies

An interview-quality answer should include:

1. A strict client deadline and a latency budget for inventory and payment.
2. Idempotency key passed to both side-effecting operations.
3. A state machine for `PENDING`, `RESERVED`, `PAID`, `CONFIRMED`, and compensation states.
4. Compensation and reconciliation for every ambiguous outcome.
5. Circuit breaker and bulkhead isolation per downstream dependency.
6. A decision to move non-critical notifications and analytics to asynchronous events.

## Rapid-fire questions

### Does Feign provide distributed transactions?

No. It only invokes HTTP. Transactional consistency across services requires
business coordination such as a saga, outbox, or compensation.

### Why not share Java DTOs between services?

Shared DTOs can create compile-time coupling and make independent contract
evolution difficult. Use explicit contracts and compatibility tests.

### How do you distinguish 409 from 503?

`409` can represent a known business conflict such as insufficient stock.
`503` represents temporary service unavailability or capacity failure. Keep the
mapping stable and document retry behavior.

### What does service discovery solve?

It resolves logical service names to instances and supports dynamic topology.
It does not solve authentication, timeout, retry, consistency, or business failure.

## Code areas to know

- `InventoryClient.java` — declarative inventory contract.
- `PaymentClient.java` — payment capture contract.
- `SyncOrderService.java` — synchronous workflow and business outcomes.
- `SyncOrderController.java` — HTTP contract and status mapping.
- `DemoDownstreamController.java` — self-contained Feign targets.
- `SyncOrderServiceTest.java` — success and stock-failure tests.
