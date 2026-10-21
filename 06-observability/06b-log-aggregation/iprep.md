# Interview preparation — Log aggregation

Emit structured logs to stdout, ship them centrally, and index with retention and access controls.

- Why JSON? Stable fields are searchable and machine-readable.
- Which fields matter? timestamp, service, environment, trace ID, span ID, severity, event name, and safe business key.
- How do you control cost? Sampling, retention tiers, cardinality limits, and redaction.

Trick: logs are not metrics; counting log lines for SLOs is brittle compared with explicit counters.
