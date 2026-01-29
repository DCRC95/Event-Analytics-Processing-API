-- Enables gen_random_uuid() for UUID defaults
create extension if not exists pgcrypto;

create table if not exists users (
  id uuid primary key default gen_random_uuid(),
  email varchar(320) not null unique,
  password_hash varchar(255) not null,
  created_at timestamptz not null default now()
);

create table if not exists events (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references users(id),
  type varchar(64) not null,
  entity_type varchar(64) not null,
  entity_id varchar(128) not null,
  occurred_at timestamptz not null,
  metadata jsonb,
  created_at timestamptz not null default now()
);

-- Analytics-friendly indexes (matches WHERE user_id + occurred_at range + optional filters)
create index if not exists idx_events_user_occurred_at
  on events(user_id, occurred_at);

create index if not exists idx_events_user_type_occurred_at
  on events(user_id, type, occurred_at);

create index if not exists idx_events_user_entity_occurred_at
  on events(user_id, entity_type, entity_id, occurred_at);
