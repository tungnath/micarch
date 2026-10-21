# Interview preparation — Retry and timeout

Retry only transient failures, bound total time, use exponential backoff with jitter, and make writes idempotent.

- Why can retries worsen an outage? They multiply load and consume caller threads.
- What is the retry budget? A bounded number of attempts/time per request, coordinated across layers.
- Which operations are safe? Reads and idempotent commands; writes need keys or a transaction protocol.

Trick: every hop needs a coherent deadline; increasing a client timeout only moves the queue.
