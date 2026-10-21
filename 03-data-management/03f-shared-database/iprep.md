# Interview preparation — Shared database anti-pattern

Shared schemas make joins and transactions easy initially, but create release coupling, unclear ownership, noisy-neighbor behavior, and a large blast radius.

- When can it be acceptable? A true modular monolith, a transitional migration stage, or a deliberately shared reporting store with ownership rules.
- How do you migrate away? Establish ownership, introduce APIs/events, copy data into owned stores, dual-read/dual-write carefully, then remove cross-schema access.

Trick: a shared read replica does not solve ownership if services depend on undocumented table shape.
