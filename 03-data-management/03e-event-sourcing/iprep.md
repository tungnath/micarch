# Interview preparation — Event sourcing

Event sourcing stores immutable business events that led to state. Current state is rebuilt by replaying events, often with snapshots and projections.

- Event sourcing vs audit logging? Events are the source of truth and drive behavior; an audit log is usually secondary.
- How do you change event schemas? Version events, use upcasters, and keep old handlers readable.
- How do you handle replay side effects? Separate pure projection updates from external effects and make handlers idempotent.
- Why use snapshots? To reduce replay time without losing event history.

Trick: deleting or editing old events destroys the source of truth. Sensitive data requires encryption, tokenization, or a designed redaction strategy.
