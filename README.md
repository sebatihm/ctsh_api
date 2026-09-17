# CTSH_API

A Spring Boot REST API for user management and JWT-based authentication made for the ctsh proyect, built with Spring Security and MySQL.

## Tech stack

- Java 26, Spring Boot 4.1.1 (Maven wrapper)
- Spring Security + JWT (jjwt 0.12.6)
- Spring Data JPA (Hibernate)
- MySQL 8 (via Docker Compose)
- Lombok, bean validation

## Requirements

- JDK 26
- Docker (for MySQL)

## Getting started

1. Start MySQL:

   ```sh
   docker compose up -d
   ```

2. Set the required environment variables (optional, has dev defaults):

   - `CTSH_PEPPER` — pepper used by the password encoder
   - `CTSH_JWT_SECRET` — secret used to sign JWT tokens

3. Run the application:

   ```sh
   ./mvnw spring-boot:run
   ```

The API is served under the `/api` context path. CORS is enabled for `http://localhost:3000` by default.

## Endpoints

| Method | Path    | Access      | Description            |
|--------|---------|-------------|------------------------|
| POST   | `/api/login` | Public | Authenticate and get a JWT token |
| POST   | `/api/user`  | Public | Create a user |
| GET    | `/api/user`  | ADMIN  | List all users |
| GET    | `/api/user/{uuid}` | ADMIN | Get a user by id |
| PUT    | `/api/user/{uuid}` | ADMIN | Update a user |
| DELETE | `/api/user/{uuid}` | ADMIN | Delete a user |

## Configuration

Database and security settings live in `src/main/resources/application.properties`.

## Tests

```sh
./mvnw test
```