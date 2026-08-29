# SalesTrack CRM

SalesTrack CRM is a RESTful CRM API that helps sales teams manage leads, companies, contacts, deals, and activities in one place.

The application includes JWT-based authentication, role-based authorization, sales pipeline tracking, and reporting.

## Features

- User registration, login, and JWT session management
- Role-based authorization (Sales Rep / Manager / Admin)
- Admin panel for user listing and role management
- Company and related contact management
- Lead creation, status tracking, and conversion to deals
- Deal management with pipeline stage transitions
- Deal detail view with full stage-change history and linked activities
- Deletion of leads and deals (with ownership/role checks)
- Logging of sales activities (calls, meetings, emails, notes)
- Reports: pipeline summary, conversion rate, won deals, and team performance
- Versioned database migrations with Flyway
- Interactive API documentation via OpenAPI/Swagger
- Quick local setup with Docker Compose + PostgreSQL

## Tech Stack

- Java & Spring Boot
- Spring Security / JWT
- Spring Data JPA (Hibernate)
- PostgreSQL
- Flyway
- Maven
- Docker & Docker Compose
- springdoc-openapi (Swagger UI)

## Project Structure

```text
src/main/java/com/salestrack
├── config/        # App, security, and OpenAPI configuration
├── controller/    # REST endpoints
├── dto/           # Request/response models
├── entity/        # JPA entities
├── enums/         # Lead, deal, activity, and role enums
├── exception/     # Centralized error handling
├── mapper/        # Entity/DTO conversions
├── repository/    # Data access layer
├── security/      # JWT and authentication components
└── service/       # Business logic

src/main/resources/db/migration/  # Flyway SQL migrations
src/main/resources/templates/     # Thymeleaf pages (login, register, dashboard, admin panel, etc.)
```

## Requirements

Either of the following is enough to run locally:

- Java (version required by the project's Maven configuration)
- Docker Desktop and Docker Compose

The project includes a Maven Wrapper, so a separate Maven installation isn't required.

## Environment Variables

Before running the app, copy `.env.example` to `.env` and fill in the values:

```bash
cp .env.example .env
```

```
DB_PASSWORD=your_postgres_password
JWT_SECRET=change_this_to_a_long_random_string
```

`docker compose up` reads this file automatically. Never commit a real `.env` file — it's already excluded via `.gitignore`.

## Quick Start

### Run with Docker

```bash
docker compose up --build
```

Run in the background:

```bash
docker compose up --build -d
```

Stop services:

```bash
docker compose down
```

### Run locally

Start PostgreSQL first and make sure the connection settings in `src/main/resources/application.properties` match your environment.

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS/Linux:

```bash
./mvnw spring-boot:run
```

The app runs at `http://localhost:8080` by default.

## Demo Login

> This is a portfolio project — seed credentials like these would never be shipped this way in a real production system.

| Email | Password | Role |
| --- | --- | --- |
| admin@salestrack.com | Admin123! | ADMIN |

New accounts can also be created via the **Register** page; self-registered users are assigned the `SALES_REP` role by default. Admins can promote other users from the **Admin Panel** (`/admin/users`).

A `scripts/demo-seed.sql` script is included to populate the database with realistic sample companies, contacts, deals, leads, and activities — useful for taking screenshots or exploring the app with non-empty data. It's meant to be run manually against a clean local database and is not a Flyway migration.

## API Documentation

While the app is running, explore and call endpoints via Swagger UI:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

For protected endpoints, log in first, then enter the JWT into Swagger's **Authorize** field as `Bearer <token>`.

## API Modules

| Module | Scope |
| --- | --- |
| Authentication | Register, login, and JWT issuance |
| Users | User listing and role management (admin only) |
| Companies | Company record management |
| Contacts | Company-linked contact management |
| Leads | Lead capture, status updates, conversion, and deletion |
| Deals | Deal creation, assignment, stage management, stage history, and deletion |
| Activities | Call, email, meeting, and note logging |
| Reports | Pipeline summary, conversion, won deals, and team performance |

## Typical Usage Flow

1. Register (or use the seeded admin) and log in to obtain a JWT.
2. Record companies and their contacts.
3. Add new leads and manage their source and status.
4. Convert qualified leads into deals.
5. Move deals through pipeline stages, assign an owner, and log activities.
6. Open a deal's detail page to review its full stage history and linked activities.
7. Admins manage user roles from the Admin Panel.
8. Monitor pipeline and team performance from the Reports endpoints.

## Database Migrations

Flyway applies the SQL files under `src/main/resources/db/migration` in version order on startup. The schema and core tables are created automatically on first run.

When adding a new migration, follow this naming convention:

```text
V9__descriptive_migration_name.sql
```

## Tests

Run all tests:

Windows:

```powershell
.\mvnw.cmd test
```

macOS/Linux:

```bash
./mvnw test
```

## Error Handling

Validation errors return field-level messages via a consistent `ErrorResponse` shape. Unmapped routes and unexpected server errors are served through a friendly Thymeleaf error page instead of the default Spring Boot whitelabel page.

## License

No license has been defined for this project yet. Adding an appropriate license file is recommended before publishing it as open source.
