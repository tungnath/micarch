# Interview preparation — Ambassador

An ambassador is a local proxy that adapts an application to an external or legacy system and centralizes connectivity policy.

- Ambassador vs ACL? Ambassador focuses on connectivity/proxy concerns; an ACL protects a domain model through translation.
- What belongs there? Protocol adaptation, retries, circuit breaking, authentication to the external system, and telemetry.
- What should not be hidden? Business semantics and failure states must remain explicit to the application.

Trick: a proxy does not remove external idempotency, contract, or timeout concerns.
