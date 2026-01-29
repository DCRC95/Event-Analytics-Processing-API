Event Analytics & Processing API

Foundation, Security & Event Ingestion

This project is a production-style backend API focused on event ingestion and analytics, not CRUD.


High-level Architecture
Client
  |
  |  HTTP (JSON + JWT)
  v
Controller Layer
  |
  |  validated DTOs
  v
Service Layer
  |
  |  business rules (auth scoping, defaults)
  v
Repository Layer (JPA)
  |
  |  SQL / JSONB
  v
PostgreSQL


Design principles:

Controllers are thin

Services own rules

Database is the source of truth

Analytics are SQL-first (added later)

— Project Scaffolding
What was built

Spring Boot 3 (Java 21) project

Maven-based build

Clean package structure:

config/
controller/
service/
repo/
domain/entity/
dto/
exception/

This enforces separation of concerns from day one. Makes later analytics and testing predictable and matches how real teams structure backend services

Infrastructure & Configuration (Docker + Env)

Docker Compose with PostgreSQL

.env-driven configuration (no hardcoded secrets)

App connects via environment variables

+---------------------+
| Spring Boot App     |
|                     |
| DB_HOST=localhost   |
| DB_PORT=5433        |
+----------+----------+
           |
           v
+---------------------+
| Postgres (Docker)   |
| jsonb support       |
+---------------------+

This creates a reproducible local environment, mirrors real deployment setups, enables Testcontainers later

Database Schema (Flyway)

Flyway migrations from day one

Tables: users   events   users
------
id (UUID PK)
email (unique)
password_hash
created_at

events
------
id (UUID PK)
user_id (FK)
type
entity_type
entity_id
occurred_at
metadata (jsonb)
created_at

Authentication (JWT, Stateless)
What was built

/auth/register

/auth/login

JWT-based authentication

Stateless security (no sessions, no cookies)

Custom JWT filter populating SecurityContext

Auth flow (sequence diagram)
Client → POST /auth/login
Client ← JWT token

Client → POST /events (Authorization: Bearer token)
JwtAuthFilter → validates token
JwtAuthFilter → sets userId as principal
Controller → Service → DB

This allows for stateless auth to scale horizontally with the user identity enforced from the server-side with no cross-user data leakage possible.

Event Ingestion (Append-only)
Endpoint
POST /events
Authorization: Bearer <JWT>

Payload
{
  "type": "TASK_CREATED",
  "entityType": "TASK",
  "entityId": "123",
  "occurredAt": "2026-01-29T12:00:00Z",
  "metadata": { "priority": "HIGH" }
}

Step 5 Architecture (Core Focus)
POST /events
   |
   v
EventController
   |
   v
EventService
   - extract userId from JWT
   - default occurredAt
   - generate UUID
   - enforce append-only
   |
   v
EventRepository
   |
   v
Postgres (jsonb)

Event Ingestion Flowchart
[Request received]
        |
        v
[JWT validated?] -- no --> 401
        |
       yes
        |
        v
[Validate DTO]
        |
        v
[Extract userId from SecurityContext]
        |
        v
[Default occurredAt if missing]
        |
        v
[Create EventEntity]
        |
        v
[Persist event (append-only)]
        |
        v
[200 OK]

Core Class Diagram (Step 5)
+----------------------+
| EventController      |
|----------------------|
| create(req, auth)    |
+----------+-----------+
           |
           v
+----------------------+
| EventService         |
|----------------------|
| create(req, auth)    |
| extractUserId()      |
+----------+-----------+
           |
           v
+----------------------+
| EventRepository      |
|----------------------|
| save(EventEntity)    |
+----------+-----------+
           |
           v
+----------------------+
| EventEntity          |
|----------------------|
| id                  |
| userId              |
| type                |
| entityType           |
| entityId             |
| occurredAt           |
| metadata (jsonb)     |
| createdAt            |
+----------------------+

JSONB Mapping issue Postgres rejected inserts because JSONB ≠ VARCHAR.

Solution

Added Hypersistence Utils

Mapped metadata properly:

@Type(JsonType.class)
@Column(columnDefinition = "jsonb")
private Map<String, Object> metadata;

JSONB is now queryable

Terminal query Examples:

Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"a@test.com","password":"Password123"}'

Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"a@test.com","password":"Password123"}'

Post Event
curl -X POST http://localhost:8080/events \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "type":"TASK_CREATED",
    "entityType":"TASK",
    "entityId":"123",
    "metadata":{"priority":"HIGH"}
  }'

Analytics Engine (SQL-first, non-CRUD) 

Architecture
Client
  |
  v
AnalyticsController
  |
  v
AnalyticsService
  |
  v
EventRepository
  |
  v
PostgreSQL (GROUP BY, COUNT, ORDER BY)

Analytics Processing Flow

[Request]
   |
   v
[JWT validated]
   |
   v
[Extract userId]
   |
   v
[Convert LocalDate → UTC Instants]
   |
   v
[Execute SQL aggregations]
   |
   v
[Assemble response DTO]
   |
   v
[Return JSON]

Date Range Handling (Important Detail)

 Defined clear, explicit semantics:

from = LocalDate at 00:00 UTC (inclusive)
to   = LocalDate + 1 day at 00:00 UTC (exclusive)

This avoids off-by-one errors,makes daily grouping consistentand works correctly across time zones. This is enforced once in the service layer and reused by all queries.