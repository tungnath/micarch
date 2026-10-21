# Interview Preparation — 02D Async Messaging with Kafka

## 60-second explanation

The producer publishes an `OrderCreatedEvent` and returns `202 Accepted`, so the
caller does not wait for inventory or notification work. Kafka stores the event and
delivers it independently to two consumer groups. Inventory uses retry topics for
transient failures and a DLT for messages that cannot be processed. Consumers must be
idempotent because at-least-once delivery can redeliver a message.

## Core concepts

- A topic is a logical append-only stream; partitions provide parallelism and ordering.
- A consumer group shares partitions, so one record is processed by one member of that group.
- Different groups provide pub/sub fan-out.
- `acks=all` and idempotent producers improve publish durability, but do not make the whole workflow exactly once.
- A retry topic separates delayed retry work from the main topic.
- A DLT is an operational quarantine, not a substitute for alerting and remediation.
- The event key determines partition placement; `orderId` preserves per-order ordering.

## Likely interview questions

### Why return 202 instead of 200?

The order request was accepted for asynchronous processing, but inventory and payment
may not have completed. The API should expose a status resource or later notification.

### Does Kafka provide exactly-once processing?

Kafka transactions can provide exactly-once semantics for Kafka-to-Kafka workflows,
but external database/API side effects still require idempotency, an outbox, or another
coordinating design. “Exactly once” is not a blanket property of the business workflow.

### What happens if a consumer crashes after reserving stock but before acking?

Kafka redelivers the record. The reservation must be protected by an idempotency key
such as `(consumerName, orderId)` and a unique database constraint.

### Why use different consumer groups?

Inventory and notification are independent subscribers. If they shared one group, each
record would be delivered to only one of them and one business capability would be skipped.

### How do you preserve ordering?

Use a stable key, such as `orderId`, and process events for a partition serially. Global
ordering is expensive and usually unnecessary; define the ordering scope explicitly.

### When should a message go to a DLT?

After bounded retries for a non-transient or repeatedly failing message. The DLT handler
must persist context, alert operators, and support safe replay after remediation.

### What is the outbox pattern solving?

It closes the dual-write gap between committing a database change and publishing an
event. The transaction writes the business record and outbox row together; a relay then
publishes the outbox row.

## Tricky questions

1. `acks=all` does not guarantee the consumer processed the event; it only confirms the broker accepted the record according to its replication policy.
2. A retry topic can change timing and partition behavior; do not assume retries preserve the same consumer execution context.
3. Increasing partitions can affect key-to-partition mapping when the partition count changes, so document ordering assumptions.
4. Poison-pill deserialization failures may happen before the listener method; configure an error handler that can publish the raw record to a DLT.
5. “More retries” is not resilience if the downstream failure is permanent; bounded retries plus compensation is safer.
6. An in-memory deduplication set disappears on restart and cannot coordinate multiple instances.

## Production checklist

- Use a schema registry or versioned event envelope.
- Add `eventId`, `occurredAt`, `correlationId`, and `schemaVersion` metadata.
- Use the transactional outbox where a DB write and event publish must be atomic.
- Persist consumer idempotency keys and make replay an explicit operation.
- Set retention, replication, min in-sync replicas, quotas, and ACLs intentionally.
- Monitor consumer lag, retry volume, DLT depth, publish failures, and processing latency.
- Define replay, DLT remediation, and backward/forward compatibility procedures.

## Code areas to discuss

- `OrderProducerController`: non-blocking publish and producer acknowledgment callback.
- `InventoryConsumer`: retry, manual acknowledgment, DLT, and idempotency.
- `NotificationConsumer`: independent consumer group for fan-out.
- `KafkaTopicConfiguration`: broker-managed topic declarations.
