# Interview preparation — Gateway security

The gateway provides a consistent edge policy, but each service remains a trust boundary and should enforce authorization for sensitive operations.

- Authentication vs authorization? Authentication establishes identity; authorization decides whether that identity may perform an action.
- What if a service is reachable directly? Enforce network policy and service-side token validation; gateway-only security is bypassable.
- Why validate audience? A token signed by the correct issuer may still be intended for another resource.

Trick: TokenRelay forwards a token; it does not replace service-level authorization or input validation.
