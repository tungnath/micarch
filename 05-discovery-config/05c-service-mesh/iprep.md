# Interview preparation — Service mesh

A service mesh moves traffic, identity, retries, telemetry, and policy into data-plane proxies managed by a control plane.

- What does the control plane do? Distributes policy and certificates; it is not on every request path.
- What does the data plane do? Proxies workload traffic and enforces the policy.
- Why mTLS? Workload identity and encryption in transit, not user authorization.
- What are the costs? CPU/memory, latency, operational complexity, and policy debugging.

Trick: a mesh does not fix bad service boundaries or business-level retries.
