# Interview preparation — Saga orchestration

An orchestrator owns workflow state and commands participants. It makes forward progress and invokes compensations when a later step fails.

- Choreography vs orchestration? Choreography distributes control; orchestration centralizes visibility and sequencing.
- How do you avoid a god orchestrator? Keep domain decisions in bounded contexts, use explicit commands, and split long workflows.
- What must be persisted? Saga id, current step, attempts, deadlines, responses, and compensation status.
- How do retries work? Retry transient failures with backoff; time out, classify errors, and compensate permanent failures.

Trick: an orchestrator is not a distributed transaction manager. A participant can acknowledge and still fail later, so reconciliation is required.
