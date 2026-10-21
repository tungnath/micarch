# Interview preparation — CQRS

CQRS separates command/write models from query/read models so each can be optimized independently. It does not require event sourcing.

- Why return 202? The read projection may update asynchronously, so the command is accepted before query consistency catches up.
- How do you rebuild a read model? Replay durable events or re-read the source of truth into a new projection.
- What consistency should the API expose? Document eventual consistency and provide version, polling, or read-your-write tokens.
- Where do invariants live? On the command side, inside the aggregate or domain service.

Trick: two databases do not automatically make CQRS; the separation is semantic and ownership-based.
