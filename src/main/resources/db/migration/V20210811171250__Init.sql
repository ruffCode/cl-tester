CREATE EXTENSION if not exists pgcrypto;

CREATE OR REPLACE FUNCTION trigger_set_timestamp()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

create table if not exists users_table
(
    id    uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    name  text        not null,
    email text unique not null,
    created_at timestamptz          default now(),
    updated_at timestamptz          default now()
);

DROP TRIGGER IF EXISTS users_updated_at_timestamp on users_table;

CREATE TRIGGER users_updated_at_timestamp
    BEFORE UPDATE
    ON users_table
    FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();