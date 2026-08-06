TaraTulong is a scalable, RESTful backend platform designed to connect community organizations with reliable volunteers. Developed entirely as a solo project, it handles the complexities of volunteer management—including capacity constraints, event attendance tracking, and dynamic "Trust Score" aggregation—while maintaining strict performance and security standards.

The motivation behind TaraTulong stems from hands-on community service experiences, such as tutoring programs at Macanhan Elementary School. A recurring challenge for organizations is volunteer flakiness. TaraTulong solves this by algorithmically calculating a dynamic volunteer "Trust Score" based on reliable attendance, enabling NGOs to make informed decisions for high-demand, limited-capacity events.
🚀 Tech Stack

    Language: Java 21

    Framework: Spring Boot 3

    Data & ORM: Spring Data JPA, Hibernate, PostgreSQL

    Security: Spring Security, JWT Authentication

    Documentation: Swagger / OpenAPI 3.0

    Tooling: MapStruct, Maven, Lombok

🧠 Architectural Decisions & Highlights

As a platform designed for potential high-traffic spikes (e.g., disaster relief mobilization), the architecture prioritizes data integrity, query optimization, and clean API boundaries.
1. Zero-Exposure Web Layer (API Versioning & Records)

The application strictly separates database entities from the web layer to prevent over-posting vulnerabilities and schema leakage.

    Implemented API Versioning (e.g., /api/v1/registrations) to ensure backward compatibility for future mobile clients.

    Replaced traditional raw JPA entity exposure with immutable Java Records and DTOs.

    Utilized MapStruct for compile-time, zero-overhead mapping between Entities and DTOs.

2. Defeating the N+1 Problem

Complex relationships (like fetching an Event, its Registrations, and the associated Volunteers) easily trigger N+1 query bottlenecks in JPA.

    Configured @ManyToOne relationships to default to FetchType.LAZY.

    Implemented custom JPQL queries using JOIN FETCH coupled with countQuery definitions to safely paginate complex relational data in a single, efficient database round-trip.

3. Concurrency & Data Integrity

When a highly anticipated volunteer event opens, multiple users may attempt to claim the final slot at the exact same millisecond.

    Implemented Optimistic Locking using @Version to handle simultaneous database writes.

    The system intercepts ObjectOptimisticLockingFailureException and routes it through a @ControllerAdvice Global Exception Handler to return clean, actionable HTTP 409 Conflict responses to the frontend.

4. High-Performance State Transitions (Trust Score)

Calculating a volunteer's reliability score on the fly by scanning their entire history is resource-heavy.

    Employed Denormalization by maintaining a running tally on the Volunteer entity.

    Engineered a thread-safe, stateless state-machine in the Service layer to handle attendance toggling (Present -> Absent -> Present). The logic relies exclusively on immutable database state transitions to perfectly aggregate scores without artificial inflation or server-memory amnesia.

    Optimized database search speeds by manually defining B-Tree Indexes (@Index) on frequently queried foreign keys (e.g., event_id, organization_id).

🔐 Security Configuration

    Stateless Authentication: Secured via JWTs passed in the Authorization header.

    Role-Based Access Control (RBAC): Distinct domains for Admin, Organization, and Volunteer roles.

    IDOR Prevention: Service-layer validations ensure that Organizations can only read or modify registrations for events they explicitly own.

📖 API Documentation (Swagger)

The API is fully documented using OpenAPI 3.0. Once the application is running locally, the interactive Swagger UI can be accessed to test endpoints, view required request schemas, and copy JWT tokens for authentication.

Access the UI at: http://localhost:8080/swagger-ui.html
⚙️ Running Locally

    Clone the repository:

    git clone https://github.com/yourusername/earthhub-taratulong.git
---
    Navigate to the project directory and build using Maven:

    mvn clean install
---
    Configure your PostgreSQL credentials in application.properties:

    spring.datasource.url=jdbc:postgresql://localhost:5432/taratulong
    spring.datasource.username=your_username
    spring.datasource.password=your_password

---
    Run the application:

    mvn spring-boot:run