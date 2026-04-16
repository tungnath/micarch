-- Schema-per-bounded-context isolation within one database
-- Each bounded context owns its schema — no cross-schema queries allowed

CREATE SCHEMA IF NOT EXISTS order_ctx;
CREATE SCHEMA IF NOT EXISTS inventory_ctx;
CREATE SCHEMA IF NOT EXISTS payment_ctx;

-- Grant usage to ddd user
GRANT ALL ON SCHEMA order_ctx TO ddd;
GRANT ALL ON SCHEMA inventory_ctx TO ddd;
GRANT ALL ON SCHEMA payment_ctx TO ddd;
