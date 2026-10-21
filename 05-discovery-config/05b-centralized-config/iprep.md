# Interview preparation — Centralized configuration

Config Server externalizes environment-specific settings and makes changes auditable through Git.

- How do you protect secrets? Keep them in Vault/KMS/secret stores; do not commit plaintext secrets.
- How do you roll out config safely? Version, validate, canary, refresh selectively, and retain rollback.
- What if Config Server is down at startup? Use local last-known-good configuration or fail fast intentionally based on risk.

Trick: dynamic refresh can change behavior mid-request or violate compatibility; configuration is production code.
