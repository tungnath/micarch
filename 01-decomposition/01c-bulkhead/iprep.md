# Interview Preparation — Bulkhead Pattern

## 60-second explanation

The Bulkhead pattern partitions concurrency so one overloaded operation cannot consume all application resources. This module demonstrates semaphore bulkheads for order and payment work, plus a dedicated thread-pool bulkhead for inventory work. Calls that exceed capacity fail through explicit fallbacks instead of waiting indefinitely or exhausting shared workers.

## Architecture points

- Order and payment operations use semaphore limits with bounded wait times.
- Inventory work uses a dedicated executor with core threads, maximum threads, and a queue.
- Rejections are intentional bounded failures: HTTP 429 for semaphore overflow and HTTP 503 for inventory capacity exhaustion.
- Resilience4j annotations are applied through Spring AOP, so the service must be proxied for interception to occur.
- The response exposes the worker thread and status to make isolation visible during the demo.

## Likely interview questions

### What problem does a bulkhead solve?

It limits resource contention and failure propagation. A slow or high-volume operation can be rejected or isolated without consuming every thread, connection, or permit needed by other operations.

### Semaphore versus thread-pool bulkhead?

A semaphore limits concurrent access while work continues on the caller's thread. A thread-pool bulkhead also isolates execution onto a dedicated executor and controls queueing. Semaphore bulkheads are lightweight; thread-pool bulkheads provide stronger execution isolation but consume more resources.

### Why are fallback methods needed?

Capacity rejection is an expected operational outcome, not necessarily an application bug. A fallback converts it into a bounded response, metric, or alternative action rather than an uncaught exception or uncontrolled retry storm.

### Why is `spring-boot-starter-aop` required?

Resilience4j's annotation-based behavior is applied by an aspect. Without Spring AOP, the annotations remain metadata and calls can bypass the configured bulkhead entirely.

### Why use a dedicated thread pool for inventory?

Inventory work may be slow or blocking. A dedicated pool prevents it from consuming the common request worker pool and allows capacity to be tuned independently.

## Tricky questions

### Does a bulkhead make a slow dependency faster?

No. It limits damage and makes overload behavior predictable. Latency still needs timeouts, and the dependency may need caching, batching, circuit breaking, or capacity improvements.

### What happens if the bulkhead queue is unbounded?

Requests can accumulate until memory, latency, or upstream timeouts become the failure mechanism. Bounded queues plus an explicit rejection policy provide backpressure.

### Why not simply increase the thread pool?

More threads can increase contention, context switching, downstream load, and memory use. Limits should reflect dependency capacity and business priority, not just local CPU count.

### Can retries make bulkhead protection worse?

Yes. Retries multiply traffic precisely when capacity is exhausted. Use bounded retries, exponential backoff, jitter, and retry only failures that are safe and likely to recover.

### What can cause an annotated bulkhead to be bypassed?

Self-invocation inside the same bean, calls made before proxy creation, direct calls to the underlying target, missing AOP configuration, or an incompatible fallback signature. Integration tests should verify actual rejection and executor behavior.

### How should HTTP status codes be selected?

Use a status that communicates retryability and ownership. `429 Too Many Requests` is suitable for caller-specific concurrency pressure; `503 Service Unavailable` can represent temporarily unavailable capacity. The API contract should document retry guidance.

## Production improvements

- Add timeout, circuit breaker, retry, and rate-limit policies where appropriate.
- Export accepted, rejected, queued, active, latency, and fallback metrics.
- Propagate correlation IDs and include capacity decisions in structured logs.
- Load-test limits against downstream capacity and adjust per environment.
- Use separate pools for blocking work and never block event-loop threads.
- Define graceful shutdown behavior for queued and running tasks.

## Capacity and tuning model

For a semaphore bulkhead, the rough upper bound for active work is the permit count. For a thread-pool bulkhead, capacity is approximately:

`active workers + queued work = max thread pool size + queue capacity`

That is a capacity boundary, not a latency guarantee. Tune it from downstream limits, request duration, memory, CPU, and acceptable queue wait. A large queue may improve burst absorption while making timeout and user experience worse.

## Implementation walkthrough

1. The controller validates `delayMs` and calls the service operation.
2. Spring AOP intercepts the Resilience4j annotation on the service bean.
3. Order and payment calls acquire semaphore permits; excess calls invoke their fallback and return `429`.
4. Inventory calls are submitted to the dedicated thread-pool bulkhead executor.
5. A full inventory pool or queue invokes the fallback and returns `503`.
6. The response exposes status, elapsed time, and worker thread so the isolation boundary can be observed.

## Policy comparison

| Policy | Protects | Typical failure signal | Main risk |
|---|---|---|---|
| Semaphore bulkhead | Concurrent access to a method/dependency | Permit rejection | Work still runs on caller thread |
| Thread-pool bulkhead | Execution threads and queue | Queue/pool rejection | More memory and scheduling overhead |
| Timeout | Maximum wait/work duration | Timeout exception | Does not stop underlying work in every client |
| Circuit breaker | Repeated dependency failure | Open circuit | Can reject healthy calls if thresholds are poor |
| Rate limiter | Request arrival rate | Rate-limit rejection | Does not directly bound execution duration |

## Failure analysis questions

### A downstream call takes 10 seconds and the bulkhead allows five calls. What happens?

Up to five calls occupy the partition for roughly 10 seconds. Further calls either wait within the configured bound or are rejected. A timeout is still needed because the bulkhead alone does not cap call duration.

### Why can a queue hide an outage?

Requests may be accepted into the queue while user-visible latency grows. By the time they fail upstream, the dependency outage is harder to diagnose. Queue depth and age must be measured and bounded.

### How do you choose 429 versus 503?

Use `429` when the caller exceeded an applicable concurrency or request limit and can retry according to contract. Use `503` when the service is temporarily unable to serve because its internal or downstream capacity is unavailable. The distinction should be consistent and documented.

### What happens during graceful shutdown?

Stop accepting new traffic, decide whether queued work is drained or rejected, allow bounded time for active work, and publish accurate readiness state. Never allow an unbounded queue to delay shutdown.

## Design exercise: protecting a payment dependency

An interview-quality design should include:

1. A per-provider or per-operation bulkhead so one provider cannot starve another.
2. A timeout shorter than the caller's deadline.
3. A circuit breaker for repeated provider failures.
4. Retries only for safe, transient failures, with exponential backoff and jitter.
5. Idempotency keys for payment requests so retries cannot double-charge.
6. Metrics for active calls, rejections, queue age, timeout rate, provider latency, and fallback rate.

## Rapid-fire questions

### Does a bulkhead replace a circuit breaker?

No. A bulkhead limits concurrency; a circuit breaker reacts to failure rates or slow calls. They solve different failure modes and are often composed.

### Does a rejected call mean the dependency is down?

No. It may simply mean the local policy is protecting capacity. Distinguish local saturation, downstream failure, caller rate excess, and validation errors in metrics.

### Why can self-invocation bypass the annotation?

Spring AOP applies advice through a proxy. A method calling another annotated method on `this` bypasses that proxy, so the annotation is not intercepted.

### Should every endpoint have its own bulkhead?

Not automatically. Partition by resource, dependency, business priority, and failure domain. Too many tiny pools create fragmentation and operational complexity.

### What load-test result matters most?

Not just throughput. Measure rejection behavior, tail latency, queue age, downstream saturation, memory, recovery after overload, and whether critical traffic remains available.

## Code areas to know

- `BulkheadDemoService.java` — semaphore and thread-pool annotations, work, and fallbacks.
- `BulkheadController.java` — HTTP contract and delay validation.
- `BulkheadExceptionHandler.java` — invalid-request response handling.
- `application.yml` — Resilience4j limits and actuator exposure.
- `BulkheadDemoServiceTest.java` — rejection and executor-isolation verification.
