# Interview preparation — Sidecar

A sidecar runs beside the application and handles language-neutral concerns such as mTLS, retries, telemetry, and traffic policy.

- Why use one? Consistent policy without duplicating client libraries in every language.
- Costs? Extra CPU/memory, a network hop, startup ordering, and harder local debugging.
- Sidecar vs gateway? A sidecar is per workload; a gateway is an ingress/egress boundary.

Trick: moving policy out of code does not remove the need to understand timeout and retry interactions.
