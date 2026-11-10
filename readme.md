# SecureTeamAPI

![CI](https://github.com/chatok-jnr/secure-team-api/actions/workflows/ci.yml/badge.svg)

A team and project management REST API built with **Spring Boot** and **Spring Security**.

> 🎯 **Main goal of this project: to learn and practice Spring Security in depth.**
> The team/project features exist to give security a realistic app to protect: real roles, real permissions, and real attack surfaces.

---

## What does it do?

SecureTeamAPI lets an organization manage **users**, **projects**, and **project members**, with three roles:

| Role | What they can do |
|------|------------------|
| **ADMIN** | Manage all users and all projects, view the admin dashboard |
| **MANAGER** | Create and manage their own projects and members, view the manager dashboard |
| **EMPLOYEE** | View the projects they are a member of |

Think of it as a small backend for a company where **who you are decides what you can see and do**.

---

## Security features (the main focus)

### Authentication
- User registration and login
- **Email verification with OTP**: new accounts must prove they own their email before they can use the platform
- **JWT access + refresh tokens**, stored in cookies
- **Refresh token rotation**: every refresh revokes the old token and issues a new one
- Logout, and **logout from all devices**
- Session management: list your active sessions and revoke any of them
- Forgot password and reset password (reset revokes all sessions)
- Change password (revokes existing sessions)

### Email verification (OTP)
- On registration the account is created with `isEmailVerified = false` and a **6-digit OTP** is emailed to the user
- OTPs are generated with `SecureRandom` (not `Random`)
- OTPs expire after **15 minutes** and are **single-use** (marked with `usedAt` once consumed)
- OTPs are **never stored in plain text**: only an **HMAC-SHA256** hash is saved, keyed with a server-side secret (`app.otp.secret`). A plain SHA-256 hash of a 6-digit code could be brute-forced instantly if the database leaked
- OTP hashes are compared in **constant time** (`MessageDigest.isEqual`)
- Each OTP has a **type** (`EMAIL_VERIFICATION`, `TWO_FACTOR_AUTHENTICATION`), so a code issued for one purpose can't be used for another
- Resend is blocked if the email is already verified or an active OTP already exists
- After successful verification the user is signed in (access + refresh cookies are issued)

### Authorization
- **Role-based access control** (ADMIN / MANAGER / EMPLOYEE)
- **Method-level security** with `@EnableMethodSecurity` and `@PreAuthorize`
- **Fine-grained permissions** (`USER_*`, `PROJECT_*`)
- **Object-level authorization**: a manager can only edit *their own* project, an employee can only see projects they belong to

### Hardening
- **CSRF mitigation**: the API is stateless and its auth cookies use `SameSite=Strict`, so Spring's CSRF filter is disabled
- **CORS** configured for credentialed requests
- **Cookie hardening**: `HttpOnly`, `Secure`, `SameSite`
- **Account locking** after repeated failed logins
- Account enable/disable support
- Centralized security error handling

---

## Tech stack

- Java 25 + Spring Boot 4
- Spring Security 7
- Spring Data JPA + PostgreSQL
- JWT
- Email (OTP and notification emails)
- REST API
- **Testing:** JUnit 5, Mockito, MockMvc, Testcontainers, JaCoCo
- **DevOps:** Docker, GitHub Actions, GitHub Container Registry, Railway, Dependabot
- Spring Boot Actuator (health endpoint)

---

## API overview

### Auth
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/auth/register` | Public (sends a verification OTP) |
| POST | `/api/auth/verify-email` | Public (email + OTP) |
| POST | `/api/auth/verify-email/resend?email=` | Public (sends a new OTP) |
| POST | `/api/auth/login` | Public (verified email required) |
| POST | `/api/auth/refresh` | Refresh cookie |
| POST | `/api/auth/logout` | Authenticated |
| POST | `/api/auth/logout-all` | Authenticated |
| GET | `/api/auth/sessions` | Authenticated |
| DELETE | `/api/auth/sessions/{id}` | Authenticated |
| POST | `/api/auth/forgot-password` | Public |
| PATCH | `/api/auth/reset-password` | Public (reset token) |

### Users
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/api/users/me` | Authenticated |
| PATCH | `/api/users/password` | Authenticated |
| GET | `/api/users` | ADMIN |
| GET | `/api/users/{id}` | ADMIN, or the user themself |
| POST | `/api/users` | ADMIN |
| PATCH | `/api/users/{id}/role` | ADMIN |
| PATCH | `/api/users/{id}/disable` | ADMIN |

### Projects
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/projects` | ADMIN, MANAGER |
| GET | `/api/projects` | ADMIN (all), MANAGER (managed), EMPLOYEE (member of) |
| GET | `/api/projects/{id}` | ADMIN any, MANAGER own, EMPLOYEE member |
| PATCH | `/api/projects/{id}` | ADMIN any, MANAGER own |
| DELETE | `/api/projects/{id}` | ADMIN |
| GET | `/api/projects/{projectId}/members` | ADMIN, MANAGER, project members |
| POST | `/api/projects/{projectId}/members/{userId}` | ADMIN, project manager |
| DELETE | `/api/projects/{projectId}/members/{userId}` | ADMIN, project manager |

### Dashboards
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/api/manager/dashboard` | MANAGER |
| GET | `/api/admin/dashboard` | ADMIN |

### Health
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/actuator/health` | Public |

### Audit logs *(coming soon)*
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/api/admin/audit-logs` | ADMIN |

---

## Email verification flow

```
POST /api/auth/register
  -> user saved with isEmailVerified = false
  -> 6-digit OTP generated, HMAC-SHA256 hash saved, plain code emailed

POST /api/auth/verify-email   { "email": "...", "otp": "048213" }
  -> latest active OTP found (not used, not expired)
  -> submitted code hashed and compared in constant time
  -> OTP marked as used, user marked as verified, session cookies issued

POST /api/auth/verify-email/resend?email=user@example.com
  -> rejected if already verified or an active OTP exists
  -> otherwise a new OTP is generated and emailed
```

---

## Testing

The project has **65+ automated tests** across four layers. Each layer checks something the others can't.

| Layer | Tools | What it checks |
|-------|-------|----------------|
| **Service** (unit) | JUnit 5, Mockito | Business logic and every branch of `UserService`, with dependencies mocked |
| **Controller** (slice) | `@WebMvcTest`, MockMvc | Status codes, JSON, request validation, error format, and role rules (401 / 403) |
| **Repository** (slice) | `@DataJpaTest`, Testcontainers | Custom JPQL queries, DTO projections, paging, and group-by counts against a real PostgreSQL |
| **Integration** | `@SpringBootTest`, Testcontainers | Real login, JWT cookies, role-based access end to end, and the health endpoint |

Repository and integration tests run against a **real PostgreSQL started in Docker by Testcontainers**, not an in-memory database, so queries behave as they do in production.

**Current coverage:** the user management layer and the login flow. The auth service, admin, manager and project modules are next. JaCoCo generates a coverage report, and no minimum threshold is enforced yet.

### Run the tests

```bash
# Docker must be running (Testcontainers starts PostgreSQL for you)
mvn verify

# Coverage report
open target/site/jacoco/index.html
```

---

## CI/CD pipeline

Built with **GitHub Actions** (`.github/workflows/ci.yml`).

```
Pull request ──► test ──► branch protection blocks the merge if it fails

Merge to main ─► test ──► docker ──► deploy
                         build and    redeploy
                         push image   on Railway
```

- **`test`**: runs `mvn verify` on a clean Ubuntu runner for every pull request and every push to `main`. It uses fake secrets, and the database comes from Testcontainers. Test and coverage reports are uploaded as artifacts.
- **`docker`**: runs only on `main` and only if `test` passed. It builds the Docker image and pushes it to GitHub Container Registry with two tags: the commit SHA (for rollbacks) and `production` (the tag the host follows).
- **`deploy`**: runs only if `docker` succeeded, and tells Railway to redeploy the service.

If a test fails, nothing is built or deployed.

Other safeguards:
- **Branch protection** on `main`: a failing build can't be merged
- **Dependabot** opens pull requests for outdated dependencies and actions, and the pipeline tests them like any other change
- **Concurrency control** cancels outdated runs on the same branch
- **Least-privilege permissions** and job timeouts

Real secrets (database URL, JWT and OTP secrets, mail credentials) live only in the host's environment variables, never in the repository.

---

## How the project was built

Every feature was built **only after everything it depends on already existed**, so nothing had to be rewritten later.

```
                    User
                     |
          +----------+----------+
          v                     v
    Authentication           Project
                                |
                                v
                         Project Member
                                |
                 +--------------+--------------+
                 v                             v
            Manager Dashboard         Employee Project Access
                 |                             |
                 +--------------+--------------+
                                v
                         Admin Dashboard
```

The build order, step by step:

1. **Security foundation**: filter chain, JWT service and filter, cookies, error handling
2. **Users and authentication**: register, login, refresh, rotation, logout
3. **User self-service**: profile, change password
4. **User management** (admin): list, get, create, change role, disable
5. **Projects and membership**: create, members, role-based project listing
6. **Dashboards**: manager and admin
7. **Authorization hardening**: method security, permissions, CSRF handling, CORS, cookie hardening
8. **Advanced account security**: account locking, forgot/reset password, session management
9. **Auditing**: audit logging and admin audit log
10. **Email verification**: OTP generation, HMAC hashing, verify and resend flow
11. **Testing**: unit, controller, repository and integration tests
12. **CI/CD**: automated tests, Docker image, deployment to Railway

---

## Getting started

```bash
# 1. Clone the repository
git clone https://github.com/chatok-jnr/secure-team-api.git
cd secure-team-api

# 2. Create a .env file in the project root (it is gitignored)
#    DB_URL=jdbc:postgresql://localhost:5432/secure_team
#    DB_USERNAME=...
#    DB_PASSWORD=...
#    JWT_SECRET=...        (long random string)
#    OTP_SECRET=...        (long random string)
#    MAIL_USERNAME=...
#    MAIL_PASSWORD=...
#    MAIL_FROM=...
#    Never commit secrets.

# 3. Run the app
./mvnw spring-boot:run
```

The API will start on `http://localhost:8080`. Check it with `GET /actuator/health`.

### Run with Docker

```bash
docker pull ghcr.io/chatok-jnr/secure-team-api:production
```

Pass the same variables as environment variables when you run the container.

---

## What I learned

- How the Spring Security filter chain works from request to response
- Stateless authentication with JWT stored in secure cookies
- Why refresh token rotation matters, and how to implement it
- Role-based vs. permission-based vs. object-level authorization
- Defending against CSRF, misconfigured CORS, and insecure cookies
- Building account protections: locking, session revocation, password reset
- Email verification with OTPs: why a short code needs a keyed hash (HMAC) instead of plain SHA-256, and why `SecureRandom` and constant-time comparison matter
- The testing pyramid in Spring Boot: when to use a Mockito unit test, a slice test, or a full integration test
- Why Testcontainers beats an in-memory database for testing real queries
- How a CI/CD pipeline works end to end: tests, Docker image, registry, deployment, and why each job depends on the previous one
- Debugging problems that only appear on a clean machine, such as a mail health check failing in CI because of fake credentials
- Branch protection, Dependabot and JaCoCo coverage reports

---

## What's next

- Tests for the auth, admin, manager and project modules, then a minimum coverage threshold
- A post-deploy health check gate
- Database migrations with Flyway instead of `ddl-auto: update`

---

## Author

**Chatok Junior**