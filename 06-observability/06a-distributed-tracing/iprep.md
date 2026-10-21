# Interview preparation — Distributed tracing

Tracing follows one logical request through spans and services using propagated context.

- Trace vs span? A trace is the end-to-end tree; a span is one timed operation.
- Why sample? Full capture is expensive; use head/tail sampling and always retain errors.
- What must not be logged? Tokens, personal data, and high-cardinality unbounded values.
- How do async messages propagate context? Copy trace context into message headers and create a consumer span.

Trick: correlation IDs are useful but are not a substitute for a parent/child span model.
