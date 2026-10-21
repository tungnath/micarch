# Interview preparation — Blue-green deployment

Blue-green keeps two production-shaped environments and switches traffic between them.

- What makes rollback fast? Route flip, provided the database change is backward compatible.
- How do you handle schema changes? Expand/contract migrations and compatibility across both versions.
- What must be tested before switching? Smoke tests, readiness, critical user journeys, and observability.

Trick: blue-green is not zero risk when stateful infrastructure or irreversible migrations are shared.
