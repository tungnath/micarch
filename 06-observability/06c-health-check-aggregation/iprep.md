# Interview preparation — Health aggregation

Health aggregation combines liveness/readiness and dependency status into an operator-facing view.

- Liveness vs readiness? Liveness asks whether restart may help; readiness asks whether traffic should be sent.
- Should every dependency make liveness fail? Usually no; otherwise a transient dependency outage causes restart storms.
- How do you avoid alert fatigue? Define severity, debounce transient failures, and alert on user-impacting symptoms.

Trick: a green dashboard is not proof of business correctness; synthetic checks and SLOs complement health endpoints.
