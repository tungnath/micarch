# Interview preparation — Circuit breaker

Know the CLOSED, OPEN, and HALF_OPEN states, failure/slow-call thresholds, permitted probe calls, and fallback semantics.

- Why is a circuit breaker different from a timeout? A timeout bounds one call; a breaker prevents repeated calls after systemic failure.
- Where should it sit with retry? Retry transient failures inside a bounded timeout, with the breaker protecting the overall dependency.
- What should be fallbacked? Safe cached data or an explicit deferred state, never fabricated success.
- What metrics matter? Open transitions, rejected calls, failure rate, slow calls, and fallback rate.

Trick: a fallback can hide an outage unless it is observable and the response communicates degraded behavior.
