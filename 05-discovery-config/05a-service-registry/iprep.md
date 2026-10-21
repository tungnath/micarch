# Interview preparation — Eureka

Eureka provides client-side service discovery with leases, heartbeats, caching, and eventual consistency.

- What happens during a registry outage? Existing clients can often use cached instances; new registrations and topology changes may lag.
- Why is self-preservation important? It prevents mass eviction during network partitions, but stale instances can remain longer.
- How do you load balance? Resolve instances then apply client-side or gateway load balancing.

Trick: discovery is not health checking; registration success does not mean the business endpoint is healthy.
