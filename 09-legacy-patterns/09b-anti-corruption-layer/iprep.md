# Interview preparation — Anti-corruption layer

An ACL translates legacy models and protocols so a new domain is not polluted by legacy concepts.

- What does it translate? Names, data types, units, error semantics, identity, and protocol shape.
- ACL vs adapter? An adapter usually makes interfaces compatible; an ACL protects a bounded-context model and may include a facade, translator, and anti-corruption policy.
- Where should retries happen? Around safe external calls, with idempotency and explicit legacy error mapping.

Trick: a pass-through proxy is not an ACL if the legacy vocabulary still leaks into the new domain.
