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
- **JWT access + refresh tokens**, stored in cookies
- **Refresh token rotation**: every refresh revokes the old token and issues a new one
- Logout, and **logout from all devices**
- Session management: list your active sessions and revoke any of them
- Forgot password and reset password (reset revokes all sessions)
- Change password (revokes existing sessions)

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
- JWT
- REST API

---

## API overview

### Auth
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public |
| POST | `/api/auth/refresh` | Refresh cookie |
| POST | `/api/auth/logout` | Authenticated |
| POST | `/api/auth/logout-all` | Authenticated |
| GET | `/api/auth/sessions` | Authenticated |
| DELETE | `/api/auth/sessions/{id}` | Authenticated |
| POST | `/api/auth/forgot-password` | Public |
| POST | `/api/auth/reset-password` | Public (reset token) |

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

---

## Getting started

```bash
# 1. Clone the repository
git clone <your-repo-url>
cd secureteamapi

# 2. Configure your database and JWT secret in
#    src/main/resources/application.properties

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

---

## Author

**Chatok Junior**