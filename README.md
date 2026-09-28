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
| POST   | `/api/logout` | Public | Clears the `jwt` auth cookie |
| POST   | `/api/user`  | Public | Create a user, `multipart/form-data` |
| GET    | `/api/user`  | ADMIN  | List all users |
| GET    | `/api/user/{uuid}` | Owner or ADMIN | Get a user by id |
| PUT    | `/api/user/{uuid}` | Owner or ADMIN | Update a user, `multipart/form-data` |
| DELETE | `/api/user/{uuid}` | Owner or ADMIN | Delete a user, returns `204` |
| GET    | `/api/uploads/{file}` | Public | Serve a stored profile picture |

`/api/logout` is not a controller endpoint — it is Spring Security's `LogoutFilter` (`src/main/java/com/ctsh/ctsh_api/config/SecurityConfig.java`), wired to `JwtLogoutHandler`, which both deletes the cookie and writes the `200` JSON body. Because `LogoutFilter` runs *before* `JwtAuthenticationFilter`, logout never inspects the token: it succeeds with an expired, malformed, or missing cookie instead of returning `401`. The trade-off is that a client with a dead token can still reliably clear its cookie.

Creating and updating users take `multipart/form-data`, not JSON. See [Profile pictures](#profile-pictures) for the fields.

`GET /api/user` lists every user and stays ADMIN-only. The `/user/{uuid}` routes instead require just `authenticated()`: Spring Security does not know whose record is being requested, so `UserService.requireSameUserOrAdmin` makes the real decision and rejects anything that is neither the owner nor an ADMIN with a `403`. The split is deliberate — coarse authorization at the edge, fine-grained ownership in the service. A `USER` cannot escalate to `ADMIN` this way, because the update DTO carries no `role` field and `updateUser` never touches it.

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
- `UserResponseDto.profilePicture` is an absolute, browser-ready URL built by `FileService.getPublicUrl` — for example `http://localhost:8080/api/uploads/profiles/9f1c….png`. The client must not construct it. It is `null` when the user has no picture, so "no photo" and "photo failed to load" stay distinguishable.

The database still stores the bare file name in `User.profilePicture`; only the response is a URL, so no data migration is needed when you deploy this.

Validation happens in `FileService`: the size must be under 5 MB, the content type must start with `image/`, and the extension must be one of `jpg`, `jpeg`, `png`, `webp`, `gif`.

Files are written to `uploads/profiles/<random-uuid>.<ext>` and served as static resources by `WebConfig`, which maps `/uploads/**` to the `file:uploads/` location. Two consequences worth knowing:

- `uploads/` is a relative path, so it resolves against the process working directory. The directory is git-ignored, and its contents are local-only state — do not expect them in a fresh clone or a scaled-out deployment.
- The `/uploads/**` route is `permitAll`, so profile pictures are readable by anyone who knows or guesses the file name. The names are random UUIDs, which is the only thing protecting them. Do not treat them as private.

### Upload size limit

Two limits apply, both set in `application.properties`:

| Property | Value | Applies to |
|----------|-------|------------|
| `spring.servlet.multipart.max-file-size` | `5MB` | One file |
| `spring.servlet.multipart.max-request-size` | `6MB` | The whole multipart body |

`max-request-size` is deliberately larger than `max-file-size`: the request also carries the `name`, `email` and `password` parts plus the MIME boundaries, so an image near the per-file limit would trip a request limit set to the same value and be rejected for the wrong reason.

The container rejects an oversized upload with a `413` before it reaches the controller, so `FileService.validateFile` never sees it — that is why the two layers agree on the same 5 MB number instead of one shadowing the other. `GlobalExceptionHandler` maps that `413` to the same `ApiError` envelope as every other error, and `validateFile` returns a `400` for files that clear the size gate but fail the content-type or extension check.

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