# Interview preparation — Rate limiter

Rate limiting protects capacity and enforces fairness. Token bucket allows bursts up to bucket capacity; a distributed limiter needs shared state or an edge gateway.

- What key should be limited? Principal, API key, tenant, route, and sometimes a global safety limit.
- What response? HTTP 429 with a useful retry hint, without unlimited client retries.
- How do you tune it? From measured capacity, latency SLOs, queue depth, and business tiers.

Trick: per-instance limits multiply when replicas scale; that is not one global quota.
