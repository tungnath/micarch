# Interview preparation — Canary release

Canary routing sends a controlled percentage of traffic to a new version and promotes or rolls back based on evidence.

- What metrics decide promotion? Error rate, latency percentiles, saturation, and business success rate compared with stable.
- Why can aggregate metrics lie? A small canary sample has noisy confidence intervals and may not cover all traffic shapes.
- How do you target users? Stable hashing by user/session avoids a user flipping versions.

Trick: canary percentage is traffic control, not a safety guarantee; incompatible data contracts can still affect all users.
