# 04b — Retry and Timeout

## What is it?

Demonstrates **Resilience4j Retry** with exponential backoff + jitter, and **TimeLimiter** to prevent runaway requests from consuming resources indefinitely.

## How Retry Works

```
Attempt 1 → FAIL
Wait: 1s (base)
Attempt 2 → FAIL
Wait: 2s (1s × 2.0 multiplier)
Attempt 3 → FAIL
Wait: 4s (2s × 2.0 multiplier, ±30% jitter)
Attempt 4 → MAX REACHED → throw RetryExhaustedException
```

## How TimeLimiter Works

```
Request sent → 3-second timer starts
  ├─ Response within 3s → ✅ proceed
  └─ No response after 3s → TimeoutException thrown (future cancelled)
```

## Configuration

```yaml
resilience4j:
  retry:
    instances:
      externalService:
        max-attempts: 4
        wait-duration: 1s
        enable-exponential-backoff: true
        exponential-backoff-multiplier: 2
        randomized-wait-factor: 0.3        # ±30% jitter prevents thundering herd
  timelimiter:
    instances:
      externalService:
        timeout-duration: 3s
        cancel-running-future: true
```

## Running the Demo

```bash
cd 04-resilience/04b-retry-timeout
mvn spring-boot:run

# Swagger: http://localhost:8402/swagger-ui.html
# Metrics: http://localhost:8402/actuator/metrics/resilience4j.retry.calls
```

## ✅ Pros
- Handles transient failures automatically
- Jitter prevents thundering herd after network outage
- Timeout prevents thread pool exhaustion from slow calls

## ❌ Cons
- Retry can increase load on already struggling downstream
- Wrong retry exceptions can cause duplicate writes (use idempotency)
- Timeout tuning requires knowing downstream SLAs

## When to Use
- Calling external services that occasionally have transient errors
- Network-dependent operations (DB, external APIs)
- Any async operation that could block indefinitely
