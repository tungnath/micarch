# Interview preparation — OAuth2 and JWT

OAuth2 is an authorization framework; OIDC adds identity. A resource server validates a signed access token and applies authorization policy.

- JWT validation steps? Signature, issuer, audience, expiry/not-before, algorithm, and required scopes/roles.
- Why not use JWT as a session? Revocation is difficult and tokens are bearer credentials; keep lifetimes short and rotate keys.
- Where should authorization live? Gateway for coarse boundary policy and service for business/resource authorization.
- How are keys rotated? JWKS publishes overlapping keys; validators cache and refresh by key ID.

Trick: decoding a JWT is not validating it. Never trust claims before signature and issuer validation.
