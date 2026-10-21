# Interview preparation — Saga choreography

Choreography coordinates a distributed business transaction through events. Participants react to facts and emit the next fact or a compensation event; there is no central coordinator.

- When is choreography a good fit? Few steps, stable event contracts, and loosely coupled participants.
- What is the hardest production problem? Reconstructing saga state and diagnosing event cycles.
- How do you prevent duplicate effects? Idempotency keys, inbox tables, unique constraints, and atomic state transitions.
- What is a compensation? A new business action that semantically offsets a prior action; it is not a database rollback.
- How are events evolved? Versioned schemas, tolerant readers, compatibility checks, and a deprecation window.

Trick: Kafka is commonly at-least-once. “Exactly once” does not make an external payment API exactly once; the business operation still needs an idempotency key.
