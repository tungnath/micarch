-- Separate databases for each microservice (demonstrating DB-per-service)
CREATE DATABASE orders;
CREATE DATABASE orders_db;
CREATE DATABASE inventory;
CREATE DATABASE products_db;
CREATE DATABASE payments;
CREATE DATABASE users;
CREATE DATABASE notifications;
CREATE DATABASE saga_orchestrator;
CREATE DATABASE cqrs_write;
CREATE DATABASE cqrs_write_db;
CREATE DATABASE cqrs_read;
CREATE DATABASE cqrs_read_db;
CREATE DATABASE ddd_ecommerce;

-- Grant all privileges to micarch user
GRANT ALL PRIVILEGES ON DATABASE orders TO micarch;
GRANT ALL PRIVILEGES ON DATABASE orders_db TO micarch;
GRANT ALL PRIVILEGES ON DATABASE inventory TO micarch;
GRANT ALL PRIVILEGES ON DATABASE products_db TO micarch;
GRANT ALL PRIVILEGES ON DATABASE payments TO micarch;
GRANT ALL PRIVILEGES ON DATABASE users TO micarch;
GRANT ALL PRIVILEGES ON DATABASE notifications TO micarch;
GRANT ALL PRIVILEGES ON DATABASE saga_orchestrator TO micarch;
GRANT ALL PRIVILEGES ON DATABASE cqrs_write TO micarch;
GRANT ALL PRIVILEGES ON DATABASE cqrs_write_db TO micarch;
GRANT ALL PRIVILEGES ON DATABASE cqrs_read TO micarch;
GRANT ALL PRIVILEGES ON DATABASE cqrs_read_db TO micarch;
GRANT ALL PRIVILEGES ON DATABASE ddd_ecommerce TO micarch;
