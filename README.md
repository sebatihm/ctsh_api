# CTSH_API

A Spring Boot REST API for user management and JWT-based authentication made for the ctsh proyect, built with Spring Security and MySQL. Users are created and updated through `multipart/form-data` so a profile picture can be uploaded with the account.

## Tech stack

- Java 26, Spring Boot 4.1.1 (Maven wrapper)
- Spring Security + JWT (jjwt 0.12.6)
- Spring Data JPA (Hibernate)
- MySQL 8 (via Docker Compose)
- Local filesystem storage for profile pictures, served as static resources
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

The API is served under the `/api` context path. There is no CORS configuration: the auth cookie is `SameSite=Strict`, which only works same-origin. In production, serve the frontend and proxy `/api` to this service from a single public origin (for example nginx in the frontend container), so the browser only ever talks to one host.

## Endpoints

| Method | Path    | Access      | Description            |
|--------|---------|-------------|------------------------|
| POST   | `/api/login` | Public | Authenticate, sets the `jwt` auth cookie |
| POST   | `/api/logout` | Public | Clears the `jwt` auth cookie |
| POST   | `/api/user`  | Public | Create a user, `multipart/form-data` |
| GET    | `/api/user`  | ADMIN  | List all users |
| GET    | `/api/user/{uuid}` | ADMIN | Get a user by id |
| PUT    | `/api/user/{uuid}` | ADMIN | Update a user, `multipart/form-data` |
| DELETE | `/api/user/{uuid}` | ADMIN | Delete a user, returns `204` |
| GET    | `/api/uploads/{file}` | Public | Serve a stored profile picture |

`/api/logout` is not a controller endpoint — it is Spring Security's `LogoutFilter` (`src/main/java/com/ctsh/ctsh_api/config/SecurityConfig.java`), wired to `JwtLogoutHandler`, which both deletes the cookie and writes the `200` JSON body. Because `LogoutFilter` runs *before* `JwtAuthenticationFilter`, logout never inspects the token: it succeeds with an expired, malformed, or missing cookie instead of returning `401`. The trade-off is that a client with a dead token can still reliably clear its cookie.

Creating and updating users take `multipart/form-data`, not JSON. See [Profile pictures](#profile-pictures) for the fields.

## Profile pictures

`POST /api/user` and `PUT /api/user/{uuid}` bind a `multipart/form-data` body to the `UserRequestDto` record, so every field is a separate form part:

| Part | Create | Update | Rules |
|------|--------|--------|-------|
| `name` | required | optional | non-blank on create |
| `email` | required | optional | must be a valid email; must not belong to another user |
| `password` | required | ignored | at least 8 characters, hashed with the pepper |
| `profilePicture` | required | optional | image, see below |

Notes:

- On update, every part is optional — an absent part leaves the field untouched. A `profilePicture` part, when present, replaces the stored file and deletes the old one.
- `password` is only read on create; there is no password-change endpoint.
- A user is always created with the `USER` role.
- `UserResponseDto.profilePicture` is a bare file name (e.g. `9f1c….png`), not a URL. Build the URL as `/api/uploads/profiles/{profilePicture}`.

Validation happens in `FileService`: the size must be under 5 MB, the content type must start with `image/`, and the extension must be one of `jpg`, `jpeg`, `png`, `webp`, `gif`.

Files are written to `uploads/profiles/<random-uuid>.<ext>` and served as static resources by `WebConfig`, which maps `/uploads/**` to the `file:uploads/` location. Two consequences worth knowing:

- `uploads/` is a relative path, so it resolves against the process working directory. The directory is git-ignored, and its contents are local-only state — do not expect them in a fresh clone or a scaled-out deployment.
- The `/uploads/**` route is `permitAll`, so profile pictures are readable by anyone who knows or guesses the file name. The names are random UUIDs, which is the only thing protecting them. Do not treat them as private.

### Upload size limit

`FileService` rejects files over 5 MB, but that check is unreachable for anything larger than **1 MB**: there is no `spring.servlet.multipart.*` configuration, so the servlet container's default `max-file-size` (1 MB) rejects the request first and it never reaches the controller. To make the documented 5 MB limit real, add to `application.properties`:

```properties
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
```

## Configuration

Database and security settings live in `src/main/resources/application.properties`.

### Auth cookie

| Key | Default | Notes |
|-----|---------|-------|
| `app.jwt.ttl-seconds` | `3600` | Access token lifetime |
| `app.cookie.name` | `jwt` | Auth cookie name |
| `app.cookie.secure` | `true` | Set to `false` only for local testing over plain HTTP on a LAN IP |
| `app.cookie.same-site` | `Strict` | Requires same-origin, see above |
| `app.cookie.max-age-seconds` | `${app.jwt.ttl-seconds}` | Derived from the token TTL, so they cannot drift apart |

The cookie is `HttpOnly`, so the frontend never reads it and auth happens automatically on every request.

A browser only stores a `Secure` cookie over HTTPS. `http://localhost` counts as a secure context in modern browsers, so local development works — but `http://192.168.x.x` does not, and with `app.cookie.secure=true` the login will appear to succeed while the cookie is silently discarded.

### If you ever split the frontend and API onto different origins

`SameSite=Strict` will not send the cookie cross-origin. Supporting that setup requires **all three** changes at once:

1. `app.cookie.same-site=None` (with `app.cookie.secure=true`, browsers reject `SameSite=None` without `Secure`).
2. A real CORS configuration with `allowCredentials(true)` and explicit origins — a wildcard `*` is rejected by the browser when credentials are involved.
3. Re-enabling CSRF, which is currently disabled. Use `CookieCsrfTokenRepository.withHttpOnlyFalse()` with an endpoint that exposes the token, and have the client send the `X-XSRF-TOKEN` header on every non-GET request.

Skipping step 3 turns cookie-based auth into a CSRF vulnerability.

## Tests

```sh
./mvnw test
```