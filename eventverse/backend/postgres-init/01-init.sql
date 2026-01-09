-- Initialize per-service databases and users for Eventverse

-- Databases (idempotent)
CREATE EXTENSION IF NOT EXISTS dblink;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'eventverse_auth') THEN
        PERFORM dblink_exec('dbname=postgres', 'CREATE DATABASE eventverse_auth');
    END IF;
END $$;
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'eventverse_events') THEN
        PERFORM dblink_exec('dbname=postgres', 'CREATE DATABASE eventverse_events');
    END IF;
END $$;
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'eventverse_ticket') THEN
        PERFORM dblink_exec('dbname=postgres', 'CREATE DATABASE eventverse_ticket');
    END IF;
END $$;
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'eventverse_analytics') THEN
        PERFORM dblink_exec('dbname=postgres', 'CREATE DATABASE eventverse_analytics');
    END IF;
END $$;
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'notification') THEN
        PERFORM dblink_exec('dbname=postgres', 'CREATE DATABASE notification');
    END IF;
END $$;

-- Users (idempotent)
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'event_user') THEN
        CREATE USER event_user WITH PASSWORD 'event_pass';
    END IF;
END $$;
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'event_ticket_user') THEN
        CREATE USER event_ticket_user WITH PASSWORD 'event_ticket_pass';
    END IF;
END $$;
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'analytics_user') THEN
        CREATE USER analytics_user WITH PASSWORD 'analytics_pass';
    END IF;
END $$;
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'ev_user') THEN
        CREATE USER ev_user WITH PASSWORD 'ev_pass';
    END IF;
END $$;

-- Grants
GRANT ALL PRIVILEGES ON DATABASE eventverse_auth TO postgres;
GRANT ALL PRIVILEGES ON DATABASE eventverse_events TO event_user;
GRANT ALL PRIVILEGES ON DATABASE eventverse_ticket TO event_ticket_user;
GRANT ALL PRIVILEGES ON DATABASE eventverse_analytics TO analytics_user;
GRANT ALL PRIVILEGES ON DATABASE notification TO ev_user;

-- ---------------------------------------------------------------------------
-- Per-database schema ownership/privileges so app users can create objects
-- ---------------------------------------------------------------------------

-- Event service DB: ensure event_user can use/create in public schema
\connect eventverse_events;
ALTER SCHEMA public OWNER TO event_user;
GRANT ALL ON SCHEMA public TO event_user;
GRANT ALL ON ALL TABLES IN SCHEMA public TO event_user;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO event_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO event_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO event_user;

-- Ticket service DB: ensure event_ticket_user can create tables/indexes
\connect eventverse_ticket;
ALTER SCHEMA public OWNER TO event_ticket_user;
GRANT ALL ON SCHEMA public TO event_ticket_user;
GRANT ALL ON ALL TABLES IN SCHEMA public TO event_ticket_user;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO event_ticket_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO event_ticket_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO event_ticket_user;

-- Analytics service DB: ensure analytics_user can manage schema and tables
\connect eventverse_analytics;
ALTER SCHEMA public OWNER TO analytics_user;
GRANT ALL ON SCHEMA public TO analytics_user;
GRANT ALL ON ALL TABLES IN SCHEMA public TO analytics_user;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO analytics_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO analytics_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO analytics_user;

-- Notification service DB: allow Flyway + app user to manage schema
\connect notification;
ALTER SCHEMA public OWNER TO ev_user;
GRANT ALL ON SCHEMA public TO ev_user;
GRANT ALL ON ALL TABLES IN SCHEMA public TO ev_user;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO ev_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO ev_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO ev_user;

-- ---------------------------------------------------------------------------
-- Schema objects for specific databases
-- NOTE: This section assumes the script is executed by psql (default in the
-- official Postgres image) so that \connect is available.
-- ---------------------------------------------------------------------------

-- Create core tables for event-service in eventverse_events
\connect eventverse_events;

CREATE TABLE IF NOT EXISTS events (
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(140)  NOT NULL,
    description      TEXT,
    city             VARCHAR(64)   NOT NULL,
    event_time       TIMESTAMPTZ   NOT NULL,
    capacity         INTEGER       NOT NULL,
    organizer_id     BIGINT        NOT NULL,
    venue            VARCHAR(255),
    category         VARCHAR(64),
    image_url        VARCHAR(512),
    is_public        BOOLEAN       NOT NULL DEFAULT TRUE,
    price            NUMERIC(12,2) NOT NULL DEFAULT 0,
    version          BIGINT,
    popularity_score BIGINT        NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_events_city_time
    ON events (city, event_time);

CREATE INDEX IF NOT EXISTS idx_events_organizer_id
    ON events (organizer_id);

-- Make sure event_user owns the table (and its sequence)
ALTER TABLE events OWNER TO event_user;
