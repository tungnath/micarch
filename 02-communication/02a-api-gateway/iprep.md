# Interview Preparation — API Gateway

## 60-second explanation

An API gateway is a single client-facing entry point that routes requests to
backend services and applies cross-cutting policies such as authentication,
rate limiting, CORS, observability, and protocol or payload transformation.
This module uses Spring Cloud Gateway with Eureka-based `lb://` routes and a
Redis-backed request rate limiter. Its demo profile uses in-process downstreams
so the route and authentication behavior can be tested without infrastructure.

## Core concepts

- Route predicates decide whether a request belongs to a route.
- Gateway filters apply request/response policy before or after forwarding.
- `lb://service-name` delegates instance resolution to service discovery and load balancing.
- A gateway should own cross-cutting concerns, not order or payment business workflows.
- The gateway is a high-blast-radius component and must be stateless and horizontally scalable.

## Likely interview questions

### API gateway versus reverse proxy?

A reverse proxy primarily forwards traffic. An API gateway usually adds API-aware
policies such as authentication, rate limiting, request transformation, service
discovery, aggregation, and observability. The boundary is conceptual, not a
strict product distinction.

### Why use a gateway if clients can call services directly?

It hides topology, centralizes security and policy, reduces client coupling, and
provides a controlled place for compatibility changes. It also introduces a
shared dependency, so high availability and careful scope are essential.

### What happens when Eureka is unavailable?

New instance resolution may fail or use a cached view depending on the client
and configuration. The gateway needs health checks, timeouts, fallback policy,
and operational visibility; discovery is not a substitute for resilience.

### Why is `lb://` different from `http://localhost:...`?

`http://` names one fixed destination. `lb://` asks the discovery/load-balancer
stack to resolve a logical service name and select an instance, enabling scaling
and distribution without hard-coding host addresses.

### Why should gateway authentication not be the only authorization check?

The gateway is a perimeter control, but services remain independently reachable
in many deployments and must protect their own resources. Services should validate
the token and enforce resource-level authorization as defense in depth.

## Tricky questions

### Is a gateway a single point of failure?

It can be if deployed as one instance. Run multiple stateless instances behind a
load balancer, use externalized configuration, define failure budgets, and test
dependency failures.

### Should the gateway retry failed POST requests?

Not by default. Retrying a non-idempotent write can create duplicates. Retries
require an idempotency key, a clear retryable failure policy, bounded backoff,
and an understanding of whether the downstream received the request.

### Why can a rate limiter make an outage worse?

If Redis is unavailable, the policy itself can fail requests or force an unsafe
fail-open decision. Choose fail-open versus fail-closed deliberately and monitor
the limiter dependency separately.

### Why is CORS not authentication?

CORS is a browser enforcement mechanism controlling which origins may read a
response. It does not authenticate callers and does not protect non-browser
clients.

### Why keep the gateway thin?

Business logic causes deployment coupling, duplicated domain rules, and a gateway
that becomes a monolith. Use dedicated BFFs or backend services for aggregation
and workflows.

## Production design checklist

- Validate JWT issuer, audience, signature, expiry, scopes, and tenant claims.
- Apply route-specific authorization and avoid trusting client-supplied identity headers.
- Configure connect, response, and overall request timeouts.
- Use bounded retries only for safe operations.
- Add correlation IDs, route IDs, destination, status, latency, and rejection metrics.
- Protect management endpoints and avoid exposing sensitive route metadata publicly.
- Use externalized secrets and configuration; never commit production tokens.
- Test discovery loss, Redis loss, downstream timeouts, large payloads, and gateway restarts.

## Design exercise: secure multi-tenant gateway

An interview-quality design should include:

1. JWT validation at the gateway and authorization enforcement in each service.
2. Tenant extraction from a verified claim, never from an untrusted header alone.
3. Per-tenant rate limits with a bounded Redis key strategy.
4. Idempotency keys for write requests and trace propagation end to end.
5. Separate public, internal, and management routes with network policy boundaries.
6. Horizontal gateway scaling with no in-memory session or limiter state.

## Rapid-fire questions

### Gateway or BFF?

Use a gateway for shared cross-cutting policy and topology hiding. Use a BFF for
client-specific aggregation and representation shaping.

### What status should an auth failure return?

`401` when credentials are missing or invalid; `403` when the identity is valid
but lacks permission. Do not leak detailed credential-validation reasons.

### Where should request correlation be created?

At the edge if absent, then propagate it downstream and include it in logs,
traces, and error responses where appropriate.

### How do you prevent route configuration drift?

Version configuration, validate it in CI, test route precedence and filters, and
observe actual destinations in production metrics.

## Code areas to know

- `application.yml` — routes, filters, CORS, discovery, Redis, and demo profile.
- `AuthenticationFilterGatewayFilterFactory.java` — filter proxy and token check.
- `DemoDownstreamController.java` — self-contained demo destinations.
- `ApiGatewayRoutingTest.java` — protected and public route verification.
