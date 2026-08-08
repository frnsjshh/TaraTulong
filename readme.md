# TaraTulong — Volunteer Management API

> A backend API for connecting volunteers with grassroots organizations in the Philippines, with built-in event management, application workflows, attendance tracking, and reputation scoring.

**TaraTulong** is a REST API designed to make volunteer coordination more accessible to small organizations such as student councils, school clubs, local NGOs, and community groups.

The project started from a simple problem: many grassroots organizations do not have dedicated volunteer-management systems. Instead, they often coordinate opportunities through Facebook groups, Google Forms, spreadsheets, and messaging applications.

TaraTulong consolidates that workflow into a single backend system while exploring several real-world backend engineering concerns:

* Authentication and authorization
* Role-based access control
* Event capacity management
* Application state transitions
* Attendance tracking
* Reputation and trust scoring
* Database performance
* Concurrent updates
* DTO-based API design
* API versioning

---

## Why I Built This

The idea for TaraTulong came from experiencing community volunteer activities firsthand, including organizing reading tutorials for students at Macanhan Elementary School.

I noticed that volunteer coordination can become surprisingly difficult once an organization has to answer questions such as:

* Who has applied?
* Who should be accepted?
* Is the event already full?
* Has this volunteer attended previous events?
* What happens when someone cancels?
* How do we distinguish an early cancellation from a no-show?
* How do we prevent two people from taking the last available slot simultaneously?

For large organizations, these problems can be handled by dedicated platforms and administrative systems.

For smaller organizations, the workflow is often distributed across forms, spreadsheets, social-media posts, and chat messages.

TaraTulong is an attempt to model that workflow as a proper backend system.

---

## Core Use Cases

### Organizations

Organizations can:

* Create and manage volunteer events
* Define event capacities
* Review volunteer applications
* Approve or reject applicants
* Track attendance
* Manage event participants
* View volunteer reputation information

### Volunteers

Volunteers can:

* Discover volunteer opportunities
* Apply to events
* Track their registration status
* Build a verifiable participation history
* Earn reputation through reliable participation

### Administrators

Administrators provide platform-level oversight, including management of organizations and users.

---

## Key Features

### Event Management

Organizations can create events with defined:

* Capacity
* Schedule
* Location
* Description
* Registration status
* Participant list

The API also handles the event lifecycle and prevents invalid state transitions.

### Application Pipeline

Instead of relying on external forms, volunteer applications are represented as persistent domain objects.

A registration moves through explicit states such as:

```text
PENDING
   │
   ├──> APPROVED
   │       │
   │       ├──> PRESENT
   │       └──> NO_SHOW
   │
   └──> REJECTED
```

Cancellation is also differentiated based on timing so that the system can distinguish between responsible cancellations and late cancellations.

### Trust Score

Volunteer reliability is represented by a point-based reputation system.

Rather than calculating reputation solely from attendance percentage, different attendance outcomes contribute different point deltas.

For example:

| Attendance Status | Points |
| ----------------- | -----: |
| `PRESENT`         |    +10 |
| `CANCELLED_EARLY` |     -2 |
| `CANCELLED_LATE`  |    -10 |
| `NO_SHOW`         |    -25 |

This allows the system to distinguish between:

> "I cancelled early enough for the organization to find someone else."

and:

> "I accepted a slot but did not show up."

The accumulated score is then mapped to a human-readable **Trust Tier**.

The important design decision here is that reputation is derived from **domain events and state transitions**, rather than being treated as an arbitrary number stored independently from the underlying participation history.

---

# Technical Stack

| Area                  | Technology                  |
| --------------------- | --------------------------- |
| Language              | Java 21                     |
| Framework             | Spring Boot 3               |
| Database              | PostgreSQL                  |
| Persistence           | Spring Data JPA / Hibernate |
| Security              | Spring Security + JWT       |
| API Documentation     | OpenAPI 3 / Swagger UI      |
| Object Mapping        | MapStruct                   |
| Boilerplate Reduction | Lombok                      |
| Build Tool            | Maven                       |

---

# Backend Architecture

The application follows a layered architecture designed to keep HTTP concerns, business logic, and persistence concerns separated.

```text
HTTP Request
     │
     ▼
┌─────────────────┐
│   Controller    │
│ REST / DTOs     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│    Service      │
│ Business Rules  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   Repository    │
│ Spring Data JPA │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   PostgreSQL    │
└─────────────────┘
```

DTOs and Java Records are used at the API boundary rather than exposing JPA entities directly.

This keeps persistence models separate from the public API contract and reduces risks associated with accidental field exposure or unintended entity updates.

---

# Engineering Decisions

The project is intentionally more than a CRUD application. Several parts were designed around problems that occur in real backend systems.

## 1. Domain-Driven Trust Score Calculation

A simple attendance percentage does not adequately represent volunteer reliability.

Consider two volunteers:

```text
Volunteer A
10 events attended
1 cancellation 48 hours before the event

Volunteer B
10 events attended
1 no-show
```

A simple attendance percentage can make these cases appear similar.

TaraTulong instead assigns different point deltas to attendance outcomes.

The calculation is based on explicit domain states represented by an `AttendanceStatus` enum.

This provides two important properties:

1. The scoring rules are centralized.
2. Changes to an attendance record can be calculated from the transition between states rather than blindly adding points.

MapStruct is also used to map domain values into API-friendly representations such as Trust Tiers.

---

## 2. Explicit State Transitions

Registration status is modeled explicitly instead of allowing arbitrary string values.

For example:

```java
public enum AttendanceStatus {
    PENDING(0),
    PRESENT(10),
    NO_SHOW(-25),
    CANCELLED_EARLY(-2),
    CANCELLED_LATE(-10);
}
```

This makes invalid states easier to prevent and keeps business rules close to the domain model.

It also provides a foundation for enforcing rules such as:

```text
PENDING → PRESENT
PENDING → CANCELLED_EARLY
PENDING → CANCELLED_LATE
PENDING → NO_SHOW
```

rather than allowing unrelated statuses to be assigned without validation.

---

## 3. Optimistic Locking for Concurrent Registrations

One of the more interesting concurrency problems is event capacity.

Imagine an event has one remaining slot:

```text
Event capacity: 30
Current registrations: 29
```

Two volunteers submit applications at nearly the same time.

Without concurrency control, both transactions could potentially observe the event as having an available slot.

TaraTulong uses **JPA optimistic locking** with `@Version` to detect conflicting updates.

When a stale entity attempts to update a record that has already changed, Hibernate raises an optimistic-locking exception.

The API catches this through a global `@ControllerAdvice` and translates the failure into an appropriate HTTP response such as:

```http
409 Conflict
```

This allows the database to remain the source of truth while providing a meaningful API-level response to the client.

---

## 4. Handling the N+1 Query Problem

Entity relationships make it easy to accidentally generate large numbers of SQL queries.

For example:

```text
Get Events
   │
   ├── Get Organization
   ├── Get Registrations
   │      ├── Get Volunteer
   │      ├── Get Volunteer
   │      └── Get Volunteer
   └── ...
```

This can result in the classic **N+1 query problem**.

To address this, the project:

* Uses `FetchType.LAZY` where appropriate
* Defines explicit repository queries for read-heavy operations
* Uses `JOIN FETCH` where eager loading is actually required
* Uses dedicated `countQuery` definitions when paginating complex queries

The goal is not to make every relationship eager, but to make fetching behavior intentional.

---

## 5. DTO-Based API Boundary

JPA entities are not exposed directly through REST endpoints.

Instead, controllers operate on DTOs / Java Records.

For example:

```text
HTTP Request
     │
     ▼
RegistrationRequest
     │
     ▼
Registration Entity
     │
     ▼
RegistrationResponse
     │
     ▼
HTTP Response
```

This provides a clear separation between:

* Persistence representation
* Business/domain representation
* Public API representation

It also reduces the risk of over-posting and accidental exposure of internal fields.

---

## 6. API Versioning

Endpoints are versioned under:

```text
/api/v1/
```

For example:

```text
/api/v1/events
/api/v1/registrations
/api/v1/organizations
```

The intention is to establish an explicit API contract so that future frontend or mobile clients can evolve independently from the backend.

---

## 7. Database Indexing

Frequently queried columns and foreign-key relationships are indexed where appropriate.

Examples include:

```text
event_id
organization_id
volunteer_id
```

The purpose is to reduce unnecessary table scanning as the dataset grows.

Indexes are treated as a query-performance optimization rather than something that should be added indiscriminately. The appropriate indexes depend on actual query patterns and database execution plans.

---

# Security

## Stateless JWT Authentication

Authentication uses Spring Security with JWT-based stateless authentication.

Requests provide a token through:

```http
Authorization: Bearer <token>
```

The backend validates the token and establishes the authenticated user before protected endpoints are executed.

---

## Role-Based Access Control

The application separates permissions between:

```text
ADMIN
ORGANIZATION
VOLUNTEER
```

Authorization is enforced both at the security configuration level and within the service layer where resource ownership matters.

---

## Resource Ownership / IDOR Prevention

Authorization does not stop at checking a user's role.

For example, being an `ORGANIZATION` user does not automatically mean that the user can modify every event in the system.

Before performing organization-specific operations, the service layer verifies that the authenticated organization actually owns the requested resource.

Conceptually:

```text
Authenticated Organization
          │
          ▼
Does this organization own Event #123?
          │
      ┌───┴───┐
     YES      NO
      │        │
      ▼        ▼
  Continue    403
```

This prevents insecure direct object reference (IDOR) scenarios where a user attempts to manipulate another organization's resources by changing an ID in the request.

---

# API Documentation

The API uses **OpenAPI 3** and Swagger UI.

After starting the application, Swagger UI is available at:

```text
http://localhost:8080/swagger-ui.html
```

It provides an interactive interface for:

* Exploring endpoints
* Viewing request/response schemas
* Understanding required parameters
* Testing API operations
* Authorizing requests using JWT

---

# Getting Started

## Prerequisites

Make sure you have:

* Java 21
* Maven
* PostgreSQL

## 1. Clone the repository

```bash
git clone https://github.com/yourusername/taratulong.git
cd taratulong
```

## 2. Create the PostgreSQL database

```sql
CREATE DATABASE taratulong;
```

## 3. Configure the database

Configure your PostgreSQL connection in `application.properties` or through environment variables.

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/taratulong
spring.datasource.username=your_username
spring.datasource.password=your_password
```

For local development, credentials should preferably be supplied through environment variables rather than committed to the repository.

## 4. Build the project

```bash
mvn clean install
```

## 5. Run the application

```bash
mvn spring-boot:run
```

The API will start on:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

---

# Project Status

TaraTulong is an actively developed backend project.

### Implemented

* [x] REST API
* [x] PostgreSQL persistence
* [x] Spring Data JPA / Hibernate
* [x] JWT authentication
* [x] Role-based authorization
* [x] Event management
* [x] Volunteer registration workflow
* [x] Attendance tracking
* [x] Trust Score calculation
* [x] Optimistic locking
* [x] DTO-based API architecture
* [x] API versioning
* [x] OpenAPI / Swagger documentation
* [x] Database indexing
* [x] Global exception handling

### In Progress

* [ ] Volunteer opportunity discovery frontend
* [ ] Organization dashboard
* [ ] Volunteer dashboard
* [ ] Automated testing expansion
* [ ] Deployment
* [ ] Production observability

---

# What I Learned

The primary goal of TaraTulong was not simply to build another CRUD API.

The project gave me an opportunity to explore what happens when a backend has to enforce **real business rules and consistency constraints**.

In particular, I gained practical experience with:

* Designing REST APIs around domain workflows
* Modeling state transitions
* Managing relational data with JPA/Hibernate
* Identifying and addressing N+1 queries
* Using optimistic locking for concurrent updates
* Separating entities from API DTOs
* Implementing JWT authentication and RBAC
* Designing database indexes around query patterns
* Handling business exceptions at the API boundary
* Thinking about consistency and authorization beyond the controller layer

The most important lesson was that backend engineering is not primarily about creating endpoints.

It is about ensuring that **the system remains correct when users, requests, and data interact in ways that the happy path does not anticipate.**

---

# Roadmap

The longer-term goal is to turn TaraTulong into a complete volunteer-management platform.

Potential next steps include:

* Frontend application using React
* Organization verification workflow
* Volunteer feedback and post-event evaluation
* Event search and filtering
* Waitlist management
* Email / notification system
* Automated integration tests
* Dockerized deployment
* CI/CD pipeline
* Production monitoring and logging
* Role-specific dashboards
* More comprehensive audit history

---

# Project Philosophy

TaraTulong is both a product idea and a backend engineering project.

The product goal is to make community volunteering more accessible to smaller organizations.

The engineering goal is to understand how to build a system where **business rules, authorization, persistence, and concurrent operations remain consistent as complexity increases.**

That distinction is important: the project is intentionally being developed beyond basic CRUD to explore the engineering problems that appear in production backend systems.
