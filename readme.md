# SecureTeamAPI

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
- **CSRF protection**
- **CORS** configured for credentialed requests
- **Cookie hardening**: `HttpOnly`, `Secure`, `SameSite`
- **Account locking** after repeated failed logins
- Account enable/disable support
- Centralized security error handling

---

## Tech stack

- Java + Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT
- Email (OTP and notification emails)
- REST API

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
| PATCH | `/api/users/me/password` | Authenticated |
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
7. **Authorization hardening**: method security, permissions, CSRF, CORS, cookie hardening
8. **Advanced account security**: account locking, forgot/reset password, session management
9. **Auditing**: audit logging and admin audit log
10. **Email verification**: OTP generation, HMAC hashing, verify and resend flow

---

## Getting started

```bash
# 1. Clone the repository
git clone https://github.com/chatok-jnr/secure-team-api.git
cd secure-team-api

# 2. Configure your database, JWT secret, OTP secret and mail settings in
#    src/main/resources/application.properties
#    (load secrets from environment variables, never commit them)

# 3. Run the app
./mvnw spring-boot:run
```

The API will start on `http://localhost:8080`.

---

## What I learned

- How the Spring Security filter chain works from request to response
- Stateless authentication with JWT stored in secure cookies
- Why refresh token rotation matters, and how to implement it
- Role-based vs. permission-based vs. object-level authorization
- Defending against CSRF, misconfigured CORS, and insecure cookies
- Building account protections: locking, session revocation, password reset
- Email verification with OTPs: why a short code needs a keyed hash (HMAC) instead of plain SHA-256, and why `SecureRandom` and constant-time comparison matter

---

## Author

**Chatok Junior**