# Interview Preparation — Domain-Driven Decomposition

## 60-second explanation

This module decomposes an e-commerce domain into Order, Inventory, and Payment bounded contexts. Each context owns its domain model and persistence schema. The contexts communicate through Kafka integration events, so the order workflow is eventually consistent and uses compensation when stock reservation or payment fails.

The sample keeps the contexts in one Spring Boot process for learning. In production, the package boundaries, schemas, event contracts, and repositories are extraction seams for separate deployable services.

## Architecture points

- `Order` owns order lifecycle and customer-facing order state.
- `InventoryItem` and `StockReservation` own stock availability and reservation state.
- `Payment` owns payment status, amount, method, and failure reason.
- PostgreSQL schemas make ownership visible: `order_ctx`, `inventory_ctx`, and `payment_ctx`.
- Kafka events cross context boundaries; repositories and entities do not.
- The workflow is `OrderPlaced → StockReserved → PaymentCaptured → OrderConfirmed`.
- Failure paths cancel the order and release any reservation already created.

## Likely interview questions

### Why use bounded contexts instead of only Java packages?

Packages are a code boundary, but a bounded context also defines ownership, language, data model, integration contracts, and team responsibility. This example demonstrates both package isolation and separate database schemas.

### Why is the order initially `PENDING`?

The order service commits before downstream inventory and payment work completes. `PENDING` represents eventual consistency; it avoids holding one distributed transaction open across Kafka and multiple databases.

### Why are integration events records instead of shared domain entities?

Events are stable messages describing a business fact. Sharing entities would couple persistence structure and internal invariants between contexts. A context must evolve its aggregate without breaking another context.

### What prevents duplicate Kafka delivery from charging twice?

Kafka provides at-least-once delivery, so consumers must be idempotent. A production payment consumer would use an idempotency key such as `orderId` or an event ID, enforce a database uniqueness constraint, and return the existing result when the event is replayed.

### Why are Kafka type headers enabled?

Several event types share the same topics. Type headers allow Spring Kafka to deserialize each message according to its actual event class. Forcing one default type per listener is unsafe when a topic contains multiple event types.

### Is the database update and Kafka publish atomic here?

No. The sample publishes with `KafkaTemplate` after persistence, but it does not implement a transactional outbox. A production design would persist the business change and an outbox record in one database transaction, then publish the outbox asynchronously with retry and deduplication.

## Tricky questions

### What happens if payment succeeds but `PaymentCapturedEvent` is lost?

The order can remain `PENDING` even though payment was captured. The system needs durable publication, consumer retries, reconciliation, and an operational query to compare payment and order states. An outbox substantially reduces publication-loss risk.

### What happens if the order confirmation event is processed twice?

`Order.confirm()` must reject or safely ignore an already-confirmed transition. The consumer should also record processed event IDs or use an idempotent state transition. “Kafka delivered once” is not a valid assumption.

### Why not use a distributed two-phase commit?

It adds coordination, latency, operational coupling, and poor availability characteristics across services. Local transactions plus events and compensation are usually more appropriate for microservices.

### Is releasing stock on payment failure always correct?

Usually, but the business policy may keep a reservation for a payment retry window. The correct answer depends on reservation expiry, payment retry semantics, and inventory scarcity.

### What is intentionally simplified?

The sample uses one process, simulated payment randomness, direct event publishing, no schema registry, and no dead-letter workflow. These are teaching simplifications, not production recommendations.

## Production improvements

- Separate each bounded context into its own deployable service and database.
- Add an outbox/inbox pattern with event IDs and deduplication.
- Add retries, backoff, dead-letter topics, observability, and correlation IDs.
- Version event schemas and validate compatibility in CI.
- Add contract tests between producers and consumers.
- Replace random payment simulation with an anti-corruption adapter for a provider.

## Implementation walkthrough

Be prepared to trace one successful request in this order:

1. `OrderController` converts the HTTP request into domain value objects and line items.
2. `OrderService.placeOrder` creates and persists the `Order` aggregate as `PENDING`.
3. `OrderPlacedEvent` is published with only the data needed by Inventory.
4. `InventoryService` reserves each line item and publishes `StockReservedEvent` with the order total.
5. `PaymentService` creates a payment, captures or fails it, and publishes the result.
6. Order reacts by confirming or cancelling; Inventory reacts by committing or releasing the reservation.

The important architectural answer is that no service reaches into another bounded context's repository or entity. Events are the integration boundary.

## Failure and consistency matrix

| Failure | Immediate state | Compensation or recovery |
|---|---|---|
| Product does not exist | Order remains `PENDING` briefly | Inventory publishes failure; Order becomes `CANCELLED` |
| Insufficient stock | Partial reservations may exist | Inventory releases all reservations for the order |
| Payment fails | Stock is reserved | Order is cancelled and Inventory releases stock |
| Payment succeeds, event is delayed | Payment is `CAPTURED`, Order may be `PENDING` | Retry/reconciliation eventually confirms the order |
| Consumer crashes after DB commit | Kafka message is redelivered | Idempotent consumer and state transition prevent duplicate effects |
| Event publish fails after DB commit | Local state changed, event absent | Outbox publisher or reconciliation is required |

## DDD distinctions to explain clearly

| Concept | Interview explanation |
|---|---|
| Entity | Has identity and a lifecycle, such as `Order` or `Payment`. |
| Value object | Defined by value and usually immutable, such as `Money` or `ShippingAddress`. |
| Aggregate | Consistency boundary with one root that protects invariants. |
| Bounded context | Explicit boundary for a model, language, ownership, and integration contract. |
| Domain event | A fact meaningful inside a domain model. |
| Integration event | A versioned contract published for another context or service. |
| Application service | Coordinates use cases, persistence, and integration without owning domain invariants. |

## Design exercise: productionizing this sample

If asked to make this production-ready, answer in layers:

1. Split the process into three deployables and give each one an independently owned database.
2. Add an event ID, aggregate ID, occurred-at timestamp, schema version, and correlation ID to every event.
3. Replace direct `KafkaTemplate` publishing with a transactional outbox.
4. Add inbox/idempotency records and unique constraints for payment capture and reservation commits.
5. Add retry and dead-letter policies with bounded backoff.
6. Define reconciliation jobs for orders, payments, and inventory that disagree.
7. Add contract tests, metrics, tracing, access control, and operational dashboards.

## Rapid-fire questions

### Why is a separate schema useful if the process is still one application?

It makes ownership visible and prevents accidental cross-context table access. It is not equivalent to true service isolation, but it creates an extraction seam.

### Should an integration event contain the entire aggregate?

Usually no. Publish the minimum stable contract needed by consumers. Full aggregate snapshots increase coupling, payload size, and privacy risk.

### Where should validation live?

Boundary validation belongs in the API layer, while business invariants belong in the aggregate. Consumers must still validate event contracts because messages can be stale or malformed.

### How would you support a payment retry?

Keep the order in a defined retryable state, retain or expire the inventory reservation by policy, use an idempotency key for each payment attempt, and emit explicit payment-attempt events.

### What would you monitor?

Pending-order age, reservation age, payment success/failure rate, event consumer lag, retry and dead-letter counts, reconciliation mismatches, and the time from placement to final state.

## Code areas to know

- `order/domain/Order.java` — aggregate lifecycle and invariants.
- `inventory/domain/InventoryItem.java` — reserve, release, and commit stock rules.
- `payment/domain/Payment.java` — payment lifecycle.
- `*/service/*Service.java` — application orchestration and event listeners.
- `shared/vo/Money.java` — amount and currency value object.
- `src/main/resources/application.yml` — Kafka, database, and payment configuration.
