# Interview preparation — Shared kernel

A shared kernel is a deliberately small set of concepts jointly owned by bounded contexts. It creates coupling and therefore needs governance.

- What belongs there? Stable, genuinely shared value objects or contracts—not service-specific entities.
- How do you version it? Semantic versioning, compatibility tests, ownership, and a deprecation policy.
- When should it be replaced? When independent teams cannot release or evolve without coordination.

Trick: “DRY” is not enough justification; duplicated code can be cheaper than distributed coupling.
