# Interview preparation — Monolith

A monolith is one deployable unit, not necessarily bad architecture. A modular monolith can have strong boundaries while sharing a process and database.

- Why start with a monolith? Fast feedback, simple transactions, and low operational overhead.
- When does it hurt? Independent scaling, release cadence, fault isolation, and team ownership become constrained.
- How do you prepare migration? Establish module boundaries, ownership, contracts, and observability before extraction.

Trick: microservices do not fix unclear domain boundaries; they distribute the complexity.
