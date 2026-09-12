# Todo App

A todo list application built with Spring Boot, Spring MVC, and Thymeleaf, backed by PostgreSQL.

## Features

- Add a task with a description and a deadline
- Delete a task
- View a list of tasks with their deadlines and completion status
- Mark a task as completed (and reopen it)
- Search tasks by keyword
- Filter by completion status and sort by description, deadline, or status
- Edit an existing task's description or deadline


## Tech Stack

| Layer | Technology | Why |
|---|---|---|
| Language | Java 17 | LTS release, required by the Spring Boot version used |
| Framework | Spring Boot 4.1.1 | Provides auto-configuration, an embedded servlet container, and integrates the rest of the stack with minimal boilerplate |
| Web layer | Spring MVC | Server-rendered request/response handling; a natural fit alongside Thymeleaf for a classic client–server web app |
| View layer | Thymeleaf | Server-side HTML templating that integrates directly with Spring MVC's `Model` and form binding, including built-in validation error display |
| Persistence | Spring Data JPA (Hibernate) | Removes hand-written CRUD/query boilerplate; `JpaSpecificationExecutor` is used for the dynamic search/filter/sort queries |
| Database | PostgreSQL 17 | Reliable, open-source relational database; a natural fit given the app's structured, relational data (tasks with fixed fields) |
| Validation | Jakarta Bean Validation (`spring-boot-starter-validation`) | Declarative validation (`@NotBlank`, `@NotNull`) on request DTOs, enforced automatically by Spring MVC before a controller method runs |
| Boilerplate reduction | Lombok | Generates getters/setters/constructors/builders for entities and DTOs, keeping those classes focused on their fields |
| Build tool | Gradle | Project's chosen build tool; manages dependencies and packages the app into a runnable jar |
| Containerization | Docker & Docker Compose | Packages the app and its dependencies (Postgres, pgAdmin) into reproducible, isolated containers that can be started with a single command |
| DB administration | pgAdmin | Web-based GUI for inspecting and querying the Postgres database during development |

## Architecture

### Client–server model

The application follows a client–server architecture: the browser (client) sends HTTP requests to the Spring Boot application (server), which processes them and returns rendered HTML. This is the traditional server-rendered flavor of client–server — as opposed to a decoupled REST API + separate frontend — chosen because it keeps the project simpler for the current scope, at the cost of the client and server not being independently deployable or swappable.

### Layered (MVC + Service) architecture

Within the server, the code is organized into a strict top-down layering, so each layer only knows about the one directly below it:

```
Browser (Thymeleaf-rendered HTML)
|
v
Controller layer -- handles HTTP requests, talks only to the service layer via DTOs
|
v
Service layer -- business logic, orchestration, entity <-> DTO mapping
|
v
Repository layer -- Spring Data JPA, talks only to the database
|
v
PostgreSQL
```

- **Controllers** (`controller/`) depend only on the service layer, never the repository. They accept and return Request/Response DTOs, not entities.
- **Services** (`service/`, `service/impl/`) contain all business logic and are the only layer that maps between entities and DTOs. This keeps persistence details out of the controllers and DTO/validation concerns out of the repository.
- **Repositories** (`repository/`) are plain Spring Data JPA interfaces. Dynamic search/filter/sort queries are built with JPA `Specification`s (`repository/spec/`) rather than hand-written queries.
- **DTOs** (`dto/request/`, `dto/response/`) are the only objects that cross the controller boundary. `TodoRequest` carries validated input from forms; `TodoResponse` carries data out to templates, including a `overdue` flag computed by the service layer so the templates don't need date logic.
- **Entities** (`entity/`) are used exclusively by the persistence layer and never leave the service layer.
- **Exception handling** (`exception/`) uses a custom `TodoNotFoundException` and a `@ControllerAdvice` to keep error handling out of individual controller methods.

### Deployment architecture

At runtime, three containers run together via Docker Compose:

```
┌──────────────┐     ┌─────────────┐
│ todo-app     │────►│ postgres    │
│ (Spring MVC) │     │ (port 5433  │
│ port 8080    │     │ on host)    │
└──────────────┘     └──────┬──────┘
                           │
                    ┌──────┴──────┐
                    │ pgadmin     │
                    │ port 5050   │
                    └─────────────┘
```

`todo-app` reaches Postgres over the internal Docker network using the service name `postgres` and Postgres's internal port `5432` — the `5433` host-side mapping is only for connecting from your own machine (e.g. via a local SQL client).

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- (Only needed if running outside Docker) Java 17+ and Gradle

## Running Locally

### Option A — Docker Compose (recommended)

This starts the app, PostgreSQL, and pgAdmin together, with the app's image built from the included `Dockerfile`.

```bash
docker compose up -d --build
```

Once the containers are healthy:

- **App**: http://localhost:8080
- **pgAdmin**: http://localhost:5050 — login `admin@admin.com` / `admin`
- **Postgres** (from your host machine, e.g. a SQL client): `localhost:5433`, user `postgres`, password `password`, db `postgres`

To view logs:
```bash
docker compose logs -f todo-app
```

To stop everything:
```bash
docker compose down
```

To stop and also wipe the database volumes:
```bash
docker compose down -v
```

### Option B — Run the app directly, database in Docker

Useful during development for faster restart cycles.

```bash
# Start only the database and pgAdmin
docker compose up -d postgres pgadmin

# Run the app on your machine
./gradlew bootRun
```

The app connects to Postgres using the settings in `src/main/resources/application.properties`, which point at `localhost:5432` by default — if you're only starting `postgres` via Compose, either add a matching host port mapping or update that file to use `localhost:5433` to match the compose file's port mapping.

Visit http://localhost:8080.

## Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/todos` | List tasks (supports `keyword`, `completed`, `sortBy`, `direction` query params) |
| GET | `/todos/new` | Show the add-task form |
| POST | `/todos` | Create a task |
| GET | `/todos/{id}/edit` | Show the edit-task form |
| POST | `/todos/{id}` | Update a task |
| POST | `/todos/{id}/delete` | Delete a task |
| POST | `/todos/{id}/complete` | Set completion status (`completed=true/false`) |