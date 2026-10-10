# Spring Boot Project: Dependency-Based Development Order

Each feature is built only after everything it depends on already exists, so nothing has to be rewritten later.

**Progress:** 6 of 39 tasks done. Current task: **#06 Refresh token rotation**.

## Dependency graph

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

## Overview

| # | Task | Status |
|---|------|--------|
| 01 | Security foundation (SecurityFilterChain, JWT service/filter, cookies, error handling) | ✅ Done |
| 02 | User entity + UserRepository | ✅ Done |
| 03 | User registration | ✅ Done |
| 04 | User login | ✅ Done |
| 05 | Refresh token subsystem + refresh endpoint | ✅ Done |
| 06 | Refresh token rotation (validate A, revoke A, create B, issue new tokens) | ✅ Done |
| 07 | Logout | ✅ Done |
| 08 | Get my profile | ✅ Done |
| 09 | Change my password (revoke existing sessions) | done |
| 10 | Account enabled/disabled behavior (enabled, accountNonLocked, failedLoginAttempts -> UserDetails) | ✅ Done |
| 11 | Get all users | ✅ Done |
| 12 | Get user by ID | ✅ Done |
| 13 | Create user | ✅ Done |
| 14 | Change user role | ✅ Done |
| 15 | Disable user | ✅ Done |
| 16 | Project entity + ProjectRepository | ✅ Done |
| 17 | Create project | ✅ Done |
| 18 | ProjectMember relationship (entity + repository) | ✅ Done |
| 19 | Add project member | ✅ Done |
| 20 | Remove project member | ✅ Done |
| 21 | Get project members | ✅ Done |
| 22 | Get projects (ADMIN all, MANAGER managed, EMPLOYEE member-of) | ✅ Done |
| 23 | Get project by ID (object-level authorization) | ✅ Done |
| 24 | Update project | ✅ Done |
| 25 | Delete project | ✅ Done |
| 26 | Manager dashboard | ✅ Done |
| 27 | Admin dashboard | ✅ Done |
| 28 | Method-level security (@EnableMethodSecurity, @PreAuthorize) | ✅ Done |
| 29 | Permissions / authorities (USER_*, PROJECT_*) | ⬜ To Do |
| 30 | CSRF protection | ✅ Done |
| 31 | CORS configuration (credentialed requests) | ✅ Done |
| 32 | Cookie hardening (SameSite, HttpOnly, Secure) | ✅ Done |
| 33 | Account locking (failedLoginAttempts, accountNonLocked, lockedAt) | ✅ Done |
| 34 | Forgot password | ⬜ To Do |
| 35 | Reset password (revoke all refresh tokens) | ⬜ To Do |
| 36 | Session management: list and revoke sessions | ⬜ To Do |
| 37 | Logout all | ⬜ To Do |
| 38 | Audit logging (LOGIN_*, LOGOUT, TOKEN_REFRESH, PASSWORD_CHANGED, ROLE_CHANGED, ACCOUNT_DISABLED, PROJECT_*) | ⬜ To Do |
| 39 | Admin audit log | ⬜ To Do |

## Security Foundation & Authentication

### 01. Security foundation (SecurityFilterChain, JWT service/filter, cookies, error handling)

- **Status:** ✅ Done |  **Priority:** High  |  **Level:** 0
- **Depends on:** Nothing
- **Blocked by:** none

### 02. User entity + UserRepository

- **Status:** ✅ Done |  **Priority:** High  |  **Level:** 1
- **Depends on:** Database, JPA
- **Blocked by:** #1

### 03. User registration

- **Status:** ✅ Done  |  **Priority:** High  |  **Level:** 1
- **Endpoint:** `POST /api/auth/register`
- **Depends on:** User, UserRepository, PasswordEncoder
- **Access:** Public
- **Returns:** CreateUserResponse
- **Blocked by:** #2

### 04. User login

- **Status:** ✅ Done |  **Priority:** High  |  **Level:** 1
- **Endpoint:** `POST /api/auth/login`
- **Depends on:** User, UserDetailsService, AuthenticationManager, AuthenticationProvider, PasswordEncoder, JwtService
- **Access:** Public
- **Returns:** Access + Refresh token cookies
- **Blocked by:** #3

### 05. Refresh token subsystem + refresh endpoint

- **Status:** ✅ Done |  **Priority:** High  |  **Level:** 1
- **Endpoint:** `POST /api/auth/refresh`
- **Depends on:** User, Authentication, JWT, RefreshToken, RefreshTokenRepository, RefreshTokenService
- **Access:** Authenticated (refresh cookie)
- **Returns:** New access token
- **Blocked by:** #4

### 06. Refresh token rotation (validate A, revoke A, create B, issue new tokens)

- **Status:** ✅ Done |  **Priority:** High  |  **Level:** 1
- **Endpoint:** `POST /api/auth/refresh`
- **Depends on:** Refresh token subsystem
- **Access:** Authenticated (refresh cookie)
- **Returns:** New access + rotated refresh token
- **Blocked by:** #5

### 07. Logout

- **Status:** ✅ Done |  **Priority:** High  |  **Level:** 1
- **Endpoint:** `POST /api/auth/logout`
- **Depends on:** RefreshToken, RefreshTokenService, Cookies
- **Access:** Authenticated
- **Returns:** 204 No Content
- **Blocked by:** #5

## User Self-Service

### 08. Get my profile

- **Status:** ✅ Done |  **Priority:** High  |  **Level:** 2
- **Endpoint:** `GET /api/users/me`
- **Depends on:** User, Authentication, SecurityContext
- **Access:** Authenticated
- **Returns:** MyProfileResponse
- **Blocked by:** #7

### 09. Change my password (revoke existing sessions)

- **Status:** ✅ Done |  **Priority:** High  |  **Level:** 2
- **Endpoint:** `PATCH /api/users/me/password`
- **Depends on:** User, PasswordEncoder, Authentication, RefreshToken
- **Access:** Authenticated
- **Returns:** 204 No Content
- **Blocked by:** #7

### 10. Account enabled/disabled behavior (enabled, accountNonLocked, failedLoginAttempts -> UserDetails)

- **Status:** ✅ Done |  **Priority:** High  |  **Level:** 2
- **Depends on:** User, UserDetails
- **Blocked by:** #2

## User Management

### 11. Get all users

- **Status:** ✅ Done |  **Priority:** Medium  |  **Level:** 3
- **Endpoint:** `GET /api/users`
- **Depends on:** User, UserRepository, Role, Authorization
- **Access:** ADMIN
- **Returns:** List<UserResponse>
- **Blocked by:** #10

### 12. Get user by ID

- **Status:** ✅ Done |  **Priority:** Medium  |  **Level:** 3
- **Endpoint:** `GET /api/users/{id}`
- **Depends on:** User, UserRepository, Role, Authorization
- **Access:** ADMIN any; EMPLOYEE self; MANAGER later
- **Returns:** UserResponse
- **Blocked by:** #11

### 13. Create user

- **Status:** ✅ Done |  **Priority:** Medium  |  **Level:** 3
- **Endpoint:** `POST /api/users`
- **Depends on:** User, UserRepository, PasswordEncoder, Role, Authorization
- **Access:** ADMIN
- **Returns:** UserResponse
- **Blocked by:** #11

### 14. Change user role

- **Status:** ✅ Done |  **Priority:** Medium  |  **Level:** 3
- **Endpoint:** `PATCH /api/users/{id}/role`
- **Depends on:** User, Role, Authorization
- **Access:** ADMIN
- **Returns:** UserResponse
- **Blocked by:** #11

### 15. Disable user

- **Status:** ✅ Done |  **Priority:** Medium  |  **Level:** 3
- **Endpoint:** `PATCH /api/users/{id}/disable`
- **Depends on:** User, Account enabled/disabled, Authorization
- **Access:** ADMIN
- **Returns:** UserResponse
- **Blocked by:** #10, #11

## Projects & Membership

### 16. Project entity + ProjectRepository

- **Status:** ✅ Done |  **Priority:** Medium  |  **Level:** 4
- **Depends on:** User (manager)
- **Blocked by:** #15

### 17. Create project

- **Status:** ✅ Done |  **Priority:** Medium  |  **Level:** 4
- **Endpoint:** `POST /api/projects`
- **Depends on:** Project, User, Role, Authorization
- **Access:** ADMIN, MANAGER
- **Returns:** ProjectResponse
- **Blocked by:** #16

### 18. ProjectMember relationship (entity + repository)

- **Status:** ✅ Done  |  **Priority:** Medium  |  **Level:** 5
- **Depends on:** Project, User
- **Blocked by:** #16

### 19. Add project member

- **Status:** ✅ Done |  **Priority:** Medium  |  **Level:** 5
- **Endpoint:** `POST /api/projects/{projectId}/members/{userId}`
- **Depends on:** Project, User, ProjectMember, Authorization
- **Access:** ADMIN, Project Manager
- **Returns:** ProjectMemberResponse
- **Blocked by:** #18

### 20. Remove project member

- **Status:** ✅ Done |  **Priority:** Medium  |  **Level:** 5
- **Endpoint:** `DELETE /api/projects/{projectId}/members/{userId}`
- **Depends on:** Project, User, ProjectMember, Authorization
- **Access:** ADMIN, Project Manager
- **Returns:** 204 No Content
- **Blocked by:** #18

### 21. Get project members

- **Status:** ✅ Done |  **Priority:** Medium  |  **Level:** 5
- **Endpoint:** `GET /api/projects/{projectId}/members`
- **Depends on:** Project, ProjectMember, Authorization
- **Access:** ADMIN, MANAGER, Project Members
- **Returns:** List<ProjectMemberResponse>
- **Blocked by:** #18

### 22. Get projects (ADMIN all, MANAGER managed, EMPLOYEE member-of)

- **Status:** ✅ Done |  **Priority:** Medium  |  **Level:** 6
- **Endpoint:** `GET /api/projects`
- **Depends on:** Project, ProjectMember, Authorization
- **Access:** ADMIN, MANAGER, EMPLOYEE
- **Returns:** List<ProjectResponse>
- **Blocked by:** #18

### 23. Get project by ID (object-level authorization)

- **Status:** ✅ Done |  **Priority:** Medium  |  **Level:** 6
- **Endpoint:** `GET /api/projects/{id}`
- **Depends on:** Project, ProjectMember, Authorization
- **Access:** ADMIN any; MANAGER own; EMPLOYEE member
- **Returns:** ProjectResponse
- **Blocked by:** #22

### 24. Update project

- **Status:** ✅ Done |  **Priority:** Medium  |  **Level:** 6
- **Endpoint:** `PATCH /api/projects/{id}`
- **Depends on:** Project, User, ProjectMember, Object-level authorization
- **Access:** ADMIN any; MANAGER own; EMPLOYEE forbidden
- **Returns:** ProjectResponse
- **Blocked by:** #23

### 25. Delete project

- **Status:** ✅ Done  |  **Priority:** Medium  |  **Level:** 6
- **Endpoint:** `DELETE /api/projects/{id}`
- **Depends on:** Project, Authorization
- **Access:** ADMIN
- **Returns:** 204 No Content
- **Blocked by:** #23

## Dashboards

### 26. Manager dashboard

- **Status:** ✅ Done |  **Priority:** Medium  |  **Level:** 7
- **Endpoint:** `GET /api/manager/dashboard`
- **Depends on:** User, Project, ProjectMember
- **Access:** MANAGER
- **Returns:** ManagerDashboardResponse
- **Blocked by:** #22

### 27. Admin dashboard

- **Status:** ✅ Done |  **Priority:** Medium  |  **Level:** 8
- **Endpoint:** `GET /api/admin/dashboard`
- **Depends on:** User, Project, ProjectMember
- **Access:** ADMIN
- **Returns:** AdminDashboardResponse
- **Blocked by:** #22

## Authorization Hardening

### 28. Method-level security (@EnableMethodSecurity, @PreAuthorize)

- **Status:** ✅ Done  |  **Priority:** Low  |  **Level:** 9
- **Depends on:** All existing endpoints
- **Blocked by:** #27

### 29. Permissions / authorities (USER_*, PROJECT_*)

- **Status:** ⬜ To Do  |  **Priority:** Low  |  **Level:** 10
- **Depends on:** Method-level security, Role system
- **Blocked by:** #28

### 30. CSRF protection

- **Status:** ✅ Done  |  **Priority:** Low  |  **Level:** 11
- **Depends on:** Working auth flow
- **Blocked by:** #27

### 31. CORS configuration (credentialed requests)

- **Status:** ✅ Done  |  **Priority:** Low  |  **Level:** 11
- **Depends on:** Working auth flow
- **Blocked by:** #27

### 32. Cookie hardening (SameSite, HttpOnly, Secure)

- **Status:** ✅ Done  |  **Priority:** Low  |  **Level:** 11
- **Depends on:** Working auth flow
- **Blocked by:** #27

## Advanced Account Security

### 33. Account locking (failedLoginAttempts, accountNonLocked, lockedAt)

- **Status:** ✅ Done |  **Priority:** Low  |  **Level:** 12
- **Depends on:** User, Login, UserDetails
- **Blocked by:** #10, #4

### 34. Forgot password

- **Status:** ⬜ To Do  |  **Priority:** Low  |  **Level:** 12
- **Endpoint:** `POST /api/auth/forgot-password`
- **Depends on:** User
- **Access:** Public
- **Returns:** 204 No Content
- **Blocked by:** #9

### 35. Reset password (revoke all refresh tokens)

- **Status:** ⬜ To Do  |  **Priority:** Low  |  **Level:** 12
- **Endpoint:** `POST /api/auth/reset-password`
- **Depends on:** User, RefreshToken
- **Access:** Public (reset token)
- **Returns:** 204 No Content
- **Blocked by:** #34

### 36. Session management: list and revoke sessions

- **Status:** ⬜ To Do  |  **Priority:** Low  |  **Level:** 13
- **Endpoint:** `GET /api/auth/sessions; DELETE /api/auth/sessions/{id}`
- **Depends on:** RefreshToken
- **Access:** Authenticated
- **Returns:** List<SessionResponse> / 204
- **Blocked by:** #6

### 37. Logout all

- **Status:** ⬜ To Do  |  **Priority:** Low  |  **Level:** 13
- **Endpoint:** `POST /api/auth/logout-all`
- **Depends on:** RefreshToken
- **Access:** Authenticated
- **Returns:** 204 No Content
- **Blocked by:** #36

## Auditing

### 38. Audit logging (LOGIN_*, LOGOUT, TOKEN_REFRESH, PASSWORD_CHANGED, ROLE_CHANGED, ACCOUNT_DISABLED, PROJECT_*)

- **Status:** ⬜ To Do  |  **Priority:** Low  |  **Level:** 14
- **Depends on:** Auth, User, Project modules
- **Blocked by:** #27

### 39. Admin audit log

- **Status:** ⬜ To Do  |  **Priority:** Low  |  **Level:** 14
- **Endpoint:** `GET /api/admin/audit-logs`
- **Depends on:** Audit logging
- **Access:** ADMIN
- **Returns:** List<AuditLogResponse>
- **Blocked by:** #38