# CTSH_API

A Spring Boot REST API for user management, global mood counters and JWT-based authentication made for the ctsh proyect, built with Spring Security and MySQL. Users are created and updated through `multipart/form-data` so a profile picture can be uploaded with the account.

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
   - `CTSH_BACKEND_URL` — public base URL of this service, used to build profile picture URLs

3. Run the application:

   ```sh
   ./mvnw spring-boot:run
   ```

The API is served under the `/api` context path. There is no CORS configuration: the auth cookie is `SameSite=Strict`, which only works same-origin. In production, serve the frontend and proxy `/api` to this service from a single public origin (for example nginx in the frontend container), so the browser only ever talks to one host.

## Endpoints

| Method | Path    | Access      | Description            |
|--------|---------|-------------|------------------------|
| POST   | `/api/login` | Public | Authenticate, sets the `jwt` auth cookie |
| POST   | `/api/logout` | Authenticated | Clears the `jwt` auth cookie |
| POST   | `/api/user`  | Public | Create a user, `multipart/form-data` |
| GET    | `/api/user`  | ADMIN  | List all users |
| GET    | `/api/user/{uuid}` | Owner or ADMIN | Get a user by id |
| PUT    | `/api/user/{uuid}` | Owner or ADMIN | Update a user, `multipart/form-data` |
| DELETE | `/api/user/{uuid}` | Owner or ADMIN | Delete a user, returns `204` |
| GET    | `/api/mood` | Authenticated | List all moods |
| GET    | `/api/mood/{name}` | Public | Get one mood by name |
| POST   | `/api/mood/{name}` | Public | Increment a mood counter, creating it on first use |
| DELETE | `/api/mood/{name}` | ADMIN | Delete a mood, returns `204` |
| GET    | `/api/uploads/{file}` | Public | Serve a stored profile picture |

`/api/logout` is a controller endpoint (`AuthController.logout`), not Spring Security's `LogoutFilter`, which is explicitly disabled in `SecurityConfig.java`. The route requires authentication, so logout answers `401` unless the request carries a valid token. `JwtAuthenticationFilter` does clear the cookie as a side effect when a token is present but expired or malformed, but the response is still `401`, so a client with a dead token never gets a clean `200` out of logout.

Creating and updating users take `multipart/form-data`, not JSON. See [Profile pictures](#profile-pictures) for the fields.

`GET /api/user` lists every user and stays ADMIN-only. The `/user/{uuid}` routes instead require just `authenticated()`: Spring Security does not know whose record is being requested, so `UserService.requireSameUserOrAdmin` makes the real decision and rejects anything that is neither the owner nor an ADMIN with a `403`. The split is deliberate — coarse authorization at the edge, fine-grained ownership in the service. A `USER` cannot escalate to `ADMIN` this way, because the update DTO carries no `role` field and `updateUser` never touches it.

## Moods

`Mood` is a global counter keyed by name. `POST /api/mood/happy` moves `happy` from 41 to 42, and the row is created with `count = 1` the first time anyone asks for it. There is no per-user vote and no vote history, so the same client can increment the same mood as many times as it wants.

The increment is a single `INSERT ... ON DUPLICATE KEY UPDATE count = count + 1` (`MoodRepository.upsertIncrement`). One statement is what makes it race-free: two simultaneous first votes for a new name cannot both insert, and the loser is folded into the same row atomically instead of failing on the `UNIQUE` index and needing a retry.

Names are lowercased and must then match `^[a-z0-9_-]{1,50}$` — ASCII only, no spaces and no accents. Anything else is a `400` before it reaches the database. `GET /api/mood/Happy` and `GET /api/mood/happy` are the same request, but `GET /api/mood/very%20happy` is not a valid name at all.

## Profile pictures

`POST /api/user` and `PUT /api/user/{uuid}` bind a `multipart/form-data` body to the `UserRequestDto` record, so every field is a separate form part:

| Part | Create | Update | Rules |
|------|--------|--------|-------|
| `name` | required | optional | non-blank on create |
| `email` | required | optional | must be a valid email; must not belong to another user |
| `password` | required | ignored | at least 8 characters, hashed with the pepper |
| `profilePicture` | required | optional | image, see below |

Notes:

- A user is always created with the `USER` role.
- `UserResponseDto.profilePicture` is an absolute, browser-ready URL built by `FileService.getPublicUrl` — for example `http://localhost:8080/api/uploads/profiles/9f1c….png`. The client must not construct it. It is `null` when the user has no picture, so "no photo" and "photo failed to load" stay distinguishable.


Validation happens in `FileService`: the size must be under 5 MB, the content type must start with `image/`, and the extension must be one of `jpg`, `jpeg`, `png`, `webp`, `gif`.

Files are written to `uploads/profiles/<random-uuid>.<ext>` and served as static resources by `WebConfig`, which maps `/uploads/**` to the `file:uploads/` location. Two consequences worth knowing:

- `uploads/` is a relative path, so it resolves against the process working directory. The directory is git-ignored, and its contents are local-only state — do not expect them in a fresh clone or a scaled-out deployment.
- The `/uploads/**` route is `permitAll`, so profile pictures are readable by anyone who knows or guesses the file name. The names are random UUIDs, which is the only thing protecting them. Do not treat them as private.

## Configuration

Database and security settings live in `src/main/resources/application.properties`.

### Uploads and public URL

| Key | Default | Notes |
|-----|---------|-------|
| `app.url.backend` | `http://localhost:8080` | Public base URL this service is reached at, i.e. `${CTSH_BACKEND_URL}` |
| `app.uploads.dir` | `uploads` | On-disk root, relative to the process working directory |
| `app.uploads.url-path` | `/uploads` | Path the files are served at, under the context path |

`FileService.getPublicUrl` assembles the profile picture URL from all of them: `{app.url.backend}{server.servlet.context-path}{app.uploads.url-path}/profiles/{file}`, so the four parts cannot drift apart.

`app.url.backend` has to be the **public** origin, because the URL ends up in a JSON response that the end user's browser fetches. Do not put the container's internal address there. In production, where nginx serves the frontend and proxies `/api` to this service, set it to the public origin (`https://miapp.com`) so the browser only ever talks to one host.


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