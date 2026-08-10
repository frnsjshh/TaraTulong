# TaraTulong — Volunteer Management REST API

A backend API for connecting volunteers with grassroots organizations in the Philippines — built with Spring Boot 4, Spring Security, and PostgreSQL.

---

## The Problem

Many grassroots organizations — student councils, school clubs, local NGOs, community groups — coordinate volunteer opportunities through Facebook groups, Google Forms, spreadsheets, and messaging apps.

This creates recurring problems:

- No centralized view of who has applied, been approved, or attended
- No way to track volunteer reliability across events
- No capacity enforcement — events can be silently overbooked
- No distinction between a responsible early cancellation and a no-show
- Race conditions when two people try to claim the last available slot

**TaraTulong** models that coordination workflow as a proper backend system with role-based access, state-managed registrations, attendance tracking, and a reputation scoring algorithm.

The idea came from firsthand experience organizing reading tutorials for students at Macanhan Elementary School, where I saw how volunteer coordination breaks down without structure.

---

## Key Features

### Registration State Machine

Volunteer applications move through explicit states rather than arbitrary string values:

```text
PENDING
   ├──→ APPROVED
   │       ├──→ PRESENT
   │       ├──→ NO_SHOW
   │       ├──→ CANCELLED_EARLY
   │       └──→ CANCELLED_LATE
   └──→ REJECTED
```

Each state transition triggers downstream effects — slot count adjustments, trust score updates, and attendance statistics recalculation.

### Trust Score Algorithm

Volunteer reliability is calculated using a **point delta system** rather than a simple attendance percentage.

| Outcome | Points |
|---|---:|
| `PRESENT` | +10 |
| `CANCELLED_EARLY` | -2 |
| `CANCELLED_LATE` | -10 |
| `NO_SHOW` | -25 |

The key design decision: when an organization **corrects** an attendance record (e.g., marking a `NO_SHOW` as `PRESENT`), the system calculates the delta between the old and new states:

```text
New points = newStatus.pointValue - currentStatus.pointValue
           = PRESENT(+10) - NO_SHOW(-25)
           = +35 correction
```

This makes corrections safe and idempotent — the score always reflects the current state regardless of how many times it was changed. The accumulated score maps to a human-readable **Trust Tier** (Platinum → Gold → Silver → Bronze → High Risk) via a custom MapStruct mapping.

### Early vs Late Cancellation

Cancellations are classified based on a **48-hour threshold** before the event start:

```java
AttendanceStatus cancelStatus = now.plusHours(48).isBefore(eventStartDateTime)
        ? AttendanceStatus.CANCELLED_EARLY   // -2 points
        : AttendanceStatus.CANCELLED_LATE;   // -10 points
```

This lets the system distinguish _"I cancelled with enough notice for the org to find a replacement"_ from _"I cancelled at the last minute."_

### Capacity Management with Optimistic Locking

Events have a `slotsAvailable` counter that decrements on approval and increments if an approved registration is rejected or cancelled.

The `Event` entity uses a JPA `@Version` field to prevent concurrent approvals from over-allocating the last slot. If two approval requests race, the second receives:

```http
409 Conflict — "The resource was modified. Refresh and try again"
```

This is caught globally via `@ControllerAdvice` handling `ObjectOptimisticLockingFailureException`.

### IDOR Prevention

Authorization goes beyond role checks. The service layer verifies **resource ownership** before allowing operations:

```text
Org user hits PATCH /registrations/42/status/approved
  → Spring Security: Is this user an ORG? ✓
  → Service layer: Does this org own the event tied to registration 42? ✓ or 403
```

This prevents insecure direct object reference (IDOR) attacks where an organization could modify another organization's events by changing the ID in the request.

### Soft Deletes

Users and events are soft-deleted using Hibernate 6's `@SQLRestriction("deleted=false")`. Deleted volunteers have their email mangled (`DELETED_<email>_<timestamp>`) to free the unique constraint for re-registration.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4 |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| Persistence | Spring Data JPA / Hibernate 6 |
| Database | PostgreSQL |
| Object Mapping | MapStruct 1.5.5 |
| Validation | Jakarta Bean Validation |
| API Docs | OpenAPI 3 / Swagger UI (springdoc) |
| Build | Maven |

---

## Architecture

```text
HTTP Request
     │
     ▼
┌─────────────────────┐
│   Controller        │  ← DTOs (Java Records), @Valid, @AuthenticationPrincipal
│   api/v1/*          │
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│   Service           │  ← Business rules, ownership verification, state transitions
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│   Repository        │  ← Spring Data JPA, custom JPQL queries
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│   PostgreSQL        │
└─────────────────────┘
```

**Package structure is organized by feature** (event, registration, user, security) rather than by technical layer. Each feature contains its entity, service, repository, and a `v1/` subpackage with the controller and DTOs.

**DTOs use Java Records** at the API boundary — request DTOs carry validation annotations, response DTOs expose only the fields the client needs. MapStruct handles the mapping between entities and DTOs, including custom logic like the trust tier calculation.

**JPA inheritance** — `AppUser` is the base entity (with `@Inheritance(strategy = InheritanceType.JOINED)`), extended by `Volunteer`, `Org`, and `Admin`. This was chosen over `SINGLE_TABLE` to avoid nullable columns and keep each role's data normalized, at the cost of requiring joins for polymorphic queries.

---

## Data Model

```text
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│   AppUser    │      │    Event     │      │ Registration │
│──────────────│      │──────────────│      │──────────────│
│ id (PK)      │      │ id (PK)      │      │ id (PK)      │
│ uuid (UK)    │      │ organizer_id │◄─────│ event_id     │ (indexed)
│ email (UK)   │      │ title        │      │ volunteer_id │ (indexed)
│ password     │      │ description  │      │ reg_status   │
│ role         │      │ startDateTime│      │ attend_status│
│ joinDate     │      │ endDateTime  │      │ rating (1-5) │
│ deleted      │      │ cutOffTime   │      │ feedback     │
└──────┬───────┘      │ location     │      │ appliedAt    │
       │              │ slotsAvail.  │      └──────────────┘
       │ JOINED       │ version (OL) │
  ┌────┼────┐         │ deleted      │
  ▼    ▼    ▼         └──────────────┘
┌────┐┌────┐┌─────┐
│Vol.││Org ││Admin│
│    ││    ││     │
│name││name││name │
│trust││desc││     │
│rate││loc  ││     │
│etc.││etc. ││     │
└────┘└────┘└─────┘
```

Key relationships:
- `Org` → `Event`: One-to-many (an org creates events)
- `Volunteer` → `Registration` → `Event`: Many-to-many through `Registration`
- `Admin` → `Org`: One-to-many (admin approves organizations)
- All `FetchType.LAZY` by default — eager loading only via explicit `JOIN FETCH` queries

---

## Security

| Concern | Implementation |
|---|---|
| Authentication | Stateless JWT (HS256, 24h expiry) via custom `OncePerRequestFilter` |
| Password storage | BCrypt via Spring Security's `PasswordEncoder` |
| Role-based access | `ADMIN`, `ORG`, `VOLUNTEER` enforced in `SecurityFilterChain` |
| Resource ownership | Service-layer checks before mutation operations |
| CSRF | Disabled (stateless JWT, no cookies) |
| CORS | Configured for `localhost:3000` (development) |
| Session | `STATELESS` — no server-side session |

---

## Error Handling

All exceptions are caught by a `@ControllerAdvice` handler and returned in a consistent structure:

```json
{
  "timeStamp": "2026-08-08T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Registration already approved.",
  "path": "/api/v1/registrations/42/status/approved"
}
```

Handled exception types include:

| Exception | HTTP Status | When |
|---|---|---|
| `BaseNotFoundException` subclasses | 404 | Entity not found |
| `UserAlreadyExistsException` | 409 | Duplicate email on registration |
| `VolunteerAlreadyRegisteredException` | 409 | Duplicate event registration |
| `RegistrationConflictException` | 409 | Invalid state transition |
| `EventRegistrationClosed` | 400 | Past cutoff or no slots |
| `ObjectOptimisticLockingFailureException` | 409 | Concurrent modification detected |
| `MethodArgumentNotValidException` | 400 | Bean validation failures (field-level messages) |
| `UnauthorizedAccessException` | 403 | Resource ownership check failed |

Validation errors extract field-level messages from `BindingResult` rather than returning raw framework exceptions.

---

## API Endpoints

### Authentication
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/auth/login` | Public | Returns JWT token |

### Events
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/events` | Public | List all events (paginated, sorted by date) |
| GET | `/api/v1/events/{id}` | Public | Get event details |
| GET | `/api/v1/events/org/{orgId}` | Public | Events by organization (paginated) |
| POST | `/api/v1/events` | ORG | Create event |
| PUT | `/api/v1/events/{id}` | ORG (owner) | Update event |
| DELETE | `/api/v1/events/{id}` | ORG (owner) | Soft delete event |

### Registrations
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/registrations` | VOLUNTEER | Apply to an event |
| GET | `/api/v1/registrations/{id}` | Authenticated | Get registration |
| GET | `/api/v1/registrations/events/{eventId}` | ORG (owner) | List registrations for an event |
| PATCH | `/api/v1/registrations/{id}/status/approved` | ORG (owner) | Approve registration (decrements slots) |
| PATCH | `/api/v1/registrations/{id}/status/rejected` | ORG (owner) | Reject registration |
| PATCH | `/api/v1/registrations/{id}/present` | ORG (owner) | Mark as present |
| PATCH | `/api/v1/registrations/{id}/absent` | ORG (owner) | Mark as no-show |
| PATCH | `/api/v1/registrations/{id}/cancel` | VOLUNTEER (owner) | Cancel registration |
| PATCH | `/api/v1/registrations/{id}/feedback` | ORG (owner) | Rate volunteer (1-5) + feedback |
| DELETE | `/api/v1/registrations/{id}` | VOLUNTEER (owner) | Delete registration |

### Users
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/volunteers` | Public | Register volunteer |
| GET | `/api/v1/volunteers/me` | VOLUNTEER | Get own profile |
| PUT | `/api/v1/volunteers/me` | VOLUNTEER | Update profile |
| DELETE | `/api/v1/volunteers/me` | VOLUNTEER | Soft delete account |
| POST | `/api/v1/orgs` | Public | Register organization |
| GET | `/api/v1/orgs/{id}` | Public | View organization |
| PATCH | `/user/password` | Authenticated | Change password |
| PATCH | `/user/email` | Authenticated | Change email |

### Admin
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| PATCH | `/api/v1/admin/me/approve/{orgId}` | ADMIN | Approve organization |
| PATCH | `/api/v1/admin/me/reject/{orgId}` | ADMIN | Reject organization |

Full interactive docs available via **Swagger UI** at `/swagger-ui.html` after starting the application.

---

## Database Performance

- **Indexes** on `registration.event_id` and `registration.volunteer_id` (defined via `@Table(indexes = ...)` on the `Registration` entity)
- **`FetchType.LAZY`** on all `@ManyToOne` and `@OneToMany` relationships
- **`JOIN FETCH`** with a dedicated `countQuery` for the paginated registrations-by-event query — avoids both N+1 queries and the Hibernate pagination-with-fetch-join issue
- **Pagination** on all list endpoints using Spring Data's `Pageable` with configurable size and sort

---

## Getting Started

### Prerequisites

- Java 21
- Maven
- PostgreSQL

### Environment Variables

| Variable | Description |
|---|---|
| `JWT_SECRET` | Base64-encoded secret key for JWT signing (HS256) |

### Setup

```bash
# Clone
git clone https://github.com/yourusername/TaraTulong.git
cd TaraTulong

# Create the database
psql -U postgres -c "CREATE DATABASE taratulong_db;"

# Configure database connection in application.properties or via env vars:
# spring.datasource.url=jdbc:postgresql://localhost:5432/taratulong_db
# spring.datasource.username=<your_username>
# spring.datasource.password=<your_password>

# Set JWT secret
export JWT_SECRET=<your-base64-encoded-secret>

# Build and run
mvn clean install
mvn spring-boot:run
```

The API starts at `http://localhost:8080`. Swagger UI is at `http://localhost:8080/swagger-ui.html`.

---

## Project Status

### Implemented
- REST API with versioned endpoints (`/api/v1/`)
- PostgreSQL persistence with Spring Data JPA / Hibernate 6
- JWT authentication with stateless sessions
- Role-based access control (Admin, Organization, Volunteer)
- Event CRUD with capacity management
- Registration workflow with state transitions
- Attendance tracking with early/late cancellation differentiation
- Trust Score algorithm with point deltas and tier mapping
- Optimistic locking for concurrent slot management
- DTO-based API boundary with MapStruct
- Global exception handling with consistent error responses
- Bean validation on all request DTOs
- Database indexing on foreign keys
- OpenAPI 3 / Swagger UI documentation
- Soft deletes with `@SQLRestriction`
- Organization approval workflow (Admin → Org)
- CORS configuration

### Not Yet Implemented
- Automated tests (test scaffolding exists but no test methods yet)
- Docker / containerized deployment
- CI/CD pipeline
- Email notifications
- Event search and filtering
- Waitlist management

---

## Challenges and Lessons Learned

**Concurrent slot management** was the most technically interesting problem. Initially I didn't account for two simultaneous approvals claiming the last slot. Adding `@Version` to the `Event` entity solved this, but I also had to handle the resulting exception at the API layer — returning a meaningful 409 instead of a 500 with a Hibernate stack trace.

**The point delta algorithm** went through several iterations. My first approach stored trust scores as absolute values recalculated from scratch on every update. This was expensive and fragile. The current delta-based approach (`newStatus.points - currentStatus.points`) handles corrections naturally — if an org accidentally marks a volunteer as `NO_SHOW` and then corrects it to `PRESENT`, the math self-corrects without needing to replay the entire history.

**Soft deletes and unique constraints** were a practical annoyance. When a user is soft-deleted, their email still occupies the unique constraint. Mangling the email with a `DELETED_` prefix and timestamp was the pragmatic solution, though a proper approach might use a partial unique index in PostgreSQL.

**JPA inheritance trade-offs** became apparent as the project grew. `JOINED` inheritance keeps data clean but means every `AppUser` query requires joins across the `volunteer`, `org`, and `admin` tables. For this project's scale, the trade-off is reasonable; at production scale, I would consider whether `SINGLE_TABLE` with discriminator columns would perform better for auth-heavy query patterns.

**The N+1 problem** appeared when listing registrations for an event — each registration lazily loaded its volunteer and event, generating dozens of queries for a single page. The `JOIN FETCH` with separate `countQuery` in the repository solved this, but I learned that fixing N+1 isn't just about adding `EAGER` everywhere — it's about making fetch behavior intentional per query.

---

## What This Project Demonstrates

- Designing REST APIs around **domain workflows** rather than database tables
- Modeling **state transitions** with enums and enforcing valid transitions in the service layer
- Implementing **JWT authentication and RBAC** with Spring Security
- Preventing **IDOR vulnerabilities** through service-layer ownership verification
- Solving the **N+1 query problem** with `JOIN FETCH` and `countQuery`
- Using **optimistic locking** to handle concurrent updates
- Building a **consistent error handling** strategy with `@ControllerAdvice`
- Separating **persistence models from API contracts** using DTOs and MapStruct
- Making **database performance decisions** (indexes, fetch strategies, pagination) based on query patterns
- Understanding that backend engineering is about ensuring the system remains correct when users, requests, and data interact in ways the happy path doesn't anticipate
