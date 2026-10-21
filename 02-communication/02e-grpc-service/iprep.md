# Interview Preparation — 02E gRPC Service Communication

## 60-second explanation

gRPC uses HTTP/2 and Protocol Buffers to provide strongly typed RPC contracts between
services. This module defines `OrderService` in a `.proto` file, generates Java stubs
with Maven, and implements unary order creation/lookup plus server-streaming listing.
Reflection makes the running service discoverable by grpcurl. In production, the design
also needs deadlines, retries only for safe operations, authentication/mTLS, schema
compatibility, status mapping, and telemetry.

## Core concepts

- `.proto` is the source contract; generated classes are build artifacts.
- Field numbers, not field names, identify serialized fields.
- Unary RPC is request/response; streaming RPC sends multiple messages over one call.
- HTTP/2 multiplexes streams over one connection and supports header compression.
- gRPC status codes are more precise than returning arbitrary HTTP-style strings.
- Deadlines propagate a time budget; without them, calls can hang and consume resources.
- Reflection is useful for development and tooling, but should be controlled in production.

## Likely interview questions

### Why choose gRPC over REST?

For internal, strongly typed, latency-sensitive communication where generated clients and
streaming matter. REST remains useful for public APIs, browser clients, and broad tooling
compatibility.

### How is Protobuf backward compatible?

Do not reuse field numbers or change their meaning. Add optional fields with new numbers,
keep old readers working, and reserve removed field numbers/names when appropriate.

### What is the difference between unary and streaming RPCs?

Unary has one request and one response. Server streaming has one request and many ordered
responses; client and bidirectional streaming allow messages in the other directions too.

### Should every gRPC call be retried?

No. Retry only transient failures, use bounded exponential backoff, respect deadlines,
and ensure the operation is idempotent or carries an idempotency key. Retrying a create
operation blindly can create duplicate orders.

### How do you handle errors?

Return canonical gRPC statuses such as `INVALID_ARGUMENT`, `NOT_FOUND`, `DEADLINE_EXCEEDED`,
and `UNAVAILABLE`. For rich errors, use structured error details rather than parsing strings.

### How do browsers call gRPC?

Usually through grpc-web and a compatible proxy, or through a REST/JSON transcoding layer.
Native browser APIs do not generally expose arbitrary HTTP/2 gRPC framing.

## Tricky questions

1. HTTP/2 does not make a slow downstream call safe; without deadlines, threads and connections can still be exhausted.
2. Protobuf’s default values can make “missing” and “explicitly zero” indistinguishable unless presence is modeled deliberately.
3. A field number is an API compatibility commitment; renaming a field is safe, reusing its number is not.
4. Streaming responses still need backpressure and cancellation handling; a server must stop work when the client cancels.
5. gRPC status `UNAVAILABLE` does not mean retry is always safe; inspect operation semantics and deadline budget first.
6. Reflection and plaintext are convenient locally but expose metadata or traffic without encryption in production.

## Production checklist

- Use TLS/mTLS and service identity authorization.
- Set per-RPC deadlines and enforce maximum message sizes.
- Define retry and hedging policies in clients, with jitter and retry budgets.
- Use idempotency keys for retried commands.
- Add request, response, error, latency, and stream-cancellation metrics.
- Propagate trace and correlation metadata through gRPC interceptors.
- Use a schema compatibility check in CI and reserve removed Protobuf fields.
- Decide whether reflection is enabled outside development.
- Test slow clients, cancellation, half-open connections, and rolling upgrades.

## Code areas to discuss

- `src/main/proto/order.proto`: versioned service contract and streaming method.
- `OrderGrpcService`: validation, status codes, unary methods, and stream completion.
- `OrderStore`: replaceable persistence boundary for the demonstration.
- `pom.xml`: platform-specific `protoc` and gRPC Java code generation.
