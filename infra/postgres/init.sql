-- Separate databases for each microservice (demonstrating DB-per-service)
CREATE DATABASE orders;
CREATE DATABASE inventory;
CREATE DATABASE payments;
CREATE DATABASE users;
CREATE DATABASE notifications;
CREATE DATABASE saga_orchestrator;
CREATE DATABASE cqrs_write;
CREATE DATABASE cqrs_read;

-- Grant all privileges to micarch user
GRANT ALL PRIVILEGES ON DATABASE orders TO micarch;
GRANT ALL PRIVILEGES ON DATABASE inventory TO micarch;
GRANT ALL PRIVILEGES ON DATABASE payments TO micarch;
GRANT ALL PRIVILEGES ON DATABASE users TO micarch;
GRANT ALL PRIVILEGES ON DATABASE notifications TO micarch;
GRANT ALL PRIVILEGES ON DATABASE saga_orchestrator TO micarch;
GRANT ALL PRIVILEGES ON DATABASE cqrs_write TO micarch;
GRANT ALL PRIVILEGES ON DATABASE cqrs_read TO micarch;
